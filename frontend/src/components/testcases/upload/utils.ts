/**
 * Utility functions for Test Case Upload Wizard
 */

import { CONFIDENCE_THRESHOLDS } from './constants';
import type { WizardStep } from './types';

/**
 * Get color class for confidence level
 */
export const getConfidenceColor = (confidence: number): string => {
  if (confidence >= CONFIDENCE_THRESHOLDS.HIGH) return 'text-green-600 dark:text-green-400';
  if (confidence >= CONFIDENCE_THRESHOLDS.MEDIUM) return 'text-yellow-600 dark:text-yellow-400';
  return 'text-orange-600 dark:text-orange-400';
};

/**
 * Check if a step is completed
 */
export const isStepCompleted = (step: WizardStep, currentStep: WizardStep): boolean => {
  const steps: WizardStep[] = ['upload', 'mapping', 'preview', 'complete'];
  const stepIndex = steps.indexOf(step);
  const currentIndex = steps.indexOf(currentStep);
  return currentIndex > stepIndex;
};

/**
 * Check if a step is current
 */
export const isStepCurrent = (step: WizardStep, currentStep: WizardStep): boolean => {
  return step === currentStep;
};

/**
 * Get step button styling
 */
export const getStepButtonClass = (step: WizardStep, currentStep: WizardStep): string => {
  if (isStepCurrent(step, currentStep)) {
    return 'bg-blue-600 text-white';
  }
  if (isStepCompleted(step, currentStep)) {
    return 'bg-green-600 text-white';
  }
  return 'bg-gray-300 dark:bg-gray-700 text-gray-600 dark:text-gray-400';
};

/**
 * Get progress bar styling
 */
export const getProgressBarClass = (fromStep: WizardStep, currentStep: WizardStep): string => {
  return isStepCompleted(fromStep, currentStep) ? 'bg-green-600' : 'bg-gray-300 dark:bg-gray-700';
};

/**
 * Calculate estimated import count
 */
export const calculateEstimatedImportCount = (totalRows: number, dataStartRow: number): number => {
  return Math.max(0, totalRows - dataStartRow);
};
