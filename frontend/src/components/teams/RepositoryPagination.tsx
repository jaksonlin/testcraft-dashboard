import React from 'react';

interface RepositoryPaginationProps {
  currentPage: number;
  totalPages: number;
  startIndex: number;
  endIndex: number;
  totalItems: number;
  onPageChange: (page: number) => void;
}

const RepositoryPagination: React.FC<RepositoryPaginationProps> = ({
  currentPage,
  totalPages,
  startIndex,
  endIndex,
  totalItems,
  onPageChange,
}) => {
  if (totalPages <= 1) return null;

  return (
    <div className="mt-4 flex items-center justify-between">
      <div className="text-sm" style={{ color: 'var(--color-muted-foreground)' }}>
        Showing{' '}
        <span className="font-medium">{startIndex + 1}</span>
        {' '}to{' '}
        <span className="font-medium">{Math.min(endIndex, totalItems)}</span>
        {' '}of{' '}
        <span className="font-medium">{totalItems}</span>
        {' '}repositories
      </div>
      <div className="flex items-center space-x-2">
        <button
          onClick={() => onPageChange(currentPage - 1)}
          disabled={currentPage === 1}
          className="px-3 py-1 text-sm border rounded disabled:opacity-50 disabled:cursor-not-allowed"
          style={{ borderColor: 'var(--color-border)', backgroundColor: 'var(--color-background)', color: 'var(--color-foreground)' }}
        >
          Previous
        </button>
        
        {/* Page numbers */}
        {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
          let pageNum;
          if (totalPages <= 5) {
            pageNum = i + 1;
          } else if (currentPage <= 3) {
            pageNum = i + 1;
          } else if (currentPage >= totalPages - 2) {
            pageNum = totalPages - 4 + i;
          } else {
            pageNum = currentPage - 2 + i;
          }
          
          return (
            <button
              key={`page-${pageNum}-${i}`}
              onClick={() => onPageChange(pageNum)}
              className={`px-3 py-1 text-sm border rounded`}
              style={{
                backgroundColor: pageNum === currentPage ? 'var(--color-accent)' : 'var(--color-background)',
                borderColor: pageNum === currentPage ? 'var(--color-primary)' : 'var(--color-border)',
                color: pageNum === currentPage ? 'var(--color-primary)' : 'var(--color-foreground)'
              }}
            >
              {pageNum}
            </button>
          );
        })}
        
        <button
          onClick={() => onPageChange(currentPage + 1)}
          disabled={currentPage === totalPages}
          className="px-3 py-1 text-sm border rounded disabled:opacity-50 disabled:cursor-not-allowed"
          style={{ borderColor: 'var(--color-border)', backgroundColor: 'var(--color-background)', color: 'var(--color-foreground)' }}
        >
          Next
        </button>
      </div>
    </div>
  );
};

export default RepositoryPagination;

