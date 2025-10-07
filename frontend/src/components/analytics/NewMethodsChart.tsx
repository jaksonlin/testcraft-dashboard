import React from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

interface NewMethodsChartProps {
  data: any[];
}

const NewMethodsChart: React.FC<NewMethodsChartProps> = ({ data }) => {
  return (
    <div className="rounded-lg shadow-sm border p-6" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)' }}>
      <h3 className="text-lg font-semibold mb-4" style={{ color: 'var(--color-foreground)' }}>New Methods Added Daily</h3>
      <ResponsiveContainer width="100%" height={400}>
        <BarChart data={data}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="date" />
          <YAxis />
          <Tooltip />
          <Legend />
          <Bar dataKey="newTestMethods" fill="#3b82f6" name="New Test Methods" />
          <Bar dataKey="newAnnotatedMethods" fill="#8b5cf6" name="New Annotated Methods" />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
};

export default NewMethodsChart;

