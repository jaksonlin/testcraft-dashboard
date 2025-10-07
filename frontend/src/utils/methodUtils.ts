/**
 * Utility functions for consistent test method handling across the application
 */

import type { TestMethodDetail } from '../lib/api';

/**
 * Determines if a test method is annotated based on consistent criteria
 * A method is considered annotated if it has a non-empty title
 */
export const isMethodAnnotated = (method: TestMethodDetail): boolean => {
  return !!(method.title && method.title.trim() !== '');
};

/**
 * Gets the annotation status of a test method
 */
export const getMethodAnnotationStatus = (method: TestMethodDetail): 'annotated' | 'not-annotated' => {
  return isMethodAnnotated(method) ? 'annotated' : 'not-annotated';
};

/**
 * Calculates coverage rate for a list of methods
 */
export const calculateCoverageRate = (methods: TestMethodDetail[]): number => {
  if (methods.length === 0) return 0;
  const annotatedCount = methods.filter(isMethodAnnotated).length;
  return (annotatedCount / methods.length) * 100;
};

/**
 * Counts annotated methods in a list
 */
export const countAnnotatedMethods = (methods: TestMethodDetail[]): number => {
  return methods.filter(isMethodAnnotated).length;
};

/**
 * Counts non-annotated methods in a list
 */
export const countNonAnnotatedMethods = (methods: TestMethodDetail[]): number => {
  return methods.filter(method => !isMethodAnnotated(method)).length;
};

/**
 * Filters methods by annotation status
 */
export const filterMethodsByAnnotation = (
  methods: TestMethodDetail[], 
  filter: 'all' | 'annotated' | 'not-annotated'
): TestMethodDetail[] => {
  switch (filter) {
    case 'annotated':
      return methods.filter(isMethodAnnotated);
    case 'not-annotated':
      return methods.filter(method => !isMethodAnnotated(method));
    case 'all':
    default:
      return methods;
  }
};

/**
 * Gets a consistent display name for annotation status
 */
export const getAnnotationStatusDisplayName = (method: TestMethodDetail): string => {
  return isMethodAnnotated(method) ? 'Annotated' : 'Not Annotated';
};

/**
 * Gets a consistent color for annotation status
 */
export const getAnnotationStatusColor = (method: TestMethodDetail): string => {
  return isMethodAnnotated(method) ? 'green' : 'red';
};

/**
 * Gets a consistent icon for annotation status
 */
export const getAnnotationStatusIcon = (method: TestMethodDetail): 'check' | 'x' => {
  return isMethodAnnotated(method) ? 'check' : 'x';
};

/**
 * Formats coverage rate for display
 */
export const formatCoverageRate = (rate: number): string => {
  return `${rate.toFixed(1)}%`;
};

/**
 * Gets coverage rate description
 */
export const getCoverageRateDescription = (methods: TestMethodDetail[]): string => {
  const total = methods.length;
  const annotated = countAnnotatedMethods(methods);
  const rate = calculateCoverageRate(methods);
  
  return `${annotated}/${total} annotated (${formatCoverageRate(rate)})`;
};
