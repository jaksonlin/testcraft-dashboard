import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useParams } from 'react-router-dom';
import { AlertCircle } from 'lucide-react';
import { api, type RepositoryDetail, type TestMethodDetail, type TestClassSummary, type PagedResponse } from '../lib/api';
import BreadcrumbNavigation from '../components/shared/BreadcrumbNavigation';
import RepositoryHeader from '../components/repository-detail/RepositoryHeader';
import RepositoryStats from '../components/repository-detail/RepositoryStats';
import ClassesTab from '../components/repository-detail/ClassesTab';
import MethodsTab from '../components/repository-detail/MethodsTab';

const RepositoryDetailView: React.FC = () => {
  const { id } = useParams<{ id: string }>();
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
  
  // Ref to track if initial load has completed to prevent duplicate API calls
  const initialLoadCompleted = useRef(false);
  
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
      initialLoadCompleted.current = false;
      fetchRepositoryDetails();
    }
  }, [id, fetchRepositoryDetails]);

  // Fetch classes when pagination parameters change (only when user interacts with filters/pagination, not on initial load)
  useEffect(() => {
    if (id && repository && initialLoadCompleted.current) {
      // Only fetch if this is not the initial load (initial load is handled by fetchRepositoryDetails)
      fetchClassesPaginated();
    }
    
    // Mark initial load as completed after first fetch
    if (repository && !initialLoadCompleted.current) {
      initialLoadCompleted.current = true;
    }
  }, [id, repository, classesPage, classesPageSize, classesSearchTerm, classesAnnotatedFilter, fetchClassesPaginated]);

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

  const handleSearchChange = (value: string) => {
    setClassesSearchTerm(value);
    setClassesPage(0); // Reset to first page when searching
  };

  const handleAnnotatedFilterChange = (value: 'all' | 'annotated' | 'not-annotated') => {
    setClassesAnnotatedFilter(value);
    setClassesPage(0); // Reset to first page when filtering
  };

  const handlePageSizeChange = (value: number) => {
    setClassesPageSize(value);
    setClassesPage(0); // Reset to first page when changing page size
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
        </div>
      </div>
    );
  }

  return (
    <div className="p-8">
      {/* Breadcrumb Navigation */}
      <BreadcrumbNavigation />
      
      {/* Header */}
      <RepositoryHeader
        repository={repository}
        scanning={scanning}
        onScan={handleScanRepository}
        onExport={handleExportData}
      />

      {/* Stats */}
      <RepositoryStats repository={repository} />

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

      {/* Tab Content */}
      {activeTab === 'classes' && (
        <ClassesTab
          classes={classes}
          classesPagination={classesPagination}
          searchTerm={classesSearchTerm}
          onSearchChange={handleSearchChange}
          annotatedFilter={classesAnnotatedFilter}
          onAnnotatedFilterChange={handleAnnotatedFilterChange}
          pageSize={classesPageSize}
          onPageSizeChange={handlePageSizeChange}
          currentPage={classesPage}
          onPageChange={setClassesPage}
          onSelectClass={handleSelectClass}
        />
      )}

      {activeTab === 'methods' && (
        <MethodsTab
          methods={testMethods}
          selectedClassName={selectedClassName}
        />
      )}
    </div>
  );
};

export default RepositoryDetailView;
