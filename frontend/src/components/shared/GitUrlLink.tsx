import React, { useState } from 'react';
import { ExternalLink, Copy, Check } from 'lucide-react';

interface GitUrlLinkProps {
  url: string;
  className?: string;
  showIcon?: boolean;
  copyable?: boolean;
  truncate?: boolean;
  maxWidth?: string;
}

const GitUrlLink: React.FC<GitUrlLinkProps> = ({
  url,
  className = '',
  showIcon = true,
  copyable = true,
  truncate = false,
  maxWidth = 'max-w-xs'
}) => {
  const [copied, setCopied] = useState(false);

  // Validate and format URL
  const isValidUrl = (url: string): boolean => {
    try {
      new URL(url);
      return true;
    } catch {
      return false;
    }
  };

  const formatUrl = (url: string): string => {
    if (!url) return '';
    
    // If it's already a valid URL, return as is
    if (isValidUrl(url)) {
      return url;
    }
    
    // If it looks like a Git URL without protocol, add https://
    if (url.includes('github.com') || url.includes('gitlab.com') || url.includes('bitbucket.org')) {
      return `https://${url}`;
    }
    
    return url;
  };

  const handleCopy = async (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    
    try {
      await navigator.clipboard.writeText(url);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      console.error('Failed to copy URL:', err);
    }
  };

  const handleClick = (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    
    const formattedUrl = formatUrl(url);
    if (isValidUrl(formattedUrl)) {
      window.open(formattedUrl, '_blank', 'noopener,noreferrer');
    }
  };

  const displayUrl = truncate && url.length > 50 ? `${url.substring(0, 47)}...` : url;
  const formattedUrl = formatUrl(url);
  const isClickable = isValidUrl(formattedUrl);

  return (
    <div className={`flex items-center space-x-2 ${className}`}>
      {showIcon && (
        <ExternalLink className="h-3 w-3 text-gray-400 dark:text-gray-500 flex-shrink-0" />
      )}
      
      <div className={`flex items-center space-x-1 ${truncate ? maxWidth : ''}`}>
        <span
          className={`text-sm font-mono ${
            isClickable 
              ? 'text-blue-600 dark:text-blue-400 hover:text-blue-800 dark:hover:text-blue-300 cursor-pointer hover:underline' 
              : 'text-gray-500 dark:text-gray-400'
          } ${truncate ? 'truncate' : ''}`}
          onClick={isClickable ? handleClick : undefined}
          title={url}
        >
          {displayUrl}
        </span>
        
        {copyable && (
          <button
            onClick={handleCopy}
            className="text-gray-400 dark:text-gray-500 hover:text-gray-600 dark:hover:text-gray-300 transition-colors p-1 rounded hover:bg-gray-100 dark:hover:bg-gray-700"
            title="Copy URL to clipboard"
          >
            {copied ? (
              <Check className="h-3 w-3 text-green-600 dark:text-green-400" />
            ) : (
              <Copy className="h-3 w-3" />
            )}
          </button>
        )}
      </div>
    </div>
  );
};

export default GitUrlLink;
