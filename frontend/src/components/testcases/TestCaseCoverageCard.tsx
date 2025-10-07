import React from 'react';
import { CheckCircle, XCircle, TrendingUp, AlertTriangle } from 'lucide-react';
import { CoverageStats } from '../../lib/testCaseApi';

interface TestCaseCoverageCardProps {
  stats: CoverageStats;
  onViewGaps?: () => void;
}

/**
 * Dashboard card showing test case coverage statistics
 */
export const TestCaseCoverageCard: React.FC<TestCaseCoverageCardProps> = ({ stats, onViewGaps }) => {
  const coveragePercentage = stats.coveragePercentage;
  const automationRate = stats.total > 0 ? (stats.automated / stats.total) * 100 : 0;

  const getCoverageColor = () => {
    if (coveragePercentage >= 80) return 'text-green-600';
    if (coveragePercentage >= 50) return 'text-yellow-600';
    return 'text-red-600';
  };

  const getCoverageBarColor = () => {
    if (coveragePercentage >= 80) return 'bg-green-600';
    if (coveragePercentage >= 50) return 'bg-yellow-600';
    return 'bg-red-600';
  };

  return (
    <div className="bg-white rounded-lg shadow-lg p-6">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-semibold text-gray-900">Test Case Coverage</h3>
        <TrendingUp className="w-5 h-5 text-blue-600" />
      </div>

      {/* Coverage Percentage */}
      <div className="mb-6">
        <div className="flex items-baseline gap-2 mb-2">
          <span className={`text-4xl font-bold ${getCoverageColor()}`}>
            {coveragePercentage.toFixed(1)}%
          </span>
          <span className="text-gray-600">automated</span>
        </div>

        {/* Progress Bar */}
        <div className="w-full bg-gray-200 rounded-full h-3">
          <div
            className={`h-3 rounded-full transition-all duration-500 ${getCoverageBarColor()}`}
            style={{ width: `${Math.min(coveragePercentage, 100)}%` }}
          />
        </div>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-3 gap-4 mb-4">
        <div className="text-center p-3 bg-gray-50 rounded-lg">
          <div className="text-2xl font-bold text-gray-900">{stats.total}</div>
          <div className="text-xs text-gray-600">Total</div>
        </div>

        <div className="text-center p-3 bg-green-50 rounded-lg">
          <div className="text-2xl font-bold text-green-600">{stats.automated}</div>
          <div className="text-xs text-gray-600">Automated</div>
        </div>

        <div className="text-center p-3 bg-orange-50 rounded-lg">
          <div className="text-2xl font-bold text-orange-600">{stats.manual}</div>
          <div className="text-xs text-gray-600">Manual</div>
        </div>
      </div>

      {/* Automation vs Manual */}
      <div className="space-y-2 mb-4">
        <div className="flex items-center justify-between text-sm">
          <div className="flex items-center gap-2">
            <CheckCircle className="w-4 h-4 text-green-600" />
            <span className="text-gray-700">Automated</span>
          </div>
          <span className="font-semibold text-gray-900">{stats.automated} ({automationRate.toFixed(1)}%)</span>
        </div>

        <div className="flex items-center justify-between text-sm">
          <div className="flex items-center gap-2">
            <XCircle className="w-4 h-4 text-orange-600" />
            <span className="text-gray-700">Manual</span>
          </div>
          <span className="font-semibold text-gray-900">{stats.manual} ({(100 - automationRate).toFixed(1)}%)</span>
        </div>
      </div>

      {/* Gap Alert */}
      {stats.manual > 0 && (
        <div className="bg-orange-50 border border-orange-200 rounded-lg p-3">
          <div className="flex items-start gap-2">
            <AlertTriangle className="w-4 h-4 text-orange-600 mt-0.5" />
            <div className="flex-1">
              <p className="text-sm font-semibold text-orange-900">
                {stats.manual} test cases need automation
              </p>
              {onViewGaps && (
                <button
                  onClick={onViewGaps}
                  className="text-xs text-orange-700 hover:text-orange-900 underline mt-1"
                >
                  View gap list →
                </button>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

