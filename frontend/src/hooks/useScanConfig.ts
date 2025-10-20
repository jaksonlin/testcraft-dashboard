import { useState } from 'react';
import { api, type ScanConfig } from '../lib/api';

interface UseScanConfigReturn {
  configLoading: boolean;
  configError: string | null;
  configSuccess: string | null;
  handleConfigUpdate: (updatedConfig: Partial<ScanConfig>) => Promise<void>;
  handleConfigSubmit: (e: React.FormEvent<HTMLFormElement>) => void;
  clearMessages: () => void;
}

export const useScanConfig = (_scanConfig: ScanConfig | null, onConfigUpdated: () => void): UseScanConfigReturn => {
  const [configLoading, setConfigLoading] = useState(false);
  const [configError, setConfigError] = useState<string | null>(null);
  const [configSuccess, setConfigSuccess] = useState<string | null>(null);

  const handleConfigUpdate = async (updatedConfig: Partial<ScanConfig>) => {
    try {
      setConfigLoading(true);
      setConfigError(null);
      setConfigSuccess(null);

      const response = await api.scan.updateConfig(updatedConfig);
      
      if (response.success) {
        setConfigSuccess('Configuration updated successfully!');
        onConfigUpdated(); // Refresh config data
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

  const clearMessages = () => {
    setConfigError(null);
    setConfigSuccess(null);
  };

  return {
    configLoading,
    configError,
    configSuccess,
    handleConfigUpdate,
    handleConfigSubmit,
    clearMessages
  };
};
