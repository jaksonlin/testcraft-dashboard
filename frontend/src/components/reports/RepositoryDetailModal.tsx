import React from 'react';
import { X, GitBranch, Users, TestTube, CheckCircle, Calendar, ExternalLink } from 'lucide-react';
import { exportRepositoryToCSV } from '../../utils/exportUtils';
import { type RepositorySummary } from '../../lib/api';

interface RepositoryDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  repository: RepositorySummary | null;
}

const RepositoryDetailModal: React.FC<RepositoryDetailModalProps> = ({
  isOpen,
  onClose,
  repository
}) => {
  if (!isOpen || !repository) return null;

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return {
      date: date.toLocaleDateString(),
      time: date.toLocaleTimeString(),
      relative: getRelativeTime(date)
    };
  };

  const getRelativeTime = (date: Date) => {
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    const diffDays = Math.floor(diffHours / 24);

    if (diffDays > 0) return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`;
    if (diffHours > 0) return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`;
    return 'Just now';
  };

  const { date, time, relative } = formatDate(repository.lastScanDate);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* Backdrop */}
      <div 
        className="absolute inset-0 bg-black/20 backdrop-blur-sm"
        onClick={onClose}
      />
      
      {/* Modal Content */}
      <div className="relative w-full max-w-4xl max-h-[90vh] overflow-y-auto bg-white dark:bg-gray-900 rounded-xl shadow-2xl border border-gray-200 dark:border-gray-700 animate-in fade-in-0 zoom-in-95 duration-300">
        {/* Header */}
        <div className="sticky top-0 bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700 px-6 py-4 rounded-t-xl">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <div className="p-2 bg-blue-100 dark:bg-blue-900/30 rounded-lg">
                <GitBranch className="h-5 w-5 text-blue-600 dark:text-blue-400" />
              </div>
              <div>
                <h3 className="text-xl font-semibold text-gray-900 dark:text-gray-100">Repository Details</h3>
                <p className="text-sm text-gray-500 dark:text-gray-400">{repository.repositoryName}</p>
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
          {/* Repository Info */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-4">
              <div>
                <h4 className="text-lg font-medium text-gray-900 dark:text-gray-100 mb-3">Repository Information</h4>
                <div className="space-y-3">
                  <div className="flex items-center space-x-3">
                    <GitBranch className="h-4 w-4 text-gray-400 dark:text-gray-500" />
                    <div>
                      <p className="text-sm text-gray-500 dark:text-gray-400">Repository Name</p>
                      <p className="font-medium text-gray-900 dark:text-gray-100">{repository.repositoryName}</p>
                    </div>
                  </div>
                  
                  <div className="flex items-start space-x-3">
                    <ExternalLink className="h-4 w-4 text-gray-400 dark:text-gray-500 mt-1" />
                    <div>
                      <p className="text-sm text-gray-500 dark:text-gray-400">Git URL</p>
                      <p className="font-mono text-sm bg-gray-100 dark:bg-gray-800 text-gray-900 dark:text-gray-100 px-2 py-1 rounded">
                        {repository.gitUrl}
                      </p>
                    </div>
                  </div>
                  
                  <div className="flex items-center space-x-3">
                    <Users className="h-4 w-4 text-gray-400 dark:text-gray-500" />
                    <div>
                      <p className="text-sm text-gray-500 dark:text-gray-400">Team</p>
                      <p className="font-medium text-gray-900 dark:text-gray-100">{repository.teamName}</p>
                    </div>
                  </div>
                  
                  <div className="flex items-center space-x-3">
                    <Calendar className="h-4 w-4 text-gray-400 dark:text-gray-500" />
                    <div>
                      <p className="text-sm text-gray-500 dark:text-gray-400">Last Scan</p>
                      <p className="font-medium text-gray-900 dark:text-gray-100">{date} at {time}</p>
                      <p className="text-xs text-gray-400 dark:text-gray-500">{relative}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <div className="space-y-4">
              <div>
                <h4 className="text-lg font-medium text-gray-900 dark:text-gray-100 mb-3">Test Metrics</h4>
                <div className="grid grid-cols-2 gap-4">
                  <div className="p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
                    <div className="flex items-center space-x-2 mb-2">
                      <TestTube className="h-4 w-4 text-blue-600 dark:text-blue-400" />
                      <span className="text-sm font-medium text-blue-900 dark:text-blue-200">Test Classes</span>
                    </div>
                    <p className="text-2xl font-bold text-blue-600 dark:text-blue-400">{repository.testClassCount}</p>
                  </div>
                  
                  <div className="p-4 bg-green-50 dark:bg-green-900/20 rounded-lg">
                    <div className="flex items-center space-x-2 mb-2">
                      <CheckCircle className="h-4 w-4 text-green-600 dark:text-green-400" />
                      <span className="text-sm font-medium text-green-900 dark:text-green-200">Test Methods</span>
                    </div>
                    <p className="text-2xl font-bold text-green-600 dark:text-green-400">{repository.testMethodCount}</p>
                  </div>
                  
                  <div className="p-4 bg-purple-custom-light dark:bg-purple-900/20 rounded-lg">
                    <div className="flex items-center space-x-2 mb-2">
                      <CheckCircle className="h-4 w-4 text-purple-custom dark:text-purple-400" />
                      <span className="text-sm font-medium text-purple-custom-dark dark:text-purple-200">Annotated</span>
                    </div>
                    <p className="text-2xl font-bold text-purple-custom dark:text-purple-400">{repository.annotatedMethodCount || 0}</p>
                  </div>
                  
                  <div className="p-4 bg-orange-custom-light dark:bg-orange-900/20 rounded-lg">
                    <div className="flex items-center space-x-2 mb-2">
                      <CheckCircle className="h-4 w-4 text-orange-custom dark:text-orange-400" />
                      <span className="text-sm font-medium text-orange-custom-dark dark:text-orange-200">Coverage</span>
                    </div>
                    <p className="text-2xl font-bold text-orange-custom dark:text-orange-400">{(repository.coverageRate || 0).toFixed(1)}%</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          {/* Coverage Breakdown */}
          <div>
            <h4 className="text-lg font-medium text-gray-900 dark:text-gray-100 mb-3">Coverage Analysis</h4>
            <div className="bg-gray-50 dark:bg-gray-800 rounded-lg p-4">
              <div className="flex items-center justify-between mb-2">
                <span className="text-sm font-medium text-gray-700 dark:text-gray-300">Overall Coverage</span>
                <span className="text-sm font-bold text-gray-900 dark:text-gray-100">{(repository.coverageRate || 0).toFixed(1)}%</span>
              </div>
              <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-4 relative overflow-hidden">
                <div 
                  style={{ 
                    width: `${Math.max(Math.min(repository.coverageRate || 0, 100), 5)}%`,
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
                <span>{repository.annotatedMethodCount || 0} annotated methods</span>
                <span>{(repository.testMethodCount || 0) - (repository.annotatedMethodCount || 0)} remaining</span>
              </div>
            </div>
          </div>
          
          {/* Action Buttons */}
          <div className="flex justify-end space-x-3 pt-4 border-t border-gray-200 dark:border-gray-700">
            <button
              onClick={onClose}
              className="btn btn-secondary"
            >
              Close
            </button>
            <button
              onClick={() => {
                try {
                  exportRepositoryToCSV(repository);
                } catch (error) {
                  console.error('Error exporting repository data:', error);
                }
              }}
              className="btn btn-primary"
            >
              Export Report
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RepositoryDetailModal;
