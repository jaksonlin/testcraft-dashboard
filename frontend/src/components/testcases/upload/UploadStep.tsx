/**
 * Upload Step Component - File selection and drag & drop
 */

import React, { useState } from 'react';
import { Upload, AlertCircle } from 'lucide-react';
import type { UploadStepProps } from './types';

export const UploadStep: React.FC<UploadStepProps> = ({ onFileSelect }) => {
  const [dragActive, setDragActive] = useState(false);

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setDragActive(false);

    const files = e.dataTransfer.files;
    if (files.length > 0) {
      onFileSelect(files[0]);
    }
  };

  const handleFileInput = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (files && files.length > 0) {
      onFileSelect(files[0]);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Upload Test Cases</h2>
        <p className="text-gray-600">
          Upload your test case Excel file. The system will automatically detect column mappings.
        </p>
      </div>

      {/* Drag & Drop Area */}
      <div
        className={`border-2 border-dashed rounded-lg p-12 text-center transition-colors ${
          dragActive
            ? 'border-blue-500 bg-blue-50'
            : 'border-gray-300 hover:border-gray-400'
        }`}
        onDragOver={(e) => { e.preventDefault(); setDragActive(true); }}
        onDragLeave={() => setDragActive(false)}
        onDrop={handleDrop}
      >
        <Upload className="w-16 h-16 mx-auto mb-4 text-gray-400" />
        <h3 className="text-lg font-semibold mb-2">Drag and drop your Excel file here</h3>
        <p className="text-gray-600 mb-4">or</p>
        <label className="inline-block">
          <input
            type="file"
            accept=".xlsx,.xls"
            onChange={handleFileInput}
            className="hidden"
          />
          <span className="px-6 py-3 bg-blue-600 text-white rounded-lg cursor-pointer hover:bg-blue-700 transition-colors inline-block">
            Choose File
          </span>
        </label>
        <p className="text-sm text-gray-500 mt-4">Supported formats: .xlsx, .xls</p>
      </div>

      {/* Info Box */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
        <div className="flex items-start">
          <AlertCircle className="w-5 h-5 text-blue-600 mt-0.5 mr-3 flex-shrink-0" />
          <div className="text-sm text-blue-900">
            <p className="font-semibold mb-1">What happens next?</p>
            <ul className="list-disc list-inside space-y-1">
              <li>System analyzes your Excel file structure</li>
              <li>Auto-detects column mappings (ID, Title, Steps, etc.)</li>
              <li>Shows preview of your test cases</li>
              <li>You review and adjust if needed</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
};
