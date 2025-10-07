import React from 'react';
import { BarChart3, RefreshCw } from 'lucide-react';
import ExportManager, { type ExportOption } from '../shared/ExportManager';

interface GroupedViewHeaderProps {
  onRefresh: () => void;
  onExport: (option: ExportOption) => void;
}

const GroupedViewHeader: React.FC<GroupedViewHeaderProps> = ({ onRefresh, onExport }) => {
  return (
    <div className="bg-white dark:bg-gray-800 shadow-sm border-b border-gray-200 dark:border-gray-700">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div className="flex items-center justify-between">
          <div className="flex items-center">
            <BarChart3 className="h-8 w-8 text-blue-600 dark:text-blue-400 mr-3" />
            <div>
              <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">
                Test Methods Analysis
              </h1>
              <p className="text-sm text-gray-600 dark:text-gray-400">
                Hierarchical view of test methods grouped by team and class
              </p>
            </div>
          </div>
          <div className="flex items-center space-x-3">
            <button
              onClick={onRefresh}
              className="btn btn-secondary flex items-center"
            >
              <RefreshCw className="h-4 w-4 mr-2" />
              Refresh
            </button>
            <ExportManager
              data={[]}
              dataType="methods"
              onExport={onExport}
              className="flex items-center"
            />
          </div>
        </div>
      </div>
    </div>
  );
};

export default GroupedViewHeader;

