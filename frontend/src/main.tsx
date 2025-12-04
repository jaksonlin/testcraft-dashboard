import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
// Import Monaco environment (workers will be configured when editor mounts)
import './lib/monacoEnvironment'
import App from './App.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
