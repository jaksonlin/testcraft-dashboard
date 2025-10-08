/**
 * Complete Step Component - Import results display
 */

import React from 'react';
import { CheckCircle } from 'lucide-react';
import type { CompleteStepProps } from './types';

export const CompleteStep: React.FC<CompleteStepProps> = ({ result, onClose }) => {
  const isSuccess = result.success !== false; // default to success if undefined

  return (
    <div className="space-y-6 text-center">
      <div className="flex justify-center">
        {isSuccess ? (
          <CheckCircle className="w-24 h-24 text-green-600" />
        ) : (
          <svg className="w-24 h-24 text-red-600" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="15" y1="9" x2="9" y2="15"></line>
            <line x1="9" y1="9" x2="15" y2="15"></line>
          </svg>
        )}
      </div>

      {isSuccess ? (
        <SuccessContent result={result} onClose={onClose} />
      ) : (
        <ErrorContent result={result} onClose={onClose} />
      )}
    </div>
  );
};

// Sub-components

const SuccessContent: React.FC<{ result: { imported: number; created?: number; updated?: number; skipped: number; message?: string }; onClose: () => void }> = ({ result, onClose }) => (
  <>
    <div>
      <h2 className="text-3xl font-bold text-gray-900 dark:text-white mb-2">Import Complete!</h2>
      <p className="text-gray-600 dark:text-gray-400 text-lg">
        Successfully processed {result.imported} test cases
      </p>
    </div>

    <div className="grid grid-cols-2 md:grid-cols-3 gap-4 max-w-3xl mx-auto">
      <div className="bg-blue-50 dark:bg-blue-900/20 border-2 border-blue-200 dark:border-blue-800 rounded-xl p-5 text-center">
        <div className="text-4xl font-bold text-blue-600 dark:text-blue-400 mb-1">{result.created || 0}</div>
        <div className="text-sm text-gray-700 dark:text-gray-300 font-semibold">Created</div>
        <div className="text-xs text-gray-500 dark:text-gray-400 mt-1">New test cases</div>
      </div>
      <div className="bg-green-50 dark:bg-green-900/20 border-2 border-green-200 dark:border-green-800 rounded-xl p-5 text-center">
        <div className="text-4xl font-bold text-green-600 dark:text-green-400 mb-1">{result.updated || 0}</div>
        <div className="text-sm text-gray-700 dark:text-gray-300 font-semibold">Updated</div>
        <div className="text-xs text-gray-500 dark:text-gray-400 mt-1">Existing test cases</div>
      </div>
      {result.skipped > 0 && (
        <div className="bg-yellow-50 dark:bg-yellow-900/20 border-2 border-yellow-200 dark:border-yellow-800 rounded-xl p-5 text-center">
          <div className="text-4xl font-bold text-yellow-600 dark:text-yellow-400 mb-1">{result.skipped}</div>
          <div className="text-sm text-gray-700 dark:text-gray-300 font-semibold">Skipped</div>
          <div className="text-xs text-gray-500 dark:text-gray-400 mt-1">Invalid entries</div>
        </div>
      )}
    </div>
    
    {result.message && (
      <div className="bg-gray-50 dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg p-4 max-w-2xl mx-auto">
        <p className="text-sm text-gray-700 dark:text-gray-300">{result.message}</p>
      </div>
    )}

    <div className="bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg p-4 text-left max-w-md mx-auto">
      <p className="font-semibold text-blue-900 dark:text-blue-100 mb-2">What's next?</p>
      <ul className="list-disc list-inside space-y-1 text-sm text-blue-800 dark:text-blue-200">
        <li>View test cases in the Test Cases section</li>
        <li>Check coverage statistics</li>
        <li>Link test methods using @TestCaseId annotation</li>
        <li>Analyze gaps (which test cases need automation)</li>
      </ul>
    </div>

    <div className="pt-4">
      <button
        onClick={onClose}
        className="px-8 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-semibold"
      >
        Done
      </button>
    </div>
  </>
);

const ErrorContent: React.FC<{ result: { message?: string; errors?: unknown[]; suggestions?: unknown[] }; onClose: () => void }> = ({ result, onClose }) => (
  <>
    <div>
      <h2 className="text-3xl font-bold text-gray-900 dark:text-white mb-2">Import Failed</h2>
      <p className="text-gray-600 dark:text-gray-400 text-lg">Please fix the issues below and try again.</p>
    </div>

    {result.message && (
      <div className="max-w-2xl mx-auto text-left bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg p-4">
        <p className="font-semibold text-red-900 dark:text-red-100 mb-1">Error</p>
        <p className="text-sm text-red-800 dark:text-red-200 break-words">{result.message}</p>
      </div>
    )}

    {Array.isArray(result.errors) && result.errors.length > 0 && (
      <div className="max-w-2xl mx-auto text-left bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg p-4">
        <p className="font-semibold text-red-900 dark:text-red-100 mb-2">Row errors</p>
        <ul className="list-disc list-inside space-y-1 text-sm text-red-800 dark:text-red-200">
          {result.errors.map((e: unknown, i: number) => (
            <li key={i}>{String(e)}</li>
          ))}
        </ul>
      </div>
    )}

    {Array.isArray(result.suggestions) && result.suggestions.length > 0 && (
      <div className="max-w-2xl mx-auto text-left bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800 rounded-lg p-4">
        <p className="font-semibold text-yellow-900 dark:text-yellow-100 mb-2">Suggestions</p>
        <ul className="list-disc list-inside space-y-1 text-sm text-yellow-800 dark:text-yellow-200">
          {result.suggestions.map((s: unknown, i: number) => (
            <li key={i}>{String(s)}</li>
          ))}
        </ul>
      </div>
    )}

    <div className="pt-2">
      <button
        onClick={onClose}
        className="px-8 py-3 bg-gray-800 dark:bg-gray-700 text-white rounded-lg hover:bg-gray-900 dark:hover:bg-gray-600 transition-colors font-semibold"
      >
        Close
      </button>
    </div>
  </>
);
