import React, { useState, useEffect } from 'react';
import { BarChart3, FileText } from 'lucide-react';
import { api, type ScanSession, type RepositorySummary, type DashboardOverview } from '../../lib/api';
import ScanHistoryTimeline from './ScanHistoryTimeline';
import ScanDetailModal from './ScanDetailModal';

type RepositoryData = RepositorySummary;


interface ReportsSectionProps {
  repositories?: RepositoryData[];
  dashboardOverview?: DashboardOverview;
}

const ReportsSection: React.FC<ReportsSectionProps> = () => {
  const [scanHistory, setScanHistory] = useState<ScanSession[]>([]);
  const [selectedScan, setSelectedScan] = useState<ScanSession | null>(null);
  const [showScanDetail, setShowScanDetail] = useState(false);
  useEffect(() => {
    fetchScanHistory();
  }, []);

  const fetchScanHistory = async () => {
    try {
      const history = await api.scan.getHistory(10);
      // Normalize response to ensure it's an array
      setScanHistory(Array.isArray(history) ? history : []);
    } catch (error) {
      console.error('Error fetching scan history:', error);
      setScanHistory([]);
    }
  };


  const handleScanClick = (scan: ScanSession) => {
    setSelectedScan(scan);
    setShowScanDetail(true);
  };

  return (
    <div className="space-y-6">
      {/* Reports Section Header */}
      <div className="flex items-center justify-between pb-4 border-b border-gray-200 dark:border-gray-700">
        <div className="flex items-center space-x-3">
          <div className="p-2 bg-purple-100 dark:bg-purple-900/30 rounded-lg">
            <BarChart3 className="h-5 w-5 text-purple-600 dark:text-purple-400" />
          </div>
          <div>
            <h2 className="text-2xl font-bold text-gray-900 dark:text-gray-100">Reports & Analytics</h2>
            <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">Detailed scan insights and historical data</p>
          </div>
        </div>
      </div>

      {/* Scan History Timeline */}
      <ScanHistoryTimeline 
        sessions={scanHistory}
        onSessionClick={handleScanClick}
      />

      {/* Repository Details Note */}
      <div className="card bg-blue-50 dark:bg-blue-900/20 border-blue-200 dark:border-blue-800">
        <div className="p-4">
          <div className="flex items-start space-x-3">
            <FileText className="h-5 w-5 text-blue-600 dark:text-blue-400 mt-0.5" />
            <div>
              <h4 className="text-sm font-medium text-blue-900 dark:text-blue-200">Interactive Repository Data</h4>
              <p className="text-sm text-blue-700 dark:text-blue-300 mt-1">
                Click on any repository in the table above to view detailed scan results, 
                test method breakdown, and coverage analysis. You can also export detailed 
                reports for individual repositories or teams.
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Scan Detail Modal */}
      <ScanDetailModal
        isOpen={showScanDetail}
        onClose={() => setShowScanDetail(false)}
        scan={selectedScan}
      />
    </div>
  );
};

export default ReportsSection;
