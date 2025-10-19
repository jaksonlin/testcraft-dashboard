import axios from 'axios';

// API Configuration
const API_BASE_URL = 'http://localhost:8090/api';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor for logging
apiClient.interceptors.request.use(
  (config) => {
    console.log(`API Request: ${config.method?.toUpperCase()} ${config.url}`);
    return config;
  },
  (error) => {
    console.error('API Request Error:', error);
    return Promise.reject(error);
  }
);

// Response interceptor for error handling
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    console.error('API Response Error:', error.response?.data || error.message);
    return Promise.reject(error);
  }
);

// Types for API responses
export interface DashboardOverview {
  totalRepositories: number;
  totalTeams: number;
  totalTestClasses: number;
  totalTestMethods: number;
  totalAnnotatedMethods: number;
  overallCoverageRate: number;
  lastScanDate: string | null;
  topTeams: TeamSummary[] | null;
  topRepositories: RepositorySummary[] | null;
}

export interface TeamSummary {
  teamId: number;
  teamName: string;
  teamCode: string;
  repositoryCount: number;
  testClassCount: number;
  testMethodCount: number;
  annotatedMethodCount: number;
  coverageRate: number;
}

export interface TeamMetrics {
  id: number;
  teamName: string;
  teamCode: string;
  department?: string;
  repositoryCount: number;
  totalTestClasses: number;
  totalTestMethods: number;
  totalAnnotatedMethods: number;
  averageCoverageRate: number;
  lastScanDate?: string;
  repositories: RepositorySummary[];
}

export interface RepositorySummary {
  repositoryId: number;
  repositoryName: string;
  gitUrl: string;
  testClassCount: number;
  testMethodCount: number;
  annotatedMethodCount: number;
  coverageRate: number;
  lastScanDate: string;
  teamName: string;
}

export interface RepositoryDetail {
  id: number;
  repository: string;
  path: string;
  gitUrl: string;
  testClasses: number;
  testMethodCount: number;
  annotatedMethods: number;
  coverageRate: number;
  lastScan: string;
  teamName: string;
  teamCode: string;
}

export interface TestMethodDetail {
  id: number;
  repository: string;
  testClass: string;
  testMethod: string;
  line: number;
  title: string;
  author: string;
  status: string;
  targetClass: string;
  targetMethod: string;
  description: string;
  testPoints: string;
  tags: string[];
  requirements: string[];
  testCaseIds: string[];
  defects: string[];
  lastModified: string | null;
  lastUpdateAuthor: string;
  teamName: string;
  teamCode: string;
  gitUrl: string;
}

export interface TestClassSummary {
  id: number;
  className: string;
  packageName: string;
  filePath: string;
  testMethodCount: number;
  annotatedMethodCount: number;
  coverageRate: number;
  lastModifiedDate: string;
}

export interface ScanStatus {
  isScanning: boolean;
  lastScanTime: string | null;
  lastScanStatus: string;
  lastScanError: string | null;
  repositoryHubPath: string;
  repositoryListFile: string;
  tempCloneMode: boolean;
  maxRepositoriesPerScan: number;
  schedulerEnabled: boolean;
  dailyScanCron: string;
  timestamp: number;
}

export interface ScanConfig {
  tempCloneMode: boolean;
  repositoryHubPath: string;
  repositoryListFile: string;
  maxRepositoriesPerScan: number;
  schedulerEnabled: boolean;
  dailyScanCron: string;
  timestamp: number;
}

export interface DailyMetric {
  id: number;
  date: string;
  totalRepositories: number;
  totalTestClasses: number;
  totalTestMethods: number;
  totalAnnotatedMethods: number;
  overallCoverageRate: number;
  newTestMethods: number;
  newAnnotatedMethods: number;
}

export interface AnalyticsOverview {
  totalDaysTracked: number;
  averageCoverageRate: number;
  coverageTrend: 'up' | 'down' | 'stable';
  totalGrowth: {
    repositories: number;
    testMethods: number;
    annotatedMethods: number;
  };
  recentActivity: {
    lastWeek: number;
    lastMonth: number;
  };
}

