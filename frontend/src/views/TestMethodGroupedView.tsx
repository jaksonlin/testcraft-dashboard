import React, { useState, useEffect, useMemo, useCallback } from 'react';
import { 
  ChevronDown, 
  ChevronRight, 
  Search, 
  Filter, 
  RefreshCw,
  Users,
  FileText,
  Target,
  CheckCircle,
  XCircle,
  AlertCircle,
  BarChart3
} from 'lucide-react';
import { api, type GroupedTestMethodResponse, type TestMethodDetail } from '../lib/api';
import { 
  isMethodAnnotated, 
  calculateCoverageRate, 
  countAnnotatedMethods
} from '../utils/methodUtils';
import ExportManager, { type ExportOption } from '../components/shared/ExportManager';
import { 
  prepareGroupedTestMethodExportData, 
  exportData as exportDataUtil
} from '../utils/exportUtils';

// Simple virtual scrolling component for method lists
const VirtualMethodList: React.FC<{
  methods: TestMethodDetail[];
  maxVisible?: number;
}> = ({ methods, maxVisible = 10 }) => {
  const [showAll, setShowAll] = useState(false);
  const visibleMethods = showAll ? methods : methods.slice(0, maxVisible);
  const hasMore = methods.length > maxVisible;

  return (
    <div className="space-y-2">
      {visibleMethods.map((method) => {
        const isAnnotated = isMethodAnnotated(method);
        return (
          <div 
            key={method.id} 
            className="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-800 rounded-lg"
          >
            <div className="flex items-center">
              {isAnnotated ? (
                <CheckCircle className="h-4 w-4 text-green-600 dark:text-green-400 mr-2" />
              ) : (
                <XCircle className="h-4 w-4 text-red-600 dark:text-red-400 mr-2" />
              )}
              <div>
                <p className="font-medium text-gray-900 dark:text-gray-100">
                  {method.testMethod}
                </p>
                {method.title && method.title !== 'No title' && (
                  <p className="text-sm text-gray-600 dark:text-gray-400">
                    {method.title}
                  </p>
                )}
              </div>
            </div>
            <div className="text-right">
              <p className="text-xs text-gray-600 dark:text-gray-400">
                {method.author || 'Unknown'}
              </p>
              <p className="text-xs text-gray-500 dark:text-gray-500">
                {method.status || 'UNKNOWN'}
              </p>
            </div>
          </div>
        );
      })}
      {hasMore && !showAll && (
        <button
          onClick={() => setShowAll(true)}
          className="w-full p-2 text-sm text-blue-600 dark:text-blue-400 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg transition-colors"
        >
          Show {methods.length - maxVisible} more methods...
        </button>
      )}
      {showAll && hasMore && (
        <button
          onClick={() => setShowAll(false)}
          className="w-full p-2 text-sm text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg transition-colors"
        >
          Show less
        </button>
      )}
    </div>
  );
};

