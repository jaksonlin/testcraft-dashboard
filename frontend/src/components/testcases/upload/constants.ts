/**
 * Constants for Test Case Upload Wizard
 */

import type { SystemField } from './types';

export const SYSTEM_FIELDS: SystemField[] = [
  { value: 'ignore', label: '-- Ignore --', required: false },
  { value: 'id', label: 'ID', required: true },
  { value: 'title', label: 'Title', required: true },
  { value: 'steps', label: 'Steps', required: true },
  { value: 'setup', label: 'Setup/Precondition', required: false },
  { value: 'teardown', label: 'Teardown/Postcondition', required: false },
  { value: 'expected_result', label: 'Expected Result', required: false },
  { value: 'priority', label: 'Priority', required: false },
  { value: 'type', label: 'Type', required: false },
  { value: 'status', label: 'Status', required: false },
  { value: 'team', label: 'Team', required: false },
];

export const REQUIRED_FIELDS = SYSTEM_FIELDS
  .filter(field => field.required)
  .map(field => field.value);

export const WIZARD_STEPS: Array<{ key: WizardStep; label: string; order: number }> = [
  { key: 'upload', label: 'Upload', order: 1 },
  { key: 'mapping', label: 'Map Columns', order: 2 },
  { key: 'preview', label: 'Preview', order: 3 },
  { key: 'complete', label: 'Complete', order: 4 },
];

export const CONFIDENCE_THRESHOLDS = {
  HIGH: 90,
  MEDIUM: 70,
  LOW: 0,
} as const;

export const PREVIEW_LIMITS = {
  COLUMNS_TO_SHOW: 8,
  DATA_ROWS_TO_SHOW: 9,
  PREVIEW_ROWS_IN_MAPPING: 4,
} as const;

type WizardStep = 'upload' | 'mapping' | 'preview' | 'complete';
