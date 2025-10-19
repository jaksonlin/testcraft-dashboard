/**
 * Preview Step Component - Preview data before import
 */

import React, { useState, useEffect } from 'react';
import { CheckCircle, ArrowLeft, Building, Users } from 'lucide-react';
import type { PreviewStepProps } from './types';
import { calculateEstimatedImportCount } from './utils';
import { getOrganizations, getTeams } from '../../../lib/testCaseApi';
import type { Team } from '../../../lib/testCaseApi';

export const PreviewStep: React.FC<PreviewStepProps> = ({
  preview,
  mappings,
  headerRow,
  dataStartRow,
  importing,
  organization,
  teamId,
  onOrganizationChange,
  onTeamIdChange,
  onImport,
  onBack
}) => {
  const [organizations, setOrganizations] = useState<string[]>([]);
  const [teams, setTeams] = useState<Team[]>([]);
  const [loading, setLoading] = useState(true);

  // Check if organization/team are mapped in Excel
  const hasOrganizationMapping = Object.values(mappings).includes('organization');
  const hasTeamMapping = Object.values(mappings).includes('team');

  // Load organizations and teams on mount
  useEffect(() => {
    const loadFilterData = async () => {
      try {
        const [orgs, teamsData] = await Promise.all([
          getOrganizations(),
          getTeams()
        ]);
        setOrganizations(orgs);
        setTeams(teamsData);
      } catch (error) {
        console.error('Failed to load organizations and teams:', error);
      } finally {
        setLoading(false);
      }
    };
    loadFilterData();
  }, []);

  const estimatedImportCount = calculateEstimatedImportCount(preview.totalRows, dataStartRow);
  
  // Map preview data using user's mappings
  const mappedPreview = preview.previewData.map((row: Record<string, string>, index: number) => {
    const mapped: Record<string, string> = {};
    Object.entries(mappings).forEach(([excelCol, systemField]) => {
      if (systemField !== 'ignore') {
        mapped[systemField] = row[excelCol] || '';
      }
    });
    
    return { ...mapped, _isHeader: index === 0 } as Record<string, string> & { _isHeader: boolean };
  });
  
  const headerRowData = mappedPreview[0];
  const dataRows = mappedPreview.slice(1);

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-2">Preview Import</h2>
        <p className="text-gray-600 dark:text-gray-400">
          Select organization and team, then review the mapped data before importing.
        </p>
      </div>

      {/* Organization and Team Selection */}
      <div className="bg-white dark:bg-gray-800 border-2 border-gray-300 dark:border-gray-600 rounded-lg p-6">
        <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Test Case Metadata</h3>
        <div className="grid grid-cols-2 gap-4">
          {/* Organization Selection/Input */}
          <div>
            <label className="flex items-center gap-2 text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              <Building className="w-4 h-4" />
              Organization {!hasOrganizationMapping && <span className="text-red-500">*</span>}
            </label>
            
            {hasOrganizationMapping ? (
              <div className="w-full px-3 py-2 border border-green-300 dark:border-green-600 bg-green-50 dark:bg-green-900/20 text-green-900 dark:text-green-100 rounded-lg">
                ✓ Using values from Excel
              </div>
            ) : (
              <>
                <input
                  type="text"
                  list="organizations-list"
                  value={organization}
                  onChange={(e) => onOrganizationChange(e.target.value)}
                  disabled={loading}
                  placeholder={organizations.length > 0 ? "Select or type organization" : "Type organization name"}
                  className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:opacity-50"
                />
                <datalist id="organizations-list">
                  {organizations.map(org => (
                    <option key={org} value={org} />
                  ))}
                </datalist>
              </>
            )}
            
            {!hasOrganizationMapping && !organization ? (
              <p className="mt-1 text-xs text-red-600 dark:text-red-400">Organization is required</p>
            ) : !hasOrganizationMapping && organizations.length === 0 && organization ? (
              <p className="mt-1 text-xs text-blue-600 dark:text-blue-400">
                New organization "{organization}" will be created
              </p>
            ) : hasOrganizationMapping ? (
              <p className="mt-1 text-xs text-green-600 dark:text-green-400">
                Organization values will be taken from your Excel column mapping
              </p>
            ) : null}
          </div>

          {/* Team Selection */}
          <div>
            <label className="flex items-center gap-2 text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              <Users className="w-4 h-4" />
              Team <span className="text-gray-400">(optional)</span>
            </label>
            
            {hasTeamMapping ? (
              <div className="w-full px-3 py-2 border border-green-300 dark:border-green-600 bg-green-50 dark:bg-green-900/20 text-green-900 dark:text-green-100 rounded-lg">
                ✓ Using values from Excel
              </div>
            ) : (
              <select
                value={teamId}
                onChange={(e) => onTeamIdChange(e.target.value)}
                disabled={loading}
                className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:opacity-50"
              >
                <option value="">No Team (or from Excel)</option>
                {teams.map(team => (
                  <option key={team.id} value={team.id}>{team.name}</option>
                ))}
              </select>
            )}
            
            <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">
              {hasTeamMapping 
                ? 'Team values will be taken from your Excel column mapping'
                : teamId 
                  ? 'Selected team will override Excel team column' 
                  : 'Team can be specified in Excel or left unassigned'
              }
            </p>
          </div>
        </div>
      </div>

      {/* Import Info */}
      <div className="bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg p-4">
        <div className="flex items-center justify-between">
          <div>
            <p className="font-semibold text-blue-900 dark:text-blue-100">Ready to import</p>
            <p className="text-sm text-blue-700 dark:text-blue-300">
              Estimated {estimatedImportCount} test cases will be imported
            </p>
            <p className="text-xs text-blue-600 dark:text-blue-400 mt-1">
              Using Header Row {headerRow} (Excel Row {headerRow + 1}), Data from Row {dataStartRow} to {preview.totalRows - 1}
            </p>
            {hasOrganizationMapping && (
              <p className="text-xs text-green-600 dark:text-green-400 mt-1 font-medium">
                ✓ Organizations will be taken from Excel column
              </p>
            )}
            {hasTeamMapping && (
              <p className="text-xs text-green-600 dark:text-green-400 mt-1 font-medium">
                ✓ Teams will be taken from Excel column
              </p>
            )}
          </div>
          <CheckCircle className="w-8 h-8 text-blue-600 dark:text-blue-400" />
        </div>
      </div>

      {/* Debug Info */}
      <DebugInfo 
        preview={preview} 
        mappings={mappings} 
        mappedPreview={mappedPreview}
        dataRows={dataRows}
        hasOrganizationMapping={hasOrganizationMapping}
        hasTeamMapping={hasTeamMapping}
        organization={organization}
        teamId={teamId}
      />

      {/* Preview Table */}
      <PreviewTable 
        headerRowData={headerRowData} 
        dataRows={dataRows} 
      />

      {/* Actions */}
      <div className="flex justify-between items-center pt-6 border-t-2 border-gray-200 dark:border-gray-700 mt-6">
        <button
          onClick={onBack}
          disabled={importing}
          className="px-6 py-2.5 text-gray-700 dark:text-gray-300 bg-gray-100 dark:bg-gray-700 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors flex items-center gap-2 disabled:opacity-50 font-medium"
        >
          <ArrowLeft className="w-4 h-4" />
          Back
        </button>
        
        <button
          onClick={onImport}
          disabled={importing || (!hasOrganizationMapping && !organization)}
          className="px-10 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed font-semibold shadow-lg hover:shadow-xl text-base"
          title={!hasOrganizationMapping && !organization ? 'Please select an organization' : ''}
        >
          {importing ? (
            <>
              <div className="animate-spin rounded-full h-5 w-5 border-2 border-white border-t-transparent" />
              Importing...
            </>
          ) : (
            <>
              Import {estimatedImportCount} Test Cases
              <CheckCircle className="w-5 h-5" />
            </>
          )}
        </button>
      </div>
    </div>
  );
};

