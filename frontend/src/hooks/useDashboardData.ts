import { useState, useEffect, useCallback } from 'react';
import { api, type DashboardOverview, type TeamSummary, type ScanStatus, type ScanConfig } from '../lib/api';

interface UseDashboardDataReturn {
  overview: DashboardOverview | null;
  teamMetrics: TeamSummary[];
  scanStatus: ScanStatus | null;
  scanConfig: ScanConfig | null;
  loading: boolean;
  error: string | null;
  lastRefreshTime: Date;
  fetchDashboardData: (skipConfig?: boolean) => Promise<void>;
  fetchScanConfig: () => Promise<void>;
}

export const useDashboardData = (showConfigPanel: boolean = false): UseDashboardDataReturn => {
  const [overview, setOverview] = useState<DashboardOverview | null>(null);
  const [teamMetrics, setTeamMetrics] = useState<TeamSummary[]>([]);
  const [scanStatus, setScanStatus] = useState<ScanStatus | null>(null);
  const [scanConfig, setScanConfig] = useState<ScanConfig | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [lastRefreshTime, setLastRefreshTime] = useState<Date>(new Date());

  const fetchDashboardData = useCallback(async (skipConfig = false) => {
    try {
      setLoading(true);
      setError(null);
      
      const [overviewData, teamsData, statusData] = await Promise.all([
        api.dashboard.getOverview(),
        api.dashboard.getTeamMetrics(),
        api.scan.getStatus()
      ]);
      
      setOverview(overviewData);
      setTeamMetrics(teamsData);
      setScanStatus(statusData);
      
      // Only fetch config data if not skipping and config modal is not open
      if (!skipConfig && !showConfigPanel) {
        const configData = await api.scan.getConfig();
        setScanConfig(configData);
      }
      
      // Update last refresh time
      setLastRefreshTime(new Date());
    } catch (err) {
      console.error('Error fetching dashboard data:', err);
      setError('Failed to load dashboard data. Please check if the backend is running.');
    } finally {
      setLoading(false);
    }
  }, [showConfigPanel]);

  const fetchScanConfig = useCallback(async () => {
    try {
      const configData = await api.scan.getConfig();
      setScanConfig(configData);
    } catch (err) {
      console.error('Error fetching scan config:', err);
    }
  }, []);

  useEffect(() => {
    fetchDashboardData();
    
    // Refresh data every 30 seconds, but skip config if modal is open
    const interval = setInterval(() => {
      fetchDashboardData(showConfigPanel);
    }, 30000);
    
    return () => clearInterval(interval);
  }, [fetchDashboardData, showConfigPanel]);

  return {
    overview,
    teamMetrics,
    scanStatus,
    scanConfig,
    loading,
    error,
    lastRefreshTime,
    fetchDashboardData,
    fetchScanConfig
  };
};
