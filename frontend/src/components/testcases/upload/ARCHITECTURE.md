# Test Case Upload Wizard - Architecture

## 📐 Component Hierarchy

```
TestCaseUploadWizard (Main Orchestrator)
│
├─ useTestCaseUpload (Custom Hook)
│  ├─ State Management
│  ├─ API Calls (previewExcelFile, validateMappings, importTestCases)
│  └─ Business Logic
│
├─ ProgressSteps
│  └─ Visual progress indicator
│
└─ Step Components (Conditional Rendering)
   │
   ├─ UploadStep
   │  ├─ Drag & Drop Area
   │  └─ Info Box
   │
   ├─ MappingStep
   │  ├─ ValidationAlert
   │  ├─ PreviewTable
   │  ├─ RowSettings
   │  └─ ColumnMappings
   │
   ├─ PreviewStep
   │  ├─ Import Info Banner
   │  ├─ DebugInfo (collapsible)
   │  ├─ PreviewTable
   │  └─ Action Buttons
   │
   └─ CompleteStep
      ├─ SuccessContent (if success)
      │  ├─ Statistics Cards
      │  └─ Next Steps Guide
      └─ ErrorContent (if failed)
         ├─ Error Messages
         └─ Suggestions
```

## 🔄 Data Flow

```
User Action → Component Event → Hook Handler → API Call → State Update → Re-render
```

### Example: File Upload Flow

```
1. User drops file
   ↓
2. UploadStep.onFileSelect()
   ↓
3. useTestCaseUpload.handleFileSelect()
   ↓
4. API: previewExcelFile()
   ↓
5. State: setPreview(), setMappings(), etc.
   ↓
6. Re-render: Navigate to MappingStep
```

## 🗂️ Module Dependencies

```
index.ts (Public API)
├─ exports → TestCaseUploadWizard
├─ exports → All Step Components
├─ exports → useTestCaseUpload
├─ exports → Types
├─ exports → Constants
└─ exports → Utils

TestCaseUploadWizard.tsx
├─ imports → useTestCaseUpload
├─ imports → All Step Components
└─ imports → Types

useTestCaseUpload.ts
├─ imports → API functions (testCaseApi)
└─ imports → Types

Step Components
├─ imports → Types
├─ imports → Constants
├─ imports → Utils
└─ imports → Lucide Icons

Utils & Constants
└─ No internal dependencies (leaf modules)
```

## 🔐 Type Safety

```typescript
// Type flow through the application
ExcelPreviewResponse (API)
  ↓
useTestCaseUpload (Hook)
  ↓
TestCaseUploadWizard (Main)
  ↓
MappingStepProps (Step Component)
  ↓
MappingStep (Rendered)
```

## 🎯 Responsibility Matrix

| Module | Responsibility | Dependencies |
|--------|---------------|--------------|
| **TestCaseUploadWizard** | Orchestrate wizard flow | Hook, Steps |
| **useTestCaseUpload** | Business logic & state | API |
| **UploadStep** | File selection UI | Types, Icons |
| **MappingStep** | Column mapping UI | Types, Constants, Utils |
| **PreviewStep** | Data preview UI | Types, Utils |
| **CompleteStep** | Results display | Types, Icons |
| **ProgressSteps** | Progress indicator | Types, Utils |
| **types.ts** | Type definitions | API types |
| **constants.ts** | Configuration | None |
| **utils.ts** | Helper functions | Constants |

## 🔄 State Management

The `useTestCaseUpload` hook manages these state variables:

```typescript
{
  currentStep: WizardStep           // Current wizard step
  file: File | null                 // Selected Excel file
  preview: ExcelPreviewResponse     // Preview data from API
  mappings: Record<string, string>  // Column mappings
  headerRow: number                 // Header row index
  dataStartRow: number              // Data start row index
  isValidMapping: boolean           // Validation status
  missingFields: string[]           // Missing required fields
  suggestions: string[]             // Validation suggestions
  importing: boolean                // Import in progress
  importResult: ImportResponse      // Import result
}
```

## 🚦 Wizard Flow