// Sub-components

const DebugInfo: React.FC<{
  preview: { columns: string[]; previewData: Record<string, string>[] };
  mappings: Record<string, string>;
  mappedPreview: Record<string, string>[];
  dataRows: Record<string, string>[];
  hasOrganizationMapping: boolean;
  hasTeamMapping: boolean;
  organization: string;
  teamId: string;
}> = ({ preview, mappings, mappedPreview, dataRows, hasOrganizationMapping, hasTeamMapping, organization, teamId }) => (
  <div className="bg-gray-100 dark:bg-gray-800 border border-gray-300 dark:border-gray-700 rounded-lg p-4">
    <details className="cursor-pointer">
      <summary className="font-semibold text-gray-700 dark:text-gray-300 text-sm">🔍 Debug Info (Click to expand)</summary>
      <div className="mt-3 space-y-2 text-xs font-mono text-gray-900 dark:text-gray-100">
        <div>
          <span className="font-bold">Available Columns:</span> {preview.columns.join(', ')}
        </div>
        <div>
          <span className="font-bold">Preview Data Rows:</span> {preview.previewData.length} (header + data rows)
        </div>
        <div>
          <span className="font-bold">Mapped Preview Rows:</span> {mappedPreview.length}
        </div>
        <div>
          <span className="font-bold">Data Rows to Display:</span> {dataRows.length}
        </div>
        <div className="pt-2 border-t border-gray-300 dark:border-gray-600">
          <span className="font-bold">Smart Logic Status:</span>
          <div className="pl-4 mt-1 space-y-1">
            <div>hasOrganizationMapping: <span className={hasOrganizationMapping ? "text-green-600" : "text-red-600"}>{String(hasOrganizationMapping)}</span></div>
            <div>hasTeamMapping: <span className={hasTeamMapping ? "text-green-600" : "text-red-600"}>{String(hasTeamMapping)}</span></div>
            <div>organization value: "{organization}"</div>
            <div>teamId value: "{teamId}"</div>
            <div className="pt-1 border-t border-gray-400">
              Button enabled: <span className={hasOrganizationMapping || organization ? "text-green-600 font-bold" : "text-red-600 font-bold"}>
                {hasOrganizationMapping || organization ? "YES" : "NO (need org)"}
              </span>
            </div>
          </div>
        </div>
        <div className="pt-2 border-t border-gray-300 dark:border-gray-600">
          <span className="font-bold">Current Mappings:</span>
          <div className="pl-4 mt-1">
            {Object.entries(mappings).map(([excel, system]) => (
              <div key={excel}>{excel} → {system}</div>
            ))}
          </div>
        </div>
        {dataRows.length > 0 && (
          <div className="pt-2 border-t border-gray-300 dark:border-gray-600">
            <span className="font-bold">First Data Row Sample:</span>
            <pre className="mt-1 p-2 bg-white dark:bg-gray-900 rounded text-[10px] overflow-auto">
              {JSON.stringify(dataRows[0], null, 2)}
            </pre>
          </div>
        )}
      </div>
    </details>
  </div>
);

