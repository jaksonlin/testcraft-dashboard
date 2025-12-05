import { useState, useEffect, useCallback } from 'react';
import {
  getAllTestCases,
  getCoverageStats,
  getUntestedCases,
  deleteTestCase
} from '../lib/testCaseApi';
import type { TestCase, CoverageStats } from '../lib/testCaseApi';

export type TabType = 'list' | 'coverage' | 'gaps';

interface PaginationState {
  page: number;
  pageSize: number;
  totalPages: number;
}

export interface TestCaseFilters {
  teamId?: number;
  type?: string;
  priority?: string;
  status?: string;
  search?: string;
}

interface TestCaseDataState {
  testCases: TestCase[];
  untestedCases: TestCase[];
  coverageStats: CoverageStats | null;
  listPagination: PaginationState;
  gapsPagination: PaginationState;
  filters: TestCaseFilters;
  loading: boolean;
  error: string | null;
}

interface UseTestCaseDataReturn extends TestCaseDataState {
  loadData: () => Promise<void>;
  loadTestCases: () => Promise<void>;
  loadGaps: () => Promise<void>;
  handleDelete: (internalId: number) => Promise<void>;
  setListPage: (page: number) => void;
  setListPageSize: (size: number) => void;
  setGapsPage: (page: number) => void;
  setGapsPageSize: (size: number) => void;
  setFilters: (filters: TestCaseFilters) => void;
  clearError: () => void;
}

/**
 * Custom hook for managing test case data, pagination, and operations
 */
export const useTestCaseData = (): UseTestCaseDataReturn => {
  const [state, setState] = useState<TestCaseDataState>({
    testCases: [],
    untestedCases: [],
    coverageStats: null,
    listPagination: { page: 0, pageSize: 20, totalPages: 0 },
    gapsPagination: { page: 0, pageSize: 20, totalPages: 0 },
    filters: {},
    loading: true,
    error: null,
  });

  // Load test cases with pagination and filters
  const loadTestCases = useCallback(async () => {
    try {
      const testCasesData = await getAllTestCases({
        page: state.listPagination.page,
        size: state.listPagination.pageSize,
        ...state.filters
      });
      
      setState(prev => ({
        ...prev,
        testCases: testCasesData.content,
        listPagination: {
          ...prev.listPagination,
          totalPages: Math.ceil(testCasesData.total / prev.listPagination.pageSize)
        },
        error: null
      }));
    } catch (error) {
      console.error('Failed to load test cases:', error);
      setState(prev => ({
        ...prev,
        error: 'Failed to load test cases'
      }));
    }
  }, [state.listPagination.page, state.listPagination.pageSize, state.filters]);

  // Load gaps with pagination
  const loadGaps = useCallback(async () => {
    try {
      const gaps = await getUntestedCases({
        page: state.gapsPagination.page,
        size: state.gapsPagination.pageSize,
        ...state.filters
      });
      
      setState(prev => ({
        ...prev,
        untestedCases: gaps.content,
        gapsPagination: {
          ...prev.gapsPagination,
          totalPages: Math.ceil(gaps.total / prev.gapsPagination.pageSize)
        },
        error: null
      }));
    } catch (error) {
      console.error('Failed to load gaps:', error);
      setState(prev => ({
        ...prev,
        error: 'Failed to load automation gaps'
      }));
    }
  }, [state.gapsPagination.page, state.gapsPagination.pageSize, state.filters]);

  // Load all data
  const loadData = useCallback(async () => {
    setState(prev => ({ ...prev, loading: true, error: null }));
    
    try {
      const [testCasesData, stats, gaps] = await Promise.all([
        getAllTestCases({
          page: state.listPagination.page,
          size: state.listPagination.pageSize,
          ...state.filters
        }),
        getCoverageStats(),
        getUntestedCases({
          page: state.gapsPagination.page,
          size: state.gapsPagination.pageSize
        })
      ]);

      setState(prev => ({
        ...prev,
        testCases: testCasesData.content,
        coverageStats: stats,
        untestedCases: gaps.content,
        listPagination: {
          ...prev.listPagination,
          totalPages: Math.ceil(testCasesData.total / prev.listPagination.pageSize)
        },
        gapsPagination: {
          ...prev.gapsPagination,
          totalPages: Math.ceil(gaps.total / prev.gapsPagination.pageSize)
        },
        loading: false,
        error: null
      }));
    } catch (error) {
      console.error('Failed to load test cases:', error);
      setState(prev => ({
        ...prev,
        loading: false,
        error: 'Failed to load test case data'
      }));
    }
  }, [state.listPagination.page, state.listPagination.pageSize, state.gapsPagination.page, state.gapsPagination.pageSize, state.filters]);

  // Delete test case
  const handleDelete = useCallback(async (internalId: number) => {
    try {
      await deleteTestCase(internalId);
      await loadData();
    } catch (error) {
      console.error('Failed to delete test case:', error);
      setState(prev => ({
        ...prev,
        error: 'Failed to delete test case'
      }));
      throw error; // Re-throw for component to handle
    }
  }, [loadData]);

  // Filter setter
  const setFilters = useCallback((filters: TestCaseFilters) => {
    setState(prev => ({
      ...prev,
      filters,
      listPagination: { ...prev.listPagination, page: 0 } // Reset to first page when filters change
    }));
  }, []);

  // Pagination setters
  const setListPage = useCallback((page: number) => {
    setState(prev => ({
      ...prev,
      listPagination: { ...prev.listPagination, page }
    }));
  }, []);

  const setListPageSize = useCallback((pageSize: number) => {
    setState(prev => ({
      ...prev,
      listPagination: { ...prev.listPagination, pageSize, page: 0 }
    }));
  }, []);

  const setGapsPage = useCallback((page: number) => {
    setState(prev => ({
      ...prev,
      gapsPagination: { ...prev.gapsPagination, page }
    }));
  }, []);

  const setGapsPageSize = useCallback((pageSize: number) => {
    setState(prev => ({
      ...prev,
      gapsPagination: { ...prev.gapsPagination, pageSize, page: 0 }
    }));
  }, []);

  const clearError = useCallback(() => {
    setState(prev => ({ ...prev, error: null }));
  }, []);

  // Load data on mount only
  useEffect(() => {
    loadData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // Empty deps - only run once on mount

  // Reload when filters or pagination change (but not on mount)
  useEffect(() => {
    // Skip if this is initial load (loading is true)
    if (state.loading) {
      return;
    }
    
    loadTestCases();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [state.filters, state.listPagination.page, state.listPagination.pageSize]); // Don't include loadTestCases to avoid recreation cycles

  return {
    ...state,
    loadData,
    loadTestCases,
    loadGaps,
    handleDelete,
    setListPage,
    setListPageSize,
    setGapsPage,
    setGapsPageSize,
    setFilters,
    clearError,
  };
};
