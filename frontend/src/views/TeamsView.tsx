import React, { useState, useEffect, useCallback } from 'react';
import { Users, TrendingUp, BarChart3, GitBranch, Search, Download, ChevronLeft, ChevronRight, Eye, X, Calendar, ExternalLink, TestTube, CheckCircle, Play, RefreshCw } from 'lucide-react';
import { api, type TeamMetrics, type PagedResponse } from '../lib/api';
import StatCard from '../components/shared/StatCard';
import BulkOperations, { type BulkAction } from '../components/shared/BulkOperations';
import ExportManager, { type ExportOption } from '../components/shared/ExportManager';
import DataControls, { type SortOption } from '../components/shared/DataControls';
import { useBulkOperations } from '../hooks/useBulkOperations';
import { exportData as exportDataUtil, prepareTeamExportData, type ExportScope } from '../utils/exportUtils';
import { useModal } from '../hooks/useModal';
import GitUrlLink from '../components/shared/GitUrlLink';

interface TeamDetailModalProps {
  team: TeamMetrics | null;
  isOpen: boolean;
  onClose: () => void;
}

const TeamDetailModal: React.FC<TeamDetailModalProps> = ({ team, isOpen, onClose }) => {
  const [repoSearchTerm, setRepoSearchTerm] = useState('');
  const [repoSortBy, setRepoSortBy] = useState<'name' | 'coverage' | 'methods' | 'classes'>('name');
  const [repoSortOrder, setRepoSortOrder] = useState<'asc' | 'desc'>('desc');
  const [repoCurrentPage, setRepoCurrentPage] = useState(1);
  const [repoItemsPerPage] = useState(10);

  if (!isOpen || !team) return null;

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return {
      date: date.toLocaleDateString(),
      time: date.toLocaleTimeString(),
      relative: getRelativeTime(date)
    };
  };

  const getRelativeTime = (date: Date) => {
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    const diffDays = Math.floor(diffHours / 24);

    if (diffDays > 0) return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`;
    if (diffHours > 0) return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`;
    return 'Just now';
  };

  // Repository filtering and pagination
  const filteredRepos = team.repositories.filter(repo =>
    repo.repositoryName.toLowerCase().includes(repoSearchTerm.toLowerCase()) ||
    repo.gitUrl.toLowerCase().includes(repoSearchTerm.toLowerCase())
  ).sort((a, b) => {
    let aValue: string | number;
    let bValue: string | number;

    switch (repoSortBy) {
      case 'name':
        aValue = a.repositoryName.toLowerCase();
        bValue = b.repositoryName.toLowerCase();
        break;
      case 'coverage':
        aValue = a.coverageRate;
        bValue = b.coverageRate;
        break;
      case 'methods':
        aValue = a.testMethodCount;
        bValue = b.testMethodCount;
        break;
      case 'classes':
        aValue = a.testClassCount;
        bValue = b.testClassCount;
        break;
      default:
        aValue = a.repositoryName.toLowerCase();
        bValue = b.repositoryName.toLowerCase();
    }

    if (repoSortOrder === 'asc') {
      return aValue < bValue ? -1 : aValue > bValue ? 1 : 0;
    } else {
      return aValue > bValue ? -1 : aValue < bValue ? 1 : 0;
    }
  });

  const repoTotalPages = Math.ceil(filteredRepos.length / repoItemsPerPage);
  const repoStartIndex = (repoCurrentPage - 1) * repoItemsPerPage;
  const repoEndIndex = repoStartIndex + repoItemsPerPage;
  const currentRepos = filteredRepos.slice(repoStartIndex, repoEndIndex);

  const handleRepoPageChange = (page: number) => {
    setRepoCurrentPage(page);
  };

  const exportTeamData = () => {
    const csvContent = [
      ['Team Name', 'Team Code', 'Department', 'Repositories', 'Test Classes', 'Test Methods', 'Coverage Rate'],
      [team.teamName, team.teamCode, team.department || '', team.repositoryCount.toString(), team.totalTestClasses.toString(), team.totalTestMethods.toString(), team.averageCoverageRate.toFixed(2)]
    ].map(row => row.join(',')).join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `team-${team.teamCode}-export-${new Date().toISOString().split('T')[0]}.csv`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* Backdrop */}
      <div 
        className="absolute inset-0 bg-black/20 backdrop-blur-sm"
        onClick={onClose}
      />
      
      {/* Modal Content */}
      <div className="relative w-full max-w-4xl max-h-[90vh] overflow-y-auto rounded-xl shadow-2xl border animate-in fade-in-0 zoom-in-95 duration-300" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)' }}>
        {/* Header */}
        <div className="sticky top-0 border-b px-6 py-4 rounded-t-xl" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)' }}>
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <div className="p-2 bg-blue-100 dark:bg-blue-900/30 rounded-lg">
                <Users className="h-5 w-5 text-blue-600 dark:text-blue-400" />
              </div>
              <div>
                <h3 className="text-xl font-semibold" style={{ color: 'var(--color-foreground)' }}>Team Details</h3>
                <p className="text-sm" style={{ color: 'var(--color-muted-foreground)' }}>{team.teamName} ({team.teamCode})</p>
              </div>
            </div>
            <button
              onClick={onClose}
              className="p-2 text-gray-400 dark:text-gray-500 hover:text-gray-600 dark:hover:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg transition-colors"
            >
              <X className="h-5 w-5" />
            </button>
          </div>
        </div>
        
        {/* Content */}
        <div className="p-6 space-y-6">
          {/* Team Information */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-4">
              <div>
                <h4 className="text-lg font-medium mb-3" style={{ color: 'var(--color-foreground)' }}>Team Information</h4>
                <div className="space-y-3">
                  <div className="flex items-center space-x-3">
                    <Users className="h-4 w-4 text-gray-400" />
                    <div>
                      <p className="text-sm" style={{ color: 'var(--color-muted-foreground)' }}>Team Name</p>
                      <p className="font-medium" style={{ color: 'var(--color-foreground)' }}>{team.teamName}</p>
                    </div>
                  </div>
                  
                  <div className="flex items-center space-x-3">
                    <ExternalLink className="h-4 w-4 text-gray-400" />
                    <div>
                      <p className="text-sm" style={{ color: 'var(--color-muted-foreground)' }}>Team Code</p>
                      <p className="font-mono text-sm px-2 py-1 rounded" style={{ backgroundColor: 'var(--color-muted)' }}>
                        {team.teamCode}
                      </p>
                    </div>
                  </div>
                  
                  {team.department && (
                    <div className="flex items-center space-x-3">
                      <Users className="h-4 w-4 text-gray-400" />
                      <div>
                        <p className="text-sm" style={{ color: 'var(--color-muted-foreground)' }}>Department</p>
                        <p className="font-medium" style={{ color: 'var(--color-foreground)' }}>{team.department}</p>
                      </div>
                    </div>
                  )}
                  
                  <div className="flex items-center space-x-3">
                    <Calendar className="h-4 w-4 text-gray-400" />
                    <div>
                      <p className="text-sm" style={{ color: 'var(--color-muted-foreground)' }}>Last Scan</p>
                      <p className="font-medium" style={{ color: 'var(--color-foreground)' }}>
                        {team.lastScanDate ? formatDate(team.lastScanDate).date : 'N/A'}
                      </p>
                      {team.lastScanDate && (
                        <p className="text-xs" style={{ color: 'var(--color-muted-foreground)' }}>{formatDate(team.lastScanDate).relative}</p>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <div className="space-y-4">
              <div>
                <h4 className="text-lg font-medium mb-3" style={{ color: 'var(--color-foreground)' }}>Team Metrics</h4>
                <div className="grid grid-cols-2 gap-4">
                  <div className="p-4 bg-blue-50 rounded-lg">
                    <div className="flex items-center space-x-2 mb-2">
                      <GitBranch className="h-4 w-4 text-blue-600" />
                      <span className="text-sm font-medium text-blue-900">Repositories</span>
                    </div>
                    <p className="text-2xl font-bold text-blue-600">{team.repositoryCount}</p>
                  </div>
                  
                  <div className="p-4 bg-green-50 rounded-lg">
                    <div className="flex items-center space-x-2 mb-2">
                      <TestTube className="h-4 w-4 text-green-600" />
                      <span className="text-sm font-medium text-green-900">Test Classes</span>
                    </div>
                    <p className="text-2xl font-bold text-green-600">{team.totalTestClasses}</p>
                  </div>
                  
                  <div className="p-4 bg-purple-custom-light rounded-lg">
                    <div className="flex items-center space-x-2 mb-2">
                      <CheckCircle className="h-4 w-4 text-purple-custom" />
                      <span className="text-sm font-medium text-purple-custom-dark">Test Methods</span>
                    </div>
                    <p className="text-2xl font-bold text-purple-custom">{team.totalTestMethods}</p>
                  </div>
                  
                  <div className="p-4 bg-orange-custom-light rounded-lg">
                    <div className="flex items-center space-x-2 mb-2">
                      <CheckCircle className="h-4 w-4 text-orange-custom" />
                      <span className="text-sm font-medium text-orange-custom-dark">Coverage</span>
                    </div>
                    <p className="text-2xl font-bold text-orange-custom">{team.averageCoverageRate.toFixed(1)}%</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          {/* Coverage Breakdown */}
          <div>
            <h4 className="text-lg font-medium mb-3" style={{ color: 'var(--color-foreground)' }}>Coverage Analysis</h4>
            <div className="rounded-lg p-4" style={{ backgroundColor: 'var(--color-muted)' }}>
              <div className="flex items-center justify-between mb-2">
                <span className="text-sm font-medium" style={{ color: 'var(--color-muted-foreground)' }}>Average Coverage</span>
                <span className="text-sm font-bold" style={{ color: 'var(--color-foreground)' }}>{team.averageCoverageRate.toFixed(1)}%</span>
              </div>
              <div className="w-full rounded-full h-4 relative overflow-hidden" style={{ backgroundColor: 'var(--color-border)' }}>
                <div 
                  style={{ 
                    width: `${Math.max(Math.min(team.averageCoverageRate, 100), 5)}%`,
                    minWidth: '20px',
                    height: '16px',
                    backgroundColor: '#3b82f6',
                    borderRadius: '9999px',
                    border: '2px solid #1d4ed8',
                    transition: 'all 0.5s ease-out',
                    boxShadow: '0 1px 3px 0 rgba(0, 0, 0, 0.1)'
                  }}
                />
              </div>
              <div className="flex justify-between text-xs text-gray-500 mt-2">
                <span style={{ color: 'var(--color-muted-foreground)' }}>{team.totalAnnotatedMethods} annotated methods</span>
                <span style={{ color: 'var(--color-muted-foreground)' }}>{team.totalTestMethods - team.totalAnnotatedMethods} remaining</span>
              </div>
            </div>
          </div>

          {/* Repositories Section */}
          <div>
            <div className="flex items-center justify-between mb-4">
              <h4 className="text-lg font-medium" style={{ color: 'var(--color-foreground)' }}>Repositories</h4>
              <div className="text-sm" style={{ color: 'var(--color-muted-foreground)' }}>
                {filteredRepos.length} of {team.repositories.length} repositories
              </div>
            </div>

            {/* Repository Filters */}
            <div className="rounded-lg p-4 mb-4" style={{ backgroundColor: 'var(--color-muted)' }}>
              <div className="flex flex-col lg:flex-row gap-4">
                <div className="flex-1">
                  <div className="relative">
                    <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
                    <input
                      type="text"
                      placeholder="Search repositories by name or URL..."
                      value={repoSearchTerm}
                      onChange={(e) => {
                        setRepoSearchTerm(e.target.value);
                        setRepoCurrentPage(1); // Reset to first page when searching
                      }}
                      className="w-full pl-10 pr-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)', color: 'var(--color-foreground)' }}
                    />
                  </div>
                </div>
                <div className="flex gap-2">
                  <select
                    value={repoSortBy}
                    onChange={(e) => {
                      setRepoSortBy(e.target.value as 'name' | 'coverage' | 'methods' | 'classes');
                      setRepoCurrentPage(1);
                    }}
                    className="px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)', color: 'var(--color-foreground)' }}
                  >
                    <option value="name">Sort by Name</option>
                    <option value="coverage">Sort by Coverage</option>
                    <option value="methods">Sort by Test Methods</option>
                    <option value="classes">Sort by Test Classes</option>
                  </select>
                  <button
                    onClick={() => {
                      setRepoSortOrder(repoSortOrder === 'asc' ? 'desc' : 'asc');
                      setRepoCurrentPage(1);
                    }}
                    className="px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)', color: 'var(--color-foreground)' }}
                  >
                    {repoSortOrder === 'asc' ? '↑' : '↓'}
                  </button>
                </div>
              </div>
            </div>

            {/* Repositories Table */}
            <div className="overflow-x-auto">
              <table className="min-w-full border rounded-lg" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)' }}>
                <thead style={{ backgroundColor: 'var(--color-muted)' }}>
                  <tr>
                    <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>
                      Repository
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>
                      Test Classes
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>
                      Test Methods
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>
                      Coverage
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>
                      Last Scan
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y" style={{ borderColor: 'var(--color-border)' }}>
                  {currentRepos.map((repo, index) => (
                    <tr key={`repo-${repo.repositoryId}-${index}`} className="hover:bg-gray-50">
                      <td className="px-4 py-3">
                        <div>
                          <div className="text-sm font-medium" style={{ color: 'var(--color-foreground)' }}>{repo.repositoryName}</div>
                          <GitUrlLink 
                            url={repo.gitUrl}
                            className="mt-1"
                            truncate={true}
                            maxWidth="max-w-xs"
                          />
                        </div>
                      </td>
                      <td className="px-4 py-3 text-sm" style={{ color: 'var(--color-foreground)' }}>{repo.testClassCount}</td>
                      <td className="px-4 py-3 text-sm" style={{ color: 'var(--color-foreground)' }}>{repo.testMethodCount}</td>
                      <td className="px-4 py-3">
                        <div className="flex items-center">
                          <div className="w-16 bg-gray-200 rounded-full h-2 mr-2">
                            <div
                              className={`h-2 rounded-full transition-all duration-300 ${
                                repo.coverageRate >= 80 ? 'bg-green-500' :
                                repo.coverageRate >= 60 ? 'bg-yellow-500' : 'bg-red-500'
                              }`}
                              style={{ width: `${Math.min(repo.coverageRate, 100)}%` }}
                            />
                          </div>
                          <span className="text-sm" style={{ color: 'var(--color-foreground)' }}>{repo.coverageRate.toFixed(1)}%</span>
                        </div>
                      </td>
                      <td className="px-4 py-3 text-sm" style={{ color: 'var(--color-muted-foreground)' }}>
                        {repo.lastScanDate ? new Date(repo.lastScanDate).toLocaleDateString() : 'N/A'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Repository Pagination */}
            {repoTotalPages > 1 && (
              <div className="mt-4 flex items-center justify-between">
                <div className="text-sm" style={{ color: 'var(--color-muted-foreground)' }}>
                  Showing{' '}
                  <span className="font-medium">{repoStartIndex + 1}</span>
                  {' '}to{' '}
                  <span className="font-medium">{Math.min(repoEndIndex, filteredRepos.length)}</span>
                  {' '}of{' '}
                  <span className="font-medium">{filteredRepos.length}</span>
                  {' '}repositories
                </div>
                <div className="flex items-center space-x-2">
                  <button
                    onClick={() => handleRepoPageChange(repoCurrentPage - 1)}
                    disabled={repoCurrentPage === 1}
                    className="px-3 py-1 text-sm border rounded disabled:opacity-50 disabled:cursor-not-allowed"
                    style={{ borderColor: 'var(--color-border)', backgroundColor: 'var(--color-background)', color: 'var(--color-foreground)' }}
                  >
                    Previous
                  </button>
                  
                  {/* Page numbers */}
                  {Array.from({ length: Math.min(5, repoTotalPages) }, (_, i) => {
                    let pageNum;
                    if (repoTotalPages <= 5) {
                      pageNum = i + 1;
                    } else if (repoCurrentPage <= 3) {
                      pageNum = i + 1;
                    } else if (repoCurrentPage >= repoTotalPages - 2) {
                      pageNum = repoTotalPages - 4 + i;
                    } else {
                      pageNum = repoCurrentPage - 2 + i;
                    }
                    
                    return (
                      <button
                        key={`page-${pageNum}-${i}`}
                        onClick={() => handleRepoPageChange(pageNum)}
                        className={`px-3 py-1 text-sm border rounded ${
                          pageNum === repoCurrentPage ? '' : ''
                        }`}
                        style={{
                          backgroundColor: pageNum === repoCurrentPage ? 'var(--color-accent)' : 'var(--color-background)',
                          borderColor: pageNum === repoCurrentPage ? 'var(--color-primary)' : 'var(--color-border)',
                          color: pageNum === repoCurrentPage ? 'var(--color-primary)' : 'var(--color-foreground)'
                        }}
                      >
                        {pageNum}
                      </button>
                    );
                  })}
                  
                  <button
                    onClick={() => handleRepoPageChange(repoCurrentPage + 1)}
                    disabled={repoCurrentPage === repoTotalPages}
                    className="px-3 py-1 text-sm border rounded disabled:opacity-50 disabled:cursor-not-allowed"
                    style={{ borderColor: 'var(--color-border)', backgroundColor: 'var(--color-background)', color: 'var(--color-foreground)' }}
                  >
                    Next
                  </button>
                </div>
              </div>
            )}

            {filteredRepos.length === 0 && (
              <div className="text-center py-8">
                <GitBranch className="h-12 w-12 text-gray-400 mx-auto mb-3" />
                <h3 className="text-lg font-semibold mb-2" style={{ color: 'var(--color-foreground)' }}>No repositories found</h3>
                <p style={{ color: 'var(--color-muted-foreground)' }}>
                  {repoSearchTerm ? 'Try adjusting your search criteria.' : 'No repositories are available for this team.'}
                </p>
              </div>
            )}
          </div>
          
          {/* Action Buttons */}
          <div className="flex justify-end space-x-3 pt-4 border-t border-gray-200">
            <button
              onClick={onClose}
              className="btn btn-secondary"
            >
              Close
            </button>
            <button
              onClick={exportTeamData}
              className="btn btn-primary"
            >
              Export Team Data
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

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

  // const handleBulkExportTeams = async (teamIds: number[]) => {
  //   try {
  //     const selectedTeams = teams.filter(team => teamIds.includes(team.id));
  //     const data = {
  //       exportDate: new Date().toISOString(),
  //       totalTeams: selectedTeams.length,
  //       teams: selectedTeams
  //     };

  //     const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
  //     const url = URL.createObjectURL(blob);
  //     const a = document.createElement('a');
  //     a.href = url;
  //     a.download = `teams-export-${new Date().toISOString().split('T')[0]}.json`;
  //     document.body.appendChild(a);
  //     a.click();
  //     document.body.removeChild(a);
  //     URL.revokeObjectURL(url);
  //   } catch (err) {
  //     console.error('Error exporting selected teams:', err);
  //   }
  // };

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

  const handleExport = async (option: ExportOption) => {
    try {
      const scope = option.scope as ExportScope;
      const exportData = prepareTeamExportData(
        teams,
        scope,
        scope === 'selected' ? bulkOps.selectedItems : undefined
      );
      
      exportDataUtil(exportData, option);
    } catch (err) {
      console.error('Error exporting teams:', err);
    }
  };

  // Pagination calculations
  const handlePageChange = (page: number) => {
    setCurrentPage(page);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  // const exportTeamsData = () => {
  //   const csvContent = [
  //     ['Team Name', 'Team Code', 'Department', 'Repositories', 'Test Classes', 'Test Methods', 'Coverage Rate'],
  //     ...filteredTeams.map(team => [
  //       team.teamName,
  //       team.teamCode,
  //       team.department || '',
  //       team.repositoryCount.toString(),
  //       team.totalTestClasses.toString(),
  //       team.totalTestMethods.toString(),
  //       team.averageCoverageRate.toFixed(2)
  //     ])
  //   ].map(row => row.join(',')).join('\n');

  //   const blob = new Blob([csvContent], { type: 'text/csv' });
  //   const url = window.URL.createObjectURL(blob);
  //   const a = document.createElement('a');
  //   a.href = url;
  //   a.download = `teams-export-${new Date().toISOString().split('T')[0]}.csv`;
  //   document.body.appendChild(a);
  //   a.click();
  //   document.body.removeChild(a);
  //   window.URL.revokeObjectURL(url);
  // };

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

  const totalRepositories = teams.reduce((sum, team) => sum + team.repositoryCount, 0);
  const totalTestMethods = teams.reduce((sum, team) => sum + team.totalTestMethods, 0);
  const averageCoverage = teams.length > 0 
    ? teams.reduce((sum, team) => sum + team.averageCoverageRate, 0) / teams.length 
    : 0;

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
          value={teams.length.toString()}
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
                          className={`h-2 rounded-full transition-all duration-300 ${
                            team.averageCoverageRate >= 80 ? 'bg-green-500' :
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