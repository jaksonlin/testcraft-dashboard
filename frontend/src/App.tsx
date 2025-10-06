import AppRouter from './routes';
import { PreferencesProvider } from './contexts/PreferencesContext';
import './App.css';

function App() {
  return (
    <PreferencesProvider>
      <div className="App">
        <AppRouter />
      </div>
    </PreferencesProvider>
  );
}

export default App;