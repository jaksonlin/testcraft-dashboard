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
  iconColor = 'text-blue-600',
  valueColor = 'text-gray-900',
  action,
}) => {
  return (
    <div className="bg-white rounded-lg shadow p-6">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-semibold text-gray-900">{title}</h3>
        <Icon className={`w-5 h-5 ${iconColor}`} />
      </div>
      <div className={`text-4xl font-bold ${valueColor}`}>{value}</div>
      <p className="text-sm text-gray-600 mt-2">{description}</p>
      {action && (
        <button
          onClick={action.onClick}
          className="mt-3 text-sm text-orange-700 hover:text-orange-900 underline"
        >
          {action.label}
        </button>
      )}
    </div>
  );
};
