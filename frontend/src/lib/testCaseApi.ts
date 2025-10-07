import axios from 'axios';

const API_BASE_URL = 'http://localhost:8090';

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
  id: string;
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
  customFields?: Record<string, any>;
  createdDate?: string;
  updatedDate?: string;
  createdBy?: string;
  organization?: string;
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
    `${API_BASE_URL}/api/testcases/upload/preview`,
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
    `${API_BASE_URL}/api/testcases/upload/validate`,
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
    `${API_BASE_URL}/api/testcases/upload/import`,
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
export const getAllTestCases = async (): Promise<{ testCases: TestCase[]; total: number }> => {
  const response = await axios.get(`${API_BASE_URL}/api/testcases`);
  return response.data;
};

/**
 * Get single test case by ID
 */
export const getTestCaseById = async (id: string): Promise<TestCase> => {
  const response = await axios.get(`${API_BASE_URL}/api/testcases/${id}`);
  return response.data;
};

/**
 * Get coverage statistics
 */
export const getCoverageStats = async (): Promise<CoverageStats> => {
  const response = await axios.get(`${API_BASE_URL}/api/testcases/stats/coverage`);
  return response.data;
};

/**
 * Get untested test cases (gaps)
 */
export const getUntestedCases = async (): Promise<{ untestedCases: TestCase[]; count: number }> => {
  const response = await axios.get(`${API_BASE_URL}/api/testcases/gaps`);
  return response.data;
};

/**
 * Delete test case
 */
export const deleteTestCase = async (id: string): Promise<void> => {
  await axios.delete(`${API_BASE_URL}/api/testcases/${id}`);
};

