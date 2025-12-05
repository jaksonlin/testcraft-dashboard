import { loader } from '@monaco-editor/react';
import editorWorker from 'monaco-editor/esm/vs/editor/editor.worker?worker';
import jsonWorker from 'monaco-editor/esm/vs/language/json/json.worker?worker';
import cssWorker from 'monaco-editor/esm/vs/language/css/css.worker?worker';
import htmlWorker from 'monaco-editor/esm/vs/language/html/html.worker?worker';
import tsWorker from 'monaco-editor/esm/vs/language/typescript/ts.worker?worker';

let isConfigured = false;
let monacoLoadPromise: Promise<typeof import('monaco-editor')> | null = null;

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

// Configure loader to use local Monaco assets from /monaco/vs
// This prevents it from trying to load from https://cdn.jsdelivr.net/
// The assets are copied by the prepare script from node_modules/monaco-editor/min/vs to public/monaco/vs
const configureLoader = () => {
  try {
    loader.config({
      paths: {
        vs: '/monaco/vs' // Point to the copied Monaco assets in public/monaco/vs
      }
    });
    debugLog('✓ Loader configured - will use local assets from /monaco/vs (CDN disabled)');
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
  // This ensures Monaco is loaded before the Editor component tries to use it
  debugLog('Pre-loading Monaco editor...');
  if (!monacoLoadPromise) {
    monacoLoadPromise = loader.init().then((monaco) => {
      debugLog('✓ Monaco editor pre-loaded successfully');
      console.log('[Monaco] ✓ Monaco editor loaded and ready');
      return monaco;
    }).catch((error) => {
      debugError('Failed to pre-load Monaco editor:', error);
      monacoLoadPromise = null; // Reset so we can retry
      throw error;
    });
  }
};


