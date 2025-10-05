import React from 'react';
import { FolderOpen } from 'lucide-react';

const RepositoriesView: React.FC = () => {
  return (
    <div className="p-8">
      <div className="flex items-center mb-6">
        <FolderOpen className="h-8 w-8 text-blue-600 mr-3" />
        <h1 className="text-3xl font-bold text-gray-900">Repositories</h1>
      </div>
      
      <div className="card">
        <div className="text-center py-12">
          <FolderOpen className="h-16 w-16 text-gray-400 mx-auto mb-4" />
          <h2 className="text-xl font-semibold text-gray-900 mb-2">Repository Management</h2>
          <p className="text-gray-600 mb-4">
            This view will contain the complete repository list with filtering, search, and detailed analysis capabilities.
          </p>
          <div className="text-sm text-gray-500">
            <p>• Full repository list (all repositories, not just top 5)</p>
            <p>• Repository filtering and search</p>
            <p>• Sortable columns (name, team, coverage, last scan, etc.)</p>
            <p>• Repository detail modals and class-level drill-down</p>
            <p>• Bulk operations (scan multiple repos)</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RepositoriesView;
