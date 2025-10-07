import React from 'react';
import { FileText, Target, Users } from 'lucide-react';

interface ClassLevelStatsProps {
  totalClasses: number;
  totalMethods: number;
  totalAnnotated: number;
}

const ClassLevelStats: React.FC<ClassLevelStatsProps> = ({
  totalClasses,
  totalMethods,
  totalAnnotated,
}) => {
  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
      <div className="card">
        <div className="flex items-center">
          <FileText className="h-8 w-8 text-blue-600 dark:text-blue-400 mr-3" />
          <div>
            <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Total Classes</p>
            <p className="text-2xl font-bold text-gray-900 dark:text-gray-100">{totalClasses}</p>
          </div>
        </div>
      </div>
      
      <div className="card">
        <div className="flex items-center">
          <Target className="h-8 w-8 text-green-600 dark:text-green-400 mr-3" />
          <div>
            <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Total Methods</p>
            <p className="text-2xl font-bold text-gray-900 dark:text-gray-100">{totalMethods}</p>
          </div>
        </div>
      </div>
      
      <div className="card">
        <div className="flex items-center">
          <Users className="h-8 w-8 text-purple-600 dark:text-purple-400 mr-3" />
          <div>
            <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Annotated Methods</p>
            <p className="text-2xl font-bold text-gray-900 dark:text-gray-100">{totalAnnotated}</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ClassLevelStats;

