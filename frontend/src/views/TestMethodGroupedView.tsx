import React, { useState, useEffect, useMemo, useCallback } from 'react';
import { RefreshCw, AlertCircle, FileText } from 'lucide-react';
import { api, type GroupedTestMethodResponse } from '../lib/api';
import { 
  isMethodAnnotated, 
  calculateCoverageRate, 
  countAnnotatedMethods
} from '../utils/methodUtils';
import { type ExportOption } from '../components/shared/ExportManager';
import { 
  prepareGroupedTestMethodExportData, 
  exportData as exportDataUtil
} from '../utils/exportUtils';
import GroupedViewHeader from '../components/test-methods/GroupedViewHeader';
import SummaryStats from '../components/test-methods/SummaryStats';
import SearchAndFilters from '../components/test-methods/SearchAndFilters';
import TeamCard from '../components/test-methods/TeamCard';
import { useGroupExpansion } from '../hooks/useGroupExpansion';

const TestMethodGroupedView: React.FC = () => {
  const [groupedData, setGroupedData] = useState<GroupedTestMethodResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [debouncedSearchTerm, setDebouncedSearchTerm] = useState('');
  const [isSearching, setIsSearching] = useState(false);
  const [filterAnnotated, setFilterAnnotated] = useState<'all' | 'annotated' | 'not-annotated'>('all');

  // Use custom hook for expansion state
  const { expandedTeams, expandedClasses, toggleTeam, toggleClass } = useGroupExpansion();

  // Debounce search term to prevent excessive filtering
  useEffect(() => {
    setIsSearching(true);
    const timer = setTimeout(() => {
      setDebouncedSearchTerm(searchTerm);
      setIsSearching(false);
    }, 300); // 300ms delay

    return () => clearTimeout(timer);
  }, [searchTerm]);

  const fetchGroupedData = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await api.dashboard.getAllTestMethodDetailsGrouped(500); // Increased limit for better overview
      setGroupedData(data);
    } catch (err) {
      console.error('Error fetching grouped test method data:', err);
      setError('Failed to load test method data');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchGroupedData();
  }, [fetchGroupedData]);

  // Filter and search logic with debounced search
  const filteredData = useMemo(() => {
    if (!groupedData) return null;

    const filteredTeams = groupedData.teams.map(team => {
      const filteredClasses = team.classes.map(classGroup => {
        const filteredMethods = classGroup.methods.filter(method => {
          // Search filter (using debounced term)
          const matchesSearch = !debouncedSearchTerm || 
            method.testMethod.toLowerCase().includes(debouncedSearchTerm.toLowerCase()) ||
            method.testClass.toLowerCase().includes(debouncedSearchTerm.toLowerCase()) ||
            method.repository.toLowerCase().includes(debouncedSearchTerm.toLowerCase()) ||
            (method.title && method.title.toLowerCase().includes(debouncedSearchTerm.toLowerCase()));

          // Annotation filter
          const matchesAnnotation = filterAnnotated === 'all' ||
            (filterAnnotated === 'annotated' && isMethodAnnotated(method)) ||
            (filterAnnotated === 'not-annotated' && !isMethodAnnotated(method));

          return matchesSearch && matchesAnnotation;
        });

        return {
          ...classGroup,
          methods: filteredMethods,
          summary: {
            ...classGroup.summary,
            totalMethods: filteredMethods.length,
            annotatedMethods: countAnnotatedMethods(filteredMethods),
            coverageRate: calculateCoverageRate(filteredMethods)
          }
        };
      }).filter(classGroup => classGroup.methods.length > 0);

      // Recalculate team summary
      const totalMethods = filteredClasses.reduce((sum, c) => sum + c.summary.totalMethods, 0);
      const annotatedMethods = filteredClasses.reduce((sum, c) => sum + c.summary.annotatedMethods, 0);

      return {
        ...team,
        classes: filteredClasses,
        summary: {
          ...team.summary,
          totalClasses: filteredClasses.length,
          totalMethods,
          annotatedMethods,
          coverageRate: totalMethods > 0 ? (annotatedMethods / totalMethods) * 100 : 0
        }
      };
    }).filter(team => team.classes.length > 0);

    // Recalculate overall summary
    const totalTeams = filteredTeams.length;
    const totalClasses = filteredTeams.reduce((sum, t) => sum + t.summary.totalClasses, 0);
    const totalMethods = filteredTeams.reduce((sum, t) => sum + t.summary.totalMethods, 0);
    const totalAnnotatedMethods = filteredTeams.reduce((sum, t) => sum + t.summary.annotatedMethods, 0);

    return {
      ...groupedData,
      teams: filteredTeams,
      summary: {
        ...groupedData.summary,
        totalTeams,
        totalClasses,
        totalMethods,
        totalAnnotatedMethods,
        overallCoverageRate: totalMethods > 0 ? (totalAnnotatedMethods / totalMethods) * 100 : 0
      }
    };
  }, [groupedData, debouncedSearchTerm, filterAnnotated]);

  const handleExport = async (option: ExportOption) => {
    if (!filteredData) return;

    try {
      const exportData = prepareGroupedTestMethodExportData(filteredData, option.scope);
      await exportDataUtil(exportData, option);
    } catch (error) {
      console.error('Export failed:', error);
      throw error;
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center">
        <div className="text-center">
          <RefreshCw className="h-8 w-8 animate-spin mx-auto text-blue-600 dark:text-blue-400" />
          <p className="mt-2 text-gray-600 dark:text-gray-400">Loading test methods...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center">
        <div className="card max-w-md text-center">
          <AlertCircle className="h-12 w-12 text-red-500 mx-auto mb-4" />
          <h2 className="text-xl font-bold text-gray-900 dark:text-gray-100 mb-2">Error</h2>
          <p className="text-gray-600 dark:text-gray-400 mb-4">{error}</p>
          <button 
            onClick={fetchGroupedData}
            className="btn btn-primary"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  if (!filteredData || filteredData.teams.length === 0) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center">
        <div className="text-center">
          <FileText className="h-12 w-12 text-gray-400 mx-auto mb-4" />
          <h2 className="text-xl font-bold text-gray-900 dark:text-gray-100 mb-2">No Test Methods Found</h2>
          <p className="text-gray-600 dark:text-gray-400">No test methods match your current filters.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      {/* Header */}
      <GroupedViewHeader
        onRefresh={fetchGroupedData}
        onExport={handleExport}
      />

      {/* Main Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        {/* Summary Stats */}
        <SummaryStats
          totalTeams={filteredData.summary.totalTeams}
          totalClasses={filteredData.summary.totalClasses}
          totalMethods={filteredData.summary.totalMethods}
          coverageRate={filteredData.summary.overallCoverageRate}
        />

        {/* Search and Filters */}
        <SearchAndFilters
          searchTerm={searchTerm}
          onSearchChange={setSearchTerm}
          isSearching={isSearching}
          filterAnnotated={filterAnnotated}
          onFilterChange={setFilterAnnotated}
        />

        {/* Results Summary */}
        <div className="mb-6">
          <p className="text-sm text-gray-600 dark:text-gray-400">
            Showing {filteredData.summary.totalMethods} test methods across {filteredData.summary.totalTeams} teams and {filteredData.summary.totalClasses} classes
            {debouncedSearchTerm && ` (filtered by "${debouncedSearchTerm}")`}
            {filterAnnotated !== 'all' && ` (${filterAnnotated === 'annotated' ? 'annotated' : 'not annotated'} only)`}
          </p>
        </div>

        {/* Hierarchical Data */}
        <div className="space-y-4">
          {filteredData.teams.map((team) => (
            <TeamCard
              key={team.teamName}
              team={team}
              isExpanded={expandedTeams.has(team.teamName)}
              onToggle={() => toggleTeam(team.teamName)}
              expandedClasses={expandedClasses}
              onClassToggle={toggleClass}
            />
          ))}
        </div>
      </div>
    </div>
  );
};

export default TestMethodGroupedView;
