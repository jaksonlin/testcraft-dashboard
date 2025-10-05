import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { FolderOpen, Play, Download, AlertCircle, CheckCircle } from 'lucide-react';
import RepositoryList from '../components/repositories/RepositoryList';
import { api, type RepositorySummary } from '../lib/api';

const RepositoriesView: React.FC = () => {
  const navigate = useNavigate();
  const [scanning, setScanning] = useState(false);
  const [scanResults, setScanResults] = useState<{ success: number; failed: number } | null>(null);

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

      {/* Repository List */}
      <RepositoryList 
        onRepositoryClick={handleRepositoryClick}
        onBulkScan={handleBulkScan}
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
