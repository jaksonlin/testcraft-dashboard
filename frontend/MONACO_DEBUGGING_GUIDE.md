# Monaco Editor Debugging Guide

## Overview

This guide helps you debug Monaco editor loading issues. The Monaco editor is configured for offline access, but if it's stuck on "Loading...", follow these steps.

## Debugging Steps

### 1. Check Browser Console

Open your browser's developer tools (F12) and check the Console tab. You should see logs prefixed with:
- `[Monaco Debug]` - Detailed debug information
- `[Monaco]` - Important status messages
- `[TestMethodSourceViewer]` - Component-level debug info

**Look for:**
- ✓ Success messages: `[Monaco] ✓ Configured offline loader at: ...`
- ✗ Error messages: Any red error messages
- Network errors: Failed requests to `/monaco/vs/...`

### 2. Check Network Tab

In the browser's Network tab:
1. Filter by "JS" or search for "monaco"
2. Look for requests to `/monaco/vs/loader.js`
3. Check if requests are:
   - ✅ 200 (OK) - Assets are accessible
   - ❌ 404 (Not Found) - Assets are missing
   - ❌ Failed - Network/CORS issue

**Common issues:**
- 404 errors: Monaco assets not copied to `public/monaco/vs`
- CORS errors: Check server configuration
- Timeout: Assets are too large or network is slow

### 3. Verify Monaco Assets

In the browser console, run:
```javascript
fetch('/monaco/vs/loader.js', { method: 'HEAD' })
  .then(res => console.log('Monaco loader status:', res.status, res.ok))
  .catch(err => console.error('Monaco loader error:', err));
```

Expected: `Monaco loader status: 200 true`

### 4. Check Monaco Configuration

In the browser console, check:
```javascript
// Check if Monaco environment is configured
console.log('MonacoEnvironment:', window.MonacoEnvironment);

// Check loader configuration (if available)
// This may not be directly accessible, but check console logs
```

### 5. Common Issues & Solutions

#### Issue: 404 Errors for Monaco Assets

**Solution:** Copy Monaco assets again:
```bash
cd frontend
npm run prepare
# Or manually:
node scripts/copy-monaco.mjs
```

#### Issue: CORS Errors

**Solution:** Check your server configuration allows serving static files from `/monaco/vs/`

#### Issue: Assets Path Incorrect

**Solution:** Check `BASE_URL` environment variable. The path should resolve to:
- Development: `http://localhost:5173/monaco/vs/loader.js`
- Production: `https://your-domain.com/monaco/vs/loader.js`

#### Issue: Monaco Never Loads (Timeout after 30s)

**Possible causes:**
1. Assets not accessible (404)
2. Worker creation failing
3. Network timeout
4. JavaScript errors blocking Monaco initialization

**Check:**
- Browser console for JavaScript errors
- Network tab for failed requests
- Console logs for worker creation errors

### 6. Enable Debug Mode

Debug mode is already enabled by default. Look for logs with:
- `[Monaco Debug]` prefix
- Detailed information about loader configuration
- Worker creation logs

### 7. Manual Verification

1. **Check file exists:**
   ```bash
   ls -la frontend/public/monaco/vs/loader.js
   ```

2. **Check file size (should be ~39KB):**
   ```bash
   ls -lh frontend/public/monaco/vs/loader.js
   ```

3. **Test in browser:**
   Open `http://localhost:5173/monaco/vs/loader.js` directly in your browser
   - Should see JavaScript code (not 404)

### 8. Reset Monaco Configuration

If Monaco is still stuck, try:
1. Clear browser cache
2. Hard refresh (Ctrl+Shift+R or Cmd+Shift+R)
3. Restart dev server
4. Re-copy Monaco assets

## Debug Output Example

When working correctly, you should see:
```
[Monaco Debug] Module loading, configuring loader...
[Monaco Debug] Configuring Monaco loader... { base: '', baseUrl: '/monaco', vsPath: '/monaco/vs', ... }
[Monaco] ✓ Configured offline loader at: /monaco/vs
[Monaco Debug] Loader configuration complete
[TestMethodSourceViewer] Component opened, initializing Monaco...
[Monaco Debug] ensureMonacoEnvironment called
[Monaco Debug] Monaco environment already configured
[Monaco Debug] Verifying Monaco assets at: http://localhost:5173/monaco/vs/loader.js
[Monaco] ✓ Assets verified at: http://localhost:5173/monaco/vs/loader.js
[TestMethodSourceViewer] Monaco beforeMount called
[TestMethodSourceViewer] Monaco editor mounted successfully
[TestMethodSourceViewer] Editor configured successfully
```

## Reporting Issues

When reporting Monaco loading issues, please include:
1. Browser console logs (all `[Monaco]` and `[Monaco Debug]` messages)
2. Network tab screenshot showing requests to `/monaco/vs/`
3. Any error messages in red
4. Browser and OS information
5. Steps to reproduce

