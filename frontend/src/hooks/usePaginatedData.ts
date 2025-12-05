import { useState, useEffect, useCallback, useRef } from 'react';

interface UsePaginatedDataOptions<T, F = Record<string, unknown>> {
  fetchFunction: (page: number, size: number, filters?: F) => Promise<{
    content: T[];
    totalElements: number;
    page: number;
    size: number;
    totalPages: number;
  }>;
  initialPage?: number;
  initialPageSize?: number;
  initialFilters?: F;
  debounceMs?: number;
}

interface UsePaginatedDataReturn<T, F = Record<string, unknown>> {
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
  setFilters: (filters: F) => void;
  refresh: () => void;
}

export const usePaginatedData = <T, F = Record<string, unknown>>({
  fetchFunction,
  initialPage = 0,
  initialPageSize = 50,
  initialFilters = {} as F,
  debounceMs = 300
}: UsePaginatedDataOptions<T, F>): UsePaginatedDataReturn<T, F> => {
  const [data, setData] = useState<T[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(initialPage);
  const [pageSize, setPageSize] = useState(initialPageSize);
  const [filters, setFilters] = useState<F>(initialFilters);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const debounceTimeoutRef = useRef<ReturnType<typeof setTimeout> | undefined>(undefined);
  const abortControllerRef = useRef<AbortController | undefined>(undefined);

  const fetchData = useCallback(async (page: number, size: number, currentFilters: F) => {
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
    } catch (err: unknown) {
      if (!abortControllerRef.current?.signal.aborted) {
        const errorMessage = err instanceof Error ? err.message : 'Failed to fetch data';
        setError(errorMessage);
        console.error('Error fetching paginated data:', err);
      }
    } finally {
      if (!abortControllerRef.current?.signal.aborted) {
        setLoading(false);
      }
    }
  }, [fetchFunction]);

  const debouncedFetchData = useCallback((page: number, size: number, currentFilters: F) => {
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

  const handleSetFilters = useCallback((newFilters: F) => {
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
    // eslint-disable-next-line react-hooks/exhaustive-deps
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
