import React from 'react';
import { FileText } from 'lucide-react';
import { type TestClassSummary } from '../../lib/api';

interface ClassesTableProps {
  classes: TestClassSummary[];
  onSelectClass: (cls: TestClassSummary) => void;
}

const ClassesTable: React.FC<ClassesTableProps> = ({ classes, onSelectClass }) => {
  if (classes.length === 0) {
    return (
      <div className="text-center py-12">
        <FileText className="h-12 w-12 text-gray-400 mx-auto mb-4" />
        <h3 className="text-lg font-medium mb-2" style={{ color: 'var(--color-foreground)' }}>No Test Classes Found</h3>
        <p style={{ color: 'var(--color-muted-foreground)' }}>This repository doesn't have any test classes yet.</p>
      </div>
    );
  }

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full divide-y divide-gray-200">
        <thead style={{ backgroundColor: 'var(--color-muted)' }}>
          <tr>
            <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>Class</th>
            <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>Methods</th>
            <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>Annotated</th>
            <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>Coverage</th>
            <th className="px-6 py-3"></th>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-200" style={{ backgroundColor: 'var(--color-background)' }}>
          {classes.map((cls) => (
            <tr key={cls.id} className="hover:bg-gray-50 dark:hover:bg-gray-700" style={{ backgroundColor: 'var(--color-background)' }}>
              <td className="px-6 py-4 whitespace-nowrap text-sm font-medium" style={{ color: 'var(--color-foreground)' }}>{cls.className}</td>
              <td className="px-6 py-4 whitespace-nowrap text-sm" style={{ color: 'var(--color-foreground)' }}>{cls.testMethodCount}</td>
              <td className="px-6 py-4 whitespace-nowrap text-sm" style={{ color: 'var(--color-foreground)' }}>{cls.annotatedMethodCount}</td>
              <td className="px-6 py-4 whitespace-nowrap text-sm" style={{ color: 'var(--color-foreground)' }}>{cls.coverageRate?.toFixed(1)}%</td>
              <td className="px-6 py-4 whitespace-nowrap text-right text-sm">
                <button className="btn btn-secondary" onClick={() => onSelectClass(cls)}>View Methods</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default ClassesTable;

