import React from 'react';
import { X } from 'lucide-react';
import { TestCaseUploadWizard } from './TestCaseUploadWizard';

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
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl max-w-5xl w-full max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-gray-200 sticky top-0 bg-white z-10">
          <div>
            <h2 className="text-2xl font-bold text-gray-900">Upload Test Cases</h2>
            <p className="text-gray-600">Import test cases from Excel file</p>
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors"
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

