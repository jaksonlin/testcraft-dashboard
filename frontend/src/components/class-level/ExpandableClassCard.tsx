import React from 'react';
import { ChevronDown, ChevronRight, Download } from 'lucide-react';
import ClassMethodsTable from './ClassMethodsTable';

interface ClassGroup {
  className: string;
  methods: any[];
  methodCount: number;
  annotatedCount: number;
  coverageRate: number;
}

interface ExpandableClassCardProps {
  classGroup: ClassGroup;
  isExpanded: boolean;
  onToggle: () => void;
  onExport: () => void;
}

const getCoverageColor = (coverage: number) => {
  if (coverage >= 80) return 'text-green-600 dark:text-green-400';
  if (coverage >= 50) return 'text-orange-600 dark:text-orange-400';
  return 'text-red-600 dark:text-red-400';
};

const getCoverageBgColor = (coverage: number) => {
  if (coverage >= 80) return 'bg-green-100 dark:bg-green-900/20';
  if (coverage >= 50) return 'bg-orange-100 dark:bg-orange-900/20';
  return 'bg-red-100 dark:bg-red-900/20';
};

const ExpandableClassCard: React.FC<ExpandableClassCardProps> = ({
  classGroup,
  isExpanded,
  onToggle,
  onExport,
}) => {
  return (
    <div className="card">
      {/* Class Header */}
      <div 
        className="flex items-center justify-between p-4 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-800 rounded-lg"
        onClick={onToggle}
      >
        <div className="flex items-center flex-1">
          {isExpanded ? (
            <ChevronDown className="h-5 w-5 text-gray-400 dark:text-gray-500 mr-3" />
          ) : (
            <ChevronRight className="h-5 w-5 text-gray-400 dark:text-gray-500 mr-3" />
          )}
          
          <div className="flex-1">
            <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">{classGroup.className}</h3>
            <div className="flex items-center mt-1 space-x-4 text-sm text-gray-600 dark:text-gray-400">
              <span>{classGroup.methodCount} methods</span>
              <span>{classGroup.annotatedCount} annotated</span>
              <span className={`font-medium ${getCoverageColor(classGroup.coverageRate ?? 0)}`}>
                {(classGroup.coverageRate ?? 0).toFixed(1)}% coverage
              </span>
            </div>
          </div>
        </div>
        
        <div className="flex items-center space-x-2">
          <div className={`px-3 py-1 rounded-full text-sm font-medium ${getCoverageBgColor(classGroup.coverageRate ?? 0)}`}>
            <span className={getCoverageColor(classGroup.coverageRate ?? 0)}>
              {(classGroup.coverageRate ?? 0).toFixed(1)}%
            </span>
          </div>
          
          <button
            onClick={(e) => {
              e.stopPropagation();
              onExport();
            }}
            className="p-2 text-gray-400 dark:text-gray-500 hover:text-gray-600 dark:hover:text-gray-300"
            title="Export class data"
          >
            <Download className="h-4 w-4" />
          </button>
        </div>
      </div>

      {/* Class Methods (Expanded) */}
      {isExpanded && <ClassMethodsTable methods={classGroup.methods} />}
    </div>
  );
};

export default ExpandableClassCard;

