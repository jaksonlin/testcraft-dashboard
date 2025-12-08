import React from 'react';
import { Calendar, Clock, CheckCircle, AlertCircle, Database } from 'lucide-react';
import { type ScanSession } from '../../lib/api';

interface ScanHistoryTimelineProps {
  sessions: ScanSession[];
  onSessionClick?: (session: ScanSession) => void;
}

const ScanHistoryTimeline: React.FC<ScanHistoryTimelineProps> = ({ 
  sessions, 
  onSessionClick 
}) => {
  // Normalize sessions to ensure it's an array
  const sessionsArray = Array.isArray(sessions) ? sessions : [];
  const formatDuration = (ms: number) => {
    const seconds = Math.round(ms / 1000);
    return seconds < 60 ? `${seconds}s` : `${Math.round(seconds / 60)}m ${seconds % 60}s`;
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return {
      date: date.toLocaleDateString(),
      time: date.toLocaleTimeString(),
      relative: getRelativeTime(date)
    };
  };

  const getRelativeTime = (date: Date) => {
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    const diffDays = Math.floor(diffHours / 24);

    if (diffDays > 0) return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`;
    if (diffHours > 0) return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`;
    return 'Just now';
  };

  const getCoverageRate = (session: ScanSession) => {
    if (!session || session.totalTestMethods === 0) return '0.0';
    const annotated = session.totalAnnotatedMethods ?? 0;
    const total = session.totalTestMethods ?? 0;
    return ((annotated / total) * 100).toFixed(1);
  };

  return (
    <div className="card">
      <div className="card-header">
        <div className="flex items-center space-x-2">
          <Calendar className="h-5 w-5 text-blue-600 dark:text-blue-400" />
          <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">Scan History</h3>
        </div>
      </div>
      
      <div className="p-6">
        {sessionsArray.length === 0 ? (
          <div className="text-center py-8 text-gray-500 dark:text-gray-400">
            <Database className="h-12 w-12 mx-auto mb-4 text-gray-300 dark:text-gray-600" />
            <p>No scan history available</p>
          </div>
        ) : (
          <div className="space-y-4">
            {sessionsArray.map((session, index) => {
              const { date, time, relative } = formatDate(session.scanDate);
              const coverageRate = getCoverageRate(session);
              const isSuccess = session.scanStatus === 'COMPLETED';
              
              return (
                <div
                  key={session.id}
                  onClick={() => onSessionClick?.(session)}
                  className={`p-4 border rounded-lg cursor-pointer transition-colors hover:bg-gray-50 dark:hover:bg-gray-800 ${
                    index === 0 ? 'border-blue-200 dark:border-blue-800 bg-blue-50 dark:bg-blue-900/20' : 'border-gray-200 dark:border-gray-700'
                  }`}
                >
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center space-x-2 mb-2">
                        {isSuccess ? (
                          <CheckCircle className="h-4 w-4 text-green-600 dark:text-green-400" />
                        ) : (
                          <AlertCircle className="h-4 w-4 text-red-600 dark:text-red-400" />
                        )}
                        <span className="font-medium text-gray-900 dark:text-gray-100">
                          Scan #{session.id}
                        </span>
                        {index === 0 && (
                          <span className="px-2 py-1 text-xs bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-300 rounded-full">
                            Latest
                          </span>
                        )}
                      </div>
                      
                      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                        <div>
                          <p className="text-gray-500 dark:text-gray-400">Date & Time</p>
                          <p className="font-medium text-gray-900 dark:text-gray-100">{date}</p>
                          <p className="text-gray-500 dark:text-gray-400">{time}</p>
                          <p className="text-xs text-gray-400 dark:text-gray-500">{relative}</p>
                        </div>
                        
                        <div>
                          <p className="text-gray-500 dark:text-gray-400">Repositories</p>
                          <p className="font-medium text-gray-900 dark:text-gray-100">{session.totalRepositories}</p>
                          <p className="text-gray-500 dark:text-gray-400">{session.totalTestClasses} test classes</p>
                        </div>
                        
                        <div>
                          <p className="text-gray-500 dark:text-gray-400">Test Methods</p>
                          <p className="font-medium text-gray-900 dark:text-gray-100">{session.totalTestMethods}</p>
                          <p className="text-gray-500 dark:text-gray-400">{session.totalAnnotatedMethods} annotated</p>
                        </div>
                        
                        <div>
                          <p className="text-gray-500 dark:text-gray-400">Coverage & Duration</p>
                          <p className="font-medium text-blue-600 dark:text-blue-400">{coverageRate}%</p>
                          <div className="flex items-center text-gray-500 dark:text-gray-400">
                            <Clock className="h-3 w-3 mr-1" />
                            {formatDuration(session.scanDurationMs)}
                          </div>
                        </div>
                      </div>
                      
                      {session.errorLog && (
                        <div className="mt-3 p-2 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded text-sm">
                          <p className="text-red-800 dark:text-red-200 font-medium">Error:</p>
                          <p className="text-red-700 dark:text-red-300">{session.errorLog}</p>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};

export default ScanHistoryTimeline;
