import React from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';

interface ClassPaginationProps {
  currentPage: number;
  totalPages: number;
  pageSize: number;
  totalElements: number;
  searchTerm: string;
  annotatedFilter: 'all' | 'annotated' | 'not-annotated';
  onPageChange: (page: number) => void;
}

const ClassPagination: React.FC<ClassPaginationProps> = ({
  currentPage,
  totalPages,
  pageSize,
  totalElements,
  searchTerm,
  annotatedFilter,
  onPageChange,
}) => {
  if (totalPages <= 1) return null;

  return (
    <div className="flex items-center justify-between mt-4">
      <div className="text-sm text-gray-600 dark:text-gray-400">
        Showing {currentPage * pageSize + 1} to {Math.min((currentPage + 1) * pageSize, totalElements)} of {totalElements} classes
        {searchTerm && ` (filtered by "${searchTerm}")`}
        {annotatedFilter !== 'all' && ` (${annotatedFilter === 'annotated' ? 'annotated' : 'not annotated'} only)`}
      </div>
      <div className="flex items-center space-x-3">
        <button
          onClick={() => onPageChange(Math.max(0, currentPage - 1))}
          disabled={currentPage === 0}
          className={`
            inline-flex items-center px-4 py-2 text-sm font-medium rounded-lg transition-all duration-200
            ${currentPage === 0 
              ? 'bg-gray-100 dark:bg-gray-700 text-gray-400 dark:text-gray-500 cursor-not-allowed' 
              : 'bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-200 border border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-700 hover:border-gray-400 dark:hover:border-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent shadow-sm hover:shadow-md'
            }
          `}
        >
          <ChevronLeft className="h-4 w-4 mr-2" />
          Previous
        </button>
        
        <div className="flex items-center px-3 py-2 bg-gray-50 dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700">
          <span className="text-sm font-medium text-gray-700 dark:text-gray-200">
            Page {currentPage + 1} of {totalPages}
          </span>
        </div>
        
        <button
          onClick={() => onPageChange(Math.min(totalPages - 1, currentPage + 1))}
          disabled={currentPage >= totalPages - 1}
          className={`
            inline-flex items-center px-4 py-2 text-sm font-medium rounded-lg transition-all duration-200
            ${currentPage >= totalPages - 1 
              ? 'bg-gray-100 dark:bg-gray-700 text-gray-400 dark:text-gray-500 cursor-not-allowed' 
              : 'bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-200 border border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-700 hover:border-gray-400 dark:hover:border-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent shadow-sm hover:shadow-md'
            }
          `}
        >
          Next
          <ChevronRight className="h-4 w-4 ml-2" />
        </button>
      </div>
    </div>
  );
};

export default ClassPagination;

