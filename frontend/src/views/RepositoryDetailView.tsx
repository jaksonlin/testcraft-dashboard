import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { 
  ArrowLeft, 
  Play, 
  Download, 
  Calendar, 
  Users, 
  Target, 
  FileText,
  GitBranch,
  Activity,
  TrendingUp,
  AlertCircle
} from 'lucide-react';
import { api, type RepositoryDetail, type TestMethodDetail } from '../lib/api';
import BreadcrumbNavigation from '../components/shared/BreadcrumbNavigation';

const RepositoryDetailView: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [repository, setRepository] = useState<RepositoryDetail | null>(null);
  const [testMethods, setTestMethods] = useState<TestMethodDetail[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [scanning, setScanning] = useState(false);

  useEffect(() => {
    if (id) {
      fetchRepositoryDetails();
    }
  }, [id]);

  const fetchRepositoryDetails = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Fetch repository details
      const repoDetails = await api.dashboard.getRepositoryDetails();
      const repo = repoDetails.find(r => r.id.toString() === id);
      
      if (!repo) {
        setError('Repository not found');
        return;
      }
      
      setRepository(repo);
      
      // Fetch test methods for this repository
      const methods = await api.dashboard.getTestMethodDetails();
      const repoMethods = methods.filter(m => m.repository === repo.repository);
      setTestMethods(repoMethods);
      
    } catch (err) {
      console.error('Error fetching repository details:', err);
      setError('Failed to load repository details');
    } finally {
      setLoading(false);
    }
  };

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
          <p className="mt-2 text-gray-600">Loading repository details...</p>
        </div>
      </div>
    );
  }

  if (error || !repository) {
    return (
      <div className="p-8">
        <div className="card text-center py-12">
          <AlertCircle className="h-12 w-12 text-red-500 mx-auto mb-4" />
          <h2 className="text-xl font-bold text-gray-900 mb-2">Repository Not Found</h2>
          <p className="text-gray-600 mb-4">{error || 'The requested repository could not be found.'}</p>
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
            className="flex items-center text-gray-600 hover:text-gray-900"
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
            <h1 className="text-3xl font-bold text-gray-900 mb-2">{repository.repository}</h1>
            <div className="flex items-center text-gray-600 mb-4">
              <GitBranch className="h-4 w-4 mr-2" />
              <span className="font-mono text-sm">{repository.gitUrl}</span>
            </div>
            <div className="flex items-center text-gray-600">
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
            <div className="text-sm text-gray-600">Coverage Rate</div>
          </div>
        </div>
      </div>

      {/* Stats Overview */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div className="card">
          <div className="flex items-center">
            <FileText className="h-8 w-8 text-blue-600 mr-3" />
            <div>
              <p className="text-sm font-medium text-gray-600">Test Classes</p>
              <p className="text-2xl font-bold text-gray-900">{repository.testClasses}</p>
            </div>
          </div>
        </div>
        
        <div className="card">
          <div className="flex items-center">
            <Activity className="h-8 w-8 text-green-600 mr-3" />
            <div>
              <p className="text-sm font-medium text-gray-600">Test Methods</p>
              <p className="text-2xl font-bold text-gray-900">{repository.testMethodCount}</p>
            </div>
          </div>
        </div>
        
        <div className="card">
          <div className="flex items-center">
            <Target className="h-8 w-8 text-purple-600 mr-3" />
            <div>
              <p className="text-sm font-medium text-gray-600">Annotated Methods</p>
              <p className="text-2xl font-bold text-gray-900">{repository.annotatedMethods}</p>
            </div>
          </div>
        </div>
        
        <div className="card">
          <div className="flex items-center">
            <Calendar className="h-8 w-8 text-orange-600 mr-3" />
            <div>
              <p className="text-sm font-medium text-gray-600">Last Scan</p>
              <p className="text-sm font-bold text-gray-900">{formatDate(repository.lastScan)}</p>
            </div>
          </div>
        </div>
      </div>

      {/* Coverage Progress */}
      <div className="card mb-8">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Coverage Analysis</h3>
        <div className="space-y-4">
          <div>
            <div className="flex justify-between text-sm mb-1">
              <span className="text-gray-600">Overall Coverage</span>
              <span className="font-medium">{repository.coverageRate.toFixed(1)}%</span>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-3">
              <div 
                className="bg-orange-custom h-3 rounded-full transition-all duration-300" 
                style={{ width: `${repository.coverageRate}%` }}
              ></div>
            </div>
          </div>
          
          <div className="grid grid-cols-3 gap-4 text-center">
            <div>
              <div className="text-2xl font-bold text-blue-600">{repository.testClasses}</div>
              <div className="text-sm text-gray-600">Test Classes</div>
            </div>
            <div>
              <div className="text-2xl font-bold text-green-600">{repository.testMethodCount}</div>
              <div className="text-sm text-gray-600">Total Methods</div>
            </div>
            <div>
              <div className="text-2xl font-bold text-purple-600">{repository.annotatedMethods}</div>
              <div className="text-sm text-gray-600">Annotated</div>
            </div>
          </div>
        </div>
      </div>

      {/* Test Methods Table */}
      <div className="card">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-lg font-semibold text-gray-900">Test Methods</h3>
          <span className="text-sm text-gray-600">{testMethods.length} methods found</span>
        </div>
        
        {testMethods.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Test Class
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Method Name
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Status
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Author
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Target
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Last Modified
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {testMethods.map((method) => (
                  <tr key={method.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                      {method.testClass}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {method.testMethod}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                        method.status === 'PASS' ? 'bg-green-100 text-green-800' :
                        method.status === 'FAIL' ? 'bg-red-100 text-red-800' :
                        'bg-yellow-100 text-yellow-800'
                      }`}>
                        {method.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {method.author}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {method.targetClass}.{method.targetMethod}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {method.lastModified ? formatDate(method.lastModified) : 'N/A'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="text-center py-12">
            <FileText className="h-12 w-12 text-gray-400 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">No Test Methods Found</h3>
            <p className="text-gray-600">This repository doesn't have any test methods yet.</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default RepositoryDetailView;
