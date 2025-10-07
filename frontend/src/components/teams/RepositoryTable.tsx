import React from 'react';
import GitUrlLink from '../shared/GitUrlLink';

interface Repository {
  repositoryId: number;
  repositoryName: string;
  gitUrl: string;
  testClassCount: number;
  testMethodCount: number;
  coverageRate: number;
  lastScanDate?: string;
}

interface RepositoryTableProps {
  repositories: Repository[];
}

const RepositoryTable: React.FC<RepositoryTableProps> = ({ repositories }) => {
  return (
    <div className="overflow-x-auto">
      <table className="min-w-full border rounded-lg" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)' }}>
        <thead style={{ backgroundColor: 'var(--color-muted)' }}>
          <tr>
            <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>
              Repository
            </th>
            <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>
              Test Classes
            </th>
            <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>
              Test Methods
            </th>
            <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>
              Coverage
            </th>
            <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>
              Last Scan
            </th>
          </tr>
        </thead>
        <tbody className="divide-y" style={{ borderColor: 'var(--color-border)' }}>
          {repositories.map((repo, index) => (
            <tr key={`repo-${repo.repositoryId}-${index}`} className="hover:bg-gray-50">
              <td className="px-4 py-3">
                <div>
                  <div className="text-sm font-medium" style={{ color: 'var(--color-foreground)' }}>{repo.repositoryName}</div>
                  <GitUrlLink 
                    url={repo.gitUrl}
                    className="mt-1"
                    truncate={true}
                    maxWidth="max-w-xs"
                  />
                </div>
              </td>
              <td className="px-4 py-3 text-sm" style={{ color: 'var(--color-foreground)' }}>{repo.testClassCount}</td>
              <td className="px-4 py-3 text-sm" style={{ color: 'var(--color-foreground)' }}>{repo.testMethodCount}</td>
              <td className="px-4 py-3">
                <div className="flex items-center">
                  <div className="w-16 bg-gray-200 rounded-full h-2 mr-2">
                    <div
                      className={`h-2 rounded-full transition-all duration-300 ${
                        repo.coverageRate >= 80 ? 'bg-green-500' :
                        repo.coverageRate >= 60 ? 'bg-yellow-500' : 'bg-red-500'
                      }`}
                      style={{ width: `${Math.min(repo.coverageRate, 100)}%` }}
                    />
                  </div>
                  <span className="text-sm" style={{ color: 'var(--color-foreground)' }}>{repo.coverageRate.toFixed(1)}%</span>
                </div>
              </td>
              <td className="px-4 py-3 text-sm" style={{ color: 'var(--color-muted-foreground)' }}>
                {repo.lastScanDate ? new Date(repo.lastScanDate).toLocaleDateString() : 'N/A'}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default RepositoryTable;

