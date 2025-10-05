import React from 'react';
import { 
  PieChart,
  Pie,
  Cell,
  Tooltip,
  ResponsiveContainer
} from 'recharts';
import { type DashboardOverview } from '../../lib/api';

interface CoverageChartProps {
  overview: DashboardOverview | null;
}

const CoverageChart: React.FC<CoverageChartProps> = ({ overview }) => {
  const coverageData = [
    { 
      name: 'Annotated Methods', 
      value: overview?.totalAnnotatedMethods || 0, 
      color: '#8b5cf6' 
    },
    { 
      name: 'Unannotated Methods', 
      value: (overview?.totalTestMethods || 0) - (overview?.totalAnnotatedMethods || 0), 
      color: '#e5e7eb' 
    }
  ];

  return (
    <div className="card">
      <div className="card-header">
        <h3 className="text-lg font-semibold text-gray-900">Test Method Coverage</h3>
      </div>
      <div className="h-80">
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie
              data={coverageData}
              cx="50%"
              cy="50%"
              innerRadius={60}
              outerRadius={100}
              paddingAngle={5}
              dataKey="value"
            >
              {coverageData.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={entry.color} />
              ))}
            </Pie>
            <Tooltip />
          </PieChart>
        </ResponsiveContainer>
      </div>
      <div className="mt-4 flex justify-center space-x-6">
        {coverageData.map((item, index) => (
          <div key={index} className="flex items-center">
            <div 
              className="w-3 h-3 rounded-full mr-2" 
              style={{ backgroundColor: item.color }}
            />
            <span className="text-sm text-gray-600">{item.name}</span>
          </div>
        ))}
      </div>
    </div>
  );
};

export default CoverageChart;
