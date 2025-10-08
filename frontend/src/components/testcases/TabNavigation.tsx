import React from 'react';
import { List, BarChart3, AlertTriangle } from 'lucide-react';
import type { TabType } from '../../hooks/useTestCaseData';

interface TabNavigationProps {
  activeTab: TabType;
  onTabChange: (tab: TabType) => void;
  totalCount: number;
  gapsCount: number;
}

/**
 * Tab navigation component for test cases view
 */
export const TabNavigation: React.FC<TabNavigationProps> = ({
  activeTab,
  onTabChange,
  totalCount,
  gapsCount,
}) => {
  const tabs = [
    {
      id: 'list' as TabType,
      label: 'All Test Cases',
      count: totalCount,
      icon: List,
    },
    {
      id: 'coverage' as TabType,
      label: 'Coverage Analytics',
      icon: BarChart3,
    },
    {
      id: 'gaps' as TabType,
      label: 'Automation Gaps',
      count: gapsCount,
      icon: AlertTriangle,
    },
  ];

  return (
    <div className="border-b border-gray-200">
      <nav className="flex space-x-8">
        {tabs.map(tab => (
          <button
            key={tab.id}
            onClick={() => onTabChange(tab.id)}
            className={`py-4 px-1 border-b-2 font-medium text-sm transition-colors flex items-center gap-2 ${
              activeTab === tab.id
                ? 'border-blue-600 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            <tab.icon className="w-4 h-4" />
            {tab.label}
            {tab.count !== undefined && ` (${tab.count})`}
          </button>
        ))}
      </nav>
    </div>
  );
};
