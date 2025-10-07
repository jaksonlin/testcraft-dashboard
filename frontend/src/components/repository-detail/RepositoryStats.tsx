import React from 'react';
import { FileText, Activity, Target, Calendar } from 'lucide-react';
import { type RepositoryDetail } from '../../lib/api';
import { formatDate } from '../../utils/dateUtils';

interface RepositoryStatsProps {
  repository: RepositoryDetail;
}

const RepositoryStats: React.FC<RepositoryStatsProps> = ({ repository }) => {
  return (
    <>
      {/* Stats Overview */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div className="card">
          <div className="flex items-center">
            <FileText className="h-8 w-8 text-blue-600 mr-3" />
            <div>
              <p className="text-sm font-medium" style={{ color: 'var(--color-muted-foreground)' }}>Test Classes</p>
              <p className="text-2xl font-bold" style={{ color: 'var(--color-foreground)' }}>{repository.testClasses}</p>
            </div>
          </div>
        </div>
        
        <div className="card">
          <div className="flex items-center">
            <Activity className="h-8 w-8 text-green-600 mr-3" />
            <div>
              <p className="text-sm font-medium" style={{ color: 'var(--color-muted-foreground)' }}>Test Methods</p>
              <p className="text-2xl font-bold" style={{ color: 'var(--color-foreground)' }}>{repository.testMethodCount}</p>
            </div>
          </div>
        </div>
        
        <div className="card">
          <div className="flex items-center">
            <Target className="h-8 w-8 text-purple-600 mr-3" />
            <div>
              <p className="text-sm font-medium" style={{ color: 'var(--color-muted-foreground)' }}>Annotated Methods</p>
              <p className="text-2xl font-bold" style={{ color: 'var(--color-foreground)' }}>{repository.annotatedMethods}</p>
            </div>
          </div>
        </div>
        
        <div className="card">
          <div className="flex items-center">
            <Calendar className="h-8 w-8 text-orange-600 mr-3" />
            <div>
              <p className="text-sm font-medium" style={{ color: 'var(--color-muted-foreground)' }}>Last Scan</p>
              <p className="text-sm font-bold" style={{ color: 'var(--color-foreground)' }}>
                {formatDate(repository.lastScan).date}
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Coverage Progress */}
      <div className="card mb-8">
        <h3 className="text-lg font-semibold mb-4" style={{ color: 'var(--color-foreground)' }}>Coverage Analysis</h3>
        <div className="space-y-4">
          <div>
            <div className="flex justify-between text-sm mb-1">
              <span style={{ color: 'var(--color-muted-foreground)' }}>Overall Coverage</span>
              <span className="font-medium" style={{ color: 'var(--color-foreground)' }}>{repository.coverageRate.toFixed(1)}%</span>
            </div>
            <div className="w-full rounded-full h-3" style={{ backgroundColor: 'var(--color-border)' }}>
              <div 
                className="bg-orange-custom h-3 rounded-full transition-all duration-300" 
                style={{ width: `${repository.coverageRate}%` }}
              ></div>
            </div>
          </div>
          
          <div className="grid grid-cols-3 gap-4 text-center">
            <div>
              <div className="text-2xl font-bold text-blue-600">{repository.testClasses}</div>
              <div className="text-sm" style={{ color: 'var(--color-muted-foreground)' }}>Test Classes</div>
            </div>
            <div>
              <div className="text-2xl font-bold text-green-600">{repository.testMethodCount}</div>
              <div className="text-sm" style={{ color: 'var(--color-muted-foreground)' }}>Total Methods</div>
            </div>
            <div>
              <div className="text-2xl font-bold text-purple-600">{repository.annotatedMethods}</div>
              <div className="text-sm" style={{ color: 'var(--color-muted-foreground)' }}>Annotated</div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default RepositoryStats;

