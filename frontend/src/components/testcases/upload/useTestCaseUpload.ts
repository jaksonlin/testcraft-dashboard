/**
 * Custom hook for Test Case Upload business logic
 */

import { useState } from 'react';
import { 
  previewExcelFile, 
  previewExcelWithRows, 
  validateMappings, 
  importTestCases 
} from '../../../lib/testCaseApi';
import type { ExcelPreviewResponse, ImportResponse } from '../../../lib/testCaseApi';
import type { WizardStep } from './types';

export const useTestCaseUpload = (onComplete?: () => void) => {
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
  
  // Team selection (organization now comes from system setting on backend)
  const [teamId, setTeamId] = useState<string>('');
  const [createdBy, setCreatedBy] = useState<string>('system');

  /**
   * Handle file upload and initial preview
   */
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

  /**
   * Handle mapping change and re-validate
   */
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

  /**
   * Handle row changes and update preview
   */
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

  /**
   * Handle advancing to preview step - refresh preview data
   */
  const handleAdvanceToPreview = async () => {
    if (!file) return;
    
    try {
      // Refresh preview with current headerRow and dataStartRow settings
      const updatedPreview = await previewExcelWithRows(file, headerRow, dataStartRow);
      setPreview(updatedPreview);
      
      // Validate that current mappings are still valid with the refreshed columns
      const currentMappedColumns = Object.keys(mappings);
      const newColumns = updatedPreview.columns;
      const invalidMappings = currentMappedColumns.filter(col => !newColumns.includes(col));
      
      if (invalidMappings.length > 0) {
        console.warn('Some mappings reference columns that no longer exist:', invalidMappings);
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

  /**
   * Handle import execution
   */
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
        createdBy || 'system',
        teamId ? Number(teamId) : undefined
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

  /**
   * Handle completion and callback
   */
  const handleComplete = () => {
    if (onComplete) {
      onComplete();
    }
  };

  return {
    // State
    currentStep,
    file,
    preview,
    mappings,
    headerRow,
    dataStartRow,
    isValidMapping,
    missingFields,
    suggestions,
    importing,
    importResult,
    teamId,
    createdBy,
    
    // Actions
    setCurrentStep,
    handleFileSelect,
    handleMappingChange,
    handleRowChange,
    handleAdvanceToPreview,
    handleImport,
    handleComplete,
    setTeamId,
    setCreatedBy,
  };
};
