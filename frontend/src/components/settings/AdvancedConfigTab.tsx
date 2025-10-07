import React from 'react';
import { Shield, FileText, AlertCircle } from 'lucide-react';

interface AdvancedConfigTabProps {
  onResetDefaults: () => void;
  onReloadConfig: () => void;
}

const AdvancedConfigTab: React.FC<AdvancedConfigTabProps> = ({ onResetDefaults, onReloadConfig }) => {
  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
      {/* Advanced Configuration */}
      <div className="rounded-lg shadow-sm border p-6" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)' }}>
        <h3 className="text-lg font-semibold mb-4 flex items-center" style={{ color: 'var(--color-foreground)' }}>
          <Shield className="h-5 w-5 mr-2 text-red-600" />
          Advanced Configuration
        </h3>
        
        <div className="space-y-4">
          <div className="p-4 border rounded-lg" style={{ backgroundColor: 'var(--color-warning)', borderColor: 'var(--color-warning)' }}>
            <div className="flex items-center">
              <AlertCircle className="h-5 w-5 mr-2" style={{ color: 'var(--color-warning-foreground)' }} />
              <span className="text-sm font-medium" style={{ color: 'var(--color-warning-foreground)' }}>
                Advanced Settings
              </span>
            </div>
            <p className="text-xs mt-1" style={{ color: 'var(--color-warning-foreground)' }}>
              These settings require system administrator privileges
            </p>
          </div>

          <div className="flex items-center justify-between">
            <div>
              <label className="text-sm font-medium" style={{ color: 'var(--color-foreground)' }}>
                Debug Mode
              </label>
              <p className="text-xs" style={{ color: 'var(--color-muted-foreground)' }}>
                Enable detailed logging and debugging
              </p>
            </div>
            <label className="relative inline-flex items-center cursor-pointer">
              <input
                type="checkbox"
                className="sr-only peer"
              />
              <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
            </label>
          </div>

          <div className="flex items-center justify-between">
            <div>
              <label className="text-sm font-medium" style={{ color: 'var(--color-foreground)' }}>
                Performance Monitoring
              </label>
              <p className="text-xs" style={{ color: 'var(--color-muted-foreground)' }}>
                Enable performance metrics collection
              </p>
            </div>
            <label className="relative inline-flex items-center cursor-pointer">
              <input
                type="checkbox"
                defaultChecked
                className="sr-only peer"
              />
              <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
            </label>
          </div>
        </div>
      </div>

      {/* Actions */}
      <div className="rounded-lg shadow-sm border p-6" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)' }}>
        <h3 className="text-lg font-semibold mb-4 flex items-center" style={{ color: 'var(--color-foreground)' }}>
          <FileText className="h-5 w-5 mr-2 text-gray-600" />
          Actions
        </h3>
        
        <div className="space-y-4">
          <button
            onClick={onResetDefaults}
            className="w-full px-4 py-2 rounded-lg transition-colors" 
            style={{ backgroundColor: 'var(--color-secondary)', color: 'var(--color-secondary-foreground)' }}
          >
            Reset to Defaults
          </button>

          <button
            onClick={onReloadConfig}
            className="w-full px-4 py-2 rounded-lg transition-colors" 
            style={{ backgroundColor: 'var(--color-accent)', color: 'var(--color-primary)' }}
          >
            Reload Configuration
          </button>

          <div className="pt-4 border-t" style={{ borderColor: 'var(--color-border)' }}>
            <button
              className="w-full px-4 py-2 rounded-lg transition-colors" 
              style={{ backgroundColor: 'var(--color-destructive)', color: 'var(--color-destructive-foreground)' }}
            >
              Clear All Data
            </button>
            <p className="text-xs mt-2 text-center" style={{ color: 'var(--color-muted-foreground)' }}>
              This will remove all scan history and metrics
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdvancedConfigTab;

