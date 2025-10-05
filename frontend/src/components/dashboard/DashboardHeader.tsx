import React from 'react';
import { RefreshCw, Settings } from 'lucide-react';
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
            <div className="text-right">
              <p className="text-xs text-gray-500">Data refreshed</p>
              <p className="text-xs text-gray-500">
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
