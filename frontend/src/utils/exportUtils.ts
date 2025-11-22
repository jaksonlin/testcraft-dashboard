import { type RepositorySummary, type TeamMetrics, type DailyMetric, type TestMethodDetail, type GroupedTestMethodResponse, type TestClassSummary } from '../lib/api';
import * as XLSX from 'xlsx';

export type ExportFormat = 'csv' | 'json' | 'excel';
export type ExportScope = 'all' | 'selected' | 'filtered';

export interface ExportData {
  repositories?: RepositorySummary[];
  teams?: TeamMetrics[];
  analytics?: DailyMetric[];
  methods?: TestMethodDetail[];
  groupedMethods?: GroupedTestMethodResponse;
  classes?: TestClassSummary[];
  scanHistory?: any[];
  dashboardOverview?: any;
  exportDate?: Date;
  metadata: {
    exportDate: string;
    totalItems: number;
    dataType: string;
    scope: ExportScope;
  };
}

export interface ExportOption {
  id: string;
  label: string;
  description: string;
  format: ExportFormat;
  scope: ExportScope;
  filename: string;
}

// Excel Sheet Creation Functions
const createMetadataSheet = (data: ExportData): XLSX.WorkSheet => {
  const metadata = [
    ['Export Information', ''],
    ['Export Date', data.metadata.exportDate],
    ['Data Type', data.metadata.dataType],
    ['Total Items', data.metadata.totalItems],
    ['Scope', data.metadata.scope],
    ['', ''],
    ['Sheet Contents', ''],
    ['Repositories', data.repositories ? `${data.repositories.length} items` : 'Not included'],
    ['Teams', data.teams ? `${data.teams.length} items` : 'Not included'],
    ['Analytics', data.analytics ? `${data.analytics.length} items` : 'Not included'],
    ['Test Method Details', data.methods ? `${data.methods.length} items` : 'Not included'],
    ['Grouped Test Methods', data.groupedMethods ? 'Included' : 'Not included']
  ];
  
  return XLSX.utils.aoa_to_sheet(metadata);
};

const createRepositoriesSheet = (repositories: RepositorySummary[]): XLSX.WorkSheet => {
  const headers = [
    'Repository ID', 'Repository Name', 'Team Name', 'Team Code', 'Department', 
    'Git URL', 'Test Classes', 'Test Methods', 'Annotated Methods', 
    'Coverage Rate (%)', 'Last Scan Date'
  ];
  
  const data = repositories.map(repo => [
    repo.id,
    repo.repositoryName,
    repo.teamName,
    '', // teamCode - not available in RepositorySummary
    '', // department - not available in RepositorySummary
    repo.gitUrl,
    repo.testClassCount,
    repo.testMethodCount,
    repo.annotatedMethodCount || 0,
    repo.coverageRate.toFixed(2),
    new Date(repo.lastScanDate).toLocaleString()
  ]);
  
  return XLSX.utils.aoa_to_sheet([headers, ...data]);
};

const createTeamsSheet = (teams: TeamMetrics[]): XLSX.WorkSheet => {
  const headers = [
    'Team ID', 'Team Name', 'Team Code', 'Department', 'Repository Count',
    'Total Test Classes', 'Total Test Methods', 'Total Annotated Methods',
    'Average Coverage Rate (%)', 'Last Scan Date'
  ];
  
  const data = teams.map(team => [
    team.id,
    team.teamName,
    team.teamCode,
    team.department || '',
    team.repositoryCount,
    team.totalTestClasses,
    team.totalTestMethods,
    team.totalAnnotatedMethods || 0,
    team.averageCoverageRate.toFixed(2),
    team.lastScanDate ? new Date(team.lastScanDate).toLocaleString() : 'N/A'
  ]);
  
  return XLSX.utils.aoa_to_sheet([headers, ...data]);
};

