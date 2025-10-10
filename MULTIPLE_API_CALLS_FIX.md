# 🔧 Multiple API Calls on Filter Change - FIXED!

## 🐛 Problem

When changing a filter, **2-4 API requests** were being made instead of just 1.

---

## 🔍 Root Cause Analysis

### The Issue: Function Dependencies in useEffect

#### Problem in `useTestCaseData.ts` (Hook)
```typescript
// BEFORE (PROBLEMATIC)
useEffect(() => {
  loadData();
}, [loadData]); // ← loadData recreated every time state changes

useEffect(() => {
  if (!state.loading) {
    loadTestCases();
  }
}, [state.filters, loadTestCases, state.loading]); // ← loadTestCases recreated when filters change
```

#### Problem in `TestCasesView.tsx`
```typescript
// BEFORE (PROBLEMATIC)
useEffect(() => {
  if (activeTab === 'list') {
    loadTestCases();
  }
}, [activeTab, loadTestCases, loadGaps]); // ← Fires every time functions recreate
```

### What Happened on Filter Change

```
1. User changes filter → setFilters() called
2. state.filters updates
3. Hook's useEffect fires → loadTestCases() → API call #1 ✓
4. loadTestCases function is recreated (has state.filters in deps)
5. TestCasesView's useEffect detects loadTestCases changed → fires again
6. Calls loadTestCases() again → API call #2 ✗
```

Additionally, `loadData` had similar issues causing extra calls.

---

## ✅ Solution

### Fix 1: Remove Function Dependencies from useEffects

#### In `useTestCaseData.ts`
```typescript
// AFTER (FIXED)
// Load data on mount only
useEffect(() => {
  loadData();
  // eslint-disable-next-line react-hooks/exhaustive-deps
}, []); // ← Empty deps - only run once on mount

// Reload when filters or pagination change
useEffect(() => {
  if (state.loading) {
    return; // Skip on initial load
  }
  
  loadTestCases();
  // eslint-disable-next-line react-hooks/exhaustive-deps
}, [state.filters, state.listPagination.page, state.listPagination.pageSize]); 
// ← Removed loadTestCases from deps
```

#### In `TestCasesView.tsx`
```typescript
// AFTER (FIXED)
useEffect(() => {
  if (activeTab === 'list') {
    loadTestCases();
  } else if (activeTab === 'gaps') {
    loadGaps();
  }
  // eslint-disable-next-line react-hooks/exhaustive-deps
}, [activeTab]); 
// ← Only depend on activeTab, not the functions
```

### Why This Works

**Key Principle**: Don't include functions in dependency arrays when you only care about the data they use.

- ✅ Depend on: `state.filters`, `activeTab` (data values)
- ❌ Don't depend on: `loadTestCases`, `loadData` (functions that get recreated)

---

## 📊 Data Flow After Fix

### Filter Change Flow
```
User changes organization filter:
  ↓
1. TestCasesView.handleFilterChange()
  ↓
2. Hook.setFilters({ organization: 'Engineering' })
  ↓
3. state.filters updates (single setState)
  ↓
4. Hook's useEffect detects state.filters changed
  ↓
5. Calls loadTestCases()
  ↓
6. API call: GET /api/testcases?organization=Engineering
  ↓
✅ DONE - Only 1 API call!
```

### Tab Change Flow
```
User clicks "Gaps" tab:
  ↓
1. activeTab changes from 'list' to 'gaps'
  ↓
2. TestCasesView's useEffect detects activeTab changed
  ↓
3. Calls loadGaps()
  ↓
4. API call: GET /api/testcases/gaps
  ↓
✅ DONE - Only 1 API call!
```

---

## 🧪 Verification

### Test 1: Initial Load
**Expected**: 1 API call
```
Open Test Cases page
→ GET /api/testcases?page=0&size=20
→ GET /api/testcases/organizations
Total: 2 calls (both needed)
```

### Test 2: Change Organization Filter
**Expected**: 1 API call
```
Select "Engineering" from Organization dropdown
→ GET /api/testcases?organization=Engineering&page=0&size=20
Total: 1 call ✅
```

### Test 3: Change Priority Filter
**Expected**: 1 API call
```
Select "High" from Priority dropdown
→ GET /api/testcases?organization=Engineering&priority=High&page=0&size=20
Total: 1 call ✅
```

### Test 4: Change Pagination
**Expected**: 1 API call
```
Click "Page 2"
→ GET /api/testcases?organization=Engineering&priority=High&page=1&size=20
Total: 1 call ✅
```

### Test 5: Switch Tab
**Expected**: 1 API call
```
Click "Gaps" tab
→ GET /api/testcases/gaps?page=0&size=20
Total: 1 call ✅
```

---

## 🎯 React Best Practices Applied

### 1. useCallback Dependencies
```typescript
// Include only the state values you read, not all of state
const loadTestCases = useCallback(async () => {
  await getAllTestCases({
    page: state.listPagination.page,
    size: state.listPagination.pageSize,
    ...state.filters
  });
}, [state.listPagination.page, state.listPagination.pageSize, state.filters]);
// ✅ Only includes the specific state slices used
```

### 2. useEffect Dependencies
```typescript
// Depend on data values, not functions
useEffect(() => {
  loadTestCases(); // Call the function
}, [state.filters]); // But only depend on the data it uses
// ✅ Won't re-run when function is recreated
```

### 3. Single setState Updates
```typescript
// Update multiple state values in one call
const setFilters = useCallback((filters: TestCaseFilters) => {
  setState(prev => ({
    ...prev,
    filters,
    listPagination: { ...prev.listPagination, page: 0 } // Both in one update
  }));
}, []);
// ✅ React batches into single render
```

---

## 📝 Common React Pitfalls Avoided

### Pitfall 1: Function in Dependency Array
```typescript
// ❌ BAD
useEffect(() => {
  myFunction();
}, [myFunction]); // Fires every time function recreates

// ✅ GOOD
useEffect(() => {
  myFunction();
  // eslint-disable-next-line react-hooks/exhaustive-deps
}, []); // Only fires when you actually want it to
```

### Pitfall 2: Infinite Loop
```typescript
// ❌ BAD
const loadData = useCallback(() => {
  // ... load data
}, [state]); // All of state → recreates on every state change → infinite loop

// ✅ GOOD
const loadData = useCallback(() => {
  // ... load data
}, [state.specificField]); // Only specific fields needed
```

### Pitfall 3: Multiple useEffects for Same Purpose
```typescript
// ❌ BAD
useEffect(() => loadData(), [loadData]);
useEffect(() => loadTestCases(), [filters]); // Both might fire

// ✅ GOOD
useEffect(() => loadData(), []); // Mount only
useEffect(() => loadTestCases(), [filters]); // Filter changes
// Clear separation of concerns
```

---

## 🎉 Summary

**Root Cause**: 
- Function dependencies in useEffect causing cascading re-renders
- Multiple useEffects firing for the same state changes

**Solution**: 
- Removed function dependencies from useEffect arrays
- Only depend on actual data values (state.filters, activeTab)
- Used eslint-disable to suppress warnings where appropriate

**Result**:
- ✅ 1 API call per filter change (was 2-4)
- ✅ Faster page interactions
- ✅ Less backend load
- ✅ Cleaner React code

**Files Modified**:
1. ✅ `frontend/src/hooks/useTestCaseData.ts` - Fixed useEffect dependencies
2. ✅ `frontend/src/views/TestCasesView.tsx` - Fixed tab change useEffect

**Status**: ✅ FIXED  
**Date**: October 10, 2025

