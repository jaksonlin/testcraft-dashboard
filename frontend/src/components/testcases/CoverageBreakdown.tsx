import React from 'react';
import type { CoverageStats } from '../../lib/testCaseApi';

interface CoverageBreakdownProps {
  stats: CoverageStats;
}

/**
 * Coverage breakdown visualization component
 */
export const CoverageBreakdown: React.FC<CoverageBreakdownProps> = ({ stats }) => {
  const automatedPercentage = (stats.automated / stats.total) * 100;
  const manualPercentage = (stats.manual / stats.total) * 100;

  return (
    <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
      <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Coverage Breakdown</h3>
      <div className="space-y-4">
        <div>
          <div className="flex justify-between text-sm mb-1">
            <span className="text-gray-700 dark:text-gray-300">Automated</span>
            <span className="font-semibold text-green-600 dark:text-green-400">
              {stats.automated} ({automatedPercentage.toFixed(1)}%)
            </span>
          </div>
          <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
            <div
              className="bg-green-600 dark:bg-green-500 h-2 rounded-full transition-all"
              style={{ width: `${automatedPercentage}%` }}
            />
          </div>
        </div>

        <div>
          <div className="flex justify-between text-sm mb-1">
            <span className="text-gray-700 dark:text-gray-300">Manual</span>
            <span className="font-semibold text-orange-600 dark:text-orange-400">
              {stats.manual} ({manualPercentage.toFixed(1)}%)
            </span>
          </div>
          <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
            <div
              className="bg-orange-600 dark:bg-orange-500 h-2 rounded-full transition-all"
              style={{ width: `${manualPercentage}%` }}
            />
          </div>
        </div>
      </div>
    </div>
  );
};
