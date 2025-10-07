import { useState, useEffect, useCallback, useRef } from 'react';

interface UsePaginatedDataOptions<T> {
  fetchFunction: (page: number, size: number, filters?: any) => Promise<{
    content: T[];
    totalElements: number;
    page: number;
    size: number;
    totalPages: number;
  }>;
  initialPage?: number;
  initialPageSize?: number;
  initialFilters?: any;
  debounceMs?: number;
}

interface UsePaginatedDataReturn<T> {
  data: T[];
  loading: boolean;
  error: string | null;
  currentPage: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
  setPage: (page: number) => void;
  setPageSize: (size: number) => void;
  setFilters: (filters: any) => void;
  refresh: () => void;
}

export const usePaginatedData = <T>({
  fetchFunction,
  initialPage = 0,
  initialPageSize = 50,
  initialFilters = {},
  debounceMs = 300
}: UsePaginatedDataOptions<T>): UsePaginatedDataReturn<T> => {
  const [data, setData] = useState<T[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(initialPage);
  const [pageSize, setPageSize] = useState(initialPageSize);
  const [filters, setFilters] = useState(initialFilters);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  
  const debounceTimeoutRef = useRef<NodeJS.Timeout>();
  const abortControllerRef = useRef<AbortController>();

  const fetchData = useCallback(async (page: number, size: number, currentFilters: any) => {
    // Cancel previous request
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }
    
    abortControllerRef.current = new AbortController();
    
    try {
      setLoading(true);
      setError(null);
      
      const result = await fetchFunction(page, size, currentFilters);
      
      if (!abortControllerRef.current.signal.aborted) {
        setData(result.content);
        setTotalElements(result.totalElements);
        setTotalPages(result.totalPages);
      }
    } catch (err: any) {
      if (!abortControllerRef.current.signal.aborted) {
        setError(err.message || 'Failed to fetch data');
        console.error('Error fetching paginated data:', err);
      }
    } finally {
      if (!abortControllerRef.current.signal.aborted) {
        setLoading(false);
      }
    }
  }, [fetchFunction]);

  const debouncedFetchData = useCallback((page: number, size: number, currentFilters: any) => {
    if (debounceTimeoutRef.current) {
      clearTimeout(debounceTimeoutRef.current);
    }
    
    debounceTimeoutRef.current = setTimeout(() => {
      fetchData(page, size, currentFilters);
    }, debounceMs);
  }, [fetchData, debounceMs]);

  const handleSetPage = useCallback((page: number) => {
    setCurrentPage(page);
    debouncedFetchData(page, pageSize, filters);
  }, [pageSize, filters, debouncedFetchData]);

  const handleSetPageSize = useCallback((size: number) => {
    setPageSize(size);
    setCurrentPage(0); // Reset to first page when changing page size
    debouncedFetchData(0, size, filters);
  }, [filters, debouncedFetchData]);

  const handleSetFilters = useCallback((newFilters: any) => {
    setFilters(newFilters);
    setCurrentPage(0); // Reset to first page when changing filters
    debouncedFetchData(0, pageSize, newFilters);
  }, [pageSize, debouncedFetchData]);

  const refresh = useCallback(() => {
    fetchData(currentPage, pageSize, filters);
  }, [currentPage, pageSize, filters, fetchData]);

  // Initial load
  useEffect(() => {
    fetchData(currentPage, pageSize, filters);
    
    return () => {
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }
      if (debounceTimeoutRef.current) {
        clearTimeout(debounceTimeoutRef.current);
      }
    };
  }, []);

  return {
    data,
    loading,
    error,
    currentPage,
    pageSize,
    totalElements,
    totalPages,
    hasNext: currentPage < totalPages - 1,
    hasPrevious: currentPage > 0,
    setPage: handleSetPage,
    setPageSize: handleSetPageSize,
    setFilters: handleSetFilters,
    refresh
  };
};

export default usePaginatedData;