const createAnalyticsSheet = (analytics: DailyMetric[]): XLSX.WorkSheet => {
  const headers = [
    'Date', 'Total Repositories', 'Total Test Classes', 'Total Test Methods',
    'Total Annotated Methods', 'Overall Coverage Rate (%)', 'New Test Methods',
    'New Annotated Methods'
  ];
  
  const data = analytics.map(metric => [
    metric.date,
    metric.totalRepositories,
    metric.totalTestClasses,
    metric.totalTestMethods,
    metric.totalAnnotatedMethods,
    metric.overallCoverageRate.toFixed(2),
    metric.newTestMethods,
    metric.newAnnotatedMethods
  ]);
  
  return XLSX.utils.aoa_to_sheet([headers, ...data]);
};

const createTestMethodDetailsSheet = (methods: TestMethodDetail[]): XLSX.WorkSheet => {
  const headers = [
    'Method ID', 'Repository', 'Test Class', 'Test Method', 'Line Number',
    'Title', 'Author', 'Test Status', 'Target Class', 'Target Method',
    'Description', 'Test Points', 'Tags', 'Requirements', 'Test Case IDs',
    'Defects', 'Last Modified', 'Last Update Author', 'Team Name', 'Team Code', 'Git URL'
  ];
  
  const data = methods.map(method => [
    method.id,
    method.repository,
    method.testClass,
    method.testMethod,
    method.line,
    method.title || '',
    method.author || '',
    method.status || '',
    method.targetClass || '',
    method.targetMethod || '',
    method.description || '',
    method.testPoints || '',
    method.tags.join('; ') || '',
    method.requirements.join('; ') || '',
    method.testCaseIds.join('; ') || '',
    method.defects.join('; ') || '',
    method.lastModified ? new Date(method.lastModified).toLocaleString() : '',
    method.lastUpdateAuthor || '',
    method.teamName || '',
    method.teamCode || '',
    method.gitUrl || ''
  ]);
  
  return XLSX.utils.aoa_to_sheet([headers, ...data]);
};

const createGroupedMethodsSheet = (groupedData: GroupedTestMethodResponse): XLSX.WorkSheet => {
  const headers = [
    'Team Name', 'Team Code', 'Class Name', 'Package Name', 'Repository',
    'Method ID', 'Test Method', 'Line Number', 'Title', 'Author',
    'Test Status', 'Target Class', 'Target Method', 'Description',
    'Test Points', 'Tags', 'Requirements', 'Test Case IDs', 'Defects',
    'Last Modified', 'Last Update Author', 'Git URL'
  ];
  
  const data: (string | number)[][] = [];
  
  groupedData.teams.forEach(team => {
    team.classes.forEach(classGroup => {
      classGroup.methods.forEach(method => {
        data.push([
          team.teamName,
          team.teamCode,
          classGroup.className,
          classGroup.packageName,
          classGroup.repository,
          method.id,
          method.testMethod,
          method.line,
          method.title || '',
          method.author || '',
          method.status || '',
          method.targetClass || '',
          method.targetMethod || '',
          method.description || '',
          method.testPoints || '',
          method.tags.join('; ') || '',
          method.requirements.join('; ') || '',
          method.testCaseIds.join('; ') || '',
          method.defects.join('; ') || '',
          method.lastModified ? new Date(method.lastModified).toLocaleString() : '',
          method.lastUpdateAuthor || '',
          method.gitUrl || ''
        ]);
      });
    });
  });
  
  return XLSX.utils.aoa_to_sheet([headers, ...data]);
};

