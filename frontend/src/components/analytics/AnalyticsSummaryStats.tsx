import React from 'react';
import { Calendar, TrendingUp, Users, BarChart3 } from 'lucide-react';
import StatCard from '../shared/StatCard';

interface AnalyticsSummaryStatsProps {
  daysTracked: number;
  averageCoverage: number;
  totalTeams: number;
  coverageTrend: 'up' | 'down' | 'stable';
  trendIcon: string;
}

const AnalyticsSummaryStats: React.FC<AnalyticsSummaryStatsProps> = ({
  daysTracked,
  averageCoverage,
  totalTeams,
  coverageTrend,
  trendIcon,
}) => {
  const getTrendColor = () => {
    if (coverageTrend === 'up') return 'green';
    if (coverageTrend === 'down') return 'red';
    return 'gray';
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
      <StatCard
        title="Days Tracked"
        value={daysTracked.toString()}
        icon={<Calendar className="h-5 w-5" />}
        color="blue"
      />
      <StatCard
        title="Average Coverage"
        value={`${(averageCoverage ?? 0).toFixed(1)}%`}
        icon={<TrendingUp className="h-5 w-5" />}
        color="orange"
      />
      <StatCard
        title="Total Teams"
        value={totalTeams.toString()}
        icon={<Users className="h-5 w-5" />}
        color="green"
      />
      <StatCard
        title="Coverage Trend"
        value={`${trendIcon} ${coverageTrend.toUpperCase()}`}
        icon={<BarChart3 className="h-5 w-5" />}
        color={getTrendColor()}
      />
    </div>
  );
};

export default AnalyticsSummaryStats;

