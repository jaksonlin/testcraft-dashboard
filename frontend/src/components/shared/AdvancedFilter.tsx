import React, { useState, useMemo } from 'react';
import { Search, Filter, X, ChevronDown, ChevronUp, Calendar, Users, GitBranch, BarChart3 } from 'lucide-react';

export interface FilterOption {
  id: string;
  label: string;
  type: 'text' | 'select' | 'multiselect' | 'range' | 'date' | 'boolean';
  options?: { value: string; label: string }[];
  min?: number;
  max?: number;
  step?: number;
}

export interface FilterState {
  [key: string]: string | string[] | number | boolean | null;
}

export interface AdvancedFilterProps {
  filters: FilterOption[];
  onFilterChange: (filters: FilterState) => void;
  onSearchChange: (searchTerm: string) => void;
  searchPlaceholder?: string;
  className?: string;
}

const AdvancedFilter: React.FC<AdvancedFilterProps> = ({
  filters,
  onFilterChange,
  onSearchChange,
  searchPlaceholder = "Search...",
  className = ""
}) => {
  const [isExpanded, setIsExpanded] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterState, setFilterState] = useState<FilterState>({});

  const handleSearchChange = (value: string) => {
    setSearchTerm(value);
    onSearchChange(value);
  };

  const handleFilterChange = (filterId: string, value: string | string[] | number | boolean | null) => {
    const newFilterState = { ...filterState, [filterId]: value };
    setFilterState(newFilterState);
    onFilterChange(newFilterState);
  };

  const clearAllFilters = () => {
    setSearchTerm('');
    setFilterState({});
    onSearchChange('');
    onFilterChange({});
  };

  const hasActiveFilters = searchTerm || Object.values(filterState).some(value => 
    value !== null && value !== '' && (Array.isArray(value) ? value.length > 0 : true)
  );

  const getFilterIcon = (filterId: string) => {
    switch (filterId) {
      case 'team':
      case 'teams':
        return <Users className="h-4 w-4" />;
      case 'coverage':
      case 'coverageRate':
        return <BarChart3 className="h-4 w-4" />;
      case 'date':
      case 'lastScan':
      case 'lastScanDate':
        return <Calendar className="h-4 w-4" />;
      case 'repository':
      case 'repositories':
        return <GitBranch className="h-4 w-4" />;
      default:
        return <Filter className="h-4 w-4" />;
    }
  };

  const renderFilterInput = (filter: FilterOption) => {
    const value = filterState[filter.id] || '';

    switch (filter.type) {
      case 'text':
        return (
          <input
            type="text"
            value={value as string}
            onChange={(e) => handleFilterChange(filter.id, e.target.value)}
            placeholder={`Filter by ${filter.label.toLowerCase()}...`}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm"
          />
        );

      case 'select':
        return (
          <select
            value={value as string}
            onChange={(e) => handleFilterChange(filter.id, e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm"
          >
            <option value="">All {filter.label}</option>
            {filter.options?.map(option => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        );

      case 'multiselect':
        const multiValue = (value as string[]) || [];
        return (
          <div className="space-y-2">
            {filter.options?.map(option => (
              <label key={option.value} className="flex items-center">
                <input
                  type="checkbox"
                  checked={multiValue.includes(option.value)}
                  onChange={(e) => {
                    const newValue = e.target.checked
                      ? [...multiValue, option.value]
                      : multiValue.filter(v => v !== option.value);
                    handleFilterChange(filter.id, newValue);
                  }}
                  className="mr-2 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                />
                <span className="text-sm text-gray-700">{option.label}</span>
              </label>
            ))}
          </div>
        );

      case 'range':
        return (
          <div className="space-y-2">
            <div className="flex items-center space-x-2">
              <input
                type="number"
                min={filter.min}
                max={filter.max}
                step={filter.step}
                value={value as number || filter.min || 0}
                onChange={(e) => handleFilterChange(filter.id, parseFloat(e.target.value))}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm"
                placeholder="Min"
              />
              <span className="text-gray-500">to</span>
              <input
                type="number"
                min={filter.min}
                max={filter.max}
                step={filter.step}
                value={value as number || filter.max || 100}
                onChange={(e) => handleFilterChange(filter.id, parseFloat(e.target.value))}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm"
                placeholder="Max"
              />
            </div>
            <div className="text-xs text-gray-500">
              Range: {filter.min || 0} - {filter.max || 100}
            </div>
          </div>
        );

      case 'boolean':
        return (
          <label className="flex items-center">
            <input
              type="checkbox"
              checked={value as boolean || false}
              onChange={(e) => handleFilterChange(filter.id, e.target.checked)}
              className="mr-2 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
            />
            <span className="text-sm text-gray-700">{filter.label}</span>
          </label>
        );

      default:
        return null;
    }
  };

  return (
    <div className={`bg-white rounded-lg shadow-sm border border-gray-200 ${className}`}>
      {/* Search Bar */}
      <div className="p-4 border-b border-gray-200">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
          <input
            type="text"
            placeholder={searchPlaceholder}
            value={searchTerm}
            onChange={(e) => handleSearchChange(e.target.value)}
            className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
          {searchTerm && (
            <button
              onClick={() => handleSearchChange('')}
              className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
            >
              <X className="h-4 w-4" />
            </button>
          )}
        </div>
      </div>

      {/* Filter Toggle */}
      <div className="p-4 border-b border-gray-200">
        <div className="flex items-center justify-between">
          <button
            onClick={() => setIsExpanded(!isExpanded)}
            className="flex items-center text-sm font-medium text-gray-700 hover:text-gray-900"
          >
            <Filter className="h-4 w-4 mr-2" />
            Advanced Filters
            {isExpanded ? (
              <ChevronUp className="h-4 w-4 ml-2" />
            ) : (
              <ChevronDown className="h-4 w-4 ml-2" />
            )}
          </button>

          {hasActiveFilters && (
            <div className="flex items-center space-x-2">
              <span className="text-xs text-gray-500">
                {Object.values(filterState).filter(v => v !== null && v !== '' && (Array.isArray(v) ? v.length > 0 : true)).length + (searchTerm ? 1 : 0)} active
              </span>
              <button
                onClick={clearAllFilters}
                className="text-xs text-blue-600 hover:text-blue-800 font-medium"
              >
                Clear All
              </button>
            </div>
          )}
        </div>
      </div>

      {/* Filter Panel */}
      {isExpanded && (
        <div className="p-4">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {filters.map(filter => (
              <div key={filter.id} className="space-y-2">
                <label className="flex items-center text-sm font-medium text-gray-700">
                  {getFilterIcon(filter.id)}
                  <span className="ml-2">{filter.label}</span>
                </label>
                {renderFilterInput(filter)}
              </div>
            ))}
          </div>

          {/* Active Filters Summary */}
          {hasActiveFilters && (
            <div className="mt-4 pt-4 border-t border-gray-200">
              <div className="flex flex-wrap gap-2">
                {searchTerm && (
                  <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                    Search: "{searchTerm}"
                    <button
                      onClick={() => handleSearchChange('')}
                      className="ml-1 text-blue-600 hover:text-blue-800"
                    >
                      <X className="h-3 w-3" />
                    </button>
                  </span>
                )}
                {Object.entries(filterState).map(([key, value]) => {
                  if (value === null || value === '' || (Array.isArray(value) && value.length === 0)) {
                    return null;
                  }
                  
                  const filter = filters.find(f => f.id === key);
                  if (!filter) return null;

                  let displayValue = '';
                  if (Array.isArray(value)) {
                    displayValue = value.join(', ');
                  } else if (typeof value === 'boolean') {
                    displayValue = value ? 'Yes' : 'No';
                  } else {
                    displayValue = String(value);
                  }

                  return (
                    <span key={key} className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
                      {filter.label}: {displayValue}
                      <button
                        onClick={() => handleFilterChange(key, null)}
                        className="ml-1 text-gray-600 hover:text-gray-800"
                      >
                        <X className="h-3 w-3" />
                      </button>
                    </span>
                  );
                })}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default AdvancedFilter;
