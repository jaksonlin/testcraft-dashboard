import React, { useState } from 'react';
import { Outlet } from 'react-router-dom';
import { Menu, X, Settings } from 'lucide-react';
import SidebarNavigation from './SidebarNavigation';
import SettingsPanel from '../shared/SettingsPanel';
import { usePreferences } from '../../contexts/PreferencesContext';

const MainLayout: React.FC = () => {
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [settingsOpen, setSettingsOpen] = useState(false);
  const { preferences } = usePreferences();

  const toggleSidebar = () => {
    setSidebarCollapsed(!sidebarCollapsed);
  };

  const toggleMobileMenu = () => {
    setMobileMenuOpen(!mobileMenuOpen);
  };

  const toggleSettings = () => {
    setSettingsOpen(!settingsOpen);
  };

  return (
    <div className={`min-h-screen flex density-${preferences.density} bg-white dark:bg-gray-900 text-gray-900 dark:text-gray-100`}>
      {/* Mobile menu button */}
      <button
        onClick={toggleMobileMenu}
        className="lg:hidden fixed top-4 left-4 z-50 p-2 rounded-lg shadow-md bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 border border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700"
        aria-label="Toggle menu"
      >
        {mobileMenuOpen ? (
          <X className="h-6 w-6 text-gray-500 dark:text-gray-400" />
        ) : (
          <Menu className="h-6 w-6 text-gray-500 dark:text-gray-400" />
        )}
      </button>

      {/* Settings button */}
      <button
        onClick={toggleSettings}
        className="fixed top-4 right-4 z-50 p-2 rounded-lg shadow-md transition-colors bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 border border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700"
        aria-label="Open settings"
      >
        <Settings className="h-6 w-6 text-gray-500 dark:text-gray-400" />
      </button>

      {/* Sidebar */}
      <div className={`${mobileMenuOpen ? 'block' : 'hidden'} lg:block`}>
        <SidebarNavigation 
          isCollapsed={sidebarCollapsed}
          onToggleCollapse={toggleSidebar}
        />
      </div>
      
      {/* Main Content Area */}
      <div className="flex-1 flex flex-col min-w-0">
        {/* Content */}
        <main className="flex-1 overflow-auto">
          <Outlet />
        </main>
      </div>

      {/* Mobile overlay */}
      {mobileMenuOpen && (
        <div
          className="lg:hidden fixed inset-0 bg-black bg-opacity-50 z-40"
          onClick={toggleMobileMenu}
        />
      )}

      {/* Settings Panel */}
      <SettingsPanel isOpen={settingsOpen} onClose={() => setSettingsOpen(false)} />
    </div>
  );
};

export default MainLayout;
