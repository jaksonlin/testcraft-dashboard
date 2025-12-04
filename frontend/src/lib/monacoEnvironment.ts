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

/**
 * Ensure Monaco environment is set up with bundled worker configuration.
 * This uses Vite's bundled workers for reliable offline functionality.
 * 
 * @monaco-editor/react will handle Monaco loading automatically.
 * We only need to configure the workers.
 */
export const ensureMonacoEnvironment = () => {
  debugLog('ensureMonacoEnvironment called');
  
  if (isConfigured) {
    debugLog('Monaco environment already configured');
    return;
  }

  debugLog('Configuring Monaco workers (bundled)...');

  // Configure Monaco workers using bundled workers from Vite
  // These are bundled at build time and work offline
  (self as unknown as { MonacoEnvironment?: { getWorker: (workerId: string, label: string) => Worker } }).MonacoEnvironment = {
    getWorker(_: string, label: string) {
      debugLog('Creating bundled worker for label:', label);
      try {
        let worker: Worker;
        switch (label) {
          case 'json':
            worker = new jsonWorker();
            break;
          case 'css':
          case 'scss':
          case 'less':
            worker = new cssWorker();
            break;
          case 'html':
          case 'handlebars':
          case 'razor':
            worker = new htmlWorker();
            break;
          case 'typescript':
          case 'javascript':
            worker = new tsWorker();
            break;
          default:
            worker = new editorWorker();
        }
        debugLog('✓ Bundled worker created for:', label);
        return worker;
      } catch (error) {
        debugError('Failed to create worker for label:', label, error);
        throw error;
      }
    }
  };

  isConfigured = true;
  debugLog('✓ Monaco environment configured (using bundled workers)');
  console.log('[Monaco] ✓ Environment setup complete - using bundled workers');
};