const TestMethodGroupedView: React.FC = () => {
  const [groupedData, setGroupedData] = useState<GroupedTestMethodResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [debouncedSearchTerm, setDebouncedSearchTerm] = useState('');
  const [isSearching, setIsSearching] = useState(false);
  const [expandedTeams, setExpandedTeams] = useState<Set<string>>(new Set());
  const [expandedClasses, setExpandedClasses] = useState<Set<string>>(new Set());
  const [filterAnnotated, setFilterAnnotated] = useState<'all' | 'annotated' | 'not-annotated'>('all');

  // Debounce search term to prevent excessive filtering
  useEffect(() => {
    setIsSearching(true);
    const timer = setTimeout(() => {
      setDebouncedSearchTerm(searchTerm);
      setIsSearching(false);
    }, 300); // 300ms delay

    return () => clearTimeout(timer);
  }, [searchTerm]);

  const fetchGroupedData = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await api.dashboard.getAllTestMethodDetailsGrouped(500); // Increased limit for better overview
      setGroupedData(data);
      
      // Auto-expand first team for better UX
      if (data.teams.length > 0) {
        setExpandedTeams(new Set([data.teams[0].teamName]));
      }
    } catch (err) {
      console.error('Error fetching grouped test method data:', err);
      setError('Failed to load test method data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchGroupedData();
  }, []);

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

  const toggleTeamExpansion = useCallback((teamName: string) => {
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

  const toggleClassExpansion = useCallback((classKey: string) => {
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

  const formatCoverageRate = (rate: number) => `${rate.toFixed(1)}%`;

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
      <div className="bg-white dark:bg-gray-800 shadow-sm border-b border-gray-200 dark:border-gray-700">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div className="flex items-center justify-between">
            <div className="flex items-center">
              <BarChart3 className="h-8 w-8 text-blue-600 dark:text-blue-400 mr-3" />
              <div>
                <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">
                  Test Methods Analysis
                </h1>
                <p className="text-sm text-gray-600 dark:text-gray-400">
                  Hierarchical view of test methods grouped by team and class
                </p>
              </div>
            </div>
            <div className="flex items-center space-x-3">
              <button
                onClick={fetchGroupedData}
                className="btn btn-secondary flex items-center"
              >
                <RefreshCw className="h-4 w-4 mr-2" />
                Refresh
              </button>
              <ExportManager
                data={[]}
                dataType="methods"
                onExport={handleExport}
                className="flex items-center"
              />
            </div>
          </div>
        </div>
      </div>

      {/* Summary Stats */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          <div className="card">
            <div className="flex items-center">
              <Users className="h-8 w-8 text-blue-600 dark:text-blue-400" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Teams</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-gray-100">
                  {filteredData.summary.totalTeams}
                </p>
              </div>
            </div>
          </div>
          <div className="card">
            <div className="flex items-center">
              <FileText className="h-8 w-8 text-green-600 dark:text-green-400" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Classes</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-gray-100">
                  {filteredData.summary.totalClasses}
                </p>
              </div>
            </div>
          </div>
          <div className="card">
            <div className="flex items-center">
              <Target className="h-8 w-8 text-purple-600 dark:text-purple-400" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Test Methods</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-gray-100">
                  {filteredData.summary.totalMethods}
                </p>
              </div>
            </div>
          </div>
          <div className="card">
            <div className="flex items-center">
              <CheckCircle className="h-8 w-8 text-orange-600 dark:text-orange-400" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Coverage Rate</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-gray-100">
                  {formatCoverageRate(filteredData.summary.overallCoverageRate)}
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Search and Filters */}
        <div className="card mb-6">
          <div className="flex flex-col sm:flex-row gap-4">
            <div className="flex-1">
              <div className="relative">
                <Search className={`absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 ${isSearching ? 'text-blue-500 animate-pulse' : 'text-gray-400'}`} />
                <input
                  type="text"
                  placeholder="Search test methods, classes, or repositories..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="input pl-10 w-full"
                />
                {isSearching && (
                  <div className="absolute right-3 top-1/2 transform -translate-y-1/2">
                    <div className="animate-spin rounded-full h-4 w-4 border-2 border-blue-500 border-t-transparent"></div>
                  </div>
                )}
              </div>
            </div>
            <div className="flex items-center space-x-4">
              <div className="flex items-center">
                <Filter className="h-4 w-4 text-gray-400 mr-2" />
                <select
                  value={filterAnnotated}
                  onChange={(e) => setFilterAnnotated(e.target.value as 'all' | 'annotated' | 'not-annotated')}
                  className="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="all">All Methods</option>
                  <option value="annotated">Annotated Only</option>
                  <option value="not-annotated">Not Annotated</option>
                </select>
              </div>
            </div>
          </div>
        </div>

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
            <div key={team.teamName} className="card">
              {/* Team Header */}
              <div 
                className="flex items-center justify-between p-4 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700 rounded-lg"
                onClick={() => toggleTeamExpansion(team.teamName)}
              >
                <div className="flex items-center">
                  {expandedTeams.has(team.teamName) ? (
                    <ChevronDown className="h-5 w-5 text-gray-400 mr-3" />
                  ) : (
                    <ChevronRight className="h-5 w-5 text-gray-400 mr-3" />
                  )}
                  <Users className="h-6 w-6 text-blue-600 dark:text-blue-400 mr-3" />
                  <div>
                    <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">
                      {team.teamName}
                    </h3>
                    <p className="text-sm text-gray-600 dark:text-gray-400">
                      {team.teamCode} • {team.summary.totalClasses} classes • {team.summary.totalMethods} methods
                    </p>
                  </div>
                </div>
                <div className="flex items-center space-x-4">
                  <div className="text-right">
                    <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
                      {formatCoverageRate(team.summary.coverageRate)}
                    </p>
                    <p className="text-xs text-gray-600 dark:text-gray-400">
                      {team.summary.annotatedMethods}/{team.summary.totalMethods} annotated
                    </p>
                  </div>
                </div>
              </div>

              {/* Team Classes */}
              {expandedTeams.has(team.teamName) && (
                <div className="border-t border-gray-200 dark:border-gray-700">
                  {team.classes.map((classGroup) => {
                    const classKey = `${team.teamName}.${classGroup.repository}.${classGroup.className}`;
                    return (
                      <div key={classKey} className="p-4">
                        {/* Class Header */}
                        <div 
                          className="flex items-center justify-between p-3 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700 rounded-lg"
                          onClick={() => toggleClassExpansion(classKey)}
                        >
                          <div className="flex items-center">
                            {expandedClasses.has(classKey) ? (
                              <ChevronDown className="h-4 w-4 text-gray-400 mr-2" />
                            ) : (
                              <ChevronRight className="h-4 w-4 text-gray-400 mr-2" />
                            )}
                            <FileText className="h-5 w-5 text-green-600 dark:text-green-400 mr-2" />
                            <div>
                              <h4 className="font-medium text-gray-900 dark:text-gray-100">
                                {classGroup.className}
                              </h4>
                              <p className="text-sm text-gray-600 dark:text-gray-400">
                                {classGroup.repository} • {classGroup.summary.totalMethods} methods
                              </p>
                            </div>
                          </div>
                          <div className="text-right">
                            <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
                              {formatCoverageRate(classGroup.summary.coverageRate)}
                            </p>
                            <p className="text-xs text-gray-600 dark:text-gray-400">
                              {classGroup.summary.annotatedMethods}/{classGroup.summary.totalMethods} annotated
                            </p>
                          </div>
                        </div>

                        {/* Class Methods */}
                        {expandedClasses.has(classKey) && (
                          <div className="mt-3 ml-6">
                            <VirtualMethodList 
                              methods={classGroup.methods} 
                              maxVisible={8}
                            />
                          </div>
                        )}
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default TestMethodGroupedView;
