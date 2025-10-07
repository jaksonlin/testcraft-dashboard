import React, { useState, useEffect } from 'react';
import { api } from '../lib/api';
import SettingsHeader from '../components/settings/SettingsHeader';
import StatusMessages from '../components/settings/StatusMessages';
import TabNavigation, { type SettingsTab } from '../components/settings/TabNavigation';
import ScanConfigTab from '../components/settings/ScanConfigTab';
import SystemConfigTab from '../components/settings/SystemConfigTab';
import NotificationsTab from '../components/settings/NotificationsTab';
import AdvancedConfigTab from '../components/settings/AdvancedConfigTab';

const SettingsView: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<SettingsTab>('scan');

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
      <SettingsHeader
        saving={saving}
        onSave={handleSave}
        onRefresh={fetchConfig}
      />

      <StatusMessages error={error} success={success} />

      <TabNavigation
        activeTab={activeTab}
        onTabChange={setActiveTab}
      />
      
      {/* Tab Content */}
      <div className="space-y-6">
        {activeTab === 'scan' && (
          <ScanConfigTab
            formData={formData}
            onInputChange={handleInputChange}
          />
        )}

        {activeTab === 'system' && <SystemConfigTab />}

        {activeTab === 'notifications' && <NotificationsTab />}

        {activeTab === 'advanced' && (
          <AdvancedConfigTab
            onResetDefaults={resetToDefaults}
            onReloadConfig={fetchConfig}
          />
        )}
      </div>
    </div>
  );
};

export default SettingsView;