// CSV Export Functions
export const exportToCSV = (data: ExportData, filename: string): void => {
  let csvContent = '';
  
  // Add metadata header
  csvContent += 'Export Metadata\n';
  csvContent += `Export Date,${data.metadata.exportDate}\n`;
  csvContent += `Data Type,${data.metadata.dataType}\n`;
  csvContent += `Total Items,${data.metadata.totalItems}\n`;
  csvContent += `Scope,${data.metadata.scope}\n`;
  csvContent += '\n';

  // Export repositories
  if (data.repositories && data.repositories.length > 0) {
    csvContent += 'Repositories\n';
    csvContent += 'Repository ID,Repository Name,Team Name,Team Code,Department,Git URL,Test Classes,Test Methods,Annotated Methods,Coverage Rate,Last Scan Date\n';
    
    data.repositories.forEach(repo => {
      csvContent += [
        repo.id,
        `"${repo.repositoryName}"`,
        `"${repo.teamName}"`,
        `""`, // teamCode - not available in RepositorySummary
        `""`, // department - not available in RepositorySummary
        `"${repo.gitUrl}"`,
        repo.testClassCount,
        repo.testMethodCount,
        repo.annotatedMethodCount || 0,
        repo.coverageRate.toFixed(2),
        `"${new Date(repo.lastScanDate).toLocaleString()}"`
      ].join(',') + '\n';
    });
    csvContent += '\n';
  }

  // Export teams
  if (data.teams && data.teams.length > 0) {
    csvContent += 'Teams\n';
    csvContent += 'Team ID,Team Name,Team Code,Department,Repository Count,Total Test Classes,Total Test Methods,Total Annotated Methods,Average Coverage Rate,Last Scan Date\n';
    
    data.teams.forEach(team => {
      csvContent += [
        team.id,
        `"${team.teamName}"`,
        `"${team.teamCode}"`,
        `"${team.department || ''}"`,
        team.repositoryCount,
        team.totalTestClasses,
        team.totalTestMethods,
        team.totalAnnotatedMethods || 0,
        team.averageCoverageRate.toFixed(2),
        `"${team.lastScanDate ? new Date(team.lastScanDate).toLocaleString() : 'N/A'}"`
      ].join(',') + '\n';
    });
    csvContent += '\n';
  }

  // Export analytics
  if (data.analytics && data.analytics.length > 0) {
    csvContent += 'Analytics\n';
    csvContent += 'Date,Total Repositories,Total Test Classes,Total Test Methods,Total Annotated Methods,Overall Coverage Rate,New Test Methods,New Annotated Methods\n';
    
    data.analytics.forEach(metric => {
      csvContent += [
        `"${metric.date}"`,
        metric.totalRepositories,
        metric.totalTestClasses,
        metric.totalTestMethods,
        metric.totalAnnotatedMethods,
        metric.overallCoverageRate.toFixed(2),
        metric.newTestMethods,
        metric.newAnnotatedMethods
      ].join(',') + '\n';
    });
    csvContent += '\n';
  }

  // Export test method details
  if (data.methods && data.methods.length > 0) {
    csvContent += 'Test Method Details\n';
    csvContent += 'Method ID,Repository,Test Class,Test Method,Line Number,Title,Author,Test Status,Target Class,Target Method,Description,Test Points,Tags,Requirements,Test Case IDs,Defects,Last Modified,Last Update Author,Team Name,Team Code,Git URL\n';
    
    data.methods.forEach(method => {
      csvContent += [
        method.id,
        `"${method.repository}"`,
        `"${method.testClass}"`,
        `"${method.testMethod}"`,
        method.line,
        `"${method.title || ''}"`,
        `"${method.author || ''}"`,
        `"${method.status || ''}"`,
        `"${method.targetClass || ''}"`,
        `"${method.targetMethod || ''}"`,
        `"${method.description || ''}"`,
        `"${method.testPoints || ''}"`,
        `"${method.tags.join('; ') || ''}"`,
        `"${method.requirements.join('; ') || ''}"`,
        `"${method.testCaseIds.join('; ') || ''}"`,
        `"${method.defects.join('; ') || ''}"`,
        `"${method.lastModified ? new Date(method.lastModified).toLocaleString() : ''}"`,
        `"${method.lastUpdateAuthor || ''}"`,
        `"${method.teamName || ''}"`,
        `"${method.teamCode || ''}"`,
        `"${method.gitUrl || ''}"`
      ].join(',') + '\n';
    });
    csvContent += '\n';
  }

  downloadFile(csvContent, filename, 'text/csv');
};

// JSON Export Functions
export const exportToJSON = (data: ExportData, filename: string): void => {
  const jsonContent = JSON.stringify(data, null, 2);
  downloadFile(jsonContent, filename, 'application/json');
};

