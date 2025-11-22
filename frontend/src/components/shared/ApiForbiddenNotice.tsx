import React, { useEffect, useState } from 'react';

interface ForbiddenDetail {
  url?: string;
  method?: string;
}

const ApiForbiddenNotice: React.FC = () => {
  const [visible, setVisible] = useState(false);
  const [detail, setDetail] = useState<ForbiddenDetail | null>(null);

  useEffect(() => {
    const onForbidden = (e: Event) => {
      const custom = e as CustomEvent<ForbiddenDetail>;
      setDetail(custom.detail || null);
      setVisible(true);

      // Auto-hide after 6s
      window.setTimeout(() => setVisible(false), 6000);
    };

    window.addEventListener('api:forbidden', onForbidden as EventListener);
    return () => {
      window.removeEventListener('api:forbidden', onForbidden as EventListener);
    };
  }, []);

  if (!visible) return null;

  return (
    <div className="fixed bottom-6 right-6 z-50 max-w-sm w-[360px]">
      <div className="rounded-lg border border-red-200 dark:border-red-900 bg-white dark:bg-gray-900 shadow-lg overflow-hidden">
        <div className="px-4 py-3 bg-red-50 dark:bg-red-900/30 text-red-800 dark:text-red-200 font-semibold">
          Access denied
        </div>
        <div className="px-4 py-3 text-sm text-gray-800 dark:text-gray-200">
          You don't have permission to perform this action.
          {detail?.method || detail?.url ? (
            <div className="mt-2 text-xs text-gray-600 dark:text-gray-400">
              {detail?.method ? <span className="font-mono mr-1">{detail.method}</span> : null}
              {detail?.url ? <span className="font-mono break-all">{detail.url}</span> : null}
            </div>
          ) : null}
        </div>
        <div className="px-4 py-2 bg-gray-50 dark:bg-gray-800 flex justify-end">
          <button
            type="button"
            className="text-xs px-2 py-1 rounded-md text-gray-700 dark:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-700"
            onClick={() => setVisible(false)}
          >
            Dismiss
          </button>
        </div>
      </div>
    </div>
  );
};

export default ApiForbiddenNotice;


