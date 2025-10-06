import React, { useState, useEffect, useCallback } from 'react';
import { TrendingUp, BarChart3, Users, GitBranch, Calendar, RefreshCw } from 'lucide-react';
import { api, type DailyMetric, type TeamMetrics } from '../lib/api';
import StatCard from '../components/shared/StatCard';
import ExportManager, { type ExportOption } from '../components/shared/ExportManager';
import { exportData as exportDataUtil, prepareAnalyticsExportData, type ExportScope } from '../utils/exportUtils';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, BarChart, Bar, PieChart, Pie, Cell } from 'recharts';

const AnalyticsView: React.FC = () => {
  const [dailyMetrics, setDailyMetrics] = useState<DailyMetric[]>([]);
  const [teams, setTeams] = useState<TeamMetrics[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [timeRange, setTimeRange] = useState<'7' | '30' | '90' | '365'>('30');
  const [activeTab, setActiveTab] = useState<'overview' | 'trends' | 'teams' | 'growth'>('overview');

  const fetchAnalyticsData = useCallback(async () => {
    try {
      setLoading(true);
      const days = parseInt(timeRange);
      
      // Fetch all analytics data in parallel
      const [metricsData, teamsData] = await Promise.all([
        api.analytics.getDailyMetrics(days).catch(() => []),
        api.teams.getComparison().catch(() => [])
      ]);

      setDailyMetrics(metricsData);
      setTeams(teamsData);
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

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', { 
      month: 'short', 
      day: 'numeric' 
    });
  };

  const getCoverageTrend = () => {
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
      
      exportDataUtil(exportData, option);
    } catch (err) {
      console.error('Error exporting analytics:', err);
    }
  };

  const chartData = dailyMetrics.map(metric => ({
    date: formatDate(metric.date),
    fullDate: metric.date,
    coverage: metric.overallCoverageRate,
    repositories: metric.totalRepositories,
    testMethods: metric.totalTestMethods,
    annotatedMethods: metric.totalAnnotatedMethods,
    newTestMethods: metric.newTestMethods,
    newAnnotatedMethods: metric.newAnnotatedMethods
  }));

  const teamComparisonData = teams.map(team => ({
    name: team.teamName,
    repositories: team.repositoryCount,
    coverage: team.averageCoverageRate,
    testMethods: team.totalTestMethods,
    annotatedMethods: team.totalAnnotatedMethods
  }));

  const coverageDistribution = [
    { name: 'High Coverage (≥80%)', value: teams.filter(t => t.averageCoverageRate >= 80).length, color: '#10b981' },
    { name: 'Medium Coverage (60-79%)', value: teams.filter(t => t.averageCoverageRate >= 60 && t.averageCoverageRate < 80).length, color: '#f59e0b' },
    { name: 'Low Coverage (<60%)', value: teams.filter(t => t.averageCoverageRate < 60).length, color: '#ef4444' }
  ].filter(item => item.value > 0); // Only show categories with teams

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
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center">
        <TrendingUp className="h-8 w-8 text-blue-600 dark:text-blue-400 mr-3" />
        <h1 className="text-3xl font-bold" style={{ color: 'var(--color-foreground)' }}>Analytics</h1>
        </div>
        <div className="flex items-center gap-3">
          <select
            value={timeRange}
            onChange={(e) => setTimeRange(e.target.value as '7' | '30' | '90' | '365')}
            className="px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)', color: 'var(--color-foreground)' }}
          >
            <option value="7">Last 7 days</option>
            <option value="30">Last 30 days</option>
            <option value="90">Last 90 days</option>
            <option value="365">Last year</option>
          </select>
          <button
            onClick={fetchAnalyticsData}
            className="flex items-center px-3 py-2 rounded-lg transition-colors" style={{ backgroundColor: 'var(--color-secondary)', color: 'var(--color-secondary-foreground)' }}
          >
            <RefreshCw className="h-4 w-4 mr-2" />
            Refresh
          </button>
          <ExportManager
            data={dailyMetrics}
            dataType="analytics"
            onExport={handleExport}
          />
        </div>
      </div>

      {/* Overview Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
        <StatCard
          title="Days Tracked"
          value={dailyMetrics.length.toString()}
          icon={<Calendar className="h-5 w-5" />}
          color="blue"
        />
        <StatCard
          title="Average Coverage"
          value={`${dailyMetrics.length > 0 ? (dailyMetrics.reduce((sum, m) => sum + m.overallCoverageRate, 0) / dailyMetrics.length).toFixed(1) : '0'}%`}
          icon={<TrendingUp className="h-5 w-5" />}
          color="orange"
        />
        <StatCard
          title="Total Teams"
          value={teams.length.toString()}
          icon={<Users className="h-5 w-5" />}
          color="green"
        />
        <StatCard
          title="Coverage Trend"
          value={`${getTrendIcon(getCoverageTrend())} ${getCoverageTrend().toUpperCase()}`}
          icon={<BarChart3 className="h-5 w-5" />}
          color={getCoverageTrend() === 'up' ? 'green' : getCoverageTrend() === 'down' ? 'red' : 'gray'}
        />
      </div>

      {/* Tab Navigation */}
      <div className="mb-6">
        <nav className="flex space-x-8">
          {[
            { id: 'overview', label: 'Overview', icon: BarChart3 },
            { id: 'trends', label: 'Trends', icon: TrendingUp },
            { id: 'teams', label: 'Team Comparison', icon: Users },
            { id: 'growth', label: 'Growth Analysis', icon: GitBranch }
          ].map(({ id, label, icon: Icon }) => (
            <button
              key={id}
              onClick={() => setActiveTab(id as 'overview' | 'trends' | 'teams' | 'growth')}
              className={`flex items-center px-3 py-2 text-sm font-medium rounded-lg transition-colors ${
                activeTab === id ? 'opacity-100' : 'opacity-70'
              }`}
              style={{
                backgroundColor: activeTab === id ? 'var(--color-accent)' : 'transparent',
                color: activeTab === id ? 'var(--color-primary)' : 'var(--color-muted-foreground)'
              }}
              onMouseEnter={(e) => {
                if (activeTab !== id) {
                  e.currentTarget.style.backgroundColor = 'var(--color-accent)';
                  e.currentTarget.style.color = 'var(--color-foreground)';
                }
              }}
              onMouseLeave={(e) => {
                if (activeTab !== id) {
                  e.currentTarget.style.backgroundColor = 'transparent';
                  e.currentTarget.style.color = 'var(--color-muted-foreground)';
                }
              }}
            >
              <Icon className="h-4 w-4 mr-2" />
              {label}
            </button>
          ))}
        </nav>
      </div>
      
      {/* Tab Content */}
      <div className="space-y-6">
        {activeTab === 'overview' && (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Coverage Trend Chart */}
            <div className="rounded-lg shadow-sm border p-6" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)' }}>
              <h3 className="text-lg font-semibold mb-4" style={{ color: 'var(--color-foreground)' }}>Coverage Trend</h3>
              <ResponsiveContainer width="100%" height={300}>
                <LineChart data={chartData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Line 
                    type="monotone" 
                    dataKey="coverage" 
                    stroke="#3b82f6" 
                    strokeWidth={2}
                    name="Coverage %"
                  />
                </LineChart>
              </ResponsiveContainer>
            </div>

            {/* Team Coverage Distribution */}
            <div className="rounded-lg shadow-sm border p-6" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)' }}>
              <h3 className="text-lg font-semibold mb-4" style={{ color: 'var(--color-foreground)' }}>Team Coverage Distribution</h3>
              {coverageDistribution.length > 0 ? (
                <ResponsiveContainer width="100%" height={300}>
                  <PieChart>
                    <Pie
                      data={coverageDistribution}
                      cx="50%"
                      cy="50%"
                      labelLine={false}
                      label={({ name, percent }) => `${name}: ${((percent as number) * 100).toFixed(0)}%`}
                      outerRadius={80}
                      fill="#8884d8"
                      dataKey="value"
                    >
                      {coverageDistribution.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={entry.color} />
                      ))}
                    </Pie>
                    <Tooltip 
                      formatter={(value: number, name: string) => [`${value} teams`, name]}
                    />
                    <Legend />
                  </PieChart>
                </ResponsiveContainer>
              ) : (
                <div className="flex items-center justify-center h-64" style={{ color: 'var(--color-muted-foreground)' }}>
                  <div className="text-center">
                    <BarChart3 className="h-12 w-12 mx-auto mb-4" style={{ color: 'var(--color-muted-foreground)' }} />
                    <p>No team coverage data available</p>
                  </div>
                </div>
              )}
            </div>
          </div>
        )}

        {activeTab === 'trends' && (
          <div className="space-y-6">
            {/* Coverage Trend */}
            <div className="rounded-lg shadow-sm border p-6" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)' }}>
              <h3 className="text-lg font-semibold mb-4" style={{ color: 'var(--color-foreground)' }}>Coverage Trend Over Time</h3>
              <ResponsiveContainer width="100%" height={400}>
                <LineChart data={chartData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" />
                  <YAxis domain={[0, 100]} />
                  <Tooltip 
                    formatter={(value: number) => [`${value.toFixed(1)}%`, 'Coverage']}
                    labelFormatter={(label) => `Date: ${label}`}
                  />
                  <Legend />
                  <Line 
                    type="monotone" 
                    dataKey="coverage" 
                    stroke="#3b82f6" 
                    strokeWidth={3}
                    name="Coverage %"
                    dot={{ fill: '#3b82f6', strokeWidth: 2, r: 4 }}
                  />
                </LineChart>
              </ResponsiveContainer>
            </div>

            {/* Test Methods Growth */}
            <div className="rounded-lg shadow-sm border p-6" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)' }}>
              <h3 className="text-lg font-semibold mb-4" style={{ color: 'var(--color-foreground)' }}>Test Methods Growth</h3>
              <ResponsiveContainer width="100%" height={400}>
                <LineChart data={chartData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Line 
                    type="monotone" 
                    dataKey="testMethods" 
                    stroke="#10b981" 
                    strokeWidth={2}
                    name="Total Test Methods"
                  />
                  <Line 
                    type="monotone" 
                    dataKey="annotatedMethods" 
                    stroke="#8b5cf6" 
                    strokeWidth={2}
                    name="Annotated Methods"
                  />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </div>
        )}

        {activeTab === 'teams' && (
          <div className="space-y-6">
            {/* Team Coverage Comparison */}
            <div className="rounded-lg shadow-sm border p-6" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)' }}>
              <h3 className="text-lg font-semibold mb-4" style={{ color: 'var(--color-foreground)' }}>Team Coverage Comparison</h3>
              <ResponsiveContainer width="100%" height={400}>
                <BarChart data={teamComparisonData} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="name" angle={-45} textAnchor="end" height={100} />
                  <YAxis domain={[0, 100]} />
                  <Tooltip formatter={(value: number) => [`${value.toFixed(1)}%`, 'Coverage']} />
                  <Legend />
                  <Bar dataKey="coverage" fill="#3b82f6" name="Coverage %" />
                </BarChart>
              </ResponsiveContainer>
            </div>

            {/* Team Metrics Table */}
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
                                  team.averageCoverageRate >= 80 ? 'bg-green-500' :
                                  team.averageCoverageRate >= 60 ? 'bg-yellow-500' : 'bg-red-500'
                                }`}
                                style={{ width: `${Math.min(team.averageCoverageRate, 100)}%` }}
                              />
                            </div>
                            <span className="text-sm font-medium" style={{ color: 'var(--color-foreground)' }}>{team.averageCoverageRate.toFixed(1)}%</span>
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                            team.averageCoverageRate >= 80 ? 'bg-green-100 text-green-800' :
                            team.averageCoverageRate >= 60 ? 'bg-yellow-100 text-yellow-800' : 'bg-red-100 text-red-800'
                          }`}>
                            {team.averageCoverageRate >= 80 ? 'Excellent' :
                             team.averageCoverageRate >= 60 ? 'Good' : 'Needs Improvement'}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'growth' && (
          <div className="space-y-6">
            {/* Repository Growth */}
            <div className="rounded-lg shadow-sm border p-6" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)' }}>
              <h3 className="text-lg font-semibold mb-4" style={{ color: 'var(--color-foreground)' }}>Repository Growth</h3>
              <ResponsiveContainer width="100%" height={400}>
                <LineChart data={chartData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Line 
                    type="monotone" 
                    dataKey="repositories" 
                    stroke="#10b981" 
                    strokeWidth={2}
                    name="Total Repositories"
                  />
                </LineChart>
              </ResponsiveContainer>
            </div>

            {/* New Methods Added */}
            <div className="rounded-lg shadow-sm border p-6" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)' }}>
              <h3 className="text-lg font-semibold mb-4" style={{ color: 'var(--color-foreground)' }}>New Methods Added Daily</h3>
              <ResponsiveContainer width="100%" height={400}>
                <BarChart data={chartData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Bar dataKey="newTestMethods" fill="#3b82f6" name="New Test Methods" />
                  <Bar dataKey="newAnnotatedMethods" fill="#8b5cf6" name="New Annotated Methods" />
                </BarChart>
              </ResponsiveContainer>
          </div>
        </div>
        )}
      </div>
    </div>
  );
};

export default AnalyticsView;