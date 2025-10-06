import React, { useState, useEffect, useCallback } from 'react';
import { Users, TrendingUp, BarChart3, GitBranch, Search, Download, ChevronLeft, ChevronRight, Eye, X, Calendar, ExternalLink, TestTube, CheckCircle, Play, RefreshCw } from 'lucide-react';
import { api, type TeamMetrics } from '../lib/api';
import StatCard from '../components/shared/StatCard';
import BulkOperations, { type BulkAction } from '../components/shared/BulkOperations';
import ExportManager, { type ExportOption } from '../components/shared/ExportManager';
import { useBulkOperations } from '../hooks/useBulkOperations';
import { exportData as exportDataUtil, prepareTeamExportData, type ExportScope } from '../utils/exportUtils';
import { useModal } from '../hooks/useModal';

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
      <div className="relative w-full max-w-4xl max-h-[90vh] overflow-y-auto bg-white rounded-xl shadow-2xl border border-gray-200 animate-in fade-in-0 zoom-in-95 duration-300">
        {/* Header */}
        <div className="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 rounded-t-xl">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <div className="p-2 bg-blue-100 rounded-lg">
                <Users className="h-5 w-5 text-blue-600" />
              </div>
              <div>
                <h3 className="text-xl font-semibold text-gray-900">Team Details</h3>
                <p className="text-sm text-gray-500">{team.teamName} ({team.teamCode})</p>
              </div>
            </div>
            <button
              onClick={onClose}
              className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-lg transition-colors"
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
                <h4 className="text-lg font-medium text-gray-900 mb-3">Team Information</h4>
                <div className="space-y-3">
                  <div className="flex items-center space-x-3">
                    <Users className="h-4 w-4 text-gray-400" />
                    <div>
                      <p className="text-sm text-gray-500">Team Name</p>
                      <p className="font-medium">{team.teamName}</p>
                    </div>
                  </div>
                  
                  <div className="flex items-center space-x-3">
                    <ExternalLink className="h-4 w-4 text-gray-400" />
                    <div>
                      <p className="text-sm text-gray-500">Team Code</p>
                      <p className="font-mono text-sm bg-gray-100 px-2 py-1 rounded">
                        {team.teamCode}
                      </p>
                    </div>
                  </div>
                  
                  {team.department && (
                    <div className="flex items-center space-x-3">
                      <Users className="h-4 w-4 text-gray-400" />
                      <div>
                        <p className="text-sm text-gray-500">Department</p>
                        <p className="font-medium">{team.department}</p>
                      </div>
                    </div>
                  )}
                  
                  <div className="flex items-center space-x-3">
                    <Calendar className="h-4 w-4 text-gray-400" />
                    <div>
                      <p className="text-sm text-gray-500">Last Scan</p>
                      <p className="font-medium">
                        {team.lastScanDate ? formatDate(team.lastScanDate).date : 'N/A'}
                      </p>
                      {team.lastScanDate && (
                        <p className="text-xs text-gray-400">{formatDate(team.lastScanDate).relative}</p>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <div className="space-y-4">
              <div>
                <h4 className="text-lg font-medium text-gray-900 mb-3">Team Metrics</h4>
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
            <h4 className="text-lg font-medium text-gray-900 mb-3">Coverage Analysis</h4>
            <div className="bg-gray-50 rounded-lg p-4">
              <div className="flex items-center justify-between mb-2">
                <span className="text-sm font-medium text-gray-700">Average Coverage</span>
                <span className="text-sm font-bold text-gray-900">{team.averageCoverageRate.toFixed(1)}%</span>
              </div>
              <div className="w-full bg-gray-200 rounded-full h-4 relative overflow-hidden">
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
                <span>{team.totalAnnotatedMethods} annotated methods</span>
                <span>{team.totalTestMethods - team.totalAnnotatedMethods} remaining</span>
              </div>
            </div>
          </div>

          {/* Repositories Section */}
          <div>
            <div className="flex items-center justify-between mb-4">
              <h4 className="text-lg font-medium text-gray-900">Repositories</h4>
              <div className="text-sm text-gray-500">
                {filteredRepos.length} of {team.repositories.length} repositories
              </div>
            </div>

            {/* Repository Filters */}
            <div className="bg-gray-50 rounded-lg p-4 mb-4">
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
                      className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm"
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
                    className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm"
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
                    className="px-3 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm"
                  >
                    {repoSortOrder === 'asc' ? '↑' : '↓'}
                  </button>
                </div>
              </div>
            </div>

            {/* Repositories Table */}
            <div className="overflow-x-auto">
              <table className="min-w-full bg-white border border-gray-200 rounded-lg">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Repository
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Test Classes
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Test Methods
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Coverage
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Last Scan
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {currentRepos.map((repo) => (
                    <tr key={repo.repositoryId} className="hover:bg-gray-50">
                      <td className="px-4 py-3">
                        <div>
                          <div className="text-sm font-medium text-gray-900">{repo.repositoryName}</div>
                          <div className="text-sm text-gray-500 truncate max-w-xs">{repo.gitUrl}</div>
                        </div>
                      </td>
                      <td className="px-4 py-3 text-sm text-gray-900">{repo.testClassCount}</td>
                      <td className="px-4 py-3 text-sm text-gray-900">{repo.testMethodCount}</td>
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
                          <span className="text-sm text-gray-900">{repo.coverageRate.toFixed(1)}%</span>
                        </div>
                      </td>
                      <td className="px-4 py-3 text-sm text-gray-500">
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
                <div className="text-sm text-gray-700">
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
                    className="px-3 py-1 text-sm border border-gray-300 rounded hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
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
                        key={pageNum}
                        onClick={() => handleRepoPageChange(pageNum)}
                        className={`px-3 py-1 text-sm border rounded ${
                          pageNum === repoCurrentPage
                            ? 'bg-blue-50 border-blue-500 text-blue-600'
                            : 'border-gray-300 text-gray-700 hover:bg-gray-50'
                        }`}
                      >
                        {pageNum}
                      </button>
                    );
                  })}
                  
                  <button
                    onClick={() => handleRepoPageChange(repoCurrentPage + 1)}
                    disabled={repoCurrentPage === repoTotalPages}
                    className="px-3 py-1 text-sm border border-gray-300 rounded hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    Next
                  </button>
                </div>
              </div>
            )}

            {filteredRepos.length === 0 && (
              <div className="text-center py-8">
                <GitBranch className="h-12 w-12 text-gray-400 mx-auto mb-3" />
                <h3 className="text-lg font-semibold text-gray-900 mb-2">No repositories found</h3>
                <p className="text-gray-600">
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
  const [filteredTeams, setFilteredTeams] = useState<TeamMetrics[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [sortBy, setSortBy] = useState<'name' | 'repositories' | 'coverage'>('name');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage] = useState(10);
  const { isOpen: isDetailOpen, open: openDetailModal, close: closeDetailModal } = useModal();
  const [selectedTeam, setSelectedTeam] = useState<TeamMetrics | null>(null);

  useEffect(() => {
    fetchTeams();
  }, []);

  const fetchTeams = async () => {
    try {
      setLoading(true);
      const data = await api.teams.getAll();
      setTeams(data);
    } catch (err) {
      setError('Failed to fetch teams data');
      console.error('Error fetching teams:', err);
    } finally {
      setLoading(false);
    }
  };

  const filterAndSortTeams = useCallback(() => {
    const filtered = teams.filter(team =>
      team.teamName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      team.teamCode.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (team.department && team.department.toLowerCase().includes(searchTerm.toLowerCase()))
    );

    filtered.sort((a, b) => {
      let aValue: string | number;
      let bValue: string | number;

      switch (sortBy) {
        case 'name':
          aValue = a.teamName.toLowerCase();
          bValue = b.teamName.toLowerCase();
          break;
        case 'repositories':
          aValue = a.repositoryCount;
          bValue = b.repositoryCount;
          break;
        case 'coverage':
          aValue = a.averageCoverageRate;
          bValue = b.averageCoverageRate;
          break;
        default:
          aValue = a.teamName.toLowerCase();
          bValue = b.teamName.toLowerCase();
      }

      if (sortOrder === 'asc') {
        return aValue < bValue ? -1 : aValue > bValue ? 1 : 0;
      } else {
        return aValue > bValue ? -1 : aValue < bValue ? 1 : 0;
      }
    });

    setFilteredTeams(filtered);
  }, [teams, searchTerm, sortBy, sortOrder]);

  useEffect(() => {
    filterAndSortTeams();
    setCurrentPage(1); // Reset to first page when filters change
  }, [filterAndSortTeams]);

  const handleTeamClick = (team: TeamMetrics) => {
    setSelectedTeam(team);
    openDetailModal();
  };

  // Bulk operations
  const bulkOps = useBulkOperations({
    items: filteredTeams,
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
  const totalPages = Math.ceil(filteredTeams.length / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  const currentTeams = filteredTeams.slice(startIndex, endIndex);

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
          <div className="text-red-600 mb-4">{error}</div>
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
        <Users className="h-8 w-8 text-blue-600 mr-3" />
        <h1 className="text-3xl font-bold text-gray-900">Teams</h1>
        </div>
        <ExportManager
          data={teams}
          dataType="teams"
          selectedItems={bulkOps.selectedItems}
          filteredData={filteredTeams}
          onExport={handleExport}
        />
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

      {/* Filters and Search */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4 mb-6">
        <div className="flex flex-col lg:flex-row gap-4">
          <div className="flex-1">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
              <input
                type="text"
                placeholder="Search teams by name, code, or department..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
          </div>
          <div className="flex gap-2">
            <select
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value as 'name' | 'repositories' | 'coverage')}
              className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              <option value="name">Sort by Name</option>
              <option value="repositories">Sort by Repositories</option>
              <option value="coverage">Sort by Coverage</option>
            </select>
            <button
              onClick={() => setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc')}
              className="px-3 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              {sortOrder === 'asc' ? '↑' : '↓'}
            </button>
          </div>
        </div>
        <div className="mt-3 text-sm text-gray-600">
          Showing {filteredTeams.length} of {teams.length} teams
        </div>
      </div>

      {/* Bulk Operations */}
      <BulkOperations
        selectedItems={bulkOps.selectedItems}
        totalItems={filteredTeams.length}
        onSelectAll={bulkOps.selectAll}
        onClearSelection={bulkOps.clearSelection}
        actions={bulkActions}
        itemType="teams"
        className="mb-6"
      />

      {/* Teams Table */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  <button
                    onClick={bulkOps.selectAll}
                    className="flex items-center hover:text-gray-700"
                  >
                    {bulkOps.isAllSelected ? (
                      <CheckCircle className="h-4 w-4 mr-2" />
                    ) : (
                      <div className="h-4 w-4 mr-2 border-2 border-gray-400 rounded"></div>
                    )}
                    Select All
                  </button>
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Team
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Repositories
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Test Classes
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Test Methods
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Coverage Rate
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {currentTeams.map((team) => (
                <tr key={team.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <button
                      onClick={() => bulkOps.toggleItem(team.id)}
                      className="text-gray-400 hover:text-gray-600"
                    >
                      {bulkOps.isSelected(team.id) ? (
                        <CheckCircle className="h-4 w-4" />
                      ) : (
                        <div className="h-4 w-4 border-2 border-gray-400 rounded"></div>
                      )}
                    </button>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div>
                      <div className="text-sm font-medium text-gray-900">{team.teamName}</div>
                      <div className="text-sm text-gray-500">{team.teamCode}</div>
                      {team.department && (
                        <div className="text-xs text-gray-400">{team.department}</div>
                      )}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-900">{team.repositoryCount}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-900">{team.totalTestClasses}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-900">{team.totalTestMethods}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center">
                      <div className="w-20 bg-gray-200 rounded-full h-2 mr-3">
                        <div
                          className={`h-2 rounded-full transition-all duration-300 ${
                            team.averageCoverageRate >= 80 ? 'bg-green-500' :
                            team.averageCoverageRate >= 60 ? 'bg-yellow-500' : 'bg-red-500'
                          }`}
                          style={{ width: `${Math.min(team.averageCoverageRate, 100)}%` }}
                        />
                      </div>
                      <span className="text-sm font-medium text-gray-900">
                        {team.averageCoverageRate.toFixed(1)}%
                      </span>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                    <button
                      onClick={() => handleTeamClick(team)}
                      className="text-blue-600 hover:text-blue-900 flex items-center gap-1"
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

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="bg-white px-4 py-3 flex items-center justify-between border-t border-gray-200 sm:px-6">
            <div className="flex-1 flex justify-between sm:hidden">
              <button
                onClick={() => handlePageChange(currentPage - 1)}
                disabled={currentPage === 1}
                className="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Previous
              </button>
              <button
                onClick={() => handlePageChange(currentPage + 1)}
                disabled={currentPage === totalPages}
                className="ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Next
              </button>
            </div>
            <div className="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
              <div>
                <p className="text-sm text-gray-700">
                  Showing{' '}
                  <span className="font-medium">{startIndex + 1}</span>
                  {' '}to{' '}
                  <span className="font-medium">{Math.min(endIndex, filteredTeams.length)}</span>
                  {' '}of{' '}
                  <span className="font-medium">{filteredTeams.length}</span>
                  {' '}results
                </p>
              </div>
              <div>
                <nav className="relative z-0 inline-flex rounded-md shadow-sm -space-x-px" aria-label="Pagination">
                  <button
                    onClick={() => handlePageChange(currentPage - 1)}
                    disabled={currentPage === 1}
                    className="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    <ChevronLeft className="h-5 w-5" />
                  </button>
                  
                  {/* Page numbers */}
                  {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                    let pageNum;
                    if (totalPages <= 5) {
                      pageNum = i + 1;
                    } else if (currentPage <= 3) {
                      pageNum = i + 1;
                    } else if (currentPage >= totalPages - 2) {
                      pageNum = totalPages - 4 + i;
                    } else {
                      pageNum = currentPage - 2 + i;
                    }
                    
                    return (
                      <button
                        key={pageNum}
                        onClick={() => handlePageChange(pageNum)}
                        className={`relative inline-flex items-center px-4 py-2 border text-sm font-medium ${
                          pageNum === currentPage
                            ? 'z-10 bg-blue-50 border-blue-500 text-blue-600'
                            : 'bg-white border-gray-300 text-gray-500 hover:bg-gray-50'
                        }`}
                      >
                        {pageNum}
                      </button>
                    );
                  })}
                  
                  <button
                    onClick={() => handlePageChange(currentPage + 1)}
                    disabled={currentPage === totalPages}
                    className="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    <ChevronRight className="h-5 w-5" />
                  </button>
                </nav>
              </div>
            </div>
          </div>
        )}
      </div>
      
      {filteredTeams.length === 0 && (
        <div className="text-center py-12">
          <Users className="h-16 w-16 text-gray-400 mx-auto mb-4" />
          <h3 className="text-lg font-semibold text-gray-900 mb-2">No teams found</h3>
          <p className="text-gray-600">
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