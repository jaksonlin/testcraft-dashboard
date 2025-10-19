import React, { useState, useEffect } from 'react';
import { List, AlertTriangle, CheckCircle, Trash2 } from 'lucide-react';
import { TestCaseUploadModal } from '../components/testcases/TestCaseUploadModal';
import { TestCaseListTable } from '../components/testcases/TestCaseListTable';
import { TestCaseCoverageCard } from '../components/testcases/TestCaseCoverageCard';
import { TestCaseDetailModal } from '../components/testcases/TestCaseDetailModal';
import { StatsCard } from '../components/testcases/StatsCard';
import { TabNavigation } from '../components/testcases/TabNavigation';
import { CoverageBreakdown } from '../components/testcases/CoverageBreakdown';
import TestCasesHeader from '../components/testcases/TestCasesHeader';
import Pagination from '../components/shared/Pagination';
import { Toast } from '../components/shared/Toast';
import { useTestCaseData, type TabType, type TestCaseFilters } from '../hooks/useTestCaseData';
import type { TestCase, Team } from '../lib/testCaseApi';
import { getOrganizations, getTeams, deleteAllTestCases } from '../lib/testCaseApi';

/**
 * Main Test Cases view with tabs for list, coverage, and gaps.
 * Upload functionality is available via the header button.
 */
