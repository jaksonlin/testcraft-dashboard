import React from 'react';
import { 
  BarChart, 
  Bar, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  ResponsiveContainer
} from 'recharts';
import { type TeamSummary } from '../../lib/api';

interface TeamPerformanceChartProps {
  teamMetrics: TeamSummary[];
}

const TeamPerformanceChart: React.FC<TeamPerformanceChartProps> = ({ teamMetrics }) => {
  const teamChartData = teamMetrics.map(team => ({
    name: team.teamName,
    repositories: team.repositoryCount,
    testMethods: team.testMethodCount,
    coverage: team.coverageRate
  }));

  return (
    <div className="card">
      <div className="card-header">
        <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">Team Performance</h3>
      </div>
      <div className="h-80">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={teamChartData}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="name" />
            <YAxis />
            <Tooltip />
            <Bar dataKey="repositories" fill="#3b82f6" name="Repositories" />
            <Bar dataKey="testMethods" fill="#10b981" name="Test Methods" />
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};

export default TeamPerformanceChart;
