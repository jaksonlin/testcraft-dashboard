# 🔍 Filter API Troubleshooting Guide

## How to Verify Filters are Working

### Step 1: Check Browser Network Tab

1. Open browser DevTools (F12)
2. Go to Network tab
3. Navigate to Test Cases page
4. You should see: `GET http://localhost:8090/api/testcases?page=0&size=20`

5. Change organization filter to "Engineering"
6. You should see NEW request: `GET http://localhost:8090/api/testcases?page=0&size=20&organization=Engineering`

### Step 2: What You Should See

**Initial Load** (no filters):
```
Request URL: http://localhost:8090/api/testcases?page=0&size=20
Query Parameters:
  page: 0
  size: 20
```

**After Selecting Organization Filter**:
```
Request URL: http://localhost:8090/api/testcases?page=0&size=20&organization=Engineering
Query Parameters:
  page: 0
  size: 20
  organization: Engineering  ← NEW!
```

**After Adding Priority Filter**:
```
Request URL: http://localhost:8090/api/testcases?page=0&size=20&organization=Engineering&priority=High
Query Parameters:
  page: 0
  size: 20
  organization: Engineering
  priority: High  ← NEW!
```

---

## 🔧 Data Flow Verification

### Frontend Flow
```javascript
// 1. User changes filter
<select onChange={(e) => handleFilterChange('organization', e.target.value)} />

// 2. TestCaseListTable calls parent callback
onFilterChange({ ...filters, organization: 'Engineering' })

// 3. TestCasesView.handleFilterChange
const backendFilters = {
  organization: 'Engineering',
  priority: newFilters.priority,
  type: newFilters.type
};
setFilters(backendFilters);

// 4. Hook's setFilters updates state
setState(prev => ({ ...prev, filters: backendFilters, listPagination: { ...prev.listPagination, page: 0 } }));

// 5. useEffect detects filters changed
useEffect(() => {
  loadTestCases(); // ← This runs
}, [state.filters, ...]);

// 6. loadTestCases makes API call
const testCasesData = await getAllTestCases({
  page: 0,
  size: 20,
  ...state.filters // ← organization: 'Engineering'
});

// 7. Axios GET request
axios.get('/api/testcases', { 
  params: { page: 0, size: 20, organization: 'Engineering' }
})
```

---

## 🐛 Common Issues

### Issue 1: Filters Not Showing in Network Tab

**Symptom**: API calls don't include filter parameters

**Possible Causes**:
1. Filter conversion not happening
2. setFilters not being called
3. useEffect not triggering

**Debug**:
Add console.logs in `TestCasesView.tsx`:
```typescript
const handleFilterChange = (newFilters: typeof uiFilters) => {
  console.log('1. UI Filters changed:', newFilters);
  setUiFilters(newFilters);
  
  const backendFilters: TestCaseFilters = {};
  if (newFilters.organization) backendFilters.organization = newFilters.organization;
  if (newFilters.priority) backendFilters.priority = newFilters.priority;
  if (newFilters.type) backendFilters.type = newFilters.type;
  
  console.log('2. Backend Filters:', backendFilters);
  setFilters(backendFilters);
};
```

Add console.log in `useTestCaseData.ts`:
```typescript
const loadTestCases = useCallback(async () => {
  console.log('3. Loading with filters:', state.filters);
  try {
    const testCasesData = await getAllTestCases({
      page: state.listPagination.page,
      size: state.listPagination.pageSize,
      ...state.filters
    });
    console.log('4. API returned:', testCasesData.content.length, 'test cases');
    // ...
  }
}, [state.listPagination.page, state.listPagination.pageSize, state.filters]);
```

### Issue 2: Empty Filters Being Sent

**Symptom**: `organization=""` appears in URL

**Cause**: Empty string is truthy in JavaScript

**Fix**: Only add to backendFilters if value is not empty:
```typescript
if (newFilters.organization && newFilters.organization !== '') {
  backendFilters.organization = newFilters.organization;
}
```

Already implemented! ✅

### Issue 3: No Re-render on Filter Change

**Symptom**: UI doesn't update when filter changes

**Cause**: React state not updating properly

**Check**: 
- Is `setFilters` being called?
- Does useEffect have correct dependencies?
- Is `loadTestCases` being called?

---

## ✅ Quick Verification Checklist

Run through these steps to verify:

- [ ] Open Test Cases page → Check network tab for API call
- [ ] Call should be: `/api/testcases?page=0&size=20`
- [ ] Select organization "Engineering" 
- [ ] Check network tab → Should see NEW call with `&organization=Engineering`
- [ ] Backend should return only Engineering test cases
- [ ] Table should update to show filtered results
- [ ] Pagination should reset to page 0

---

## 📝 Expected API Calls

### Load Organizations (on mount)
```
GET /api/testcases/organizations
Response: ["Engineering", "Marketing", "Sales"]
```

### Load Test Cases (initial)
```
GET /api/testcases?page=0&size=20
Response: {
  content: [...], 
  page: 0, 
  size: 20, 
  total: 100
}
```

### Load Test Cases (with org filter)
```
GET /api/testcases?page=0&size=20&organization=Engineering
Response: {
  content: [...],  // Only Engineering test cases
  page: 0,
  size: 20,
  total: 45  // Only 45 match the filter
}
```

### Load Test Cases (multiple filters)
```
GET /api/testcases?page=0&size=20&organization=Engineering&priority=High&type=Functional
Response: {
  content: [...],  // Only matching test cases
  page: 0,
  size: 20,
  total: 12  // Only 12 match all filters
}
```

---

## 🔬 Testing in Browser Console

Open DevTools Console and run:

```javascript
// Test API directly
const response = await fetch('http://localhost:8090/api/testcases?organization=Engineering&page=0&size=5');
const data = await response.json();
console.log('Test cases:', data.content);
console.log('Total:', data.total);

// Test organizations endpoint
const orgs = await fetch('http://localhost:8090/api/testcases/organizations');
console.log('Organizations:', await orgs.json());
```

---

## Summary

If filters are not showing in API calls:
1. ✅ Check network tab for query parameters
2. ✅ Add console.logs to trace data flow
3. ✅ Verify handleFilterChange is being called
4. ✅ Verify setFilters updates hook state
5. ✅ Verify loadTestCases includes filters in API call

All the code is in place - if filters aren't appearing, use the debugging steps above to trace where the flow is breaking!

**Date**: October 10, 2025

