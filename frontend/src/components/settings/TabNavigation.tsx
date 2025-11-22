import React from 'react';
import { GitBranch, Database, Bell, Shield, Users } from 'lucide-react';

export type SettingsTab = 'scan' | 'system' | 'notifications' | 'users' | 'advanced';

interface TabNavigationProps {
  activeTab: SettingsTab;
  onTabChange: (tab: SettingsTab) => void;
}

const tabs = [
  { id: 'scan' as const, label: 'Scan Configuration', icon: GitBranch },
  { id: 'system' as const, label: 'System Settings', icon: Database },
  { id: 'notifications' as const, label: 'Notifications', icon: Bell },
  { id: 'users' as const, label: 'User Management', icon: Users },
  { id: 'advanced' as const, label: 'Advanced', icon: Shield }
];

const TabNavigation: React.FC<TabNavigationProps> = ({ activeTab, onTabChange }) => {
  return (
    <div className="mb-6">
      <nav className="flex space-x-8">
        {tabs.map(({ id, label, icon: Icon }) => (
          <button
            key={id}
            onClick={() => onTabChange(id)}
            className={`flex items-center px-3 py-2 text-sm font-medium rounded-lg transition-colors ${
              activeTab === id ? 'opacity-100' : 'opacity-70'
            }`}
            style={{
              backgroundColor: activeTab === id ? 'var(--color-accent)' : 'transparent',
              color: activeTab === id ? 'var(--color-primary)' : 'var(--color-muted-foreground)'
            }}
            onMouseEnter={(e) => {
              if (activeTab !== id) {
                e.currentTarget.style.backgroundColor = 'var(--color-accent)';
                e.currentTarget.style.color = 'var(--color-foreground)';
              }
            }}
            onMouseLeave={(e) => {
              if (activeTab !== id) {
                e.currentTarget.style.backgroundColor = 'transparent';
                e.currentTarget.style.color = 'var(--color-muted-foreground)';
              }
            }}
          >
            <Icon className="h-4 w-4 mr-2" />
            {label}
          </button>
        ))}
      </nav>
    </div>
  );
};

export default TabNavigation;

