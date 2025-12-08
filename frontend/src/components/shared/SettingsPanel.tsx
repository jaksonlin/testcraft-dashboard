import React, { useState } from 'react';
import { Settings, Palette, Columns, Bell, RotateCcw, X, Monitor, Sun, Moon, Globe } from 'lucide-react';
import { usePreferences, type Theme, type Density } from '../../contexts/PreferencesContext';
import { useTranslation } from '../../hooks/useTranslation';

interface SettingsPanelProps {
  isOpen: boolean;
  onClose: () => void;
}

const SettingsPanel: React.FC<SettingsPanelProps> = ({ isOpen, onClose }) => {
  const { preferences, updatePreference, resetPreferences, getEffectiveTheme } = usePreferences();
  const { currentLanguage, changeLanguage, t } = useTranslation();
  const [activeTab, setActiveTab] = useState<'appearance' | 'tables' | 'notifications' | 'advanced'>('appearance');

  if (!isOpen) return null;

  const themeOptions: { value: Theme; label: string; icon: React.ReactNode }[] = [
    { value: 'light', label: 'Light', icon: <Sun className="h-4 w-4" /> },
    { value: 'dark', label: 'Dark', icon: <Moon className="h-4 w-4" /> },
    { value: 'system', label: 'System', icon: <Monitor className="h-4 w-4" /> },
  ];

  const densityOptions: { value: Density; label: string; description: string }[] = [
    { value: 'compact', label: 'Compact', description: 'More items per screen' },
    { value: 'normal', label: 'Normal', description: 'Balanced spacing' },
    { value: 'spacious', label: 'Spacious', description: 'More breathing room' },
  ];

  const tabs = [
    { id: 'appearance', label: 'Appearance', icon: <Palette className="h-4 w-4" /> },
    { id: 'tables', label: 'Tables', icon: <Columns className="h-4 w-4" /> },
    { id: 'notifications', label: 'Notifications', icon: <Bell className="h-4 w-4" /> },
    { id: 'advanced', label: 'Advanced', icon: <Settings className="h-4 w-4" /> },
  ] as const;

  return (
    <>
      {/* Backdrop */}
      <div
        className="fixed inset-0 bg-black bg-opacity-30 z-40"
        onClick={onClose}
        style={{ backgroundColor: 'rgba(0, 0, 0, 0.3)' }}
      />
      
      {/* Panel */}
      <div className="fixed right-0 top-0 h-full w-96 shadow-xl z-50 flex flex-col bg-white dark:bg-gray-900 border-l border-gray-200 dark:border-gray-700">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-gray-200 dark:border-gray-700">
          <div className="flex items-center">
            <Settings className="h-6 w-6 mr-3 text-blue-600 dark:text-blue-400" />
            <h2 className="text-xl font-semibold text-gray-900 dark:text-gray-100">Settings</h2>
          </div>
          <button
            onClick={onClose}
            className="p-2 rounded-lg transition-colors bg-transparent text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Tabs */}
        <div className="flex border-b border-gray-200 dark:border-gray-700">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`flex-1 flex items-center justify-center px-4 py-3 text-sm font-medium transition-colors ${
                activeTab === tab.id
                  ? 'text-blue-600 dark:text-blue-400 border-b-2 border-blue-600 dark:border-blue-400 bg-blue-50 dark:bg-blue-900/20'
                  : 'text-gray-600 dark:text-gray-400 border-b-2 border-transparent hover:text-gray-900 dark:hover:text-gray-100'
              }`}
            >
              {tab.icon}
              <span className="ml-2">{tab.label}</span>
            </button>
          ))}
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto p-6">
          {activeTab === 'appearance' && (
            <div className="space-y-6">
              {/* Theme Selection */}
              <div>
                <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-4">Theme</h3>
                <div className="space-y-3">
                  {themeOptions.map((option) => (
                    <label
                      key={option.value}
                      className={`flex items-center p-3 rounded-lg border cursor-pointer transition-colors ${
                        preferences.theme === option.value
                          ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20'
                          : 'border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800'
                      }`}
                    >
                      <input
                        type="radio"
                        name="theme"
                        value={option.value}
                        checked={preferences.theme === option.value}
                        onChange={(e) => updatePreference('theme', e.target.value as Theme)}
                        className="sr-only"
                      />
                      <div className="flex items-center">
                        {option.icon}
                        <span className="ml-3 text-sm font-medium text-gray-900 dark:text-white">
                          {option.label}
                        </span>
                        {option.value === 'system' && (
                          <span className="ml-2 text-xs text-gray-500 dark:text-gray-400">
                            ({getEffectiveTheme()})
                          </span>
                        )}
                      </div>
                    </label>
                  ))}
                </div>
              </div>

              {/* Density Selection */}
              <div>
                <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-4">Density</h3>
                <div className="space-y-3">
                  {densityOptions.map((option) => (
                    <label
                      key={option.value}
                      className={`flex items-center p-3 rounded-lg border cursor-pointer transition-colors ${
                        preferences.density === option.value
                          ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20'
                          : 'border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800'
                      }`}
                    >
                      <input
                        type="radio"
                        name="density"
                        value={option.value}
                        checked={preferences.density === option.value}
                        onChange={(e) => updatePreference('density', e.target.value as Density)}
                        className="sr-only"
                      />
                      <div className="flex-1">
                        <div className="text-sm font-medium text-gray-900 dark:text-white">
                          {option.label}
                        </div>
                        <div className="text-xs text-gray-500 dark:text-gray-400">
                          {option.description}
                        </div>
                      </div>
                    </label>
                  ))}
                </div>
              </div>

              {/* Language Selection */}
              <div>
                <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-4">{t('language.switchLanguage')}</h3>
                <div className="space-y-3">
                  {(['en', 'zh-CN'] as const).map((lang) => (
                    <label
                      key={lang}
                      className={`flex items-center p-3 rounded-lg border cursor-pointer transition-colors ${
                        currentLanguage === lang
                          ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20'
                          : 'border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800'
                      }`}
                    >
                      <input
                        type="radio"
                        name="language"
                        value={lang}
                        checked={currentLanguage === lang}
                        onChange={() => changeLanguage(lang)}
                        className="sr-only"
                      />
                      <div className="flex items-center">
                        <Globe className="h-4 w-4 text-gray-600 dark:text-gray-400" />
                        <span className="ml-3 text-sm font-medium text-gray-900 dark:text-white">
                          {lang === 'en' ? t('language.english') : t('language.chinese')}
                        </span>
                      </div>
                    </label>
                  ))}
                </div>
              </div>

              {/* Sidebar */}
              <div>
                <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-4">Layout</h3>
                <label className="flex items-center justify-between p-3 rounded-lg border border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800 cursor-pointer">
                  <div>
                    <div className="text-sm font-medium text-gray-900 dark:text-white">
                      Collapsed Sidebar
                    </div>
                    <div className="text-xs text-gray-500 dark:text-gray-400">
                      Start with sidebar collapsed
                    </div>
                  </div>
                  <input
                    type="checkbox"
                    checked={preferences.sidebarCollapsed}
                    onChange={(e) => updatePreference('sidebarCollapsed', e.target.checked)}
                    className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                </label>
              </div>
            </div>
          )}

          {activeTab === 'tables' && (
            <div className="space-y-6">
              <div>
                <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-4">Table Settings</h3>
                <p className="text-sm text-gray-600 dark:text-gray-400 mb-4">
                  Customize table columns and behavior for each view.
                </p>
                
                {/* Auto Refresh */}
                <div className="mb-6">
                  <label className="flex items-center justify-between p-3 rounded-lg border border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800 cursor-pointer">
                    <div>
                      <div className="text-sm font-medium text-gray-900 dark:text-white">
                        Auto Refresh Tables
                      </div>
                      <div className="text-xs text-gray-500 dark:text-gray-400">
                        Automatically refresh table data
                      </div>
                    </div>
                    <input
                      type="checkbox"
                      checked={preferences.autoRefresh}
                      onChange={(e) => updatePreference('autoRefresh', e.target.checked)}
                      className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                    />
                  </label>
                </div>

                {/* Refresh Interval */}
                {preferences.autoRefresh && (
                  <div className="mb-6">
                    <label className="block text-sm font-medium text-gray-900 dark:text-white mb-2">
                      Refresh Interval (seconds)
                    </label>
                    <select
                      value={preferences.refreshInterval}
                      onChange={(e) => updatePreference('refreshInterval', parseInt(e.target.value))}
                      className="w-full p-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-800 text-gray-900 dark:text-white"
                    >
                      <option value={60}>1 minute</option>
                      <option value={300}>5 minutes</option>
                      <option value={600}>10 minutes</option>
                      <option value={1800}>30 minutes</option>
                    </select>
                  </div>
                )}

                <div className="text-sm text-gray-600 dark:text-gray-400">
                  <p>Table-specific settings are managed within each view:</p>
                  <ul className="list-disc list-inside mt-2 space-y-1">
                    <li>Column visibility and ordering</li>
                    <li>Sorting preferences</li>
                    <li>Page size settings</li>
                  </ul>
                </div>
              </div>
            </div>
          )}

          {activeTab === 'notifications' && (
            <div className="space-y-6">
              <div>
                <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-4">Notification Preferences</h3>
                
                <div className="space-y-4">
                  <label className="flex items-center justify-between p-3 rounded-lg border border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800 cursor-pointer">
                    <div>
                      <div className="text-sm font-medium text-gray-900 dark:text-white">
                        Scan Complete
                      </div>
                      <div className="text-xs text-gray-500 dark:text-gray-400">
                        Notify when repository scans finish
                      </div>
                    </div>
                    <input
                      type="checkbox"
                      checked={preferences.notifications.scanComplete}
                      onChange={(e) => updatePreference('notifications', {
                        ...preferences.notifications,
                        scanComplete: e.target.checked,
                      })}
                      className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                    />
                  </label>

                  <label className="flex items-center justify-between p-3 rounded-lg border border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800 cursor-pointer">
                    <div>
                      <div className="text-sm font-medium text-gray-900 dark:text-white">
                        Errors
                      </div>
                      <div className="text-xs text-gray-500 dark:text-gray-400">
                        Notify about scan errors and failures
                      </div>
                    </div>
                    <input
                      type="checkbox"
                      checked={preferences.notifications.errors}
                      onChange={(e) => updatePreference('notifications', {
                        ...preferences.notifications,
                        errors: e.target.checked,
                      })}
                      className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                    />
                  </label>

                  <label className="flex items-center justify-between p-3 rounded-lg border border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800 cursor-pointer">
                    <div>
                      <div className="text-sm font-medium text-gray-900 dark:text-white">
                        Updates
                      </div>
                      <div className="text-xs text-gray-500 dark:text-gray-400">
                        Notify about system updates and new features
                      </div>
                    </div>
                    <input
                      type="checkbox"
                      checked={preferences.notifications.updates}
                      onChange={(e) => updatePreference('notifications', {
                        ...preferences.notifications,
                        updates: e.target.checked,
                      })}
                      className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                    />
                  </label>
                </div>
              </div>
            </div>
          )}

          {activeTab === 'advanced' && (
            <div className="space-y-6">
              <div>
                <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-4">Advanced Settings</h3>
                
                <div className="space-y-4">
                  <div className="p-4 rounded-lg border border-gray-200 dark:border-gray-700">
                    <h4 className="text-sm font-medium text-gray-900 dark:text-white mb-2">
                      Reset All Preferences
                    </h4>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mb-3">
                      This will reset all your preferences to their default values.
                    </p>
                    <button
                      onClick={() => {
                        if (confirm('Are you sure you want to reset all preferences? This action cannot be undone.')) {
                          resetPreferences();
                        }
                      }}
                      className="flex items-center px-3 py-2 text-sm text-red-600 hover:text-red-700 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-lg transition-colors"
                    >
                      <RotateCcw className="h-4 w-4 mr-2" />
                      Reset Preferences
                    </button>
                  </div>

                  <div className="p-4 rounded-lg border border-gray-200 dark:border-gray-700">
                    <h4 className="text-sm font-medium text-gray-900 dark:text-white mb-2">
                      Export Preferences
                    </h4>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mb-3">
                      Download your preferences as a backup file.
                    </p>
                    <button
                      onClick={() => {
                        const dataStr = JSON.stringify(preferences, null, 2);
                        const dataBlob = new Blob([dataStr], { type: 'application/json' });
                        const url = URL.createObjectURL(dataBlob);
                        const link = document.createElement('a');
                        link.href = url;
                        link.download = 'testcraft-preferences.json';
                        link.click();
                        URL.revokeObjectURL(url);
                      }}
                      className="flex items-center px-3 py-2 text-sm text-blue-600 hover:text-blue-700 hover:bg-blue-50 dark:hover:bg-blue-900/20 rounded-lg transition-colors"
                    >
                      <Settings className="h-4 w-4 mr-2" />
                      Export Settings
                    </button>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </>
  );
};

export default SettingsPanel;
