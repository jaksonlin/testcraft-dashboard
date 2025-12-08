import AppRouter from './routes';
import { PreferencesProvider } from './contexts/PreferencesContext';
import { AuthProvider } from './contexts/AuthContext';
import './lib/i18n'; // Initialize i18n
import './App.css';

function App() {
  return (
    <AuthProvider>
      <PreferencesProvider>
        <div className="App">
          <AppRouter />
        </div>
      </PreferencesProvider>
    </AuthProvider>
  );
}

export default App;