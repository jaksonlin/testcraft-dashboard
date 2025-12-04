import React, { useState, useEffect } from 'react';
import { ChevronRight, ChevronDown, Folder, FolderOpen, FileCode, Users, RefreshCw, AlertCircle } from 'lucide-react';
import { api, type HierarchyNode, type TestMethodDetail } from '../lib/api';

interface BreadcrumbItem {
  label: string;
  level: 'ROOT' | 'TEAM' | 'PACKAGE' | 'CLASS';
  teamName?: string;
  packageName?: string;
}

const TestMethodHierarchicalView: React.FC = () => {
  const [breadcrumbs, setBreadcrumbs] = useState<BreadcrumbItem[]>([
    { label: 'All Teams', level: 'ROOT' }
  ]);
  const [currentNodes, setCurrentNodes] = useState<HierarchyNode[]>([]);
  const [expandedNodes, setExpandedNodes] = useState<Set<string>>(new Set());
  const [methodsCache, setMethodsCache] = useState<Map<number, TestMethodDetail[]>>(new Map());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Load current level data
  const loadCurrentLevel = React.useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const currentLevel = breadcrumbs[breadcrumbs.length - 1];

      if (currentLevel.level === 'ROOT') {
        // Load teams
        const teams = await api.dashboard.getHierarchy('TEAM');
        setCurrentNodes(teams);
      } else if (currentLevel.level === 'TEAM' && currentLevel.teamName) {
        // Load packages within team
        const packages = await api.dashboard.getHierarchy('PACKAGE', currentLevel.teamName);
        setCurrentNodes(packages);
      } else if (currentLevel.level === 'PACKAGE' && currentLevel.teamName && currentLevel.packageName) {
        // Load classes within package
        const classes = await api.dashboard.getHierarchy('CLASS', currentLevel.teamName, currentLevel.packageName);
        setCurrentNodes(classes);
      }
    } catch (err) {
      console.error('Error loading hierarchy:', err);
      setError('Failed to load hierarchy data');
    } finally {
      setLoading(false);
    }
  }, [breadcrumbs]);

  useEffect(() => {
    loadCurrentLevel();
  }, [loadCurrentLevel]);

  // Navigate into a node
  const handleNodeClick = (node: HierarchyNode) => {
    if (node.type === 'TEAM') {
      setBreadcrumbs([
        ...breadcrumbs,
        { label: node.name, level: 'TEAM', teamName: node.name }
      ]);
    } else if (node.type === 'PACKAGE') {
      const currentTeam = breadcrumbs.find(b => b.level === 'TEAM');
      setBreadcrumbs([
        ...breadcrumbs,
        { label: node.name, level: 'PACKAGE', teamName: currentTeam?.teamName, packageName: node.name }
      ]);
    } else if (node.type === 'CLASS') {
      // Load methods for this class
      toggleClassMethods(node);
    }
  };

  // Toggle class methods expansion
  const toggleClassMethods = async (node: HierarchyNode) => {
    const nodeKey = `class-${node.id}`;

    if (expandedNodes.has(nodeKey)) {
      // Collapse
      const newExpanded = new Set(expandedNodes);
      newExpanded.delete(nodeKey);
      setExpandedNodes(newExpanded);
    } else {
      // Expand - load methods if not cached
      if (!methodsCache.has(node.id!)) {
        try {
          // Load methods for this class
          const currentTeam = breadcrumbs.find(b => b.level === 'TEAM');
          if (!currentTeam?.teamName) return;

          // For now, use the existing API to get methods by class
          // TODO: Add a specific endpoint for methods by class ID
          const allMethods = await api.dashboard.getTestMethodDetailsPaginated(
            0, 1000, undefined, currentTeam.teamName, undefined, undefined, node.fullName
          );

          const newCache = new Map(methodsCache);
          newCache.set(node.id!, allMethods.content);
          setMethodsCache(newCache);
        } catch (err) {
          console.error('Error loading methods:', err);
          return;
        }
      }

      const newExpanded = new Set(expandedNodes);
      newExpanded.add(nodeKey);
      setExpandedNodes(newExpanded);
    }
  };

  // Navigate back via breadcrumb
  const handleBreadcrumbClick = (index: number) => {
    setBreadcrumbs(breadcrumbs.slice(0, index + 1));
  };

  // Get icon for node type
  const getNodeIcon = (node: HierarchyNode, isExpanded: boolean) => {
    if (node.type === 'TEAM') {
      return <Users className="w-5 h-5 text-blue-600 dark:text-blue-400" />;
    } else if (node.type === 'PACKAGE') {
      return isExpanded
        ? <FolderOpen className="w-5 h-5 text-yellow-600 dark:text-yellow-400" />
        : <Folder className="w-5 h-5 text-yellow-600 dark:text-yellow-400" />;
    } else {
      return <FileCode className="w-5 h-5 text-green-600 dark:text-green-400" />;
    }
  };

  // Get coverage badge color
  const getCoverageBadgeColor = (coverage: number) => {
    if (coverage >= 80) return 'bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-300';
    if (coverage >= 50) return 'bg-yellow-100 dark:bg-yellow-900/30 text-yellow-800 dark:text-yellow-300';
    return 'bg-red-100 dark:bg-red-900/30 text-red-800 dark:text-red-300';
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center">
        <div className="text-center">
          <RefreshCw className="h-8 w-8 animate-spin mx-auto text-blue-600 dark:text-blue-400" />
          <p className="mt-2 text-gray-600 dark:text-gray-400">Loading hierarchy...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center">
        <div className="text-center">
          <AlertCircle className="h-12 w-12 text-red-500 mx-auto mb-4" />
          <h2 className="text-xl font-semibold text-gray-900 dark:text-gray-100 mb-2">Error</h2>
          <p className="text-gray-600 dark:text-gray-400 mb-4">{error}</p>
          <button
            onClick={loadCurrentLevel}
            className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      {/* Header */}
      <div className="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
                Test Methods Hierarchy
              </h1>
              <p className="text-sm text-gray-600 dark:text-gray-400">
                Navigate through your test suite by team, package, and class
              </p>
            </div>
            <button
              onClick={loadCurrentLevel}
              className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 flex items-center gap-2"
            >
              <RefreshCw className="w-4 h-4" />
              Refresh
            </button>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        {/* Breadcrumbs */}
        <div className="mb-6">
          <nav className="flex items-center space-x-2 text-sm">
            {breadcrumbs.map((crumb, index) => (
              <React.Fragment key={index}>
                {index > 0 && (
                  <ChevronRight className="w-4 h-4 text-gray-400" />
                )}
                <button
                  onClick={() => handleBreadcrumbClick(index)}
                  className={`px-3 py-1 rounded-md transition-colors ${index === breadcrumbs.length - 1
                      ? 'bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-300 font-medium'
                      : 'text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700'
                    }`}
                >
                  {crumb.label}
                </button>
              </React.Fragment>
            ))}
          </nav>
        </div>

        {/* Hierarchy Nodes */}
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow">
          {currentNodes.length === 0 ? (
            <div className="p-8 text-center text-gray-500 dark:text-gray-400">
              No items found at this level
            </div>
          ) : (
            <div className="divide-y divide-gray-200 dark:divide-gray-700">
              {currentNodes.map((node, index) => {
                const nodeKey = `${node.type}-${node.id || node.name}`;
                const isExpanded = expandedNodes.has(nodeKey);
                const methods = node.id ? methodsCache.get(node.id) : undefined;

                return (
                  <div key={index}>
                    {/* Node Row */}
                    <div
                      className="p-4 hover:bg-gray-50 dark:hover:bg-gray-700 cursor-pointer transition-colors"
                      onClick={() => handleNodeClick(node)}
                    >
                      <div className="flex items-center justify-between">
                        <div className="flex items-center space-x-3 flex-1">
                          {/* Expand/Collapse Icon (only for classes) */}
                          {node.type === 'CLASS' && (
                            <div className="w-5">
                              {isExpanded ? (
                                <ChevronDown className="w-4 h-4 text-gray-400" />
                              ) : (
                                <ChevronRight className="w-4 h-4 text-gray-400" />
                              )}
                            </div>
                          )}

                          {/* Node Icon */}
                          <div>
                            {getNodeIcon(node, isExpanded)}
                          </div>

                          {/* Node Name */}
                          <div className="flex-1">
                            <div className="font-medium text-gray-900 dark:text-white">
                              {node.name}
                            </div>
                            {node.code && (
                              <div className="text-xs text-gray-500 dark:text-gray-400">
                                {node.code}
                              </div>
                            )}
                          </div>

                          {/* Stats */}
                          <div className="flex items-center gap-4 text-sm">
                            {node.classCount !== undefined && (
                              <div className="text-gray-600 dark:text-gray-400">
                                <span className="font-medium">{node.classCount}</span> classes
                              </div>
                            )}
                            <div className="text-gray-600 dark:text-gray-400">
                              <span className="font-medium">{node.methodCount}</span> methods
                            </div>
                            <div className="text-gray-600 dark:text-gray-400">
                              <span className="font-medium">{node.annotatedCount}</span> annotated
                            </div>
                            <div className={`px-2 py-1 rounded text-xs font-semibold ${getCoverageBadgeColor(node.coverageRate)}`}>
                              {node.coverageRate.toFixed(1)}%
                            </div>
                          </div>

                          {/* Navigate Arrow */}
                          {node.type !== 'CLASS' && (
                            <ChevronRight className="w-5 h-5 text-gray-400" />
                          )}
                        </div>
                      </div>
                    </div>

                    {/* Expanded Methods (for CLASS nodes) */}
                    {node.type === 'CLASS' && isExpanded && methods && (
                      <div className="bg-gray-50 dark:bg-gray-900 border-t border-gray-200 dark:border-gray-700">
                        <div className="p-4 space-y-2">
                          {methods.map((method, methodIndex) => {
                            const isAnnotated = method.title && method.title.trim() !== '';

                            return (
                              <div
                                key={methodIndex}
                                className="flex items-center justify-between p-3 bg-white dark:bg-gray-800 rounded border border-gray-200 dark:border-gray-700"
                              >
                                <div className="flex items-center space-x-3 flex-1">
                                  <div className={`w-2 h-2 rounded-full ${isAnnotated ? 'bg-green-500' : 'bg-red-500'}`} />
                                  <code className="text-sm font-mono text-gray-900 dark:text-gray-100">
                                    {method.testMethod}()
                                  </code>
                                  {method.title && (
                                    <span className="text-sm text-gray-600 dark:text-gray-400">
                                      - {method.title}
                                    </span>
                                  )}
                                </div>
                                <div className="flex items-center gap-2 text-xs">
                                  {isAnnotated ? (
                                    <span className="px-2 py-1 bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-300 rounded">
                                      Annotated
                                    </span>
                                  ) : (
                                    <span className="px-2 py-1 bg-red-100 dark:bg-red-900/30 text-red-800 dark:text-red-300 rounded">
                                      Not Annotated
                                    </span>
                                  )}
                                </div>
                              </div>
                            );
                          })}
                        </div>
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* Summary Stats */}
        <div className="mt-6 bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
            Current Level Summary
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div>
              <p className="text-sm text-gray-600 dark:text-gray-400">Total Items</p>
              <p className="text-2xl font-bold text-gray-900 dark:text-white">
                {currentNodes.length}
              </p>
            </div>
            <div>
              <p className="text-sm text-gray-600 dark:text-gray-400">Total Methods</p>
              <p className="text-2xl font-bold text-gray-900 dark:text-white">
                {currentNodes.reduce((sum, n) => sum + n.methodCount, 0).toLocaleString()}
              </p>
            </div>
            <div>
              <p className="text-sm text-gray-600 dark:text-gray-400">Annotated Methods</p>
              <p className="text-2xl font-bold text-gray-900 dark:text-white">
                {currentNodes.reduce((sum, n) => sum + n.annotatedCount, 0).toLocaleString()}
              </p>
            </div>
            <div>
              <p className="text-sm text-gray-600 dark:text-gray-400">Average Coverage</p>
              <p className="text-2xl font-bold text-gray-900 dark:text-white">
                {currentNodes.length > 0
                  ? (currentNodes.reduce((sum, n) => sum + n.coverageRate, 0) / currentNodes.length).toFixed(1)
                  : '0.0'
                }%
              </p>
            </div>
          </div>
        </div>

        {/* Help Text */}
        <div className="mt-6 bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg p-4">
          <p className="text-sm text-blue-900 dark:text-blue-100">
            <strong>💡 Tip:</strong> Click on any {
              breadcrumbs[breadcrumbs.length - 1].level === 'ROOT' ? 'team' :
                breadcrumbs[breadcrumbs.length - 1].level === 'TEAM' ? 'package' :
                  breadcrumbs[breadcrumbs.length - 1].level === 'PACKAGE' ? 'class' : 'item'
            } to drill down. Use breadcrumbs to navigate back up the hierarchy.
          </p>
        </div>
      </div>
    </div>
  );
};

export default TestMethodHierarchicalView;

