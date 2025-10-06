import React, { createContext, useContext, useState, useEffect, useCallback, type ReactNode } from 'react';

export type Theme = 'light' | 'dark' | 'system';
export type Density = 'compact' | 'normal' | 'spacious';

export interface ColumnPreference {
  id: string;
  visible: boolean;
  order: number;
  width?: number;
}

export interface TablePreferences {
  [tableId: string]: {
    columns: ColumnPreference[];
    sortBy?: string;
    sortOrder?: 'asc' | 'desc';
    pageSize?: number;
  };
}

export interface UserPreferences {
  theme: Theme;
  density: Density;
  tables: TablePreferences;
  sidebarCollapsed: boolean;
  autoRefresh: boolean;
  refreshInterval: number; // in seconds
  notifications: {
    scanComplete: boolean;
    errors: boolean;
    updates: boolean;
  };
}

const defaultPreferences: UserPreferences = {
  theme: 'system',
  density: 'normal',
  tables: {},
  sidebarCollapsed: false,
  autoRefresh: false,
  refreshInterval: 300, // 5 minutes
  notifications: {
    scanComplete: true,
    errors: true,
    updates: false,
  },
};

interface PreferencesContextType {
  preferences: UserPreferences;
  updatePreference: <K extends keyof UserPreferences>(
    key: K,
    value: UserPreferences[K]
  ) => void;
  updateTablePreference: (
    tableId: string,
    preferences: Partial<TablePreferences[string]>
  ) => void;
  resetPreferences: () => void;
  getEffectiveTheme: () => 'light' | 'dark';
}

const PreferencesContext = createContext<PreferencesContextType | undefined>(undefined);

interface PreferencesProviderProps {
  children: ReactNode;
}

export const PreferencesProvider: React.FC<PreferencesProviderProps> = ({ children }) => {
  const [preferences, setPreferences] = useState<UserPreferences>(defaultPreferences);
  const [isLoaded, setIsLoaded] = useState(false);

  const getEffectiveTheme = useCallback((): 'light' | 'dark' => {
    if (preferences.theme === 'system') {
      return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    }
    return preferences.theme;
  }, [preferences.theme]);

  // Load preferences from localStorage on mount
  useEffect(() => {
    try {
      const saved = localStorage.getItem('testcraft-preferences');
      if (saved) {
        const parsedPreferences = JSON.parse(saved);
        setPreferences({ ...defaultPreferences, ...parsedPreferences });
      }
    } catch (error) {
      console.error('Failed to load preferences:', error);
    } finally {
      setIsLoaded(true);
    }
  }, []);

  // Save preferences to localStorage whenever they change
  useEffect(() => {
    if (isLoaded) {
      try {
        localStorage.setItem('testcraft-preferences', JSON.stringify(preferences));
      } catch (error) {
        console.error('Failed to save preferences:', error);
      }
    }
  }, [preferences, isLoaded]);

  // Apply theme to document using Tailwind's dark mode
  useEffect(() => {
    if (isLoaded) {
      const effectiveTheme = preferences.theme === 'system' 
        ? (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light')
        : preferences.theme;
      
      // Use Tailwind's dark mode class system
      if (effectiveTheme === 'dark') {
        document.documentElement.classList.add('dark');
      } else {
        document.documentElement.classList.remove('dark');
      }
    }
  }, [preferences.theme, isLoaded]);

  const updatePreference = <K extends keyof UserPreferences>(
    key: K,
    value: UserPreferences[K]
  ) => {
    setPreferences(prev => ({
      ...prev,
      [key]: value,
    }));
  };

  const updateTablePreference = (
    tableId: string,
    tablePreferences: Partial<TablePreferences[string]>
  ) => {
    setPreferences(prev => ({
      ...prev,
      tables: {
        ...prev.tables,
        [tableId]: {
          ...prev.tables[tableId],
          ...tablePreferences,
        },
      },
    }));
  };

  const resetPreferences = () => {
    setPreferences(defaultPreferences);
    localStorage.removeItem('testcraft-preferences');
  };

  const contextValue: PreferencesContextType = {
    preferences,
    updatePreference,
    updateTablePreference,
    resetPreferences,
    getEffectiveTheme,
  };

  // Don't render children until preferences are loaded to prevent flash
  if (!isLoaded) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <PreferencesContext.Provider value={contextValue}>
      {children}
    </PreferencesContext.Provider>
  );
};

export const usePreferences = (): PreferencesContextType => {
  const context = useContext(PreferencesContext);
  if (context === undefined) {
    throw new Error('usePreferences must be used within a PreferencesProvider');
  }
  return context;
};

// Hook for table-specific preferences
export const useTablePreferences = (tableId: string) => {
  const { preferences, updateTablePreference } = usePreferences();
  const tablePrefs = preferences.tables[tableId] || { columns: [] };

  const updateColumns = (columns: ColumnPreference[]) => {
    updateTablePreference(tableId, { columns });
  };

  const toggleColumn = (columnId: string) => {
    const columns = tablePrefs.columns.map(col =>
      col.id === columnId ? { ...col, visible: !col.visible } : col
    );
    updateColumns(columns);
  };

  const reorderColumns = (fromIndex: number, toIndex: number) => {
    const columns = [...tablePrefs.columns];
    const [movedColumn] = columns.splice(fromIndex, 1);
    columns.splice(toIndex, 0, movedColumn);
    
    // Update order numbers
    const reorderedColumns = columns.map((col, index) => ({
      ...col,
      order: index,
    }));
    
    updateColumns(reorderedColumns);
  };

  const updateSorting = (sortBy: string, sortOrder: 'asc' | 'desc') => {
    updateTablePreference(tableId, { sortBy, sortOrder });
  };

  const updatePageSize = (pageSize: number) => {
    updateTablePreference(tableId, { pageSize });
  };

  return {
    preferences: tablePrefs,
    updateColumns,
    toggleColumn,
    reorderColumns,
    updateSorting,
    updatePageSize,
  };
};
