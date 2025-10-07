import React from 'react';
import { ChevronDown, ChevronRight, Users } from 'lucide-react';
import { type TeamGroup } from '../../lib/api';
import ClassCard from './ClassCard';

interface TeamCardProps {
  team: TeamGroup;
  isExpanded: boolean;
  onToggle: () => void;
  expandedClasses: Set<string>;
  onClassToggle: (classKey: string) => void;
}

const TeamCard: React.FC<TeamCardProps> = ({
  team,
  isExpanded,
  onToggle,
  expandedClasses,
  onClassToggle,
}) => {
  const formatCoverageRate = (rate: number) => `${rate.toFixed(1)}%`;

  return (
    <div className="card">
      {/* Team Header */}
      <div 
        className="flex items-center justify-between p-4 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700 rounded-lg"
        onClick={onToggle}
      >
        <div className="flex items-center">
          {isExpanded ? (
            <ChevronDown className="h-5 w-5 text-gray-400 mr-3" />
          ) : (
            <ChevronRight className="h-5 w-5 text-gray-400 mr-3" />
          )}
          <Users className="h-6 w-6 text-blue-600 dark:text-blue-400 mr-3" />
          <div>
            <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">
              {team.teamName}
            </h3>
            <p className="text-sm text-gray-600 dark:text-gray-400">
              {team.teamCode} • {team.summary.totalClasses} classes • {team.summary.totalMethods} methods
            </p>
          </div>
        </div>
        <div className="flex items-center space-x-4">
          <div className="text-right">
            <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
              {formatCoverageRate(team.summary.coverageRate)}
            </p>
            <p className="text-xs text-gray-600 dark:text-gray-400">
              {team.summary.annotatedMethods}/{team.summary.totalMethods} annotated
            </p>
          </div>
        </div>
      </div>

      {/* Team Classes */}
      {isExpanded && (
        <div className="border-t border-gray-200 dark:border-gray-700">
          {team.classes.map((classGroup) => {
            const classKey = `${team.teamName}.${classGroup.repository}.${classGroup.className}`;
            return (
              <ClassCard
                key={classKey}
                classGroup={classGroup}
                classKey={classKey}
                isExpanded={expandedClasses.has(classKey)}
                onToggle={() => onClassToggle(classKey)}
              />
            );
          })}
        </div>
      )}
    </div>
  );
};

export default TeamCard;

