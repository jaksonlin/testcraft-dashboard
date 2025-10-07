import React from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

interface TestMethodsGrowthChartProps {
  data: any[];
}

const TestMethodsGrowthChart: React.FC<TestMethodsGrowthChartProps> = ({ data }) => {
  return (
    <div className="rounded-lg shadow-sm border p-6" style={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)' }}>
      <h3 className="text-lg font-semibold mb-4" style={{ color: 'var(--color-foreground)' }}>Test Methods Growth</h3>
      <ResponsiveContainer width="100%" height={400}>
        <LineChart data={data}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="date" />
          <YAxis />
          <Tooltip />
          <Legend />
          <Line 
            type="monotone" 
            dataKey="testMethods" 
            stroke="#10b981" 
            strokeWidth={2}
            name="Total Test Methods"
          />
          <Line 
            type="monotone" 
            dataKey="annotatedMethods" 
            stroke="#8b5cf6" 
            strokeWidth={2}
            name="Annotated Methods"
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
};

export default TestMethodsGrowthChart;

