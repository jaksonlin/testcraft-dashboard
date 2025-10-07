import React from 'react';
import { AlertCircle, CheckCircle } from 'lucide-react';

interface StatusMessagesProps {
  error: string | null;
  success: string | null;
}

const StatusMessages: React.FC<StatusMessagesProps> = ({ error, success }) => {
  if (!error && !success) return null;

  return (
    <>
      {error && (
        <div className="mb-6 p-4 border rounded-lg flex items-center" style={{ backgroundColor: 'var(--color-destructive)', borderColor: 'var(--color-destructive)', color: 'var(--color-destructive-foreground)' }}>
          <AlertCircle className="h-5 w-5 text-red-600 dark:text-red-400 mr-3" />
          <span style={{ color: 'var(--color-destructive-foreground)' }}>{error}</span>
        </div>
      )}

      {success && (
        <div className="mb-6 p-4 border rounded-lg flex items-center" style={{ backgroundColor: 'var(--color-success)', borderColor: 'var(--color-success)', color: 'var(--color-success-foreground)' }}>
          <CheckCircle className="h-5 w-5 text-green-600 dark:text-green-400 mr-3" />
          <span style={{ color: 'var(--color-success-foreground)' }}>{success}</span>
        </div>
      )}
    </>
  );
};

export default StatusMessages;

