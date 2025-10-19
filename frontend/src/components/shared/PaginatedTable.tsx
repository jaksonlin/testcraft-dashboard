import React, { useState, useMemo, useCallback } from 'react';
import { ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight, Search } from 'lucide-react';

interface PaginatedTableProps<T> {
  data: T[];
  totalItems: number;
  currentPage: number;
  pageSize: number;
  onPageChange: (page: number) => void;
  onPageSizeChange: (size: number) => void;
  columns: ColumnDef<T>[];
  loading?: boolean;
  searchable?: boolean;
  onSearch?: (term: string) => void;
  searchPlaceholder?: string;
  className?: string;
}

interface ColumnDef<T> {
  key: string;
  header: string;
  render: (item: T) => React.ReactNode;
  sortable?: boolean;
  width?: string;
}

const PaginatedTable = <T,>({
  data,
  totalItems,
  currentPage,
  pageSize,
  onPageChange,
  onPageSizeChange,
  columns,
  loading = false,
  searchable = false,
  onSearch,
  searchPlaceholder = "Search...",
  className = ""
}: PaginatedTableProps<T>) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [sortColumn, setSortColumn] = useState<string | null>(null);
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc');
  const [jumpToPage, setJumpToPage] = useState('');

  const totalPages = Math.ceil(totalItems / pageSize);
  const startItem = currentPage * pageSize + 1;
  const endItem = Math.min((currentPage + 1) * pageSize, totalItems);

  const handleSearch = useCallback((term: string) => {
    setSearchTerm(term);
    onSearch?.(term);
  }, [onSearch]);

  const handleSort = useCallback((columnKey: string) => {
    if (sortColumn === columnKey) {
      setSortDirection(prev => prev === 'asc' ? 'desc' : 'asc');
    } else {
      setSortColumn(columnKey);
      setSortDirection('asc');
    }
  }, [sortColumn]);

  const goToPage = useCallback((page: number) => {
    if (page >= 0 && page < totalPages) {
      onPageChange(page);
    }
  }, [onPageChange, totalPages]);
  
  const handleJumpToPage = useCallback(() => {
    const pageNum = parseInt(jumpToPage) - 1; // User enters 1-based, we use 0-based
    if (!isNaN(pageNum) && pageNum >= 0 && pageNum < totalPages) {
      goToPage(pageNum);
      setJumpToPage('');
    }
  }, [jumpToPage, totalPages, goToPage]);
  
  const handleJumpKeyPress = useCallback((e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      handleJumpToPage();
    }
  }, [handleJumpToPage]);

  const pageNumbers = useMemo(() => {
    const pages: (number | string)[] = [];
    const maxVisible = 5;
    
    if (totalPages <= maxVisible) {
      for (let i = 0; i < totalPages; i++) {
        pages.push(i);
      }
    } else {
      const start = Math.max(0, currentPage - 2);
      const end = Math.min(totalPages - 1, currentPage + 2);
      
      if (start > 0) {
        pages.push(0);
        if (start > 1) pages.push('...');
      }
      
      for (let i = start; i <= end; i++) {
        pages.push(i);
      }
      
      if (end < totalPages - 1) {
        if (end < totalPages - 2) pages.push('...');
        pages.push(totalPages - 1);
      }
    }
    
    return pages;
  }, [currentPage, totalPages]);

  if (loading) {
    return (
      <div className={`bg-white dark:bg-gray-900 rounded-lg shadow ${className}`}>
        <div className="p-6">
          <div className="animate-pulse">
            <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded w-1/4 mb-4"></div>
            <div className="space-y-3">
              {[...Array(5)].map((_, i) => (
                <div key={i} className="h-4 bg-gray-200 dark:bg-gray-700 rounded"></div>
              ))}
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className={`bg-white dark:bg-gray-900 rounded-lg shadow ${className}`}>
      {/* Search and Controls */}
      {(searchable || pageSize !== 50) && (
        <div className="p-4 border-b border-gray-200 dark:border-gray-700">
          <div className="flex items-center justify-between">
            {searchable && (
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
                <input
                  type="text"
                  placeholder={searchPlaceholder}
                  value={searchTerm}
                  onChange={(e) => handleSearch(e.target.value)}
                  className="pl-10 pr-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
            )}
            
            <div className="flex items-center space-x-2">
              <span className="text-sm text-gray-700 dark:text-gray-300">Show:</span>
              <select
                value={pageSize}
                onChange={(e) => onPageSizeChange(parseInt(e.target.value))}
                className="px-3 py-1 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value={25}>25</option>
                <option value={50}>50</option>
                <option value={100}>100</option>
                <option value={200}>200</option>
                <option value={500}>500</option>
              </select>
              <span className="text-sm text-gray-700 dark:text-gray-300">per page</span>
            </div>
          </div>
        </div>
      )}

      {/* Table */}
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
          <thead className="bg-gray-50 dark:bg-gray-800">
            <tr>
              {columns.map((column) => (
                <th
                  key={column.key}
                  className={`px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider ${
                    column.sortable ? 'cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-700' : ''
                  }`}
                  onClick={() => column.sortable && handleSort(column.key)}
                  style={{ width: column.width }}
                >
                  <div className="flex items-center space-x-1">
                    <span>{column.header}</span>
                    {column.sortable && sortColumn === column.key && (
                      <span className="text-blue-600 dark:text-blue-400">
                        {sortDirection === 'asc' ? '↑' : '↓'}
                      </span>
                    )}
                  </div>
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="bg-white dark:bg-gray-900 divide-y divide-gray-200 dark:divide-gray-700">
            {data.map((item, index) => (
              <tr key={index} className="hover:bg-gray-50 dark:hover:bg-gray-800">
                {columns.map((column) => (
                  <td key={column.key} className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-100">
                    {column.render(item)}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      <div className="px-4 py-3 border-t border-gray-200 dark:border-gray-700">
        <div className="flex items-center justify-between">
          <div className="text-sm text-gray-700 dark:text-gray-300">
            Showing {startItem} to {endItem} of {totalItems} results
          </div>
          
          <div className="flex items-center space-x-2">
            <button
              onClick={() => goToPage(0)}
              disabled={currentPage === 0}
              className="p-2 rounded-md border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-500 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <ChevronsLeft className="h-4 w-4" />
            </button>
            
            <button
              onClick={() => goToPage(currentPage - 1)}
              disabled={currentPage === 0}
              className="p-2 rounded-md border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-500 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <ChevronLeft className="h-4 w-4" />
            </button>
            
            <div className="flex space-x-1">
              {pageNumbers.map((page, index) => (
                <button
                  key={index}
                  onClick={() => typeof page === 'number' && goToPage(page)}
                  disabled={typeof page !== 'number'}
                  className={`px-3 py-2 rounded-md text-sm font-medium ${
                    typeof page === 'number' && page === currentPage
                      ? 'bg-blue-600 text-white'
                      : typeof page === 'number'
                      ? 'bg-white dark:bg-gray-700 text-gray-700 dark:text-gray-300 border border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-600'
                      : 'text-gray-400 cursor-default'
                  }`}
                >
                  {page}
                </button>
              ))}
            </div>
            
            <button
              onClick={() => goToPage(currentPage + 1)}
              disabled={currentPage >= totalPages - 1}
              className="p-2 rounded-md border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-500 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <ChevronRight className="h-4 w-4" />
            </button>
            
            <button
              onClick={() => goToPage(totalPages - 1)}
              disabled={currentPage >= totalPages - 1}
              className="p-2 rounded-md border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-500 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <ChevronsRight className="h-4 w-4" />
            </button>
            
            {/* Jump to Page */}
            {totalPages > 10 && (
              <div className="flex items-center space-x-2 ml-4">
                <span className="text-sm text-gray-700 dark:text-gray-300">Go to:</span>
                <input
                  type="number"
                  min="1"
                  max={totalPages}
                  value={jumpToPage}
                  onChange={(e) => setJumpToPage(e.target.value)}
                  onKeyPress={handleJumpKeyPress}
                  placeholder={`1-${totalPages}`}
                  className="w-20 px-2 py-1 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm"
                />
                <button
                  onClick={handleJumpToPage}
                  className="px-3 py-1 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
                >
                  Go
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default PaginatedTable;
export type { PaginatedTableProps, ColumnDef };
