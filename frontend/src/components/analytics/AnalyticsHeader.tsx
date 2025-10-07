import React from 'react';
import { TrendingUp, RefreshCw } from 'lucide-react';
import ExportManager, { type ExportOption } from '../shared/ExportManager';

interface AnalyticsHeaderProps {
  timeRange: '7' | '30' | '90' | '365';
  onTimeRangeChange: (range: '7' | '30' | '90' | '365') => void;
  onRefresh: () => void;
  onExport: (option: ExportOption) => void;
  dailyMetrics: any[];
}

const AnalyticsHeader: React.FC<AnalyticsHeaderProps> = ({
  timeRange,
  onTimeRangeChange,
  onRefresh,
  onExport,
  dailyMetrics,
}) => {
  return (
    <div className="flex items-center justify-between mb-6">
      <div className="flex items-center">
        <TrendingUp className="h-8 w-8 text-blue-600 dark:text-blue-400 mr-3" />
        <h1 className="text-3xl font-bold" style={{ color: 'var(--color-foreground)' }}>Analytics</h1>
      </div>
      <div className="flex items-center gap-3">
        <select
          value={timeRange}
          onChange={(e) => onTimeRangeChange(e.target.value as '7' | '30' | '90' | '365')}
          className="px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" 
          style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)', color: 'var(--color-foreground)' }}
        >
          <option value="7">Last 7 days</option>
          <option value="30">Last 30 days</option>
          <option value="90">Last 90 days</option>
          <option value="365">Last year</option>
        </select>
        <button
          onClick={onRefresh}
          className="flex items-center px-3 py-2 rounded-lg transition-colors" 
          style={{ backgroundColor: 'var(--color-secondary)', color: 'var(--color-secondary-foreground)' }}
        >
          <RefreshCw className="h-4 w-4 mr-2" />
          Refresh
        </button>
        <ExportManager
          data={dailyMetrics}
          dataType="analytics"
          onExport={onExport}
        />
      </div>
    </div>
  );
};

export default AnalyticsHeader;

