import React from 'react';
import { Settings, X, Save, RefreshCw, AlertCircle, CheckCircle } from 'lucide-react';
import { type ScanConfig } from '../../lib/api';

interface ScanConfigModalProps {
  isOpen: boolean;
  onClose: () => void;
  scanConfig: ScanConfig | null;
  configLoading: boolean;
  configError: string | null;
  configSuccess: string | null;
  onSubmit: (e: React.FormEvent<HTMLFormElement>) => void;
  onRetry: () => void;
}

const ScanConfigModal: React.FC<ScanConfigModalProps> = ({
  isOpen,
  onClose,
  scanConfig,
  configLoading,
  configError,
  configSuccess,
  onSubmit,
  onRetry
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* Backdrop with blur effect */}
      <div 
        className="absolute inset-0 bg-black/20 backdrop-blur-sm"
        onClick={onClose}
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
              onClick={onClose}
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
            <form onSubmit={onSubmit} className="space-y-6">
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
                  onClick={onClose}
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
                  onClick={onRetry}
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
  );
};

export default ScanConfigModal;
