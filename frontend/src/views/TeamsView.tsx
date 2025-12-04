import React, { useState, useEffect, useCallback } from 'react';
import { Users, TrendingUp, BarChart3, GitBranch, Search, Download, ChevronLeft, ChevronRight, Eye, CheckCircle, Play, RefreshCw } from 'lucide-react';
import { api, type TeamMetrics, type PagedResponse, type DashboardOverview } from '../lib/api';
import StatCard from '../components/shared/StatCard';
import BulkOperations, { type BulkAction } from '../components/shared/BulkOperations';
import ExportManager, { type ExportOption } from '../components/shared/ExportManager';
import DataControls, { type SortOption } from '../components/shared/DataControls';
import { useBulkOperations } from '../hooks/useBulkOperations';
import { exportData as exportDataUtil, prepareTeamExportData, type ExportScope } from '../utils/exportUtils';
import { useModal } from '../hooks/useModal';
import TeamDetailModal from '../components/teams/TeamDetailModal';

const TeamsView: React.FC = () => {
  const [teams, setTeams] = useState<TeamMetrics[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [sortBy, setSortBy] = useState<'name' | 'repositories' | 'coverage'>('name');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [pagination, setPagination] = useState<PagedResponse<TeamMetrics> | null>(null);
  const { isOpen: isDetailOpen, open: openDetailModal, close: closeDetailModal } = useModal();
  const [selectedTeam, setSelectedTeam] = useState<TeamMetrics | null>(null);
  const [overview, setOverview] = useState<DashboardOverview | null>(null);

  // Sort options configuration
  const sortOptions: SortOption[] = [
    { value: 'name', label: 'Name' },
    { value: 'repositories', label: 'Repositories' },
    { value: 'coverage', label: 'Coverage' }
  ];

  const fetchTeams = useCallback(async () => {
    try {
      setLoading(true);
      const response = await api.teams.getPaginated(
        currentPage,
        pageSize,
        searchTerm || undefined,
        sortBy,
        sortOrder
      );
      setTeams(response.content);
      setPagination(response);

      try {
        const overviewResponse = await api.dashboard.getOverview();
        setOverview(overviewResponse);
      } catch (overviewErr) {
        console.error('Error fetching dashboard overview:', overviewErr);
      }
    } catch (err) {
      setError('Failed to fetch teams data');
      console.error('Error fetching teams:', err);
    } finally {
      setLoading(false);
    }
  }, [currentPage, pageSize, searchTerm, sortBy, sortOrder]);

  useEffect(() => {
    fetchTeams();
  }, [fetchTeams]);

  const handleSearchChange = (newSearchTerm: string) => {
    setSearchTerm(newSearchTerm);
    setCurrentPage(0); // Reset to first page when search changes
  };

  const handleSortByChange = (field: string) => {
    setSortBy(field as 'name' | 'repositories' | 'coverage');
    setCurrentPage(0); // Reset to first page when sorting changes
  };

  const handleSortOrderChange = (order: 'asc' | 'desc') => {
    setSortOrder(order);
    setCurrentPage(0); // Reset to first page when sort order changes
  };

  const handlePageSizeChange = (size: number) => {
    setPageSize(size);
    setCurrentPage(0); // Reset to first page when page size changes
  };

  const handleTeamClick = (team: TeamMetrics) => {
    setSelectedTeam(team);
    openDetailModal();
  };

  // Bulk operations
  const bulkOps = useBulkOperations({
    items: teams,
    getId: (team) => team.id
  });

  const handleBulkScanTeams = async () => {
    try {
      // Trigger scan for all repositories
      const result = await api.scan.trigger();

      if (result.success) {
        // Refresh teams data after scan
        await fetchTeams();
      }
    } catch (err) {
      console.error('Error scanning teams:', err);
    }
  };

  const bulkActions: BulkAction[] = [
    {
      id: 'export',
      label: 'Export Selected',
      icon: <Download className="h-4 w-4" />,
      variant: 'primary',
      onClick: async (selectedIds) => {
        // Use ExportManager for selected items
        const exportData = prepareTeamExportData(
          teams,
          'selected',
          new Set(selectedIds)
        );

        const option = {
          id: 'csv-selected',
          label: 'Export Selected (CSV)',
          description: `Export ${selectedIds.length} selected teams to CSV`,
          format: 'csv' as const,
          scope: 'selected' as const,
          filename: `teams-selected-${new Date().toISOString().split('T')[0]}.csv`
        };

        await exportDataUtil(exportData, option);
      },
      loadingText: 'Exporting...'
    },
    {
      id: 'refresh',
      label: 'Refresh Data',
      icon: <RefreshCw className="h-4 w-4" />,
      variant: 'secondary',
      onClick: async () => {
        await fetchTeams();
        bulkOps.clearSelection();
      },
      loadingText: 'Refreshing...'
    },
    {
      id: 'scan',
      label: 'Scan All Repositories',
      icon: <Play className="h-4 w-4" />,
      variant: 'success',
      onClick: async () => {
        await handleBulkScanTeams();
        bulkOps.clearSelection();
      },
      loadingText: 'Scanning...'
    }
  ];

  const handleExport = async (option: ExportOption) => {
    try {
      const scope = option.scope as ExportScope;
      const exportData = prepareTeamExportData(
        teams,
        scope,
        scope === 'selected' ? bulkOps.selectedItems : undefined
      );

      await exportDataUtil(exportData, option);
    } catch (err) {
      console.error('Error exporting teams:', err);
    }
  };

  // Pagination calculations
  const handlePageChange = (page: number) => {
    setCurrentPage(page);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  if (loading) {
    return (
      <div className="p-8">
        <div className="flex items-center justify-center h-64">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-8">
        <div className="text-center py-12">
          <div className="text-red-600 dark:text-red-400 mb-4">{error}</div>
          <button
            onClick={fetchTeams}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  const totalTeams = overview?.totalTeams ?? pagination?.totalElements ?? teams.length;
  const totalRepositories = overview?.totalRepositories
    ?? teams.reduce((sum, team) => sum + team.repositoryCount, 0);
  const totalTestMethods = overview?.totalTestMethods
    ?? teams.reduce((sum, team) => sum + team.totalTestMethods, 0);
  const averageCoverage = overview?.overallCoverageRate
    ?? (teams.length > 0
      ? teams.reduce((sum, team) => sum + team.averageCoverageRate, 0) / teams.length
      : 0);

  return (
    <div className="p-8">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center">
          <Users className="h-8 w-8 text-blue-600 dark:text-blue-400 mr-3" />
          <h1 className="text-3xl font-bold" style={{ color: 'var(--color-foreground)' }}>Teams</h1>
        </div>

        <div className="flex items-center gap-4">
          {/* Data Controls */}
          <DataControls
            pageSize={pageSize}
            onPageSizeChange={handlePageSizeChange}
            sortBy={sortBy}
            onSortByChange={handleSortByChange}
            sortOptions={sortOptions}
            sortOrder={sortOrder}
            onSortOrderChange={handleSortOrderChange}
          />

          <ExportManager
            data={teams}
            dataType="teams"
            selectedItems={bulkOps.selectedItems}
            filteredData={teams}
            onExport={handleExport}
          />
        </div>
      </div>

      {/* Summary Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
        <StatCard
          title="Total Teams"
          value={totalTeams.toString()}
          icon={<Users className="h-5 w-5" />}
          color="blue"
        />
        <StatCard
          title="Total Repositories"
          value={totalRepositories.toString()}
          icon={<GitBranch className="h-5 w-5" />}
          color="green"
        />
        <StatCard
          title="Total Test Methods"
          value={totalTestMethods.toString()}
          icon={<TrendingUp className="h-5 w-5" />}
          color="purple"
        />
        <StatCard
          title="Average Coverage"
          value={`${averageCoverage.toFixed(1)}%`}
          icon={<BarChart3 className="h-5 w-5" />}
          color="orange"
        />
      </div>

      {/* Search */}
      <div className="rounded-lg shadow-sm border p-4 mb-6" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)' }}>
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 dark:text-gray-500 h-4 w-4" />
          <input
            type="text"
            placeholder="Search teams by name, code, or department..."
            value={searchTerm}
            onChange={(e) => handleSearchChange(e.target.value)}
            className="w-full pl-10 pr-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100"
          />
        </div>
        {pagination && (
          <div className="mt-3 text-sm" style={{ color: 'var(--color-muted-foreground)' }}>
            Showing {teams.length} of {pagination.totalElements} teams
            {searchTerm && ' (filtered)'}
          </div>
        )}
      </div>

      {/* Bulk Operations */}
      <BulkOperations
        selectedItems={bulkOps.selectedItems}
        totalItems={teams.length}
        onSelectAll={bulkOps.selectAll}
        onClearSelection={bulkOps.clearSelection}
        actions={bulkActions}
        itemType="teams"
        className="mb-6"
      />

      {/* Teams Table */}
      <div className="rounded-lg shadow-sm border overflow-hidden" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)' }}>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
            <thead style={{ backgroundColor: 'var(--color-muted)' }}>
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>
                  <button
                    onClick={bulkOps.selectAll}
                    className="flex items-center hover:opacity-80"
                  >
                    {bulkOps.isAllSelected ? (
                      <CheckCircle className="h-4 w-4 mr-2" />
                    ) : (
                      <div className="h-4 w-4 mr-2 border-2 rounded" style={{ borderColor: 'var(--color-border)' }}></div>
                    )}
                    Select All
                  </button>
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>
                  Team
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>
                  Repositories
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>
                  Test Classes
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>
                  Test Methods
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>
                  Coverage Rate
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="divide-y" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)' }}>
              {teams.map((team) => (
                <tr key={team.id} className="hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <button
                      onClick={() => bulkOps.toggleItem(team.id)}
                      className="text-gray-400 dark:text-gray-500 hover:text-gray-600 dark:hover:text-gray-400"
                    >
                      {bulkOps.isSelected(team.id) ? (
                        <CheckCircle className="h-4 w-4" />
                      ) : (
                        <div className="h-4 w-4 border-2 border-gray-400 dark:border-gray-500 rounded"></div>
                      )}
                    </button>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div>
                      <div className="text-sm font-medium" style={{ color: 'var(--color-foreground)' }}>{team.teamName}</div>
                      <div className="text-sm" style={{ color: 'var(--color-muted-foreground)' }}>{team.teamCode}</div>
                      {team.department && (
                        <div className="text-xs" style={{ color: 'var(--color-muted-foreground)' }}>{team.department}</div>
                      )}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm" style={{ color: 'var(--color-foreground)' }}>{team.repositoryCount}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm" style={{ color: 'var(--color-foreground)' }}>{team.totalTestClasses}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm" style={{ color: 'var(--color-foreground)' }}>{team.totalTestMethods}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center">
                      <div className="w-20 rounded-full h-2 mr-3" style={{ backgroundColor: 'var(--color-border)' }}>
                        <div
                          className={`h-2 rounded-full transition-all duration-300 ${team.averageCoverageRate >= 80 ? 'bg-green-500' :
                            team.averageCoverageRate >= 60 ? 'bg-yellow-500' : 'bg-red-500'
                            }`}
                          style={{ width: `${Math.min(team.averageCoverageRate, 100)}%` }}
                        />
                      </div>
                      <span className="text-sm font-medium" style={{ color: 'var(--color-foreground)' }}>
                        {team.averageCoverageRate.toFixed(1)}%
                      </span>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                    <button
                      onClick={() => handleTeamClick(team)}
                      className="text-blue-600 dark:text-blue-400 hover:text-blue-900 dark:hover:text-blue-300 flex items-center gap-1"
                    >
                      <Eye className="h-4 w-4" />
                      View Details
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

      </div>

      {/* Pagination Controls */}
      {pagination && pagination.totalPages > 1 && (
        <div className="flex items-center justify-end mt-6">
          <div className="flex items-center space-x-3">
            <button
              onClick={() => handlePageChange(currentPage - 1)}
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
              onClick={() => handlePageChange(currentPage + 1)}
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

      {teams.length === 0 && !loading && (
        <div className="text-center py-12">
          <Users className="h-16 w-16 text-gray-400 dark:text-gray-500 mx-auto mb-4" />
          <h3 className="text-lg font-semibold mb-2" style={{ color: 'var(--color-foreground)' }}>No teams found</h3>
          <p style={{ color: 'var(--color-muted-foreground)' }}>
            {searchTerm ? 'Try adjusting your search criteria.' : 'No teams are available.'}
          </p>
        </div>
      )}

      {/* Team Detail Modal */}
      <TeamDetailModal
        team={selectedTeam}
        isOpen={isDetailOpen}
        onClose={closeDetailModal}
      />
    </div>
  );
};

export default TeamsView;
