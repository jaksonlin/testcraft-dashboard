import React, { useState } from 'react';
import { CheckCircle, XCircle } from 'lucide-react';
import { type TestMethodDetail } from '../../lib/api';
import { isMethodAnnotated } from '../../utils/methodUtils';

interface VirtualMethodListProps {
  methods: TestMethodDetail[];
  maxVisible?: number;
}

const VirtualMethodList: React.FC<VirtualMethodListProps> = ({ methods, maxVisible = 10 }) => {
  const [showAll, setShowAll] = useState(false);
  const visibleMethods = showAll ? methods : methods.slice(0, maxVisible);
  const hasMore = methods.length > maxVisible;

  return (
    <div className="space-y-2">
      {visibleMethods.map((method) => {
        const isAnnotated = isMethodAnnotated(method);
        return (
          <div 
            key={method.id} 
            className="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-800 rounded-lg"
          >
            <div className="flex items-center">
              {isAnnotated ? (
                <CheckCircle className="h-4 w-4 text-green-600 dark:text-green-400 mr-2" />
              ) : (
                <XCircle className="h-4 w-4 text-red-600 dark:text-red-400 mr-2" />
              )}
              <div>
                <p className="font-medium text-gray-900 dark:text-gray-100">
                  {method.testMethod}
                </p>
                {method.title && method.title !== 'No title' && (
                  <p className="text-sm text-gray-600 dark:text-gray-400">
                    {method.title}
                  </p>
                )}
              </div>
            </div>
            <div className="text-right">
              <p className="text-xs text-gray-600 dark:text-gray-400">
                {method.author || 'Unknown'}
              </p>
              <p className="text-xs text-gray-500 dark:text-gray-500">
                {method.status || 'UNKNOWN'}
              </p>
            </div>
          </div>
        );
      })}
      {hasMore && !showAll && (
        <button
          onClick={() => setShowAll(true)}
          className="w-full p-2 text-sm text-blue-600 dark:text-blue-400 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg transition-colors"
        >
          Show {methods.length - maxVisible} more methods...
        </button>
      )}
      {showAll && hasMore && (
        <button
          onClick={() => setShowAll(false)}
          className="w-full p-2 text-sm text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg transition-colors"
        >
          Show less
        </button>
      )}
    </div>
  );
};

export default VirtualMethodList;

