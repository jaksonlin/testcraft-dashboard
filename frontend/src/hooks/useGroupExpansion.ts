import { useState, useCallback } from 'react';

interface UseGroupExpansionReturn {
  expandedTeams: Set<string>;
  expandedClasses: Set<string>;
  toggleTeam: (teamName: string) => void;
  toggleClass: (classKey: string) => void;
  setExpandedTeams: React.Dispatch<React.SetStateAction<Set<string>>>;
  setExpandedClasses: React.Dispatch<React.SetStateAction<Set<string>>>;
}

export const useGroupExpansion = (): UseGroupExpansionReturn => {
  const [expandedTeams, setExpandedTeams] = useState<Set<string>>(new Set());
  const [expandedClasses, setExpandedClasses] = useState<Set<string>>(new Set());

  const toggleTeam = useCallback((teamName: string) => {
    setExpandedTeams(prev => {
      const newSet = new Set(prev);
      if (newSet.has(teamName)) {
        newSet.delete(teamName);
      } else {
        newSet.add(teamName);
      }
      return newSet;
    });
  }, []);

  const toggleClass = useCallback((classKey: string) => {
    setExpandedClasses(prev => {
      const newSet = new Set(prev);
      if (newSet.has(classKey)) {
        newSet.delete(classKey);
      } else {
        newSet.add(classKey);
      }
      return newSet;
    });
  }, []);

  return {
    expandedTeams,
    expandedClasses,
    toggleTeam,
    toggleClass,
    setExpandedTeams,
    setExpandedClasses,
  };
};

