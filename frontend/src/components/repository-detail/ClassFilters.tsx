import React from 'react';
import { Search, Filter } from 'lucide-react';

interface ClassFiltersProps {
  searchTerm: string;
  onSearchChange: (value: string) => void;
  annotatedFilter: 'all' | 'annotated' | 'not-annotated';
  onAnnotatedFilterChange: (value: 'all' | 'annotated' | 'not-annotated') => void;
  pageSize: number;
  onPageSizeChange: (value: number) => void;
}

const ClassFilters: React.FC<ClassFiltersProps> = ({
  searchTerm,
  onSearchChange,
  annotatedFilter,
  onAnnotatedFilterChange,
  pageSize,
  onPageSizeChange,
}) => {
  return (
    <div className="mb-4 flex flex-col sm:flex-row gap-4">
      <div className="flex-1">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
          <input
            type="text"
            placeholder="Search classes..."
            value={searchTerm}
            onChange={(e) => onSearchChange(e.target.value)}
            className="input pl-10 w-full"
          />
        </div>
      </div>
      <div className="flex items-center space-x-4">
        <div className="flex items-center">
          <Filter className="h-4 w-4 text-gray-400 mr-2" />
          <select
            value={annotatedFilter}
            onChange={(e) => onAnnotatedFilterChange(e.target.value as 'all' | 'annotated' | 'not-annotated')}
            className="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            <option value="all">All Classes</option>
            <option value="annotated">Annotated Only</option>
            <option value="not-annotated">Not Annotated</option>
          </select>
        </div>
        <div className="flex items-center">
          <span className="text-sm text-gray-600 dark:text-gray-400 mr-2">Show:</span>
          <select
            value={pageSize}
            onChange={(e) => onPageSizeChange(parseInt(e.target.value))}
            className="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            <option value={5}>5 per page</option>
            <option value={10}>10 per page</option>
            <option value={20}>20 per page</option>
            <option value={50}>50 per page</option>
          </select>
        </div>
      </div>
    </div>
  );
};

export default ClassFilters;

