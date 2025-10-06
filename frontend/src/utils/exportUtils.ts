import { type RepositorySummary, type TeamMetrics, type DailyMetric } from '../lib/api';

export type ExportFormat = 'csv' | 'json' | 'excel';
export type ExportScope = 'all' | 'selected' | 'filtered';

export interface ExportData {
  repositories?: RepositorySummary[];
  teams?: TeamMetrics[];
  analytics?: DailyMetric[];
  methods?: any[];
  classes?: any[];
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
        repo.repositoryId,
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

  downloadFile(csvContent, filename, 'text/csv');
};

// JSON Export Functions
export const exportToJSON = (data: ExportData, filename: string): void => {
  const jsonContent = JSON.stringify(data, null, 2);
  downloadFile(jsonContent, filename, 'application/json');
};

// Excel Export Functions (using CSV format for now, can be enhanced with actual Excel library)
export const exportToExcel = (data: ExportData, filename: string): void => {
  // For now, we'll use CSV format but with .xlsx extension
  // In a real implementation, you'd use a library like xlsx or exceljs
  exportToCSV(data, filename.replace('.xlsx', '.csv'));
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
    exportRepositories = repositories.filter(repo => selectedIds.has(repo.repositoryId));
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

// Legacy functions for backward compatibility
export const exportRepositoryToCSV = (repository: RepositorySummary, filename?: string): void => {
  const defaultFilename = filename || `repository-${repository.repositoryName.replace(/[^a-zA-Z0-9]/g, '-')}`;
  const data = prepareRepositoryExportData([repository], 'all');
  exportToCSV(data, `${defaultFilename}-${new Date().toISOString().split('T')[0]}.csv`);
};

export const exportDashboardToCSV = (data: any, filename: string = 'dashboard-export'): void => {
  // Legacy function - keeping for backward compatibility
  const csvContent = createLegacyCSVContent(data);
  downloadFile(csvContent, `${filename}-${new Date().toISOString().split('T')[0]}.csv`, 'text/csv');
};

const createLegacyCSVContent = (data: any): string => {
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
  repositories.forEach((repo: any) => {
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