import React, { useState } from 'react';
import { X, Calendar, Clock, CheckCircle, AlertCircle, Database, Download } from 'lucide-react';
import { type ScanSession, api } from '../../lib/api';

interface ScanDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  scan: ScanSession | null;
}

const ScanDetailModal: React.FC<ScanDetailModalProps> = ({ isOpen, onClose, scan }) => {
  const [downloadingServerReport, setDownloadingServerReport] = useState(false);
  
  if (!isOpen || !scan) return null;

  const formatDuration = (ms: number) => {
    const seconds = Math.round(ms / 1000);
    return seconds < 60 ? `${seconds}s` : `${Math.round(seconds / 60)}m ${seconds % 60}s`;
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return {
      date: date.toLocaleDateString(),
      time: date.toLocaleTimeString(),
      full: date.toLocaleString()
    };
  };

  const getCoverageRate = () => {
    if (scan.totalTestMethods === 0) return '0.0';
    return ((scan.totalAnnotatedMethods / scan.totalTestMethods) * 100).toFixed(1);
  };

  const { date, time, full } = formatDate(scan.scanDate);
  const coverageRate = getCoverageRate();
  const isSuccess = scan.scanStatus === 'COMPLETED';

  const handleDownloadServerReport = async () => {
    try {
      setDownloadingServerReport(true);
      const blob = await api.scan.downloadReport(scan.id);
      
      // Create download link
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `scan-${scan.id}-report.xlsx`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Error downloading server report:', error);
      alert('Failed to download server report. The report file may not be available.');
    } finally {
      setDownloadingServerReport(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* Backdrop */}
      <div 
        className="absolute inset-0 bg-black/20 backdrop-blur-sm"
        onClick={onClose}
      />
      
      {/* Modal Content */}
      <div className="relative w-full max-w-3xl max-h-[90vh] overflow-y-auto bg-white dark:bg-gray-900 rounded-xl shadow-2xl border border-gray-200 dark:border-gray-700 animate-in fade-in-0 zoom-in-95 duration-300">
        {/* Header */}
        <div className="sticky top-0 bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700 px-6 py-4 rounded-t-xl">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <div className="p-2 bg-blue-100 dark:bg-blue-900/30 rounded-lg">
                <Database className="h-5 w-5 text-blue-600 dark:text-blue-400" />
              </div>
              <div>
                <h3 className="text-xl font-semibold text-gray-900 dark:text-gray-100">Scan Details</h3>
                <p className="text-sm text-gray-500 dark:text-gray-400">Scan #{scan.id} - {full}</p>
              </div>
            </div>
            <button
              onClick={onClose}
              className="p-2 text-gray-400 dark:text-gray-500 hover:text-gray-600 dark:hover:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg transition-colors"
            >
              <X className="h-5 w-5" />
            </button>
          </div>
        </div>
        
        {/* Content */}
        <div className="p-6 space-y-6">
          {/* Status Header */}
          <div className="flex items-center space-x-3 p-4 bg-gray-50 dark:bg-gray-800 rounded-lg">
            {isSuccess ? (
              <CheckCircle className="h-6 w-6 text-green-600 dark:text-green-400" />
            ) : (
              <AlertCircle className="h-6 w-6 text-red-600 dark:text-red-400" />
            )}
            <div>
              <h4 className="text-lg font-semibold text-gray-900 dark:text-gray-100">
                {scan.scanStatus}
              </h4>
              <p className="text-sm text-gray-500 dark:text-gray-400">
                {isSuccess ? 'Scan completed successfully' : 'Scan encountered issues'}
              </p>
            </div>
          </div>

          {/* Scan Information Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-4">
              <h4 className="text-lg font-medium text-gray-900 dark:text-gray-100">Scan Information</h4>
              <div className="space-y-3">
                <div className="flex items-center space-x-3">
                  <Calendar className="h-4 w-4 text-gray-400 dark:text-gray-500" />
                  <div>
                    <p className="text-sm text-gray-500 dark:text-gray-400">Scan Date</p>
                    <p className="font-medium text-gray-900 dark:text-gray-100">{date} at {time}</p>
                  </div>
                </div>
                
                <div className="flex items-center space-x-3">
                  <Clock className="h-4 w-4 text-gray-400 dark:text-gray-500" />
                  <div>
                    <p className="text-sm text-gray-500 dark:text-gray-400">Duration</p>
                    <p className="font-medium text-gray-900 dark:text-gray-100">{formatDuration(scan.scanDurationMs)}</p>
                  </div>
                </div>
                
                <div className="flex items-center space-x-3">
                  <Database className="h-4 w-4 text-gray-400 dark:text-gray-500" />
                  <div>
                    <p className="text-sm text-gray-500 dark:text-gray-400">Scan Directory</p>
                    <p className="font-mono text-sm bg-gray-100 dark:bg-gray-800 text-gray-900 dark:text-gray-100 px-2 py-1 rounded">
                      {scan.scanDirectory}
                    </p>
                  </div>
                </div>
              </div>
            </div>
            
            <div className="space-y-4">
              <h4 className="text-lg font-medium text-gray-900 dark:text-gray-100">Scan Results</h4>
              <div className="grid grid-cols-2 gap-4">
                <div className="p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
                  <div className="flex items-center space-x-2 mb-2">
                    <Database className="h-4 w-4 text-blue-600 dark:text-blue-400" />
                    <span className="text-sm font-medium text-blue-900 dark:text-blue-200">Repositories</span>
                  </div>
                  <p className="text-2xl font-bold text-blue-600 dark:text-blue-400">{scan.totalRepositories}</p>
                </div>
                
                <div className="p-4 bg-green-50 dark:bg-green-900/20 rounded-lg">
                  <div className="flex items-center space-x-2 mb-2">
                    <CheckCircle className="h-4 w-4 text-green-600 dark:text-green-400" />
                    <span className="text-sm font-medium text-green-900 dark:text-green-200">Test Classes</span>
                  </div>
                  <p className="text-2xl font-bold text-green-600 dark:text-green-400">{scan.totalTestClasses}</p>
                </div>
                
                <div className="p-4 bg-purple-custom-light dark:bg-purple-900/20 rounded-lg">
                  <div className="flex items-center space-x-2 mb-2">
                    <CheckCircle className="h-4 w-4 text-purple-custom dark:text-purple-400" />
                    <span className="text-sm font-medium text-purple-custom-dark dark:text-purple-200">Test Methods</span>
                  </div>
                  <p className="text-2xl font-bold text-purple-custom dark:text-purple-400">{scan.totalTestMethods}</p>
                </div>
                
                <div className="p-4 bg-orange-custom-light dark:bg-orange-900/20 rounded-lg">
                  <div className="flex items-center space-x-2 mb-2">
                    <CheckCircle className="h-4 w-4 text-orange-custom dark:text-orange-400" />
                    <span className="text-sm font-medium text-orange-custom-dark dark:text-orange-200">Coverage</span>
                  </div>
                  <p className="text-2xl font-bold text-orange-custom dark:text-orange-400">{coverageRate}%</p>
                </div>
              </div>
            </div>
          </div>
          
          {/* Coverage Breakdown */}
          <div>
            <h4 className="text-lg font-medium text-gray-900 dark:text-gray-100 mb-3">Coverage Analysis</h4>
            <div className="bg-gray-50 dark:bg-gray-800 rounded-lg p-4">
              <div className="flex items-center justify-between mb-2">
                <span className="text-sm font-medium text-gray-700 dark:text-gray-300">Test Method Coverage</span>
                <span className="text-sm font-bold text-gray-900 dark:text-gray-100">{coverageRate}%</span>
              </div>
              <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-4 relative overflow-hidden">
                <div 
                  style={{ 
                    width: `${Math.max(Math.min(parseFloat(coverageRate), 100), 5)}%`,
                    minWidth: '20px',
                    height: '16px',
                    backgroundColor: '#3b82f6',
                    borderRadius: '9999px',
                    border: '2px solid #1d4ed8',
                    transition: 'all 0.5s ease-out',
                    boxShadow: '0 1px 3px 0 rgba(0, 0, 0, 0.1)'
                  }}
                />
              </div>
              <div className="flex justify-between text-xs text-gray-500 dark:text-gray-400 mt-2">
                <span>{scan.totalAnnotatedMethods} annotated methods</span>
                <span>{scan.totalTestMethods - scan.totalAnnotatedMethods} remaining</span>
              </div>
            </div>
          </div>

          {/* Error Log (if any) */}
          {scan.errorLog && (
            <div>
              <h4 className="text-lg font-medium text-gray-900 dark:text-gray-100 mb-3">Error Log</h4>
              <div className="p-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
                <p className="text-red-800 dark:text-red-200 font-medium mb-2">Scan Errors:</p>
                <p className="text-red-700 dark:text-red-300 text-sm whitespace-pre-wrap">{scan.errorLog}</p>
              </div>
            </div>
          )}
          
                     {/* Action Buttons */}
           <div className="flex justify-end space-x-3 pt-4 border-t border-gray-200 dark:border-gray-700">
             <button
               onClick={onClose}
               className="btn btn-secondary"
             >
               Close
             </button>
             <button
               onClick={handleDownloadServerReport}
               disabled={downloadingServerReport}
               className="btn btn-primary flex items-center space-x-2"
             >
               <Download className="h-4 w-4" />
               <span>{downloadingServerReport ? 'Downloading...' : 'Download Full Report'}</span>
             </button>
           </div>
        </div>
      </div>
    </div>
  );
};

export default ScanDetailModal;
