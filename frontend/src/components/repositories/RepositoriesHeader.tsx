import React from 'react';
import { FolderOpen, Columns } from 'lucide-react';
import DataControls, { type SortOption } from '../shared/DataControls';
import ExportManager, { type ExportOption } from '../shared/ExportManager';
import { type RepositorySummary } from '../../lib/api';

interface RepositoriesHeaderProps {
  pageSize: number;
  onPageSizeChange: (size: number) => void;
  sortBy: string;
  onSortByChange: (field: string) => void;
  sortOptions: SortOption[];
  sortOrder: 'asc' | 'desc';
  onSortOrderChange: (order: 'asc' | 'desc') => void;
  repositories: RepositorySummary[];
  selectedItems: Set<number>;
  onExport: (option: ExportOption) => Promise<void>;
  onColumnManagerOpen: () => void;
}

const RepositoriesHeader: React.FC<RepositoriesHeaderProps> = ({
  pageSize,
  onPageSizeChange,
  sortBy,
  onSortByChange,
  sortOptions,
  sortOrder,
  onSortOrderChange,
  repositories,
  selectedItems,
  onExport,
  onColumnManagerOpen,
}) => {
  return (
    <div className="flex items-center justify-between mb-6">
      <div className="flex items-center">
        <FolderOpen className="h-8 w-8 text-blue-600 mr-3" />
        <div>
          <h1 className="text-3xl font-bold text-gray-900 dark:text-white">Repositories</h1>
          <p className="text-gray-600 dark:text-gray-400 mt-1">Manage and analyze all repositories</p>
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

        <div className="flex gap-2">
          <button
            onClick={onColumnManagerOpen}
            className="btn btn-secondary flex items-center"
          >
            <Columns className="h-4 w-4 mr-2" />
            Columns
          </button>
          <ExportManager
            data={repositories}
            dataType="repositories"
            selectedItems={selectedItems}
            filteredData={repositories}
            onExport={onExport}
          />
        </div>
      </div>
    </div>
  );
};

export default RepositoriesHeader;

