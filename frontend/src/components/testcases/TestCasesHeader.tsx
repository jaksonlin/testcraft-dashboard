import React from 'react';
import { FileText, Upload } from 'lucide-react';
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
}) => {
  return (
    <div className="flex items-center justify-between mb-6">
      <div className="flex items-center">
        <FileText className="h-8 w-8 text-blue-600 mr-3" />
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Test Case Management</h1>
          <p className="text-gray-600 mt-1">Upload test case designs and track automation coverage</p>
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

