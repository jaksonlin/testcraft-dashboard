import React, { useState, useEffect } from 'react';
import { 
  BarChart, 
  Bar, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell
} from 'recharts';
import { 
  GitBranch, 
  Users, 
  TestTube, 
  CheckCircle, 
  RefreshCw,
  AlertCircle,
  Settings,
  Save,
  X
} from 'lucide-react';
import { api, type DashboardOverview, type TeamSummary, type ScanStatus, type ScanConfig } from '../lib/api';

interface StatCardProps {
  title: string;
  value: number | string;
  icon: React.ReactNode;
  color: string;
  trend?: string;
}

const StatCard: React.FC<StatCardProps> = ({ title, value, icon, color, trend }) => (
  <div className="card">
    <div className="flex items-center justify-between">
      <div>
        <p className="text-sm font-medium text-gray-600">{title}</p>
        <p className="text-2xl font-bold text-gray-900">{value}</p>
        {trend && (
          <p className="text-xs text-green-600 mt-1">{trend}</p>
        )}
      </div>
      <div className={`p-3 rounded-full ${color}`}>
        {icon}
      </div>
    </div>
  </div>
);

const Dashboard: React.FC = () => {
  const [overview, setOverview] = useState<DashboardOverview | null>(null);
  const [teamMetrics, setTeamMetrics] = useState<TeamSummary[]>([]);
  const [scanStatus, setScanStatus] = useState<ScanStatus | null>(null);
  const [scanConfig, setScanConfig] = useState<ScanConfig | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [scanning, setScanning] = useState(false);
  const [showConfigPanel, setShowConfigPanel] = useState(false);
  const [configLoading, setConfigLoading] = useState(false);
  const [configError, setConfigError] = useState<string | null>(null);
  const [configSuccess, setConfigSuccess] = useState<string | null>(null);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const [overviewData, teamsData, statusData, configData] = await Promise.all([
        api.dashboard.getOverview(),
        api.dashboard.getTeamMetrics(),
        api.scan.getStatus(),
        api.scan.getConfig()
      ]);
      
      
      setOverview(overviewData);
      setTeamMetrics(teamsData);
      setScanStatus(statusData);
      setScanConfig(configData);
    } catch (err) {
      console.error('Error fetching dashboard data:', err);
      setError('Failed to load dashboard data. Please check if the backend is running.');
    } finally {
      setLoading(false);
    }
  };

  const fetchScanConfig = async () => {
    try {
      const configData = await api.scan.getConfig();
      setScanConfig(configData);
    } catch (err) {
      console.error('Error fetching scan config:', err);
      setConfigError('Failed to load scan configuration. Please try again.');
    }
  };

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

  const handleConfigUpdate = async (updatedConfig: Partial<ScanConfig>) => {
    try {
      setConfigLoading(true);
      setConfigError(null);
      setConfigSuccess(null);

      const response = await api.scan.updateConfig(updatedConfig);
      
      if (response.success) {
        setConfigSuccess('Configuration updated successfully!');
        await fetchScanConfig(); // Refresh config data
        setTimeout(() => setConfigSuccess(null), 3000);
      } else {
        setConfigError(response.message || 'Failed to update configuration');
      }
    } catch (err) {
      console.error('Error updating scan config:', err);
      setConfigError('Failed to update configuration. Please try again.');
    } finally {
      setConfigLoading(false);
    }
  };

  const handleConfigSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);
    const updatedConfig: Partial<ScanConfig> = {};

    // Only include fields that have been modified
    if (formData.get('tempCloneMode') !== null) {
      updatedConfig.tempCloneMode = formData.get('tempCloneMode') === 'on';
    }
    if (formData.get('repositoryHubPath')) {
      updatedConfig.repositoryHubPath = formData.get('repositoryHubPath') as string;
    }
    if (formData.get('repositoryListFile')) {
      updatedConfig.repositoryListFile = formData.get('repositoryListFile') as string;
    }
    if (formData.get('maxRepositoriesPerScan')) {
      updatedConfig.maxRepositoriesPerScan = parseInt(formData.get('maxRepositoriesPerScan') as string);
    }
    if (formData.get('schedulerEnabled') !== null) {
      updatedConfig.schedulerEnabled = formData.get('schedulerEnabled') === 'on';
    }
    if (formData.get('dailyScanCron')) {
      updatedConfig.dailyScanCron = formData.get('dailyScanCron') as string;
    }

    handleConfigUpdate(updatedConfig);
  };

  useEffect(() => {
    fetchDashboardData();
    
    // Refresh data every 30 seconds
    const interval = setInterval(fetchDashboardData, 30000);
    return () => clearInterval(interval);
  }, []);

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
            onClick={fetchDashboardData}
            className="btn btn-primary"
          >
            Retry Connection
          </button>
        </div>
      </div>
    );
  }

  // Prepare chart data
  const teamChartData = teamMetrics.map(team => ({
    name: team.teamName,
    repositories: team.repositoryCount,
    testMethods: team.testMethodCount,
    coverage: team.coverageRate
  }));

  const coverageData = [
    { name: 'Annotated Methods', value: overview?.totalAnnotatedMethods || 0, color: '#22c55e' },
    { name: 'Unannotated Methods', value: (overview?.totalTestMethods || 0) - (overview?.totalAnnotatedMethods || 0), color: '#e5e7eb' }
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-6">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">TestCraft Dashboard</h1>
              <p className="text-gray-600">Continuous Git Repository Monitoring</p>
            </div>
            <div className="flex items-center space-x-4">
              {scanStatus && (
                <div className="text-right">
                  <p className="text-sm text-gray-600">Last Scan</p>
                  <p className="text-sm font-medium">
                    {scanStatus.lastScanTime 
                      ? new Date(scanStatus.lastScanTime).toLocaleString()
                      : 'Never'
                    }
                  </p>
                </div>
              )}
              <button
                onClick={async () => {
                  setShowConfigPanel(true);
                  if (!scanConfig) {
                    await fetchScanConfig();
                  }
                }}
                className="btn btn-secondary flex items-center space-x-2"
              >
                <Settings className="h-4 w-4" />
                <span>Settings</span>
              </button>
              <button
                onClick={handleManualScan}
                disabled={scanning || scanStatus?.isScanning}
                className="btn btn-primary flex items-center space-x-2"
              >
                {scanning || scanStatus?.isScanning ? (
                  <RefreshCw className="h-4 w-4 animate-spin" />
                ) : (
                  <RefreshCw className="h-4 w-4" />
                )}
                <span>{scanning || scanStatus?.isScanning ? 'Scanning...' : 'Scan Now'}</span>
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Stats Overview */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <StatCard
            title="Total Repositories"
            value={overview?.totalRepositories || 0}
            icon={<GitBranch className="h-6 w-6 text-white" />}
            color="bg-blue-500"
          />
          <StatCard
            title="Teams"
            value={overview?.totalTeams || 0}
            icon={<Users className="h-6 w-6 text-white" />}
            color="bg-green-500"
          />
          <StatCard
            title="Test Methods"
            value={overview?.totalTestMethods || 0}
            icon={<TestTube className="h-6 w-6 text-white" />}
            color="bg-purple-500"
          />
          <StatCard
            title="Coverage Rate"
            value={`${overview?.overallCoverageRate.toFixed(1) || 0}%`}
            icon={<CheckCircle className="h-6 w-6 text-white" />}
            color="bg-orange-500"
          />
        </div>

        {/* Charts Section */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
          {/* Team Performance Chart */}
          <div className="card">
            <div className="card-header">
              <h3 className="text-lg font-semibold text-gray-900">Team Performance</h3>
            </div>
            <div className="h-80">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={teamChartData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="name" />
                  <YAxis />
                  <Tooltip />
                  <Bar dataKey="repositories" fill="#3b82f6" name="Repositories" />
                  <Bar dataKey="testMethods" fill="#10b981" name="Test Methods" />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Coverage Distribution */}
          <div className="card">
            <div className="card-header">
              <h3 className="text-lg font-semibold text-gray-900">Test Method Coverage</h3>
            </div>
            <div className="h-80">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={coverageData}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={100}
                    paddingAngle={5}
                    dataKey="value"
                  >
                    {coverageData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            </div>
            <div className="mt-4 flex justify-center space-x-6">
              {coverageData.map((item, index) => (
                <div key={index} className="flex items-center">
                  <div 
                    className="w-3 h-3 rounded-full mr-2" 
                    style={{ backgroundColor: item.color }}
                  />
                  <span className="text-sm text-gray-600">{item.name}</span>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Top Repositories Table */}
        {overview?.topRepositories && overview.topRepositories.length > 0 && (
          <div className="card">
            <div className="card-header">
              <h3 className="text-lg font-semibold text-gray-900">Top Repositories</h3>
            </div>
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Repository
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Team
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Test Methods
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Coverage
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Last Scan
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {overview.topRepositories.map((repo) => (
                    <tr key={repo.repositoryId}>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div>
                          <div className="text-sm font-medium text-gray-900">{repo.repositoryName}</div>
                          <div className="text-sm text-gray-500">{repo.gitUrl}</div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {repo.teamName}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {repo.testMethodCount}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <div className="text-sm text-gray-900 mr-2">
                            {repo.coverageRate.toFixed(1)}%
                          </div>
                          <div className="w-16 bg-gray-200 rounded-full h-2">
                            <div 
                              className="bg-green-500 h-2 rounded-full" 
                              style={{ width: `${repo.coverageRate}%` }}
                            />
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {new Date(repo.lastScanDate).toLocaleDateString()}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </main>

      {/* Configuration Panel Modal */}
      {showConfigPanel && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          {/* Backdrop with blur effect */}
          <div 
            className="absolute inset-0 bg-black/20 backdrop-blur-sm"
            onClick={() => {
              setShowConfigPanel(false);
              setConfigError(null);
              setConfigSuccess(null);
            }}
          />
          
          {/* Modal Content */}
          <div className="relative w-full max-w-2xl max-h-[90vh] overflow-y-auto bg-white rounded-xl shadow-2xl border border-gray-200 animate-in fade-in-0 zoom-in-95 duration-300">
            {/* Header */}
            <div className="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 rounded-t-xl">
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-3">
                  <div className="p-2 bg-blue-100 rounded-lg">
                    <Settings className="h-5 w-5 text-blue-600" />
                  </div>
                  <div>
                    <h3 className="text-xl font-semibold text-gray-900">Scan Configuration</h3>
                    <p className="text-sm text-gray-500">Configure repository scanning settings</p>
                  </div>
                </div>
                <button
                  onClick={() => {
                    setShowConfigPanel(false);
                    setConfigError(null);
                    setConfigSuccess(null);
                  }}
                  className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-lg transition-colors"
                >
                  <X className="h-5 w-5" />
                </button>
              </div>
            </div>
            
            {/* Content */}
            <div className="p-6">

            {configError && (
              <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-md">
                <div className="flex">
                  <AlertCircle className="h-5 w-5 text-red-400" />
                  <div className="ml-3">
                    <p className="text-sm text-red-800">{configError}</p>
                  </div>
                </div>
              </div>
            )}

            {configSuccess && (
              <div className="mb-4 p-4 bg-green-50 border border-green-200 rounded-md">
                <div className="flex">
                  <CheckCircle className="h-5 w-5 text-green-400" />
                  <div className="ml-3">
                    <p className="text-sm text-green-800">{configSuccess}</p>
                  </div>
                </div>
              </div>
            )}

            {scanConfig ? (
              <form onSubmit={handleConfigSubmit} className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {/* Repository Hub Path */}
                  <div className="space-y-2">
                    <label className="block text-sm font-medium text-gray-700">
                      Repository Hub Path
                    </label>
                    <input
                      type="text"
                      name="repositoryHubPath"
                      defaultValue={scanConfig.repositoryHubPath}
                      className="input w-full"
                      placeholder="./repositories"
                    />
                    <p className="text-xs text-gray-500">
                      Directory containing cloned repositories
                    </p>
                  </div>

                  {/* Repository List File */}
                  <div className="space-y-2">
                    <label className="block text-sm font-medium text-gray-700">
                      Repository List File
                    </label>
                    <input
                      type="text"
                      name="repositoryListFile"
                      defaultValue={scanConfig.repositoryListFile}
                      className="input w-full"
                      placeholder="./sample-repositories.txt"
                    />
                    <p className="text-xs text-gray-500">
                      File containing list of repository URLs to scan
                    </p>
                  </div>

                  {/* Max Repositories Per Scan */}
                  <div className="space-y-2">
                    <label className="block text-sm font-medium text-gray-700">
                      Max Repositories Per Scan
                    </label>
                    <input
                      type="number"
                      name="maxRepositoriesPerScan"
                      defaultValue={scanConfig.maxRepositoriesPerScan}
                      min="1"
                      max="1000"
                      className="input w-full"
                    />
                    <p className="text-xs text-gray-500">
                      Limit to prevent long-running scans (1-1000)
                    </p>
                  </div>

                  {/* Daily Scan Cron */}
                  <div className="space-y-2">
                    <label className="block text-sm font-medium text-gray-700">
                      Daily Scan Schedule (Cron)
                    </label>
                    <input
                      type="text"
                      name="dailyScanCron"
                      defaultValue={scanConfig.dailyScanCron}
                      className="input w-full font-mono text-sm"
                      placeholder="0 0 2 * * ?"
                    />
                    <p className="text-xs text-gray-500">
                      Format: second minute hour day month day-of-week (current: 2 AM daily)
                    </p>
                  </div>
                </div>

                {/* Checkboxes */}
                <div className="space-y-4 pt-4 border-t border-gray-200">
                  <div className="flex items-start space-x-3">
                    <input
                      type="checkbox"
                      name="tempCloneMode"
                      defaultChecked={scanConfig.tempCloneMode}
                      className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded mt-1"
                    />
                    <div className="flex-1">
                      <label className="block text-sm font-medium text-gray-700">
                        Use Temporary Clone Mode
                      </label>
                      <p className="text-xs text-gray-500 mt-1">
                        Clone repositories to temporary directories during scanning
                      </p>
                    </div>
                  </div>

                  <div className="flex items-start space-x-3">
                    <input
                      type="checkbox"
                      name="schedulerEnabled"
                      defaultChecked={scanConfig.schedulerEnabled}
                      className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded mt-1"
                    />
                    <div className="flex-1">
                      <label className="block text-sm font-medium text-gray-700">
                        Enable Automatic Scheduling
                      </label>
                      <p className="text-xs text-gray-500 mt-1">
                        Automatically run scans according to the cron schedule
                      </p>
                    </div>
                  </div>
                </div>

              {/* Action Buttons */}
              <div className="flex justify-end space-x-3 pt-6 border-t border-gray-200">
                <button
                  type="button"
                  onClick={() => {
                    setShowConfigPanel(false);
                    setConfigError(null);
                    setConfigSuccess(null);
                  }}
                  className="btn btn-secondary"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={configLoading}
                  className="btn btn-primary flex items-center space-x-2"
                >
                  {configLoading ? (
                    <RefreshCw className="h-4 w-4 animate-spin" />
                  ) : (
                    <Save className="h-4 w-4" />
                  )}
                  <span>{configLoading ? 'Saving...' : 'Save Configuration'}</span>
                </button>
              </div>
            </form>
          ) : (
            <div className="flex items-center justify-center py-12">
              <div className="text-center">
                <RefreshCw className="h-8 w-8 animate-spin mx-auto text-gray-400 mb-4" />
                <p className="text-gray-500">Loading configuration...</p>
                <button
                  onClick={fetchScanConfig}
                  className="mt-4 btn btn-secondary"
                >
                  Retry
                </button>
              </div>
            </div>
          )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Dashboard;
