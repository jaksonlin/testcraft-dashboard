import { useMemo } from 'react';
import { type DailyMetric, type TeamMetrics } from '../lib/api';

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('en-US', { 
    month: 'short', 
    day: 'numeric' 
  });
};

export const useChartData = (dailyMetrics: DailyMetric[], teams: TeamMetrics[]) => {
  const chartData = useMemo(() => {
    return dailyMetrics.map(metric => ({
      date: formatDate(metric.date),
      fullDate: metric.date,
      coverage: metric.overallCoverageRate,
      repositories: metric.totalRepositories,
      testMethods: metric.totalTestMethods,
      annotatedMethods: metric.totalAnnotatedMethods,
      newTestMethods: metric.newTestMethods,
      newAnnotatedMethods: metric.newAnnotatedMethods
    }));
  }, [dailyMetrics]);

  const teamComparisonData = useMemo(() => {
    return teams.map(team => ({
      name: team.teamName,
      repositories: team.repositoryCount,
      coverage: team.averageCoverageRate,
      testMethods: team.totalTestMethods,
      annotatedMethods: team.totalAnnotatedMethods
    }));
  }, [teams]);

  const coverageDistribution = useMemo(() => {
    return [
      { name: 'High Coverage (≥80%)', value: teams.filter(t => t.averageCoverageRate >= 80).length, color: '#10b981' },
      { name: 'Medium Coverage (60-79%)', value: teams.filter(t => t.averageCoverageRate >= 60 && t.averageCoverageRate < 80).length, color: '#f59e0b' },
      { name: 'Low Coverage (<60%)', value: teams.filter(t => t.averageCoverageRate < 60).length, color: '#ef4444' }
    ].filter(item => item.value > 0); // Only show categories with teams
  }, [teams]);

  return {
    chartData,
    teamComparisonData,
    coverageDistribution
  };
};

