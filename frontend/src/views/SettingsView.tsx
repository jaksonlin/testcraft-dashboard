import React from 'react';
import { Settings } from 'lucide-react';

const SettingsView: React.FC = () => {
  return (
    <div className="p-8">
      <div className="flex items-center mb-6">
        <Settings className="h-8 w-8 text-blue-600 mr-3" />
        <h1 className="text-3xl font-bold text-gray-900">Settings</h1>
      </div>
      
      <div className="card">
        <div className="text-center py-12">
          <Settings className="h-16 w-16 text-gray-400 mx-auto mb-4" />
          <h2 className="text-xl font-semibold text-gray-900 mb-2">Configuration</h2>
          <p className="text-gray-600 mb-4">
            This view will contain system configuration and management options.
          </p>
          <div className="text-sm text-gray-500">
            <p>• Scan configuration (current modal content)</p>
            <p>• System settings and preferences</p>
            <p>• User preferences and customization</p>
            <p>• Integration settings</p>
            <p>• Advanced configuration options</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SettingsView;
