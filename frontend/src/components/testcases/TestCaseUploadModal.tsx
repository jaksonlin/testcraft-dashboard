import React from 'react';
import { X } from 'lucide-react';
import { TestCaseUploadWizard } from './upload';

interface TestCaseUploadModalProps {
  isOpen: boolean;
  onClose: () => void;
  onComplete: () => void;
}

/**
 * Modal wrapper for TestCaseUploadWizard
 */
export const TestCaseUploadModal: React.FC<TestCaseUploadModalProps> = ({ 
  isOpen, 
  onClose, 
  onComplete 
}) => {
  if (!isOpen) return null;

  const handleComplete = () => {
    onComplete();
    onClose();
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex z-50">
      <div className="bg-white dark:bg-gray-900 w-full h-screen overflow-y-auto">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-gray-200 dark:border-gray-700 sticky top-0 bg-white dark:bg-gray-900 z-10">
          <div>
            <h2 className="text-2xl font-bold text-gray-900 dark:text-white">Upload Test Cases</h2>
            <p className="text-gray-600 dark:text-gray-400">Import test cases from Excel file</p>
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 dark:text-gray-500 hover:text-gray-600 dark:hover:text-gray-300 transition-colors"
            aria-label="Close"
          >
            <X className="w-6 h-6" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6">
          <TestCaseUploadWizard onComplete={handleComplete} />
        </div>
      </div>
    </div>
  );
};

