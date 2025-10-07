import React, { useState } from 'react';
import { 
  Play, 
  Download, 
  Trash2, 
  RefreshCw, 
  MoreHorizontal,
  CheckSquare,
  Square,
  X
} from 'lucide-react';

export interface BulkAction {
  id: string;
  label: string;
  icon: React.ReactNode;
  variant: 'primary' | 'secondary' | 'success' | 'warning' | 'danger';
  onClick: (selectedIds: number[]) => Promise<void>;
  confirmMessage?: string;
  loadingText?: string;
}

export interface BulkOperationsProps {
  selectedItems: Set<number>;
  totalItems: number;
  onSelectAll: () => void;
  onClearSelection: () => void;
  actions: BulkAction[];
  itemType: string; // e.g., "repositories", "teams", "methods"
  className?: string;
}

const BulkOperations: React.FC<BulkOperationsProps> = ({
  selectedItems,
  totalItems,
  onSelectAll,
  onClearSelection,
  actions,
  itemType,
  className = ""
}) => {
  const [loadingAction, setLoadingAction] = useState<string | null>(null);
  const [showActions, setShowActions] = useState(false);

  const selectedCount = selectedItems.size;
  const isAllSelected = selectedCount === totalItems && totalItems > 0;
  const isPartiallySelected = selectedCount > 0 && selectedCount < totalItems;

  const handleAction = async (action: BulkAction) => {
    if (selectedCount === 0) return;

    // Show confirmation if required
    if (action.confirmMessage) {
      const confirmed = window.confirm(
        `${action.confirmMessage}\n\nThis will affect ${selectedCount} ${itemType}.`
      );
      if (!confirmed) return;
    }

    try {
      setLoadingAction(action.id);
      await action.onClick(Array.from(selectedItems));
    } catch (error) {
      console.error(`Error executing ${action.label}:`, error);
      // You might want to show a toast notification here
    } finally {
      setLoadingAction(null);
    }
  };

  const handleSelectAll = () => {
    if (isAllSelected) {
      onClearSelection();
    } else {
      onSelectAll();
    }
  };

  if (selectedCount === 0) {
    return (
      <div className={`flex items-center justify-between ${className}`}>
        <div className="flex items-center">
          <button
            onClick={handleSelectAll}
            className="flex items-center text-sm text-gray-600 hover:text-gray-800"
          >
            {isAllSelected ? (
              <CheckSquare className="h-4 w-4 mr-2" />
            ) : (
              <Square className="h-4 w-4 mr-2" />
            )}
            Select All ({totalItems})
          </button>
        </div>
        <div className="text-sm text-gray-500">
          No {itemType} selected
        </div>
      </div>
    );
  }

  return (
    <div className={`bg-blue-50 border border-blue-200 rounded-lg p-4 ${className}`}>
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-4">
          <div className="flex items-center">
            <button
              onClick={handleSelectAll}
              className="flex items-center text-sm font-medium text-blue-700 hover:text-blue-800"
            >
              {isAllSelected ? (
                <CheckSquare className="h-4 w-4 mr-2" />
              ) : isPartiallySelected ? (
                <div className="h-4 w-4 mr-2 border-2 border-blue-600 bg-blue-100 rounded flex items-center justify-center">
                  <div className="h-2 w-2 bg-blue-600 rounded"></div>
                </div>
              ) : (
                <Square className="h-4 w-4 mr-2" />
              )}
              {isAllSelected ? 'Deselect All' : 'Select All'} ({totalItems})
            </button>
          </div>
          
          <div className="text-sm text-blue-700">
            <span className="font-medium">{selectedCount}</span> {itemType} selected
          </div>
        </div>

        <div className="flex items-center space-x-2">
          {/* Primary Actions */}
          <div className="flex items-center space-x-2">
            {actions.slice(0, 3).map((action) => (
              <button
                key={action.id}
                onClick={() => handleAction(action)}
                disabled={loadingAction === action.id}
                className={`btn btn-sm ${
                  action.variant === 'primary' ? 'btn-primary' :
                  action.variant === 'secondary' ? 'btn-secondary' :
                  action.variant === 'success' ? 'btn-success' :
                  action.variant === 'warning' ? 'btn-warning' :
                  'btn-danger'
                } ${loadingAction === action.id ? 'opacity-50 cursor-not-allowed' : ''}`}
              >
                {loadingAction === action.id ? (
                  <RefreshCw className="h-4 w-4 mr-2 animate-spin" />
                ) : (
                  action.icon
                )}
                <span className="ml-2">
                  {loadingAction === action.id ? action.loadingText || 'Processing...' : action.label}
                </span>
              </button>
            ))}
          </div>

          {/* More Actions Dropdown */}
          {actions.length > 3 && (
            <div className="relative">
              <button
                onClick={() => setShowActions(!showActions)}
                className="btn btn-sm btn-secondary"
              >
                <MoreHorizontal className="h-4 w-4" />
              </button>

              {showActions && (
                <div className="absolute right-0 top-full mt-1 w-48 bg-white rounded-md shadow-lg border border-gray-200 z-10">
                  <div className="py-1">
                    {actions.slice(3).map((action) => (
                      <button
                        key={action.id}
                        onClick={() => {
                          handleAction(action);
                          setShowActions(false);
                        }}
                        disabled={loadingAction === action.id}
                        className={`w-full text-left px-4 py-2 text-sm hover:bg-gray-100 flex items-center ${
                          loadingAction === action.id ? 'opacity-50 cursor-not-allowed' : ''
                        }`}
                      >
                        {loadingAction === action.id ? (
                          <RefreshCw className="h-4 w-4 mr-2 animate-spin" />
                        ) : (
                          <span className="mr-2">{action.icon}</span>
                        )}
                        {loadingAction === action.id ? action.loadingText || 'Processing...' : action.label}
                      </button>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}

          <button
            onClick={onClearSelection}
            className="btn btn-sm btn-secondary"
            title="Clear selection"
          >
            <X className="h-4 w-4" />
          </button>
        </div>
      </div>

      {/* Action Progress Indicator */}
      {loadingAction && (
        <div className="mt-3 pt-3 border-t border-blue-200">
          <div className="flex items-center text-sm text-blue-600">
            <RefreshCw className="h-4 w-4 mr-2 animate-spin" />
            Processing {selectedCount} {itemType}...
          </div>
        </div>
      )}
    </div>
  );
};

export default BulkOperations;