// Excel Export Functions using XLSX library
export const exportToExcel = (data: ExportData, filename: string): void => {
  const workbook = XLSX.utils.book_new();
  
  // Add metadata sheet
  const metadataSheet = createMetadataSheet(data);
  XLSX.utils.book_append_sheet(workbook, metadataSheet, 'Export Info');
  
  // Add repositories sheet if data exists
  if (data.repositories && data.repositories.length > 0) {
    const repositoriesSheet = createRepositoriesSheet(data.repositories);
    XLSX.utils.book_append_sheet(workbook, repositoriesSheet, 'Repositories');
  }
  
  // Add teams sheet if data exists
  if (data.teams && data.teams.length > 0) {
    const teamsSheet = createTeamsSheet(data.teams);
    XLSX.utils.book_append_sheet(workbook, teamsSheet, 'Teams');
  }
  
  // Add analytics sheet if data exists
  if (data.analytics && data.analytics.length > 0) {
    const analyticsSheet = createAnalyticsSheet(data.analytics);
    XLSX.utils.book_append_sheet(workbook, analyticsSheet, 'Analytics');
  }
  
  // Add test method details sheet if data exists
  if (data.methods && data.methods.length > 0) {
    const methodsSheet = createTestMethodDetailsSheet(data.methods);
    XLSX.utils.book_append_sheet(workbook, methodsSheet, 'Test Method Details');
  }
  
  // Add grouped test methods sheet if data exists
  if (data.groupedMethods) {
    const groupedMethodsSheet = createGroupedMethodsSheet(data.groupedMethods);
    XLSX.utils.book_append_sheet(workbook, groupedMethodsSheet, 'Grouped Test Methods');
  }
  
  // Generate and download the Excel file
  XLSX.writeFile(workbook, filename);
};

// Generic export function
export const exportData = (data: ExportData, option: ExportOption): void => {
  switch (option.format) {
    case 'csv':
      exportToCSV(data, option.filename);
      break;
    case 'json':
      exportToJSON(data, option.filename);
      break;
    case 'excel':
      exportToExcel(data, option.filename);
      break;
    default:
      throw new Error(`Unsupported export format: ${option.format}`);
  }
};

// Helper function to download files
const downloadFile = (content: string, filename: string, mimeType: string): void => {
  const blob = new Blob([content], { type: `${mimeType};charset=utf-8;` });
  const link = document.createElement('a');
  const url = URL.createObjectURL(blob);
  
  link.setAttribute('href', url);
  link.setAttribute('download', filename);
  link.style.visibility = 'hidden';
  
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  
  URL.revokeObjectURL(url);
};

// Data preparation functions
export const prepareRepositoryExportData = (
  repositories: RepositorySummary[],
  scope: ExportScope,
  selectedIds?: Set<number>
): ExportData => {
  let exportRepositories = repositories;
  
  if (scope === 'selected' && selectedIds) {
    exportRepositories = repositories.filter(repo => selectedIds.has(repo.id));
  }
  
  return {
    repositories: exportRepositories,
    metadata: {
      exportDate: new Date().toISOString(),
      totalItems: exportRepositories.length,
      dataType: 'repositories',
      scope
    }
  };
};

export const prepareTeamExportData = (
  teams: TeamMetrics[],
  scope: ExportScope,
  selectedIds?: Set<number>
): ExportData => {
  let exportTeams = teams;
  
  if (scope === 'selected' && selectedIds) {
    exportTeams = teams.filter(team => selectedIds.has(team.id));
  }
  
  return {
    teams: exportTeams,
    metadata: {
      exportDate: new Date().toISOString(),
      totalItems: exportTeams.length,
      dataType: 'teams',
      scope
    }
  };
};

export const prepareAnalyticsExportData = (
  analytics: DailyMetric[],
  scope: ExportScope
): ExportData => {
  return {
    analytics,
    metadata: {
      exportDate: new Date().toISOString(),
      totalItems: analytics.length,
      dataType: 'analytics',
      scope
    }
  };
};

