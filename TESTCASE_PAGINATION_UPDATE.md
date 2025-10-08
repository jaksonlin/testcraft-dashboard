# Test Case Pagination Update

## Summary
Updated the test case frontend pagination to align with the repository page pagination design, ensuring consistency across the application.

## Changes Made

### 1. New Component: TestCasesHeader
**File**: `frontend/src/components/testcases/TestCasesHeader.tsx`

Created a new header component that includes:
- Title and description
- DataControls component for page size selection
- Optional sorting controls (for future use)

### 2. Updated TestCasesView
**File**: `frontend/src/views/TestCasesView.tsx`

#### State Management
- Separated pagination state for "list" and "gaps" tabs:
  - List tab: `listPage`, `listPageSize`, `listTotalPages`
  - Gaps tab: `gapsPage`, `gapsPageSize`, `gapsTotalPages`
- Removed old `page`, `size`, `totalCases`, `totalGaps` state

#### Data Loading
- Created separate `loadTestCases()` and `loadGaps()` callbacks with proper pagination dependencies
- Updated `loadData()` to calculate `totalPages` using `Math.ceil(total / pageSize)`
- Added useEffect hooks to reload data when pagination changes

#### UI Components
- Replaced header section with `TestCasesHeader` component
- Integrated `DataControls` for page size selection
- Replaced inline pagination controls with shared `Pagination` component
- Both "list" and "gaps" tabs now use the consistent "Page X of Y" format with Previous/Next buttons

## Pagination Design

### Header Controls
```tsx
<TestCasesHeader
  pageSize={activeTab === 'gaps' ? gapsPageSize : listPageSize}
  onPageSizeChange={(size) => {
    if (activeTab === 'gaps') {
      setGapsPageSize(size);
      setGapsPage(0);
    } else {
      setListPageSize(size);
      setListPage(0);
    }
  }}
/>
```

### Footer Pagination
```tsx
<Pagination
  currentPage={listPage}
  totalPages={listTotalPages}
  onPageChange={setListPage}
/>
```

### Page Size Options
- 1 per page
- 10 per page
- 20 per page (default)
- 50 per page
- 100 per page

## Benefits

1. **Consistency**: Test case pagination now matches repository pagination design
2. **Separation**: List and gaps tabs have independent pagination state
3. **Reusability**: Uses shared components (DataControls, Pagination)
4. **User Experience**: 
   - Page size control in header
   - Clear "Page X of Y" display
   - Disabled state for prev/next buttons at boundaries
5. **Performance**: Only loads data when page or page size changes

## Backend API
The backend already supports pagination correctly:

### GET /api/testcases
```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "total": 100
}
```

### GET /api/testcases/gaps
```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "total": 15
}
```

The frontend calculates `totalPages = Math.ceil(total / size)` for the pagination component.

## Testing
To test the changes:
1. Navigate to Test Cases view
2. Verify page size dropdown in header works for both tabs
3. Verify pagination controls show "Page X of Y" format
4. Verify Previous/Next buttons work correctly
5. Verify changing page size resets to page 1
6. Verify both "All Test Cases" and "Automation Gaps" tabs have independent pagination

## Files Modified
- `frontend/src/views/TestCasesView.tsx` - Main view with updated pagination logic
- `frontend/src/components/testcases/TestCasesHeader.tsx` - New header component (created)

## Files Used (Existing)
- `frontend/src/components/shared/Pagination.tsx` - Shared pagination component
- `frontend/src/components/shared/DataControls.tsx` - Shared data controls component

