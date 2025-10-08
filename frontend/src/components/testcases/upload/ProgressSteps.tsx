/**
 * Progress Steps Component - Visual step indicator
 */

import React from 'react';
import { CheckCircle } from 'lucide-react';
import type { ProgressStepsProps } from './types';
import { getStepButtonClass, getProgressBarClass, isStepCompleted } from './utils';

export const ProgressSteps: React.FC<ProgressStepsProps> = ({ currentStep }) => {
  const steps = [
    { key: 'upload' as const, label: 'Upload', number: 1 },
    { key: 'mapping' as const, label: 'Map Columns', number: 2 },
    { key: 'preview' as const, label: 'Preview', number: 3 },
    { key: 'complete' as const, label: 'Complete', number: 4 },
  ];

  return (
    <div className="mb-8">
      <div className="flex items-center justify-between">
        {steps.map((step, index) => (
          <React.Fragment key={step.key}>
            {/* Step Circle */}
            <div className="flex items-center">
              <div className={`flex items-center justify-center w-10 h-10 rounded-full ${getStepButtonClass(step.key, currentStep)}`}>
                {isStepCompleted(step.key, currentStep) ? 
                  <CheckCircle className="w-5 h-5" /> : step.number}
              </div>
              <span className="ml-2 font-medium">{step.label}</span>
            </div>

            {/* Progress Bar (not after last step) */}
            {index < steps.length - 1 && (
              <div className="flex-1 h-1 mx-4 bg-gray-300">
                <div className={`h-full ${getProgressBarClass(step.key, currentStep)}`} />
              </div>
            )}
          </React.Fragment>
        ))}
      </div>
    </div>
  );
};
