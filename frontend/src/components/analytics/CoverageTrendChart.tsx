import React from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

interface CoverageTrendChartProps {
  data: any[];
  height?: number;
  showTitle?: boolean;
}

const CoverageTrendChart: React.FC<CoverageTrendChartProps> = ({ 
  data, 
  height = 300,
  showTitle = true 
}) => {
  return (
    <div className="rounded-lg shadow-sm border p-6" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)' }}>
      {showTitle && (
        <h3 className="text-lg font-semibold mb-4" style={{ color: 'var(--color-foreground)' }}>Coverage Trend</h3>
      )}
      <ResponsiveContainer width="100%" height={height}>
        <LineChart data={data}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="date" />
          <YAxis />
          <Tooltip />
          <Legend />
          <Line 
            type="monotone" 
            dataKey="coverage" 
            stroke="#3b82f6" 
            strokeWidth={2}
            name="Coverage %"
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
};

export default CoverageTrendChart;

