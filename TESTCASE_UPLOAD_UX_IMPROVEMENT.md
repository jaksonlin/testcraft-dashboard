# Test Case Upload UX Improvement

## Summary
Improved the test case upload UX by moving upload functionality from a dedicated tab to a header button with modal, following the design pattern used in the repository page.

## Changes Made

### 1. New Component: TestCaseUploadModal
**File**: `frontend/src/components/testcases/TestCaseUploadModal.tsx`

Created a modal wrapper for the `TestCaseUploadWizard`:
- Fixed backdrop overlay
- Centered modal with max-width (5xl)
- Close button in header
- Scrollable content for long wizards
- Auto-closes on upload completion

```tsx
<TestCaseUploadModal
  isOpen={isUploadModalOpen}
  onClose={() => setIsUploadModalOpen(false)}
  onComplete={handleUploadComplete}
/>
```

### 2. Updated TestCasesHeader
**File**: `frontend/src/components/testcases/TestCasesHeader.tsx`

Added Upload button to header:
- New `onUploadClick` prop
- Upload button with icon (matches repo page pattern)
- Positioned alongside DataControls
- Uses primary button styling

```tsx
<button onClick={onUploadClick} className="btn btn-primary flex items-center">
  <Upload className="h-4 w-4 mr-2" />
  Upload Test Cases
</button>
```

### 3. Updated TestCasesView
**File**: `frontend/src/views/TestCasesView.tsx`

#### Removed Upload Tab
- Changed `TabType` from `'upload' | 'list' | 'coverage' | 'gaps'` to `'list' | 'coverage' | 'gaps'`
- Removed "Upload Test Cases" tab from navigation
- Removed upload tab content section
- Cleaner 3-tab navigation (was 4 tabs)

#### Added Modal State
- Added `isUploadModalOpen` state
- Connected header button to open modal
- Modal renders at end of component (like detail modal)
- Upload completion reloads data and closes modal

#### Updated Imports
- Removed `TestCaseUploadWizard` direct import
- Added `TestCaseUploadModal` import
- Removed `Upload` icon from lucide-react (no longer needed)

## Benefits

### ✅ Better UX
- Upload accessible from any tab
- No need to switch to dedicated upload tab
- Modal provides clear focus on upload task
- Follows modern web app patterns

### ✅ Consistent Design
- Matches repository page pattern (action buttons in header)
- Upload, Export, Columns buttons all in header
- Follows the principle: "Tabs for views, buttons for actions"

### ✅ Cleaner Navigation
- Reduced from 4 tabs to 3 tabs
- Less cognitive load for users
- Upload is clearly an action, not a view

### ✅ Improved Workflow
- Users can upload from any tab without losing context
- After upload, stays on current tab with refreshed data
- Modal can be closed/cancelled easily

## Design Pattern

### Before
```
Header: [Title]
Tabs: [Upload] [List] [Coverage] [Gaps]
Content: Upload wizard takes full content area
```

### After
```
Header: [Title] [Page Size] [Upload Button]
Tabs: [List] [Coverage] [Gaps]
Content: Data views
Modal: Upload wizard (when button clicked)
```

## User Flow

1. User clicks "Upload Test Cases" button in header
2. Modal opens with upload wizard
3. User completes upload (4 steps: upload → mapping → preview → complete)
4. On completion:
   - Data reloads automatically
   - Modal closes
   - User stays on current tab
5. User can also cancel by clicking X or outside modal

## Technical Details

### Modal Implementation
- Uses fixed positioning with backdrop
- Z-index: 50 (above other content)
- Backdrop: semi-transparent black
- Content: white with shadow
- Max height: 90vh with scroll
- Responsive width with max-width

### State Management
- `isUploadModalOpen`: Controls modal visibility
- Modal is conditionally rendered (not just hidden)
- Clean unmount when closed

### Integration
- Header button triggers `setIsUploadModalOpen(true)`
- Modal handles its own close logic
- `onComplete` prop chains to reload data

## Files Modified
1. `frontend/src/components/testcases/TestCaseUploadModal.tsx` - New modal component
2. `frontend/src/components/testcases/TestCasesHeader.tsx` - Added upload button
3. `frontend/src/views/TestCasesView.tsx` - Removed upload tab, added modal

## Testing Checklist
- ✅ Click "Upload Test Cases" button opens modal
- ✅ Modal displays upload wizard correctly
- ✅ Can navigate through all wizard steps
- ✅ Upload completes successfully
- ✅ Data reloads after upload
- ✅ Modal closes after completion
- ✅ Can cancel upload by clicking X
- ✅ Can cancel by clicking outside modal
- ✅ Upload button accessible from all tabs
- ✅ No console errors
- ✅ No linting errors

## Future Enhancements
- Add Export button to header (like repositories page)
- Add keyboard shortcuts (Escape to close modal)
- Add upload progress indicator in header
- Add drag-and-drop zone in modal
- Add recent uploads list

