import React from 'react';
import { type TeamMetrics } from '../../lib/api';

interface CoverageAnalysisSectionProps {
  team: TeamMetrics;
}

const CoverageAnalysisSection: React.FC<CoverageAnalysisSectionProps> = ({ team }) => {
  return (
    <div>
      <h4 className="text-lg font-medium mb-3" style={{ color: 'var(--color-foreground)' }}>Coverage Analysis</h4>
      <div className="rounded-lg p-4" style={{ backgroundColor: 'var(--color-muted)' }}>
        <div className="flex items-center justify-between mb-2">
          <span className="text-sm font-medium" style={{ color: 'var(--color-muted-foreground)' }}>Average Coverage</span>
          <span className="text-sm font-bold" style={{ color: 'var(--color-foreground)' }}>{team.averageCoverageRate.toFixed(1)}%</span>
        </div>
        <div className="w-full rounded-full h-4 relative overflow-hidden" style={{ backgroundColor: 'var(--color-border)' }}>
          <div 
            style={{ 
              width: `${Math.max(Math.min(team.averageCoverageRate, 100), 5)}%`,
              minWidth: '20px',
              height: '16px',
              backgroundColor: '#3b82f6',
              borderRadius: '9999px',
              border: '2px solid #1d4ed8',
              transition: 'all 0.5s ease-out',
              boxShadow: '0 1px 3px 0 rgba(0, 0, 0, 0.1)'
            }}
          />
        </div>
        <div className="flex justify-between text-xs text-gray-500 mt-2">
          <span style={{ color: 'var(--color-muted-foreground)' }}>{team.totalAnnotatedMethods} annotated methods</span>
          <span style={{ color: 'var(--color-muted-foreground)' }}>{team.totalTestMethods - team.totalAnnotatedMethods} remaining</span>
        </div>
      </div>
    </div>
  );
};

export default CoverageAnalysisSection;