export const TestCasesView: React.FC = () => {
  const [activeTab, setActiveTab] = useState<TabType>('list');
  const [selectedTestCase, setSelectedTestCase] = useState<TestCase | null>(null);
  const [isUploadModalOpen, setIsUploadModalOpen] = useState(false);
  const [showToast, setShowToast] = useState(false);
  const [toastMessage, setToastMessage] = useState('');
  const [toastType, setToastType] = useState<'success' | 'error' | 'info'>('info');
  const [organizations, setOrganizations] = useState<string[]>([]);
  const [teams, setTeams] = useState<Team[]>([]);
  const [uiFilters, setUiFilters] = useState({
    organization: '',
    teamId: '',
    priority: '',
    type: '',
    status: '',
    search: ''
  });

  // Use the custom hook for data management
  const {
    testCases,
    untestedCases,
    coverageStats,
    listPagination,
    gapsPagination,
    loading,
    error,
    loadData,
    loadTestCases,
    loadGaps,
    handleDelete,
    setListPage,
    setListPageSize,
    setGapsPage,
    setGapsPageSize,
    setFilters,
    clearError,
  } = useTestCaseData();

  // Load organizations and teams on mount
  useEffect(() => {
    const loadFiltersData = async () => {
      try {
        const [orgs, teamsData] = await Promise.all([
          getOrganizations(),
          getTeams()
        ]);
        setOrganizations(orgs);
        setTeams(teamsData);
      } catch (error) {
        console.error('Failed to load filter data:', error);
      }
    };
    loadFiltersData();
  }, []);

  // Show error toast
  useEffect(() => {
    if (error) {
      setToastMessage(error);
      setToastType('error');
      setShowToast(true);
      clearError();
    }
  }, [error, clearError]);

  // Reload data when tab changes (not when functions are recreated)
  useEffect(() => {
    if (activeTab === 'list') {
      loadTestCases();
    } else if (activeTab === 'gaps') {
      loadGaps();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeTab]); // Only depend on activeTab, not the functions themselves

  const handleUploadComplete = () => {
    loadData();
    setToastMessage('Test cases uploaded successfully');
    setToastType('success');
    setShowToast(true);
  };

  const handleDeleteWithToast = async (internalId: number) => {
    try {
      await handleDelete(internalId);
      setToastMessage('Test case deleted successfully');
      setToastType('success');
      setShowToast(true);
    } catch {
      setToastMessage('Failed to delete test case');
      setToastType('error');
      setShowToast(true);
    }
  };

  const handleBulkDelete = async () => {
    // Check if any filters are active
    const hasActiveFilters = !!(
      uiFilters.organization ||
      uiFilters.teamId ||
      uiFilters.priority ||
      uiFilters.type ||
      uiFilters.status ||
      uiFilters.search
    );

    if (!hasActiveFilters) {
      setToastMessage('Please apply at least one filter before bulk delete');
      setToastType('error');
      setShowToast(true);
      return;
    }

    // Get count of filtered results
    const count = activeTab === 'list' 
      ? listPagination.totalPages * listPagination.pageSize 
      : coverageStats?.total || 0;

    // First confirmation
    const filterSummary = [
      uiFilters.organization && `Organization: ${uiFilters.organization}`,
      uiFilters.teamId && `Team ID: ${uiFilters.teamId}`,
      uiFilters.type && `Type: ${uiFilters.type}`,
      uiFilters.priority && `Priority: ${uiFilters.priority}`,
      uiFilters.status && `Status: ${uiFilters.status}`,
      uiFilters.search && `Search: ${uiFilters.search}`,
    ].filter(Boolean).join('\n');

    const confirmed = window.confirm(
      `⚠️ WARNING: PERMANENT DELETION\n\n` +
      `You are about to DELETE approximately ${count} test cases matching:\n\n` +
      `${filterSummary}\n\n` +
      `THIS CANNOT BE UNDONE!\n\n` +
      `Click OK to continue, or Cancel to abort.`
    );

    if (!confirmed) return;

    // Second confirmation (safety)
    const doubleConfirmed = window.confirm(
      `🚨 FINAL CONFIRMATION\n\n` +
      `This will PERMANENTLY DELETE test cases from the database.\n\n` +
      `Are you ABSOLUTELY SURE?\n\n` +
      `Click OK to DELETE, or Cancel to abort.`
    );

    if (!doubleConfirmed) return;

    try {
      // Execute bulk delete
      const result = await deleteAllTestCases(
        {
          organization: uiFilters.organization || undefined,
          teamId: uiFilters.teamId ? Number(uiFilters.teamId) : undefined,
          type: uiFilters.type || undefined,
          priority: uiFilters.priority || undefined,
          status: uiFilters.status || undefined,
          search: uiFilters.search || undefined,
        },
        true // confirm
      );

      setToastMessage(`Successfully deleted ${result.deleted} test case(s)`);
      setToastType('success');
      setShowToast(true);
      
      // Reload data
      loadData();
    } catch (error) {
      const err = error as { response?: { data?: { error?: string } }; message?: string };
      const errorMsg = err.response?.data?.error || err.message || 'Failed to delete test cases';
      setToastMessage(`Error: ${errorMsg}`);
      setToastType('error');
      setShowToast(true);
    }
  };

  const handlePageSizeChange = (size: number) => {
    if (activeTab === 'gaps') {
      setGapsPageSize(size);
    } else {
      setListPageSize(size);
    }
  };

  const currentPageSize = activeTab === 'gaps' ? gapsPagination.pageSize : listPagination.pageSize;

  // Handle filter changes - convert UI filters to backend filters
  const handleFilterChange = (newFilters: typeof uiFilters) => {
    setUiFilters(newFilters);
    
    // Convert to backend filter format
    const backendFilters: TestCaseFilters = {};
    if (newFilters.organization) backendFilters.organization = newFilters.organization;
    if (newFilters.teamId) backendFilters.teamId = Number(newFilters.teamId);
    if (newFilters.priority) backendFilters.priority = newFilters.priority;
    if (newFilters.type) backendFilters.type = newFilters.type;
    if (newFilters.status) backendFilters.status = newFilters.status;
    if (newFilters.search) backendFilters.search = newFilters.search;
    
    setFilters(backendFilters);
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="animate-spin rounded-full h-12 w-12 border-4 border-blue-600 border-t-transparent" />
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6">
      {/* Header with Data Controls */}
      <TestCasesHeader
        pageSize={currentPageSize}
        onPageSizeChange={handlePageSizeChange}
        onUploadClick={() => setIsUploadModalOpen(true)}
      />

      {/* Coverage Stats Cards */}
      {coverageStats && (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <TestCaseCoverageCard
            stats={coverageStats}
            onViewGaps={() => setActiveTab('gaps')}
          />

          <StatsCard
            title="Total Test Cases"
            value={coverageStats.total}
            description="Test cases in database"
            icon={List}
            iconColor="text-blue-600 dark:text-blue-400"
            valueColor="text-gray-900 dark:text-white"
          />

          <StatsCard
            title="Automation Gaps"
            value={coverageStats.manual}
            description="Test cases need automation"
            icon={AlertTriangle}
            iconColor="text-orange-600 dark:text-orange-400"
            valueColor="text-orange-600 dark:text-orange-400"
            action={
              coverageStats.manual > 0
                ? {
                    label: 'View gaps →',
                    onClick: () => setActiveTab('gaps'),
                  }
                : undefined
            }
          />
        </div>
      )}

      {/* Tab Navigation */}
      <TabNavigation
        activeTab={activeTab}
        onTabChange={setActiveTab}
        totalCount={coverageStats?.total || 0}
        gapsCount={coverageStats?.manual || 0}
      />

      {/* Tab Content */}
      <div>
        {activeTab === 'list' && (
          <div>
            <div className="mb-4">
              <h2 className="text-xl font-bold text-gray-900 dark:text-white">All Test Cases</h2>
              <p className="text-sm text-gray-600 dark:text-gray-400">View and manage all imported test cases</p>
            </div>
            
            {/* Bulk Delete Section - Only visible when filters are active */}
            {(uiFilters.organization || uiFilters.teamId || uiFilters.priority || 
              uiFilters.type || uiFilters.status || uiFilters.search) && (
              <div className="mb-4 p-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm font-semibold text-red-900 dark:text-red-200">
                      ⚠️ Bulk Actions Available
                    </p>
                    <p className="text-xs text-red-700 dark:text-red-300 mt-1">
                      {listPagination.totalPages * listPagination.pageSize} test cases match current filters
                    </p>
                  </div>
                  <button
                    onClick={handleBulkDelete}
                    className="px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg flex items-center gap-2 transition-colors"
                  >
                    <Trash2 className="w-4 h-4" />
                    Delete All Filtered Test Cases
                  </button>
                </div>
                <p className="text-xs text-red-600 dark:text-red-400 mt-2">
                  ⚠️ Warning: This action is permanent and cannot be undone. Always export data before bulk deletion.
                </p>
              </div>
            )}
            
            <TestCaseListTable
              testCases={testCases}
              filters={uiFilters}
              organizations={organizations}
              teams={teams}
              onFilterChange={handleFilterChange}
              onViewDetails={setSelectedTestCase}
              onDelete={handleDeleteWithToast}
            />
            <Pagination
              currentPage={listPagination.page}
              totalPages={listPagination.totalPages}
              onPageChange={setListPage}
            />
          </div>
        )}

        {activeTab === 'coverage' && coverageStats && (
          <div>
            <div className="mb-6">
              <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-2">Coverage Analytics</h2>
              <p className="text-sm text-gray-600 dark:text-gray-400">Test case automation coverage metrics</p>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <TestCaseCoverageCard
                stats={coverageStats}
                onViewGaps={() => setActiveTab('gaps')}
              />
              <CoverageBreakdown stats={coverageStats} />
            </div>
          </div>
        )}

        {activeTab === 'gaps' && (
          <div>
            <div className="mb-6">
              <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-2">Automation Gaps</h2>
              <p className="text-sm text-gray-600 dark:text-gray-400">
                Test cases that need automation ({coverageStats?.manual || 0} total)
              </p>
            </div>

            {untestedCases.length === 0 ? (
              <div className="bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 rounded-lg p-8 text-center">
                <CheckCircle className="w-16 h-16 text-green-600 dark:text-green-400 mx-auto mb-4" />
                <h3 className="text-xl font-semibold text-green-900 dark:text-green-100 mb-2">
                  Excellent! No gaps found
                </h3>
                <p className="text-green-700 dark:text-green-300">
                  All test cases are automated with test methods
                </p>
              </div>
            ) : (
              <>
                <TestCaseListTable
                  testCases={untestedCases}
                  filters={uiFilters}
                  organizations={organizations}
                  teams={teams}
                  onFilterChange={handleFilterChange}
                  onViewDetails={setSelectedTestCase}
                />
                <Pagination
                  currentPage={gapsPagination.page}
                  totalPages={gapsPagination.totalPages}
                  onPageChange={setGapsPage}
                />
              </>
            )}
          </div>
        )}
      </div>

      {/* Detail Modal */}
      {selectedTestCase && (
        <TestCaseDetailModal
          testCase={selectedTestCase}
          onClose={() => setSelectedTestCase(null)}
        />
      )}

      {/* Upload Modal */}
      <TestCaseUploadModal
        isOpen={isUploadModalOpen}
        onClose={() => setIsUploadModalOpen(false)}
        onComplete={handleUploadComplete}
      />

      {/* Toast Notification */}
      {showToast && (
        <Toast
          message={toastMessage}
          type={toastType}
          onClose={() => setShowToast(false)}
        />
      )}
    </div>
  );
};