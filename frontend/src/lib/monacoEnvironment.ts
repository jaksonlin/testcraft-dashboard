import * as monaco from 'monaco-editor';
import { loader } from '@monaco-editor/react';
import editorWorker from 'monaco-editor/esm/vs/editor/editor.worker?worker';
import jsonWorker from 'monaco-editor/esm/vs/language/json/json.worker?worker';
import cssWorker from 'monaco-editor/esm/vs/language/css/css.worker?worker';
import htmlWorker from 'monaco-editor/esm/vs/language/html/html.worker?worker';
import tsWorker from 'monaco-editor/esm/vs/language/typescript/ts.worker?worker';

let isConfigured = false;

// Debug flag - set to true to enable verbose logging
const DEBUG = true;

const debugLog = (...args: unknown[]) => {
  if (DEBUG) {
    console.log('[Monaco Debug]', ...args);
  }
};

const debugError = (...args: unknown[]) => {
  console.error('[Monaco Debug Error]', ...args);
};

// Configure loader to use the bundled Monaco instance
// This avoids loading from CDN or external paths and ensures compatibility
const configureLoader = () => {
  try {
    loader.config({ monaco });
    debugLog('✓ Loader configured - using bundled Monaco instance');
  } catch (error) {
    debugError('Failed to configure loader:', error);
  }
};

// Configure loader immediately when module loads
configureLoader();

export const ensureMonacoEnvironment = () => {
  if (isConfigured) {
    return;
  }

  debugLog('Configuring Monaco workers (bundled)...');

  // Configure Monaco workers using bundled workers from Vite
  // These are bundled at build time and work offline
  (self as unknown as { MonacoEnvironment?: { getWorker: (workerId: string, label: string) => Worker } }).MonacoEnvironment = {
    getWorker(_: string, label: string) {
      debugLog('Creating bundled worker for label:', label);
      console.log('[Monaco] Creating worker for:', label);
      try {
        switch (label) {
          case 'json':
            return new jsonWorker();
          case 'css':
          case 'scss':
          case 'less':
            return new cssWorker();
          case 'html':
          case 'handlebars':
          case 'razor':
            return new htmlWorker();
          case 'typescript':
          case 'javascript':
            return new tsWorker();
          default:
            return new editorWorker();
        }
      } catch (error) {
        debugError('Failed to create worker for label:', label, error);
        throw error;
      }
    }
  };

  isConfigured = true;
  debugLog('✓ Monaco environment configured (CDN disabled, using local assets from /monaco/vs)');
  console.log('[Monaco] ✓ Environment setup complete - CDN disabled, using local assets from /monaco/vs');

  // Pre-load Monaco to make it available for @monaco-editor/react
  // With bundled Monaco, we don't need to call loader.init() manually
  debugLog('Monaco environment ready (bundled)');
};
