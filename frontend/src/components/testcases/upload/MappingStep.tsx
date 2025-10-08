/**
 * Mapping Step Component - Column mapping interface
 */

import React from 'react';
import { CheckCircle, XCircle, AlertCircle, ArrowLeft, ArrowRight } from 'lucide-react';
import type { MappingStepProps, ExcelPreviewResponse } from './types';
import { SYSTEM_FIELDS, PREVIEW_LIMITS, CONFIDENCE_THRESHOLDS } from './constants';
import { getConfidenceColor } from './utils';

/**
 * Get icon component for confidence level
 */
const getConfidenceIcon = (confidence: number) => {
  if (confidence >= CONFIDENCE_THRESHOLDS.HIGH) 
    return <CheckCircle className="w-4 h-4 text-green-600" />;
  if (confidence >= CONFIDENCE_THRESHOLDS.MEDIUM) 
    return <AlertCircle className="w-4 h-4 text-yellow-600" />;
  return <AlertCircle className="w-4 h-4 text-orange-600" />;
};

export const MappingStep: React.FC<MappingStepProps> = ({
  preview,
  mappings,
  headerRow,
  dataStartRow,
  isValid,
  missingFields,
  suggestions,
  onMappingChange,
  onHeaderRowChange,
  onDataStartRowChange,
  onNext,
  onBack
}) => {
  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Map Excel Columns</h2>
        <p className="text-gray-600">
          Review and adjust the column mappings. Required fields are marked with *.
        </p>
      </div>

      {/* Validation Status */}
      {!isValid && missingFields.length > 0 && (
        <ValidationAlert missingFields={missingFields} suggestions={suggestions} />
      )}

      {isValid && (
        <div className="bg-green-50 border-2 border-green-300 rounded-lg p-3">
          <div className="flex items-center gap-2">
            <CheckCircle className="w-5 h-5 text-green-600" />
            <span className="text-sm font-semibold text-green-900">
              ✓ All required fields are mapped - ready to proceed
            </span>
          </div>
        </div>
      )}

      {/* Two Column Layout */}
      <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
        {/* Left Column: Preview + Row Settings */}
        <div className="space-y-6">
          <PreviewTable 
            preview={preview} 
            headerRow={headerRow} 
            dataStartRow={dataStartRow} 
          />
          <RowSettings
            headerRow={headerRow}
            dataStartRow={dataStartRow}
            onHeaderRowChange={onHeaderRowChange}
            onDataStartRowChange={onDataStartRowChange}
          />
        </div>

        {/* Right Column: Column Mappings */}
        <ColumnMappings
          preview={preview}
          mappings={mappings}
          onMappingChange={onMappingChange}
        />
      </div>

      {/* Actions */}
      <div className="flex justify-between items-center pt-6 border-t-2 border-gray-200 mt-6">
        <button
          onClick={onBack}
          className="px-6 py-2.5 text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors flex items-center gap-2 font-medium"
        >
          <ArrowLeft className="w-4 h-4" />
          Back
        </button>
        
        <button
          onClick={onNext}
          disabled={!isValid}
          className={`px-8 py-2.5 rounded-lg transition-colors flex items-center gap-2 font-semibold ${
            isValid
              ? 'bg-blue-600 text-white hover:bg-blue-700 shadow-md hover:shadow-lg'
              : 'bg-gray-300 text-gray-500 cursor-not-allowed'
          }`}
        >
          Preview Import
          <ArrowRight className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
};

// Sub-components

const ValidationAlert: React.FC<{ missingFields: string[]; suggestions: string[] }> = ({ 
  missingFields, 
  suggestions 
}) => (
  <div className="bg-red-50 border-2 border-red-300 rounded-lg p-4">
    <div className="flex items-start gap-3">
      <XCircle className="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5" />
      <div className="flex-1">
        <p className="font-semibold text-red-900 mb-2">Missing required mappings:</p>
        <div className="flex flex-wrap gap-2 mb-3">
          {missingFields.map(field => (
            <span key={field} className="px-2.5 py-1 bg-red-200 text-red-800 rounded-md text-xs font-semibold">
              {field} *
            </span>
          ))}
        </div>
        <p className="text-sm text-red-700 mb-2">
          <strong>Action:</strong> Map these fields in the Column Mappings section →
        </p>
        {suggestions.length > 0 && (
          <div className="mt-2 text-sm">
            <p className="font-semibold text-red-800 mb-1">💡 Suggestions:</p>
            <ul className="list-disc list-inside text-red-700 space-y-0.5">
              {suggestions.map((suggestion, idx) => (
                <li key={idx}>{suggestion}</li>
              ))}
            </ul>
          </div>
        )}
      </div>
    </div>
  </div>
);

