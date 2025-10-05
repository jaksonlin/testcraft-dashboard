# TestCraft Dashboard Frontend

## Overview

This is the React TypeScript frontend for the TestCraft Dashboard - a modern, interactive web application for monitoring Git repository test analytics.

## Features

### 🎨 **Modern UI/UX**
- **Responsive Design**: Works on desktop, tablet, and mobile devices
- **Clean Interface**: Modern, professional dashboard design
- **Interactive Charts**: Real-time data visualization with Recharts
- **Dark/Light Theme**: Automatic theme switching based on system preferences

### 📊 **Dashboard Components**
- **Overview Statistics**: Key metrics at a glance
- **Team Performance**: Interactive bar charts showing team metrics
- **Coverage Analysis**: Pie charts for test method coverage
- **Repository Details**: Comprehensive repository information table
- **Real-time Updates**: Auto-refresh every 30 seconds

### 🔧 **Functionality**
- **Manual Scan Trigger**: One-click repository scanning
- **Scan Status Monitoring**: Real-time scan progress tracking
- **Error Handling**: Graceful error handling with user-friendly messages
- **Loading States**: Smooth loading animations and indicators

## Technology Stack

- **React 18**: Modern React with hooks and functional components
- **TypeScript**: Type-safe development
- **Vite**: Fast build tool and development server
- **Tailwind CSS**: Utility-first CSS framework
- **Recharts**: Beautiful, composable charts for React
- **Axios**: HTTP client for API communication
- **Lucide React**: Beautiful, customizable SVG icons

## Project Structure

```
frontend/
├── public/                 # Static assets
├── src/
│   ├── components/         # React components
│   │   └── Dashboard.tsx   # Main dashboard component
│   ├── lib/               # Utility libraries
│   │   └── api.ts         # API client and types
│   ├── App.tsx            # Main app component
│   ├── App.css            # App-specific styles
│   ├── index.css          # Global styles and Tailwind
│   └── main.tsx           # Application entry point
├── tailwind.config.js     # Tailwind CSS configuration
├── postcss.config.js      # PostCSS configuration
├── package.json           # Dependencies and scripts
└── README.md              # This file
```

## Getting Started

### Prerequisites

- **Node.js**: Version 20.19.0 or higher (recommended: 22.19.0)
- **npm**: Package manager
- **Backend API**: Spring Boot backend running on port 8090

### Installation

1. **Navigate to frontend directory**:
   ```bash
   cd frontend
   ```

2. **Install dependencies**:
   ```bash
   npm install
   ```

3. **Start development server**:
   ```bash
   npm run dev
   ```

4. **Open in browser**:
   ```
   http://localhost:5173
   ```

### Available Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint

## API Integration

The frontend communicates with the Spring Boot backend through a comprehensive API client:

### API Client Features

- **Type Safety**: Full TypeScript interfaces for all API responses
- **Error Handling**: Automatic error handling with user feedback
- **Request/Response Logging**: Development-time API call logging
- **Timeout Handling**: 10-second timeout for API requests

### Available Endpoints

- **Dashboard Data**: Overview, teams, repositories, test methods
- **Scan Management**: Trigger scans, check status, view history
- **Debug Information**: Database status, table counts

## Styling

### Tailwind CSS Configuration

The project uses a custom Tailwind configuration with:

- **Custom Color Palette**: Primary, success, warning, danger colors
- **Component Classes**: Pre-built button and card components
- **Responsive Design**: Mobile-first responsive utilities

### Component Styling

```css
/* Button variants */
.btn-primary     /* Primary action buttons */
.btn-secondary   /* Secondary actions */
.btn-success     /* Success actions */
.btn-danger      /* Danger actions */

/* Card components */
.card           /* Standard card container */
.card-header    /* Card header with border */
```

## Development

### Adding New Components

1. Create component in `src/components/`
2. Use TypeScript interfaces for props
3. Apply Tailwind CSS classes for styling
4. Import and use in main Dashboard component

### API Integration

1. Add new types to `src/lib/api.ts`
2. Create API methods in the `api` object
3. Use in components with proper error handling

### Styling Guidelines

- Use Tailwind utility classes
- Follow mobile-first responsive design
- Maintain consistent spacing and colors
- Use semantic color names (primary, success, etc.)

## Build and Deployment

### Production Build

```bash
npm run build
```

This creates optimized production files in the `dist/` directory.

### Environment Configuration

The API base URL is configured in `src/lib/api.ts`:

```typescript
const API_BASE_URL = 'http://localhost:8090/api';
```

For production, update this to your backend URL.

## Browser Support

- **Chrome**: 90+
- **Firefox**: 88+
- **Safari**: 14+
- **Edge**: 90+

## Performance

- **Bundle Size**: Optimized with Vite
- **Lazy Loading**: Components loaded on demand
- **Caching**: Automatic browser caching for static assets
- **Tree Shaking**: Unused code eliminated in production

## Troubleshooting

### Common Issues

1. **API Connection Errors**:
   - Ensure backend is running on port 8090
   - Check CORS configuration
   - Verify API endpoints

2. **Build Errors**:
   - Clear node_modules and reinstall
   - Check Node.js version compatibility
   - Verify TypeScript configuration

3. **Styling Issues**:
   - Ensure Tailwind CSS is properly configured
   - Check PostCSS configuration
   - Verify CSS import order

### Development Tips

- Use browser developer tools for debugging
- Check console for API call logs
- Use React Developer Tools extension
- Monitor network tab for API requests

## Contributing

1. Follow TypeScript best practices
2. Use meaningful component and variable names
3. Add proper error handling
4. Write responsive, accessible components
5. Test on multiple browsers and devices

## License

This project is part of the TestCraft Dashboard system.