// Grouped test method data structures
export interface GroupedTestMethodResponse {
  teams: TeamGroup[];
  summary: Summary;
}

export interface TeamGroup {
  teamName: string;
  teamCode: string;
  classes: ClassGroup[];
  summary: TeamSummary;
}

export interface ClassGroup {
  className: string;
  packageName: string;
  repository: string;
  methods: TestMethodDetail[];
  summary: ClassSummary;
}

export interface Summary {
  totalTeams: number;
  totalClasses: number;
  totalMethods: number;
  totalAnnotatedMethods: number;
  overallCoverageRate: number;
}

export interface TeamSummary {
  totalClasses: number;
  totalMethods: number;
  annotatedMethods: number;
  coverageRate: number;
}

export interface ClassSummary {
  totalMethods: number;
  annotatedMethods: number;
  coverageRate: number;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface ExportRequest {
  dataType: 'test-methods' | 'repositories' | 'teams';
  format: 'csv' | 'excel' | 'json';
  scope: 'all' | 'filtered';
  filters?: {
    teamName?: string;
    repositoryName?: string;
    annotated?: boolean;
  };
  filename?: string;
}

export interface ExportStatus {
  jobId: string;
  status: 'pending' | 'processing' | 'completed' | 'failed' | 'cancelled';
  progress: number;
  message: string;
  createdAt: string;
  completedAt?: string;
  downloadUrl?: string;
  filename?: string;
  totalRecords: number;
  processedRecords: number;
  errorMessage?: string;
}

export interface ScanSession {
  id: number;
  startTime: string;
  endTime: string;
  status: string;
  totalRepositories: number;
  successfulRepositories: number;
  failedRepositories: number;
  totalTestClasses: number;
  totalTestMethods: number;
  totalAnnotatedMethods: number;
  overallCoverageRate: number;
}

// API Methods
export const api = {
  // Dashboard endpoints
  dashboard: {
    getOverview: (): Promise<DashboardOverview> =>
      apiClient.get('/dashboard/overview').then(res => res.data),
    
    getTeamMetrics: (): Promise<TeamSummary[]> =>
      apiClient.get('/dashboard/teams').then(res => res.data),
    
    getRepositoryMetrics: (): Promise<RepositorySummary[]> =>
      apiClient.get('/dashboard/repositories').then(res => res.data),
    
    getRepositoryDetails: (): Promise<RepositoryDetail[]> =>
      apiClient.get('/dashboard/repositories/details').then(res => res.data),
    
    getTestMethodDetails: (teamId?: number, limit?: number): Promise<TestMethodDetail[]> => {
      const params = new URLSearchParams();
      if (teamId) params.append('teamId', teamId.toString());
      if (limit) params.append('limit', limit.toString());
      const queryString = params.toString();
      return apiClient.get(`/dashboard/test-methods/details${queryString ? `?${queryString}` : ''}`).then(res => res.data);
    },
    
    getAllTestMethodDetails: (limit?: number): Promise<TestMethodDetail[]> =>
      apiClient.get(`/dashboard/test-methods/all${limit ? `?limit=${limit}` : ''}`).then(res => res.data),
    
    // Paginated test method details for better performance
    getTestMethodDetailsPaginated: (page: number, size: number, teamName?: string, repositoryName?: string, annotated?: boolean): Promise<PagedResponse<TestMethodDetail>> =>
      apiClient.get(`/dashboard/test-methods/paginated?page=${page}&size=${size}${teamName ? `&teamName=${teamName}` : ''}${repositoryName ? `&repositoryName=${repositoryName}` : ''}${annotated !== undefined ? `&annotated=${annotated}` : ''}`).then(res => res.data),
    
    // Grouped test method details for hierarchical display
    getAllTestMethodDetailsGrouped: (limit?: number): Promise<GroupedTestMethodResponse> =>
      apiClient.get(`/dashboard/test-methods/grouped${limit ? `?limit=${limit}` : ''}`).then(res => res.data),
    
    // Get global test method statistics (not limited to current page)
    getGlobalTestMethodStats: (organization?: string, teamId?: number, repositoryName?: string, annotated?: boolean): Promise<{
      totalMethods: number;
      totalAnnotated: number;
      totalNotAnnotated: number;
      coverageRate: number;
    }> => {
      const params = new URLSearchParams();
      if (organization) params.append('organization', organization);
      if (teamId) params.append('teamId', teamId.toString());
      if (repositoryName) params.append('repositoryName', repositoryName);
      if (annotated !== undefined) params.append('annotated', annotated.toString());
      const queryString = params.toString();
      return apiClient.get(`/dashboard/test-methods/stats/global${queryString ? `?${queryString}` : ''}`).then(res => res.data);
    },
  },

  // Repository endpoints
  repositories: {
    getAll: (): Promise<RepositorySummary[]> =>
      apiClient.get('/repositories').then(res => res.data),

    getPaginated: (page: number, size: number, search?: string, team?: string, coverage?: string, testMethods?: string, lastScan?: string, sortBy?: string, sortOrder?: string): Promise<PagedResponse<RepositorySummary>> =>
      apiClient.get(`/repositories/paginated?page=${page}&size=${size}${search ? `&search=${encodeURIComponent(search)}` : ''}${team ? `&team=${encodeURIComponent(team)}` : ''}${coverage ? `&coverage=${coverage}` : ''}${testMethods ? `&testMethods=${testMethods}` : ''}${lastScan ? `&lastScan=${lastScan}` : ''}${sortBy ? `&sortBy=${sortBy}` : ''}${sortOrder ? `&sortOrder=${sortOrder}` : ''}`).then(res => res.data),
    
    getById: (id: number): Promise<RepositoryDetail> =>
      apiClient.get(`/repositories/${id}`).then(res => res.data),
    
    getTestMethods: (repositoryId: number, limit?: number): Promise<TestMethodDetail[]> => {
      const params = new URLSearchParams();
      if (limit) params.append('limit', limit.toString());
      const queryString = params.toString();
      return apiClient.get(`/repositories/${repositoryId}/test-methods${queryString ? `?${queryString}` : ''}`).then(res => res.data);
    },

    // New: repository classes and class methods
    getClasses: (repositoryId: number): Promise<TestClassSummary[]> =>
      apiClient.get(`/repositories/${repositoryId}/classes`).then(res => res.data),

    getClassesPaginated: (repositoryId: number, page: number, size: number, className?: string, annotated?: boolean): Promise<PagedResponse<TestClassSummary>> =>
      apiClient.get(`/repositories/${repositoryId}/classes/paginated?page=${page}&size=${size}${className ? `&className=${className}` : ''}${annotated !== undefined ? `&annotated=${annotated}` : ''}`).then(res => res.data),

    getClassMethods: (repositoryId: number, classId: number, limit: number = 200): Promise<TestMethodDetail[]> =>
      apiClient.get(`/repositories/${repositoryId}/classes/${classId}/methods?limit=${limit}`).then(res => res.data),
    
    getByTeam: (teamId: number): Promise<RepositorySummary[]> =>
      apiClient.get(`/repositories/team/${teamId}`).then(res => res.data),
    
    search: (name?: string, team?: string, coverage?: string): Promise<RepositorySummary[]> => {
      const params = new URLSearchParams();
      if (name) params.append('name', name);
      if (team) params.append('team', team);
      if (coverage) params.append('coverage', coverage);
      const queryString = params.toString();
      return apiClient.get(`/repositories/search${queryString ? `?${queryString}` : ''}`).then(res => res.data);
    },
  },

  // Team endpoints
  teams: {
    getAll: (): Promise<TeamMetrics[]> =>
      apiClient.get('/dashboard/teams').then(res => res.data),
    
    getPaginated: (page: number, size: number, search?: string, sortBy?: string, sortOrder?: string): Promise<PagedResponse<TeamMetrics>> => {
      const params = new URLSearchParams();
      params.append('page', page.toString());
      params.append('size', size.toString());
      if (search) params.append('search', search);
      if (sortBy) params.append('sortBy', sortBy);
      if (sortOrder) params.append('sortOrder', sortOrder);
      return apiClient.get(`/teams/paginated?${params.toString()}`).then(res => res.data);
    },
    
    getById: (id: number): Promise<TeamMetrics> =>
      apiClient.get(`/teams/${id}`).then(res => res.data),
    
    getRepositories: (teamId: number): Promise<RepositorySummary[]> =>
      apiClient.get(`/teams/${teamId}/repositories`).then(res => res.data),
    
    getComparison: (): Promise<TeamMetrics[]> =>
      apiClient.get('/teams/comparison').then(res => res.data),
  },

  // Scan endpoints
  scan: {
    trigger: (): Promise<{ success: boolean; message: string; timestamp: number }> =>
      apiClient.post('/scan/trigger').then(res => res.data),
    
    getStatus: (): Promise<ScanStatus> =>
      apiClient.get('/scan/status').then(res => res.data),
    
    getConfig: (): Promise<ScanConfig> =>
      apiClient.get('/scan/config').then(res => res.data),
    
    updateConfig: (config: Partial<ScanConfig>): Promise<{ success: boolean; message: string; timestamp: number }> =>
      apiClient.put('/scan/config', config).then(res => res.data),
    
        getSessions: (limit: number = 10): Promise<ScanSession[]> =>
          apiClient.get(`/scan/sessions?limit=${limit}`).then(res => res.data),
        
        getHistory: (limit: number = 20): Promise<ScanSession[]> =>
          apiClient.get(`/scan/sessions?limit=${limit}`).then(res => res.data),
    
    getHealth: (): Promise<{ status: string; service: string; databaseAvailable: boolean; timestamp: number }> =>
      apiClient.get('/scan/health').then(res => res.data),
  },

  // Analytics endpoints
  analytics: {
    getDailyMetrics: (days: number = 30): Promise<DailyMetric[]> =>
      apiClient.get(`/analytics/daily-metrics?days=${days}`).then(res => res.data),
    
    getCoverageTrend: (days: number = 30): Promise<{ date: string; coverage: number }[]> =>
      apiClient.get(`/analytics/coverage-trend?days=${days}`).then(res => res.data),
    
    getTeamComparison: (): Promise<TeamMetrics[]> =>
      apiClient.get('/analytics/team-comparison').then(res => res.data),
    
    getGrowthMetrics: (days: number = 30): Promise<{ date: string; repositories: number; testMethods: number; annotatedMethods: number }[]> =>
      apiClient.get(`/analytics/growth-metrics?days=${days}`).then(res => res.data),
    
    getOverview: (): Promise<AnalyticsOverview> =>
      apiClient.get('/analytics/overview').then(res => res.data),
  },

  // Debug endpoints
  debug: {
    getDatabaseInfo: (): Promise<{ persistenceFacadeAvailable: boolean; status: string }> =>
      apiClient.get('/debug/database-info').then(res => res.data),
    
    getTableCounts: (): Promise<{ repositories: number; teams: number; recentScanSessions: number; status: string }> =>
      apiClient.get('/debug/table-counts').then(res => res.data),
  },

  // Export endpoints
  export: {
    initiate: (request: ExportRequest): Promise<ExportStatus> =>
      apiClient.post('/export/initiate', request).then(res => res.data),
    
    getStatus: (jobId: string): Promise<ExportStatus> =>
      apiClient.get(`/export/status/${jobId}`).then(res => res.data),
    
    download: (jobId: string): Promise<Blob> =>
      apiClient.get(`/export/download/${jobId}`, { responseType: 'blob' }).then(res => res.data),
    
    cancel: (jobId: string): Promise<void> =>
      apiClient.delete(`/export/cancel/${jobId}`).then(res => res.data),
    
    cleanup: (): Promise<void> =>
      apiClient.delete('/export/cleanup').then(res => res.data),
  },
};

export default api;