const PreviewTable: React.FC<{ 
  preview: ExcelPreviewResponse; 
  headerRow: number; 
  dataStartRow: number;
}> = ({ preview, headerRow, dataStartRow }) => (
  <div className="bg-gray-50 rounded-lg p-4">
    <div className="flex items-center justify-between mb-3">
      <h3 className="font-semibold text-gray-900">Excel Preview</h3>
      <div className="flex items-center gap-3 text-xs text-gray-600">
        <div className="flex items-center gap-1">
          <div className="w-3 h-3 bg-blue-500 rounded"></div>
          <span>Header Row {headerRow}</span>
        </div>
        <div className="flex items-center gap-1">
          <div className="w-3 h-3 bg-green-500 rounded"></div>
          <span>Data from Row {dataStartRow}</span>
        </div>
      </div>
    </div>
    <div className="overflow-x-auto border border-gray-200 rounded-lg">
      <table className="min-w-full text-sm">
        <thead>
          <tr className="border-b-2 border-blue-500 bg-blue-50">
            {preview.columns.slice(0, PREVIEW_LIMITS.COLUMNS_TO_SHOW).map((col: string, idx: number) => (
              <th key={idx} className="px-3 py-2 text-left font-semibold text-blue-900 min-w-[100px] whitespace-nowrap">
                <div className="flex items-center gap-1">
                  <span title={col}>{col}</span>
                </div>
              </th>
            ))}
            {preview.columns.length > PREVIEW_LIMITS.COLUMNS_TO_SHOW && (
              <th className="px-3 py-2 text-center font-semibold text-blue-600 min-w-[60px] sticky right-0 bg-blue-50">
                +{preview.columns.length - PREVIEW_LIMITS.COLUMNS_TO_SHOW}
              </th>
            )}
          </tr>
        </thead>
        <tbody>
          {preview.previewData.slice(1, PREVIEW_LIMITS.PREVIEW_ROWS_IN_MAPPING + 1).map((row: Record<string, string>, idx: number) => (
            <tr key={idx} className={`border-b border-gray-200 ${idx === 0 ? 'bg-green-50' : 'bg-white'}`}>
              {preview.columns.slice(0, PREVIEW_LIMITS.COLUMNS_TO_SHOW).map((col: string, colIdx: number) => (
                <td key={colIdx} className={`px-3 py-2 ${idx === 0 ? 'text-green-800 font-medium' : 'text-gray-600'} min-w-[100px]`}>
                  <div className="truncate max-w-[150px]" title={row[col]}>
                    {row[col] || '-'}
                  </div>
                </td>
              ))}
              {preview.columns.length > PREVIEW_LIMITS.COLUMNS_TO_SHOW && (
                <td className={`px-3 py-2 text-center sticky right-0 ${idx === 0 ? 'bg-green-50' : 'bg-white'}`}>
                  <span className="text-gray-400">…</span>
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
    <div className="mt-2 text-xs text-gray-500">
      Showing first {PREVIEW_LIMITS.PREVIEW_ROWS_IN_MAPPING} data rows • {preview.columns.length} columns total • Scroll horizontally →
    </div>
  </div>
);

const RowSettings: React.FC<{
  headerRow: number;
  dataStartRow: number;
  onHeaderRowChange: (row: number) => void;
  onDataStartRowChange: (row: number) => void;
}> = ({ headerRow, dataStartRow, onHeaderRowChange, onDataStartRowChange }) => (
  <div className="bg-gray-50 rounded-lg p-4">
    <h3 className="font-semibold text-gray-900 mb-3">Excel Row Settings</h3>
    <div className="grid grid-cols-2 gap-4">
      {/* Header Row */}
      <div>
        <label className="block text-sm font-semibold text-gray-700 mb-1.5">
          Header Row (0-based):
        </label>
        <input
          type="number"
          min={0}
          value={headerRow}
          onChange={(e) => onHeaderRowChange(parseInt(e.target.value) || 0)}
          className="px-3 py-2 border border-gray-300 rounded-lg w-full text-sm"
        />
        <p className="text-xs text-gray-600 mt-1">
          Row with column headers (usually 0)
        </p>
      </div>

      {/* Data Start Row */}
      <div>
        <label className="block text-sm font-semibold text-gray-700 mb-1.5">
          Data Start Row (0-based):
        </label>
        <input
          type="number"
          min={headerRow + 1}
          value={dataStartRow}
          onChange={(e) => onDataStartRowChange(parseInt(e.target.value) || headerRow + 1)}
          className="px-3 py-2 border border-gray-300 rounded-lg w-full text-sm"
        />
        <p className="text-xs text-gray-600 mt-1">
          First row with data (after header)
        </p>
      </div>
    </div>
    
    <div className="mt-3 bg-blue-50 border border-blue-200 rounded p-3">
      <div className="flex items-start gap-2">
        <AlertCircle className="w-4 h-4 text-blue-600 flex-shrink-0 mt-0.5" />
        <div className="text-xs text-blue-800 space-y-1.5">
          <p>
            <strong>Row Numbering:</strong> 0-based (Row 0 = Excel Row 1)
          </p>
          <p>
            <strong>Header Row {headerRow}</strong> (Excel {headerRow + 1}): 
            <span className="ml-1 text-blue-900">Column names (ID, Title, Steps)</span>
          </p>
          <p>
            <strong>Data Row {dataStartRow}</strong> (Excel {dataStartRow + 1}): 
            <span className="ml-1 text-green-700">First test case data</span>
          </p>
          {dataStartRow > headerRow + 1 && (
            <p className="text-yellow-700">
              ⚠️ Rows {headerRow + 1} to {dataStartRow - 1} will be skipped
            </p>
          )}
        </div>
      </div>
    </div>
  </div>
);

const ColumnMappings: React.FC<{
  preview: ExcelPreviewResponse;
  mappings: Record<string, string>;
  onMappingChange: (excelColumn: string, systemField: string) => void;
}> = ({ preview, mappings, onMappingChange }) => (
  <div className="flex flex-col">
    <div className="mb-3 pb-3 border-b border-gray-200">
      <div className="flex items-center justify-between">
        <h3 className="font-semibold text-gray-900">Column Mappings</h3>
        <div className="text-sm text-gray-600 bg-blue-50 px-3 py-1 rounded-full">
          {Object.values(mappings).filter(f => f !== 'ignore').length} of {preview.columns.length} mapped
        </div>
      </div>
      <p className="text-xs text-gray-500 mt-1">Map Excel columns to system fields (* = required)</p>
    </div>
    <div className="overflow-y-auto max-h-[600px] pr-2 scrollbar-thin">
      <div className="space-y-3">
        {preview.columns.map((excelCol: string, idx: number) => {
          const mappedField = mappings[excelCol] || 'ignore';
          const confidence = preview.confidence[excelCol] || 0;
          const isRequired = ['id', 'title', 'steps'].includes(mappedField);
          const isMapped = mappedField !== 'ignore';

          return (
            <div
              key={idx}
              className={`flex items-center gap-3 p-3 rounded-lg border transition-all ${
                isMapped 
                  ? 'bg-green-50 border-green-200 shadow-sm' 
                  : 'bg-white border-gray-200 hover:border-gray-300'
              }`}
            >
              {/* Excel Column Name */}
              <div className="flex-shrink-0 w-[140px]">
                <div className="font-semibold text-gray-900 text-sm truncate" title={excelCol}>
                  {excelCol}
                </div>
                {confidence > 0 && (
                  <div className="flex items-center gap-1 mt-0.5">
                    <div className="text-xs text-blue-600">Auto</div>
                    <span className={`text-xs font-medium ${getConfidenceColor(confidence)}`}>
                      {confidence}%
                    </span>
                  </div>
                )}
              </div>

              {/* Arrow */}
              <div className="flex-shrink-0 text-gray-400 text-lg">→</div>

              {/* System Field Dropdown */}
              <div className="flex-1 min-w-[160px]">
                <select
                  value={mappedField}
                  onChange={(e) => onMappingChange(excelCol, e.target.value)}
                  className={`w-full px-2.5 py-1.5 text-sm rounded-md border focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all ${
                    isRequired && !isMapped
                      ? 'border-red-300 bg-red-50'
                      : isMapped
                      ? 'border-green-300 bg-white'
                      : 'border-gray-300 bg-white'
                  }`}
                >
                  {SYSTEM_FIELDS.map(field => (
                    <option key={field.value} value={field.value}>
                      {field.label}{field.required ? ' *' : ''}
                    </option>
                  ))}
                </select>
              </div>

              {/* Status Badge */}
              <div className="flex-shrink-0">
                {isRequired && (
                  <span className={`text-xs font-semibold px-2 py-1 rounded whitespace-nowrap ${
                    isMapped 
                      ? 'text-green-700 bg-green-100' 
                      : 'text-red-700 bg-red-100'
                  }`}>
                    {isMapped ? '✓' : '⚠'}
                  </span>
                )}
                {!isRequired && confidence > 0 && (
                  <div className="flex items-center">
                    {getConfidenceIcon(confidence)}
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  </div>
);
