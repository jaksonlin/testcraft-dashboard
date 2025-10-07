import React from 'react';
import { ChevronDown, ChevronRight, FileText } from 'lucide-react';
import { type ClassGroup } from '../../lib/api';
import VirtualMethodList from './VirtualMethodList';

interface ClassCardProps {
  classGroup: ClassGroup;
  classKey: string;
  isExpanded: boolean;
  onToggle: () => void;
}

const ClassCard: React.FC<ClassCardProps> = ({
  classGroup,
  isExpanded,
  onToggle,
}) => {
  const formatCoverageRate = (rate: number) => `${rate.toFixed(1)}%`;

  return (
    <div className="p-4">
      {/* Class Header */}
      <div 
        className="flex items-center justify-between p-3 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700 rounded-lg"
        onClick={onToggle}
      >
        <div className="flex items-center">
          {isExpanded ? (
            <ChevronDown className="h-4 w-4 text-gray-400 mr-2" />
          ) : (
            <ChevronRight className="h-4 w-4 text-gray-400 mr-2" />
          )}
          <FileText className="h-5 w-5 text-green-600 dark:text-green-400 mr-2" />
          <div>
            <h4 className="font-medium text-gray-900 dark:text-gray-100">
              {classGroup.className}
            </h4>
            <p className="text-sm text-gray-600 dark:text-gray-400">
              {classGroup.repository} • {classGroup.summary.totalMethods} methods
            </p>
          </div>
        </div>
        <div className="text-right">
          <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
            {formatCoverageRate(classGroup.summary.coverageRate)}
          </p>
          <p className="text-xs text-gray-600 dark:text-gray-400">
            {classGroup.summary.annotatedMethods}/{classGroup.summary.totalMethods} annotated
          </p>
        </div>
      </div>

      {/* Class Methods */}
      {isExpanded && (
        <div className="mt-3 ml-6">
          <VirtualMethodList 
            methods={classGroup.methods} 
            maxVisible={8}
          />
        </div>
      )}
    </div>
  );
};

export default ClassCard;

