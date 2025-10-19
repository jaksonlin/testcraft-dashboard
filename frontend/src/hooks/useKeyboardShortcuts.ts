import { useEffect, useCallback, useState } from 'react';

interface KeyboardShortcut {
  key: string;
  ctrlKey?: boolean;
  shiftKey?: boolean;
  altKey?: boolean;
  callback: () => void;
  description: string;
}

interface UseKeyboardShortcutsOptions {
  enabled?: boolean;
  preventDefault?: boolean;
}

/**
 * Custom hook for managing keyboard shortcuts
 * Automatically handles event cleanup and prevents shortcuts when typing in inputs
 */
export const useKeyboardShortcuts = (
  shortcuts: KeyboardShortcut[],
  options: UseKeyboardShortcutsOptions = {}
) => {
  const { enabled = true, preventDefault = true } = options;

  const isInputFocused = useCallback(() => {
    const activeElement = document.activeElement;
    const tagName = activeElement?.tagName.toLowerCase();
    return (
      tagName === 'input' ||
      tagName === 'textarea' ||
      tagName === 'select' ||
      (activeElement as HTMLElement)?.isContentEditable
    );
  }, []);

  const handleKeyDown = useCallback(
    (event: KeyboardEvent) => {
      if (!enabled) return;
      
      // Don't trigger shortcuts when typing in inputs (unless explicitly allowed)
      if (isInputFocused() && !event.ctrlKey && !event.metaKey) {
        return;
      }

      // Check each shortcut
      for (const shortcut of shortcuts) {
        const keyMatches = event.key === shortcut.key;
        const ctrlMatches = shortcut.ctrlKey === undefined || event.ctrlKey === shortcut.ctrlKey;
        const shiftMatches = shortcut.shiftKey === undefined || event.shiftKey === shortcut.shiftKey;
        const altMatches = shortcut.altKey === undefined || event.altKey === shortcut.altKey;

        if (keyMatches && ctrlMatches && shiftMatches && altMatches) {
          if (preventDefault) {
            event.preventDefault();
          }
          shortcut.callback();
          return;
        }
      }
    },
    [shortcuts, enabled, preventDefault, isInputFocused]
  );

  useEffect(() => {
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [handleKeyDown]);

  return shortcuts;
};

/**
 * Hook to display keyboard shortcuts help
 */
export const useKeyboardShortcutsHelp = () => {
  const [showHelp, setShowHelp] = useState(false);

  useKeyboardShortcuts([
    {
      key: '?',
      shiftKey: true,
      callback: () => setShowHelp(prev => !prev),
      description: 'Show keyboard shortcuts help'
    }
  ]);

  return { showHelp, setShowHelp };
};

// Helper to check if user is on Mac
export const isMac = () => {
  return typeof window !== 'undefined' && navigator.platform.toUpperCase().indexOf('MAC') >= 0;
};

// Format keyboard shortcut for display
export const formatShortcut = (shortcut: KeyboardShortcut): string => {
  const parts: string[] = [];
  
  if (shortcut.ctrlKey) parts.push(isMac() ? '⌘' : 'Ctrl');
  if (shortcut.shiftKey) parts.push('Shift');
  if (shortcut.altKey) parts.push(isMac() ? '⌥' : 'Alt');
  
  // Format special keys
  const keyDisplay = {
    'ArrowLeft': '←',
    'ArrowRight': '→',
    'ArrowUp': '↑',
    'ArrowDown': '↓',
    'PageUp': 'Page Up',
    'PageDown': 'Page Down',
    'Escape': 'Esc',
    'Enter': '↵',
    '/': '/'
  }[shortcut.key] || shortcut.key.toUpperCase();
  
  parts.push(keyDisplay);
  
  return parts.join('+');
};

