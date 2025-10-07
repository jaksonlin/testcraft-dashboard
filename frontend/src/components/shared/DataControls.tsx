import React from 'react';

export interface SortOption {
  value: string;
  label: string;
}

export interface DataControlsProps {
  // Page size controls
  pageSize?: number;
  onPageSizeChange?: (size: number) => void;
  pageSizeOptions?: { value: number; label: string }[];
  
  // Sorting controls
  sortBy?: string;
  onSortByChange?: (field: string) => void;
  sortOptions?: SortOption[];
  
  sortOrder?: 'asc' | 'desc';
  onSortOrderChange?: (order: 'asc' | 'desc') => void;
  
  // Additional controls (time range, filters, etc.)
  children?: React.ReactNode;
  
  // Styling
  className?: string;
}

const DataControls: React.FC<DataControlsProps> = ({
  pageSize,
  onPageSizeChange,
  pageSizeOptions = [
    { value: 1, label: '1 per page' },
    { value: 10, label: '10 per page' },
    { value: 20, label: '20 per page' },
    { value: 50, label: '50 per page' },
    { value: 100, label: '100 per page' }
  ],
  sortBy,
  onSortByChange,
  sortOptions = [],
  sortOrder,
  onSortOrderChange,
  children,
  className = ''
}) => {
  return (
    <div className={`flex items-center space-x-3 ${className}`}>
      {/* Page Size Control */}
      {pageSize !== undefined && onPageSizeChange && (
        <div className="flex items-center">
          <span className="text-sm text-gray-600 dark:text-gray-400 mr-2">Show:</span>
          <select
            value={pageSize}
            onChange={(e) => onPageSizeChange(parseInt(e.target.value))}
            className="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            {pageSizeOptions.map(option => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>
      )}
      
      {/* Sort By Control */}
      {sortBy !== undefined && onSortByChange && sortOptions.length > 0 && (
        <div className="flex items-center">
          <span className="text-sm text-gray-600 dark:text-gray-400 mr-2">Sort by:</span>
          <select
            value={sortBy}
            onChange={(e) => onSortByChange(e.target.value)}
            className="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            {sortOptions.map(option => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>
      )}
      
      {/* Sort Order Control */}
      {sortOrder !== undefined && onSortOrderChange && (
        <div className="flex items-center">
          <span className="text-sm text-gray-600 dark:text-gray-400 mr-2">Order:</span>
          <select
            value={sortOrder}
            onChange={(e) => onSortOrderChange(e.target.value as 'asc' | 'desc')}
            className="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            <option value="asc">Ascending</option>
            <option value="desc">Descending</option>
          </select>
        </div>
      )}
      
      {/* Additional custom controls */}
      {children}
    </div>
  );
};

export default DataControls;
