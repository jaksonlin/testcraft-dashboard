import React from 'react';

interface ResultsSummaryProps {
  showing: number;
  total: number;
  hasActiveFilters: boolean;
  onClearFilters: () => void;
}

const ResultsSummary: React.FC<ResultsSummaryProps> = ({
  showing,
  total,
  hasActiveFilters,
  onClearFilters,
}) => {
  return (
    <div className="flex items-center justify-between text-sm text-gray-600 dark:text-gray-400 mb-4">
      <span>
        Showing {showing} of {total} repositories
        {hasActiveFilters && ' (filtered)'}
      </span>
      {hasActiveFilters && (
        <button
          onClick={onClearFilters}
          className="text-blue-600 dark:text-blue-400 hover:text-blue-800 dark:hover:text-blue-300 font-medium"
        >
          Clear all filters
        </button>
      )}
    </div>
  );
};

export default ResultsSummary;

