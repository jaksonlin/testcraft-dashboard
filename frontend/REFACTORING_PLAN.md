# Dashboard Component Refactoring Plan

## Current Issues
- Single component with 662 lines
- Multiple responsibilities mixed together
- Hard to maintain and test
- Poor reusability

## Proposed Structure

```
frontend/src/
├── components/
│   ├── Dashboard/                    # Main dashboard container
│   │   ├── index.tsx                # Main Dashboard component (orchestrator)
│   │   ├── DashboardHeader.tsx      # Header with title, scan info, controls
│   │   ├── DashboardContent.tsx     # Main content area
│   │   └── DashboardLayout.tsx      # Layout wrapper
│   │
│   ├── dashboard/                   # Dashboard-specific components
│   │   ├── StatCard.tsx            # Statistics card component
│   │   ├── StatsOverview.tsx       # Stats grid section
│   │   ├── ChartsSection.tsx       # Charts container
│   │   ├── TeamPerformanceChart.tsx # Team performance bar chart
│   │   ├── CoverageChart.tsx       # Coverage pie chart
│   │   └── RepositoriesTable.tsx   # Top repositories table
│   │
│   ├── config/                     # Configuration components
│   │   ├── ScanConfigModal.tsx     # Configuration modal
│   │   ├── ConfigForm.tsx          # Configuration form
│   │   └── ConfigField.tsx         # Individual form field
│   │
│   └── shared/                     # Shared components
│       ├── LoadingSpinner.tsx      # Loading states
│       ├── ErrorMessage.tsx        # Error display
│       └── Modal.tsx              # Reusable modal wrapper
│
├── hooks/                          # Custom hooks
│   ├── useDashboardData.ts         # Dashboard data fetching
│   ├── useScanConfig.ts           # Scan configuration logic
│   ├── useAutoRefresh.ts          # Auto-refresh logic
│   └── useModal.ts                # Modal state management
│
├── services/                       # Business logic
│   ├── dashboardService.ts         # Dashboard data operations
│   └── configService.ts           # Configuration operations
│
└── types/                         # Type definitions
    ├── dashboard.ts               # Dashboard-specific types
    └── config.ts                 # Configuration types
```

## Benefits of Refactoring

### 1. **Separation of Concerns**
- Each component has a single responsibility
- Business logic separated from UI
- Easier to understand and maintain

### 2. **Reusability**
- Components can be reused across different pages
- Shared components for common UI patterns
- Configurable and flexible components

### 3. **Testability**
- Smaller components are easier to test
- Business logic can be tested independently
- Mock dependencies easily

### 4. **Performance**
- Smaller components re-render less frequently
- Better code splitting opportunities
- Lazy loading possibilities

### 5. **Developer Experience**
- Easier to find and modify specific functionality
- Better IDE support and autocomplete
- Reduced merge conflicts

## Implementation Steps

### Phase 1: Extract Components
1. Create StatCard component
2. Extract modal components
3. Create chart components
4. Extract header component

### Phase 2: Extract Hooks
1. Create useDashboardData hook
2. Create useScanConfig hook
3. Create useAutoRefresh hook

### Phase 3: Extract Services
1. Create dashboard service
2. Create config service
3. Move business logic

### Phase 4: Cleanup
1. Update imports
2. Remove unused code
3. Add proper TypeScript types
4. Add tests

## File Size Reduction
- Current: 662 lines in single file
- After refactoring: ~50-100 lines per file
- Total files: ~15-20 smaller, focused files
