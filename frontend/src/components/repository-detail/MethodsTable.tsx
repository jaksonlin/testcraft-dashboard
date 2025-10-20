import React from 'react';
import { FileText, CheckCircle, XCircle } from 'lucide-react';
import { type TestMethodDetail } from '../../lib/api';
import { isMethodAnnotated, getAnnotationStatusDisplayName } from '../../utils/methodUtils';
import { formatDate } from '../../utils/dateUtils';

interface MethodsTableProps {
  methods: TestMethodDetail[];
  className?: string;
}

const MethodsTable: React.FC<MethodsTableProps> = ({ methods }) => {
  if (methods.length === 0) {
    return (
      <div className="text-center py-12">
        <FileText className="h-12 w-12 text-gray-400 mx-auto mb-4" />
        <h3 className="text-lg font-medium mb-2" style={{ color: 'var(--color-foreground)' }}>Select a class first</h3>
        <p style={{ color: 'var(--color-muted-foreground)' }}>Choose a class on the Classes tab to view its methods.</p>
      </div>
    );
  }

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full divide-y divide-gray-200">
        <thead style={{ backgroundColor: 'var(--color-muted)' }}>
          <tr>
            <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>Method</th>
            <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>Annotation</th>
            <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>Test Status</th>
            <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>Author</th>
            <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>Target</th>
            <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>Last Modified</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-200" style={{ backgroundColor: 'var(--color-background)' }}>
          {methods.map((method) => (
            <tr key={method.id} className="hover:bg-gray-50 dark:hover:bg-gray-700" style={{ backgroundColor: 'var(--color-background)' }}>
              <td className="px-6 py-4 whitespace-nowrap text-sm font-medium" style={{ color: 'var(--color-foreground)' }}>{method.testMethod}</td>
              <td className="px-6 py-4 whitespace-nowrap">
                <div className="flex items-center">
                  {isMethodAnnotated(method) ? (
                    <CheckCircle className="h-4 w-4 text-green-600 dark:text-green-400 mr-2" />
                  ) : (
                    <XCircle className="h-4 w-4 text-red-600 dark:text-red-400 mr-2" />
                  )}
                  <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                    isMethodAnnotated(method) ? 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300' : 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-300'
                  }`}>
                    {getAnnotationStatusDisplayName(method)}
                  </span>
                </div>
              </td>
              <td className="px-6 py-4 whitespace-nowrap">
                <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                  method.status === 'PASS' ? 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300' :
                  method.status === 'FAIL' ? 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-300' :
                  'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-300'
                }`}>
                  {method.status || 'UNKNOWN'}
                </span>
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm" style={{ color: 'var(--color-foreground)' }}>{method.author || 'N/A'}</td>
              <td className="px-6 py-4 whitespace-nowrap text-sm" style={{ color: 'var(--color-muted-foreground)' }}>{method.targetClass}.{method.targetMethod}</td>
              <td className="px-6 py-4 whitespace-nowrap text-sm" style={{ color: 'var(--color-muted-foreground)' }}>
                {method.lastModified ? formatDate(method.lastModified).date : 'N/A'}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default MethodsTable;

