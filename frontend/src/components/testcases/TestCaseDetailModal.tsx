import React from 'react';
import { X, CheckCircle, Tag, FileText } from 'lucide-react';
import type { TestCase } from '../../lib/testCaseApi';

interface TestCaseDetailModalProps {
  testCase: TestCase;
  onClose: () => void;
}

/**
 * Modal to show detailed test case information
 */
export const TestCaseDetailModal: React.FC<TestCaseDetailModalProps> = ({ testCase, onClose }) => {
  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-xl max-w-4xl w-full max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-gray-200 dark:border-gray-700 sticky top-0 bg-white dark:bg-gray-800">
          <div>
            <h2 className="text-2xl font-bold text-gray-900 dark:text-white">{testCase.externalId}</h2>
            <p className="text-gray-600 dark:text-gray-400">{testCase.title}</p>
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 dark:text-gray-500 hover:text-gray-600 dark:hover:text-gray-300 transition-colors"
          >
            <X className="w-6 h-6" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 space-y-6">
          {/* Metadata */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {testCase.priority && (
              <div>
                <label className="text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase">Priority</label>
                <p className="text-sm font-medium text-gray-900 dark:text-white">{testCase.priority}</p>
              </div>
            )}
            {testCase.type && (
              <div>
                <label className="text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase">Type</label>
                <p className="text-sm font-medium text-gray-900 dark:text-white">{testCase.type}</p>
              </div>
            )}
            {testCase.status && (
              <div>
                <label className="text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase">Status</label>
                <div className="flex items-center gap-2">
                  <CheckCircle className="w-4 h-4 text-green-600 dark:text-green-400" />
                  <span className="text-sm font-medium text-gray-900 dark:text-white">{testCase.status}</span>
                </div>
              </div>
            )}
          </div>

          {/* Setup/Precondition */}
          {testCase.setup && (
            <div className="bg-blue-50 dark:bg-blue-900/20 rounded-lg p-4">
              <h4 className="font-semibold text-gray-900 dark:text-white mb-2 flex items-center gap-2">
                <FileText className="w-4 h-4 text-blue-600 dark:text-blue-400" />
                Setup / Precondition
              </h4>
              <p className="text-sm text-gray-700 dark:text-gray-300 whitespace-pre-wrap">{testCase.setup}</p>
            </div>
          )}

          {/* Test Steps */}
          <div className="bg-gray-50 dark:bg-gray-900 rounded-lg p-4">
            <h4 className="font-semibold text-gray-900 dark:text-white mb-2">Test Steps</h4>
            <div className="text-sm text-gray-700 dark:text-gray-300 whitespace-pre-wrap">{testCase.steps}</div>
          </div>

          {/* Expected Result */}
          {testCase.expectedResult && (
            <div className="bg-green-50 dark:bg-green-900/20 rounded-lg p-4">
              <h4 className="font-semibold text-gray-900 dark:text-white mb-2 flex items-center gap-2">
                <CheckCircle className="w-4 h-4 text-green-600 dark:text-green-400" />
                Expected Result
              </h4>
              <p className="text-sm text-gray-700 dark:text-gray-300 whitespace-pre-wrap">{testCase.expectedResult}</p>
            </div>
          )}

          {/* Teardown/Postcondition */}
          {testCase.teardown && (
            <div className="bg-purple-50 dark:bg-purple-900/20 rounded-lg p-4">
              <h4 className="font-semibold text-gray-900 dark:text-white mb-2">Teardown / Postcondition</h4>
              <p className="text-sm text-gray-700 dark:text-gray-300 whitespace-pre-wrap">{testCase.teardown}</p>
            </div>
          )}

          {/* Tags */}
          {testCase.tags && testCase.tags.length > 0 && (
            <div>
              <h4 className="font-semibold text-gray-900 dark:text-white mb-2 flex items-center gap-2">
                <Tag className="w-4 h-4 text-gray-600 dark:text-gray-400" />
                Tags
              </h4>
              <div className="flex flex-wrap gap-2">
                {testCase.tags.map((tag, idx) => (
                  <span
                    key={idx}
                    className="px-2 py-1 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 text-xs rounded border border-gray-300 dark:border-gray-600"
                  >
                    {tag}
                  </span>
                ))}
              </div>
            </div>
          )}

          {/* Requirements */}
          {testCase.requirements && testCase.requirements.length > 0 && (
            <div>
              <h4 className="font-semibold text-gray-900 dark:text-white mb-2">Linked Requirements</h4>
              <div className="flex flex-wrap gap-2">
                {testCase.requirements.map((req, idx) => (
                  <span
                    key={idx}
                    className="px-2 py-1 bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-300 text-xs font-mono rounded border border-blue-300 dark:border-blue-700"
                  >
                    {req}
                  </span>
                ))}
              </div>
            </div>
          )}

          {/* Custom Fields */}
          {testCase.customFields && Object.keys(testCase.customFields).length > 0 && (
            <div>
              <h4 className="font-semibold text-gray-900 dark:text-white mb-2">Additional Fields</h4>
              <div className="bg-gray-50 dark:bg-gray-900 rounded-lg p-4 space-y-2">
                {Object.entries(testCase.customFields).map(([key, value]) => (
                  <div key={key} className="flex items-start gap-4">
                    <span className="text-sm font-medium text-gray-600 dark:text-gray-400 w-32">{key}:</span>
                    <span className="text-sm text-gray-900 dark:text-white flex-1">{String(value)}</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="p-6 border-t border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-900 flex justify-end sticky bottom-0">
          <button
            onClick={onClose}
            className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
};

