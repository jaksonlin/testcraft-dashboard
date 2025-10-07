import { useMemo } from 'react';
import { type TestMethodDetail } from '../lib/api';
import { calculateCoverageRate, countAnnotatedMethods } from '../utils/methodUtils';

export interface ClassGroup {
  className: string;
  methods: TestMethodDetail[];
  methodCount: number;
  annotatedCount: number;
  coverageRate: number;
}

export const useClassGrouping = (methods: TestMethodDetail[]): ClassGroup[] => {
  return useMemo(() => {
    const groups = new Map<string, TestMethodDetail[]>();
    
    methods.forEach(method => {
      const className = method.testClass;
      if (!groups.has(className)) {
        groups.set(className, []);
      }
      groups.get(className)!.push(method);
    });
    
    return Array.from(groups.entries()).map(([className, methods]) => {
      const annotatedCount = countAnnotatedMethods(methods);
      const coverageRate = calculateCoverageRate(methods);
      
      return {
        className,
        methods: methods.sort((a, b) => a.testMethod.localeCompare(b.testMethod)),
        methodCount: methods.length,
        annotatedCount,
        coverageRate
      };
    }).sort((a, b) => a.className.localeCompare(b.className));
  }, [methods]);
};

