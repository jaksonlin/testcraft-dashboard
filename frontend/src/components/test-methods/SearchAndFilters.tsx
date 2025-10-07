import React from 'react';
import { Search, Filter } from 'lucide-react';

interface SearchAndFiltersProps {
  searchTerm: string;
  onSearchChange: (value: string) => void;
  isSearching: boolean;
  filterAnnotated: 'all' | 'annotated' | 'not-annotated';
  onFilterChange: (value: 'all' | 'annotated' | 'not-annotated') => void;
}

const SearchAndFilters: React.FC<SearchAndFiltersProps> = ({
  searchTerm,
  onSearchChange,
  isSearching,
  filterAnnotated,
  onFilterChange,
}) => {
  return (
    <div className="card mb-6">
      <div className="flex flex-col sm:flex-row gap-4">
        <div className="flex-1">
          <div className="relative">
            <Search className={`absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 ${isSearching ? 'text-blue-500 animate-pulse' : 'text-gray-400'}`} />
            <input
              type="text"
              placeholder="Search test methods, classes, or repositories..."
              value={searchTerm}
              onChange={(e) => onSearchChange(e.target.value)}
              className="input pl-10 w-full"
            />
            {isSearching && (
              <div className="absolute right-3 top-1/2 transform -translate-y-1/2">
                <div className="animate-spin rounded-full h-4 w-4 border-2 border-blue-500 border-t-transparent"></div>
              </div>
            )}
          </div>
        </div>
        <div className="flex items-center space-x-4">
          <div className="flex items-center">
            <Filter className="h-4 w-4 text-gray-400 mr-2" />
            <select
              value={filterAnnotated}
              onChange={(e) => onFilterChange(e.target.value as 'all' | 'annotated' | 'not-annotated')}
              className="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              <option value="all">All Methods</option>
              <option value="annotated">Annotated Only</option>
              <option value="not-annotated">Not Annotated</option>
            </select>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SearchAndFilters;

