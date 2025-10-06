import { useState, useCallback } from 'react';

export interface UseBulkOperationsOptions<T> {
  items: T[];
  getId: (item: T) => number;
  onBulkAction?: (selectedIds: number[], action: string) => Promise<void>;
}

export interface UseBulkOperationsReturn {
  selectedItems: Set<number>;
  selectedCount: number;
  isAllSelected: boolean;
  isPartiallySelected: boolean;
  selectItem: (id: number) => void;
  deselectItem: (id: number) => void;
  toggleItem: (id: number) => void;
  selectAll: () => void;
  clearSelection: () => void;
  isSelected: (id: number) => boolean;
  getSelectedItems: <T>(items: T[], getId: (item: T) => number) => T[];
}

export function useBulkOperations<T>({
  items,
  getId
}: Omit<UseBulkOperationsOptions<T>, 'onBulkAction'>): UseBulkOperationsReturn {
  const [selectedItems, setSelectedItems] = useState<Set<number>>(new Set());

  const selectedCount = selectedItems.size;
  const totalCount = items.length;
  const isAllSelected = selectedCount === totalCount && totalCount > 0;
  const isPartiallySelected = selectedCount > 0 && selectedCount < totalCount;

  const selectItem = useCallback((id: number) => {
    setSelectedItems(prev => new Set([...prev, id]));
  }, []);

  const deselectItem = useCallback((id: number) => {
    setSelectedItems(prev => {
      const newSet = new Set(prev);
      newSet.delete(id);
      return newSet;
    });
  }, []);

  const toggleItem = useCallback((id: number) => {
    setSelectedItems(prev => {
      const newSet = new Set(prev);
      if (newSet.has(id)) {
        newSet.delete(id);
      } else {
        newSet.add(id);
      }
      return newSet;
    });
  }, []);

  const selectAll = useCallback(() => {
    setSelectedItems(new Set(items.map(getId)));
  }, [items, getId]);

  const clearSelection = useCallback(() => {
    setSelectedItems(new Set());
  }, []);

  const isSelected = useCallback((id: number) => {
    return selectedItems.has(id);
  }, [selectedItems]);

  const getSelectedItems = useCallback(<T>(items: T[], getId: (item: T) => number): T[] => {
    return items.filter(item => selectedItems.has(getId(item)));
  }, [selectedItems]);

  return {
    selectedItems,
    selectedCount,
    isAllSelected,
    isPartiallySelected,
    selectItem,
    deselectItem,
    toggleItem,
    selectAll,
    clearSelection,
    isSelected,
    getSelectedItems
  };
}
