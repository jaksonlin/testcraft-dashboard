import { type RepositorySummary, type TeamMetrics, type DailyMetric, type TestMethodDetail, type GroupedTestMethodResponse, type TestClassSummary, type ScanSession, type DashboardOverview } from '../lib/api';
import ExcelJS from 'exceljs';
import { saveAs } from 'file-saver';

export type ExportFormat = 'csv' | 'json' | 'excel';
export type ExportScope = 'all' | 'selected' | 'filtered';

export interface ExportData {
  repositories?: RepositorySummary[];
  teams?: TeamMetrics[];
  analytics?: DailyMetric[];
  methods?: TestMethodDetail[];
  groupedMethods?: GroupedTestMethodResponse;
  classes?: TestClassSummary[];
  scanHistory?: ScanSession[];
  dashboardOverview?: DashboardOverview;
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
const addMetadataSheet = (workbook: ExcelJS.Workbook, data: ExportData): void => {
  const sheet = workbook.addWorksheet('Export Info');

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

  metadata.forEach(row => sheet.addRow(row));

  // Style the header
  sheet.getRow(1).font = { bold: true, size: 14 };
  sheet.getColumn(1).width = 25;
  sheet.getColumn(2).width = 40;
};

const addRepositoriesSheet = (workbook: ExcelJS.Workbook, repositories: RepositorySummary[]): void => {
  const sheet = workbook.addWorksheet('Repositories');

  sheet.columns = [
    { header: 'Repository ID', key: 'id', width: 15 },
    { header: 'Repository Name', key: 'repositoryName', width: 30 },
    { header: 'Team Name', key: 'teamName', width: 20 },
    { header: 'Team Code', key: 'teamCode', width: 15 },
    { header: 'Department', key: 'department', width: 20 },
    { header: 'Git URL', key: 'gitUrl', width: 40 },
    { header: 'Test Classes', key: 'testClassCount', width: 15 },
    { header: 'Test Methods', key: 'testMethodCount', width: 15 },
    { header: 'Annotated Methods', key: 'annotatedMethodCount', width: 20 },
    { header: 'Coverage Rate (%)', key: 'coverageRate', width: 20 },
    { header: 'Last Scan Date', key: 'lastScanDate', width: 25 }
  ];

  repositories.forEach(repo => {
    sheet.addRow({
      id: repo.id,
      repositoryName: repo.repositoryName,
      teamName: repo.teamName,
      teamCode: '', // teamCode - not available in RepositorySummary
      department: '', // department - not available in RepositorySummary
      gitUrl: repo.gitUrl,
      testClassCount: repo.testClassCount,
      testMethodCount: repo.testMethodCount,
      annotatedMethodCount: repo.annotatedMethodCount || 0,
      coverageRate: (repo.coverageRate ?? 0).toFixed(2),
      lastScanDate: new Date(repo.lastScanDate).toLocaleString()
    });
  });

  // Style header
  sheet.getRow(1).font = { bold: true };
};

const addTeamsSheet = (workbook: ExcelJS.Workbook, teams: TeamMetrics[]): void => {
  const sheet = workbook.addWorksheet('Teams');

  sheet.columns = [
    { header: 'Team ID', key: 'id', width: 10 },
    { header: 'Team Name', key: 'teamName', width: 25 },
    { header: 'Team Code', key: 'teamCode', width: 15 },
    { header: 'Department', key: 'department', width: 20 },
    { header: 'Repository Count', key: 'repositoryCount', width: 20 },
    { header: 'Total Test Classes', key: 'totalTestClasses', width: 20 },
    { header: 'Total Test Methods', key: 'totalTestMethods', width: 20 },
    { header: 'Total Annotated Methods', key: 'totalAnnotatedMethods', width: 25 },
    { header: 'Average Coverage Rate (%)', key: 'averageCoverageRate', width: 25 },
    { header: 'Last Scan Date', key: 'lastScanDate', width: 25 }
  ];

  teams.forEach(team => {
    sheet.addRow({
      id: team.id,
      teamName: team.teamName,
      teamCode: team.teamCode,
      department: team.department || '',
      repositoryCount: team.repositoryCount,
      totalTestClasses: team.totalTestClasses,
      totalTestMethods: team.totalTestMethods,
      totalAnnotatedMethods: team.totalAnnotatedMethods || 0,
      averageCoverageRate: (team.averageCoverageRate ?? 0).toFixed(2),
      lastScanDate: team.lastScanDate ? new Date(team.lastScanDate).toLocaleString() : 'N/A'
    });
  });

  sheet.getRow(1).font = { bold: true };
};

const addAnalyticsSheet = (workbook: ExcelJS.Workbook, analytics: DailyMetric[]): void => {
  const sheet = workbook.addWorksheet('Analytics');

  sheet.columns = [
    { header: 'Date', key: 'date', width: 15 },
    { header: 'Total Repositories', key: 'totalRepositories', width: 20 },
    { header: 'Total Test Classes', key: 'totalTestClasses', width: 20 },
    { header: 'Total Test Methods', key: 'totalTestMethods', width: 20 },
    { header: 'Total Annotated Methods', key: 'totalAnnotatedMethods', width: 25 },
    { header: 'Overall Coverage Rate (%)', key: 'overallCoverageRate', width: 25 },
    { header: 'New Test Methods', key: 'newTestMethods', width: 20 },
    { header: 'New Annotated Methods', key: 'newAnnotatedMethods', width: 25 }
  ];

  analytics.forEach(metric => {
    sheet.addRow({
      date: metric.date,
      totalRepositories: metric.totalRepositories,
      totalTestClasses: metric.totalTestClasses,
      totalTestMethods: metric.totalTestMethods,
      totalAnnotatedMethods: metric.totalAnnotatedMethods,
      overallCoverageRate: (metric.overallCoverageRate ?? 0).toFixed(2),
      newTestMethods: metric.newTestMethods,
      newAnnotatedMethods: metric.newAnnotatedMethods
    });
  });

  sheet.getRow(1).font = { bold: true };
};

const addTestMethodDetailsSheet = (workbook: ExcelJS.Workbook, methods: TestMethodDetail[]): void => {
  const sheet = workbook.addWorksheet('Test Method Details');

  sheet.columns = [
    { header: 'Method ID', key: 'id', width: 15 },
    { header: 'Repository', key: 'repository', width: 25 },
    { header: 'Test Class', key: 'testClass', width: 30 },
    { header: 'Test Method', key: 'testMethod', width: 30 },
    { header: 'Line Number', key: 'line', width: 15 },
    { header: 'Title', key: 'title', width: 30 },
    { header: 'Author', key: 'author', width: 20 },
    { header: 'Test Status', key: 'status', width: 15 },
    { header: 'Target Class', key: 'targetClass', width: 30 },
    { header: 'Target Method', key: 'targetMethod', width: 30 },
    { header: 'Description', key: 'description', width: 40 },
    { header: 'Test Points', key: 'testPoints', width: 20 },
    { header: 'Tags', key: 'tags', width: 20 },
    { header: 'Requirements', key: 'requirements', width: 20 },
    { header: 'Test Case IDs', key: 'testCaseIds', width: 20 },
    { header: 'Defects', key: 'defects', width: 20 },
    { header: 'Last Modified', key: 'lastModified', width: 25 },
    { header: 'Last Update Author', key: 'lastUpdateAuthor', width: 20 },
    { header: 'Team Name', key: 'teamName', width: 20 },
    { header: 'Team Code', key: 'teamCode', width: 15 },
    { header: 'Git URL', key: 'gitUrl', width: 40 }
  ];

  methods.forEach(method => {
    sheet.addRow({
      id: method.id,
      repository: method.repository,
      testClass: method.testClass,
      testMethod: method.testMethod,
      line: method.line,
      title: method.title || '',
      author: method.author || '',
      status: method.status || '',
      targetClass: method.targetClass || '',
      targetMethod: method.targetMethod || '',
      description: method.description || '',
      testPoints: method.testPoints || '',
      tags: method.tags.join('; ') || '',
      requirements: method.requirements.join('; ') || '',
      testCaseIds: method.testCaseIds.join('; ') || '',
      defects: method.defects.join('; ') || '',
      lastModified: method.lastModified ? new Date(method.lastModified).toLocaleString() : '',
      lastUpdateAuthor: method.lastUpdateAuthor || '',
      teamName: method.teamName || '',
      teamCode: method.teamCode || '',
      gitUrl: method.gitUrl || ''
    });
  });

  sheet.getRow(1).font = { bold: true };
};

const addGroupedMethodsSheet = (workbook: ExcelJS.Workbook, groupedData: GroupedTestMethodResponse): void => {
  const sheet = workbook.addWorksheet('Grouped Test Methods');

  sheet.columns = [
    { header: 'Team Name', key: 'teamName', width: 20 },
    { header: 'Team Code', key: 'teamCode', width: 15 },
    { header: 'Class Name', key: 'className', width: 30 },
    { header: 'Package Name', key: 'packageName', width: 30 },
    { header: 'Repository', key: 'repository', width: 25 },
    { header: 'Method ID', key: 'id', width: 15 },
    { header: 'Test Method', key: 'testMethod', width: 30 },
    { header: 'Line Number', key: 'line', width: 15 },
    { header: 'Title', key: 'title', width: 30 },
    { header: 'Author', key: 'author', width: 20 },
    { header: 'Test Status', key: 'status', width: 15 },
    { header: 'Target Class', key: 'targetClass', width: 30 },
    { header: 'Target Method', key: 'targetMethod', width: 30 },
    { header: 'Description', key: 'description', width: 40 },
    { header: 'Test Points', key: 'testPoints', width: 20 },
    { header: 'Tags', key: 'tags', width: 20 },
    { header: 'Requirements', key: 'requirements', width: 20 },
    { header: 'Test Case IDs', key: 'testCaseIds', width: 20 },
    { header: 'Defects', key: 'defects', width: 20 },
    { header: 'Last Modified', key: 'lastModified', width: 25 },
    { header: 'Last Update Author', key: 'lastUpdateAuthor', width: 20 },
    { header: 'Git URL', key: 'gitUrl', width: 40 }
  ];

  groupedData.teams.forEach(team => {
    team.classes.forEach(classGroup => {
      classGroup.methods.forEach(method => {
        sheet.addRow({
          teamName: team.teamName,
          teamCode: team.teamCode,
          className: classGroup.className,
          packageName: classGroup.packageName,
          repository: classGroup.repository,
          id: method.id,
          testMethod: method.testMethod,
          line: method.line,
          title: method.title || '',
          author: method.author || '',
          status: method.status || '',
          targetClass: method.targetClass || '',
          targetMethod: method.targetMethod || '',
          description: method.description || '',
          testPoints: method.testPoints || '',
          tags: method.tags.join('; ') || '',
          requirements: method.requirements.join('; ') || '',
          testCaseIds: method.testCaseIds.join('; ') || '',
          defects: method.defects.join('; ') || '',
          lastModified: method.lastModified ? new Date(method.lastModified).toLocaleString() : '',
          lastUpdateAuthor: method.lastUpdateAuthor || '',
          gitUrl: method.gitUrl || ''
        });
      });
    });
  });

  sheet.getRow(1).font = { bold: true };
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
        (repo.coverageRate ?? 0).toFixed(2),
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
        (team.averageCoverageRate ?? 0).toFixed(2),
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
        (metric.overallCoverageRate ?? 0).toFixed(2),
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

// Excel Export Functions using ExcelJS library
export const exportToExcel = async (data: ExportData, filename: string): Promise<void> => {
  const workbook = new ExcelJS.Workbook();
  workbook.creator = 'TestCraft Dashboard';
  workbook.created = new Date();

  // Add metadata sheet
  addMetadataSheet(workbook, data);

  // Add repositories sheet if data exists
  if (data.repositories && data.repositories.length > 0) {
    addRepositoriesSheet(workbook, data.repositories);
  }

  // Add teams sheet if data exists
  if (data.teams && data.teams.length > 0) {
    addTeamsSheet(workbook, data.teams);
  }

  // Add analytics sheet if data exists
  if (data.analytics && data.analytics.length > 0) {
    addAnalyticsSheet(workbook, data.analytics);
  }

  // Add test method details sheet if data exists
  if (data.methods && data.methods.length > 0) {
    addTestMethodDetailsSheet(workbook, data.methods);
  }

  // Add grouped test methods sheet if data exists
  if (data.groupedMethods) {
    addGroupedMethodsSheet(workbook, data.groupedMethods);
  }

  // Generate and download the Excel file
  const buffer = await workbook.xlsx.writeBuffer();
  const blob = new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
  saveAs(blob, filename);
};

// Generic export function
export const exportData = async (data: ExportData, option: ExportOption): Promise<void> => {
  switch (option.format) {
    case 'csv':
      exportToCSV(data, option.filename);
      break;
    case 'json':
      exportToJSON(data, option.filename);
      break;
    case 'excel':
      await exportToExcel(data, option.filename);
      break;
    default:
      throw new Error(`Unsupported export format: ${option.format}`);
  }
};

// Helper function to download files
const downloadFile = (content: string, filename: string, mimeType: string): void => {
  const blob = new Blob([content], { type: `${mimeType};charset=utf-8;` });
  saveAs(blob, filename);
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
  scanHistory: ScanSession[];
  dashboardOverview: DashboardOverview;
  exportDate: Date;
}, filename: string = 'dashboard-export'): void => {
  // Legacy function - keeping for backward compatibility
  const csvContent = createLegacyCSVContent(data);
  downloadFile(csvContent, `${filename}-${new Date().toISOString().split('T')[0]}.csv`, 'text/csv');
};

const createLegacyCSVContent = (data: {
  repositories: RepositorySummary[];
  scanHistory: ScanSession[];
  dashboardOverview: DashboardOverview;
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
    csvContent += `"${repo.repositoryName}","${repo.teamName}",${repo.testMethodCount},${repo.annotatedMethodCount},${(repo.coverageRate ?? 0).toFixed(2)}%,"${new Date(repo.lastScanDate).toLocaleDateString()}","${repo.gitUrl}"\n`;
  });
  csvContent += '\n';

  // Scan History Section
  csvContent += 'Scan History\n';
  csvContent += 'Scan ID,Scan Date,Scan Directory,Total Repositories,Total Test Classes,Total Test Methods,Total Annotated Methods,Coverage Rate,Scan Duration (ms),Status\n';
  scanHistory.forEach((scan) => {
    const coverageRate = scan.totalTestMethods > 0 ? ((scan.totalAnnotatedMethods / scan.totalTestMethods) * 100).toFixed(2) : '0';
    csvContent += `${scan.id},"${new Date(scan.scanDate).toLocaleString()}","${scan.scanDirectory}",${scan.totalRepositories},${scan.totalTestClasses},${scan.totalTestMethods},${scan.totalAnnotatedMethods},${coverageRate}%,${scan.scanDurationMs},"${scan.scanStatus}"\n`;
  });

  return csvContent;
};