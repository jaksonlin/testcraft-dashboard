import React, { useState } from 'react';
import { Upload, CheckCircle, XCircle, AlertCircle, ArrowRight, ArrowLeft } from 'lucide-react';
import { previewExcelFile, previewExcelWithRows, validateMappings, importTestCases } from '../../lib/testCaseApi';
import type { ExcelPreviewResponse, ImportResponse } from '../../lib/testCaseApi';

type WizardStep = 'upload' | 'mapping' | 'preview' | 'complete';

interface TestCaseUploadWizardProps {
  onComplete?: () => void;
}

/**
 * Multi-step wizard for test case upload
 * 
 * Steps:
 * 1. Upload Excel file
 * 2. Review/adjust column mappings
 * 3. Preview data and confirm
 * 4. Import complete
 */
export const TestCaseUploadWizard: React.FC<TestCaseUploadWizardProps> = ({ onComplete }) => {
  const [currentStep, setCurrentStep] = useState<WizardStep>('upload');
  const [file, setFile] = useState<File | null>(null);
  const [preview, setPreview] = useState<ExcelPreviewResponse | null>(null);
  const [mappings, setMappings] = useState<Record<string, string>>({});
  const [headerRow, setHeaderRow] = useState<number>(0);
  const [dataStartRow, setDataStartRow] = useState<number>(1);
  const [isValidMapping, setIsValidMapping] = useState<boolean>(false);
  const [missingFields, setMissingFields] = useState<string[]>([]);
  const [suggestions, setSuggestions] = useState<string[]>([]);
  const [importing, setImporting] = useState<boolean>(false);
  const [importResult, setImportResult] = useState<ImportResponse | null>(null);

  // Step 1: Handle file upload
  const handleFileSelect = async (selectedFile: File) => {
    setFile(selectedFile);
    
    try {
      const previewData = await previewExcelFile(selectedFile);
      setPreview(previewData);
      setMappings(previewData.suggestedMappings);
      setHeaderRow(previewData.suggestedHeaderRow);
      setDataStartRow(previewData.suggestedDataStartRow);
      setIsValidMapping(previewData.validation.valid);
      setMissingFields(previewData.validation.missingRequiredFields);
      setSuggestions(previewData.validation.suggestions);
      
      // Auto-advance to mapping step
      setCurrentStep('mapping');
    } catch (error) {
      console.error('Failed to preview file:', error);
      alert('Failed to preview Excel file. Please check the file format.');
    }
  };

  // Step 2: Handle mapping change
  const handleMappingChange = async (excelColumn: string, systemField: string) => {
    const newMappings = {
      ...mappings,
      [excelColumn]: systemField
    };
    
    // Remove mapping if "ignore" is selected
    if (systemField === 'ignore') {
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const { [excelColumn]: _removed, ...rest } = newMappings;
      setMappings(rest);
    } else {
      setMappings(newMappings);
    }
    
    // Re-validate
    if (preview) {
      try {
        const validation = await validateMappings(newMappings, preview.columns);
        setIsValidMapping(validation.valid);
        setMissingFields(validation.missingRequiredFields);
        setSuggestions(validation.suggestions);
      } catch (error) {
        console.error('Validation failed:', error);
      }
    }
  };

  // Handle row changes and update preview
  const handleRowChange = async (newHeaderRow: number, newDataStartRow: number) => {
    setHeaderRow(newHeaderRow);
    setDataStartRow(newDataStartRow);
    
    if (file) {
      try {
        const updatedPreview = await previewExcelWithRows(file, newHeaderRow, newDataStartRow);
        setPreview(updatedPreview);
        // Update mappings based on new columns
        setMappings(updatedPreview.suggestedMappings);
        setIsValidMapping(updatedPreview.validation.valid);
        setMissingFields(updatedPreview.validation.missingRequiredFields);
        setSuggestions(updatedPreview.validation.suggestions);
      } catch (error) {
        console.error('Failed to update preview:', error);
      }
    }
  };

  // Handle advancing to preview step - refresh preview data
  const handleAdvanceToPreview = async () => {
    if (!file) return;
    
    try {
      // Refresh preview with current headerRow and dataStartRow settings
      const updatedPreview = await previewExcelWithRows(file, headerRow, dataStartRow);
      setPreview(updatedPreview);
      
      // Validate that current mappings are still valid with the refreshed columns
      // If columns changed, user's mappings might be invalid
      const currentMappedColumns = Object.keys(mappings);
      const newColumns = updatedPreview.columns;
      const invalidMappings = currentMappedColumns.filter(col => !newColumns.includes(col));
      
      if (invalidMappings.length > 0) {
        console.warn('Some mappings reference columns that no longer exist:', invalidMappings);
        // Optionally: filter out invalid mappings or use suggested mappings
        // For now, keep user mappings as-is and let them see the data
      }
      
      console.log('Preview refreshed:', {
        columns: updatedPreview.columns,
        previewDataRows: updatedPreview.previewData.length,
        mappings: mappings,
        headerRow,
        dataStartRow
      });
      
      setCurrentStep('preview');
    } catch (error) {
      console.error('Failed to refresh preview:', error);
      alert('Failed to refresh preview. Please try again.');
    }
  };

  // Step 3: Handle import
  const handleImport = async () => {
    if (!file || !isValidMapping) {
      return;
    }

    setImporting(true);

    try {
      const result = await importTestCases(
        file,
        mappings,
        headerRow,
        dataStartRow,
        true, // replaceExisting
        'system', // TODO: Get from user context
        'default' // TODO: Get from organization context
      );

      setImportResult(result);
      setCurrentStep('complete');
    } catch (error: unknown) {
      console.error('Import failed:', error);
      // Try to extract structured server response (400) with errors list
      const respData = (error as {response?: {data?: {message?: string; errors?: string[]; suggestions?: string[]}}})?.response?.data;
      const failureResult: ImportResponse = {
        success: false,
        imported: 0,
        created: 0,
        updated: 0,
        skipped: 0,
        message: respData?.message || (error instanceof Error ? error.message : 'Unknown error'),
        errors: respData?.errors || [],
        suggestions: respData?.suggestions || []
      };

      setImportResult(failureResult);
      setCurrentStep('complete');
    } finally {
      setImporting(false);
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-lg p-6 w-[98vw] max-w-[1800px] mx-auto">
      {/* Progress Steps */}
      <div className="mb-8">
        <div className="flex items-center justify-between">
          {/* Step 1 */}
          <div className="flex items-center">
            <div className={`flex items-center justify-center w-10 h-10 rounded-full ${
              currentStep === 'upload' ? 'bg-blue-600 text-white' :
              ['mapping', 'preview', 'complete'].includes(currentStep) ? 'bg-green-600 text-white' :
              'bg-gray-300 text-gray-600'
            }`}>
              {['mapping', 'preview', 'complete'].includes(currentStep) ? 
                <CheckCircle className="w-5 h-5" /> : '1'}
            </div>
            <span className="ml-2 font-medium">Upload</span>
          </div>

          <div className="flex-1 h-1 mx-4 bg-gray-300">
            <div className={`h-full ${['mapping', 'preview', 'complete'].includes(currentStep) ? 'bg-green-600' : 'bg-gray-300'}`} />
          </div>

          {/* Step 2 */}
          <div className="flex items-center">
            <div className={`flex items-center justify-center w-10 h-10 rounded-full ${
              currentStep === 'mapping' ? 'bg-blue-600 text-white' :
              ['preview', 'complete'].includes(currentStep) ? 'bg-green-600 text-white' :
              'bg-gray-300 text-gray-600'
            }`}>
              {['preview', 'complete'].includes(currentStep) ? 
                <CheckCircle className="w-5 h-5" /> : '2'}
            </div>
            <span className="ml-2 font-medium">Map Columns</span>
          </div>

          <div className="flex-1 h-1 mx-4 bg-gray-300">
            <div className={`h-full ${['preview', 'complete'].includes(currentStep) ? 'bg-green-600' : 'bg-gray-300'}`} />
          </div>

          {/* Step 3 */}
          <div className="flex items-center">
            <div className={`flex items-center justify-center w-10 h-10 rounded-full ${
              currentStep === 'preview' ? 'bg-blue-600 text-white' :
              currentStep === 'complete' ? 'bg-green-600 text-white' :
              'bg-gray-300 text-gray-600'
            }`}>
              {currentStep === 'complete' ? <CheckCircle className="w-5 h-5" /> : '3'}
            </div>
            <span className="ml-2 font-medium">Preview</span>
          </div>

          <div className="flex-1 h-1 mx-4 bg-gray-300">
            <div className={`h-full ${currentStep === 'complete' ? 'bg-green-600' : 'bg-gray-300'}`} />
          </div>

          {/* Step 4 */}
          <div className="flex items-center">
            <div className={`flex items-center justify-center w-10 h-10 rounded-full ${
              currentStep === 'complete' ? 'bg-green-600 text-white' : 'bg-gray-300 text-gray-600'
            }`}>
              {currentStep === 'complete' ? <CheckCircle className="w-5 h-5" /> : '4'}
            </div>
            <span className="ml-2 font-medium">Complete</span>
          </div>
        </div>
      </div>

      {/* Step Content */}
      <div className="min-h-96">
        {currentStep === 'upload' && (
          <UploadStep onFileSelect={handleFileSelect} />
        )}

        {currentStep === 'mapping' && preview && (
          <MappingStep
            preview={preview}
            mappings={mappings}
            headerRow={headerRow}
            dataStartRow={dataStartRow}
            isValid={isValidMapping}
            missingFields={missingFields}
            suggestions={suggestions}
            onMappingChange={handleMappingChange}
            onHeaderRowChange={(row) => handleRowChange(row, dataStartRow)}
            onDataStartRowChange={(row) => handleRowChange(headerRow, row)}
            onNext={handleAdvanceToPreview}
            onBack={() => setCurrentStep('upload')}
          />
        )}

        {currentStep === 'preview' && preview && (
          <PreviewStep
            preview={preview}
            mappings={mappings}
            headerRow={headerRow}
            dataStartRow={dataStartRow}
            importing={importing}
            onImport={handleImport}
            onBack={() => setCurrentStep('mapping')}
          />
        )}

        {currentStep === 'complete' && importResult && (
          <CompleteStep
            result={importResult}
            onClose={() => onComplete && onComplete()}
          />
        )}
      </div>
    </div>
  );
};

// ============================================================================
// Step 1: Upload File
// ============================================================================

const UploadStep: React.FC<{ onFileSelect: (file: File) => void }> = ({ onFileSelect }) => {
  const [dragActive, setDragActive] = useState(false);

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setDragActive(false);

    const files = e.dataTransfer.files;
    if (files.length > 0) {
      onFileSelect(files[0]);
    }
  };

  const handleFileInput = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (files && files.length > 0) {
      onFileSelect(files[0]);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Upload Test Cases</h2>
        <p className="text-gray-600">
          Upload your test case Excel file. The system will automatically detect column mappings.
        </p>
      </div>

      {/* Drag & Drop Area */}
      <div
        className={`border-2 border-dashed rounded-lg p-12 text-center transition-colors ${
          dragActive
            ? 'border-blue-500 bg-blue-50'
            : 'border-gray-300 hover:border-gray-400'
        }`}
        onDragOver={(e) => { e.preventDefault(); setDragActive(true); }}
        onDragLeave={() => setDragActive(false)}
        onDrop={handleDrop}
      >
        <Upload className="w-16 h-16 mx-auto mb-4 text-gray-400" />
        <h3 className="text-lg font-semibold mb-2">Drag and drop your Excel file here</h3>
        <p className="text-gray-600 mb-4">or</p>
        <label className="inline-block">
          <input
            type="file"
            accept=".xlsx,.xls"
            onChange={handleFileInput}
            className="hidden"
          />
          <span className="px-6 py-3 bg-blue-600 text-white rounded-lg cursor-pointer hover:bg-blue-700 transition-colors inline-block">
            Choose File
          </span>
        </label>
        <p className="text-sm text-gray-500 mt-4">Supported formats: .xlsx, .xls</p>
      </div>

      {/* Info Box */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
        <div className="flex items-start">
          <AlertCircle className="w-5 h-5 text-blue-600 mt-0.5 mr-3 flex-shrink-0" />
          <div className="text-sm text-blue-900">
            <p className="font-semibold mb-1">What happens next?</p>
            <ul className="list-disc list-inside space-y-1">
              <li>System analyzes your Excel file structure</li>
              <li>Auto-detects column mappings (ID, Title, Steps, etc.)</li>
              <li>Shows preview of your test cases</li>
              <li>You review and adjust if needed</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
};

// ============================================================================
// Step 2: Map Columns
// ============================================================================

interface MappingStepProps {
  preview: ExcelPreviewResponse;
  mappings: Record<string, string>;
  headerRow: number;
  dataStartRow: number;
  isValid: boolean;
  missingFields: string[];
  suggestions: string[];
  onMappingChange: (excelColumn: string, systemField: string) => void;
  onHeaderRowChange: (row: number) => void;
  onDataStartRowChange: (row: number) => void;
  onNext: () => void;
  onBack: () => void;
}

const MappingStep: React.FC<MappingStepProps> = ({
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
  const systemFields = [
    { value: 'ignore', label: '-- Ignore --', required: false },
    { value: 'id', label: 'ID', required: true },
    { value: 'title', label: 'Title', required: true },
    { value: 'steps', label: 'Steps', required: true },
    { value: 'setup', label: 'Setup/Precondition', required: false },
    { value: 'teardown', label: 'Teardown/Postcondition', required: false },
    { value: 'expected_result', label: 'Expected Result', required: false },
    { value: 'priority', label: 'Priority', required: false },
    { value: 'type', label: 'Type', required: false },
    { value: 'status', label: 'Status', required: false },
  ];

  const getConfidenceColor = (confidence: number) => {
    if (confidence >= 90) return 'text-green-600';
    if (confidence >= 70) return 'text-yellow-600';
    return 'text-orange-600';
  };

  const getConfidenceIcon = (confidence: number) => {
    if (confidence >= 90) return <CheckCircle className="w-4 h-4 text-green-600" />;
    if (confidence >= 70) return <AlertCircle className="w-4 h-4 text-yellow-600" />;
    return <AlertCircle className="w-4 h-4 text-orange-600" />;
  };

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

      {/* Two Column Layout - Balanced spacing */}
      <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
        {/* Left Column: Preview + Row Settings */}
        <div className="space-y-6">

          {/* Preview Data */}
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
                    {preview.columns.slice(0, 8).map((col, idx) => (
                      <th key={idx} className="px-3 py-2 text-left font-semibold text-blue-900 min-w-[100px] whitespace-nowrap">
                        <div className="flex items-center gap-1">
                          <span title={col}>{col}</span>
                        </div>
                      </th>
                    ))}
                    {preview.columns.length > 8 && (
                      <th className="px-3 py-2 text-center font-semibold text-blue-600 min-w-[60px] sticky right-0 bg-blue-50">
                        +{preview.columns.length - 8}
                      </th>
                    )}
                  </tr>
                </thead>
                <tbody>
                  {preview.previewData.slice(1, 5).map((row, idx) => (
                    <tr key={idx} className={`border-b border-gray-200 ${idx === 0 ? 'bg-green-50' : 'bg-white'}`}>
                      {preview.columns.slice(0, 8).map((col, colIdx) => (
                        <td key={colIdx} className={`px-3 py-2 ${idx === 0 ? 'text-green-800 font-medium' : 'text-gray-600'} min-w-[100px]`}>
                          <div className="truncate max-w-[150px]" title={row[col]}>
                            {row[col] || '-'}
                          </div>
                        </td>
                      ))}
                      {preview.columns.length > 8 && (
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
              Showing first 4 data rows • {preview.columns.length} columns total • Scroll horizontally →
            </div>
          </div>

          {/* Row Settings */}
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
        </div>

        {/* Right Column: Column Mappings */}
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
              {preview.columns.map((excelCol, idx) => {
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
                        {systemFields.map(field => (
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

// ============================================================================
// Step 3: Preview & Confirm
// ============================================================================

interface PreviewStepProps {
  preview: ExcelPreviewResponse;
  mappings: Record<string, string>;
  headerRow: number;
  dataStartRow: number;
  importing: boolean;
  onImport: () => void;
  onBack: () => void;
}

const PreviewStep: React.FC<PreviewStepProps> = (props) => {
  const { preview, mappings, headerRow: headerRowIndex, dataStartRow, importing, onImport, onBack } = props;
  
  // Calculate actual number of rows to be imported
  // Total rows in sheet minus the data start row (all rows from dataStartRow to end)
  const estimatedImportCount = Math.max(0, preview.totalRows - dataStartRow);
  
  // Debug: Log what we received
  console.log('PreviewStep received:', {
    previewColumns: preview.columns,
    previewDataLength: preview.previewData.length,
    mappings: mappings,
    firstRowKeys: preview.previewData[0] ? Object.keys(preview.previewData[0]) : [],
    firstRowSample: preview.previewData[0]
  });
  
  // Map preview data using user's mappings
  // First row is header row, rest are data rows
  const mappedPreview = preview.previewData.map((row, index) => {
    const mapped: Record<string, string> = {};
    Object.entries(mappings).forEach(([excelCol, systemField]) => {
      if (systemField !== 'ignore') {
        mapped[systemField] = row[excelCol] || '';
      }
    });
    
    // Debug: Log mapping for first data row
    if (index === 1) {
      console.log('First data row mapping:', {
        originalRow: row,
        mappedRow: mapped,
        mappings: mappings
      });
    }
    
    return { ...mapped, _isHeader: index === 0 } as Record<string, string> & { _isHeader: boolean };
  });
  
  const headerRowData = mappedPreview[0];
  const dataRows = mappedPreview.slice(1);
  
  console.log('Mapped preview result:', {
    headerRowData,
    dataRowsCount: dataRows.length,
    firstDataRow: dataRows[0]
  });

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Preview Import</h2>
        <p className="text-gray-600">
          Review the mapped data before importing. Showing header row + first 9 data rows.
        </p>
      </div>

      {/* Import Info */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
        <div className="flex items-center justify-between">
          <div>
            <p className="font-semibold text-blue-900">Ready to import</p>
            <p className="text-sm text-blue-700">
              Estimated {estimatedImportCount} test cases will be imported
            </p>
            <p className="text-xs text-blue-600 mt-1">
              Using Header Row {headerRowIndex} (Excel Row {headerRowIndex + 1}), Data from Row {dataStartRow} to {preview.totalRows - 1}
            </p>
          </div>
          <CheckCircle className="w-8 h-8 text-blue-600" />
        </div>
      </div>

      {/* Debug Info */}
      <div className="bg-gray-100 border border-gray-300 rounded-lg p-4">
        <details className="cursor-pointer">
          <summary className="font-semibold text-gray-700 text-sm">🔍 Debug Info (Click to expand)</summary>
          <div className="mt-3 space-y-2 text-xs font-mono">
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
            <div className="pt-2 border-t border-gray-300">
              <span className="font-bold">Current Mappings:</span>
              <div className="pl-4 mt-1">
                {Object.entries(mappings).map(([excel, system]) => (
                  <div key={excel}>{excel} → {system}</div>
                ))}
              </div>
            </div>
            {dataRows.length > 0 && (
              <div className="pt-2 border-t border-gray-300">
                <span className="font-bold">First Data Row Sample:</span>
                <pre className="mt-1 p-2 bg-white rounded text-[10px] overflow-auto">
                  {JSON.stringify(dataRows[0], null, 2)}
                </pre>
              </div>
            )}
          </div>
        </details>
      </div>

      {/* Preview Table */}
      <div className="bg-gray-50 rounded-lg p-4">
        <div className="overflow-x-auto border border-gray-200 rounded-lg">
          <table className="min-w-full text-sm">
            <thead>
              <tr className="border-b-2 border-gray-300 bg-gray-100">
                <th className="px-4 py-2.5 text-left font-semibold text-gray-700 whitespace-nowrap">ID *</th>
                <th className="px-4 py-2.5 text-left font-semibold text-gray-700 whitespace-nowrap">Title *</th>
                <th className="px-4 py-2.5 text-left font-semibold text-gray-700 whitespace-nowrap">Steps *</th>
                <th className="px-4 py-2.5 text-left font-semibold text-gray-700 whitespace-nowrap">Priority</th>
                <th className="px-4 py-2.5 text-left font-semibold text-gray-700 whitespace-nowrap">Type</th>
              </tr>
            </thead>
            <tbody>
              {/* Header Row (shows column descriptions) */}
              {headerRowData && (
                <tr className="border-b-2 border-blue-300 bg-blue-50">
                  <td className="px-4 py-2 text-blue-800 font-semibold text-xs italic">
                    {headerRowData.id}
                  </td>
                  <td className="px-4 py-2 text-blue-800 font-semibold text-xs italic">
                    {headerRowData.title}
                  </td>
                  <td className="px-4 py-2 text-blue-800 font-semibold text-xs italic">
                    {headerRowData.steps}
                  </td>
                  <td className="px-4 py-2 text-blue-800 font-semibold text-xs italic">
                    {headerRowData.priority || '-'}
                  </td>
                  <td className="px-4 py-2 text-blue-800 font-semibold text-xs italic">
                    {headerRowData.type || '-'}
                  </td>
                </tr>
              )}
              
              {/* Data Rows */}
              {dataRows.slice(0, 9).map((row, idx) => (
                <tr key={idx} className="border-b border-gray-200 bg-white hover:bg-gray-50">
                  <td className="px-4 py-2 text-gray-900 font-mono text-xs">{row.id}</td>
                  <td className="px-4 py-2 text-gray-900">
                    <div className="max-w-xs truncate" title={row.title}>{row.title}</div>
                  </td>
                  <td className="px-4 py-2 text-gray-600 text-xs">
                    <div className="max-w-md truncate" title={row.steps}>
                      {row.steps || '-'}
                    </div>
                  </td>
                  <td className="px-4 py-2 text-gray-600 text-center">{row.priority || '-'}</td>
                  <td className="px-4 py-2 text-gray-600 text-center">{row.type || '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <div className="mt-3 text-xs text-gray-500 flex items-center justify-between">
          <span>Showing header + first {Math.min(9, dataRows.length)} data rows</span>
          <span className="text-blue-600">Scroll horizontally to see all columns →</span>
        </div>
      </div>

      {/* Actions */}
      <div className="flex justify-between items-center pt-6 border-t-2 border-gray-200 mt-6">
        <button
          onClick={onBack}
          disabled={importing}
          className="px-6 py-2.5 text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors flex items-center gap-2 disabled:opacity-50 font-medium"
        >
          <ArrowLeft className="w-4 h-4" />
          Back
        </button>
        
        <button
          onClick={onImport}
          disabled={importing}
          className="px-10 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors flex items-center gap-2 disabled:opacity-50 font-semibold shadow-lg hover:shadow-xl text-base"
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

// ============================================================================
// Step 4: Complete
// ============================================================================

interface CompleteStepProps {
  result: ImportResponse;
  onClose: () => void;
}

const CompleteStep: React.FC<CompleteStepProps> = ({ result, onClose }) => {
  const isSuccess = result.success !== false; // default to success if undefined
  return (
    <div className="space-y-6 text-center">
      <div className="flex justify-center">
        {isSuccess ? (
          <CheckCircle className="w-24 h-24 text-green-600" />
        ) : (
          <svg className="w-24 h-24 text-red-600" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="15" y1="9" x2="9" y2="15"></line><line x1="9" y1="9" x2="15" y2="15"></line></svg>
        )}
      </div>

      {isSuccess ? (
        <>
          <div>
            <h2 className="text-3xl font-bold text-gray-900 mb-2">Import Complete!</h2>
            <p className="text-gray-600 text-lg">
              Successfully processed {result.imported} test cases
            </p>
          </div>

          <div className="grid grid-cols-2 md:grid-cols-3 gap-4 max-w-3xl mx-auto">
            <div className="bg-blue-50 border-2 border-blue-200 rounded-xl p-5 text-center">
              <div className="text-4xl font-bold text-blue-600 mb-1">{result.created || 0}</div>
              <div className="text-sm text-gray-700 font-semibold">Created</div>
              <div className="text-xs text-gray-500 mt-1">New test cases</div>
            </div>
            <div className="bg-green-50 border-2 border-green-200 rounded-xl p-5 text-center">
              <div className="text-4xl font-bold text-green-600 mb-1">{result.updated || 0}</div>
              <div className="text-sm text-gray-700 font-semibold">Updated</div>
              <div className="text-xs text-gray-500 mt-1">Existing test cases</div>
            </div>
            {result.skipped > 0 && (
              <div className="bg-yellow-50 border-2 border-yellow-200 rounded-xl p-5 text-center">
                <div className="text-4xl font-bold text-yellow-600 mb-1">{result.skipped}</div>
                <div className="text-sm text-gray-700 font-semibold">Skipped</div>
                <div className="text-xs text-gray-500 mt-1">Invalid entries</div>
              </div>
            )}
          </div>
          
          {result.message && (
            <div className="bg-gray-50 border border-gray-200 rounded-lg p-4 max-w-2xl mx-auto">
              <p className="text-sm text-gray-700">{result.message}</p>
            </div>
          )}

          <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 text-left max-w-md mx-auto">
            <p className="font-semibold text-blue-900 mb-2">What's next?</p>
            <ul className="list-disc list-inside space-y-1 text-sm text-blue-800">
              <li>View test cases in the Test Cases section</li>
              <li>Check coverage statistics</li>
              <li>Link test methods using @TestCaseId annotation</li>
              <li>Analyze gaps (which test cases need automation)</li>
            </ul>
          </div>

          <div className="pt-4">
            <button
              onClick={onClose}
              className="px-8 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-semibold"
            >
              Done
            </button>
          </div>
        </>
      ) : (
        <>
          <div>
            <h2 className="text-3xl font-bold text-gray-900 mb-2">Import Failed</h2>
            <p className="text-gray-600 text-lg">Please fix the issues below and try again.</p>
          </div>

          {result.message && (
            <div className="max-w-2xl mx-auto text-left bg-red-50 border border-red-200 rounded-lg p-4">
              <p className="font-semibold text-red-900 mb-1">Error</p>
              <p className="text-sm text-red-800 break-words">{result.message}</p>
            </div>
          )}

          {Array.isArray(result.errors) && result.errors.length > 0 && (
            <div className="max-w-2xl mx-auto text-left bg-red-50 border border-red-200 rounded-lg p-4">
              <p className="font-semibold text-red-900 mb-2">Row errors</p>
              <ul className="list-disc list-inside space-y-1 text-sm text-red-800">
                {result.errors.map((e, i) => (
                  <li key={i}>{String(e)}</li>
                ))}
              </ul>
            </div>
          )}

          {Array.isArray(result.suggestions) && result.suggestions.length > 0 && (
            <div className="max-w-2xl mx-auto text-left bg-yellow-50 border border-yellow-200 rounded-lg p-4">
              <p className="font-semibold text-yellow-900 mb-2">Suggestions</p>
              <ul className="list-disc list-inside space-y-1 text-sm text-yellow-800">
                {result.suggestions.map((s, i) => (
                  <li key={i}>{String(s)}</li>
                ))}
              </ul>
            </div>
          )}

          <div className="pt-2">
            <button
              onClick={onClose}
              className="px-8 py-3 bg-gray-800 text-white rounded-lg hover:bg-gray-900 transition-colors font-semibold"
            >
              Close
            </button>
          </div>
        </>
      )}
    </div>
  );
};

