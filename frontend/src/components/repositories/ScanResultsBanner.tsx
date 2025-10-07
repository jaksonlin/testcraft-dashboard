import React from 'react';
import { CheckCircle, AlertCircle } from 'lucide-react';

interface ScanResultsBannerProps {
  scanResults: { success: number; failed: number } | null;
}

const ScanResultsBanner: React.FC<ScanResultsBannerProps> = ({ scanResults }) => {
  if (!scanResults) return null;

  const isSuccess = scanResults.failed === 0;

  return (
    <div className={`card mb-6 ${
      isSuccess ? 'bg-green-50 dark:bg-green-900/20 border-green-200 dark:border-green-800' : 'bg-red-50 dark:bg-red-900/20 border-red-200 dark:border-red-800'
    }`}>
      <div className="flex items-center">
        {isSuccess ? (
          <CheckCircle className="h-5 w-5 text-green-600 dark:text-green-400 mr-3" />
        ) : (
          <AlertCircle className="h-5 w-5 text-red-600 dark:text-red-400 mr-3" />
        )}
        <div>
          <h3 className={`font-semibold ${
            isSuccess ? 'text-green-800 dark:text-green-200' : 'text-red-800 dark:text-red-200'
          }`}>
            Bulk Scan {isSuccess ? 'Completed' : 'Failed'}
          </h3>
          <p className={`text-sm ${
            isSuccess ? 'text-green-700 dark:text-green-300' : 'text-red-700 dark:text-red-300'
          }`}>
            {scanResults.success} repositories scanned successfully
            {scanResults.failed > 0 && `, ${scanResults.failed} failed`}
          </p>
        </div>
      </div>
    </div>
  );
};

export default ScanResultsBanner;