```
┌─────────────┐
│   UPLOAD    │ handleFileSelect()
│   (Step 1)  │────────────────────┐
└─────────────┘                    │
                                   ↓
                          ┌─────────────────┐
                          │   MAPPING       │ handleMappingChange()
                          │   (Step 2)      │ handleRowChange()
                          └─────────────────┘
                                   │
                                   ↓ handleAdvanceToPreview()
                          ┌─────────────────┐
                          │   PREVIEW       │
                          │   (Step 3)      │
                          └─────────────────┘
                                   │
                                   ↓ handleImport()
                          ┌─────────────────┐
                          │   COMPLETE      │ handleComplete()
                          │   (Step 4)      │─────────────────┐
                          └─────────────────┘                 │
                                                              ↓
                                                        onComplete()
                                                        (Parent callback)
```

## 📦 Bundle Size Optimization

The modular structure enables:

### Code Splitting
```typescript
// Lazy load steps if needed
const MappingStep = lazy(() => import('./MappingStep'));
const PreviewStep = lazy(() => import('./PreviewStep'));
```

### Tree Shaking
```typescript
// Only import what you need
import { getConfidenceColor } from './upload';
// Other utils won't be included in bundle
```

## 🧪 Testing Strategy

### Unit Tests
```typescript
// Test pure functions
utils.test.ts
  - getConfidenceColor()
  - calculateEstimatedImportCount()

// Test hook
useTestCaseUpload.test.ts
  - handleFileSelect()
  - handleMappingChange()
  - handleImport()
```

### Component Tests
```typescript
// Test UI rendering
UploadStep.test.tsx
  - Renders file input
  - Handles drag and drop
  - Calls onFileSelect

MappingStep.test.tsx
  - Renders mappings
  - Validates required fields
  - Updates on change
```

### Integration Tests
```typescript
// Test wizard flow
TestCaseUploadWizard.test.tsx
  - Complete wizard flow
  - Step transitions
  - API integration
```

## 🎨 Design Patterns Used

1. **Custom Hook Pattern** - `useTestCaseUpload`
   - Encapsulates stateful logic
   - Reusable across components

2. **Composition Pattern** - Step components
   - Build complex UI from simple parts
   - Easy to modify individual pieces

3. **Container/Presenter Pattern**
   - Container: `TestCaseUploadWizard` (logic)
   - Presenters: Step components (UI)

4. **Factory Pattern** - Step rendering
   - Dynamic component rendering based on state

5. **Single Responsibility** - Each module
   - One reason to change

## 🔧 Extension Points

### Adding a New Step

```typescript
// 1. Add step to types
type WizardStep = 'upload' | 'mapping' | 'preview' | 'NEW_STEP' | 'complete';

// 2. Create component
export const NewStep: React.FC<NewStepProps> = (props) => { /* ... */ };

// 3. Add to wizard
{currentStep === 'NEW_STEP' && <NewStep {...props} />}

// 4. Update hook logic
const handleNewStepAction = () => { /* ... */ };
```

### Adding a New Field

```typescript
// 1. Add to constants
export const SYSTEM_FIELDS: SystemField[] = [
  // ...
  { value: 'new_field', label: 'New Field', required: false },
];

// 2. That's it! The UI will automatically include it
```

## 🔍 Code Quality Metrics

```
Cyclomatic Complexity: Low
  - Most functions < 5 branches
  - Hook functions < 10 branches

Code Duplication: Minimal
  - Shared logic in utils
  - Constants prevent magic numbers

Test Coverage: (Recommended)
  - Utils: 100%
  - Hook: 80%+
  - Components: 70%+

Type Coverage: 100%
  - All exports typed
  - No 'any' types used
```

## 📈 Performance Considerations

1. **Memoization** - Consider adding for expensive computations
2. **Lazy Loading** - Steps can be code-split
3. **API Caching** - Preview data could be cached
4. **Virtual Scrolling** - For large column lists in MappingStep

## 🎯 Future Improvements

1. ✅ **Done**: Modular architecture
2. 🔄 **Next**: Add unit tests
3. 🔄 **Next**: Add error boundaries
4. 🔄 **Next**: Add loading skeletons
5. 🔄 **Next**: Implement undo/redo
6. 🔄 **Next**: Add keyboard shortcuts
7. 🔄 **Next**: Accessibility improvements (ARIA labels)
