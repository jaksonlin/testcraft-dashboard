import React from 'react';
import { NavLink } from 'react-router-dom';
import { 
  BarChart3, 
  FolderOpen, 
  Users, 
  TrendingUp, 
  Settings,
  ChevronLeft,
  ChevronRight
} from 'lucide-react';

interface SidebarNavigationProps {
  isCollapsed: boolean;
  onToggleCollapse: () => void;
}

const SidebarNavigation: React.FC<SidebarNavigationProps> = ({ 
  isCollapsed, 
  onToggleCollapse 
}) => {
  const navigationItems = [
    {
      name: 'Dashboard',
      href: '/',
      icon: BarChart3,
      description: 'Overview and key metrics'
    },
    {
      name: 'Repositories',
      href: '/repositories',
      icon: FolderOpen,
      description: 'Repository management and analysis'
    },
    {
      name: 'Teams',
      href: '/teams',
      icon: Users,
      description: 'Team-focused analysis'
    },
    {
      name: 'Analytics',
      href: '/analytics',
      icon: TrendingUp,
      description: 'Advanced reporting and trends'
    },
    {
      name: 'Settings',
      href: '/settings',
      icon: Settings,
      description: 'Configuration and preferences'
    }
  ];

  return (
    <div className={`bg-white border-r border-gray-200 sidebar-transition ${
      isCollapsed ? 'sidebar-collapsed' : 'sidebar-expanded'
    } fixed lg:relative h-full z-30`}>
      {/* Header */}
      <div className="flex items-center justify-between p-4 border-b border-gray-200">
        {!isCollapsed && (
          <h1 className="text-xl font-bold text-gray-900">TestCraft</h1>
        )}
        <button
          onClick={onToggleCollapse}
          className="p-2 rounded-lg hover:bg-gray-100 transition-colors"
          aria-label={isCollapsed ? 'Expand sidebar' : 'Collapse sidebar'}
        >
          {isCollapsed ? (
            <ChevronRight className="h-5 w-5 text-gray-600" />
          ) : (
            <ChevronLeft className="h-5 w-5 text-gray-600" />
          )}
        </button>
      </div>

      {/* Navigation */}
      <nav className="p-4 space-y-2">
        {navigationItems.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.name}
              to={item.href}
              className={({ isActive }) =>
                `flex items-center px-3 py-2 rounded-lg text-sm font-medium transition-colors group ${
                  isActive
                    ? 'bg-blue-50 text-blue-700 border-r-2 border-blue-700'
                    : 'text-gray-700 hover:bg-gray-50 hover:text-gray-900'
                }`
              }
              title={isCollapsed ? item.description : undefined}
            >
              <Icon className={`h-5 w-5 ${isCollapsed ? '' : 'mr-3'} flex-shrink-0`} />
              {!isCollapsed && (
                <span className="truncate">{item.name}</span>
              )}
            </NavLink>
          );
        })}
      </nav>

      {/* Footer */}
      {!isCollapsed && (
        <div className="absolute bottom-4 left-4 right-4">
          <div className="text-xs text-gray-500 text-center">
            TestCraft Dashboard v1.0
          </div>
        </div>
      )}
    </div>
  );
};

export default SidebarNavigation;
