import React, { useState, useEffect } from 'react';
import { List, AlertTriangle, CheckCircle } from 'lucide-react';
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
import { useTestCaseData, type TabType } from '../hooks/useTestCaseData';
import type { TestCase } from '../lib/testCaseApi';

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
    clearError,
  } = useTestCaseData();

  // Show error toast
  useEffect(() => {
    if (error) {
      setToastMessage(error);
      setToastType('error');
      setShowToast(true);
      clearError();
    }
  }, [error, clearError]);

  // Reload data when tab changes
  useEffect(() => {
    if (activeTab === 'list') {
      loadTestCases();
    } else if (activeTab === 'gaps') {
      loadGaps();
    }
  }, [activeTab, loadTestCases, loadGaps]);

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

  const handlePageSizeChange = (size: number) => {
    if (activeTab === 'gaps') {
      setGapsPageSize(size);
    } else {
      setListPageSize(size);
    }
  };

  const currentPageSize = activeTab === 'gaps' ? gapsPagination.pageSize : listPagination.pageSize;

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
            <TestCaseListTable
              testCases={testCases}
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