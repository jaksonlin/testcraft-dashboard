import { loader } from '@monaco-editor/react';
import editorWorker from 'monaco-editor/esm/vs/editor/editor.worker?worker';
import jsonWorker from 'monaco-editor/esm/vs/language/json/json.worker?worker';
import cssWorker from 'monaco-editor/esm/vs/language/css/css.worker?worker';
import htmlWorker from 'monaco-editor/esm/vs/language/html/html.worker?worker';
import tsWorker from 'monaco-editor/esm/vs/language/typescript/ts.worker?worker';

let isConfigured = false;

const configureLoader = () => {
  const base = import.meta.env.BASE_URL?.replace(/\/$/, '') ?? '';
  const baseUrl = `${base}/monaco`;
  loader.config({ paths: { vs: `${baseUrl}/vs` } });
};

configureLoader();

export const ensureMonacoEnvironment = () => {
  if (isConfigured) {
    return;
  }

  (self as unknown as { MonacoEnvironment?: { getWorker: (workerId: string, label: string) => Worker } }).MonacoEnvironment = {
    getWorker(_: string, label: string) {
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
    }
  };

  isConfigured = true;
};

