import React, { useState, useEffect } from 'react';
import { Download, FileText, Table, BarChart3, CheckCircle, XCircle, Clock, AlertCircle } from 'lucide-react';
import { api, type ExportRequest, type ExportStatus } from '../../lib/api';

export type ExportFormat = 'csv' | 'json' | 'excel';
export type ExportScope = 'all' | 'filtered';

export interface ExportOption {
  id: string;
  label: string;
  description: string;
  icon: React.ReactNode;
  format: ExportFormat;
  scope: ExportScope;
  filename: string;
}

export interface ServerSideExportManagerProps {
  dataType: 'repositories' | 'teams' | 'methods' | 'classes' | 'analytics';
  filters?: {
    teamName?: string;
    repositoryName?: string;
    annotated?: boolean;
  };
  className?: string;
}

const ServerSideExportManager: React.FC<ServerSideExportManagerProps> = ({
  dataType,
  filters = {},
  className = ""
}) => {
  const [isOpen, setIsOpen] = useState(false);
  const [exporting, setExporting] = useState<string | null>(null);
  const [exportJobs, setExportJobs] = useState<Map<string, ExportStatus>>(new Map());
  const [pollingInterval, setPollingInterval] = useState<number | null>(null);

  const getExportOptions = (): ExportOption[] => {
    const baseOptions: ExportOption[] = [
      {
        id: 'excel-all',
        label: 'Export All (Excel)',
        description: `Export all ${dataType} to Excel with multiple sheets`,
        icon: <Table className="h-4 w-4" />,
        format: 'excel',
        scope: 'all',
        filename: `${dataType}-comprehensive-${new Date().toISOString().split('T')[0]}.xlsx`
      },
      {
        id: 'csv-all',
        label: 'Export All (CSV)',
        description: `Export all ${dataType} to CSV format`,
        icon: <FileText className="h-4 w-4" />,
        format: 'csv',
        scope: 'all',
        filename: `${dataType}-all-${new Date().toISOString().split('T')[0]}.csv`
      },
      {
        id: 'json-all',
        label: 'Export All (JSON)',
        description: `Export all ${dataType} to JSON format`,
        icon: <BarChart3 className="h-4 w-4" />,
        format: 'json',
        scope: 'all',
        filename: `${dataType}-all-${new Date().toISOString().split('T')[0]}.json`
      }
    ];

    return baseOptions;
  };

  const handleExport = async (option: ExportOption) => {
    try {
      setExporting(option.id);
      setIsOpen(false);

      const request: ExportRequest = {
        dataType: dataType === 'methods' ? 'test-methods' : dataType as 'repositories' | 'teams' | 'test-methods',
        format: option.format,
        scope: option.scope,
        filters: filters,
        filename: option.filename
      };

      console.log('Initiating server-side export:', request);
      const status = await api.export.initiate(request);
      
      console.log('Export job initiated:', status);
      setExportJobs(prev => new Map(prev.set(status.jobId, status)));
      
      // Start polling for status updates
      startPolling(status.jobId);

    } catch (error) {
      console.error('Export failed:', error);
      setExporting(null);
      // TODO: Show error notification
    }
  };

  const startPolling = (jobId: string) => {
    const interval = setInterval(async () => {
      try {
        const status = await api.export.getStatus(jobId);
        setExportJobs(prev => new Map(prev.set(jobId, status)));

        if (status.status === 'completed' || status.status === 'failed' || status.status === 'cancelled') {
          clearInterval(interval);
          setExporting(null);
          if (pollingInterval === interval) {
            setPollingInterval(null);
          }
        }
      } catch (error) {
        console.error('Error polling export status:', error);
        clearInterval(interval);
        setExporting(null);
        if (pollingInterval === interval) {
          setPollingInterval(null);
        }
      }
    }, 2000); // Poll every 2 seconds

    setPollingInterval(interval);
  };

  const handleDownload = async (jobId: string) => {
    try {
      const blob = await api.export.download(jobId);
      const status = exportJobs.get(jobId);
      const filename = status?.filename || 'export.csv';
      
      // Create download link
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = filename;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      
      console.log('Download completed:', filename);
    } catch (error) {
      console.error('Download failed:', error);
    }
  };

  const handleCancel = async (jobId: string) => {
    try {
      await api.export.cancel(jobId);
      const status = exportJobs.get(jobId);
      if (status) {
        status.status = 'cancelled';
        setExportJobs(prev => new Map(prev.set(jobId, status)));
      }
    } catch (error) {
      console.error('Cancel failed:', error);
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'completed':
        return <CheckCircle className="h-4 w-4 text-green-500" />;
      case 'failed':
        return <XCircle className="h-4 w-4 text-red-500" />;
      case 'cancelled':
        return <XCircle className="h-4 w-4 text-gray-500" />;
      case 'processing':
        return <Clock className="h-4 w-4 text-blue-500 animate-spin" />;
      default:
        return <AlertCircle className="h-4 w-4 text-yellow-500" />;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'completed':
        return 'text-green-600 dark:text-green-400';
      case 'failed':
        return 'text-red-600 dark:text-red-400';
      case 'cancelled':
        return 'text-gray-600 dark:text-gray-400';
      case 'processing':
        return 'text-blue-600 dark:text-blue-400';
      default:
        return 'text-yellow-600 dark:text-yellow-400';
    }
  };

  // Cleanup polling on unmount
  useEffect(() => {
    return () => {
      if (pollingInterval) {
        clearInterval(pollingInterval);
      }
    };
  }, [pollingInterval]);

  const exportOptions = getExportOptions();
  const activeJobs = Array.from(exportJobs.values()).filter(job => 
    job.status === 'pending' || job.status === 'processing'
  );

  return (
    <div className={`relative ${className}`}>
      {/* Export Button */}
      <button
        onClick={() => setIsOpen(!isOpen)}
        disabled={exporting !== null}
        className="inline-flex items-center px-3 py-2 border border-gray-300 dark:border-gray-600 shadow-sm text-sm leading-4 font-medium rounded-md text-gray-700 dark:text-gray-200 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
      >
        <Download className="h-4 w-4 mr-2" />
        {exporting ? 'Exporting...' : 'Export'}
      </button>

      {/* Export Options Dropdown */}
      {isOpen && (
        <div className="absolute right-0 mt-2 w-80 bg-white dark:bg-gray-800 rounded-md shadow-lg ring-1 ring-black ring-opacity-5 z-50">
          <div className="py-1">
            <div className="px-4 py-2 border-b border-gray-200 dark:border-gray-700">
              <h3 className="text-sm font-medium text-gray-900 dark:text-gray-100">
                Export {dataType.charAt(0).toUpperCase() + dataType.slice(1)}
              </h3>
              <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                Server-side export for large datasets
              </p>
            </div>
            
            {exportOptions.map((option) => (
              <button
                key={option.id}
                onClick={() => handleExport(option)}
                disabled={exporting !== null}
                className="w-full text-left px-4 py-3 hover:bg-gray-50 dark:hover:bg-gray-700 focus:outline-none focus:bg-gray-50 dark:focus:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <div className="flex items-start">
                  <div className="flex-shrink-0 mr-3 mt-0.5">
                    {option.icon}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
                      {option.label}
                    </p>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                      {option.description}
                    </p>
                  </div>
                </div>
              </button>
            ))}
          </div>
        </div>
      )}

      {/* Active Export Jobs */}
      {activeJobs.length > 0 && (
        <div className="absolute right-0 mt-2 w-96 bg-white dark:bg-gray-800 rounded-md shadow-lg ring-1 ring-black ring-opacity-5 z-50">
          <div className="p-4">
            <h4 className="text-sm font-medium text-gray-900 dark:text-gray-100 mb-3">
              Active Exports
            </h4>
            {activeJobs.map((job) => (
              <div key={job.jobId} className="mb-3 last:mb-0">
                <div className="flex items-center justify-between mb-2">
                  <div className="flex items-center">
                    {getStatusIcon(job.status)}
                    <span className={`ml-2 text-sm font-medium ${getStatusColor(job.status)}`}>
                      {job.status.charAt(0).toUpperCase() + job.status.slice(1)}
                    </span>
                  </div>
                  <button
                    onClick={() => handleCancel(job.jobId)}
                    className="text-xs text-gray-500 hover:text-red-500 dark:text-gray-400 dark:hover:text-red-400"
                  >
                    Cancel
                  </button>
                </div>
                
                <div className="text-xs text-gray-500 dark:text-gray-400 mb-2">
                  {job.message}
                </div>
                
                <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
                  <div
                    className="bg-blue-600 h-2 rounded-full transition-all duration-300"
                    style={{ width: `${job.progress}%` }}
                  />
                </div>
                
                <div className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                  {job.processedRecords.toLocaleString()} / {job.totalRecords.toLocaleString()} records
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Completed Exports */}
      {Array.from(exportJobs.values()).filter(job => job.status === 'completed').length > 0 && (
        <div className="absolute right-0 mt-2 w-96 bg-white dark:bg-gray-800 rounded-md shadow-lg ring-1 ring-black ring-opacity-5 z-50">
          <div className="p-4">
            <h4 className="text-sm font-medium text-gray-900 dark:text-gray-100 mb-3">
              Completed Exports
            </h4>
            {Array.from(exportJobs.values())
              .filter(job => job.status === 'completed')
              .slice(0, 3) // Show only last 3 completed exports
              .map((job) => (
                <div key={job.jobId} className="flex items-center justify-between mb-2 last:mb-0">
                  <div className="flex items-center">
                    {getStatusIcon(job.status)}
                    <span className="ml-2 text-sm text-gray-900 dark:text-gray-100">
                      {job.filename}
                    </span>
                  </div>
                  <button
                    onClick={() => handleDownload(job.jobId)}
                    className="text-xs text-blue-600 hover:text-blue-800 dark:text-blue-400 dark:hover:text-blue-300"
                  >
                    Download
                  </button>
                </div>
              ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default ServerSideExportManager;
