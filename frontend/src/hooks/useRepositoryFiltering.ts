import { useState, useMemo } from 'react';

export type RepositorySortBy = 'name' | 'coverage' | 'methods' | 'classes';
export type SortOrder = 'asc' | 'desc';

interface Repository {
  repositoryName: string;
  gitUrl: string;
  coverageRate: number;
  testMethodCount: number;
  testClassCount: number;
  [key: string]: any;
}

interface UseRepositoryFilteringOptions {
  repositories: Repository[];
  itemsPerPage?: number;
}

export const useRepositoryFiltering = ({ repositories, itemsPerPage = 10 }: UseRepositoryFilteringOptions) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [sortBy, setSortBy] = useState<RepositorySortBy>('name');
  const [sortOrder, setSortOrder] = useState<SortOrder>('desc');
  const [currentPage, setCurrentPage] = useState(1);

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
  const startIndex = (currentPage - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  const currentRepos = filteredRepos.slice(startIndex, endIndex);

  const handleSearchChange = (newSearchTerm: string) => {
    setSearchTerm(newSearchTerm);
    setCurrentPage(1);
  };

  const handleSortByChange = (newSortBy: RepositorySortBy) => {
    setSortBy(newSortBy);
    setCurrentPage(1);
  };

  const handleSortOrderToggle = () => {
    setSortOrder(prev => prev === 'asc' ? 'desc' : 'asc');
    setCurrentPage(1);
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

