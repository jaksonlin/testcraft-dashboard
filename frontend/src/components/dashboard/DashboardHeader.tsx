import React from 'react';
import { RefreshCw, Settings, BarChart3 } from 'lucide-react';
import { type ScanStatus } from '../../lib/api';

interface DashboardHeaderProps {
  scanStatus: ScanStatus | null;
  lastRefreshTime: Date;
  scanning: boolean;
  onOpenConfig: () => void;
  onManualScan: () => void;
}

const DashboardHeader: React.FC<DashboardHeaderProps> = ({
  scanStatus,
  lastRefreshTime,
  scanning,
  onOpenConfig,
  onManualScan
}) => {
  return (
    <header className="bg-white dark:bg-gray-800 shadow-sm border-b border-gray-200 dark:border-gray-700">
      <div className="w-full px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center py-6">
          <div className="flex items-center">
            <BarChart3 className="h-8 w-8 text-blue-600 mr-3" />
            <div>
              <h1 className="text-3xl font-bold text-gray-900 dark:text-gray-100">TestCraft Dashboard</h1>
              <p className="text-gray-600 dark:text-gray-400">Continuous Git Repository Monitoring</p>
            </div>
          </div>
          <div className="flex items-center space-x-4">
            {scanStatus && (
              <div className="text-right">
                <p className="text-sm text-gray-600 dark:text-gray-400">Last Scan</p>
                <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
                  {scanStatus.lastScanTime 
                    ? new Date(scanStatus.lastScanTime).toLocaleString()
                    : 'Never'
                  }
                </p>
              </div>
            )}
            <div className="text-right">
              <p className="text-xs text-gray-500 dark:text-gray-400">Data refreshed</p>
              <p className="text-xs text-gray-500 dark:text-gray-400">
                {lastRefreshTime.toLocaleTimeString()}
              </p>
            </div>
            <button
              onClick={onOpenConfig}
              className="btn btn-secondary flex items-center space-x-2"
            >
              <Settings className="h-4 w-4" />
              <span>Settings</span>
            </button>
            <button
              onClick={onManualScan}
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
  );
};

export default DashboardHeader;