const PreviewTable: React.FC<{
  headerRowData: Record<string, string>;
  dataRows: Record<string, string>[];
}> = ({ headerRowData, dataRows }) => (
  <div className="bg-gray-50 dark:bg-gray-900 rounded-lg p-4">
    <div className="overflow-x-auto border border-gray-200 dark:border-gray-700 rounded-lg">
      <table className="min-w-full text-sm">
        <thead>
          <tr className="border-b-2 border-gray-300 dark:border-gray-600 bg-gray-100 dark:bg-gray-800">
            <th className="px-4 py-2.5 text-left font-semibold text-gray-700 dark:text-gray-300 whitespace-nowrap">ID *</th>
            <th className="px-4 py-2.5 text-left font-semibold text-gray-700 dark:text-gray-300 whitespace-nowrap">Title *</th>
            <th className="px-4 py-2.5 text-left font-semibold text-gray-700 dark:text-gray-300 whitespace-nowrap">Steps *</th>
            <th className="px-4 py-2.5 text-left font-semibold text-gray-700 dark:text-gray-300 whitespace-nowrap">Priority</th>
            <th className="px-4 py-2.5 text-left font-semibold text-gray-700 dark:text-gray-300 whitespace-nowrap">Type</th>
          </tr>
        </thead>
        <tbody>
          {/* Header Row (shows column descriptions) */}
          {headerRowData && (
            <tr className="border-b-2 border-blue-300 dark:border-blue-700 bg-blue-50 dark:bg-blue-900/20">
              <td className="px-4 py-2 text-blue-800 dark:text-blue-200 font-semibold text-xs italic">
                {headerRowData.id}
              </td>
              <td className="px-4 py-2 text-blue-800 dark:text-blue-200 font-semibold text-xs italic">
                {headerRowData.title}
              </td>
              <td className="px-4 py-2 text-blue-800 dark:text-blue-200 font-semibold text-xs italic">
                {headerRowData.steps}
              </td>
              <td className="px-4 py-2 text-blue-800 dark:text-blue-200 font-semibold text-xs italic">
                {headerRowData.priority || '-'}
              </td>
              <td className="px-4 py-2 text-blue-800 dark:text-blue-200 font-semibold text-xs italic">
                {headerRowData.type || '-'}
              </td>
            </tr>
          )}
          
          {/* Data Rows */}
          {dataRows.slice(0, 9).map((row, idx) => (
            <tr key={idx} className="border-b border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700">
              <td className="px-4 py-2 text-gray-900 dark:text-gray-100 font-mono text-xs">{row.id}</td>
              <td className="px-4 py-2 text-gray-900 dark:text-gray-100">
                <div className="max-w-xs truncate" title={row.title}>{row.title}</div>
              </td>
              <td className="px-4 py-2 text-gray-600 dark:text-gray-400 text-xs">
                <div className="max-w-md truncate" title={row.steps}>
                  {row.steps || '-'}
                </div>
              </td>
              <td className="px-4 py-2 text-gray-600 dark:text-gray-400">{row.priority || '-'}</td>
              <td className="px-4 py-2 text-gray-600 dark:text-gray-400">{row.type || '-'}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
    <div className="mt-3 text-xs text-gray-500 dark:text-gray-400 flex items-center justify-between">
      <span>Showing header + first {Math.min(9, dataRows.length)} data rows</span>
      <span className="text-blue-600 dark:text-blue-400">Scroll horizontally to see all columns →</span>
    </div>
  </div>
);
