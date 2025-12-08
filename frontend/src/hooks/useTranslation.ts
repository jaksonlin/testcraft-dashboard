import { useTranslation as useI18nTranslation } from 'react-i18next';

/**
 * Custom hook wrapper for react-i18next's useTranslation
 * Provides type-safe translation access and language switching utilities
 */
export const useTranslation = () => {
  const { t, i18n } = useI18nTranslation();

  /**
   * Change the current language
   * @param lng - Language code ('en' or 'zh-CN')
   */
  const changeLanguage = async (lng: 'en' | 'zh-CN') => {
    await i18n.changeLanguage(lng);
  };

  /**
   * Get the current language
   */
  const currentLanguage = i18n.language as 'en' | 'zh-CN';

  /**
   * Check if current language is Chinese
   */
  const isChinese = currentLanguage === 'zh-CN';

  /**
   * Toggle between English and Chinese
   */
  const toggleLanguage = async () => {
    const newLang = currentLanguage === 'en' ? 'zh-CN' : 'en';
    await changeLanguage(newLang);
  };

  return {
    t,
    changeLanguage,
    currentLanguage,
    isChinese,
    toggleLanguage,
    i18n,
  };
};

