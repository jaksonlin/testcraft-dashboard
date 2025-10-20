import React from 'react';
import { BarChart3 } from 'lucide-react';
import { PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer } from 'recharts';

interface DistributionData {
  name: string;
  value: number;
  color: string;
  [key: string]: any;
}

interface CoverageDistributionChartProps {
  data: DistributionData[];
  height?: number;
}

const CoverageDistributionChart: React.FC<CoverageDistributionChartProps> = ({ 
  data, 
  height = 300 
}) => {
  return (
    <div className="rounded-lg shadow-sm border p-6" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)' }}>
      <h3 className="text-lg font-semibold mb-4" style={{ color: 'var(--color-foreground)' }}>Team Coverage Distribution</h3>
      {data.length > 0 ? (
        <ResponsiveContainer width="100%" height={height}>
          <PieChart>
            <Pie
              data={data}
              cx="50%"
              cy="50%"
              labelLine={false}
              label={({ name, percent }) => `${name}: ${((percent as number) * 100).toFixed(0)}%`}
              outerRadius={80}
              fill="#8884d8"
              dataKey="value"
            >
              {data.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={entry.color} />
              ))}
            </Pie>
            <Tooltip 
              formatter={(value: number, name: string) => [`${value} teams`, name]}
            />
            <Legend />
          </PieChart>
        </ResponsiveContainer>
      ) : (
        <div className="flex items-center justify-center h-64" style={{ color: 'var(--color-muted-foreground)' }}>
          <div className="text-center">
            <BarChart3 className="h-12 w-12 mx-auto mb-4" style={{ color: 'var(--color-muted-foreground)' }} />
            <p>No team coverage data available</p>
          </div>
        </div>
      )}
    </div>
  );
};

export default CoverageDistributionChart;

