import React, { useState } from 'react';
import { Download, FileText, Table, BarChart3, Users, GitBranch, Calendar } from 'lucide-react';

export type ExportFormat = 'csv' | 'json' | 'excel';
export type ExportScope = 'all' | 'selected' | 'filtered';

export interface ExportOption {
  id: string;
  label: string;
  description: string;
  icon: React.ReactNode;
  format: ExportFormat;
  scope: ExportScope;
  filename: string;
}

export interface ExportManagerProps {
  data: any[];
  dataType: 'repositories' | 'teams' | 'methods' | 'classes' | 'analytics';
  selectedItems?: Set<number>;
  filteredData?: any[];
  onExport: (option: ExportOption) => Promise<void>;
  className?: string;
}

const ExportManager: React.FC<ExportManagerProps> = ({
  data,
  dataType,
  selectedItems,
  filteredData,
  onExport,
  className = ""
}) => {
  const [isOpen, setIsOpen] = useState(false);
  const [exporting, setExporting] = useState<string | null>(null);

  const getExportOptions = (): ExportOption[] => {
    const baseOptions: ExportOption[] = [
      {
        id: 'excel-all',
        label: 'Export All (Excel)',
        description: `Export all ${dataType} to Excel with multiple sheets`,
        icon: <Table className="h-4 w-4" />,
        format: 'excel',
        scope: 'all',
        filename: `${dataType}-comprehensive-${new Date().toISOString().split('T')[0]}.xlsx`
      },
      {
        id: 'csv-all',
        label: 'Export All (CSV)',
        description: `Export all ${dataType} to CSV format`,
        icon: <Table className="h-4 w-4" />,
        format: 'csv',
        scope: 'all',
        filename: `${dataType}-all-${new Date().toISOString().split('T')[0]}.csv`
      },
      {
        id: 'json-all',
        label: 'Export All (JSON)',
        description: `Export all ${dataType} to JSON format`,
        icon: <FileText className="h-4 w-4" />,
        format: 'json',
        scope: 'all',
        filename: `${dataType}-all-${new Date().toISOString().split('T')[0]}.json`
      }
    ];

    if (selectedItems && selectedItems.size > 0) {
      baseOptions.push({
        id: 'csv-selected',
        label: `Export Selected (CSV)`,
        description: `Export ${selectedItems.size} selected ${dataType} to CSV`,
        icon: <Table className="h-4 w-4" />,
        format: 'csv',
        scope: 'selected',
        filename: `${dataType}-selected-${new Date().toISOString().split('T')[0]}.csv`
      });
    }

    if (filteredData && filteredData.length !== data.length) {
      baseOptions.push({
        id: 'csv-filtered',
        label: `Export Filtered (CSV)`,
        description: `Export ${filteredData.length} filtered ${dataType} to CSV`,
        icon: <Table className="h-4 w-4" />,
        format: 'csv',
        scope: 'filtered',
        filename: `${dataType}-filtered-${new Date().toISOString().split('T')[0]}.csv`
      });
    }

    return baseOptions;
  };

  const handleExport = async (option: ExportOption) => {
    try {
      setExporting(option.id);
      await onExport(option);
    } catch (error) {
      console.error('Export failed:', error);
    } finally {
      setExporting(null);
      setIsOpen(false);
    }
  };

  const getDataIcon = () => {
    switch (dataType) {
      case 'repositories':
        return <GitBranch className="h-4 w-4" />;
      case 'teams':
        return <Users className="h-4 w-4" />;
      case 'methods':
        return <BarChart3 className="h-4 w-4" />;
      case 'classes':
        return <FileText className="h-4 w-4" />;
      case 'analytics':
        return <Calendar className="h-4 w-4" />;
      default:
        return <Download className="h-4 w-4" />;
    }
  };

  const exportOptions = getExportOptions();

  return (
    <div className={`relative ${className}`}>
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="btn btn-secondary flex items-center"
        disabled={exporting !== null}
      >
        {exporting ? (
          <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
        ) : (
          <Download className="h-4 w-4 mr-2" />
        )}
        {exporting ? 'Exporting...' : 'Export'}
      </button>

      {isOpen && (
        <>
          {/* Backdrop */}
          <div
            className="fixed inset-0 z-10"
            onClick={() => setIsOpen(false)}
          />
          
          {/* Dropdown */}
          <div className="absolute right-0 top-full mt-2 w-80 bg-white rounded-lg shadow-lg border border-gray-200 z-20">
            <div className="p-4">
              <div className="flex items-center mb-4">
                {getDataIcon()}
                <h3 className="text-lg font-semibold text-gray-900 ml-2 capitalize">
                  Export {dataType}
                </h3>
              </div>
              
              <div className="space-y-2">
                {exportOptions.map((option) => (
                  <button
                    key={option.id}
                    onClick={() => handleExport(option)}
                    disabled={exporting !== null}
                    className="w-full text-left p-3 rounded-lg border border-gray-200 hover:bg-gray-50 hover:border-gray-300 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    <div className="flex items-center">
                      <div className="flex-shrink-0 mr-3">
                        {option.icon}
                      </div>
                      <div className="flex-1">
                        <div className="text-sm font-medium text-gray-900">
                          {option.label}
                        </div>
                        <div className="text-xs text-gray-500 mt-1">
                          {option.description}
                        </div>
                      </div>
                      {exporting === option.id && (
                        <div className="flex-shrink-0 ml-2">
                          <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600"></div>
                        </div>
                      )}
                    </div>
                  </button>
                ))}
              </div>
              
              <div className="mt-4 pt-4 border-t border-gray-200">
                <div className="text-xs text-gray-500">
                  <div className="flex justify-between">
                    <span>Total {dataType}:</span>
                    <span className="font-medium">{data.length}</span>
                  </div>
                  {selectedItems && selectedItems.size > 0 && (
                    <div className="flex justify-between mt-1">
                      <span>Selected:</span>
                      <span className="font-medium">{selectedItems.size}</span>
                    </div>
                  )}
                  {filteredData && filteredData.length !== data.length && (
                    <div className="flex justify-between mt-1">
                      <span>Filtered:</span>
                      <span className="font-medium">{filteredData.length}</span>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
};

export default ExportManager;
