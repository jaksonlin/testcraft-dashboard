import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { FolderOpen, Download, AlertCircle, CheckCircle, Play, Trash2, RefreshCw } from 'lucide-react';
import RepositoryList from '../components/repositories/RepositoryList';
import AdvancedFilter, { type FilterOption } from '../components/shared/AdvancedFilter';
import BulkOperations, { type BulkAction } from '../components/shared/BulkOperations';
import { useBulkOperations } from '../hooks/useBulkOperations';
import { api, type RepositorySummary } from '../lib/api';

const RepositoriesView: React.FC = () => {
  const navigate = useNavigate();
  const [scanning, setScanning] = useState(false);
  const [scanResults, setScanResults] = useState<{ success: number; failed: number } | null>(null);
  const [repositories, setRepositories] = useState<RepositorySummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [teamOptions, setTeamOptions] = useState<{ value: string; label: string }[]>([]);

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
      type: 'range',
      min: 0,
      max: 100,
      step: 1
    },
    {
      id: 'testMethods',
      label: 'Test Methods Range',
      type: 'range',
      min: 0,
      max: 1000,
      step: 1
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

  // Initialize repositories data
  React.useEffect(() => {
    const fetchRepositories = async () => {
      try {
        setLoading(true);
        const data = await api.repositories.getAll();
        setRepositories(data);
        
        // Update team filter options
        const uniqueTeams = Array.from(new Set(data.map(repo => repo.teamName)))
          .sort()
          .map(team => ({ value: team, label: team }));
        
        setTeamOptions(uniqueTeams);
      } catch (err) {
        console.error('Error fetching repositories:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchRepositories();
  }, []);

  // Advanced filtering
  const [searchTerm, setSearchTerm] = useState('');
  const [filters, setFilters] = useState<Record<string, string | number | boolean | null>>({});

  const filteredRepositories = React.useMemo(() => {
    let filtered = [...repositories];

    // Apply search filter
    if (searchTerm.trim()) {
      const searchLower = searchTerm.toLowerCase();
      filtered = filtered.filter(repo =>
        repo.repositoryName.toLowerCase().includes(searchLower) ||
        repo.teamName.toLowerCase().includes(searchLower) ||
        repo.gitUrl.toLowerCase().includes(searchLower)
      );
    }

    // Apply advanced filters
    Object.entries(filters).forEach(([key, filterValue]) => {
      if (filterValue === null || filterValue === '' || (Array.isArray(filterValue) && filterValue.length === 0)) {
        return;
      }

      filtered = filtered.filter(repo => {
        switch (key) {
          case 'team':
            return repo.teamName === filterValue;
          case 'coverage':
            return repo.coverageRate >= (filterValue as number);
          case 'testMethods':
            return repo.testMethodCount >= (filterValue as number);
          case 'lastScan': {
            const date = new Date(repo.lastScanDate);
            const now = new Date();
            const diffDays = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60 * 24));
            
            switch (filterValue) {
              case 'today': return diffDays === 0;
              case 'week': return diffDays <= 7;
              case 'month': return diffDays <= 30;
              case 'older': return diffDays > 30;
              default: return true;
            }
          }
          default:
            return true;
        }
      });
    });

    return filtered;
  }, [repositories, searchTerm, filters]);

  const hasActiveFilters = searchTerm.trim() !== '' || Object.values(filters).some(value =>
    value !== null && value !== '' && (Array.isArray(value) ? value.length > 0 : true)
  );

  const clearAllFilters = () => {
    setSearchTerm('');
    setFilters({});
  };

  // Bulk operations
  const bulkOps = useBulkOperations({
    items: filteredRepositories,
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
        await handleBulkExport(selectedIds);
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

  const handleExportAll = async () => {
    try {
      const overview = await api.dashboard.getOverview();
      const repositories = overview.topRepositories || [];
      const data = {
        exportDate: new Date().toISOString(),
        totalRepositories: repositories.length,
        repositories: repositories
      };

      const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `repositories-export-${new Date().toISOString().split('T')[0]}.json`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    } catch (err) {
      console.error('Error exporting repositories:', err);
    }
  };

  const handleBulkExport = async (repositoryIds: number[]) => {
    try {
      const selectedRepos = repositories.filter(repo => repositoryIds.includes(repo.repositoryId));
      const data = {
        exportDate: new Date().toISOString(),
        totalRepositories: selectedRepos.length,
        repositories: selectedRepos
      };

      const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `repositories-export-${new Date().toISOString().split('T')[0]}.json`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    } catch (err) {
      console.error('Error exporting selected repositories:', err);
    }
  };

  const handleBulkRefresh = async () => {
    try {
      setScanning(true);
      // Trigger refresh for all repositories
      const result = await api.scan.trigger();
      
      if (result.success) {
        // Refresh the repositories data
        const data = await api.repositories.getAll();
        setRepositories(data);
        
        // Update team filter options
        const uniqueTeams = Array.from(new Set(data.map(repo => repo.teamName)))
          .sort()
          .map(team => ({ value: team, label: team }));
        
        setTeamOptions(uniqueTeams);
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
        
        <div className="flex gap-2">
          <button
            onClick={handleExportAll}
            className="btn btn-secondary"
          >
            <Download className="h-4 w-4 mr-2" />
            Export All
          </button>
        </div>
      </div>

      {/* Scan Results Banner */}
      {scanResults && (
        <div className={`card mb-6 ${
          scanResults.failed === 0 ? 'bg-green-50 border-green-200' : 'bg-red-50 border-red-200'
        }`}>
          <div className="flex items-center">
            {scanResults.failed === 0 ? (
              <CheckCircle className="h-5 w-5 text-green-600 mr-3" />
            ) : (
              <AlertCircle className="h-5 w-5 text-red-600 mr-3" />
            )}
            <div>
              <h3 className={`font-semibold ${
                scanResults.failed === 0 ? 'text-green-800' : 'text-red-800'
              }`}>
                Bulk Scan {scanResults.failed === 0 ? 'Completed' : 'Failed'}
              </h3>
              <p className={`text-sm ${
                scanResults.failed === 0 ? 'text-green-700' : 'text-red-700'
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
        onFilterChange={(filters) => setFilters(filters as Record<string, string | number | boolean | null>)}
        onSearchChange={setSearchTerm}
        searchPlaceholder="Search repositories, teams, or URLs..."
        className="mb-6"
      />

      {/* Results Summary */}
      <div className="flex items-center justify-between text-sm text-gray-600 mb-4">
        <span>
          Showing {filteredRepositories.length} of {repositories.length} repositories
          {hasActiveFilters && ' (filtered)'}
        </span>
        {hasActiveFilters && (
          <button
            onClick={clearAllFilters}
            className="text-blue-600 hover:text-blue-800 font-medium"
          >
            Clear all filters
          </button>
        )}
      </div>

      {/* Bulk Operations */}
      <BulkOperations
        selectedItems={bulkOps.selectedItems}
        totalItems={filteredRepositories.length}
        onSelectAll={bulkOps.selectAll}
        onClearSelection={bulkOps.clearSelection}
        actions={bulkActions}
        itemType="repositories"
        className="mb-6"
      />

      {/* Repository List */}
      <RepositoryList 
        repositories={filteredRepositories}
        onRepositoryClick={handleRepositoryClick}
        onBulkScan={handleBulkScan}
        loading={loading}
        selectedRepositories={bulkOps.selectedItems}
        onSelectRepository={bulkOps.toggleItem}
        onSelectAll={bulkOps.selectAll}
      />

      {/* Loading Overlay */}
      {scanning && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 max-w-sm w-full mx-4">
            <div className="flex items-center">
              <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600 mr-3"></div>
              <div>
                <h3 className="text-lg font-semibold text-gray-900">Scanning Repositories</h3>
                <p className="text-sm text-gray-600">Please wait while repositories are being scanned...</p>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default RepositoriesView;
