/**
 * Test Case Upload Wizard - Main Component
 * 
 * Multi-step wizard for uploading and importing test cases from Excel files.
 * 
 * Steps:
 * 1. Upload - File selection and drag & drop
 * 2. Mapping - Review and adjust column mappings
 * 3. Preview - Preview data before import
 * 4. Complete - Import results and next steps
 */

import React from 'react';
import type { TestCaseUploadWizardProps } from './types';
import { useTestCaseUpload } from './useTestCaseUpload';
import { ProgressSteps } from './ProgressSteps';
import { UploadStep } from './UploadStep';
import { MappingStep } from './MappingStep';
import { PreviewStep } from './PreviewStep';
import { CompleteStep } from './CompleteStep';

export const TestCaseUploadWizard: React.FC<TestCaseUploadWizardProps> = ({ onComplete }) => {
  const {
    currentStep,
    preview,
    mappings,
    headerRow,
    dataStartRow,
    isValidMapping,
    missingFields,
    suggestions,
    importing,
    importResult,
    setCurrentStep,
    handleFileSelect,
    handleMappingChange,
    handleRowChange,
    handleAdvanceToPreview,
    handleImport,
    handleComplete,
  } = useTestCaseUpload(onComplete);

  return (
    <div className="bg-white dark:bg-gray-800 rounded-lg shadow-lg p-6 w-[98vw] max-w-[1800px] mx-auto">
      {/* Progress Steps */}
      <ProgressSteps currentStep={currentStep} />

      {/* Step Content */}
      <div className="min-h-96">
        {currentStep === 'upload' && (
          <UploadStep onFileSelect={handleFileSelect} />
        )}

        {currentStep === 'mapping' && preview && (
          <MappingStep
            preview={preview}
            mappings={mappings}
            headerRow={headerRow}
            dataStartRow={dataStartRow}
            isValid={isValidMapping}
            missingFields={missingFields}
            suggestions={suggestions}
            onMappingChange={handleMappingChange}
            onHeaderRowChange={(row) => handleRowChange(row, dataStartRow)}
            onDataStartRowChange={(row) => handleRowChange(headerRow, row)}
            onNext={handleAdvanceToPreview}
            onBack={() => setCurrentStep('upload')}
          />
        )}

        {currentStep === 'preview' && preview && (
          <PreviewStep
            preview={preview}
            mappings={mappings}
            headerRow={headerRow}
            dataStartRow={dataStartRow}
            importing={importing}
            onImport={handleImport}
            onBack={() => setCurrentStep('mapping')}
          />
        )}

        {currentStep === 'complete' && importResult && (
          <CompleteStep
            result={importResult}
            onClose={handleComplete}
          />
        )}
      </div>
    </div>
  );
};
