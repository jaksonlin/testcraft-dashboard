import React from 'react';
import { Settings, Save, RefreshCw } from 'lucide-react';

interface SettingsHeaderProps {
  saving: boolean;
  onSave: () => void;
  onRefresh: () => void;
}

const SettingsHeader: React.FC<SettingsHeaderProps> = ({ saving, onSave, onRefresh }) => {
  return (
    <div className="flex items-center justify-between mb-6">
      <div className="flex items-center">
        <Settings className="h-8 w-8 text-blue-600 dark:text-blue-400 mr-3" />
        <h1 className="text-3xl font-bold" style={{ color: 'var(--color-foreground)' }}>Settings</h1>
      </div>
      <div className="flex items-center gap-3">
        <button
          onClick={onRefresh}
          className="flex items-center px-3 py-2 rounded-lg transition-colors" 
          style={{ backgroundColor: 'var(--color-secondary)', color: 'var(--color-secondary-foreground)' }}
        >
          <RefreshCw className="h-4 w-4 mr-2" />
          Refresh
        </button>
        <button
          onClick={onSave}
          disabled={saving}
          className="flex items-center px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          {saving ? (
            <RefreshCw className="h-4 w-4 mr-2 animate-spin" />
          ) : (
            <Save className="h-4 w-4 mr-2" />
          )}
          {saving ? 'Saving...' : 'Save Changes'}
        </button>
      </div>
    </div>
  );
};

export default SettingsHeader;

