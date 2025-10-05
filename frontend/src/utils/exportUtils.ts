import { type ScanSession } from '../lib/api';

export interface ExportData {
  repositories: any[];
  scanHistory: ScanSession[];
  dashboardOverview: any;
  exportDate: Date;
}

export const exportToCSV = (data: ExportData, filename: string = 'dashboard-export') => {
  const { repositories, scanHistory, dashboardOverview, exportDate } = data;
  
  // Create CSV content
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
  repositories.forEach(repo => {
    csvContent += `"${repo.repositoryName}","${repo.teamName}",${repo.testMethodCount},${repo.annotatedMethodCount},${repo.coverageRate.toFixed(2)}%,"${new Date(repo.lastScanDate).toLocaleDateString()}","${repo.gitUrl}"\n`;
  });
  csvContent += '\n';
  
  // Scan History Section
  csvContent += 'Scan History\n';
  csvContent += 'Scan ID,Scan Date,Scan Directory,Total Repositories,Total Test Classes,Total Test Methods,Total Annotated Methods,Coverage Rate,Scan Duration (ms),Status\n';
  scanHistory.forEach(scan => {
    const coverageRate = scan.totalTestMethods > 0 ? ((scan.totalAnnotatedMethods / scan.totalTestMethods) * 100).toFixed(2) : '0';
    csvContent += `${scan.id},"${new Date(scan.scanDate).toLocaleString()}","${scan.scanDirectory}",${scan.totalRepositories},${scan.totalTestClasses},${scan.totalTestMethods},${scan.totalAnnotatedMethods},${coverageRate}%,${scan.scanDurationMs},"${scan.scanStatus}"\n`;
  });
  
  // Create and download file
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
  const link = document.createElement('a');
  const url = URL.createObjectURL(blob);
  link.setAttribute('href', url);
  link.setAttribute('download', `${filename}-${exportDate.toISOString().split('T')[0]}.csv`);
  link.style.visibility = 'hidden';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};

export const exportRepositoryToCSV = (repository: any, filename?: string) => {
  const exportDate = new Date();
  const defaultFilename = filename || `repository-${repository.repositoryName.replace(/[^a-zA-Z0-9]/g, '-')}`;
  
  let csvContent = '';
  csvContent += 'Repository Details\n';
  csvContent += 'Export Date,' + exportDate.toISOString() + '\n';
  csvContent += '\n';
  csvContent += 'Repository Information\n';
  csvContent += 'Repository Name,' + repository.repositoryName + '\n';
  csvContent += 'Team,' + repository.teamName + '\n';
  csvContent += 'Git URL,' + repository.gitUrl + '\n';
  csvContent += 'Last Scan Date,' + new Date(repository.lastScanDate).toLocaleString() + '\n';
  csvContent += '\n';
  csvContent += 'Test Metrics\n';
  csvContent += 'Test Classes,' + repository.testClassCount + '\n';
  csvContent += 'Test Methods,' + repository.testMethodCount + '\n';
  csvContent += 'Annotated Methods,' + repository.annotatedMethodCount + '\n';
  csvContent += 'Coverage Rate,' + repository.coverageRate.toFixed(2) + '%\n';
  csvContent += 'Unannotated Methods,' + (repository.testMethodCount - repository.annotatedMethodCount) + '\n';
  
  // Create and download file
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
  const link = document.createElement('a');
  const url = URL.createObjectURL(blob);
  link.setAttribute('href', url);
  link.setAttribute('download', `${defaultFilename}-${exportDate.toISOString().split('T')[0]}.csv`);
  link.style.visibility = 'hidden';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};
