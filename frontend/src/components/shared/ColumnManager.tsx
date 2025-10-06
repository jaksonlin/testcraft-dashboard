import React, { useState } from 'react';
import { Columns, Eye, EyeOff, GripVertical, X, Check } from 'lucide-react';
import { useTablePreferences, type ColumnPreference } from '../../contexts/PreferencesContext';

interface Column {
  id: string;
  label: string;
  required?: boolean;
}

interface ColumnManagerProps {
  tableId: string;
  columns: Column[];
  isOpen: boolean;
  onClose: () => void;
}

const ColumnManager: React.FC<ColumnManagerProps> = ({ tableId, columns, isOpen, onClose }) => {
  const { preferences, updateColumns, toggleColumn, reorderColumns } = useTablePreferences(tableId);
  const [draggedIndex, setDraggedIndex] = useState<number | null>(null);

  if (!isOpen) return null;

  // Initialize columns if not set
  const currentColumns = preferences.columns.length > 0 
    ? preferences.columns 
    : columns.map((col, index) => ({
        id: col.id,
        visible: true,
        order: index,
      }));

  const handleDragStart = (e: React.DragEvent, index: number) => {
    setDraggedIndex(index);
    e.dataTransfer.effectAllowed = 'move';
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    e.dataTransfer.dropEffect = 'move';
  };

  const handleDrop = (e: React.DragEvent, dropIndex: number) => {
    e.preventDefault();
    if (draggedIndex !== null && draggedIndex !== dropIndex) {
      reorderColumns(draggedIndex, dropIndex);
    }
    setDraggedIndex(null);
  };

  const handleDragEnd = () => {
    setDraggedIndex(null);
  };

  const visibleColumns = currentColumns.filter(col => col.visible).length;
  const totalColumns = columns.length;

  return (
    <>
      {/* Backdrop */}
      <div
        className="fixed inset-0 bg-black bg-opacity-30 z-40"
        onClick={onClose}
        style={{ backgroundColor: 'rgba(0, 0, 0, 0.3)' }}
      />
      
      {/* Panel */}
      <div className="fixed right-0 top-0 h-full w-80 shadow-xl z-50 flex flex-col bg-background border-l border-border">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-border">
          <div className="flex items-center">
            <Columns className="h-6 w-6 text-primary mr-3" />
            <h2 className="text-xl font-semibold text-foreground">Manage Columns</h2>
          </div>
          <button
            onClick={onClose}
            className="p-2 rounded-lg transition-colors bg-transparent text-muted-foreground hover:bg-accent"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Summary */}
        <div className="p-4 bg-gray-50 dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700">
          <div className="text-sm text-gray-600 dark:text-gray-400">
            Showing {visibleColumns} of {totalColumns} columns
          </div>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto p-6">
          <div className="space-y-2">
            {currentColumns
              .sort((a, b) => a.order - b.order)
              .map((columnPref, index) => {
                const column = columns.find(col => col.id === columnPref.id);
                if (!column) return null;

                return (
                  <div
                    key={columnPref.id}
                    draggable={!column.required}
                    onDragStart={(e) => handleDragStart(e, index)}
                    onDragOver={handleDragOver}
                    onDrop={(e) => handleDrop(e, index)}
                    onDragEnd={handleDragEnd}
                    className={`flex items-center p-3 rounded-lg border transition-colors ${
                      draggedIndex === index
                        ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20'
                        : 'border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800'
                    } ${column.required ? 'opacity-75' : 'cursor-move'}`}
                  >
                    {/* Drag Handle */}
                    {!column.required && (
                      <div className="mr-3 text-gray-400 cursor-grab active:cursor-grabbing">
                        <GripVertical className="h-4 w-4" />
                      </div>
                    )}

                    {/* Column Info */}
                    <div className="flex-1">
                      <div className="text-sm font-medium text-gray-900 dark:text-white">
                        {column.label}
                      </div>
                      {column.required && (
                        <div className="text-xs text-gray-500 dark:text-gray-400">
                          Required column
                        </div>
                      )}
                    </div>

                    {/* Visibility Toggle */}
                    <button
                      onClick={() => !column.required && toggleColumn(columnPref.id)}
                      disabled={column.required}
                      className={`p-2 rounded-lg transition-colors ${
                        column.required
                          ? 'text-gray-400 cursor-not-allowed'
                          : columnPref.visible
                          ? 'text-green-600 hover:bg-green-50 dark:hover:bg-green-900/20'
                          : 'text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-800'
                      }`}
                    >
                      {columnPref.visible ? (
                        <Eye className="h-4 w-4" />
                      ) : (
                        <EyeOff className="h-4 w-4" />
                      )}
                    </button>
                  </div>
                );
              })}
          </div>

          {/* Actions */}
          <div className="mt-6 pt-6 border-t border-gray-200 dark:border-gray-700">
            <div className="flex gap-3">
              <button
                onClick={() => {
                  const allVisible = currentColumns.map(col => ({ ...col, visible: true }));
                  updateColumns(allVisible);
                }}
                className="flex-1 flex items-center justify-center px-4 py-2 text-sm font-medium text-blue-600 hover:text-blue-700 hover:bg-blue-50 dark:hover:bg-blue-900/20 rounded-lg transition-colors"
              >
                <Check className="h-4 w-4 mr-2" />
                Show All
              </button>
              <button
                onClick={() => {
                  const onlyRequired = currentColumns.map(col => ({
                    ...col,
                    visible: columns.find(c => c.id === col.id)?.required || false,
                  }));
                  updateColumns(onlyRequired);
                }}
                className="flex-1 flex items-center justify-center px-4 py-2 text-sm font-medium text-gray-600 hover:text-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800 rounded-lg transition-colors"
              >
                <EyeOff className="h-4 w-4 mr-2" />
                Hide Optional
              </button>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default ColumnManager;
