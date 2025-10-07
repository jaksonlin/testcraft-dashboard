import React from 'react';
import { Database, Shield } from 'lucide-react';

const SystemConfigTab: React.FC = () => {
  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
      {/* Database Configuration */}
      <div className="rounded-lg shadow-sm border p-6" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)' }}>
        <h3 className="text-lg font-semibold mb-4 flex items-center" style={{ color: 'var(--color-foreground)' }}>
          <Database className="h-5 w-5 mr-2 text-purple-600" />
          Database Configuration
        </h3>
        
        <div className="space-y-4">
          <div className="p-4 rounded-lg" style={{ backgroundColor: 'var(--color-muted)' }}>
            <div className="flex items-center justify-between">
              <span className="text-sm font-medium" style={{ color: 'var(--color-foreground)' }}>Database Status</span>
              <span className="px-2 py-1 text-xs font-semibold rounded-full bg-green-100 text-green-800">
                Connected
              </span>
            </div>
            <p className="text-xs mt-1" style={{ color: 'var(--color-muted-foreground)' }}>
              H2 Database running on localhost:8090
            </p>
          </div>

          <div className="p-4 rounded-lg" style={{ backgroundColor: 'var(--color-muted)' }}>
            <div className="flex items-center justify-between">
              <span className="text-sm font-medium" style={{ color: 'var(--color-foreground)' }}>Connection Pool</span>
              <span className="px-2 py-1 text-xs font-semibold rounded-full bg-blue-100 text-blue-800">
                Active
              </span>
            </div>
            <p className="text-xs mt-1" style={{ color: 'var(--color-muted-foreground)' }}>
              HikariCP connection pool configured
            </p>
          </div>
        </div>
      </div>

      {/* System Information */}
      <div className="rounded-lg shadow-sm border p-6" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)' }}>
        <h3 className="text-lg font-semibold mb-4 flex items-center" style={{ color: 'var(--color-foreground)' }}>
          <Shield className="h-5 w-5 mr-2 text-orange-600" />
          System Information
        </h3>
        
        <div className="space-y-4">
          <div className="p-4 rounded-lg" style={{ backgroundColor: 'var(--color-muted)' }}>
            <div className="flex items-center justify-between">
              <span className="text-sm font-medium" style={{ color: 'var(--color-foreground)' }}>Application Version</span>
              <span className="text-sm" style={{ color: 'var(--color-muted-foreground)' }}>v1.0.0</span>
            </div>
          </div>

          <div className="p-4 rounded-lg" style={{ backgroundColor: 'var(--color-muted)' }}>
            <div className="flex items-center justify-between">
              <span className="text-sm font-medium" style={{ color: 'var(--color-foreground)' }}>Java Version</span>
              <span className="text-sm" style={{ color: 'var(--color-muted-foreground)' }}>17.0.x</span>
            </div>
          </div>

          <div className="p-4 rounded-lg" style={{ backgroundColor: 'var(--color-muted)' }}>
            <div className="flex items-center justify-between">
              <span className="text-sm font-medium" style={{ color: 'var(--color-foreground)' }}>Spring Boot</span>
              <span className="text-sm" style={{ color: 'var(--color-muted-foreground)' }}>3.x</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SystemConfigTab;

