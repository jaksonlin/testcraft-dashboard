import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, FileText, Target } from 'lucide-react';

interface ClassLevelHeaderProps {
  repositoryName: string;
  totalClasses: number;
  onExpandAll: () => void;
  onCollapseAll: () => void;
}

const ClassLevelHeader: React.FC<ClassLevelHeaderProps> = ({
  repositoryName,
  totalClasses,
  onExpandAll,
  onCollapseAll,
}) => {
  const navigate = useNavigate();

  return (
    <div className="mb-8">
      <div className="flex items-center justify-between mb-4">
        <button
          onClick={() => navigate('/repositories')}
          className="flex items-center text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-100"
        >
          <ArrowLeft className="h-5 w-5 mr-2" />
          Back to Repositories
        </button>
        
        <div className="flex gap-2">
          <button
            onClick={onExpandAll}
            className="btn btn-secondary"
          >
            Expand All
          </button>
          <button
            onClick={onCollapseAll}
            className="btn btn-secondary"
          >
            Collapse All
          </button>
        </div>
      </div>
      
      <div>
        <h1 className="text-3xl font-bold text-gray-900 dark:text-gray-100 mb-2">Class-Level Analysis</h1>
        <div className="flex items-center text-gray-600 dark:text-gray-400 mb-4">
          <FileText className="h-4 w-4 mr-2" />
          <span className="font-mono text-sm">{repositoryName}</span>
        </div>
        <div className="flex items-center text-gray-600 dark:text-gray-400">
          <Target className="h-4 w-4 mr-2" />
          <span className="text-sm">{totalClasses} test classes found</span>
        </div>
      </div>
    </div>
  );
};

export default ClassLevelHeader;

