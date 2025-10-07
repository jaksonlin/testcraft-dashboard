import React from 'react';
import { Users, X } from 'lucide-react';
import { type TeamMetrics } from '../../lib/api';
import TeamInfoSection from './TeamInfoSection';
import TeamMetricsSection from './TeamMetricsSection';
import CoverageAnalysisSection from './CoverageAnalysisSection';
import TeamRepositoriesSection from './TeamRepositoriesSection';

interface TeamDetailModalProps {
  team: TeamMetrics | null;
  isOpen: boolean;
  onClose: () => void;
}

const TeamDetailModal: React.FC<TeamDetailModalProps> = ({ team, isOpen, onClose }) => {
  if (!isOpen || !team) return null;

  const exportTeamData = () => {
    const csvContent = [
      ['Team Name', 'Team Code', 'Department', 'Repositories', 'Test Classes', 'Test Methods', 'Coverage Rate'],
      [team.teamName, team.teamCode, team.department || '', team.repositoryCount.toString(), team.totalTestClasses.toString(), team.totalTestMethods.toString(), team.averageCoverageRate.toFixed(2)]
    ].map(row => row.join(',')).join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `team-${team.teamCode}-export-${new Date().toISOString().split('T')[0]}.csv`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* Backdrop */}
      <div 
        className="absolute inset-0 bg-black/20 backdrop-blur-sm"
        onClick={onClose}
      />
      
      {/* Modal Content */}
      <div className="relative w-full max-w-4xl max-h-[90vh] overflow-y-auto rounded-xl shadow-2xl border animate-in fade-in-0 zoom-in-95 duration-300" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)' }}>
        {/* Header */}
        <div className="sticky top-0 border-b px-6 py-4 rounded-t-xl" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)' }}>
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <div className="p-2 bg-blue-100 dark:bg-blue-900/30 rounded-lg">
                <Users className="h-5 w-5 text-blue-600 dark:text-blue-400" />
              </div>
              <div>
                <h3 className="text-xl font-semibold" style={{ color: 'var(--color-foreground)' }}>Team Details</h3>
                <p className="text-sm" style={{ color: 'var(--color-muted-foreground)' }}>{team.teamName} ({team.teamCode})</p>
              </div>
            </div>
            <button
              onClick={onClose}
              className="p-2 text-gray-400 dark:text-gray-500 hover:text-gray-600 dark:hover:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg transition-colors"
            >
              <X className="h-5 w-5" />
            </button>
          </div>
        </div>
        
        {/* Content */}
        <div className="p-6 space-y-6">
          {/* Team Information and Metrics */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <TeamInfoSection team={team} />
            <TeamMetricsSection team={team} />
          </div>
          
          {/* Coverage Breakdown */}
          <CoverageAnalysisSection team={team} />

          {/* Repositories Section */}
          <TeamRepositoriesSection team={team} />
          
          {/* Action Buttons */}
          <div className="flex justify-end space-x-3 pt-4 border-t border-gray-200">
            <button
              onClick={onClose}
              className="btn btn-secondary"
            >
              Close
            </button>
            <button
              onClick={exportTeamData}
              className="btn btn-primary"
            >
              Export Team Data
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default TeamDetailModal;

