/**
 * Type definitions for Test Case Upload Wizard
 */

export type WizardStep = 'upload' | 'mapping' | 'preview' | 'complete';

export interface TestCaseUploadWizardProps {
  onComplete?: () => void;
}

export interface SystemField {
  value: string;
  label: string;
  required: boolean;
}

export interface UploadStepProps {
  onFileSelect: (file: File) => void;
}

export interface MappingStepProps {
  preview: ExcelPreviewResponse;
  mappings: Record<string, string>;
  headerRow: number;
  dataStartRow: number;
  isValid: boolean;
  missingFields: string[];
  suggestions: string[];
  onMappingChange: (excelColumn: string, systemField: string) => void;
  onHeaderRowChange: (row: number) => void;
  onDataStartRowChange: (row: number) => void;
  onNext: () => void;
  onBack: () => void;
}

export interface PreviewStepProps {
  preview: ExcelPreviewResponse;
  mappings: Record<string, string>;
  headerRow: number;
  dataStartRow: number;
  importing: boolean;
  onImport: () => void;
  onBack: () => void;
}

export interface CompleteStepProps {
  result: ImportResponse;
  onClose: () => void;
}

export interface ProgressStepsProps {
  currentStep: WizardStep;
}

// Re-export API types
export type { ExcelPreviewResponse, ImportResponse } from '../../../lib/testCaseApi';
