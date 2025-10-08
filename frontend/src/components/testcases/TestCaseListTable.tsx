import React, { useState } from 'react';
import { CheckCircle, XCircle, Eye, Trash2, Filter } from 'lucide-react';
import type { TestCase } from '../../lib/testCaseApi';

interface TestCaseListTableProps {
  testCases: TestCase[];
  onViewDetails?: (testCase: TestCase) => void;
  onDelete?: (internalId: number) => void;
}

export const TestCaseListTable: React.FC<TestCaseListTableProps> = ({
  testCases,
  onViewDetails,
  onDelete
}) => {
  const [filter, setFilter] = useState({
    priority: '',
    type: '',
    status: '',
    search: ''
  });

  // Filter test cases
  const filteredTestCases = testCases.filter(tc => {
    if (filter.priority && tc.priority !== filter.priority) return false;
    if (filter.type && tc.type !== filter.type) return false;
    if (filter.status && tc.status !== filter.status) return false;
    if (filter.search) {
      const searchLower = filter.search.toLowerCase();
      return tc.externalId.toLowerCase().includes(searchLower) ||
             tc.title.toLowerCase().includes(searchLower);
    }
    return true;
  });

  // Extract unique values for filters
  const priorities = [...new Set(testCases.map(tc => tc.priority).filter(Boolean))];
  const types = [...new Set(testCases.map(tc => tc.type).filter(Boolean))];
  const statuses = [...new Set(testCases.map(tc => tc.status).filter(Boolean))];

  const getPriorityBadgeColor = (priority?: string) => {
    switch (priority?.toLowerCase()) {
      case 'high':
      case 'critical':
        return 'bg-red-100 text-red-800 border-red-200';
      case 'medium':
        return 'bg-yellow-100 text-yellow-800 border-yellow-200';
      case 'low':
        return 'bg-green-100 text-green-800 border-green-200';
      default:
        return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  const getStatusIcon = (status?: string) => {
    if (status?.toLowerCase() === 'active') {
      return <CheckCircle className="w-4 h-4 text-green-600" />;
    }
    return <XCircle className="w-4 h-4 text-gray-400" />;
  };

  return (
    <div className="space-y-4">
      {/* Filters */}
      <div className="bg-gray-50 dark:bg-gray-800 rounded-lg p-4">
        <div className="flex items-center gap-2 mb-3">
          <Filter className="w-4 h-4 text-gray-600 dark:text-gray-400" />
          <span className="font-semibold text-gray-900 dark:text-white">Filters</span>
        </div>
        
        <div className="grid grid-cols-4 gap-4">
          {/* Search */}
          <input
            type="text"
            placeholder="Search ID or Title..."
            value={filter.search}
            onChange={(e) => setFilter({ ...filter, search: e.target.value })}
            className="px-3 py-2 border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />

          {/* Priority Filter */}
          <select
            value={filter.priority}
            onChange={(e) => setFilter({ ...filter, priority: e.target.value })}
            className="px-3 py-2 border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            <option value="">All Priorities</option>
            {priorities.map(p => (
              <option key={p} value={p}>{p}</option>
            ))}
          </select>

          {/* Type Filter */}
          <select
            value={filter.type}
            onChange={(e) => setFilter({ ...filter, type: e.target.value })}
            className="px-3 py-2 border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            <option value="">All Types</option>
            {types.map(t => (
              <option key={t} value={t}>{t}</option>
            ))}
          </select>

          {/* Status Filter */}
          <select
            value={filter.status}
            onChange={(e) => setFilter({ ...filter, status: e.target.value })}
            className="px-3 py-2 border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            <option value="">All Statuses</option>
            {statuses.map(s => (
              <option key={s} value={s}>{s}</option>
            ))}
          </select>
        </div>
      </div>

      {/* Results Count */}
      <div className="text-sm text-gray-600 dark:text-gray-400">
        Showing {filteredTestCases.length} of {testCases.length} test cases
      </div>

      {/* Table */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
          <thead className="bg-gray-50 dark:bg-gray-900">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                ID
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                Title
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                Priority
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                Type
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                Status
              </th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                Actions
              </th>
            </tr>
          </thead>
          <tbody className="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
            {filteredTestCases.map(tc => (
              <tr key={tc.id} className="hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors">
                <td className="px-6 py-4 whitespace-nowrap">
                  <span className="font-mono text-sm font-semibold text-gray-900 dark:text-white">
                    {tc.id}
                  </span>
                </td>
                <td className="px-6 py-4">
                  <div className="text-sm font-medium text-gray-900 dark:text-white">
                    {tc.title}
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  {tc.priority && (
                    <span className={`px-2 py-1 text-xs font-semibold rounded border ${getPriorityBadgeColor(tc.priority)}`}>
                      {tc.priority}
                    </span>
                  )}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <span className="text-sm text-gray-600 dark:text-gray-400">{tc.type || '-'}</span>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="flex items-center gap-2">
                    {getStatusIcon(tc.status)}
                    <span className="text-sm text-gray-600 dark:text-gray-400">{tc.status || '-'}</span>
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  <div className="flex items-center justify-end gap-2">
                    {onViewDetails && (
                      <button
                        onClick={() => onViewDetails(tc)}
                        className="text-blue-600 dark:text-blue-400 hover:text-blue-900 dark:hover:text-blue-300 transition-colors"
                        title="View Details"
                      >
                        <Eye className="w-4 h-4" />
                      </button>
                    )}
                    {onDelete && (
                      <button
                        onClick={() => {
                          if (confirm(`Delete test case ${tc.externalId}?`)) {
                            onDelete(tc.internalId);
                          }
                        }}
                        className="text-red-600 dark:text-red-400 hover:text-red-900 dark:hover:text-red-300 transition-colors"
                        title="Delete"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {filteredTestCases.length === 0 && (
          <div className="text-center py-12 text-gray-500 dark:text-gray-400">
            No test cases found matching the filters
          </div>
        )}
      </div>
    </div>
  );
};

