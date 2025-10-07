import React from 'react';
import { type TestMethodDetail } from '../../lib/api';
import MethodsTable from './MethodsTable';

interface MethodsTabProps {
  methods: TestMethodDetail[];
  selectedClassName: string;
}

const MethodsTab: React.FC<MethodsTabProps> = ({ methods, selectedClassName }) => {
  return (
    <div className="card">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-semibold" style={{ color: 'var(--color-foreground)' }}>
          Methods {selectedClassName ? `for ${selectedClassName}` : ''}
        </h3>
        <span className="text-sm" style={{ color: 'var(--color-muted-foreground)' }}>
          {methods.length} methods
        </span>
      </div>
      <MethodsTable methods={methods} className={selectedClassName} />
    </div>
  );
};

export default MethodsTab;

