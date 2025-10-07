import React from 'react';
import { type TestClassSummary, type PagedResponse } from '../../lib/api';
import ClassFilters from './ClassFilters';
import ClassesTable from './ClassesTable';
import ClassPagination from './ClassPagination';

interface ClassesTabProps {
  classes: TestClassSummary[];
  classesPagination: PagedResponse<TestClassSummary> | null;
  searchTerm: string;
  onSearchChange: (value: string) => void;
  annotatedFilter: 'all' | 'annotated' | 'not-annotated';
  onAnnotatedFilterChange: (value: 'all' | 'annotated' | 'not-annotated') => void;
  pageSize: number;
  onPageSizeChange: (value: number) => void;
  currentPage: number;
  onPageChange: (page: number) => void;
  onSelectClass: (cls: TestClassSummary) => void;
}

const ClassesTab: React.FC<ClassesTabProps> = ({
  classes,
  classesPagination,
  searchTerm,
  onSearchChange,
  annotatedFilter,
  onAnnotatedFilterChange,
  pageSize,
  onPageSizeChange,
  currentPage,
  onPageChange,
  onSelectClass,
}) => {
  return (
    <div className="card">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-semibold" style={{ color: 'var(--color-foreground)' }}>Test Classes</h3>
        <span className="text-sm" style={{ color: 'var(--color-muted-foreground)' }}>
          {classesPagination ? `${classesPagination.totalElements} classes` : `${classes.length} classes`}
        </span>
      </div>
      
      <ClassFilters
        searchTerm={searchTerm}
        onSearchChange={onSearchChange}
        annotatedFilter={annotatedFilter}
        onAnnotatedFilterChange={onAnnotatedFilterChange}
        pageSize={pageSize}
        onPageSizeChange={onPageSizeChange}
      />

      <ClassesTable 
        classes={classes} 
        onSelectClass={onSelectClass} 
      />
      
      {classesPagination && (
        <ClassPagination
          currentPage={currentPage}
          totalPages={classesPagination.totalPages}
          pageSize={pageSize}
          totalElements={classesPagination.totalElements}
          searchTerm={searchTerm}
          annotatedFilter={annotatedFilter}
          onPageChange={onPageChange}
        />
      )}
    </div>
  );
};

export default ClassesTab;

