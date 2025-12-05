# Monaco Editor Overlay Fix

## Issue
User reported overlapping layer showing "Loading Monaco editor..." that persists after Monaco loads.

## Changes Made

### Removed ✅
1. **Custom loading overlay** - The overlay div with "Loading Monaco editor..." text
2. **`monacoLoading` state** - Removed unused state management
3. **Editor `loading` prop** - Removed custom loading prop from Editor component

### Result
- Editor component now handles its own loading state automatically
- No custom overlay interfering with the editor
- Cleaner, simpler code

## Current State

The Editor component from `@monaco-editor/react` may show its own brief loading indicator while initializing, which is normal and should disappear automatically once Monaco loads.

If a loading overlay persists after Monaco has loaded, it might be:
1. Browser caching issue - try hard refresh (Ctrl+Shift+R)
2. Editor's built-in loading indicator - should disappear automatically
3. CSS z-index issue - check if there are conflicting styles

## Testing

Monaco editor should now:
- ✅ Load successfully (using bundled workers)
- ✅ Display without persistent overlays
- ✅ Show content immediately once loaded