export const prepareTestMethodExportData = (
  methods: TestMethodDetail[],
  scope: ExportScope,
  selectedIds?: Set<number>
): ExportData => {
  let exportMethods = methods;
  
  if (scope === 'selected' && selectedIds) {
    exportMethods = methods.filter(method => selectedIds.has(method.id));
  }
  
  return {
    methods: exportMethods,
    metadata: {
      exportDate: new Date().toISOString(),
      totalItems: exportMethods.length,
      dataType: 'test-methods',
      scope
    }
  };
};

export const prepareGroupedTestMethodExportData = (
  groupedData: GroupedTestMethodResponse,
  scope: ExportScope
): ExportData => {
  return {
    groupedMethods: groupedData,
    metadata: {
      exportDate: new Date().toISOString(),
      totalItems: groupedData.summary.totalMethods,
      dataType: 'grouped-test-methods',
      scope
    }
  };
};

// Legacy functions for backward compatibility
export const exportRepositoryToCSV = (repository: RepositorySummary, filename?: string): void => {
  const defaultFilename = filename || `repository-${repository.repositoryName.replace(/[^a-zA-Z0-9]/g, '-')}`;
  const data = prepareRepositoryExportData([repository], 'all');
  exportToCSV(data, `${defaultFilename}-${new Date().toISOString().split('T')[0]}.csv`);
};

export const exportDashboardToCSV = (data: {
  repositories: RepositorySummary[];
  scanHistory: any[];
  dashboardOverview: any;
  exportDate: Date;
}, filename: string = 'dashboard-export'): void => {
  // Legacy function - keeping for backward compatibility
  const csvContent = createLegacyCSVContent(data);
  downloadFile(csvContent, `${filename}-${new Date().toISOString().split('T')[0]}.csv`, 'text/csv');
};

const createLegacyCSVContent = (data: {
  repositories: RepositorySummary[];
  scanHistory: any[];
  dashboardOverview: any;
  exportDate: Date;
}): string => {
  const { repositories, scanHistory, dashboardOverview, exportDate } = data;
  
  let csvContent = '';
  
  // Dashboard Overview Section
  csvContent += 'Dashboard Overview\n';
  csvContent += 'Export Date,' + exportDate.toISOString() + '\n';
  csvContent += 'Total Repositories,' + (dashboardOverview?.totalRepositories || 0) + '\n';
  csvContent += 'Total Teams,' + (dashboardOverview?.totalTeams || 0) + '\n';
  csvContent += 'Total Test Methods,' + (dashboardOverview?.totalTestMethods || 0) + '\n';
  csvContent += 'Total Annotated Methods,' + (dashboardOverview?.totalAnnotatedMethods || 0) + '\n';
  csvContent += 'Overall Coverage Rate,' + (dashboardOverview?.overallCoverageRate?.toFixed(2) || '0') + '%\n';
  csvContent += '\n';
  
  // Repositories Section
  csvContent += 'Repositories\n';
  csvContent += 'Repository Name,Team,Test Methods,Annotated Methods,Coverage Rate,Last Scan Date,Git URL\n';
  repositories.forEach((repo: RepositorySummary) => {
    csvContent += `"${repo.repositoryName}","${repo.teamName}",${repo.testMethodCount},${repo.annotatedMethodCount},${repo.coverageRate.toFixed(2)}%,"${new Date(repo.lastScanDate).toLocaleDateString()}","${repo.gitUrl}"\n`;
  });
  csvContent += '\n';
  
  // Scan History Section
  csvContent += 'Scan History\n';
  csvContent += 'Scan ID,Scan Date,Scan Directory,Total Repositories,Total Test Classes,Total Test Methods,Total Annotated Methods,Coverage Rate,Scan Duration (ms),Status\n';
  scanHistory.forEach((scan: any) => {
    const coverageRate = scan.totalTestMethods > 0 ? ((scan.totalAnnotatedMethods / scan.totalTestMethods) * 100).toFixed(2) : '0';
    csvContent += `${scan.id},"${new Date(scan.scanDate).toLocaleString()}","${scan.scanDirectory}",${scan.totalRepositories},${scan.totalTestClasses},${scan.totalTestMethods},${scan.totalAnnotatedMethods},${coverageRate}%,${scan.scanDurationMs},"${scan.scanStatus}"\n`;
  });
  
  return csvContent;
};