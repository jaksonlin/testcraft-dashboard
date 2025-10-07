import React from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

interface TeamComparisonChartProps {
  data: any[];
}

const TeamComparisonChart: React.FC<TeamComparisonChartProps> = ({ data }) => {
  return (
    <div className="rounded-lg shadow-sm border p-6" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)' }}>
      <h3 className="text-lg font-semibold mb-4" style={{ color: 'var(--color-foreground)' }}>Team Coverage Comparison</h3>
      <ResponsiveContainer width="100%" height={400}>
        <BarChart data={data} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="name" angle={-45} textAnchor="end" height={100} />
          <YAxis domain={[0, 100]} />
          <Tooltip formatter={(value: number) => [`${value.toFixed(1)}%`, 'Coverage']} />
          <Legend />
          <Bar dataKey="coverage" fill="#3b82f6" name="Coverage %" />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
};

export default TeamComparisonChart;

