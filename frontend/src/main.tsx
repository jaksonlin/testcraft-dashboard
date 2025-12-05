import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
// Import and initialize Monaco environment before app renders
// This ensures Monaco is configured and pre-loaded for offline use
import { ensureMonacoEnvironment } from './lib/monacoEnvironment'
import App from './App.tsx'

// Initialize Monaco environment immediately (non-blocking)
// Loader is already configured at module load time, this sets up workers and pre-loads Monaco
ensureMonacoEnvironment();

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
