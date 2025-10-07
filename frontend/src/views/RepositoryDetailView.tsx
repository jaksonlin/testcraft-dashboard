import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { 
  ArrowLeft, 
  Play, 
  Download, 
  Calendar, 
  Users, 
  Target, 
  FileText,
  Activity,
  AlertCircle,
  CheckCircle,
  XCircle,
  Search,
  Filter,
  ChevronLeft,
  ChevronRight
} from 'lucide-react';
import { api, type RepositoryDetail, type TestMethodDetail, type TestClassSummary, type PagedResponse } from '../lib/api';
import { isMethodAnnotated, getAnnotationStatusDisplayName } from '../utils/methodUtils';
import BreadcrumbNavigation from '../components/shared/BreadcrumbNavigation';
import GitUrlLink from '../components/shared/GitUrlLink';

const RepositoryDetailView: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [repository, setRepository] = useState<RepositoryDetail | null>(null);
  const [testMethods, setTestMethods] = useState<TestMethodDetail[]>([]);
  const [classes, setClasses] = useState<TestClassSummary[]>([]);
  const [classesPagination, setClassesPagination] = useState<PagedResponse<TestClassSummary> | null>(null);
  const [activeTab, setActiveTab] = useState<'classes' | 'methods'>('classes');
  const [selectedClassId, setSelectedClassId] = useState<number | null>(null);
  const [selectedClassName, setSelectedClassName] = useState<string>('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [scanning, setScanning] = useState(false);
  
  // Pagination state for classes
  const [classesPage, setClassesPage] = useState(0);
  const [classesPageSize, setClassesPageSize] = useState(10);
  const [classesSearchTerm, setClassesSearchTerm] = useState('');
  const [classesAnnotatedFilter, setClassesAnnotatedFilter] = useState<'all' | 'annotated' | 'not-annotated'>('all');

  const fetchClassesPaginated = useCallback(async () => {
    if (!id) return;
    
    try {
      const annotated = classesAnnotatedFilter === 'all' ? undefined : classesAnnotatedFilter === 'annotated';
      const paginatedClasses = await api.repositories.getClassesPaginated(
        parseInt(id), 
        classesPage, 
        classesPageSize, 
        classesSearchTerm || undefined, 
        annotated
      );
      setClassesPagination(paginatedClasses);
      setClasses(paginatedClasses.content);
    } catch (err) {
      console.error('Error fetching paginated classes:', err);
      setError('Failed to load classes');
    }
  }, [id, classesPage, classesPageSize, classesSearchTerm, classesAnnotatedFilter]);

  const fetchRepositoryDetails = useCallback(async () => {
    if (!id) return;
    
    try {
      setLoading(true);
      setError(null);
      
      // Fetch repository details using repository API
      const repo = await api.repositories.getById(parseInt(id));
      setRepository(repo);
      
      // Fetch paginated classes for this repository
      await fetchClassesPaginated();
      
    } catch (err) {
      console.error('Error fetching repository details:', err);
      setError('Failed to load repository details');
    } finally {
      setLoading(false);
    }
  }, [id, fetchClassesPaginated]);
  // When a class is selected, load its methods and switch to Methods tab
  const handleSelectClass = async (cls: TestClassSummary) => {
    if (!id) return;
    setSelectedClassId(cls.id);
    setSelectedClassName(cls.className);
    setActiveTab('methods');
    try {
      const methods = await api.repositories.getClassMethods(parseInt(id), cls.id, 500);
      setTestMethods(methods);
    } catch (e) {
      console.error('Error fetching class methods', e);
      setTestMethods([]);
    }
  };


  useEffect(() => {
    if (id) {
      fetchRepositoryDetails();
    }
  }, [id, fetchRepositoryDetails]);

  // Fetch classes when pagination parameters change
  useEffect(() => {
    if (id && repository) {
      fetchClassesPaginated();
    }
  }, [id, repository, fetchClassesPaginated]);

  const handleScanRepository = async () => {
    try {
      setScanning(true);
      await api.scan.trigger();
      // Refresh data after scan
      setTimeout(fetchRepositoryDetails, 2000);
    } catch (err) {
      console.error('Error triggering scan:', err);
    } finally {
      setScanning(false);
    }
  };

  const handleExportData = () => {
    if (!repository) return;
    
    const data = {
      repository: repository.repository,
      path: repository.path,
      gitUrl: repository.gitUrl,
      teamName: repository.teamName,
      teamCode: repository.teamCode,
      testClasses: repository.testClasses,
      testMethodCount: repository.testMethodCount,
      annotatedMethods: repository.annotatedMethods,
      coverageRate: repository.coverageRate,
      lastScan: repository.lastScan,
      testMethods: testMethods
    };

    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${repository.repository}-details.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getCoverageColor = (coverage: number) => {
    if (coverage >= 80) return 'text-green-600';
    if (coverage >= 50) return 'text-orange-600';
    return 'text-red-600';
  };

  const getCoverageBgColor = (coverage: number) => {
    if (coverage >= 80) return 'bg-green-100';
    if (coverage >= 50) return 'bg-orange-100';
    return 'bg-red-100';
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-2" style={{ color: 'var(--color-muted-foreground)' }}>Loading repository details...</p>
        </div>
      </div>
    );
  }

  if (error || !repository) {
    return (
      <div className="p-8">
        <div className="card text-center py-12">
          <AlertCircle className="h-12 w-12 text-red-500 mx-auto mb-4" />
          <h2 className="text-xl font-bold mb-2" style={{ color: 'var(--color-foreground)' }}>Repository Not Found</h2>
          <p className="mb-4" style={{ color: 'var(--color-muted-foreground)' }}>{error || 'The requested repository could not be found.'}</p>
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
            className="flex items-center hover:opacity-80" style={{ color: 'var(--color-muted-foreground)' }}
          >
            <ArrowLeft className="h-5 w-5 mr-2" />
            Back to Repositories
          </button>
          
          <div className="flex gap-2">
            <button
              onClick={handleScanRepository}
              disabled={scanning}
              className="btn btn-primary"
            >
              <Play className="h-4 w-4 mr-2" />
              {scanning ? 'Scanning...' : 'Scan Repository'}
            </button>
            <button
              onClick={handleExportData}
              className="btn btn-secondary"
            >
              <Download className="h-4 w-4 mr-2" />
              Export Data
            </button>
          </div>
        </div>
        
        <div className="flex items-start justify-between">
          <div>
            <h1 className="text-3xl font-bold mb-2" style={{ color: 'var(--color-foreground)' }}>{repository.repository}</h1>
            <div className="flex items-center mb-4" style={{ color: 'var(--color-muted-foreground)' }}>
              <GitUrlLink 
                url={repository.gitUrl}
                showIcon={true}
                copyable={true}
                className="font-mono text-sm"
              />
            </div>
            <div className="flex items-center" style={{ color: 'var(--color-muted-foreground)' }}>
              <Users className="h-4 w-4 mr-2" />
              <span className="text-sm">{repository.teamName} ({repository.teamCode})</span>
            </div>
          </div>
          
          <div className={`px-4 py-2 rounded-lg ${getCoverageBgColor(repository.coverageRate)}`}>
            <div className="flex items-center">
              <Target className="h-5 w-5 mr-2" />
              <span className={`text-lg font-bold ${getCoverageColor(repository.coverageRate)}`}>
                {repository.coverageRate.toFixed(1)}%
              </span>
            </div>
            <div className="text-sm" style={{ color: 'var(--color-muted-foreground)' }}>Coverage Rate</div>
          </div>
        </div>
      </div>

      {/* Stats Overview */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div className="card">
          <div className="flex items-center">
            <FileText className="h-8 w-8 text-blue-600 mr-3" />
            <div>
              <p className="text-sm font-medium" style={{ color: 'var(--color-muted-foreground)' }}>Test Classes</p>
              <p className="text-2xl font-bold" style={{ color: 'var(--color-foreground)' }}>{repository.testClasses}</p>
            </div>
          </div>
        </div>
        
        <div className="card">
          <div className="flex items-center">
            <Activity className="h-8 w-8 text-green-600 mr-3" />
            <div>
              <p className="text-sm font-medium" style={{ color: 'var(--color-muted-foreground)' }}>Test Methods</p>
              <p className="text-2xl font-bold" style={{ color: 'var(--color-foreground)' }}>{repository.testMethodCount}</p>
            </div>
          </div>
        </div>
        
        <div className="card">
          <div className="flex items-center">
            <Target className="h-8 w-8 text-purple-600 mr-3" />
            <div>
              <p className="text-sm font-medium" style={{ color: 'var(--color-muted-foreground)' }}>Annotated Methods</p>
              <p className="text-2xl font-bold" style={{ color: 'var(--color-foreground)' }}>{repository.annotatedMethods}</p>
            </div>
          </div>
        </div>
        
        <div className="card">
          <div className="flex items-center">
            <Calendar className="h-8 w-8 text-orange-600 mr-3" />
            <div>
              <p className="text-sm font-medium" style={{ color: 'var(--color-muted-foreground)' }}>Last Scan</p>
              <p className="text-sm font-bold" style={{ color: 'var(--color-foreground)' }}>{formatDate(repository.lastScan)}</p>
            </div>
          </div>
        </div>
      </div>

      {/* Coverage Progress */}
      <div className="card mb-8">
        <h3 className="text-lg font-semibold mb-4" style={{ color: 'var(--color-foreground)' }}>Coverage Analysis</h3>
        <div className="space-y-4">
          <div>
            <div className="flex justify-between text-sm mb-1">
              <span style={{ color: 'var(--color-muted-foreground)' }}>Overall Coverage</span>
              <span className="font-medium" style={{ color: 'var(--color-foreground)' }}>{repository.coverageRate.toFixed(1)}%</span>
            </div>
            <div className="w-full rounded-full h-3" style={{ backgroundColor: 'var(--color-border)' }}>
              <div 
                className="bg-orange-custom h-3 rounded-full transition-all duration-300" 
                style={{ width: `${repository.coverageRate}%` }}
              ></div>
            </div>
          </div>
          
          <div className="grid grid-cols-3 gap-4 text-center">
            <div>
              <div className="text-2xl font-bold text-blue-600">{repository.testClasses}</div>
              <div className="text-sm" style={{ color: 'var(--color-muted-foreground)' }}>Test Classes</div>
            </div>
            <div>
              <div className="text-2xl font-bold text-green-600">{repository.testMethodCount}</div>
              <div className="text-sm" style={{ color: 'var(--color-muted-foreground)' }}>Total Methods</div>
            </div>
            <div>
              <div className="text-2xl font-bold text-purple-600">{repository.annotatedMethods}</div>
              <div className="text-sm" style={{ color: 'var(--color-muted-foreground)' }}>Annotated</div>
            </div>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="mb-4 border-b border-gray-200">
        <nav className="-mb-px flex space-x-6" aria-label="Tabs">
          <button
            onClick={() => setActiveTab('classes')}
            className={`whitespace-nowrap py-3 px-1 border-b-2 font-medium text-sm ${activeTab === 'classes' ? 'border-blue-600 text-blue-600' : 'border-transparent hover:border-gray-300'}`}
            style={{ color: activeTab === 'classes' ? '#3b82f6' : 'var(--color-muted-foreground)' }}
            onMouseEnter={(e) => { if (activeTab !== 'classes') (e.target as HTMLElement).style.color = 'var(--color-foreground)'; }}
            onMouseLeave={(e) => { if (activeTab !== 'classes') (e.target as HTMLElement).style.color = 'var(--color-muted-foreground)'; }}
          >
            Classes
          </button>
          <button
            onClick={() => selectedClassId && setActiveTab('methods')}
            className={`whitespace-nowrap py-3 px-1 border-b-2 font-medium text-sm ${activeTab === 'methods' ? 'border-blue-600 text-blue-600' : 'border-transparent hover:border-gray-300'} ${!selectedClassId ? 'opacity-50 cursor-not-allowed' : ''}`}
            style={{ color: activeTab === 'methods' ? '#3b82f6' : 'var(--color-muted-foreground)' }}
            onMouseEnter={(e) => { if (activeTab !== 'methods') (e.target as HTMLElement).style.color = 'var(--color-foreground)'; }}
            onMouseLeave={(e) => { if (activeTab !== 'methods') (e.target as HTMLElement).style.color = 'var(--color-muted-foreground)'; }}
            disabled={!selectedClassId}
          >
            Methods{selectedClassName ? `: ${selectedClassName}` : ''}
          </button>
        </nav>
      </div>

      {activeTab === 'classes' && (
        <div className="card">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-semibold" style={{ color: 'var(--color-foreground)' }}>Test Classes</h3>
            <span className="text-sm" style={{ color: 'var(--color-muted-foreground)' }}>
              {classesPagination ? `${classesPagination.totalElements} classes` : `${classes.length} classes`}
            </span>
          </div>
          
          {/* Search and Filter Controls */}
          <div className="mb-4 flex flex-col sm:flex-row gap-4">
            <div className="flex-1">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
                <input
                  type="text"
                  placeholder="Search classes..."
                  value={classesSearchTerm}
                  onChange={(e) => {
                    setClassesSearchTerm(e.target.value);
                    setClassesPage(0); // Reset to first page when searching
                  }}
                  className="input pl-10 w-full"
                />
              </div>
            </div>
            <div className="flex items-center space-x-4">
              <div className="flex items-center">
                <Filter className="h-4 w-4 text-gray-400 mr-2" />
                <select
                  value={classesAnnotatedFilter}
                  onChange={(e) => {
                    setClassesAnnotatedFilter(e.target.value as 'all' | 'annotated' | 'not-annotated');
                    setClassesPage(0); // Reset to first page when filtering
                  }}
                  className="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="all">All Classes</option>
                  <option value="annotated">Annotated Only</option>
                  <option value="not-annotated">Not Annotated</option>
                </select>
              </div>
              <div className="flex items-center">
                <span className="text-sm text-gray-600 dark:text-gray-400 mr-2">Show:</span>
                <select
                  value={classesPageSize}
                  onChange={(e) => {
                    setClassesPageSize(parseInt(e.target.value));
                    setClassesPage(0); // Reset to first page when changing page size
                  }}
                  className="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value={5}>5 per page</option>
                  <option value={10}>10 per page</option>
                  <option value={20}>20 per page</option>
                  <option value={50}>50 per page</option>
                </select>
              </div>
            </div>
          </div>

          {classes.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead style={{ backgroundColor: 'var(--color-muted)' }}>
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>Class</th>
                    <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>Methods</th>
                    <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>Annotated</th>
                    <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>Coverage</th>
                    <th className="px-6 py-3"></th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200" style={{ backgroundColor: 'var(--color-background)' }}>
                  {classes.map((cls) => (
                    <tr key={cls.id} className="hover:bg-gray-50 dark:hover:bg-gray-700" style={{ backgroundColor: 'var(--color-background)' }}>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium" style={{ color: 'var(--color-foreground)' }}>{cls.className}</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm" style={{ color: 'var(--color-foreground)' }}>{cls.testMethodCount}</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm" style={{ color: 'var(--color-foreground)' }}>{cls.annotatedMethodCount}</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm" style={{ color: 'var(--color-foreground)' }}>{cls.coverageRate?.toFixed(1)}%</td>
                      <td className="px-6 py-4 whitespace-nowrap text-right text-sm">
                        <button className="btn btn-secondary" onClick={() => handleSelectClass(cls)}>View Methods</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="text-center py-12">
              <FileText className="h-12 w-12 text-gray-400 mx-auto mb-4" />
              <h3 className="text-lg font-medium mb-2" style={{ color: 'var(--color-foreground)' }}>No Test Classes Found</h3>
              <p style={{ color: 'var(--color-muted-foreground)' }}>This repository doesn't have any test classes yet.</p>
            </div>
          )}
          
          {/* Pagination Controls */}
          {classesPagination && classesPagination.totalPages > 1 && (
            <div className="flex items-center justify-between mt-4">
              <div className="text-sm text-gray-600 dark:text-gray-400">
                Showing {classesPage * classesPageSize + 1} to {Math.min((classesPage + 1) * classesPageSize, classesPagination.totalElements)} of {classesPagination.totalElements} classes
                {classesSearchTerm && ` (filtered by "${classesSearchTerm}")`}
                {classesAnnotatedFilter !== 'all' && ` (${classesAnnotatedFilter === 'annotated' ? 'annotated' : 'not annotated'} only)`}
              </div>
              <div className="flex items-center space-x-3">
                <button
                  onClick={() => setClassesPage(Math.max(0, classesPage - 1))}
                  disabled={classesPage === 0}
                  className={`
                    inline-flex items-center px-4 py-2 text-sm font-medium rounded-lg transition-all duration-200
                    ${classesPage === 0 
                      ? 'bg-gray-100 dark:bg-gray-700 text-gray-400 dark:text-gray-500 cursor-not-allowed' 
                      : 'bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-200 border border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-700 hover:border-gray-400 dark:hover:border-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent shadow-sm hover:shadow-md'
                    }
                  `}
                >
                  <ChevronLeft className="h-4 w-4 mr-2" />
                  Previous
                </button>
                
                <div className="flex items-center px-3 py-2 bg-gray-50 dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700">
                  <span className="text-sm font-medium text-gray-700 dark:text-gray-200">
                    Page {classesPage + 1} of {classesPagination.totalPages}
                  </span>
                </div>
                
                <button
                  onClick={() => setClassesPage(Math.min(classesPagination.totalPages - 1, classesPage + 1))}
                  disabled={classesPage >= classesPagination.totalPages - 1}
                  className={`
                    inline-flex items-center px-4 py-2 text-sm font-medium rounded-lg transition-all duration-200
                    ${classesPage >= classesPagination.totalPages - 1 
                      ? 'bg-gray-100 dark:bg-gray-700 text-gray-400 dark:text-gray-500 cursor-not-allowed' 
                      : 'bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-200 border border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-700 hover:border-gray-400 dark:hover:border-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent shadow-sm hover:shadow-md'
                    }
                  `}
                >
                  Next
                  <ChevronRight className="h-4 w-4 ml-2" />
                </button>
              </div>
            </div>
          )}
        </div>
      )}

      {activeTab === 'methods' && (
        <div className="card">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-semibold" style={{ color: 'var(--color-foreground)' }}>Methods {selectedClassName ? `for ${selectedClassName}` : ''}</h3>
            <span className="text-sm" style={{ color: 'var(--color-muted-foreground)' }}>{testMethods.length} methods</span>
          </div>
          {testMethods.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead style={{ backgroundColor: 'var(--color-muted)' }}>
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>Method</th>
                    <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>Annotation</th>
                    <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>Test Status</th>
                    <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>Author</th>
                    <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>Target</th>
                    <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider" style={{ color: 'var(--color-muted-foreground)' }}>Last Modified</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200" style={{ backgroundColor: 'var(--color-background)' }}>
                  {testMethods.map((method) => (
                    <tr key={method.id} className="hover:bg-gray-50 dark:hover:bg-gray-700" style={{ backgroundColor: 'var(--color-background)' }}>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium" style={{ color: 'var(--color-foreground)' }}>{method.testMethod}</td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          {isMethodAnnotated(method) ? (
                            <CheckCircle className="h-4 w-4 text-green-600 dark:text-green-400 mr-2" />
                          ) : (
                            <XCircle className="h-4 w-4 text-red-600 dark:text-red-400 mr-2" />
                          )}
                          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                            isMethodAnnotated(method) ? 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300' : 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-300'
                          }`}>
                            {getAnnotationStatusDisplayName(method)}
                          </span>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                          method.status === 'PASS' ? 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300' :
                          method.status === 'FAIL' ? 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-300' :
                          'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-300'
                        }`}>
                          {method.status || 'UNKNOWN'}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm" style={{ color: 'var(--color-foreground)' }}>{method.author || 'N/A'}</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm" style={{ color: 'var(--color-muted-foreground)' }}>{method.targetClass}.{method.targetMethod}</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm" style={{ color: 'var(--color-muted-foreground)' }}>{method.lastModified ? formatDate(method.lastModified) : 'N/A'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="text-center py-12">
              <FileText className="h-12 w-12 text-gray-400 mx-auto mb-4" />
              <h3 className="text-lg font-medium mb-2" style={{ color: 'var(--color-foreground)' }}>Select a class first</h3>
              <p style={{ color: 'var(--color-muted-foreground)' }}>Choose a class on the Classes tab to view its methods.</p>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default RepositoryDetailView;
