/**
 * Test Case Upload Module
 * 
 * Exports all components, hooks, types, and utilities for the test case upload wizard.
 */

// Main component
export { TestCaseUploadWizard } from './TestCaseUploadWizard';

// Step components
export { UploadStep } from './UploadStep';
export { MappingStep } from './MappingStep';
export { PreviewStep } from './PreviewStep';
export { CompleteStep } from './CompleteStep';
export { ProgressSteps } from './ProgressSteps';

// Hook
export { useTestCaseUpload } from './useTestCaseUpload';

// Types
export type {
  WizardStep,
  TestCaseUploadWizardProps,
  SystemField,
  UploadStepProps,
  MappingStepProps,
  PreviewStepProps,
  CompleteStepProps,
  ProgressStepsProps,
  ExcelPreviewResponse,
  ImportResponse,
} from './types';

// Constants
export {
  SYSTEM_FIELDS,
  REQUIRED_FIELDS,
  WIZARD_STEPS,
  CONFIDENCE_THRESHOLDS,
  PREVIEW_LIMITS,
} from './constants';

// Utilities
export {
  getConfidenceColor,
  isStepCompleted,
  isStepCurrent,
  getStepButtonClass,
  getProgressBarClass,
  calculateEstimatedImportCount,
} from './utils';
