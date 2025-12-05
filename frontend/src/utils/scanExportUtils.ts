import { type ScanSession } from '../lib/api';
import ExcelJS from 'exceljs';
import { saveAs } from 'file-saver';

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

export const exportScanDataToExcel = async (scan: ScanSession): Promise<void> => {
  const workbook = new ExcelJS.Workbook();
  workbook.creator = 'TestCraft Dashboard';
  workbook.created = new Date();

  const coverageRate = scan.totalTestMethods > 0
    ? ((scan.totalAnnotatedMethods / scan.totalTestMethods) * 100).toFixed(1)
    : '0.0';

  // Scan Information Sheet
  const sheet = workbook.addWorksheet('Scan Details');

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

  scanInfoData.forEach(row => sheet.addRow(row));

  // Style header
  sheet.getColumn(1).width = 25;
  sheet.getColumn(2).width = 40;
  sheet.getRow(1).font = { bold: true, size: 14 };

  // Download the file
  const filename = `scan-${scan.id}-${new Date(scan.scanDate).toISOString().split('T')[0]}.xlsx`;
  const buffer = await workbook.xlsx.writeBuffer();
  const blob = new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
  saveAs(blob, filename);
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
  saveAs(blob, filename);
};

