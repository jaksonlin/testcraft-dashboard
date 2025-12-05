# Monaco Editor Stuck Loading - Debug Guide

## Problem

Monaco editor is stuck in loading state. All files are copied correctly (121 files verified), but Monaco isn't initializing.

## Quick Checks

### 1. Check Browser Console

Open DevTools (F12) → Console tab. Look for:

**Expected messages:**
- `[Monaco Debug] ✓ Loader configured - will use local assets from /monaco/vs (CDN disabled)`
- `[Monaco Debug] Configuring Monaco workers (bundled)...`
- `[Monaco Debug] ✓ Monaco environment configured...`
- `[TestMethodSourceViewer] Component opened, initializing Monaco...`

**Check for errors:**
- Any red error messages?
- Worker creation errors?
- Network request failures?

### 2. Check Network Tab

Open DevTools (F12) → Network tab. Filter by "monaco":

**What to check:**
- ✅ Requests to `/monaco/vs/loader.js` - Should return 200 OK
- ✅ Requests to `/monaco/vs/editor/editor.main.js` - Should return 200 OK
- ❌ NO requests to `cdn.jsdelivr.net` - If you see these, loader config isn't working
- ❌ NO 404 errors - If you see 404s, files are missing

### 3. Test Loader Manually

Open browser console and run:

```javascript
// Test 1: Check loader.js accessibility
fetch('/monaco/vs/loader.js')
  .then(r => {
    console.log('Loader status:', r.status, r.ok ? '✓ OK' : '✗ FAILED');
    return r.text();
  })
  .then(text => console.log('Loader content length:', text.length))
  .catch(e => console.error('✗ Loader failed:', e));

// Test 2: Check editor.main.js accessibility  
fetch('/monaco/vs/editor/editor.main.js')
  .then(r => {
    console.log('Editor main status:', r.status, r.ok ? '✓ OK' : '✗ FAILED');
    return r.text();
  })
  .then(text => console.log('Editor main content length:', text.length))
  .catch(e => console.error('✗ Editor main failed:', e));

// Test 3: Check MonacoEnvironment
console.log('MonacoEnvironment:', window.MonacoEnvironment);
```

### 4. Check Editor Component

In `TestMethodSourceViewer.tsx`, the `beforeMount` callback should be called when Monaco starts loading.

**If `beforeMount` is never called:**
- Monaco isn't even attempting to load
- Issue is with `@monaco-editor/react` initialization
- Check if Editor component is rendering in DOM

**If `beforeMount` is called but editor doesn't mount:**
- Monaco is trying to load but failing
- Check network requests for failures
- Check console for loading errors

## Common Issues

### Issue 1: Loader Config Not Applied

**Symptom:** Requests still go to `cdn.jsdelivr.net`

**Solution:** Check if `loader.config()` is being called before Editor mounts. The config must happen before `@monaco-editor/react` initializes.

### Issue 2: Path Resolution

**Symptom:** 404 errors for Monaco files

**Solution:** Verify the path `/monaco/vs` is correct for your setup. If app is served from a subpath, adjust accordingly.

### Issue 3: Worker Creation Failing

**Symptom:** Console errors about workers

**Solution:** Check worker imports are correct and workers are bundled properly.

### Issue 4: CSP Blocking Scripts

**Symptom:** CSP errors in console

**Solution:** Adjust Content Security Policy to allow Monaco scripts.

## Next Steps

Please share:

1. **Console output** - All `[Monaco Debug]` messages and any errors
2. **Network tab** - Screenshot or list of requests filtered by "monaco"
3. **beforeMount status** - Is it being called?
4. **Manual test results** - Results from the 3 test commands above

This will help identify the exact issue.

