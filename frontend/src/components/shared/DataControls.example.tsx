// Example usage of DataControls component in different views

import React from 'react';
import DataControls, { type SortOption } from './DataControls';

// Example 1: RepositoriesView (current implementation)
const RepositoriesViewExample = () => {
  const sortOptions: SortOption[] = [
    { value: 'name', label: 'Name' },
    { value: 'team', label: 'Team' },
    { value: 'coverage', label: 'Coverage' },
    { value: 'testmethods', label: 'Test Methods' },
    { value: 'lastscan', label: 'Last Scan' }
  ];

  return (
    <DataControls
      pageSize={20}
      onPageSizeChange={(size) => console.log('Page size:', size)}
      sortBy="name"
      onSortByChange={(field) => console.log('Sort by:', field)}
      sortOptions={sortOptions}
      sortOrder="asc"
      onSortOrderChange={(order) => console.log('Sort order:', order)}
    />
  );
};

// Example 2: TeamsView
const TeamsViewExample = () => {
  const sortOptions: SortOption[] = [
    { value: 'name', label: 'Name' },
    { value: 'repositories', label: 'Repositories' },
    { value: 'coverage', label: 'Coverage' }
  ];

  return (
    <DataControls
      sortBy="name"
      onSortByChange={(field) => console.log('Sort by:', field)}
      sortOptions={sortOptions}
      sortOrder="asc"
      onSortOrderChange={(order) => console.log('Sort order:', order)}
    />
  );
};

// Example 3: AnalyticsView (time range instead of sorting)
const AnalyticsViewExample = () => {
  return (
    <DataControls>
      {/* Custom time range control */}
      <div className="flex items-center">
        <span className="text-sm text-gray-600 dark:text-gray-400 mr-2">Time Range:</span>
        <select
          value="30"
          onChange={(e) => console.log('Time range:', e.target.value)}
          className="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        >
          <option value="7">Last 7 days</option>
          <option value="30">Last 30 days</option>
          <option value="90">Last 90 days</option>
          <option value="365">Last year</option>
        </select>
      </div>
    </DataControls>
  );
};

// Example 4: RepositoryDetailView (page size only)
const RepositoryDetailViewExample = () => {
  const pageSizeOptions = [
    { value: 5, label: '5 per page' },
    { value: 10, label: '10 per page' },
    { value: 20, label: '20 per page' },
    { value: 50, label: '50 per page' }
  ];

  return (
    <DataControls
      pageSize={10}
      onPageSizeChange={(size) => console.log('Page size:', size)}
      pageSizeOptions={pageSizeOptions}
    />
  );
};

// Example 5: TestMethodsView (using PaginatedTable - no changes needed)
// This view already uses PaginatedTable component which has its own controls

export {
  RepositoriesViewExample,
  TeamsViewExample,
  AnalyticsViewExample,
  RepositoryDetailViewExample
};
