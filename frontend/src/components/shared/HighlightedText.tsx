import React from 'react';

interface HighlightedTextProps {
  text: string;
  highlight?: string;
  className?: string;
}

/**
 * Component that highlights matching text within a string
 * Used for search result highlighting
 */
export const HighlightedText: React.FC<HighlightedTextProps> = ({ 
  text, 
  highlight, 
  className = '' 
}) => {
  if (!highlight || !text) {
    return <span className={className}>{text}</span>;
  }

  // Escape special regex characters
  const escapeRegex = (str: string) => {
    return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  };

  try {
    // Split text by highlight matches (case-insensitive)
    const parts = text.split(new RegExp(`(${escapeRegex(highlight)})`, 'gi'));
    
    return (
      <span className={className}>
        {parts.map((part, index) => 
          part.toLowerCase() === highlight.toLowerCase() ? (
            <mark 
              key={index} 
              className="bg-yellow-200 dark:bg-yellow-700 text-gray-900 dark:text-gray-100 px-0.5 rounded"
            >
              {part}
            </mark>
          ) : (
            <React.Fragment key={index}>{part}</React.Fragment>
          )
        )}
      </span>
    );
  } catch {
    // Fallback if regex fails
    return <span className={className}>{text}</span>;
  }
};

export default HighlightedText;

