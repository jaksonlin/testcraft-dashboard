import React, { useState, useMemo } from 'react';
import { CheckCircle, XCircle, ExternalLink, FileText, Target } from 'lucide-react';
import { api, type TestMethodDetail } from '../lib/api';
import PaginatedTable, { type ColumnDef } from '../components/shared/PaginatedTable';
import { usePaginatedData } from '../hooks/usePaginatedData';
import { isMethodAnnotated, getAnnotationStatusDisplayName } from '../utils/methodUtils';
import TestMethodsHeader from '../components/test-methods/TestMethodsHeader';

const TestMethodsView: React.FC = () => {
  const [filters, setFilters] = useState({
    teamName: '',
    repositoryName: '',
    annotated: undefined as boolean | undefined
  });
  
  // Global statistics (not per-page)
  const [globalStats, setGlobalStats] = useState({
    totalMethods: 0,
    totalAnnotated: 0,
    totalNotAnnotated: 0,
    coverageRate: 0
  });

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
      const response = await api.dashboard.getTestMethodDetailsPaginated(
        page,
        size,
        currentFilters.teamName || undefined,
        currentFilters.repositoryName || undefined,
        currentFilters.annotated
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


  const columns: ColumnDef<TestMethodDetail>[] = useMemo(() => [
    {
      key: 'repository',
      header: 'Repository',
      render: (method) => (
        <div className="flex items-center space-x-2">
          <span className="font-medium text-gray-900 dark:text-gray-100">
            {method.repository}
          </span>
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
      width: '200px'
    },
    {
      key: 'testClass',
      header: 'Test Class',
      render: (method) => (
        <span className="text-gray-900 dark:text-gray-100 font-mono text-sm">
          {method.testClass}
        </span>
      ),
      sortable: true,
      width: '250px'
    },
    {
      key: 'testMethod',
      header: 'Test Method',
      render: (method) => (
        <span className="text-gray-900 dark:text-gray-100 font-mono text-sm">
          {method.testMethod}
        </span>
      ),
      sortable: true,
      width: '200px'
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
            <span className={`text-sm ${
              isAnnotated 
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
        <span className="text-gray-900 dark:text-gray-100 truncate max-w-xs">
          {method.title || 'No title'}
        </span>
      ),
      sortable: true,
      width: '300px'
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
        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
          method.status === 'PASS' ? 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300' :
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
    }
  ], []);

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
      />

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">

        {/* Filters */}
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6 mb-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Team Filter
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
                Repository Filter
              </label>
              <input
                type="text"
                placeholder="Filter by repository name..."
                value={filters.repositoryName}
                onChange={(e) => handleFilterChange({ repositoryName: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
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
          onPageSizeChange={setPageSize}
          columns={columns}
          loading={loading}
          searchable={false}
          className="shadow-lg"
        />
      </main>
    </div>
  );
};

export default TestMethodsView;
