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
  // Handle empty data
  if (!teamMetrics || teamMetrics.length === 0) {
    return (
      <div className="card">
        <div className="card-header">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">Team Performance</h3>
        </div>
        <div className="h-80 flex items-center justify-center">
          <p className="text-gray-500 dark:text-gray-400">No team data available</p>
        </div>
      </div>
    );
  }

  // Sort by test method count (descending) to show top performance teams on the left
  // Map backend data structure (totalTestMethods) to frontend expected structure (testMethodCount)
  const sortedTeamMetrics = [...teamMetrics].sort((a, b) => {
    const aCount = a.testMethodCount ?? (a as any).totalTestMethods ?? 0;
    const bCount = b.testMethodCount ?? (b as any).totalTestMethods ?? 0;
    return bCount - aCount;
  });
  
  const teamChartData = sortedTeamMetrics.map(team => {
    const testMethodCount = team.testMethodCount ?? (team as any).totalTestMethods ?? 0;
    const coverageRate = team.coverageRate ?? (team as any).averageCoverageRate ?? 0;
    return {
      name: team.teamName,
      testMethods: testMethodCount,
      coverage: coverageRate
    };
  });

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
            <Bar dataKey="testMethods" fill="#10b981" name="Test Methods" />
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};

export default TeamPerformanceChart;
