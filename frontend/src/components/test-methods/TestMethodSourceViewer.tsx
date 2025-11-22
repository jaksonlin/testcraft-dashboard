import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Maximize2, Minimize2, X } from 'lucide-react';
import type { TestMethodSource } from '../../lib/api';
import Editor, { type Monaco, type OnMount } from '@monaco-editor/react';
import type { editor as MonacoEditor } from 'monaco-editor';
import { ensureMonacoEnvironment } from '../../lib/monacoEnvironment';

interface TestMethodSourceViewerProps {
  isOpen: boolean;
  loading: boolean;
  error?: string | null;
  source?: TestMethodSource | null;
  onClose: () => void;
}

const TestMethodSourceViewer: React.FC<TestMethodSourceViewerProps> = ({
  isOpen,
  loading,
  error,
  source,
  onClose
}) => {
  ensureMonacoEnvironment();
  const editorRef = useRef<MonacoEditor.IStandaloneCodeEditor | null>(null);
  const monacoRef = useRef<Monaco | null>(null);
  const decorationsRef = useRef<string[]>([]);
  const [isFullscreen, setIsFullscreen] = useState(false);

  const language = useMemo(() => {
    if (!source?.filePath) {
      return 'java';
    }
    const extension = source.filePath.split('.').pop()?.toLowerCase();
    switch (extension) {
      case 'java':
        return 'java';
      case 'kt':
        return 'kotlin';
      case 'js':
      case 'mjs':
      case 'cjs':
        return 'javascript';
      case 'ts':
        return 'typescript';
      case 'tsx':
        return 'typescriptreact';
      case 'jsx':
        return 'javascriptreact';
      case 'py':
        return 'python';
      case 'rb':
        return 'ruby';
      case 'go':
        return 'go';
      case 'cs':
        return 'csharp';
      case 'php':
        return 'php';
      case 'xml':
      case 'html':
        return 'xml';
      case 'json':
        return 'json';
      case 'yaml':
      case 'yml':
        return 'yaml';
      default:
        return 'java';
    }
  }, [source?.filePath]);

  const applyHighlight = useCallback(() => {
    const editor = editorRef.current;
    const monacoInstance = monacoRef.current;

    if (!editor || !monacoInstance || !source?.methodLine) {
      return;
    }

    const lineNumber = source.methodLine;
    const columnEnd = editor.getModel()?.getLineMaxColumn(lineNumber) ?? 1;

    editor.revealLineInCenter(lineNumber);
    editor.setSelection({
      startLineNumber: lineNumber,
      startColumn: 1,
      endLineNumber: lineNumber,
      endColumn: columnEnd
    });

    decorationsRef.current = editor.deltaDecorations(decorationsRef.current, [
      {
        range: new monacoInstance.Range(lineNumber, 1, lineNumber, columnEnd),
        options: {
          isWholeLine: true,
          className: 'tm-source-highlight',
          inlineClassName: 'tm-source-highlight-inline'
        }
      }
    ]);
  }, [source?.methodLine]);

  useEffect(() => {
    applyHighlight();
  }, [applyHighlight, source?.methodLine]);

  useEffect(() => {
    return () => {
      editorRef.current = null;
      monacoRef.current = null;
      decorationsRef.current = [];
    };
  }, []);

  const handleEditorMount: OnMount = (editor, monacoInstance) => {
    editorRef.current = editor;
    monacoRef.current = monacoInstance;
    editor.updateOptions({
      readOnly: true,
      minimap: { enabled: false },
      scrollBeyondLastLine: false,
      lineNumbersMinChars: 4,
      wordWrap: 'on',
      automaticLayout: true,
      renderLineHighlight: 'all'
    });

    monacoInstance.editor.defineTheme('tm-source-dark', {
      base: 'vs-dark',
      inherit: true,
      rules: [],
      colors: {
        'editor.background': '#0f1729',
        'editorLineNumber.foreground': '#64748b',
        'editor.selectionBackground': '#fde04744'
      }
    });
    monacoInstance.editor.setTheme('tm-source-dark');

    applyHighlight();
  };

  useEffect(() => {
    if (!isOpen) {
      return;
    }

    if (editorRef.current && source?.classContent) {
      editorRef.current.setValue(source.classContent);
      requestAnimationFrame(() => {
        applyHighlight();
      });
    }
  }, [isOpen, source, applyHighlight]);

  useEffect(() => {
    if (!isOpen) {
      return;
    }

    if (editorRef.current) {
      const timeout = window.setTimeout(() => {
        applyHighlight();
      }, 50);

      return () => window.clearTimeout(timeout);
    }
  }, [isFullscreen, applyHighlight, isOpen]);

  if (!isOpen) {
    return null;
  }

  return (
    <div
      className={`fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm ${
        isFullscreen ? '' : 'px-4'
      }`}
    >
      <div
        className={`bg-white dark:bg-gray-900 rounded-xl shadow-2xl flex flex-col overflow-hidden border border-gray-200 dark:border-gray-700 transition-all duration-200 ${
          isFullscreen
            ? 'w-[95vw] h-[95vh]'
            : 'w-full max-w-6xl h-[80vh]'
        }`}
      >
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200 dark:border-gray-700">
          <div>
            <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">
              {source ? source.testClassName : 'Loading source...'}
            </h3>
            <p className="text-sm text-gray-600 dark:text-gray-400">
              {source
                ? `${source.packageName || 'Unknown package'} • ${source.filePath || 'No file path available'}`
                : 'Fetching test class content'}
            </p>
          </div>
          <div className="flex items-center space-x-2">
            <button
              onClick={() => setIsFullscreen((prev: boolean) => !prev)}
              className="inline-flex items-center justify-center h-9 w-9 rounded-full text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
              aria-label={isFullscreen ? 'Exit fullscreen' : 'Enter fullscreen'}
            >
              {isFullscreen ? <Minimize2 className="h-4 w-4" /> : <Maximize2 className="h-4 w-4" />}
            </button>
            <button
              onClick={() => {
                setIsFullscreen(false);
                onClose();
              }}
              className="inline-flex items-center justify-center h-9 w-9 rounded-full text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
              aria-label="Close test class viewer"
            >
              <X className="h-4 w-4" />
            </button>
          </div>
        </div>

        <div className="p-4 flex-1 flex flex-col space-y-4">
          {error && (
            <div className="rounded-md border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700 dark:border-red-800 dark:bg-red-950/40 dark:text-red-300">
              {error}
            </div>
          )}

          {loading && (
            <div className="flex-1 flex items-center justify-center rounded-lg border border-dashed border-gray-300 dark:border-gray-700">
              <span className="text-sm text-gray-600 dark:text-gray-400">Loading test class content…</span>
            </div>
          )}

          {!loading && !source && !error && (
            <div className="flex-1 flex items-center justify-center rounded-lg border border-dashed border-gray-300 dark:border-gray-700">
              <span className="text-sm text-gray-600 dark:text-gray-400">No source content available for this method.</span>
            </div>
          )}
          {!loading && source && (
            <div className="flex-1 overflow-hidden rounded-lg border border-gray-200 dark:border-gray-800">
              <Editor
                height="100%"
                value={source.classContent}
                defaultLanguage={language}
                theme="tm-source-dark"
                onMount={handleEditorMount}
                options={{
                  readOnly: true,
                  minimap: { enabled: false },
                  scrollBeyondLastLine: false,
                  lineNumbersMinChars: 4,
                  wordWrap: 'on'
                }}
              />
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default TestMethodSourceViewer;

