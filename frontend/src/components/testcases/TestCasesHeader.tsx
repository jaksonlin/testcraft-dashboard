import React, { useState, useRef, useEffect } from 'react';
import { FileText, Upload, MoreVertical, Trash2, Download } from 'lucide-react';
import DataControls, { type SortOption } from '../shared/DataControls';

interface TestCasesHeaderProps {
  pageSize: number;
  onPageSizeChange: (size: number) => void;
  sortBy?: string;
  onSortByChange?: (field: string) => void;
  sortOptions?: SortOption[];
  sortOrder?: 'asc' | 'desc';
  onSortOrderChange?: (order: 'asc' | 'desc') => void;
  onUploadClick?: () => void;
  onBulkDelete?: () => void;
  hasActiveFilters?: boolean;
}

const TestCasesHeader: React.FC<TestCasesHeaderProps> = ({
  pageSize,
  onPageSizeChange,
  sortBy,
  onSortByChange,
  sortOptions,
  sortOrder,
  onSortOrderChange,
  onUploadClick,
  onBulkDelete,
  hasActiveFilters = false,
}) => {
  const [showActionsMenu, setShowActionsMenu] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  // Close menu when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setShowActionsMenu(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  return (
    <div className="flex items-center justify-between mb-6">
      <div className="flex items-center">
        <FileText className="h-8 w-8 text-blue-600 dark:text-blue-400 mr-3" />
        <div>
          <h1 className="text-3xl font-bold text-gray-900 dark:text-white">Test Case Management</h1>
          <p className="text-gray-600 dark:text-gray-400 mt-1">Upload test case designs and track automation coverage</p>
        </div>
      </div>
      
      <div className="flex items-center gap-4">
        {/* Data Controls */}
        <DataControls
          pageSize={pageSize}
          onPageSizeChange={onPageSizeChange}
          sortBy={sortBy}
          onSortByChange={onSortByChange}
          sortOptions={sortOptions}
          sortOrder={sortOrder}
          onSortOrderChange={onSortOrderChange}
        />

        {/* Actions Menu (Advanced operations) */}
        {onBulkDelete && (
          <div className="relative" ref={menuRef}>
            <button
              onClick={() => setShowActionsMenu(!showActionsMenu)}
              className="px-4 py-2 border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 flex items-center gap-2"
              title="Advanced actions"
            >
              <MoreVertical className="h-4 w-4" />
              Actions
            </button>
            
            {/* Dropdown Menu */}
            {showActionsMenu && (
              <div className="absolute right-0 mt-2 w-64 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg shadow-lg z-10">
                <div className="py-1">
                  {/* Export All (future) */}
                  <button
                    className="w-full px-4 py-2 text-left text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 flex items-center gap-2"
                    disabled
                  >
                    <Download className="h-4 w-4" />
                    Export All (Coming Soon)
                  </button>
                  
                  <div className="border-t border-gray-200 dark:border-gray-700 my-1" />
                  
                  {/* Bulk Delete - Only enabled with filters */}
                  <button
                    onClick={() => {
                      setShowActionsMenu(false);
                      if (onBulkDelete) onBulkDelete();
                    }}
                    disabled={!hasActiveFilters}
                    className={`w-full px-4 py-2 text-left text-sm flex items-center gap-2 ${
                      hasActiveFilters
                        ? 'text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20'
                        : 'text-gray-400 dark:text-gray-600 cursor-not-allowed'
                    }`}
                    title={hasActiveFilters ? 'Delete all test cases matching active filters' : 'Apply filters first to enable bulk delete'}
                  >
                    <Trash2 className="h-4 w-4" />
                    Delete Filtered Test Cases
                  </button>
                  
                  {!hasActiveFilters && (
                    <p className="px-4 py-2 text-xs text-gray-500 dark:text-gray-400 italic">
                      Apply filters to enable bulk delete
                    </p>
                  )}
                </div>
              </div>
            )}
          </div>
        )}

        {/* Upload Button */}
        {onUploadClick && (
          <button
            onClick={onUploadClick}
            className="btn btn-primary flex items-center"
          >
            <Upload className="h-4 w-4 mr-2" />
            Upload Test Cases
          </button>
        )}
      </div>
    </div>
  );
};

export default TestCasesHeader;

