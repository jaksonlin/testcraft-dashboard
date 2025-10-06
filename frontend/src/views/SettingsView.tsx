import React, { useState, useEffect } from 'react';
import { Settings, Save, RefreshCw, AlertCircle, CheckCircle, Clock, Database, GitBranch, Shield, Bell, FileText } from 'lucide-react';
import { api } from '../lib/api';

const SettingsView: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'scan' | 'system' | 'notifications' | 'advanced'>('scan');

  // Form state
  const [formData, setFormData] = useState({
    tempCloneMode: false,
    repositoryHubPath: '',
    repositoryListFile: '',
    maxRepositoriesPerScan: 10,
    schedulerEnabled: false,
    dailyScanCron: '0 0 2 * * ?' // Default: 2 AM daily
  });

  const fetchConfig = async () => {
    try {
      setLoading(true);
      setError(null);
      const configData = await api.scan.getConfig();
      setFormData({
        tempCloneMode: configData.tempCloneMode || false,
        repositoryHubPath: configData.repositoryHubPath || '',
        repositoryListFile: configData.repositoryListFile || '',
        maxRepositoriesPerScan: configData.maxRepositoriesPerScan || 10,
        schedulerEnabled: configData.schedulerEnabled || false,
        dailyScanCron: configData.dailyScanCron || '0 0 2 * * ?'
      });
    } catch (err) {
      setError('Failed to load configuration');
      console.error('Error fetching config:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchConfig();
  }, []);

  const handleSave = async () => {
    try {
      setSaving(true);
      setError(null);
      setSuccess(null);

      const response = await api.scan.updateConfig(formData);
      
      if (response.success) {
        setSuccess('Configuration saved successfully');
        await fetchConfig(); // Refresh config
        setTimeout(() => setSuccess(null), 3000);
      } else {
        setError(response.message || 'Failed to save configuration');
      }
    } catch (err) {
      setError('Failed to save configuration');
      console.error('Error saving config:', err);
    } finally {
      setSaving(false);
    }
  };

  const handleInputChange = (field: string, value: string | number | boolean) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const resetToDefaults = () => {
    setFormData({
      tempCloneMode: false,
      repositoryHubPath: '',
      repositoryListFile: '',
      maxRepositoriesPerScan: 10,
      schedulerEnabled: false,
      dailyScanCron: '0 0 2 * * ?'
    });
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

  return (
    <div className="p-8">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center">
        <Settings className="h-8 w-8 text-blue-600 dark:text-blue-400 mr-3" />
        <h1 className="text-3xl font-bold" style={{ color: 'var(--color-foreground)' }}>Settings</h1>
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={fetchConfig}
            className="flex items-center px-3 py-2 rounded-lg transition-colors" style={{ backgroundColor: 'var(--color-secondary)', color: 'var(--color-secondary-foreground)' }}
          >
            <RefreshCw className="h-4 w-4 mr-2" />
            Refresh
          </button>
          <button
            onClick={handleSave}
            disabled={saving}
            className="flex items-center px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            {saving ? (
              <RefreshCw className="h-4 w-4 mr-2 animate-spin" />
            ) : (
              <Save className="h-4 w-4 mr-2" />
            )}
            {saving ? 'Saving...' : 'Save Changes'}
          </button>
        </div>
      </div>

      {/* Status Messages */}
      {error && (
        <div className="mb-6 p-4 border rounded-lg flex items-center" style={{ backgroundColor: 'var(--color-destructive)', borderColor: 'var(--color-destructive)', color: 'var(--color-destructive-foreground)' }}>
          <AlertCircle className="h-5 w-5 text-red-600 dark:text-red-400 mr-3" />
          <span style={{ color: 'var(--color-destructive-foreground)' }}>{error}</span>
        </div>
      )}

      {success && (
        <div className="mb-6 p-4 border rounded-lg flex items-center" style={{ backgroundColor: 'var(--color-success)', borderColor: 'var(--color-success)', color: 'var(--color-success-foreground)' }}>
          <CheckCircle className="h-5 w-5 text-green-600 dark:text-green-400 mr-3" />
          <span style={{ color: 'var(--color-success-foreground)' }}>{success}</span>
        </div>
      )}

      {/* Tab Navigation */}
      <div className="mb-6">
        <nav className="flex space-x-8">
          {[
            { id: 'scan', label: 'Scan Configuration', icon: GitBranch },
            { id: 'system', label: 'System Settings', icon: Database },
            { id: 'notifications', label: 'Notifications', icon: Bell },
            { id: 'advanced', label: 'Advanced', icon: Shield }
          ].map(({ id, label, icon: Icon }) => (
            <button
              key={id}
              onClick={() => setActiveTab(id as 'scan' | 'system' | 'notifications' | 'advanced')}
              className={`flex items-center px-3 py-2 text-sm font-medium rounded-lg transition-colors ${
                activeTab === id ? 'opacity-100' : 'opacity-70'
              }`}
              style={{
                backgroundColor: activeTab === id ? 'var(--color-accent)' : 'transparent',
                color: activeTab === id ? 'var(--color-primary)' : 'var(--color-muted-foreground)'
              }}
              onMouseEnter={(e) => {
                if (activeTab !== id) {
                  e.currentTarget.style.backgroundColor = 'var(--color-accent)';
                  e.currentTarget.style.color = 'var(--color-foreground)';
                }
              }}
              onMouseLeave={(e) => {
                if (activeTab !== id) {
                  e.currentTarget.style.backgroundColor = 'transparent';
                  e.currentTarget.style.color = 'var(--color-muted-foreground)';
                }
              }}
            >
              <Icon className="h-4 w-4 mr-2" />
              {label}
            </button>
          ))}
        </nav>
      </div>
      
      {/* Tab Content */}
      <div className="space-y-6">
        {activeTab === 'scan' && (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Repository Configuration */}
            <div className="rounded-lg shadow-sm border p-6" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)' }}>
              <h3 className="text-lg font-semibold mb-4 flex items-center" style={{ color: 'var(--color-foreground)' }}>
                <GitBranch className="h-5 w-5 mr-2 text-blue-600 dark:text-blue-400" />
                Repository Configuration
              </h3>
              
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Repository Hub Path
                  </label>
                  <input
                    type="text"
                    value={formData.repositoryHubPath}
                    onChange={(e) => handleInputChange('repositoryHubPath', e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-100 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    placeholder="/path/to/repositories"
                  />
                  <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                    Base directory where repositories are stored
                  </p>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Repository List File
                  </label>
                  <input
                    type="text"
                    value={formData.repositoryListFile}
                    onChange={(e) => handleInputChange('repositoryListFile', e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-100 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    placeholder="repositories.txt"
                  />
                  <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                    File containing list of repositories to scan
                  </p>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Max Repositories Per Scan
                  </label>
                  <input
                    type="number"
                    min="1"
                    max="100"
                    value={formData.maxRepositoriesPerScan}
                    onChange={(e) => handleInputChange('maxRepositoriesPerScan', parseInt(e.target.value))}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-100 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                  <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                    Maximum number of repositories to process in a single scan
                  </p>
                </div>
              </div>
            </div>

            {/* Scan Behavior */}
            <div className="rounded-lg shadow-sm border p-6" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)' }}>
              <h3 className="text-lg font-semibold mb-4 flex items-center" style={{ color: 'var(--color-foreground)' }}>
                <Clock className="h-5 w-5 mr-2 text-green-600 dark:text-green-400" />
                Scan Behavior
              </h3>
              
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <div>
                    <label className="text-sm font-medium text-gray-700 dark:text-gray-300">
                      Temporary Clone Mode
                    </label>
                    <p className="text-xs text-gray-500 dark:text-gray-400">
                      Clone repositories to temporary directories during scan
                    </p>
                  </div>
                  <label className="relative inline-flex items-center cursor-pointer">
                    <input
                      type="checkbox"
                      checked={formData.tempCloneMode}
                      onChange={(e) => handleInputChange('tempCloneMode', e.target.checked)}
                      className="sr-only peer"
                    />
                    <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                  </label>
                </div>

                <div className="flex items-center justify-between">
                  <div>
                    <label className="text-sm font-medium text-gray-700 dark:text-gray-300">
                      Scheduler Enabled
                    </label>
                    <p className="text-xs text-gray-500 dark:text-gray-400">
                      Enable automatic scheduled scans
                    </p>
                  </div>
                  <label className="relative inline-flex items-center cursor-pointer">
                    <input
                      type="checkbox"
                      checked={formData.schedulerEnabled}
                      onChange={(e) => handleInputChange('schedulerEnabled', e.target.checked)}
                      className="sr-only peer"
                    />
                    <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                  </label>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Daily Scan Schedule (Cron Expression)
                  </label>
                  <input
                    type="text"
                    value={formData.dailyScanCron}
                    onChange={(e) => handleInputChange('dailyScanCron', e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-100 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    placeholder="0 0 2 * * ?"
                  />
                  <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                    Cron expression for scheduled scans (default: 2 AM daily)
                  </p>
                </div>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'system' && (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Database Configuration */}
            <div className="rounded-lg shadow-sm border p-6" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)' }}>
              <h3 className="text-lg font-semibold mb-4 flex items-center" style={{ color: 'var(--color-foreground)' }}>
                <Database className="h-5 w-5 mr-2 text-purple-600" />
                Database Configuration
              </h3>
              
              <div className="space-y-4">
                <div className="p-4 rounded-lg" style={{ backgroundColor: 'var(--color-muted)' }}>
                  <div className="flex items-center justify-between">
                    <span className="text-sm font-medium" style={{ color: 'var(--color-foreground)' }}>Database Status</span>
                    <span className="px-2 py-1 text-xs font-semibold rounded-full bg-green-100 text-green-800">
                      Connected
                    </span>
                  </div>
                  <p className="text-xs mt-1" style={{ color: 'var(--color-muted-foreground)' }}>
                    H2 Database running on localhost:8090
                  </p>
                </div>

                <div className="p-4 rounded-lg" style={{ backgroundColor: 'var(--color-muted)' }}>
                  <div className="flex items-center justify-between">
                    <span className="text-sm font-medium" style={{ color: 'var(--color-foreground)' }}>Connection Pool</span>
                    <span className="px-2 py-1 text-xs font-semibold rounded-full bg-blue-100 text-blue-800">
                      Active
                    </span>
                  </div>
                  <p className="text-xs mt-1" style={{ color: 'var(--color-muted-foreground)' }}>
                    HikariCP connection pool configured
                  </p>
                </div>
              </div>
            </div>

            {/* System Information */}
            <div className="rounded-lg shadow-sm border p-6" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)' }}>
              <h3 className="text-lg font-semibold mb-4 flex items-center" style={{ color: 'var(--color-foreground)' }}>
                <Shield className="h-5 w-5 mr-2 text-orange-600" />
                System Information
              </h3>
              
              <div className="space-y-4">
                <div className="p-4 rounded-lg" style={{ backgroundColor: 'var(--color-muted)' }}>
                  <div className="flex items-center justify-between">
                    <span className="text-sm font-medium" style={{ color: 'var(--color-foreground)' }}>Application Version</span>
                    <span className="text-sm" style={{ color: 'var(--color-muted-foreground)' }}>v1.0.0</span>
                  </div>
                </div>

                <div className="p-4 rounded-lg" style={{ backgroundColor: 'var(--color-muted)' }}>
                  <div className="flex items-center justify-between">
                    <span className="text-sm font-medium" style={{ color: 'var(--color-foreground)' }}>Java Version</span>
                    <span className="text-sm" style={{ color: 'var(--color-muted-foreground)' }}>17.0.x</span>
                  </div>
                </div>

                <div className="p-4 rounded-lg" style={{ backgroundColor: 'var(--color-muted)' }}>
                  <div className="flex items-center justify-between">
                    <span className="text-sm font-medium" style={{ color: 'var(--color-foreground)' }}>Spring Boot</span>
                    <span className="text-sm" style={{ color: 'var(--color-muted-foreground)' }}>3.x</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'notifications' && (
          <div className="rounded-lg shadow-sm border p-6" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)' }}>
            <h3 className="text-lg font-semibold mb-4 flex items-center" style={{ color: 'var(--color-foreground)' }}>
              <Bell className="h-5 w-5 mr-2 text-yellow-600" />
              Notification Settings
            </h3>
            
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <label className="text-sm font-medium text-gray-700">
                    Scan Completion Notifications
                  </label>
                  <p className="text-xs text-gray-500">
                    Receive notifications when scans complete
                  </p>
                </div>
                <label className="relative inline-flex items-center cursor-pointer">
                  <input
                    type="checkbox"
                    defaultChecked
                    className="sr-only peer"
                  />
                  <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                </label>
              </div>

              <div className="flex items-center justify-between">
                <div>
                  <label className="text-sm font-medium text-gray-700">
                    Error Notifications
                  </label>
                  <p className="text-xs text-gray-500">
                    Receive notifications when scans fail
                  </p>
                </div>
                <label className="relative inline-flex items-center cursor-pointer">
                  <input
                    type="checkbox"
                    defaultChecked
                    className="sr-only peer"
                  />
                  <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                </label>
              </div>

              <div className="flex items-center justify-between">
                <div>
                  <label className="text-sm font-medium text-gray-700">
                    Coverage Threshold Alerts
                  </label>
                  <p className="text-xs text-gray-500">
                    Alert when coverage drops below threshold
                  </p>
                </div>
                <label className="relative inline-flex items-center cursor-pointer">
                  <input
                    type="checkbox"
                    className="sr-only peer"
                  />
                  <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                </label>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'advanced' && (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Advanced Configuration */}
            <div className="rounded-lg shadow-sm border p-6" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)' }}>
              <h3 className="text-lg font-semibold mb-4 flex items-center" style={{ color: 'var(--color-foreground)' }}>
                <Shield className="h-5 w-5 mr-2 text-red-600" />
                Advanced Configuration
              </h3>
              
              <div className="space-y-4">
                <div className="p-4 border rounded-lg" style={{ backgroundColor: 'var(--color-warning)', borderColor: 'var(--color-warning)' }}>
                  <div className="flex items-center">
                    <AlertCircle className="h-5 w-5 mr-2" style={{ color: 'var(--color-warning-foreground)' }} />
                    <span className="text-sm font-medium" style={{ color: 'var(--color-warning-foreground)' }}>
                      Advanced Settings
                    </span>
                  </div>
                  <p className="text-xs mt-1" style={{ color: 'var(--color-warning-foreground)' }}>
                    These settings require system administrator privileges
                  </p>
                </div>

                <div className="flex items-center justify-between">
                  <div>
                    <label className="text-sm font-medium" style={{ color: 'var(--color-foreground)' }}>
                      Debug Mode
                    </label>
                    <p className="text-xs" style={{ color: 'var(--color-muted-foreground)' }}>
                      Enable detailed logging and debugging
                    </p>
                  </div>
                  <label className="relative inline-flex items-center cursor-pointer">
                    <input
                      type="checkbox"
                      className="sr-only peer"
                    />
                    <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                  </label>
                </div>

                <div className="flex items-center justify-between">
                  <div>
                    <label className="text-sm font-medium" style={{ color: 'var(--color-foreground)' }}>
                      Performance Monitoring
                    </label>
                    <p className="text-xs" style={{ color: 'var(--color-muted-foreground)' }}>
                      Enable performance metrics collection
                    </p>
                  </div>
                  <label className="relative inline-flex items-center cursor-pointer">
                    <input
                      type="checkbox"
                      defaultChecked
                      className="sr-only peer"
                    />
                    <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                  </label>
                </div>
              </div>
            </div>

            {/* Actions */}
            <div className="rounded-lg shadow-sm border p-6" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)' }}>
              <h3 className="text-lg font-semibold mb-4 flex items-center" style={{ color: 'var(--color-foreground)' }}>
                <FileText className="h-5 w-5 mr-2 text-gray-600" />
                Actions
              </h3>
              
              <div className="space-y-4">
                <button
                  onClick={resetToDefaults}
                  className="w-full px-4 py-2 rounded-lg transition-colors" style={{ backgroundColor: 'var(--color-secondary)', color: 'var(--color-secondary-foreground)' }}
                >
                  Reset to Defaults
                </button>

                <button
                  onClick={fetchConfig}
                  className="w-full px-4 py-2 rounded-lg transition-colors" style={{ backgroundColor: 'var(--color-accent)', color: 'var(--color-primary)' }}
                >
                  Reload Configuration
                </button>

                <div className="pt-4 border-t" style={{ borderColor: 'var(--color-border)' }}>
                  <button
                    className="w-full px-4 py-2 rounded-lg transition-colors" style={{ backgroundColor: 'var(--color-destructive)', color: 'var(--color-destructive-foreground)' }}
                  >
                    Clear All Data
                  </button>
                  <p className="text-xs mt-2 text-center" style={{ color: 'var(--color-muted-foreground)' }}>
                    This will remove all scan history and metrics
                  </p>
                </div>
              </div>
          </div>
        </div>
        )}
      </div>
    </div>
  );
};

export default SettingsView;