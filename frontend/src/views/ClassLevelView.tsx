import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { FileText, AlertCircle } from 'lucide-react';
import { api, type TestMethodDetail } from '../lib/api';
import BreadcrumbNavigation from '../components/shared/BreadcrumbNavigation';
import ClassLevelHeader from '../components/class-level/ClassLevelHeader';
import ClassLevelStats from '../components/class-level/ClassLevelStats';
import ExpandableClassCard from '../components/class-level/ExpandableClassCard';
import { useClassGrouping } from '../hooks/useClassGrouping';

const ClassLevelView: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [methods, setMethods] = useState<TestMethodDetail[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [expandedClasses, setExpandedClasses] = useState<Set<string>>(new Set());
  const [repositoryName, setRepositoryName] = useState<string>('');

  // Use custom hook for grouping methods by class
  const classGroups = useClassGrouping(methods);

  const fetchClassData = useCallback(async () => {
    if (!id) return;
    
    try {
      setLoading(true);
      setError(null);
      
      // Fetch test methods for this repository using repository API
      const fetchedMethods = await api.repositories.getTestMethods(parseInt(id));
      
      if (fetchedMethods.length === 0) {
        setError('No test methods found for this repository');
        return;
      }
      
      setRepositoryName(fetchedMethods[0]?.repository || 'Unknown Repository');
      setMethods(fetchedMethods);
      
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

  // Calculate total stats
  const totalMethods = classGroups.reduce((sum, c) => sum + c.methodCount, 0);
  const totalAnnotated = classGroups.reduce((sum, c) => sum + c.annotatedCount, 0);

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
      <ClassLevelHeader
        repositoryName={repositoryName}
        totalClasses={classGroups.length}
        onExpandAll={expandAllClasses}
        onCollapseAll={collapseAllClasses}
      />

      {/* Summary Stats */}
      <ClassLevelStats
        totalClasses={classGroups.length}
        totalMethods={totalMethods}
        totalAnnotated={totalAnnotated}
      />

      {/* Class List */}
      <div className="space-y-4">
        {classGroups.map((classGroup) => (
          <ExpandableClassCard
            key={classGroup.className}
            classGroup={classGroup}
            isExpanded={expandedClasses.has(classGroup.className)}
            onToggle={() => toggleClassExpansion(classGroup.className)}
            onExport={() => handleExportClassData(classGroup.className)}
          />
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
