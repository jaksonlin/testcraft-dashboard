import React from 'react';
import { GitBranch } from 'lucide-react';
import { type TeamMetrics } from '../../lib/api';
import { useRepositoryFiltering } from '../../hooks/useRepositoryFiltering';
import RepositoryFilters from './RepositoryFilters';
import RepositoryTable from './RepositoryTable';
import RepositoryPagination from './RepositoryPagination';

interface TeamRepositoriesSectionProps {
  team: TeamMetrics;
}

const TeamRepositoriesSection: React.FC<TeamRepositoriesSectionProps> = ({ team }) => {
  const {
    searchTerm,
    sortBy,
    sortOrder,
    currentPage,
    filteredRepos,
    currentRepos,
    totalPages,
    startIndex,
    endIndex,
    setSearchTerm,
    setSortBy,
    toggleSortOrder,
    setCurrentPage,
  } = useRepositoryFiltering({
    repositories: team.repositories,
    itemsPerPage: 10,
  });

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h4 className="text-lg font-medium" style={{ color: 'var(--color-foreground)' }}>Repositories</h4>
        <div className="text-sm" style={{ color: 'var(--color-muted-foreground)' }}>
          {filteredRepos.length} of {team.repositories.length} repositories
        </div>
      </div>

      <RepositoryFilters
        searchTerm={searchTerm}
        onSearchChange={setSearchTerm}
        sortBy={sortBy}
        onSortByChange={setSortBy}
        sortOrder={sortOrder}
        onSortOrderToggle={toggleSortOrder}
      />

      {currentRepos.length > 0 ? (
        <>
          <RepositoryTable repositories={currentRepos as any} />
          <RepositoryPagination
            currentPage={currentPage}
            totalPages={totalPages}
            startIndex={startIndex}
            endIndex={endIndex}
            totalItems={filteredRepos.length}
            onPageChange={setCurrentPage}
          />
        </>
      ) : (
        <div className="text-center py-8">
          <GitBranch className="h-12 w-12 text-gray-400 mx-auto mb-3" />
          <h3 className="text-lg font-semibold mb-2" style={{ color: 'var(--color-foreground)' }}>No repositories found</h3>
          <p style={{ color: 'var(--color-muted-foreground)' }}>
            {searchTerm ? 'Try adjusting your search criteria.' : 'No repositories are available for this team.'}
          </p>
        </div>
      )}
    </div>
  );
};

export default TeamRepositoriesSection;

