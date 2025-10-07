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
  XCircle
} from 'lucide-react';
import { api, type RepositoryDetail, type TestMethodDetail, type TestClassSummary } from '../lib/api';
import { isMethodAnnotated, getAnnotationStatusDisplayName } from '../utils/methodUtils';
import BreadcrumbNavigation from '../components/shared/BreadcrumbNavigation';
import GitUrlLink from '../components/shared/GitUrlLink';

const RepositoryDetailView: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [repository, setRepository] = useState<RepositoryDetail | null>(null);
  const [testMethods, setTestMethods] = useState<TestMethodDetail[]>([]);
  const [classes, setClasses] = useState<TestClassSummary[]>([]);
  const [activeTab, setActiveTab] = useState<'classes' | 'methods'>('classes');
  const [selectedClassId, setSelectedClassId] = useState<number | null>(null);
  const [selectedClassName, setSelectedClassName] = useState<string>('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [scanning, setScanning] = useState(false);

  const fetchRepositoryDetails = useCallback(async () => {
    if (!id) return;
    
    try {
      setLoading(true);
      setError(null);
      
      // Fetch repository details using repository API
      const repo = await api.repositories.getById(parseInt(id));
      setRepository(repo);
      
      // Fetch classes for this repository
      const classList = await api.repositories.getClasses(parseInt(id));
      setClasses(classList);
      
    } catch (err) {
      console.error('Error fetching repository details:', err);
      setError('Failed to load repository details');
    } finally {
      setLoading(false);
    }
  }, [id]);
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
            <span className="text-sm" style={{ color: 'var(--color-muted-foreground)' }}>{classes.length} classes</span>
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
