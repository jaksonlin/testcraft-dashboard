import React from 'react';
import { type TeamMetrics } from '../../lib/api';

interface TeamPerformanceTableProps {
  teams: TeamMetrics[];
}

const TeamPerformanceTable: React.FC<TeamPerformanceTableProps> = ({ teams }) => {
  return (
    <div className="rounded-lg shadow-sm border overflow-hidden" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)' }}>
      <div className="px-6 py-4 border-b" style={{ borderColor: 'var(--color-border)' }}>
        <h3 className="text-lg font-semibold" style={{ color: 'var(--color-foreground)' }}>Team Performance Summary</h3>
      </div>
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y" style={{ borderColor: 'var(--color-border)' }}>
          <thead style={{ backgroundColor: 'var(--color-muted)' }}>
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>Team</th>
              <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>Repositories</th>
              <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>Test Methods</th>
              <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>Coverage</th>
              <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>Performance</th>
            </tr>
          </thead>
          <tbody className="divide-y" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)' }}>
            {teams.map((team) => (
              <tr key={team.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="text-sm font-medium" style={{ color: 'var(--color-foreground)' }}>{team.teamName}</div>
                  <div className="text-sm" style={{ color: 'var(--color-muted-foreground)' }}>{team.teamCode}</div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm" style={{ color: 'var(--color-foreground)' }}>{team.repositoryCount}</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm" style={{ color: 'var(--color-foreground)' }}>{team.totalTestMethods}</td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="flex items-center">
                    <div className="w-20 rounded-full h-2 mr-3" style={{ backgroundColor: 'var(--color-border)' }}>
                      <div
                        className={`h-2 rounded-full ${
                          (team.averageCoverageRate ?? 0) >= 80 ? 'bg-green-500' :
                          (team.averageCoverageRate ?? 0) >= 60 ? 'bg-yellow-500' : 'bg-red-500'
                        }`}
                        style={{ width: `${Math.min(team.averageCoverageRate ?? 0, 100)}%` }}
                      />
                    </div>
                    <span className="text-sm font-medium" style={{ color: 'var(--color-foreground)' }}>{(team.averageCoverageRate ?? 0).toFixed(1)}%</span>
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                    (team.averageCoverageRate ?? 0) >= 80 ? 'bg-green-100 text-green-800' :
                    (team.averageCoverageRate ?? 0) >= 60 ? 'bg-yellow-100 text-yellow-800' : 'bg-red-100 text-red-800'
                  }`}>
                    {(team.averageCoverageRate ?? 0) >= 80 ? 'Excellent' :
                     (team.averageCoverageRate ?? 0) >= 60 ? 'Good' : 'Needs Improvement'}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default TeamPerformanceTable;

