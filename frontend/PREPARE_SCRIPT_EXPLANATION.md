# Prepare Script - Do You Need It?

## Answer: **No, you don't need to manually run it**

The `prepare` script runs **automatically** after `npm ci` via npm lifecycle hooks.

## How It Works in Docker Build

Looking at `Dockerfile.frontend`:

```dockerfile
# Line 32: Install dependencies
RUN npm ci
```

When `npm ci` runs, it automatically executes:
1. ✅ Install dependencies
2. ✅ Run `prepare` script (automatically)
3. ✅ Copy Monaco assets to `public/monaco/vs`

So you don't need to do anything - it happens automatically!

## Current Status

The `prepare` script copies Monaco assets to `public/monaco/vs`, but:
- ❌ Monaco is still trying to load from CDN (`cdn.jsdelivr.net`)
- ❌ The copied assets aren't being used
- ✅ Workers are bundled by Vite

## Should You Remove It?

Since Monaco is loading from CDN (not from `/monaco/vs/`), the copied assets are **not being used**. You have two options:

### Option 1: Keep It (Safe)
- Runs automatically, no extra work
- Might be useful as a fallback
- Adds ~20MB to build

### Option 2: Remove It (Cleaner)
- Remove from `package.json` scripts
- Faster builds
- Cleaner setup
- Only do this after fixing CDN loading

## Recommendation

**Keep it for now** since:
1. It runs automatically (no manual step)
2. Doesn't hurt anything
3. Might be useful once CDN loading is fixed

Once Monaco stops trying to load from CDN, you can remove it if you want.

