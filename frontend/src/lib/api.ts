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

export interface ScanStatus {
  isScanning: boolean;
  lastScanTime: string | null;
  lastScanStatus: string;
  lastScanError: string | null;
  repositoryHubPath: string;
  repositoryListFile: string;
  tempCloneMode: boolean;
  timestamp: number;
}

export interface ScanSession {
  id: number;
  scanDate: string;
  scanDirectory: string;
  totalRepositories: number;
  totalTestClasses: number;
  totalTestMethods: number;
  totalAnnotatedMethods: number;
  scanDurationMs: number;
  scanStatus: string;
  errorLog: string | null;
  metadata: string | null;
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
  },

  // Scan endpoints
  scan: {
    trigger: (): Promise<{ success: boolean; message: string; timestamp: number }> =>
      apiClient.post('/scan/trigger').then(res => res.data),
    
    getStatus: (): Promise<ScanStatus> =>
      apiClient.get('/scan/status').then(res => res.data),
    
    getConfig: (): Promise<Omit<ScanStatus, 'isScanning' | 'lastScanTime' | 'lastScanStatus' | 'lastScanError'>> =>
      apiClient.get('/scan/config').then(res => res.data),
    
    getSessions: (limit: number = 10): Promise<ScanSession[]> =>
      apiClient.get(`/scan/sessions?limit=${limit}`).then(res => res.data),
    
    getHealth: (): Promise<{ status: string; service: string; databaseAvailable: boolean; timestamp: number }> =>
      apiClient.get('/scan/health').then(res => res.data),
  },

  // Debug endpoints
  debug: {
    getDatabaseInfo: (): Promise<{ persistenceFacadeAvailable: boolean; status: string }> =>
      apiClient.get('/debug/database-info').then(res => res.data),
    
    getTableCounts: (): Promise<{ repositories: number; teams: number; recentScanSessions: number; status: string }> =>
      apiClient.get('/debug/table-counts').then(res => res.data),
  },
};

export default api;
