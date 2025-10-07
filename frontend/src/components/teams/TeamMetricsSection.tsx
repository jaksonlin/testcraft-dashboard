import React from 'react';
import { GitBranch, TestTube, CheckCircle } from 'lucide-react';
import { type TeamMetrics } from '../../lib/api';

interface TeamMetricsSectionProps {
  team: TeamMetrics;
}

const TeamMetricsSection: React.FC<TeamMetricsSectionProps> = ({ team }) => {
  return (
    <div className="space-y-4">
      <div>
        <h4 className="text-lg font-medium mb-3" style={{ color: 'var(--color-foreground)' }}>Team Metrics</h4>
        <div className="grid grid-cols-2 gap-4">
          <div className="p-4 bg-blue-50 rounded-lg">
            <div className="flex items-center space-x-2 mb-2">
              <GitBranch className="h-4 w-4 text-blue-600" />
              <span className="text-sm font-medium text-blue-900">Repositories</span>
            </div>
            <p className="text-2xl font-bold text-blue-600">{team.repositoryCount}</p>
          </div>
          
          <div className="p-4 bg-green-50 rounded-lg">
            <div className="flex items-center space-x-2 mb-2">
              <TestTube className="h-4 w-4 text-green-600" />
              <span className="text-sm font-medium text-green-900">Test Classes</span>
            </div>
            <p className="text-2xl font-bold text-green-600">{team.totalTestClasses}</p>
          </div>
          
          <div className="p-4 bg-purple-custom-light rounded-lg">
            <div className="flex items-center space-x-2 mb-2">
              <CheckCircle className="h-4 w-4 text-purple-custom" />
              <span className="text-sm font-medium text-purple-custom-dark">Test Methods</span>
            </div>
            <p className="text-2xl font-bold text-purple-custom">{team.totalTestMethods}</p>
          </div>
          
          <div className="p-4 bg-orange-custom-light rounded-lg">
            <div className="flex items-center space-x-2 mb-2">
              <CheckCircle className="h-4 w-4 text-orange-custom" />
              <span className="text-sm font-medium text-orange-custom-dark">Coverage</span>
            </div>
            <p className="text-2xl font-bold text-orange-custom">{team.averageCoverageRate.toFixed(1)}%</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default TeamMetricsSection;

