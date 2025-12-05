import { apiClient } from './api';

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
  suggestedHeaderRow: number;
  suggestedDataStartRow: number;
  totalRows: number;
}

export interface ValidationResponse {
  valid: boolean;
  missingRequiredFields: string[];
  suggestions: string[];
}

export interface ImportResponse {
  success: boolean;
  imported: number;
  created: number;
  updated: number;
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

  // Team association
  teamId?: number;
  teamName?: string;

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

  const response = await apiClient.post('/testcases/upload/preview', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });

  return response.data;
};

export const previewExcelWithRows = async (file: File, headerRow: number, dataStartRow: number): Promise<ExcelPreviewResponse> => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('headerRow', headerRow.toString());
  formData.append('dataStartRow', dataStartRow.toString());

  const response = await apiClient.post('/testcases/upload/preview-with-rows', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });

  return response.data;
};

/**
 * Validate column mappings
 */
export const validateMappings = async (
  mappings: Record<string, string>,
  columns: string[]
): Promise<ValidationResponse> => {
  const response = await apiClient.post('/testcases/upload/validate', { mappings, columns });

  return response.data;
};

/**
 * Import test cases with column mappings
 */
export const importTestCases = async (
  file: File,
  mappings: Record<string, string>,
  headerRow: number,
  dataStartRow: number,
  replaceExisting: boolean = true,
  createdBy: string = 'system',
  teamId?: number
): Promise<ImportResponse> => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('mappings', JSON.stringify(mappings));
  formData.append('headerRow', headerRow.toString());
  formData.append('dataStartRow', dataStartRow.toString());
  formData.append('replaceExisting', replaceExisting.toString());
  formData.append('createdBy', createdBy);
  if (teamId) {
    formData.append('teamId', teamId.toString());
  }

  const response = await apiClient.post('/testcases/upload/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });

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

export const getAllTestCases = async (params?: { page?: number; size?: number; type?: string; priority?: string; teamId?: number; status?: string; search?: string }): Promise<PageResponse<TestCase>> => {
  const response = await apiClient.get('/testcases', { params });
  return response.data as PageResponse<TestCase>;
};

/**
 * Get single test case by internal ID
 */
export const getTestCaseById = async (internalId: number): Promise<TestCase> => {
  const response = await apiClient.get(`/testcases/${internalId}`);
  return response.data;
};

/**
 * Get coverage statistics
 */
export const getCoverageStats = async (): Promise<CoverageStats> => {
  const response = await apiClient.get('/testcases/stats/coverage');
  return response.data;
};

/**
 * Get untested test cases (gaps)
 */
export const getUntestedCases = async (params?: { 
  page?: number; 
  size?: number;
  type?: string;
  priority?: string;
  teamId?: number;
  status?: string;
  search?: string;
}): Promise<PageResponse<TestCase>> => {
  const response = await apiClient.get('/testcases/gaps', { params });
  return response.data as PageResponse<TestCase>;
};

/**
 * Delete test case by internal ID
 */
export const deleteTestCase = async (internalId: number): Promise<void> => {
  await apiClient.delete(`/testcases/${internalId}`);
};

/**
 * Delete all test cases matching filters (bulk deletion)
 * WARNING: This is a destructive operation!
 * Requires at least one filter and explicit confirmation.
 * 
 * @param filters - Filter criteria (organization, teamId, type, priority, status, search)
 * @param confirm - Must be true to execute (safety check)
 * @returns Number of deleted test cases
 */
export const deleteAllTestCases = async (
  filters: {
    teamId?: number;
    type?: string;
    priority?: string;
    status?: string;
    search?: string;
  },
  confirm: boolean = false
): Promise<{ success: boolean; deleted: number; message: string }> => {
  const params = new URLSearchParams();

  if (filters.teamId) params.append('teamId', filters.teamId.toString());
  if (filters.type) params.append('type', filters.type);
  if (filters.priority) params.append('priority', filters.priority);
  if (filters.status) params.append('status', filters.status);
  if (filters.search) params.append('search', filters.search);
  params.append('confirm', confirm.toString());

  const response = await apiClient.delete(`/testcases?${params.toString()}`);
  return response.data;
};

/**
 * Get distinct organizations for filter dropdown
 */
export const getOrganizations = async (): Promise<string[]> => {
  const response = await apiClient.get('/testcases/organizations');
  return response.data;
};

/**
 * Team interface for filter dropdown
 */
export interface Team {
  id: number;
  name: string;
}

/**
 * Get all teams for filter dropdown
 */
export const getTeams = async (): Promise<Team[]> => {
  const response = await apiClient.get('/testcases/teams');
  return response.data;
};

