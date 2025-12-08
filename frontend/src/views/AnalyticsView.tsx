import React, { useState, useEffect, useCallback } from 'react';
import { api, type DailyMetric, type TeamMetrics } from '../lib/api';
import { type ExportOption } from '../components/shared/ExportManager';
import { exportData as exportDataUtil, prepareAnalyticsExportData, type ExportScope } from '../utils/exportUtils';
import { useChartData } from '../hooks/useChartData';
import AnalyticsHeader from '../components/analytics/AnalyticsHeader';
import AnalyticsSummaryStats from '../components/analytics/AnalyticsSummaryStats';
import AnalyticsTabNavigation, { type AnalyticsTab } from '../components/analytics/AnalyticsTabNavigation';
import CoverageTrendChart from '../components/analytics/CoverageTrendChart';
import CoverageDistributionChart from '../components/analytics/CoverageDistributionChart';
import DetailedCoverageTrendChart from '../components/analytics/DetailedCoverageTrendChart';
import TestMethodsGrowthChart from '../components/analytics/TestMethodsGrowthChart';
import TeamComparisonChart from '../components/analytics/TeamComparisonChart';
import TeamPerformanceTable from '../components/analytics/TeamPerformanceTable';
import RepositoryGrowthChart from '../components/analytics/RepositoryGrowthChart';
import NewMethodsChart from '../components/analytics/NewMethodsChart';

const AnalyticsView: React.FC = () => {
  const [dailyMetrics, setDailyMetrics] = useState<DailyMetric[]>([]);
  const [teams, setTeams] = useState<TeamMetrics[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [timeRange, setTimeRange] = useState<'7' | '30' | '90' | '365'>('30');
  const [activeTab, setActiveTab] = useState<AnalyticsTab>('overview');

  const fetchAnalyticsData = useCallback(async () => {
    try {
      setLoading(true);
      const days = parseInt(timeRange);

      // Fetch all analytics data in parallel
      const [metricsData, teamsData] = await Promise.all([
        api.analytics.getDailyMetrics(days).catch(() => []),
        api.teams.getComparison().catch(() => [])
      ]);

      // Normalize responses to ensure they're arrays (handle null/undefined)
      setDailyMetrics(Array.isArray(metricsData) ? metricsData : []);
      setTeams(Array.isArray(teamsData) ? teamsData : []);
    } catch (err) {
      setError('Failed to fetch analytics data');
      console.error('Error fetching analytics:', err);
    } finally {
      setLoading(false);
    }
  }, [timeRange]);

  useEffect(() => {
    fetchAnalyticsData();
  }, [fetchAnalyticsData]);

  const getCoverageTrend = (): 'up' | 'down' | 'stable' => {
    if (dailyMetrics.length < 2) return 'stable';
    const first = dailyMetrics[0].overallCoverageRate;
    const last = dailyMetrics[dailyMetrics.length - 1].overallCoverageRate;
    const diff = last - first;

    if (diff > 1) return 'up';
    if (diff < -1) return 'down';
    return 'stable';
  };

  const getTrendIcon = (trend: string) => {
    switch (trend) {
      case 'up': return '↗️';
      case 'down': return '↘️';
      default: return '→';
    }
  };

  const handleExport = async (option: ExportOption) => {
    try {
      const scope = option.scope as ExportScope;
      const exportData = prepareAnalyticsExportData(dailyMetrics, scope);

      await exportDataUtil(exportData, option);
    } catch (err) {
      console.error('Error exporting analytics:', err);
    }
  };

  // Use custom hook for chart data transformation
  const { chartData, teamComparisonData, coverageDistribution } = useChartData(dailyMetrics, teams);

  // Calculated metrics
  const averageCoverage = dailyMetrics.length > 0
    ? dailyMetrics.reduce((sum, m) => sum + m.overallCoverageRate, 0) / dailyMetrics.length
    : 0;
  const coverageTrend = getCoverageTrend();

  if (loading) {
    return (
      <div className="p-8">
        <div className="flex items-center justify-center h-64">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-8">
        <div className="text-center py-12">
          <div className="mb-4" style={{ color: 'var(--color-destructive)' }}>{error}</div>
          <button
            onClick={fetchAnalyticsData}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="p-8">
      {/* Header */}
      <AnalyticsHeader
        timeRange={timeRange}
        onTimeRangeChange={setTimeRange}
        onRefresh={fetchAnalyticsData}
        onExport={handleExport}
        dailyMetrics={dailyMetrics}
      />

      {/* Overview Stats */}
      <AnalyticsSummaryStats
        daysTracked={dailyMetrics.length}
        averageCoverage={averageCoverage}
        totalTeams={teams.length}
        coverageTrend={coverageTrend}
        trendIcon={getTrendIcon(coverageTrend)}
      />

      {/* Tab Navigation */}
      <AnalyticsTabNavigation
        activeTab={activeTab}
        onTabChange={setActiveTab}
      />

      {/* Tab Content */}
      <div className="space-y-6">
        {activeTab === 'overview' && (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <CoverageTrendChart data={chartData} />
            <CoverageDistributionChart data={coverageDistribution} />
          </div>
        )}

        {activeTab === 'trends' && (
          <div className="space-y-6">
            <DetailedCoverageTrendChart data={chartData} />
            <TestMethodsGrowthChart data={chartData} />
          </div>
        )}

        {activeTab === 'teams' && (
          <div className="space-y-6">
            <TeamComparisonChart data={teamComparisonData} />
            <TeamPerformanceTable teams={teams} />
          </div>
        )}

        {activeTab === 'growth' && (
          <div className="space-y-6">
            <RepositoryGrowthChart data={chartData} />
            <NewMethodsChart data={chartData} />
          </div>
        )}
      </div>
    </div>
  );
};

export default AnalyticsView;
