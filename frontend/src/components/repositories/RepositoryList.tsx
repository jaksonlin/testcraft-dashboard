import React from 'react';
import { 
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

const RepositoryList: React.FC<RepositoryListProps> = ({ 
  repositories,
  onRepositoryClick, 
  onBulkScan,
  loading = false,
  selectedRepositories: externalSelectedRepositories,
  onSelectRepository: externalOnSelectRepository,
  onSelectAll: externalOnSelectAll
}) => {
  // Use external bulk operations if provided, otherwise use internal state
  const selectedRepositories = externalSelectedRepositories || new Set<number>();
  const onSelectRepository = externalOnSelectRepository || (() => {});
  const onSelectAll = externalOnSelectAll || (() => {});

  const handleSelectRepository = (repositoryId: number) => {
    onSelectRepository(repositoryId);
  };

  const handleSelectAll = () => {
    onSelectAll();
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
                    {selectedRepositories.size === repositories.length && repositories.length > 0 ? (
                      <CheckSquare className="h-4 w-4 mr-2" />
                    ) : (
                      <Square className="h-4 w-4 mr-2" />
                    )}
                    Select All
                  </button>
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  Repository Name
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  Team
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  Test Classes
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  Test Methods
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  Coverage
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  Last Scan
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white dark:bg-gray-900 divide-y divide-gray-200 dark:divide-gray-700">
              {repositories.map((repository) => (
                <tr key={repository.id} className="hover:bg-gray-50 dark:hover:bg-gray-800">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <button
                      onClick={() => handleSelectRepository(repository.id)}
                      className="text-gray-400 dark:text-gray-500 hover:text-gray-600 dark:hover:text-gray-400"
                    >
                      {selectedRepositories.has(repository.id) ? (
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
                        onClick={() => onBulkScan([repository.id])}
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
        
        {repositories.length === 0 && (
          <div className="text-center py-12">
            <FolderOpen className="h-12 w-12 text-gray-400 dark:text-gray-500 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100 mb-2">No repositories found</h3>
            <p className="text-gray-600 dark:text-gray-400">
              No repositories are available
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

export default RepositoryList;
