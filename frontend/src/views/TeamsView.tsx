import React from 'react';
import { Users } from 'lucide-react';

const TeamsView: React.FC = () => {
  return (
    <div className="p-8">
      <div className="flex items-center mb-6">
        <Users className="h-8 w-8 text-blue-600 mr-3" />
        <h1 className="text-3xl font-bold text-gray-900">Teams</h1>
      </div>
      
      <div className="card">
        <div className="text-center py-12">
          <Users className="h-16 w-16 text-gray-400 mx-auto mb-4" />
          <h2 className="text-xl font-semibold text-gray-900 mb-2">Team Management</h2>
          <p className="text-gray-600 mb-4">
            This view will provide team-focused analysis and management capabilities.
          </p>
          <div className="text-sm text-gray-500">
            <p>• Team performance metrics and comparison charts</p>
            <p>• Team repository assignments</p>
            <p>• Team coverage trends and analysis</p>
            <p>• Team-specific reports and insights</p>
            <p>• Individual team detail views</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default TeamsView;
