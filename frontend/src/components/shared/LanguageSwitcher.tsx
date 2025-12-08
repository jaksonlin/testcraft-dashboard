import React from 'react';
import { useTranslation } from '../../hooks/useTranslation';
import { Globe } from 'lucide-react';

/**
 * Language switcher component
 * Allows users to toggle between English and Simplified Chinese
 */
export const LanguageSwitcher: React.FC = () => {
  const { currentLanguage, changeLanguage, t } = useTranslation();

  const handleLanguageChange = async (e: React.ChangeEvent<HTMLSelectElement>) => {
    const newLang = e.target.value as 'en' | 'zh-CN';
    await changeLanguage(newLang);
  };

  return (
    <div className="flex items-center gap-2 px-2 py-1.5 rounded-lg shadow-md bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700">
      <Globe className="w-4 h-4 text-gray-600 dark:text-gray-400" />
      <select
        value={currentLanguage}
        onChange={handleLanguageChange}
        className="text-sm bg-transparent border-none text-gray-900 dark:text-gray-100 focus:outline-none cursor-pointer appearance-none pr-6"
        aria-label={t('language.switchLanguage')}
      >
        <option value="en">{t('language.english')}</option>
        <option value="zh-CN">{t('language.chinese')}</option>
      </select>
    </div>
  );
};

