import React, { useState, useCallback, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Download, Play, Trash2, RefreshCw } from 'lucide-react';
import RepositoryList from '../components/repositories/RepositoryList';
import RepositoriesHeader from '../components/repositories/RepositoriesHeader';
import ScanResultsBanner from '../components/repositories/ScanResultsBanner';
import ResultsSummary from '../components/repositories/ResultsSummary';
import AdvancedFilter, { type FilterOption } from '../components/shared/AdvancedFilter';
import BulkOperations, { type BulkAction } from '../components/shared/BulkOperations';
import { type ExportOption } from '../components/shared/ExportManager';
import ColumnManager from '../components/shared/ColumnManager';
import Pagination from '../components/shared/Pagination';
import LoadingOverlay from '../components/shared/LoadingOverlay';
import { type SortOption } from '../components/shared/DataControls';
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
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc');

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
    } catch (err) {
      console.error('Error fetching repositories:', err);
    } finally {
      setLoading(false);
    }
  }, [currentPage, pageSize, searchTerm, filters, sortBy, sortOrder]);

  // Load team options on mount
  useEffect(() => {
    const loadTeamOptions = async () => {
      try {
        const teams = await api.teams.getAll();
        const teamOptionsList = teams.map(team => ({ value: team.teamName, label: team.teamName }));
        setTeamOptions(teamOptionsList);
      } catch (err) {
        console.error('Error loading team options:', err);
      }
    };
    loadTeamOptions();
  }, []);

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
    getId: (repo) => repo.id
  });

  const handleBulkScan = async (repositoryIds: number[]) => {
    try {
      setScanning(true);
      setScanResults(null);

      // Trigger scan for multiple repositories
      const result = await api.scan.trigger(repositoryIds);

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
      setRepositories(prev => prev.filter(repo => !repositoryIds.includes(repo.id)));
    } catch (err) {
      console.error('Error removing repositories:', err);
    }
  };

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
    navigate(`/repositories/${repository.id}`);
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
      <RepositoriesHeader
        pageSize={pageSize}
        onPageSizeChange={(size) => {
          setPageSize(size);
          setCurrentPage(0);
        }}
        sortBy={sortBy}
        onSortByChange={(field) => {
          setSortBy(field);
          setCurrentPage(0);
        }}
        sortOptions={sortOptions}
        sortOrder={sortOrder}
        onSortOrderChange={(order) => {
          setSortOrder(order);
          setCurrentPage(0);
        }}
        repositories={repositories}
        selectedItems={bulkOps.selectedItems}
        onExport={handleExport}
        onColumnManagerOpen={() => setColumnManagerOpen(true)}
      />

      {/* Scan Results Banner */}
      <ScanResultsBanner scanResults={scanResults} />

      {/* Advanced Filter */}
      <AdvancedFilter
        filters={filterOptions}
        onFilterChange={handleFilterChange}
        onSearchChange={handleSearchChange}
        searchPlaceholder="Search repositories, teams, or URLs..."
        className="mb-6"
      />

      {/* Results Summary */}
      <ResultsSummary
        showing={repositories.length}
        total={pagination?.totalElements || 0}
        hasActiveFilters={hasActiveFilters}
        onClearFilters={clearAllFilters}
      />

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
      <Pagination
        currentPage={currentPage}
        totalPages={pagination?.totalPages || 0}
        onPageChange={setCurrentPage}
      />

      {/* Loading Overlay */}
      <LoadingOverlay
        isVisible={scanning}
        title="Scanning Repositories"
        message="Please wait while repositories are being scanned..."
      />

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
