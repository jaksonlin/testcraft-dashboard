import React from 'react';
import { GitBranch, Clock } from 'lucide-react';

interface ScanConfigData {
  repositoryHubPath: string;
  repositoryListFile: string;
  maxRepositoriesPerScan: number;
  tempCloneMode: boolean;
  schedulerEnabled: boolean;
  dailyScanCron: string;
  repositoryConfigContent?: string;
  organization?: string;
  scanBranch?: string;
}

interface ScanConfigTabProps {
  formData: ScanConfigData;
  onInputChange: (field: string, value: string | number | boolean) => void;
}

const ScanConfigTab: React.FC<ScanConfigTabProps> = ({ formData, onInputChange }) => {
  return (
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
              Organization (Instance-wide)
            </label>
            <input
              type="text"
              value={formData.organization || ''}
              onChange={(e) => onInputChange('organization', e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-100 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="e.g. acme"
            />
            <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
              Used when linking test methods to test cases (multi-tenant safety).
            </p>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Default Scan Branch
            </label>
            <input
              type="text"
              value={formData.scanBranch || ''}
              onChange={(e) => onInputChange('scanBranch', e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-100 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="main"
            />
            <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
              Git branch that will be cloned during scan operations.
            </p>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Repository Hub Path
            </label>
            <input
              type="text"
              value={formData.repositoryHubPath}
              onChange={(e) => onInputChange('repositoryHubPath', e.target.value)}
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
              onChange={(e) => onInputChange('repositoryListFile', e.target.value)}
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
              onChange={(e) => onInputChange('maxRepositoriesPerScan', parseInt(e.target.value))}
              className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-100 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
            <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
              Maximum number of repositories to process in a single scan
            </p>
          </div>
        </div>
      </div>

      {/* Repository Configuration Content */}
      <div className="rounded-lg shadow-sm border p-6" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)' }}>
        <h3 className="text-lg font-semibold mb-4 flex items-center" style={{ color: 'var(--color-foreground)' }}>
          <GitBranch className="h-5 w-5 mr-2 text-purple-600 dark:text-purple-400" />
          Repository Configuration Content
        </h3>
        
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Repository Configuration
            </label>
            <textarea
              value={formData.repositoryConfigContent || ''}
              onChange={(e) => onInputChange('repositoryConfigContent', e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-100 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              rows={8}
              placeholder="ssh://git@192.168.1.49:12022/zhishun/testcraft-dashboard.git,team_a,dev_0001&#10;ssh://git@192.168.1.49:12022/zhishun/db2h2.git,team_b,dev_0002&#10;ssh://git@192.168.1.49:12022/zhishun/testcraft-dashboard,team_b,dev_0002"
            />
            <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
              Enter repository configurations in the format: git_repo_url,team_name,team_identification_id (one per line)
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
                onChange={(e) => onInputChange('tempCloneMode', e.target.checked)}
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
                onChange={(e) => onInputChange('schedulerEnabled', e.target.checked)}
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
              onChange={(e) => onInputChange('dailyScanCron', e.target.value)}
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
  );
};

export default ScanConfigTab;

