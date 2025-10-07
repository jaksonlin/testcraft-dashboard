import React, { useState } from 'react';
import { Upload, CheckCircle, XCircle, AlertCircle, ArrowRight, ArrowLeft } from 'lucide-react';
import { previewExcelFile, validateMappings, importTestCases } from '../../lib/testCaseApi';
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
  const [dataStartRow, setDataStartRow] = useState<number>(2);
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
        dataStartRow,
        true, // replaceExisting
        'system', // TODO: Get from user context
        'default' // TODO: Get from organization context
      );

      setImportResult(result);
      setCurrentStep('complete');
      
      if (onComplete) {
        onComplete();
      }
    } catch (error) {
      console.error('Import failed:', error);
      const errorMessage = error instanceof Error ? error.message : 'Unknown error';
      alert('Import failed: ' + errorMessage);
    } finally {
      setImporting(false);
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-lg p-6 max-w-6xl mx-auto">
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
            dataStartRow={dataStartRow}
            isValid={isValidMapping}
            missingFields={missingFields}
            suggestions={suggestions}
            onMappingChange={handleMappingChange}
            onDataStartRowChange={setDataStartRow}
            onNext={() => setCurrentStep('preview')}
            onBack={() => setCurrentStep('upload')}
          />
        )}

        {currentStep === 'preview' && preview && (
          <PreviewStep
            preview={preview}
            mappings={mappings}
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
  dataStartRow: number;
  isValid: boolean;
  missingFields: string[];
  suggestions: string[];
  onMappingChange: (excelColumn: string, systemField: string) => void;
  onDataStartRowChange: (row: number) => void;
  onNext: () => void;
  onBack: () => void;
}

const MappingStep: React.FC<MappingStepProps> = ({
  preview,
  mappings,
  dataStartRow,
  isValid,
  missingFields,
  suggestions,
  onMappingChange,
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
        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
          <div className="flex items-start">
            <XCircle className="w-5 h-5 text-red-600 mt-0.5 mr-3 flex-shrink-0" />
            <div className="text-sm text-red-900">
              <p className="font-semibold mb-1">Missing required fields:</p>
              <ul className="list-disc list-inside">
                {missingFields.map(field => (
                  <li key={field}>{field}</li>
                ))}
              </ul>
              {suggestions.length > 0 && (
                <>
                  <p className="font-semibold mt-2 mb-1">Suggestions:</p>
                  <ul className="list-disc list-inside">
                    {suggestions.map((suggestion, idx) => (
                      <li key={idx}>{suggestion}</li>
                    ))}
                  </ul>
                </>
              )}
            </div>
          </div>
        </div>
      )}

      {isValid && (
        <div className="bg-green-50 border border-green-200 rounded-lg p-4">
          <div className="flex items-center">
            <CheckCircle className="w-5 h-5 text-green-600 mr-3" />
            <span className="text-sm font-semibold text-green-900">
              All required fields are mapped
            </span>
          </div>
        </div>
      )}

      {/* Preview Data */}
      <div className="bg-gray-50 rounded-lg p-4">
        <h3 className="font-semibold text-gray-900 mb-3">Excel Preview (First 5 Rows)</h3>
        <div className="overflow-x-auto">
          <table className="min-w-full text-sm">
            <thead>
              <tr className="border-b border-gray-300">
                {preview.columns.map((col, idx) => (
                  <th key={idx} className="px-3 py-2 text-left font-semibold text-gray-700">
                    {col}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {preview.previewData.slice(0, 5).map((row, idx) => (
                <tr key={idx} className="border-b border-gray-200">
                  {preview.columns.map((col, colIdx) => (
                    <td key={colIdx} className="px-3 py-2 text-gray-600">
                      {row[col]?.substring(0, 50)}{row[col]?.length > 50 ? '...' : ''}
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Column Mappings */}
      <div>
        <h3 className="font-semibold text-gray-900 mb-3">Column Mappings</h3>
        <div className="space-y-3">
          {preview.columns.map((excelCol, idx) => {
            const mappedField = mappings[excelCol] || 'ignore';
            const confidence = preview.confidence[excelCol] || 0;
            const isRequired = ['id', 'title', 'steps'].includes(mappedField);

            return (
              <div key={idx} className="flex items-center gap-4 p-3 bg-gray-50 rounded-lg">
                <div className="flex-1">
                  <span className="font-medium text-gray-900">{excelCol}</span>
                </div>
                
                <span className="text-gray-400">→</span>
                
                <select
                  value={mappedField}
                  onChange={(e) => onMappingChange(excelCol, e.target.value)}
                  className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  {systemFields.map(field => (
                    <option key={field.value} value={field.value}>
                      {field.label}{field.required ? ' *' : ''}
                    </option>
                  ))}
                </select>

                {confidence > 0 && (
                  <div className="flex items-center gap-2 w-24">
                    {getConfidenceIcon(confidence)}
                    <span className={`text-sm font-medium ${getConfidenceColor(confidence)}`}>
                      {confidence}%
                    </span>
                  </div>
                )}

                {isRequired && (
                  <span className="text-green-600 font-semibold text-sm">Required ✓</span>
                )}
              </div>
            );
          })}
        </div>
      </div>

      {/* Data Start Row */}
      <div className="bg-gray-50 rounded-lg p-4">
        <label className="block font-semibold text-gray-900 mb-2">
          Data starts at row:
        </label>
        <input
          type="number"
          min={1}
          value={dataStartRow}
          onChange={(e) => onDataStartRowChange(parseInt(e.target.value) || 1)}
          className="px-3 py-2 border border-gray-300 rounded-lg w-32"
        />
        <p className="text-sm text-gray-600 mt-1">
          Usually row 2 (row 1 is headers)
        </p>
      </div>

      {/* Actions */}
      <div className="flex justify-between pt-4 border-t">
        <button
          onClick={onBack}
          className="px-6 py-2 text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors flex items-center gap-2"
        >
          <ArrowLeft className="w-4 h-4" />
          Back
        </button>
        
        <button
          onClick={onNext}
          disabled={!isValid}
          className={`px-6 py-2 rounded-lg transition-colors flex items-center gap-2 ${
            isValid
              ? 'bg-blue-600 text-white hover:bg-blue-700'
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
  dataStartRow: number;
  importing: boolean;
  onImport: () => void;
  onBack: () => void;
}

const PreviewStep: React.FC<PreviewStepProps> = (props) => {
  const { preview, mappings, importing, onImport, onBack } = props;
  // Note: dataStartRow reserved for future use (showing row range in preview)
  // Map preview data using user's mappings
  const mappedPreview = preview.previewData.map(row => {
    const mapped: Record<string, string> = {};
    Object.entries(mappings).forEach(([excelCol, systemField]) => {
      if (systemField !== 'ignore') {
        mapped[systemField] = row[excelCol] || '';
      }
    });
    return mapped;
  });

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Preview Import</h2>
        <p className="text-gray-600">
          Review the mapped data before importing. Showing first 10 test cases.
        </p>
      </div>

      {/* Import Info */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
        <div className="flex items-center justify-between">
          <div>
            <p className="font-semibold text-blue-900">Ready to import</p>
            <p className="text-sm text-blue-700">
              {preview.previewData.length} test cases found
            </p>
          </div>
          <CheckCircle className="w-8 h-8 text-blue-600" />
        </div>
      </div>

      {/* Preview Table */}
      <div className="bg-gray-50 rounded-lg p-4 overflow-x-auto">
        <table className="min-w-full text-sm">
          <thead>
            <tr className="border-b-2 border-gray-300">
              <th className="px-3 py-2 text-left font-semibold text-gray-700">ID *</th>
              <th className="px-3 py-2 text-left font-semibold text-gray-700">Title *</th>
              <th className="px-3 py-2 text-left font-semibold text-gray-700">Steps *</th>
              <th className="px-3 py-2 text-left font-semibold text-gray-700">Priority</th>
              <th className="px-3 py-2 text-left font-semibold text-gray-700">Type</th>
            </tr>
          </thead>
          <tbody>
            {mappedPreview.slice(0, 10).map((row, idx) => (
              <tr key={idx} className="border-b border-gray-200">
                <td className="px-3 py-2 text-gray-900 font-mono">{row.id}</td>
                <td className="px-3 py-2 text-gray-900">{row.title}</td>
                <td className="px-3 py-2 text-gray-600 text-xs">
                  {row.steps?.substring(0, 60)}{row.steps?.length > 60 ? '...' : ''}
                </td>
                <td className="px-3 py-2 text-gray-600">{row.priority || '-'}</td>
                <td className="px-3 py-2 text-gray-600">{row.type || '-'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Actions */}
      <div className="flex justify-between pt-4 border-t">
        <button
          onClick={onBack}
          disabled={importing}
          className="px-6 py-2 text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors flex items-center gap-2 disabled:opacity-50"
        >
          <ArrowLeft className="w-4 h-4" />
          Back
        </button>
        
        <button
          onClick={onImport}
          disabled={importing}
          className="px-8 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors flex items-center gap-2 disabled:opacity-50 font-semibold"
        >
          {importing ? (
            <>
              <div className="animate-spin rounded-full h-4 w-4 border-2 border-white border-t-transparent" />
              Importing...
            </>
          ) : (
            <>
              Import {preview.previewData.length} Test Cases
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
  return (
    <div className="space-y-6 text-center">
      <div className="flex justify-center">
        <CheckCircle className="w-24 h-24 text-green-600" />
      </div>

      <div>
        <h2 className="text-3xl font-bold text-gray-900 mb-2">Import Complete!</h2>
        <p className="text-gray-600 text-lg">
          Successfully imported {result.imported} test cases
        </p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 gap-4 max-w-md mx-auto">
        <div className="bg-green-50 border border-green-200 rounded-lg p-4">
          <div className="text-3xl font-bold text-green-600">{result.imported}</div>
          <div className="text-sm text-gray-600">Imported</div>
        </div>
        
        {result.skipped > 0 && (
          <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
            <div className="text-3xl font-bold text-yellow-600">{result.skipped}</div>
            <div className="text-sm text-gray-600">Skipped</div>
          </div>
        )}
      </div>

      {/* Next Steps */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 text-left max-w-md mx-auto">
        <p className="font-semibold text-blue-900 mb-2">What's next?</p>
        <ul className="list-disc list-inside space-y-1 text-sm text-blue-800">
          <li>View test cases in the Test Cases section</li>
          <li>Check coverage statistics</li>
          <li>Link test methods using @TestCaseId annotation</li>
          <li>Analyze gaps (which test cases need automation)</li>
        </ul>
      </div>

      {/* Actions */}
      <div className="pt-4">
        <button
          onClick={onClose}
          className="px-8 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-semibold"
        >
          Done
        </button>
      </div>
    </div>
  );
};

