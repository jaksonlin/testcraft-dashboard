import React, { useState, useEffect } from 'react';
import { Upload, List, BarChart3, AlertTriangle, CheckCircle } from 'lucide-react';
import { TestCaseUploadWizard } from '../components/testcases/TestCaseUploadWizard';
import { TestCaseListTable } from '../components/testcases/TestCaseListTable';
import { TestCaseCoverageCard } from '../components/testcases/TestCaseCoverageCard';
import { TestCaseDetailModal } from '../components/testcases/TestCaseDetailModal';
import {
  getAllTestCases,
  getCoverageStats,
  getUntestedCases,
  deleteTestCase
} from '../lib/testCaseApi';
import type { TestCase, CoverageStats } from '../lib/testCaseApi';
import type { PageResponse } from '../lib/testCaseApi';

type TabType = 'upload' | 'list' | 'coverage' | 'gaps';

/**
 * Main Test Cases view with tabs for upload, list, coverage, and gaps
 */
export const TestCasesView: React.FC = () => {
  const [activeTab, setActiveTab] = useState<TabType>('list');
  const [testCases, setTestCases] = useState<TestCase[]>([]);
  const [coverageStats, setCoverageStats] = useState<CoverageStats | null>(null);
  const [untestedCases, setUntestedCases] = useState<TestCase[]>([]);
  const [totalCases, setTotalCases] = useState(0);
  const [totalGaps, setTotalGaps] = useState(0);
  const [selectedTestCase, setSelectedTestCase] = useState<TestCase | null>(null);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [gapsPage, setGapsPage] = useState(0);
  const [gapsSize, setGapsSize] = useState(20);

  // Load data on mount
  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      const [testCasesData, stats, gaps] = await Promise.all([
        getAllTestCases({ page, size }),
        getCoverageStats(),
        getUntestedCases({ page: gapsPage, size: gapsSize })
      ]);

      setTestCases(testCasesData.content);
      setTotalCases(testCasesData.total);
      setCoverageStats(stats);
      setUntestedCases(gaps.content);
      setTotalGaps(gaps.total);
    } catch (error) {
      console.error('Failed to load test cases:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleUploadComplete = () => {
    // Reload data after upload
    loadData();
    // Switch to list view
    setActiveTab('list');
  };

  const handleDelete = async (id: string) => {
    try {
      await deleteTestCase(id);
      // Reload data
      loadData();
    } catch (error) {
      console.error('Failed to delete test case:', error);
      alert('Failed to delete test case');
    }
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
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold text-gray-900 mb-2">Test Case Management</h1>
        <p className="text-gray-600">
          Upload test case designs and track automation coverage
        </p>
      </div>

      {/* Coverage Card (Always Visible) */}
      {coverageStats && (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <TestCaseCoverageCard
            stats={coverageStats}
            onViewGaps={() => setActiveTab('gaps')}
          />

          {/* Quick Stats Cards */}
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-900">Total Test Cases</h3>
              <List className="w-5 h-5 text-blue-600" />
            </div>
            <div className="text-4xl font-bold text-gray-900">{coverageStats.total}</div>
            <p className="text-sm text-gray-600 mt-2">Test cases in database</p>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-900">Automation Gaps</h3>
              <AlertTriangle className="w-5 h-5 text-orange-600" />
            </div>
            <div className="text-4xl font-bold text-orange-600">{coverageStats.manual}</div>
            <p className="text-sm text-gray-600 mt-2">Test cases need automation</p>
            {coverageStats.manual > 0 && (
              <button
                onClick={() => setActiveTab('gaps')}
                className="mt-3 text-sm text-orange-700 hover:text-orange-900 underline"
              >
                View gaps →
              </button>
            )}
          </div>
        </div>
      )}

      {/* Tab Navigation */}
      <div className="border-b border-gray-200">
        <nav className="flex space-x-8">
          <button
            onClick={() => setActiveTab('upload')}
            className={`py-4 px-1 border-b-2 font-medium text-sm transition-colors flex items-center gap-2 ${
              activeTab === 'upload'
                ? 'border-blue-600 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            <Upload className="w-4 h-4" />
            Upload Test Cases
          </button>

          <button
            onClick={() => setActiveTab('list')}
            className={`py-4 px-1 border-b-2 font-medium text-sm transition-colors flex items-center gap-2 ${
              activeTab === 'list'
                ? 'border-blue-600 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            <List className="w-4 h-4" />
            All Test Cases ({totalCases})
          </button>

          <button
            onClick={() => setActiveTab('coverage')}
            className={`py-4 px-1 border-b-2 font-medium text-sm transition-colors flex items-center gap-2 ${
              activeTab === 'coverage'
                ? 'border-blue-600 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            <BarChart3 className="w-4 h-4" />
            Coverage Analytics
          </button>

          <button
            onClick={() => setActiveTab('gaps')}
            className={`py-4 px-1 border-b-2 font-medium text-sm transition-colors flex items-center gap-2 ${
              activeTab === 'gaps'
                ? 'border-blue-600 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            <AlertTriangle className="w-4 h-4" />
            Automation Gaps ({totalGaps})
          </button>
        </nav>
      </div>

      {/* Tab Content */}
      <div>
        {activeTab === 'upload' && (
          <TestCaseUploadWizard
            onComplete={handleUploadComplete}
          />
        )}

        {activeTab === 'list' && (
          <div>
            <div className="mb-4">
              <h2 className="text-xl font-bold text-gray-900">All Test Cases</h2>
              <p className="text-sm text-gray-600">View and manage all imported test cases</p>
            </div>
            <TestCaseListTable
              testCases={testCases}
              onViewDetails={setSelectedTestCase}
              onDelete={handleDelete}
            />
            <div className="flex items-center justify-between mt-4">
              <div className="text-sm text-gray-600">Page {page + 1}</div>
              <div className="flex gap-2">
                <button className="px-3 py-1 bg-gray-100 rounded disabled:opacity-50" disabled={page === 0} onClick={() => { setPage(Math.max(0, page - 1)); loadData(); }}>Prev</button>
                <button className="px-3 py-1 bg-gray-100 rounded" onClick={() => { setPage(page + 1); loadData(); }}>Next</button>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'coverage' && coverageStats && (
          <div>
            <div className="mb-6">
              <h2 className="text-xl font-bold text-gray-900 mb-2">Coverage Analytics</h2>
              <p className="text-sm text-gray-600">Test case automation coverage metrics</p>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <TestCaseCoverageCard
                stats={coverageStats}
                onViewGaps={() => setActiveTab('gaps')}
              />

              {/* Additional Analytics Could Go Here */}
              <div className="bg-white rounded-lg shadow p-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">Coverage Breakdown</h3>
                <div className="space-y-4">
                  <div>
                    <div className="flex justify-between text-sm mb-1">
                      <span className="text-gray-700">Automated</span>
                      <span className="font-semibold text-green-600">{coverageStats.automated}</span>
                    </div>
                    <div className="w-full bg-gray-200 rounded-full h-2">
                      <div
                        className="bg-green-600 h-2 rounded-full transition-all"
                        style={{ width: `${(coverageStats.automated / coverageStats.total) * 100}%` }}
                      />
                    </div>
                  </div>

                  <div>
                    <div className="flex justify-between text-sm mb-1">
                      <span className="text-gray-700">Manual</span>
                      <span className="font-semibold text-orange-600">{coverageStats.manual}</span>
                    </div>
                    <div className="w-full bg-gray-200 rounded-full h-2">
                      <div
                        className="bg-orange-600 h-2 rounded-full transition-all"
                        style={{ width: `${(coverageStats.manual / coverageStats.total) * 100}%` }}
                      />
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'gaps' && (
          <div>
            <div className="mb-6">
              <h2 className="text-xl font-bold text-gray-900 mb-2">Automation Gaps</h2>
              <p className="text-sm text-gray-600">
                Test cases that need automation ({untestedCases.length} total)
              </p>
            </div>

            {untestedCases.length === 0 ? (
              <div className="bg-green-50 border border-green-200 rounded-lg p-8 text-center">
                <CheckCircle className="w-16 h-16 text-green-600 mx-auto mb-4" />
                <h3 className="text-xl font-semibold text-green-900 mb-2">
                  Excellent! No gaps found
                </h3>
                <p className="text-green-700">
                  All test cases are automated with test methods
                </p>
              </div>
            ) : (
              <TestCaseListTable
                testCases={untestedCases}
                onViewDetails={setSelectedTestCase}
              />
            )}
            {untestedCases.length > 0 && (
              <div className="flex items-center justify-between mt-4">
                <div className="text-sm text-gray-600">Page {gapsPage + 1}</div>
                <div className="flex gap-2">
                  <button className="px-3 py-1 bg-gray-100 rounded disabled:opacity-50" disabled={gapsPage === 0} onClick={() => { setGapsPage(Math.max(0, gapsPage - 1)); loadData(); }}>Prev</button>
                  <button className="px-3 py-1 bg-gray-100 rounded" onClick={() => { setGapsPage(gapsPage + 1); loadData(); }}>Next</button>
                </div>
              </div>
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
    </div>
  );
};

