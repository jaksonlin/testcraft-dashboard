import React, { useState } from 'react';
import { RefreshCw, AlertCircle } from 'lucide-react';
import { api, type RepositorySummary } from '../lib/api';

// Import refactored components
import DashboardHeader from '../components/dashboard/DashboardHeader';
import StatsOverview from '../components/dashboard/StatsOverview';
import TeamPerformanceChart from '../components/dashboard/TeamPerformanceChart';
import CoverageChart from '../components/dashboard/CoverageChart';
import RepositoriesTable from '../components/dashboard/RepositoriesTable';
import ReportsSection from '../components/reports/ReportsSection';
import RepositoryDetailModal from '../components/reports/RepositoryDetailModal';
import ScanConfigModal from '../components/config/ScanConfigModal';

// Import custom hooks
import { useDashboardData } from '../hooks/useDashboardData';
import { useScanConfig } from '../hooks/useScanConfig';
import { useModal } from '../hooks/useModal';

const DashboardView: React.FC = () => {
  const configModal = useModal();
  const repositoryDetailModal = useModal();
  const [scanning, setScanning] = useState(false);
  const [selectedRepository, setSelectedRepository] = useState<RepositorySummary | null>(null);

  const {
    overview,
    teamMetrics,
    scanStatus,
    scanConfig,
    loading,
    error,
    lastRefreshTime,
    fetchDashboardData,
    fetchScanConfig
  } = useDashboardData(configModal.isOpen);

  const {
    configLoading,
    configError,
    configSuccess,
    handleConfigSubmit,
    clearMessages
  } = useScanConfig(scanConfig, fetchScanConfig);

  const handleManualScan = async () => {
    try {
      setScanning(true);
      await api.scan.trigger();
      // Refresh data after scan
      setTimeout(fetchDashboardData, 2000);
    } catch (err) {
      console.error('Error triggering scan:', err);
    } finally {
      setScanning(false);
    }
  };

  const handleOpenConfig = async () => {
    configModal.open();
    if (!scanConfig) {
      await fetchScanConfig();
    }
  };

  const handleCloseConfig = () => {
    configModal.close();
    clearMessages();
  };

  const handleRepositoryClick = (repository: RepositorySummary) => {
    setSelectedRepository(repository);
    repositoryDetailModal.open();
  };

  const handleCloseRepositoryDetail = () => {
    repositoryDetailModal.close();
    setSelectedRepository(null);
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <RefreshCw className="h-8 w-8 animate-spin mx-auto text-primary-600" />
          <p className="mt-2 text-gray-600">Loading dashboard...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="card max-w-md text-center">
          <AlertCircle className="h-12 w-12 text-red-500 mx-auto mb-4" />
          <h2 className="text-xl font-bold text-gray-900 mb-2">Connection Error</h2>
          <p className="text-gray-600 mb-4">{error}</p>
          <button 
            onClick={() => fetchDashboardData()}
            className="btn btn-primary"
          >
            Retry Connection
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <DashboardHeader
        scanStatus={scanStatus}
        lastRefreshTime={lastRefreshTime}
        scanning={scanning}
        onOpenConfig={handleOpenConfig}
        onManualScan={handleManualScan}
      />

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Stats Overview */}
        <StatsOverview overview={overview} />

        {/* Charts Section */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
          <TeamPerformanceChart teamMetrics={teamMetrics} />
          <CoverageChart overview={overview} />
        </div>

        {/* Top Repositories Table */}
        <RepositoriesTable 
          repositories={overview?.topRepositories || null}
          onRepositoryClick={handleRepositoryClick}
        />

        {/* Reports & Analytics Section */}
        <ReportsSection 
          repositories={overview?.topRepositories || []} 
          dashboardOverview={overview || undefined}
        />
      </main>

      {/* Repository Detail Modal */}
      <RepositoryDetailModal
        isOpen={repositoryDetailModal.isOpen}
        onClose={handleCloseRepositoryDetail}
        repository={selectedRepository}
      />

      {/* Configuration Modal */}
      <ScanConfigModal
        isOpen={configModal.isOpen}
        onClose={handleCloseConfig}
        scanConfig={scanConfig}
        configLoading={configLoading}
        configError={configError}
        configSuccess={configSuccess}
        onSubmit={handleConfigSubmit}
        onRetry={fetchScanConfig}
      />
    </div>
  );
};

export default DashboardView;
