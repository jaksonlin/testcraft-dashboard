/**
 * Date utility functions for formatting and displaying dates
 */

export interface FormattedDate {
  date: string;
  time: string;
  relative: string;
}

/**
 * Formats a date string into date, time, and relative time
 */
export const formatDate = (dateString: string): FormattedDate => {
  const date = new Date(dateString);
  return {
    date: date.toLocaleDateString(),
    time: date.toLocaleTimeString(),
    relative: getRelativeTime(date)
  };
};

/**
 * Gets a human-readable relative time from a date
 */
export const getRelativeTime = (date: Date): string => {
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
  const diffDays = Math.floor(diffHours / 24);

  if (diffDays > 0) return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`;
  if (diffHours > 0) return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`;
  return 'Just now';
};

