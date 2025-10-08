import axios from 'axios';

const API_BASE_URL = 'http://localhost:8090/api';

export interface ExcelPreviewResponse {
  columns: string[];
  previewData: Record<string, string>[];
  suggestedMappings: Record<string, string>;
  confidence: Record<string, number>;
  validation: {
    valid: boolean;
    missingRequiredFields: string[];
    suggestions: string[];
  };
  suggestedDataStartRow: number;
}

export interface ValidationResponse {
  valid: boolean;
  missingRequiredFields: string[];
  suggestions: string[];
}

export interface ImportResponse {
  success: boolean;
  imported: number;
  skipped: number;
  message?: string;
  errors?: string[];
  suggestions?: string[];
}

export interface TestCase {
  internalId: number;        // Internal database ID (primary key)
  externalId: string;        // External test case ID from test management system (TC-1234, etc.)
  title: string;
  steps: string;
  setup?: string;
  teardown?: string;
  expectedResult?: string;
  priority?: string;
  type?: string;
  status?: string;
  tags?: string[];
  requirements?: string[];
  customFields?: Record<string, unknown>;
  createdDate?: string;
  updatedDate?: string;
  createdBy?: string;
  organization?: string;
  
  // Legacy field for backward compatibility (returns externalId)
  id?: string;
}

export interface CoverageStats {
  total: number;
  automated: number;
  manual: number;
  coveragePercentage: number;
}

/**
 * Upload Excel file and get preview with auto-detected column mappings
 */
export const previewExcelFile = async (file: File): Promise<ExcelPreviewResponse> => {
  const formData = new FormData();
  formData.append('file', file);

  const response = await axios.post(
    `${API_BASE_URL}/testcases/upload/preview`,
    formData,
    {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    }
  );

  return response.data;
};

/**
 * Validate column mappings
 */
export const validateMappings = async (
  mappings: Record<string, string>,
  columns: string[]
): Promise<ValidationResponse> => {
  const response = await axios.post(
    `${API_BASE_URL}/testcases/upload/validate`,
    { mappings, columns }
  );

  return response.data;
};

/**
 * Import test cases with column mappings
 */
export const importTestCases = async (
  file: File,
  mappings: Record<string, string>,
  dataStartRow: number,
  replaceExisting: boolean = true,
  createdBy: string = 'system',
  organization: string = 'default'
): Promise<ImportResponse> => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('mappings', JSON.stringify(mappings));
  formData.append('dataStartRow', dataStartRow.toString());
  formData.append('replaceExisting', replaceExisting.toString());
  formData.append('createdBy', createdBy);
  formData.append('organization', organization);

  const response = await axios.post(
    `${API_BASE_URL}/testcases/upload/import`,
    formData,
    {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    }
  );

  return response.data;
};

/**
 * Get all test cases
 */
export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  total: number;
}

export const getAllTestCases = async (params?: { page?: number; size?: number; organization?: string; type?: string; priority?: string }): Promise<PageResponse<TestCase>> => {
  const response = await axios.get(`${API_BASE_URL}/testcases`, { params });
  return response.data as PageResponse<TestCase>;
};

/**
 * Get single test case by internal ID
 */
export const getTestCaseById = async (internalId: number): Promise<TestCase> => {
  const response = await axios.get(`${API_BASE_URL}/testcases/${internalId}`);
  return response.data;
};

/**
 * Get coverage statistics
 */
export const getCoverageStats = async (): Promise<CoverageStats> => {
  const response = await axios.get(`${API_BASE_URL}/testcases/stats/coverage`);
  return response.data;
};

/**
 * Get untested test cases (gaps)
 */
export const getUntestedCases = async (params?: { page?: number; size?: number }): Promise<PageResponse<TestCase>> => {
  const response = await axios.get(`${API_BASE_URL}/testcases/gaps`, { params });
  return response.data as PageResponse<TestCase>;
};

/**
 * Delete test case by internal ID
 */
export const deleteTestCase = async (internalId: number): Promise<void> => {
  await axios.delete(`${API_BASE_URL}/testcases/${internalId}`);
};

