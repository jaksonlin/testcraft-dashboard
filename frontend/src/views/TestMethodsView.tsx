import React, { useState, useMemo, useEffect, useCallback } from 'react';
import { CheckCircle, XCircle, ExternalLink, FileText, Target, Eye } from 'lucide-react';
import { api, type TestMethodDetail, type TestMethodSource } from '../lib/api';
import PaginatedTable, { type ColumnDef } from '../components/shared/PaginatedTable';
import { usePaginatedData } from '../hooks/usePaginatedData';
import { isMethodAnnotated, getAnnotationStatusDisplayName } from '../utils/methodUtils';
import TestMethodsHeader from '../components/test-methods/TestMethodsHeader';
import { useSearchParams } from 'react-router-dom';
import { HighlightedText } from '../components/shared/HighlightedText';
import { useKeyboardShortcuts } from '../hooks/useKeyboardShortcuts';
import TestMethodSourceViewer from '../components/test-methods/TestMethodSourceViewer';

const TestMethodsView: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();

  // Initialize filters from URL query parameters
  const getInitialFilters = () => {
    return {
      teamName: searchParams.get('team') || '',
      repositoryName: searchParams.get('repo') || '',
      packageName: searchParams.get('package') || '',
      className: searchParams.get('class') || '',
      annotated: searchParams.get('annotated') === 'true' ? true :
        searchParams.get('annotated') === 'false' ? false : undefined,
      codePattern: searchParams.get('code') || ''
    };
  };

  const [filters, setFilters] = useState(getInitialFilters);

  // Global statistics (not per-page)
  const [globalStats, setGlobalStats] = useState({
    totalMethods: 0,
    totalAnnotated: 0,
    totalNotAnnotated: 0,
    coverageRate: 0
  });
  const [sourceModalOpen, setSourceModalOpen] = useState(false);
  const [sourceLoading, setSourceLoading] = useState(false);
  const [sourceError, setSourceError] = useState<string | null>(null);
  const [selectedSource, setSelectedSource] = useState<TestMethodSource | null>(null);

  const {
    data: testMethods,
    loading,
    error,
    currentPage,
    pageSize,
    totalElements,
    setPage,
    setPageSize,
    setFilters: setDataFilters,
    refresh
  } = usePaginatedData({
    fetchFunction: async (page, size, currentFilters) => {
      const filtersToUse = currentFilters || filters;
      const response = await api.dashboard.getTestMethodDetailsPaginated(
        page,
        size,
        undefined, // organization filter removed
        filtersToUse.teamName || undefined,
        filtersToUse.repositoryName || undefined,
        filtersToUse.packageName || undefined,
        filtersToUse.className || undefined,
        filtersToUse.annotated,
        filtersToUse.codePattern || undefined
      );
      return {
        content: response.content,
        totalElements: response.totalElements,
        page: response.page,
        size: response.size,
        totalPages: response.totalPages
      };
    },
    initialPageSize: 50,
    initialFilters: filters
  });

  // Load global statistics
  React.useEffect(() => {
    const loadGlobalStats = async () => {
      try {
        const stats = await api.dashboard.getGlobalTestMethodStats(
          undefined,
          undefined,
          filters.repositoryName || undefined,
          filters.annotated
        );
        setGlobalStats(stats);
      } catch (error) {
        console.error('Failed to load global stats:', error);
      }
    };
    loadGlobalStats();
  }, [filters]);

  const handleFilterChange = (newFilters: Partial<typeof filters>) => {
    const updatedFilters = { ...filters, ...newFilters };
    setFilters(updatedFilters);
    setDataFilters(updatedFilters);
  };

  // Clear all filters
  const clearAllFilters = useCallback(() => {
    const emptyFilters = {
      teamName: '',
      repositoryName: '',
      packageName: '',
      className: '',
      annotated: undefined as boolean | undefined,
      codePattern: ''
    };
    setFilters(emptyFilters);
    setDataFilters(emptyFilters);
  }, [setDataFilters]);

  const handleViewSource = useCallback(async (methodId: number) => {
    setSourceModalOpen(true);
    setSourceLoading(true);
    setSourceError(null);
    setSelectedSource(null);

    try {
      const source = await api.dashboard.getTestMethodSource(methodId);
      setSelectedSource(source);
    } catch (err) {
      console.error('Failed to load test class source', err);
      setSourceError('Unable to load test class content right now.');
    } finally {
      setSourceLoading(false);
    }
  }, []);

  const handleCloseSource = useCallback(() => {
    setSourceModalOpen(false);
    setSourceError(null);
    setSelectedSource(null);
    setSourceLoading(false);
  }, []);

  // Keyboard shortcuts for power users
  useKeyboardShortcuts([
    {
      key: 'ArrowRight',
      callback: () => {
        const totalPages = Math.ceil(totalElements / pageSize);
        if (currentPage < totalPages - 1) {
          setPage(currentPage + 1);
        }
      },
      description: 'Next page'
    },
    {
      key: 'ArrowLeft',
      callback: () => {
        if (currentPage > 0) {
          setPage(currentPage - 1);
        }
      },
      description: 'Previous page'
    },
    {
      key: 'PageDown',
      callback: () => {
        const totalPages = Math.ceil(totalElements / pageSize);
        if (currentPage < totalPages - 1) {
          setPage(currentPage + 1);
        }
      },
      description: 'Next page'
    },
    {
      key: 'PageUp',
      callback: () => {
        if (currentPage > 0) {
          setPage(currentPage - 1);
        }
      },
      description: 'Previous page'
    },
    {
      key: '/',
      ctrlKey: true,
      callback: clearAllFilters,
      description: 'Clear all filters'
    },
    {
      key: 'r',
      ctrlKey: true,
      callback: refresh,
      description: 'Refresh data'
    }
  ]);

  // Update URL when filters change (makes filters bookmarkable and shareable)
  useEffect(() => {
    const params = new URLSearchParams();

    if (filters.teamName) params.set('team', filters.teamName);
    if (filters.repositoryName) params.set('repo', filters.repositoryName);
    if (filters.packageName) params.set('package', filters.packageName);
    if (filters.className) params.set('class', filters.className);
    if (filters.annotated !== undefined) params.set('annotated', filters.annotated.toString());
    if (filters.codePattern) params.set('code', filters.codePattern);

    // Update URL without triggering navigation
    setSearchParams(params, { replace: true });
  }, [filters, setSearchParams]);


  const columns: ColumnDef<TestMethodDetail>[] = useMemo(() => [
    {
      key: 'repository',
      header: 'Repository',
      render: (method) => (
        <div className="flex items-center space-x-2">
          <HighlightedText
            text={method.repository}
            highlight={filters.repositoryName}
            className="font-medium text-gray-900 dark:text-gray-100"
          />
          <a
            href={method.gitUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="text-blue-600 dark:text-blue-400 hover:text-blue-800 dark:hover:text-blue-300"
            onClick={(e) => e.stopPropagation()}
          >
            <ExternalLink className="h-3 w-3" />
          </a>
        </div>
      ),
      sortable: true,
      width: '150px'
    },
    {
      key: 'testClass',
      header: 'Test Class',
      render: (method) => (
        <HighlightedText
          text={method.testClass}
          highlight={filters.className || filters.packageName}
          className="text-gray-900 dark:text-gray-100 font-mono text-sm"
        />
      ),
      sortable: true,
      width: '200px'
    },
    {
      key: 'testMethod',
      header: 'Test Method',
      render: (method) => (
        <HighlightedText
          text={method.testMethod}
          highlight={filters.className}
          className="text-gray-900 dark:text-gray-100 font-mono text-sm"
        />
      ),
      sortable: true,
      width: '150px'
    },
    {
      key: 'annotation',
      header: 'Annotation Status',
      render: (method) => {
        const isAnnotated = isMethodAnnotated(method);
        return (
          <div className="flex items-center space-x-2">
            {isAnnotated ? (
              <CheckCircle className="h-4 w-4 text-green-600 dark:text-green-400" />
            ) : (
              <XCircle className="h-4 w-4 text-red-600 dark:text-red-400" />
            )}
            <span className={`text-sm ${isAnnotated
                ? 'text-green-600 dark:text-green-400'
                : 'text-red-600 dark:text-red-400'
              }`}>
              {getAnnotationStatusDisplayName(method)}
            </span>
          </div>
        );
      },
      sortable: true,
      width: '150px'
    },
    {
      key: 'title',
      header: 'Title',
      render: (method) => (
        <HighlightedText
          text={method.title || 'No title'}
          highlight={filters.className || filters.packageName}
          className="text-gray-900 dark:text-gray-100 truncate max-w-xs inline-block"
        />
      ),
      sortable: true,
      width: '200px'
    },
    {
      key: 'author',
      header: 'Author',
      render: (method) => (
        <span className="text-gray-700 dark:text-gray-300">
          {method.author || 'Unknown'}
        </span>
      ),
      sortable: true,
      width: '120px'
    },
    {
      key: 'status',
      header: 'Test Status',
      render: (method) => (
        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${method.status === 'PASS' ? 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300' :
            method.status === 'FAIL' ? 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-300' :
              'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-300'
          }`}>
          {method.status || 'UNKNOWN'}
        </span>
      ),
      sortable: true,
      width: '120px'
    },
    {
      key: 'teamName',
      header: 'Team',
      render: (method) => (
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-300">
          {method.teamName}
        </span>
      ),
      sortable: true,
      width: '100px'
    },
    {
      key: 'actions',
      header: 'Actions',
      render: (method) => (
        <button
          onClick={(event) => {
            event.stopPropagation();
            handleViewSource(method.id);
          }}
          className="inline-flex items-center px-3 py-1 text-xs font-medium text-blue-600 dark:text-blue-400 border border-blue-200 dark:border-blue-700 rounded-md hover:bg-blue-50 dark:hover:bg-blue-900/40 transition-colors"
        >
          <Eye className="h-4 w-4 mr-1" />
          View
        </button>
      ),
      sortable: false,
      width: '110px'
    }
  ], [filters.repositoryName, filters.className, filters.packageName, handleViewSource]);

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center">
        <div className="text-center">
          <XCircle className="h-12 w-12 text-red-500 mx-auto mb-4" />
          <h2 className="text-xl font-semibold text-gray-900 dark:text-gray-100 mb-2">
            Error Loading Test Methods
          </h2>
          <p className="text-gray-600 dark:text-gray-400 mb-4">{error}</p>
          <button
            onClick={refresh}
            className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
          >
            Try Again
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      {/* Header */}
      <TestMethodsHeader
        loading={loading}
        filters={filters}
        onRefresh={refresh}
        pageSize={pageSize}
        onPageSizeChange={setPageSize}
      />

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">

        {/* Filters */}
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6 mb-6">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Filters</h3>

          {/* Primary Filters */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Team
              </label>
              <input
                type="text"
                placeholder="Filter by team name..."
                value={filters.teamName}
                onChange={(e) => handleFilterChange({ teamName: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Repository
              </label>
              <input
                type="text"
                placeholder="Filter by repository..."
                value={filters.repositoryName}
                onChange={(e) => handleFilterChange({ repositoryName: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
          </div>

          {/* Secondary Filters */}
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Package
              </label>
              <input
                type="text"
                placeholder="com.acme.tests..."
                value={filters.packageName}
                onChange={(e) => handleFilterChange({ packageName: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Class Name
              </label>
              <input
                type="text"
                placeholder="UserService..."
                value={filters.className}
                onChange={(e) => handleFilterChange({ className: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Code Pattern
              </label>
              <input
                type="text"
                placeholder="Filter by target class/method..."
                value={filters.codePattern}
                onChange={(e) => handleFilterChange({ codePattern: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                title="Filter by the class or method being tested (target class/method)"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Annotation Status
              </label>
              <select
                value={filters.annotated === undefined ? 'all' : filters.annotated ? 'annotated' : 'not-annotated'}
                onChange={(e) => {
                  const value = e.target.value;
                  handleFilterChange({
                    annotated: value === 'all' ? undefined : value === 'annotated'
                  });
                }}
                className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="all">All Methods</option>
                <option value="annotated">Annotated Only</option>
                <option value="not-annotated">Not Annotated</option>
              </select>
            </div>
          </div>

          {/* Active Filters Display */}
          {(filters.teamName || filters.repositoryName || filters.packageName || filters.className || filters.codePattern || filters.annotated !== undefined) && (
            <div className="mt-4 flex items-center gap-2 flex-wrap">
              <span className="text-sm text-gray-600 dark:text-gray-400">Active filters:</span>
              {filters.teamName && (
                <span className="px-2 py-1 bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-300 rounded text-xs">
                  Team: {filters.teamName}
                </span>
              )}
              {filters.repositoryName && (
                <span className="px-2 py-1 bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-300 rounded text-xs">
                  Repo: {filters.repositoryName}
                </span>
              )}
              {filters.packageName && (
                <span className="px-2 py-1 bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-300 rounded text-xs">
                  Package: {filters.packageName}
                </span>
              )}
              {filters.className && (
                <span className="px-2 py-1 bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-300 rounded text-xs">
                  Class: {filters.className}
                </span>
              )}
              {filters.codePattern && (
                <span className="px-2 py-1 bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-300 rounded text-xs">
                  Code: {filters.codePattern}
                </span>
              )}
              {filters.annotated !== undefined && (
                <span className="px-2 py-1 bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-300 rounded text-xs">
                  {filters.annotated ? 'Annotated' : 'Not Annotated'}
                </span>
              )}
              <button
                onClick={clearAllFilters}
                className="px-2 py-1 text-xs text-red-600 dark:text-red-400 hover:underline"
                title="Clear all filters (Ctrl+/)"
              >
                Clear all
              </button>
            </div>
          )}
        </div>

        {/* Stats */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-6">
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="p-2 bg-blue-100 dark:bg-blue-900/30 rounded-lg">
                <FileText className="h-6 w-6 text-blue-600 dark:text-blue-400" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Total Methods</p>
                <p className="text-2xl font-semibold text-gray-900 dark:text-gray-100">
                  {globalStats.totalMethods.toLocaleString()}
                </p>
                <p className="text-xs text-gray-500 dark:text-gray-500">
                  Showing {testMethods.length} on page
                </p>
              </div>
            </div>
          </div>
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="p-2 bg-green-100 dark:bg-green-900/30 rounded-lg">
                <CheckCircle className="h-6 w-6 text-green-600 dark:text-green-400" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Annotated</p>
                <p className="text-2xl font-semibold text-gray-900 dark:text-gray-100">
                  {globalStats.totalAnnotated.toLocaleString()}
                </p>
                <p className="text-xs text-gray-500 dark:text-gray-500">
                  {testMethods.filter(isMethodAnnotated).length} on page
                </p>
              </div>
            </div>
          </div>
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="p-2 bg-red-100 dark:bg-red-900/30 rounded-lg">
                <XCircle className="h-6 w-6 text-red-600 dark:text-red-400" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Not Annotated</p>
                <p className="text-2xl font-semibold text-gray-900 dark:text-gray-100">
                  {globalStats.totalNotAnnotated.toLocaleString()}
                </p>
                <p className="text-xs text-gray-500 dark:text-gray-500">
                  {testMethods.filter(m => !isMethodAnnotated(m)).length} on page
                </p>
              </div>
            </div>
          </div>
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <div className="flex items-center">
              <div className="p-2 bg-purple-100 dark:bg-purple-900/30 rounded-lg">
                <Target className="h-6 w-6 text-purple-600 dark:text-purple-400" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Coverage Rate</p>
                <p className="text-2xl font-semibold text-gray-900 dark:text-gray-100">
                  {globalStats.coverageRate.toFixed(1)}%
                </p>
                <p className="text-xs text-gray-500 dark:text-gray-500">
                  Global coverage
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Table */}
        <PaginatedTable
          data={testMethods}
          totalItems={totalElements}
          currentPage={currentPage}
          pageSize={pageSize}
          onPageChange={setPage}
          columns={columns}
          loading={loading}
          searchable={false}
          className="shadow-lg"
        />

        {/* Keyboard Shortcuts Help */}
        <div className="mt-4 bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg p-3">
          <p className="text-xs text-blue-900 dark:text-blue-100">
            <strong>⌨️ Keyboard Shortcuts:</strong>
            <span className="ml-2">←/→ or PageUp/PageDown: Navigate pages</span>
            <span className="ml-3">Ctrl+/: Clear filters</span>
            <span className="ml-3">Ctrl+R: Refresh</span>
          </p>
        </div>
      </main>

      <TestMethodSourceViewer
        isOpen={sourceModalOpen}
        loading={sourceLoading}
        error={sourceError}
        source={selectedSource}
        onClose={handleCloseSource}
      />
    </div>
  );
};

export default TestMethodsView;
