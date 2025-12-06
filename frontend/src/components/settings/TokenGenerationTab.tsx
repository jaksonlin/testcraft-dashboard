import React, { useState } from 'react';
import { api, type TokenGenerationResponse } from '../../lib/api';
import { Key, Copy, Check, RefreshCw, AlertCircle, Info } from 'lucide-react';

const TokenGenerationTab: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [tokenData, setTokenData] = useState<TokenGenerationResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [copied, setCopied] = useState<'token' | 'refresh' | null>(null);

  const generateToken = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await api.auth.generateToken();
      setTokenData(response);
    } catch (err: unknown) {
      console.error('Failed to generate token:', err);
      if (err && typeof err === 'object' && 'response' in err) {
        const axiosError = err as { response?: { data?: { message?: string; error?: string } } };
        const errorMessage = axiosError.response?.data?.message || 
                            axiosError.response?.data?.error || 
                            'Failed to generate token. Please try again.';
        setError(errorMessage);
      } else {
        setError('Failed to generate token. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  const copyToClipboard = async (text: string, type: 'token' | 'refresh') => {
    try {
      await navigator.clipboard.writeText(text);
      setCopied(type);
      setTimeout(() => setCopied(null), 2000);
    } catch (err) {
      console.error('Failed to copy:', err);
    }
  };

  const formatExpiration = (milliseconds: number): string => {
    const hours = Math.floor(milliseconds / (1000 * 60 * 60));
    const days = Math.floor(hours / 24);
    if (days > 0) {
      return `${days} day${days > 1 ? 's' : ''}`;
    }
    return `${hours} hour${hours > 1 ? 's' : ''}`;
  };

  return (
    <div className="space-y-6">
      <div className="bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg p-4">
        <div className="flex items-start">
          <Info className="h-5 w-5 text-blue-600 dark:text-blue-400 mt-0.5 mr-3 flex-shrink-0" />
          <div className="flex-1">
            <h3 className="text-sm font-medium text-blue-900 dark:text-blue-100 mb-1">
              API Tokens for MCP Clients
            </h3>
            <p className="text-sm text-blue-700 dark:text-blue-300">
              Generate authentication tokens to use with MCP (Model Context Protocol) clients. 
              These tokens allow external applications to access the MCP endpoints securely. 
              Tokens expire after {tokenData ? formatExpiration(tokenData.expiresIn) : '24 hours'}.
            </p>
          </div>
        </div>
      </div>

      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-lg font-semibold text-gray-900 dark:text-gray-100">
            Generate New Token
          </h2>
          <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
            Click the button below to generate a new access token and refresh token
          </p>
        </div>
        <button
          onClick={generateToken}
          disabled={loading}
          className="inline-flex items-center px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {loading ? (
            <>
              <RefreshCw className="h-4 w-4 mr-2 animate-spin" />
              Generating...
            </>
          ) : (
            <>
              <Key className="h-4 w-4 mr-2" />
              Generate Token
            </>
          )}
        </button>
      </div>

      {error && (
        <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg p-4 flex items-start">
          <AlertCircle className="h-5 w-5 text-red-600 dark:text-red-400 mt-0.5 mr-3 flex-shrink-0" />
          <p className="text-sm text-red-700 dark:text-red-300">{error}</p>
        </div>
      )}

      {tokenData && (
        <div className="space-y-4">
          <div className="bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg p-6">
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Access Token
                </label>
                <div className="flex items-center space-x-2">
                  <input
                    type="text"
                    readOnly
                    value={tokenData.token}
                    className="flex-1 px-3 py-2 bg-gray-50 dark:bg-gray-900 border border-gray-300 dark:border-gray-600 rounded-lg text-sm font-mono text-gray-900 dark:text-gray-100"
                  />
                  <button
                    onClick={() => copyToClipboard(tokenData.token, 'token')}
                    className="p-2 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-lg transition-colors"
                    title="Copy to clipboard"
                  >
                    {copied === 'token' ? (
                      <Check className="h-4 w-4 text-green-600 dark:text-green-400" />
                    ) : (
                      <Copy className="h-4 w-4 text-gray-600 dark:text-gray-400" />
                    )}
                  </button>
                </div>
                <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">
                  Expires in: {formatExpiration(tokenData.expiresIn)}
                </p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Refresh Token
                </label>
                <div className="flex items-center space-x-2">
                  <input
                    type="text"
                    readOnly
                    value={tokenData.refreshToken}
                    className="flex-1 px-3 py-2 bg-gray-50 dark:bg-gray-900 border border-gray-300 dark:border-gray-600 rounded-lg text-sm font-mono text-gray-900 dark:text-gray-100"
                  />
                  <button
                    onClick={() => copyToClipboard(tokenData.refreshToken, 'refresh')}
                    className="p-2 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 rounded-lg transition-colors"
                    title="Copy to clipboard"
                  >
                    {copied === 'refresh' ? (
                      <Check className="h-4 w-4 text-green-600 dark:text-green-400" />
                    ) : (
                      <Copy className="h-4 w-4 text-gray-600 dark:text-gray-400" />
                    )}
                  </button>
                </div>
                <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">
                  Use this to refresh your access token when it expires
                </p>
              </div>
            </div>
          </div>

          <div className="bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800 rounded-lg p-4">
            <h3 className="text-sm font-medium text-yellow-900 dark:text-yellow-100 mb-2">
              Usage Instructions
            </h3>
            <ul className="text-sm text-yellow-700 dark:text-yellow-300 space-y-1 list-disc list-inside">
              <li>Store these tokens securely - they provide access to your account</li>
              <li>Use the access token in the Authorization header: <code className="bg-yellow-100 dark:bg-yellow-900/50 px-1 rounded">Bearer YOUR_TOKEN</code></li>
              <li>Use the refresh token to get a new access token when it expires</li>
              <li>Do not share these tokens publicly or commit them to version control</li>
            </ul>
          </div>
        </div>
      )}
    </div>
  );
};

export default TokenGenerationTab;

