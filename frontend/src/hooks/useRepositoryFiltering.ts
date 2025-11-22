import { useState, useMemo } from 'react';
import { type RepositorySummary } from '../lib/api';

export type RepositorySortBy = 'name' | 'coverage' | 'methods' | 'classes';
export type SortOrder = 'asc' | 'desc';

interface UseRepositoryFilteringOptions {
  repositories: RepositorySummary[];
  itemsPerPage?: number;
}

export const useRepositoryFiltering = ({ repositories, itemsPerPage = 10 }: UseRepositoryFilteringOptions) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [sortBy, setSortBy] = useState<RepositorySortBy>('name');
  const [sortOrder, setSortOrder] = useState<SortOrder>('desc');
  const [currentPage, setCurrentPage] = useState(0);

  const filteredRepos = useMemo(() => {
    return repositories
      .filter(repo =>
        repo.repositoryName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        repo.gitUrl.toLowerCase().includes(searchTerm.toLowerCase())
      )
      .sort((a, b) => {
        let aValue: string | number;
        let bValue: string | number;

        switch (sortBy) {
          case 'name':
            aValue = a.repositoryName.toLowerCase();
            bValue = b.repositoryName.toLowerCase();
            break;
          case 'coverage':
            aValue = a.coverageRate;
            bValue = b.coverageRate;
            break;
          case 'methods':
            aValue = a.testMethodCount;
            bValue = b.testMethodCount;
            break;
          case 'classes':
            aValue = a.testClassCount;
            bValue = b.testClassCount;
            break;
          default:
            aValue = a.repositoryName.toLowerCase();
            bValue = b.repositoryName.toLowerCase();
        }

        if (sortOrder === 'asc') {
          return aValue < bValue ? -1 : aValue > bValue ? 1 : 0;
        } else {
          return aValue > bValue ? -1 : aValue < bValue ? 1 : 0;
        }
      });
  }, [repositories, searchTerm, sortBy, sortOrder]);

  const totalPages = Math.ceil(filteredRepos.length / itemsPerPage);
  const startIndex = currentPage * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  const currentRepos = filteredRepos.slice(startIndex, endIndex);

  const handleSearchChange = (newSearchTerm: string) => {
    setSearchTerm(newSearchTerm);
    setCurrentPage(0);
  };

  const handleSortByChange = (newSortBy: RepositorySortBy) => {
    setSortBy(newSortBy);
    setCurrentPage(0);
  };

  const handleSortOrderToggle = () => {
    setSortOrder(prev => prev === 'asc' ? 'desc' : 'asc');
    setCurrentPage(0);
  };

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  return {
    // State
    searchTerm,
    sortBy,
    sortOrder,
    currentPage,
    
    // Computed data
    filteredRepos,
    currentRepos,
    totalPages,
    startIndex,
    endIndex,
    
    // Actions
    setSearchTerm: handleSearchChange,
    setSortBy: handleSortByChange,
    setSortOrder: setSortOrder,
    toggleSortOrder: handleSortOrderToggle,
    setCurrentPage: handlePageChange,
  };
};

