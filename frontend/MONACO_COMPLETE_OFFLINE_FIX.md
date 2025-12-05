# Monaco Editor - Complete Offline Fix

## Problem

Monaco editor fails to load and there are no network requests in the Network panel. This means Monaco isn't even attempting to load, suggesting a configuration issue.

## Solution: Self-Contained Monaco with All Assets Downloaded Locally

We need to ensure:
1. All Monaco assets are copied to `public/monaco/vs/` (done via `prepare` script)
2. Loader is configured to use local assets (`/monaco/vs`)
3. Monaco is pre-loaded before Editor component tries to use it
4. Workers are configured to use bundled workers from Vite

## Implementation Steps

### 1. Verify Monaco Assets Are Copied

The `prepare` script (`scripts/copy-monaco.mjs`) copies Monaco from `node_modules/monaco-editor/min/vs` to `public/monaco/vs/`. This runs automatically on `npm install`.

**Check if assets exist:**
```bash
cd frontend
ls -la public/monaco/vs/loader.js  # Should exist
ls -la public/monaco/vs/editor/editor.main.js  # Should exist
```

### 2. Configure Loader to Use Local Assets

The loader must be configured to point to `/monaco/vs` instead of CDN:

```typescript
loader.config({
  paths: {
    vs: '/monaco/vs'  // Local assets path
  }
});
```

### 3. Pre-load Monaco

Use `loader.init()` to pre-load Monaco before the Editor component tries to use it:

```typescript
loader.init().then((monaco) => {
  // Monaco is ready
});
```

### 4. Configure Workers

Workers should use Vite's bundled workers (via `?worker` imports) for offline functionality.

## Files Modified

1. **`frontend/src/lib/monacoEnvironment.ts`** - Configure loader and pre-load Monaco
2. **`frontend/src/main.tsx`** - Initialize Monaco before app renders
3. **`frontend/scripts/copy-monaco.mjs`** - Copy Monaco assets (already exists)
4. **`frontend/package.json`** - `prepare` script runs copy script (already exists)

## Docker Build Process

The Dockerfile ensures Monaco assets are available:

1. `npm ci` - Installs dependencies, runs `prepare` script automatically
2. `npm run build` - Builds the app, includes Monaco assets in `dist/`
3. Assets are copied to production stage with `COPY --from=build /app/dist .`

## Testing

1. **Development:**
   ```bash
   cd frontend
   npm install  # Runs prepare script
   npm run dev
   ```

2. **Production Build:**
   ```bash
   cd frontend
   npm run build
   # Check dist/ directory contains Monaco assets
   ```

3. **Offline Test:**
   - Build Docker image
   - Run with `--network none`
   - Monaco should load from bundled assets

## Debugging

If Monaco still doesn't load:

1. **Check Network Tab:**
   - Should see requests to `/monaco/vs/loader.js`
   - Should NOT see requests to `cdn.jsdelivr.net`

2. **Check Console:**
   - Look for `[Monaco Debug]` logs
   - Check for errors in `loader.init()`

3. **Verify Assets:**
   ```bash
   curl http://localhost:5173/monaco/vs/loader.js
   # Should return JavaScript code
   ```

4. **Check Browser Console:**
   - Run: `window.MonacoEnvironment` - should be defined
   - Check if `loader` is configured correctly

## Key Points

- ✅ All Monaco assets are downloaded locally via `prepare` script
- ✅ Loader points to `/monaco/vs` (local assets)
- ✅ Monaco is pre-loaded before Editor component mounts
- ✅ Workers use bundled workers from Vite
- ✅ No CDN dependencies at runtime
- ✅ Works completely offline after build

