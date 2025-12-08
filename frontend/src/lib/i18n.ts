import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

// Import translation files
import enTranslations from '../locales/en.json';
import zhCNTranslations from '../locales/zh-CN.json';

// Language resources
const resources = {
  en: {
    translation: enTranslations,
  },
  'zh-CN': {
    translation: zhCNTranslations,
  },
};

// Get saved language preference from localStorage
const getSavedLanguage = (): string => {
  try {
    const saved = localStorage.getItem('testcraft-language');
    if (saved && (saved === 'en' || saved === 'zh-CN')) {
      return saved;
    }
  } catch (error) {
    console.error('Failed to load language preference:', error);
  }
  return 'en'; // Default to English
};

i18n
  // Detect user language
  .use(LanguageDetector)
  // Pass the i18n instance to react-i18next
  .use(initReactI18next)
  // Initialize i18next
  .init({
    resources,
    fallbackLng: 'en',
    lng: getSavedLanguage(), // Use saved preference or default to English
    supportedLngs: ['en', 'zh-CN'],
    
    interpolation: {
      escapeValue: false, // React already escapes values
    },
    
    detection: {
      // Order of language detection
      order: ['localStorage', 'navigator'],
      // Keys to lookup language from
      lookupLocalStorage: 'testcraft-language',
      // Cache user language
      caches: ['localStorage'],
    },
  });

// Save language preference when changed
i18n.on('languageChanged', (lng) => {
  try {
    localStorage.setItem('testcraft-language', lng);
  } catch (error) {
    console.error('Failed to save language preference:', error);
  }
});

export default i18n;

