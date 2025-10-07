import React, { useState, useMemo } from 'react';
import { 
  Search, 
  Filter, 
  ChevronUp, 
  ChevronDown, 
  Play, 
  Eye,
  CheckSquare,
  Square,
  FolderOpen
} from 'lucide-react';
import { type RepositorySummary } from '../../lib/api';
import GitUrlLink from '../shared/GitUrlLink';

interface RepositoryListProps {
  repositories: RepositorySummary[];
  onRepositoryClick: (repository: RepositorySummary) => void;
  onBulkScan: (repositoryIds: number[]) => void;
  loading?: boolean;
  selectedRepositories?: Set<number>;
  onSelectRepository?: (repositoryId: number) => void;
  onSelectAll?: () => void;
}

type SortField = 'repositoryName' | 'teamName' | 'testClassCount' | 'testMethodCount' | 'coverageRate' | 'lastScanDate';
type SortDirection = 'asc' | 'desc';

const RepositoryList: React.FC<RepositoryListProps> = ({ 
  repositories,
  onRepositoryClick, 
  onBulkScan,
  loading = false,
  selectedRepositories: externalSelectedRepositories,
  onSelectRepository: externalOnSelectRepository,
  onSelectAll: externalOnSelectAll
}) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [teamFilter, setTeamFilter] = useState<string>('');
  const [coverageFilter, setCoverageFilter] = useState<string>('');
  const [sortField, setSortField] = useState<SortField>('repositoryName');
  const [sortDirection, setSortDirection] = useState<SortDirection>('asc');
  const [showFilters, setShowFilters] = useState(false);

  // Use external bulk operations if provided, otherwise use internal state
  const selectedRepositories = externalSelectedRepositories || new Set<number>();
  const onSelectRepository = externalOnSelectRepository || (() => {});
  const onSelectAll = externalOnSelectAll || (() => {});

  // Get unique teams for filter
  const uniqueTeams = useMemo(() => {
    const teams = new Set(repositories.map(repo => repo.teamName));
    return Array.from(teams).sort();
  }, [repositories]);

  // Filter and sort repositories
  const filteredAndSortedRepositories = useMemo(() => {
    const filtered = repositories.filter(repo => {
      const matchesSearch = repo.repositoryName.toLowerCase().includes(searchTerm.toLowerCase()) ||
                           repo.teamName.toLowerCase().includes(searchTerm.toLowerCase());
      const matchesTeam = !teamFilter || repo.teamName === teamFilter;
      const matchesCoverage = !coverageFilter || (
        coverageFilter === 'high' && repo.coverageRate >= 80 ||
        coverageFilter === 'medium' && repo.coverageRate >= 50 && repo.coverageRate < 80 ||
        coverageFilter === 'low' && repo.coverageRate < 50
      );
      
      return matchesSearch && matchesTeam && matchesCoverage;
    });

    // Sort repositories
    filtered.sort((a, b) => {
      let aValue: string | number = a[sortField];
      let bValue: string | number = b[sortField];

      if (sortField === 'lastScanDate') {
        aValue = new Date(aValue).getTime();
        bValue = new Date(bValue).getTime();
      }

      if (typeof aValue === 'string') {
        aValue = aValue.toLowerCase();
        bValue = (bValue as string).toLowerCase();
      }

      if (sortDirection === 'asc') {
        return aValue < bValue ? -1 : aValue > bValue ? 1 : 0;
      } else {
        return aValue > bValue ? -1 : aValue < bValue ? 1 : 0;
      }
    });

    return filtered;
  }, [repositories, searchTerm, teamFilter, coverageFilter, sortField, sortDirection]);

  const handleSort = (field: SortField) => {
    if (sortField === field) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortField(field);
      setSortDirection('asc');
    }
  };

  const handleSelectRepository = (repositoryId: number) => {
    onSelectRepository(repositoryId);
  };

  const handleSelectAll = () => {
    onSelectAll();
  };

  const handleBulkScan = () => {
    onBulkScan(Array.from(selectedRepositories));
  };

  const clearFilters = () => {
    setSearchTerm('');
    setTeamFilter('');
    setCoverageFilter('');
  };

  const getSortIcon = (field: SortField) => {
    if (sortField !== field) return null;
    return sortDirection === 'asc' ? 
      <ChevronUp className="h-4 w-4" /> : 
      <ChevronDown className="h-4 w-4" />;
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-2 text-gray-600 dark:text-gray-400">Loading repositories...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">

      {/* Server side filters panel */}
      {showFilters && (
        <div className="card">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Team</label>
              <select
                value={teamFilter}
                onChange={(e) => setTeamFilter(e.target.value)}
                className="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="">All Teams</option>
                {uniqueTeams.map(team => (
                  <option key={team} value={team}>{team}</option>
                ))}
              </select>
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Coverage</label>
              <select
                value={coverageFilter}
                onChange={(e) => setCoverageFilter(e.target.value)}
                className="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="">All Coverage</option>
                <option value="high">High (≥80%)</option>
                <option value="medium">Medium (50-79%)</option>
                <option value="low">Low (&lt;50%)</option>
              </select>
            </div>
            
            <div className="flex items-end">
              <button
                onClick={clearFilters}
                className="btn btn-secondary w-full"
              >
                Clear Filters
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Selected Count Summary */}
      {selectedRepositories.size > 0 && (
        <div className="flex items-center justify-end text-sm text-gray-600 dark:text-gray-400">
          <span className="text-blue-600 dark:text-blue-400 font-medium">
            {selectedRepositories.size} selected
          </span>
        </div>
      )}

      {/* Repository Table */}
      <div className="card p-0 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
            <thead className="bg-gray-50 dark:bg-gray-800">
              <tr>
                <th className="px-6 py-3 text-left">
                  <button
                    onClick={handleSelectAll}
                    className="flex items-center text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider hover:text-gray-700 dark:hover:text-gray-300"
                  >
                    {selectedRepositories.size === filteredAndSortedRepositories.length && filteredAndSortedRepositories.length > 0 ? (
                      <CheckSquare className="h-4 w-4 mr-2" />
                    ) : (
                      <Square className="h-4 w-4 mr-2" />
                    )}
                    Select All
                  </button>
                </th>
                <th className="px-6 py-3 text-left">
                  <button
                    onClick={() => handleSort('repositoryName')}
                    className="flex items-center text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider hover:text-gray-700 dark:hover:text-gray-300"
                  >
                    Repository Name
                    {getSortIcon('repositoryName')}
                  </button>
                </th>
                <th className="px-6 py-3 text-left">
                  <button
                    onClick={() => handleSort('teamName')}
                    className="flex items-center text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider hover:text-gray-700 dark:hover:text-gray-300"
                  >
                    Team
                    {getSortIcon('teamName')}
                  </button>
                </th>
                <th className="px-6 py-3 text-left">
                  <button
                    onClick={() => handleSort('testClassCount')}
                    className="flex items-center text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider hover:text-gray-700 dark:hover:text-gray-300"
                  >
                    Test Classes
                    {getSortIcon('testClassCount')}
                  </button>
                </th>
                <th className="px-6 py-3 text-left">
                  <button
                    onClick={() => handleSort('testMethodCount')}
                    className="flex items-center text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider hover:text-gray-700 dark:hover:text-gray-300"
                  >
                    Test Methods
                    {getSortIcon('testMethodCount')}
                  </button>
                </th>
                <th className="px-6 py-3 text-left">
                  <button
                    onClick={() => handleSort('coverageRate')}
                    className="flex items-center text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider hover:text-gray-700 dark:hover:text-gray-300"
                  >
                    Coverage
                    {getSortIcon('coverageRate')}
                  </button>
                </th>
                <th className="px-6 py-3 text-left">
                  <button
                    onClick={() => handleSort('lastScanDate')}
                    className="flex items-center text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider hover:text-gray-700 dark:hover:text-gray-300"
                  >
                    Last Scan
                    {getSortIcon('lastScanDate')}
                  </button>
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white dark:bg-gray-900 divide-y divide-gray-200 dark:divide-gray-700">
              {filteredAndSortedRepositories.map((repository) => (
                <tr key={repository.repositoryId} className="hover:bg-gray-50 dark:hover:bg-gray-800">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <button
                      onClick={() => handleSelectRepository(repository.repositoryId)}
                      className="text-gray-400 dark:text-gray-500 hover:text-gray-600 dark:hover:text-gray-400"
                    >
                      {selectedRepositories.has(repository.repositoryId) ? (
                        <CheckSquare className="h-4 w-4" />
                      ) : (
                        <Square className="h-4 w-4" />
                      )}
                    </button>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm font-medium text-gray-900 dark:text-gray-100">
                      {repository.repositoryName}
                    </div>
                    <GitUrlLink 
                      url={repository.gitUrl} 
                      className="mt-1"
                      truncate={true}
                      maxWidth="max-w-xs"
                    />
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-300">
                      {repository.teamName}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-100">
                    {repository.testClassCount}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-100">
                    {repository.testMethodCount}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center">
                      <div className="flex-1 bg-gray-200 dark:bg-gray-700 rounded-full h-2 mr-2">
                        <div 
                          className="bg-orange-custom h-2 rounded-full" 
                          style={{ width: `${repository.coverageRate}%` }}
                        ></div>
                      </div>
                      <span className="text-sm text-gray-900 dark:text-gray-100 font-medium">
                        {repository.coverageRate.toFixed(1)}%
                      </span>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                    {formatDate(repository.lastScanDate)}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                    <div className="flex items-center space-x-2">
                      <button
                        onClick={() => onRepositoryClick(repository)}
                        className="text-blue-600 dark:text-blue-400 hover:text-blue-900 dark:hover:text-blue-300"
                        title="View Details"
                      >
                        <Eye className="h-4 w-4" />
                      </button>
                      <button
                        onClick={() => onBulkScan([repository.repositoryId])}
                        className="text-green-600 dark:text-green-400 hover:text-green-900 dark:hover:text-green-300"
                        title="Scan Repository"
                      >
                        <Play className="h-4 w-4" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        
        {filteredAndSortedRepositories.length === 0 && (
          <div className="text-center py-12">
            <FolderOpen className="h-12 w-12 text-gray-400 dark:text-gray-500 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100 mb-2">No repositories found</h3>
            <p className="text-gray-600 dark:text-gray-400">
              {searchTerm || teamFilter || coverageFilter 
                ? 'Try adjusting your search or filters'
                : 'No repositories are available'
              }
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

export default RepositoryList;
