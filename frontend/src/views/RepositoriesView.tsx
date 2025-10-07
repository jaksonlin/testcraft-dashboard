import React, { useState, useCallback, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { FolderOpen, Download, AlertCircle, CheckCircle, Play, Trash2, RefreshCw, Columns, ChevronLeft, ChevronRight } from 'lucide-react';
import RepositoryList from '../components/repositories/RepositoryList';
import AdvancedFilter, { type FilterOption } from '../components/shared/AdvancedFilter';
import BulkOperations, { type BulkAction } from '../components/shared/BulkOperations';
import ExportManager, { type ExportOption } from '../components/shared/ExportManager';
import ColumnManager from '../components/shared/ColumnManager';
import DataControls, { type SortOption } from '../components/shared/DataControls';
import { useBulkOperations } from '../hooks/useBulkOperations';
import { exportData as exportDataUtil, prepareRepositoryExportData, type ExportScope } from '../utils/exportUtils';
import { api, type RepositorySummary, type PagedResponse } from '../lib/api';

const RepositoriesView: React.FC = () => {
  const navigate = useNavigate();
  const [scanning, setScanning] = useState(false);
  const [scanResults, setScanResults] = useState<{ success: number; failed: number } | null>(null);
  const [repositories, setRepositories] = useState<RepositorySummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [teamOptions, setTeamOptions] = useState<{ value: string; label: string }[]>([]);
  const [columnManagerOpen, setColumnManagerOpen] = useState(false);

  // Pagination state
  const [pagination, setPagination] = useState<PagedResponse<RepositorySummary> | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sortBy, setSortBy] = useState<string>('name');
  const [sortOrder, setSortOrder] = useState<string>('asc');

  // Advanced filtering
  const [searchTerm, setSearchTerm] = useState('');
  const [filters, setFilters] = useState<Record<string, string | number | boolean | null>>({});

  // Column definitions for the repositories table
  const repositoryColumns = [
    { id: 'select', label: 'Select', required: false },
    { id: 'repository', label: 'Repository Name', required: true },
    { id: 'team', label: 'Team', required: true },
    { id: 'testClasses', label: 'Test Classes', required: false },
    { id: 'testMethods', label: 'Test Methods', required: false },
    { id: 'coverage', label: 'Coverage', required: true },
    { id: 'lastScan', label: 'Last Scan', required: false },
    { id: 'actions', label: 'Actions', required: true },
  ];

  // Sort options configuration
  const sortOptions: SortOption[] = [
    { value: 'name', label: 'Name' },
    { value: 'team', label: 'Team' },
    { value: 'coverage', label: 'Coverage' },
    { value: 'testmethods', label: 'Test Methods' },
    { value: 'lastscan', label: 'Last Scan' }
  ];

  // Advanced filtering configuration
  const filterOptions: FilterOption[] = React.useMemo(() => [
    {
      id: 'team',
      label: 'Team',
      type: 'select',
      options: teamOptions
    },
    {
      id: 'coverage',
      label: 'Coverage Range',
      type: 'select',
      options: [
        { value: 'high', label: 'High (≥80%)' },
        { value: 'medium', label: 'Medium (50-79%)' },
        { value: 'low', label: 'Low (<50%)' }
      ]
    },
    {
      id: 'testMethods',
      label: 'Test Methods Range',
      type: 'select',
      options: [
        { value: 'high', label: 'High (≥100)' },
        { value: 'medium', label: 'Medium (20-99)' },
        { value: 'low', label: 'Low (<20)' }
      ]
    },
    {
      id: 'lastScan',
      label: 'Last Scan',
      type: 'select',
      options: [
        { value: 'today', label: 'Today' },
        { value: 'week', label: 'This Week' },
        { value: 'month', label: 'This Month' },
        { value: 'older', label: 'Older' }
      ]
    }
  ], [teamOptions]);

  // Fetch paginated repositories
  const fetchRepositories = useCallback(async () => {
    try {
      setLoading(true);
      const response = await api.repositories.getPaginated(
        currentPage, 
        pageSize, 
        searchTerm || undefined, 
        filters.team as string || undefined, 
        filters.coverage as string || undefined,
        filters.testMethods as string || undefined,
        filters.lastScan as string || undefined,
        sortBy, 
        sortOrder
      );
      
      setPagination(response);
      setRepositories(response.content);
      
      // Update team filter options from all available teams
      // For now, we'll get teams from the current page, but ideally we'd have a separate teams endpoint
      const uniqueTeams = Array.from(new Set(response.content.map(repo => repo.teamName)))
        .sort()
        .map(team => ({ value: team, label: team }));
      
      setTeamOptions(uniqueTeams);
    } catch (err) {
      console.error('Error fetching repositories:', err);
    } finally {
      setLoading(false);
    }
  }, [currentPage, pageSize, searchTerm, filters, sortBy, sortOrder]);

  // Initialize repositories data
  useEffect(() => {
    fetchRepositories();
  }, [fetchRepositories]);

  // Handle filter changes - reset to page 0 when filters change
  const handleFilterChange = (newFilters: Record<string, string | string[] | number | boolean | null>) => {
    setFilters(newFilters as Record<string, string | number | boolean | null>);
    setCurrentPage(0); // Reset to first page when filters change
  };

  const handleSearchChange = (newSearchTerm: string) => {
    setSearchTerm(newSearchTerm);
    setCurrentPage(0); // Reset to first page when search changes
  };

  const hasActiveFilters = searchTerm.trim() !== '' || Object.values(filters).some(value =>
    value !== null && value !== '' && (Array.isArray(value) ? value.length > 0 : true)
  );

  const clearAllFilters = () => {
    setSearchTerm('');
    setFilters({});
    setCurrentPage(0);
  };

  // Bulk operations
  const bulkOps = useBulkOperations({
    items: repositories,
    getId: (repo) => repo.repositoryId
  });

  const bulkActions: BulkAction[] = [
    {
      id: 'scan',
      label: 'Scan Selected',
      icon: <Play className="h-4 w-4" />,
      variant: 'success',
      onClick: async (selectedIds) => {
        await handleBulkScan(selectedIds);
        bulkOps.clearSelection();
      },
      loadingText: 'Scanning...'
    },
    {
      id: 'export',
      label: 'Export Selected',
      icon: <Download className="h-4 w-4" />,
      variant: 'primary',
      onClick: async (selectedIds) => {
        // Use ExportManager for selected items
        const exportData = prepareRepositoryExportData(
          repositories,
          'selected',
          new Set(selectedIds)
        );
        
        const option = {
          id: 'csv-selected',
          label: 'Export Selected (CSV)',
          description: `Export ${selectedIds.length} selected repositories to CSV`,
          format: 'csv' as const,
          scope: 'selected' as const,
          filename: `repositories-selected-${new Date().toISOString().split('T')[0]}.csv`
        };
        
        exportDataUtil(exportData, option);
      },
      loadingText: 'Exporting...'
    },
    {
      id: 'refresh',
      label: 'Refresh Data',
      icon: <RefreshCw className="h-4 w-4" />,
      variant: 'secondary',
      onClick: async () => {
        await handleBulkRefresh();
        bulkOps.clearSelection();
      },
      loadingText: 'Refreshing...'
    },
    {
      id: 'delete',
      label: 'Remove from List',
      icon: <Trash2 className="h-4 w-4" />,
      variant: 'danger',
      onClick: async (selectedIds) => {
        await handleBulkDelete(selectedIds);
        bulkOps.clearSelection();
      },
      confirmMessage: 'Are you sure you want to remove these repositories from the list?',
      loadingText: 'Removing...'
    }
  ];

  const handleRepositoryClick = (repository: RepositorySummary) => {
    navigate(`/repositories/${repository.repositoryId}`);
  };

  const handleBulkScan = async (repositoryIds: number[]) => {
    try {
      setScanning(true);
      setScanResults(null);
      
      // Trigger scan for multiple repositories
      const result = await api.scan.trigger();
      
      if (result.success) {
        setScanResults({ success: repositoryIds.length, failed: 0 });
        
        // Clear results after 5 seconds
        setTimeout(() => {
          setScanResults(null);
        }, 5000);
      } else {
        setScanResults({ success: 0, failed: repositoryIds.length });
      }
      
    } catch (err) {
      console.error('Error triggering bulk scan:', err);
      setScanResults({ success: 0, failed: repositoryIds.length });
    } finally {
      setScanning(false);
    }
  };

  // const handleExportAll = async () => {
  //   try {
  //     const overview = await api.dashboard.getOverview();
  //     const repositories = overview.topRepositories || [];
  //     const data = {
  //       exportDate: new Date().toISOString(),
  //       totalRepositories: repositories.length,
  //       repositories: repositories
  //     };

  //     const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
  //     const url = URL.createObjectURL(blob);
  //     const a = document.createElement('a');
  //     a.href = url;
  //     a.download = `repositories-export-${new Date().toISOString().split('T')[0]}.json`;
  //     document.body.appendChild(a);
  //     a.click();
  //     document.body.removeChild(a);
  //     URL.revokeObjectURL(url);
  //   } catch (err) {
  //     console.error('Error exporting repositories:', err);
  //   }
  //   // };

  const handleBulkRefresh = async () => {
    try {
      setScanning(true);
      // Trigger refresh for all repositories
      const result = await api.scan.trigger();
      
      if (result.success) {
        // Refresh the repositories data using paginated API
        await fetchRepositories();
      }
    } catch (err) {
      console.error('Error refreshing repositories:', err);
    } finally {
      setScanning(false);
    }
  };

  const handleBulkDelete = async (repositoryIds: number[]) => {
    try {
      // Remove selected repositories from the local state
      setRepositories(prev => prev.filter(repo => !repositoryIds.includes(repo.repositoryId)));
      
      // Update team filter options
      const remainingRepos = repositories.filter(repo => !repositoryIds.includes(repo.repositoryId));
      const uniqueTeams = Array.from(new Set(remainingRepos.map(repo => repo.teamName)))
        .sort()
        .map(team => ({ value: team, label: team }));
      
      setTeamOptions(uniqueTeams);
    } catch (err) {
      console.error('Error removing repositories:', err);
    }
  };

  const handleExport = async (option: ExportOption) => {
    try {
      const scope = option.scope as ExportScope;
      const exportData = prepareRepositoryExportData(
        repositories,
        scope,
        scope === 'selected' ? bulkOps.selectedItems : undefined
      );
      
      exportDataUtil(exportData, option);
    } catch (err) {
      console.error('Error exporting repositories:', err);
    }
  };

  return (
    <div className="p-8">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center">
          <FolderOpen className="h-8 w-8 text-blue-600 mr-3" />
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Repositories</h1>
            <p className="text-gray-600 mt-1">Manage and analyze all repositories</p>
          </div>
        </div>
        
        <div className="flex items-center gap-4">
          {/* Data Controls */}
          <DataControls
            pageSize={pageSize}
            onPageSizeChange={(size) => {
              setPageSize(size);
              setCurrentPage(0); // Reset to first page when changing page size
            }}
            sortBy={sortBy}
            onSortByChange={(field) => {
              setSortBy(field);
              setCurrentPage(0); // Reset to first page when sorting
            }}
            sortOptions={sortOptions}
            sortOrder={sortOrder}
            onSortOrderChange={(order) => {
              setSortOrder(order);
              setCurrentPage(0); // Reset to first page when changing order
            }}
          />

          <div className="flex gap-2">
            <button
              onClick={() => setColumnManagerOpen(true)}
              className="btn btn-secondary flex items-center"
            >
              <Columns className="h-4 w-4 mr-2" />
              Columns
            </button>
            <ExportManager
              data={repositories}
              dataType="repositories"
              selectedItems={bulkOps.selectedItems}
              filteredData={repositories}
              onExport={handleExport}
            />
          </div>
        </div>
      </div>

      {/* Scan Results Banner */}
      {scanResults && (
        <div className={`card mb-6 ${
          scanResults.failed === 0 ? 'bg-green-50 dark:bg-green-900/20 border-green-200 dark:border-green-800' : 'bg-red-50 dark:bg-red-900/20 border-red-200 dark:border-red-800'
        }`}>
          <div className="flex items-center">
            {scanResults.failed === 0 ? (
              <CheckCircle className="h-5 w-5 text-green-600 dark:text-green-400 mr-3" />
            ) : (
              <AlertCircle className="h-5 w-5 text-red-600 dark:text-red-400 mr-3" />
            )}
            <div>
              <h3 className={`font-semibold ${
                scanResults.failed === 0 ? 'text-green-800 dark:text-green-200' : 'text-red-800 dark:text-red-200'
              }`}>
                Bulk Scan {scanResults.failed === 0 ? 'Completed' : 'Failed'}
              </h3>
              <p className={`text-sm ${
                scanResults.failed === 0 ? 'text-green-700 dark:text-green-300' : 'text-red-700 dark:text-red-300'
              }`}>
                {scanResults.success} repositories scanned successfully
                {scanResults.failed > 0 && `, ${scanResults.failed} failed`}
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Advanced Filter */}
      <AdvancedFilter
        filters={filterOptions}
        onFilterChange={handleFilterChange}
        onSearchChange={handleSearchChange}
        searchPlaceholder="Search repositories, teams, or URLs..."
        className="mb-6"
      />

      {/* Results Summary */}
      <div className="flex items-center justify-between text-sm text-gray-600 dark:text-gray-400 mb-4">
        <span>
          Showing {repositories.length} of {pagination?.totalElements || 0} repositories
          {hasActiveFilters && ' (filtered)'}
        </span>
        {hasActiveFilters && (
          <button
            onClick={clearAllFilters}
            className="text-blue-600 dark:text-blue-400 hover:text-blue-800 dark:hover:text-blue-300 font-medium"
          >
            Clear all filters
          </button>
        )}
      </div>


      {/* Bulk Operations */}
      <BulkOperations
        selectedItems={bulkOps.selectedItems}
        totalItems={repositories.length}
        onSelectAll={bulkOps.selectAll}
        onClearSelection={bulkOps.clearSelection}
        actions={bulkActions}
        itemType="repositories"
        className="mb-6"
      />

      {/* Repository List */}
      <RepositoryList 
        repositories={repositories}
        onRepositoryClick={handleRepositoryClick}
        onBulkScan={handleBulkScan}
        loading={loading}
        selectedRepositories={bulkOps.selectedItems}
        onSelectRepository={bulkOps.toggleItem}
        onSelectAll={bulkOps.selectAll}
      />

      {/* Pagination Controls */}
      {pagination && pagination.totalPages > 1 && (
        <div className="flex items-center justify-end mt-6">
          <div className="flex items-center space-x-3">
            <button
              onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
              disabled={currentPage === 0}
              className={`
                inline-flex items-center px-4 py-2 text-sm font-medium rounded-lg transition-all duration-200
                ${currentPage === 0
                  ? 'bg-gray-100 dark:bg-gray-700 text-gray-400 dark:text-gray-500 cursor-not-allowed'
                  : 'bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-200 border border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-700 hover:border-gray-400 dark:hover:border-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent shadow-sm hover:shadow-md'
                }
              `}
            >
              <ChevronLeft className="h-4 w-4 mr-2" />
              Previous
            </button>

            <div className="flex items-center px-3 py-2 bg-gray-50 dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700">
              <span className="text-sm font-medium text-gray-700 dark:text-gray-200">
                Page {currentPage + 1} of {pagination.totalPages}
              </span>
            </div>

            <button
              onClick={() => setCurrentPage(Math.min(pagination.totalPages - 1, currentPage + 1))}
              disabled={currentPage >= pagination.totalPages - 1}
              className={`
                inline-flex items-center px-4 py-2 text-sm font-medium rounded-lg transition-all duration-200
                ${currentPage >= pagination.totalPages - 1
                  ? 'bg-gray-100 dark:bg-gray-700 text-gray-400 dark:text-gray-500 cursor-not-allowed'
                  : 'bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-200 border border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-700 hover:border-gray-400 dark:hover:border-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent shadow-sm hover:shadow-md'
                }
              `}
            >
              Next
              <ChevronRight className="h-4 w-4 ml-2" />
            </button>
          </div>
        </div>
      )}

      {/* Loading Overlay */}
      {scanning && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white dark:bg-gray-800 rounded-lg p-6 max-w-sm w-full mx-4">
            <div className="flex items-center">
              <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600 mr-3"></div>
              <div>
                <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">Scanning Repositories</h3>
                <p className="text-sm text-gray-600 dark:text-gray-400">Please wait while repositories are being scanned...</p>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Column Manager */}
      <ColumnManager
        tableId="repositories"
        columns={repositoryColumns}
        isOpen={columnManagerOpen}
        onClose={() => setColumnManagerOpen(false)}
      />
    </div>
  );
};

export default RepositoriesView;
