import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  ArrowLeft,
  FileText,
  Target,
  Users,
  ChevronRight,
  ChevronDown,
  Download,
  AlertCircle
} from 'lucide-react';
import { api, type TestMethodDetail } from '../lib/api';
import BreadcrumbNavigation from '../components/shared/BreadcrumbNavigation';
import { 
  calculateCoverageRate, 
  countAnnotatedMethods
} from '../utils/methodUtils';

interface ClassGroup {
  className: string;
  methods: TestMethodDetail[];
  methodCount: number;
  annotatedCount: number;
  coverageRate: number;
}

const ClassLevelView: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [classGroups, setClassGroups] = useState<ClassGroup[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [expandedClasses, setExpandedClasses] = useState<Set<string>>(new Set());
  const [repositoryName, setRepositoryName] = useState<string>('');

  const fetchClassData = useCallback(async () => {
    if (!id) return;
    
    try {
      setLoading(true);
      setError(null);
      
      // Fetch test methods for this repository using repository API
      const methods = await api.repositories.getTestMethods(parseInt(id));
      
      if (methods.length === 0) {
        setError('No test methods found for this repository');
        return;
      }
      
      setRepositoryName(methods[0]?.repository || 'Unknown Repository');
      
      // Group methods by class
      const grouped = groupMethodsByClass(methods);
      setClassGroups(grouped);
      
    } catch (err) {
      console.error('Error fetching class data:', err);
      setError('Failed to load class data');
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    if (id) {
      fetchClassData();
    }
  }, [id, fetchClassData]);

  const groupMethodsByClass = (methods: TestMethodDetail[]): ClassGroup[] => {
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
  };

  const toggleClassExpansion = (className: string) => {
    const newExpanded = new Set(expandedClasses);
    if (newExpanded.has(className)) {
      newExpanded.delete(className);
    } else {
      newExpanded.add(className);
    }
    setExpandedClasses(newExpanded);
  };

  const expandAllClasses = () => {
    setExpandedClasses(new Set(classGroups.map(c => c.className)));
  };

  const collapseAllClasses = () => {
    setExpandedClasses(new Set());
  };

  const handleExportClassData = (className: string) => {
    const classData = classGroups.find(c => c.className === className);
    if (!classData) return;
    
    const data = {
      repository: repositoryName,
      className: className,
      methodCount: classData.methodCount,
      annotatedCount: classData.annotatedCount,
      coverageRate: classData.coverageRate,
      methods: classData.methods
    };

    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${className}-details.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const getCoverageColor = (coverage: number) => {
    if (coverage >= 80) return 'text-green-600 dark:text-green-400';
    if (coverage >= 50) return 'text-orange-600 dark:text-orange-400';
    return 'text-red-600 dark:text-red-400';
  };

  const getCoverageBgColor = (coverage: number) => {
    if (coverage >= 80) return 'bg-green-100 dark:bg-green-900/20';
    if (coverage >= 50) return 'bg-orange-100 dark:bg-orange-900/20';
    return 'bg-red-100 dark:bg-red-900/20';
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PASS': return 'bg-green-100 dark:bg-green-900/20 text-green-800 dark:text-green-200';
      case 'FAIL': return 'bg-red-100 dark:bg-red-900/20 text-red-800 dark:text-red-200';
      case 'SKIP': return 'bg-yellow-100 dark:bg-yellow-900/20 text-yellow-800 dark:text-yellow-200';
      default: return 'bg-gray-100 dark:bg-gray-800 text-gray-800 dark:text-gray-200';
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-2 text-gray-600 dark:text-gray-400">Loading class data...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-8">
        <div className="card text-center py-12">
          <AlertCircle className="h-12 w-12 text-red-500 dark:text-red-400 mx-auto mb-4" />
          <h2 className="text-xl font-bold text-gray-900 dark:text-gray-100 mb-2">Error Loading Class Data</h2>
          <p className="text-gray-600 dark:text-gray-400 mb-4">{error}</p>
          <button 
            onClick={() => navigate('/repositories')}
            className="btn btn-primary"
          >
            Back to Repositories
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="p-8">
      {/* Breadcrumb Navigation */}
      <BreadcrumbNavigation />
      
      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center justify-between mb-4">
          <button
            onClick={() => navigate('/repositories')}
            className="flex items-center text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-100"
          >
            <ArrowLeft className="h-5 w-5 mr-2" />
            Back to Repositories
          </button>
          
          <div className="flex gap-2">
            <button
              onClick={expandAllClasses}
              className="btn btn-secondary"
            >
              Expand All
            </button>
            <button
              onClick={collapseAllClasses}
              className="btn btn-secondary"
            >
              Collapse All
            </button>
          </div>
        </div>
        
        <div>
          <h1 className="text-3xl font-bold text-gray-900 dark:text-gray-100 mb-2">Class-Level Analysis</h1>
          <div className="flex items-center text-gray-600 dark:text-gray-400 mb-4">
            <FileText className="h-4 w-4 mr-2" />
            <span className="font-mono text-sm">{repositoryName}</span>
          </div>
          <div className="flex items-center text-gray-600 dark:text-gray-400">
            <Target className="h-4 w-4 mr-2" />
            <span className="text-sm">{classGroups.length} test classes found</span>
          </div>
        </div>
      </div>

      {/* Summary Stats */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <div className="card">
          <div className="flex items-center">
            <FileText className="h-8 w-8 text-blue-600 dark:text-blue-400 mr-3" />
            <div>
              <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Total Classes</p>
              <p className="text-2xl font-bold text-gray-900 dark:text-gray-100">{classGroups.length}</p>
            </div>
          </div>
        </div>
        
        <div className="card">
          <div className="flex items-center">
            <Target className="h-8 w-8 text-green-600 dark:text-green-400 mr-3" />
            <div>
              <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Total Methods</p>
              <p className="text-2xl font-bold text-gray-900 dark:text-gray-100">
                {classGroups.reduce((sum, c) => sum + c.methodCount, 0)}
              </p>
            </div>
          </div>
        </div>
        
        <div className="card">
          <div className="flex items-center">
            <Users className="h-8 w-8 text-purple-600 dark:text-purple-400 mr-3" />
            <div>
              <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Annotated Methods</p>
              <p className="text-2xl font-bold text-gray-900 dark:text-gray-100">
                {classGroups.reduce((sum, c) => sum + c.annotatedCount, 0)}
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Class List */}
      <div className="space-y-4">
        {classGroups.map((classGroup) => (
          <div key={classGroup.className} className="card">
            {/* Class Header */}
            <div 
              className="flex items-center justify-between p-4 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-800 rounded-lg"
              onClick={() => toggleClassExpansion(classGroup.className)}
            >
              <div className="flex items-center flex-1">
                {expandedClasses.has(classGroup.className) ? (
                  <ChevronDown className="h-5 w-5 text-gray-400 dark:text-gray-500 mr-3" />
                ) : (
                  <ChevronRight className="h-5 w-5 text-gray-400 dark:text-gray-500 mr-3" />
                )}
                
                <div className="flex-1">
                  <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">{classGroup.className}</h3>
                  <div className="flex items-center mt-1 space-x-4 text-sm text-gray-600 dark:text-gray-400">
                    <span>{classGroup.methodCount} methods</span>
                    <span>{classGroup.annotatedCount} annotated</span>
                    <span className={`font-medium ${getCoverageColor(classGroup.coverageRate)}`}>
                      {classGroup.coverageRate.toFixed(1)}% coverage
                    </span>
                  </div>
                </div>
              </div>
              
              <div className="flex items-center space-x-2">
                <div className={`px-3 py-1 rounded-full text-sm font-medium ${getCoverageBgColor(classGroup.coverageRate)}`}>
                  <span className={getCoverageColor(classGroup.coverageRate)}>
                    {classGroup.coverageRate.toFixed(1)}%
                  </span>
                </div>
                
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    handleExportClassData(classGroup.className);
                  }}
                  className="p-2 text-gray-400 dark:text-gray-500 hover:text-gray-600 dark:hover:text-gray-300"
                  title="Export class data"
                >
                  <Download className="h-4 w-4" />
                </button>
              </div>
            </div>

            {/* Class Methods (Expanded) */}
            {expandedClasses.has(classGroup.className) && (
              <div className="border-t border-gray-200 dark:border-gray-700 p-4">
                <div className="overflow-x-auto">
                  <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                    <thead className="bg-gray-50 dark:bg-gray-800">
                      <tr>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                          Method Name
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                          Status
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                          Author
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                          Target
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                          Line
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                          Last Modified
                        </th>
                      </tr>
                    </thead>
                    <tbody className="bg-white dark:bg-gray-900 divide-y divide-gray-200 dark:divide-gray-700">
                      {classGroup.methods.map((method) => (
                        <tr key={method.id} className="hover:bg-gray-50 dark:hover:bg-gray-800">
                          <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900 dark:text-gray-100">
                            {method.testMethod}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap">
                            <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(method.status)}`}>
                              {method.status}
                            </span>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-100">
                            {method.author}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                            {method.targetClass}.{method.targetMethod}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                            {method.line}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                            {method.lastModified ? new Date(method.lastModified).toLocaleDateString() : 'N/A'}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}
          </div>
        ))}
      </div>

      {classGroups.length === 0 && (
        <div className="card text-center py-12">
          <FileText className="h-12 w-12 text-gray-400 dark:text-gray-500 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100 mb-2">No Test Classes Found</h3>
          <p className="text-gray-600 dark:text-gray-400">This repository doesn't have any test classes yet.</p>
        </div>
      )}
    </div>
  );
};

export default ClassLevelView;
