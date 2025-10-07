import React from 'react';
import { BarChart3, TrendingUp, Users, GitBranch } from 'lucide-react';

export type AnalyticsTab = 'overview' | 'trends' | 'teams' | 'growth';

interface AnalyticsTabNavigationProps {
  activeTab: AnalyticsTab;
  onTabChange: (tab: AnalyticsTab) => void;
}

const tabs = [
  { id: 'overview' as const, label: 'Overview', icon: BarChart3 },
  { id: 'trends' as const, label: 'Trends', icon: TrendingUp },
  { id: 'teams' as const, label: 'Team Comparison', icon: Users },
  { id: 'growth' as const, label: 'Growth Analysis', icon: GitBranch }
];

const AnalyticsTabNavigation: React.FC<AnalyticsTabNavigationProps> = ({ activeTab, onTabChange }) => {
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

export default AnalyticsTabNavigation;

