import React from 'react';
import { Users, ExternalLink, Calendar } from 'lucide-react';
import { type TeamMetrics } from '../../lib/api';
import { formatDate } from '../../utils/dateUtils';

interface TeamInfoSectionProps {
  team: TeamMetrics;
}

const TeamInfoSection: React.FC<TeamInfoSectionProps> = ({ team }) => {
  return (
    <div className="space-y-4">
      <div>
        <h4 className="text-lg font-medium mb-3" style={{ color: 'var(--color-foreground)' }}>Team Information</h4>
        <div className="space-y-3">
          <div className="flex items-center space-x-3">
            <Users className="h-4 w-4 text-gray-400" />
            <div>
              <p className="text-sm" style={{ color: 'var(--color-muted-foreground)' }}>Team Name</p>
              <p className="font-medium" style={{ color: 'var(--color-foreground)' }}>{team.teamName}</p>
            </div>
          </div>
          
          <div className="flex items-center space-x-3">
            <ExternalLink className="h-4 w-4 text-gray-400" />
            <div>
              <p className="text-sm" style={{ color: 'var(--color-muted-foreground)' }}>Team Code</p>
              <p className="font-mono text-sm px-2 py-1 rounded" style={{ backgroundColor: 'var(--color-muted)' }}>
                {team.teamCode}
              </p>
            </div>
          </div>
          
          {team.department && (
            <div className="flex items-center space-x-3">
              <Users className="h-4 w-4 text-gray-400" />
              <div>
                <p className="text-sm" style={{ color: 'var(--color-muted-foreground)' }}>Department</p>
                <p className="font-medium" style={{ color: 'var(--color-foreground)' }}>{team.department}</p>
              </div>
            </div>
          )}
          
          <div className="flex items-center space-x-3">
            <Calendar className="h-4 w-4 text-gray-400" />
            <div>
              <p className="text-sm" style={{ color: 'var(--color-muted-foreground)' }}>Last Scan</p>
              <p className="font-medium" style={{ color: 'var(--color-foreground)' }}>
                {team.lastScanDate ? formatDate(team.lastScanDate).date : 'N/A'}
              </p>
              {team.lastScanDate && (
                <p className="text-xs" style={{ color: 'var(--color-muted-foreground)' }}>{formatDate(team.lastScanDate).relative}</p>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default TeamInfoSection;

