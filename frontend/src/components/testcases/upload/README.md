# Test Case Upload Wizard - Refactored Structure

This directory contains the refactored Test Case Upload Wizard, which has been split from a monolithic 1075-line file into a well-organized, modular architecture.

## 📁 File Structure

```
upload/
├── README.md                      # This file
├── index.ts                       # Public API exports
├── TestCaseUploadWizard.tsx      # Main orchestrator component (90 lines)
├── useTestCaseUpload.ts          # Business logic hook (218 lines)
├── types.ts                       # TypeScript type definitions (57 lines)
├── constants.ts                   # Configuration constants (44 lines)
├── utils.ts                       # Helper functions (72 lines)
├── ProgressSteps.tsx             # Progress indicator component (44 lines)
├── UploadStep.tsx                # Step 1: File upload (84 lines)
├── MappingStep.tsx               # Step 2: Column mapping (366 lines)
├── PreviewStep.tsx               # Step 3: Data preview (230 lines)
└── CompleteStep.tsx              # Step 4: Results display (139 lines)
```

## 🎯 Refactoring Benefits

### 1. **Separation of Concerns**
- **Business Logic**: Moved to `useTestCaseUpload.ts` hook
- **UI Components**: Each step is a separate component
- **Types**: Centralized in `types.ts`
- **Constants**: Grouped in `constants.ts`
- **Utilities**: Helper functions in `utils.ts`

### 2. **Improved Maintainability**
- Each file has a single, clear responsibility
- Easy to locate and modify specific functionality
- Reduced cognitive load when reading code

### 3. **Enhanced Reusability**
- Step components can be used independently
- Hook can be reused in different contexts
- Utilities available for other components

### 4. **Better Testability**
- Each module can be tested in isolation
- Mock dependencies more easily
- Clearer test structure

### 5. **Scalability**
- Easy to add new steps or features
- Simple to modify existing steps
- Clear extension points

## 🔧 Architecture Overview

### Main Component (`TestCaseUploadWizard.tsx`)

The orchestrator component that:
- Uses the `useTestCaseUpload` hook for state and logic
- Renders the appropriate step component based on current state
- Passes props to child components
- **Size**: 90 lines (down from 1075)

### Custom Hook (`useTestCaseUpload.ts`)

Encapsulates all business logic:
- File upload handling
- Excel preview and validation
- Column mapping management
- Row configuration
- Import execution
- State management for the entire wizard flow

**Key Functions**:
- `handleFileSelect`: Process file upload and get preview
- `handleMappingChange`: Update column mappings and validate
- `handleRowChange`: Update header/data row settings
- `handleAdvanceToPreview`: Refresh preview before import
- `handleImport`: Execute the import operation
- `handleComplete`: Handle wizard completion

### Step Components

Each step is a self-contained component:

#### 1. **UploadStep** (84 lines)
- Drag & drop file upload
- File input selector
- Informational guidance

#### 2. **MappingStep** (366 lines)
- Excel preview table
- Column mapping interface
- Row settings configuration
- Validation feedback
- Sub-components: `ValidationAlert`, `PreviewTable`, `RowSettings`, `ColumnMappings`

#### 3. **PreviewStep** (230 lines)
- Mapped data preview
- Import summary
- Debug information
- Sub-components: `DebugInfo`, `PreviewTable`

#### 4. **CompleteStep** (139 lines)
- Success/failure display
- Import statistics
- Error messages and suggestions
- Next steps guidance
- Sub-components: `SuccessContent`, `ErrorContent`

### Supporting Modules

#### Types (`types.ts`)
- `WizardStep`: Step identifier type
- Component prop interfaces
- Re-exports API types

#### Constants (`constants.ts`)
- `SYSTEM_FIELDS`: Available mapping fields
- `REQUIRED_FIELDS`: Required field identifiers
- `WIZARD_STEPS`: Step configuration
- `CONFIDENCE_THRESHOLDS`: Confidence level definitions
- `PREVIEW_LIMITS`: Display limits

#### Utilities (`utils.ts`)
Helper functions:
- `getConfidenceColor`: Get color for confidence level
- `isStepCompleted`: Check if step is completed
- `isStepCurrent`: Check if step is current
- `getStepButtonClass`: Get styling for step button
- `getProgressBarClass`: Get styling for progress bar
- `calculateEstimatedImportCount`: Calculate import count

Note: `getConfidenceIcon` is now internal to `MappingStep.tsx` since it returns JSX.

## 📦 Public API

The `index.ts` file exports everything needed to use the module:

```typescript
// Main component
import { TestCaseUploadWizard } from './upload';

// Individual steps (if needed)
import { UploadStep, MappingStep, PreviewStep, CompleteStep } from './upload';

// Hook (for custom implementations)
import { useTestCaseUpload } from './upload';

// Types (for TypeScript)
import type { WizardStep, TestCaseUploadWizardProps } from './upload';

// Constants (for configuration)
import { SYSTEM_FIELDS, REQUIRED_FIELDS } from './upload';

// Utilities (for helpers)
import { getConfidenceColor, calculateEstimatedImportCount } from './upload';
```

## 🔄 Migration Guide

### Before (Old Import)
```typescript
import { TestCaseUploadWizard } from './TestCaseUploadWizard';
```

### After (New Import)
```typescript
import { TestCaseUploadWizard } from './upload';
```

The component API remains **100% backward compatible**. No changes needed in parent components!

## 🎨 Code Quality Improvements

1. **TypeScript**: Full type safety with explicit interfaces
2. **Comments**: Comprehensive JSDoc comments
3. **Naming**: Clear, descriptive variable and function names
4. **Structure**: Logical file organization
5. **DRY**: No code duplication
6. **SOLID**: Single Responsibility Principle applied

## 🚀 Performance

No performance impact - same runtime behavior with better:
- Code splitting potential
- Tree shaking opportunities
- Bundle size optimization

## 🧪 Testing Strategy

Each module can now be tested independently:

```typescript
// Test the hook
describe('useTestCaseUpload', () => {
  it('should handle file selection', async () => {
    // Test hook in isolation
  });
});

// Test individual steps
describe('MappingStep', () => {
  it('should render column mappings', () => {
    // Test component with mock props
  });
});

// Test utilities
describe('getConfidenceColor', () => {
  it('should return correct color for confidence level', () => {
    // Test pure function
  });
});
```

## 📊 Statistics

- **Original**: 1 file, 1075 lines
- **Refactored**: 12 files, ~1344 lines total (includes documentation)
- **Average file size**: ~112 lines
- **Largest file**: MappingStep.tsx (366 lines)
- **Smallest file**: constants.ts (44 lines)

## 🔮 Future Enhancements

The refactored structure makes it easy to:
1. Add new wizard steps
2. Implement alternative upload methods
3. Create different wizard themes
4. Add step-specific tests
5. Build wizard templates
6. Extract reusable sub-components

## 👥 Contributing

When modifying this module:
1. Keep components focused on single responsibility
2. Update types when adding new props
3. Add constants for magic numbers/strings
4. Extract reusable logic to utilities
5. Update this README for major changes

## 📝 Notes

- All components are React functional components with TypeScript
- Uses Tailwind CSS for styling
- Integrates with `testCaseApi` for backend communication
- Follows project's Spring-style dependency injection pattern (via props)
