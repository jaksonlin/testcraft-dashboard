import { type ScanSession } from '../lib/api';
import * as XLSX from 'xlsx';

export const exportScanDataToCSV = (scan: ScanSession): void => {
  const coverageRate = scan.totalTestMethods > 0 
    ? ((scan.totalAnnotatedMethods / scan.totalTestMethods) * 100).toFixed(1) 
    : '0.0';
  
  let csvContent = '';
  
  // Scan Information
  csvContent += 'Scan Session Export\n';
  csvContent += 'Scan Information\n';
  csvContent += `Scan ID,${scan.id}\n`;
  csvContent += `Scan Date,"${new Date(scan.scanDate).toLocaleString()}"\n`;
  csvContent += `Scan Directory,"${scan.scanDirectory}"\n`;
  csvContent += `Scan Status,"${scan.scanStatus}"\n`;
  csvContent += '\n';
  
  // Scan Results
  csvContent += 'Scan Results\n';
  csvContent += 'Total Repositories,Total Test Classes,Total Test Methods,Total Annotated Methods,Coverage Rate,Scan Duration (ms)\n';
  csvContent += `${scan.totalRepositories},${scan.totalTestClasses},${scan.totalTestMethods},${scan.totalAnnotatedMethods},${coverageRate}%,${scan.scanDurationMs}\n`;
  
  // Coverage Analysis
  csvContent += '\nCoverage Analysis\n';
  csvContent += 'Annotated Methods,Remaining Methods,Coverage Rate\n';
  csvContent += `${scan.totalAnnotatedMethods},${scan.totalTestMethods - scan.totalAnnotatedMethods},${coverageRate}%\n`;
  
  // Error Log if present
  if (scan.errorLog) {
    csvContent += '\nError Log\n';
    csvContent += 'Error Details\n';
    csvContent += `"${scan.errorLog.replace(/"/g, '""')}"\n`;
  }
  
  downloadFile(csvContent, `scan-${scan.id}-${new Date(scan.scanDate).toISOString().split('T')[0]}.csv`, 'text/csv');
};

export const exportScanDataToExcel = (scan: ScanSession): void => {
  const workbook = XLSX.utils.book_new();
  const coverageRate = scan.totalTestMethods > 0 
    ? ((scan.totalAnnotatedMethods / scan.totalTestMethods) * 100).toFixed(1) 
    : '0.0';
  
  // Scan Information Sheet
  const scanInfoData = [
    ['Scan Information', ''],
    ['Scan ID', scan.id],
    ['Scan Date', new Date(scan.scanDate).toLocaleString()],
    ['Scan Directory', scan.scanDirectory],
    ['Scan Status', scan.scanStatus],
    ['Duration', formatDuration(scan.scanDurationMs)],
    ['', ''],
    ['Scan Results', ''],
    ['Total Repositories', scan.totalRepositories],
    ['Total Test Classes', scan.totalTestClasses],
    ['Total Test Methods', scan.totalTestMethods],
    ['Total Annotated Methods', scan.totalAnnotatedMethods],
    ['Coverage Rate', `${coverageRate}%`],
    ['', ''],
    ['Coverage Analysis', ''],
    ['Annotated Methods', scan.totalAnnotatedMethods],
    ['Remaining Methods', scan.totalTestMethods - scan.totalAnnotatedMethods],
    ['Coverage Percentage', `${coverageRate}%`]
  ];
  
  if (scan.errorLog) {
    scanInfoData.push(['', '']);
    scanInfoData.push(['Error Log', '']);
    scanInfoData.push(['Error Details', scan.errorLog]);
  }
  
  const scanInfoSheet = XLSX.utils.aoa_to_sheet(scanInfoData);
  XLSX.utils.book_append_sheet(workbook, scanInfoSheet, 'Scan Details');
  
  // Download the file
  const filename = `scan-${scan.id}-${new Date(scan.scanDate).toISOString().split('T')[0]}.xlsx`;
  XLSX.writeFile(workbook, filename);
};

const formatDuration = (ms: number): string => {
  const seconds = Math.round(ms / 1000);
  if (seconds < 60) return `${seconds}s`;
  const minutes = Math.floor(seconds / 60);
  const remainingSeconds = seconds % 60;
  return remainingSeconds > 0 ? `${minutes}m ${remainingSeconds}s` : `${minutes}m`;
};

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
