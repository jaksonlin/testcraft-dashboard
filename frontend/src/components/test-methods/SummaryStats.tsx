import React from 'react';
import { Users, FileText, Target, CheckCircle } from 'lucide-react';

interface SummaryStatsProps {
  totalTeams: number;
  totalClasses: number;
  totalMethods: number;
  coverageRate: number;
}

const SummaryStats: React.FC<SummaryStatsProps> = ({
  totalTeams,
  totalClasses,
  totalMethods,
  coverageRate,
}) => {
  return (
    <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
      <div className="card">
        <div className="flex items-center">
          <Users className="h-8 w-8 text-blue-600 dark:text-blue-400" />
          <div className="ml-4">
            <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Teams</p>
            <p className="text-2xl font-bold text-gray-900 dark:text-gray-100">
              {totalTeams}
            </p>
          </div>
        </div>
      </div>
      <div className="card">
        <div className="flex items-center">
          <FileText className="h-8 w-8 text-green-600 dark:text-green-400" />
          <div className="ml-4">
            <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Classes</p>
            <p className="text-2xl font-bold text-gray-900 dark:text-gray-100">
              {totalClasses}
            </p>
          </div>
        </div>
      </div>
      <div className="card">
        <div className="flex items-center">
          <Target className="h-8 w-8 text-purple-600 dark:text-purple-400" />
          <div className="ml-4">
            <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Test Methods</p>
            <p className="text-2xl font-bold text-gray-900 dark:text-gray-100">
              {totalMethods}
            </p>
          </div>
        </div>
      </div>
      <div className="card">
        <div className="flex items-center">
          <CheckCircle className="h-8 w-8 text-orange-600 dark:text-orange-400" />
          <div className="ml-4">
            <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Coverage Rate</p>
            <p className="text-2xl font-bold text-gray-900 dark:text-gray-100">
              {coverageRate.toFixed(1)}%
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SummaryStats;

