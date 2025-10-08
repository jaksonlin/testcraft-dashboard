import React from 'react';
import { RefreshCw, Target } from 'lucide-react';
import ServerSideExportManager from '../shared/ServerSideExportManager';

interface TestMethodsHeaderProps {
  loading: boolean;
  filters: {
    teamName: string;
    repositoryName: string;
    annotated: boolean | undefined;
  };
  onRefresh: () => void;
}

const TestMethodsHeader: React.FC<TestMethodsHeaderProps> = ({
  loading,
  filters,
  onRefresh
}) => {
  return (
    <header className="bg-white dark:bg-gray-800 shadow-sm border-b border-gray-200 dark:border-gray-700">
      <div className="w-full px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center py-6">
          <div className="flex items-center">
            <Target className="h-8 w-8 text-blue-600 mr-3" />
            <div>
              <h1 className="text-3xl font-bold text-gray-900 dark:text-gray-100">
                Test Methods
              </h1>
              <p className="text-gray-600 dark:text-gray-400">
                Comprehensive view of all test methods with advanced filtering and pagination
              </p>
            </div>
          </div>
          <div className="flex items-center space-x-4">
            <button
              onClick={onRefresh}
              disabled={loading}
              className="flex items-center px-4 py-2 bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300 rounded-md hover:bg-gray-200 dark:hover:bg-gray-700 transition-colors disabled:opacity-50"
            >
              <RefreshCw className={`h-4 w-4 mr-2 ${loading ? 'animate-spin' : ''}`} />
              Refresh
            </button>
            <ServerSideExportManager
              dataType="methods"
              filters={filters}
              className="ml-2"
            />
          </div>
        </div>
      </div>
    </header>
  );
};

export default TestMethodsHeader;


