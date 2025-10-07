import React from 'react';
import { Search } from 'lucide-react';
import { type RepositorySortBy, type SortOrder } from '../../hooks/useRepositoryFiltering';

interface RepositoryFiltersProps {
  searchTerm: string;
  onSearchChange: (value: string) => void;
  sortBy: RepositorySortBy;
  onSortByChange: (value: RepositorySortBy) => void;
  sortOrder: SortOrder;
  onSortOrderToggle: () => void;
}

const RepositoryFilters: React.FC<RepositoryFiltersProps> = ({
  searchTerm,
  onSearchChange,
  sortBy,
  onSortByChange,
  sortOrder,
  onSortOrderToggle,
}) => {
  return (
    <div className="rounded-lg p-4 mb-4" style={{ backgroundColor: 'var(--color-muted)' }}>
      <div className="flex flex-col lg:flex-row gap-4">
        <div className="flex-1">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
            <input
              type="text"
              placeholder="Search repositories by name or URL..."
              value={searchTerm}
              onChange={(e) => onSearchChange(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm" 
              style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)', color: 'var(--color-foreground)' }}
            />
          </div>
        </div>
        <div className="flex gap-2">
          <select
            value={sortBy}
            onChange={(e) => onSortByChange(e.target.value as RepositorySortBy)}
            className="px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm" 
            style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)', color: 'var(--color-foreground)' }}
          >
            <option value="name">Sort by Name</option>
            <option value="coverage">Sort by Coverage</option>
            <option value="methods">Sort by Test Methods</option>
            <option value="classes">Sort by Test Classes</option>
          </select>
          <button
            onClick={onSortOrderToggle}
            className="px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm" 
            style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)', color: 'var(--color-foreground)' }}
          >
            {sortOrder === 'asc' ? '↑' : '↓'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default RepositoryFilters;

