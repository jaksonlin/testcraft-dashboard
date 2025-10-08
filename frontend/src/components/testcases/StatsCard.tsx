import React from 'react';

interface StatsCardProps {
  title: string;
  value: number;
  description: string;
  icon: React.ComponentType<{ className?: string }>;
  iconColor?: string;
  valueColor?: string;
  action?: {
    label: string;
    onClick: () => void;
  };
}

/**
 * Reusable statistics card component
 */
export const StatsCard: React.FC<StatsCardProps> = ({
  title,
  value,
  description,
  icon: Icon,
  iconColor = 'text-blue-600 dark:text-blue-400',
  valueColor = 'text-gray-900 dark:text-white',
  action,
}) => {
  return (
    <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-semibold text-gray-900 dark:text-white">{title}</h3>
        <Icon className={`w-5 h-5 ${iconColor}`} />
      </div>
      <div className={`text-4xl font-bold ${valueColor}`}>{value}</div>
      <p className="text-sm text-gray-600 dark:text-gray-400 mt-2">{description}</p>
      {action && (
        <button
          onClick={action.onClick}
          className="mt-3 text-sm text-orange-700 dark:text-orange-400 hover:text-orange-900 dark:hover:text-orange-300 underline"
        >
          {action.label}
        </button>
      )}
    </div>
  );
};
