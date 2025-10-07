import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Play, Download, Users, Target } from 'lucide-react';
import { type RepositoryDetail } from '../../lib/api';
import GitUrlLink from '../shared/GitUrlLink';

interface RepositoryHeaderProps {
  repository: RepositoryDetail;
  scanning: boolean;
  onScan: () => void;
  onExport: () => void;
}

const getCoverageColor = (coverage: number) => {
  if (coverage >= 80) return 'text-green-600';
  if (coverage >= 50) return 'text-orange-600';
  return 'text-red-600';
};

const getCoverageBgColor = (coverage: number) => {
  if (coverage >= 80) return 'bg-green-100';
  if (coverage >= 50) return 'bg-orange-100';
  return 'bg-red-100';
};

const RepositoryHeader: React.FC<RepositoryHeaderProps> = ({
  repository,
  scanning,
  onScan,
  onExport,
}) => {
  const navigate = useNavigate();

  return (
    <div className="mb-8">
      <div className="flex items-center justify-between mb-4">
        <button
          onClick={() => navigate('/repositories')}
          className="flex items-center hover:opacity-80" 
          style={{ color: 'var(--color-muted-foreground)' }}
        >
          <ArrowLeft className="h-5 w-5 mr-2" />
          Back to Repositories
        </button>
        
        <div className="flex gap-2">
          <button
            onClick={onScan}
            disabled={scanning}
            className="btn btn-primary"
          >
            <Play className="h-4 w-4 mr-2" />
            {scanning ? 'Scanning...' : 'Scan Repository'}
          </button>
          <button
            onClick={onExport}
            className="btn btn-secondary"
          >
            <Download className="h-4 w-4 mr-2" />
            Export Data
          </button>
        </div>
      </div>
      
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-3xl font-bold mb-2" style={{ color: 'var(--color-foreground)' }}>
            {repository.repository}
          </h1>
          <div className="flex items-center mb-4" style={{ color: 'var(--color-muted-foreground)' }}>
            <GitUrlLink 
              url={repository.gitUrl}
              showIcon={true}
              copyable={true}
              className="font-mono text-sm"
            />
          </div>
          <div className="flex items-center" style={{ color: 'var(--color-muted-foreground)' }}>
            <Users className="h-4 w-4 mr-2" />
            <span className="text-sm">{repository.teamName} ({repository.teamCode})</span>
          </div>
        </div>
        
        <div className={`px-4 py-2 rounded-lg ${getCoverageBgColor(repository.coverageRate)}`}>
          <div className="flex items-center">
            <Target className="h-5 w-5 mr-2" />
            <span className={`text-lg font-bold ${getCoverageColor(repository.coverageRate)}`}>
              {repository.coverageRate.toFixed(1)}%
            </span>
          </div>
          <div className="text-sm" style={{ color: 'var(--color-muted-foreground)' }}>Coverage Rate</div>
        </div>
      </div>
    </div>
  );
};

export default RepositoryHeader;

