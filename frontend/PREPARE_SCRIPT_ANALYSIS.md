# Prepare Script Analysis

## What the `prepare` Script Does

The `prepare` script copies Monaco assets from:
- **Source**: `node_modules/monaco-editor/min/vs`
- **Target**: `public/monaco/vs`

## When It Runs

The `prepare` script runs automatically via npm lifecycle hooks:
- ✅ After `npm install` or `npm ci`
- ✅ Before `npm publish`
- ✅ **In Docker build**: After `npm ci` (line 32 of Dockerfile.frontend)

## Current Status

### With Bundled Workers Configuration
- Workers are bundled by Vite using `?worker` syntax
- Monaco editor should be bundled by `@monaco-editor/react`
- The copied assets in `public/monaco/vs` are **NOT being used**

### Problem
Monaco is still trying to load from CDN (`cdn.jsdelivr.net`), which means:
- The bundled configuration isn't working
- The copied assets won't help (Monaco is loading from CDN, not `/monaco/vs/`)

## Recommendation

**You don't need to manually run it** - it runs automatically. But:

### Option 1: Keep It (Safe)
- ✅ Runs automatically, no manual intervention
- ✅ Acts as a fallback if Monaco needs local assets
- ❌ Adds unnecessary build time
- ❌ Copies unused files (~20MB+)

### Option 2: Remove It (If Bundled Works)
- ✅ Faster builds (no copy step)
- ✅ Cleaner setup
- ❌ No fallback assets

## Decision

Since Monaco is still trying to load from CDN, the copied assets aren't being used anyway. You can:

1. **Keep it for now** - It runs automatically, so no harm
2. **Remove it later** - Once CDN loading is fixed and bundled Monaco works

## To Remove (Future)

If you want to remove it:
```json
{
  "scripts": {
    "dev": "vite",
    "build": "tsc -b && vite build",
    "lint": "eslint .",
    "preview": "vite preview"
    // Remove: "prepare": "node scripts/copy-monaco.mjs"
  }
}
```

## Conclusion

**Answer: No, you don't need to manually run it** - it runs automatically after `npm ci` in Docker. The script will execute during the Docker build process.

