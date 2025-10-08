# Test Case Row Controls UI Feature

## Summary
Added user controls to manually adjust header row and data start row in the upload wizard, with visual feedback showing which rows will be used.

## Feature Overview

Users can now manually control:
- **Header Row**: Which row contains the column headers (ID, Title, Steps, etc.)
- **Data Start Row**: Which row the actual test case data begins

## UI Design

### Location
In the **Mapping Step** (Step 2) of the upload wizard, after the column mappings section.

### Layout
```
┌─────────────────────────────────────────────────────┐
│ Excel Row Settings                                  │
├──────────────────────┬──────────────────────────────┤
│ Header Row (0-based):│ Data Start Row (0-based):    │
│ [  1  ]              │ [  3  ]                      │
│ Row containing       │ First row containing         │
│ column headers       │ data (must be after header)  │
└──────────────────────┴──────────────────────────────┘

Note: Row numbers are 0-based. Row 0 = Excel Row 1
┌──────────────────────────────────────────────────────┐
│ Row 0 (Excel Row 1): Title | Description | ...      │
│ Row 1 (Excel Row 2): ID | Title | Steps ← Header Row│
│ Row 3 (Excel Row 4): TC-001 | ... ← Data Start Row  │
└──────────────────────────────────────────────────────┘
```

### Visual Preview
The blue info box shows a live preview of:
- Which row is the header row (highlighted in bold blue)
- Which row is the data start row (highlighted in bold green)
- Shows actual data from the Excel file

## User Workflow

### Auto-Detection (Default)
1. User uploads Excel file
2. System auto-detects:
   - `headerRow` (first row with multiple columns)
   - `dataStartRow` (row after header, `headerRow + 1`)
3. User sees pre-populated values
4. User can proceed or adjust if needed

### Manual Adjustment
1. User sees auto-detected values don't match their file
2. User adjusts header row number
3. System validates data start row must be after header
4. Visual preview updates to show selection
5. User sees preview of what will be imported
6. User continues to import

## Validation Rules

### Header Row
- **Min**: 0 (first row in Excel)
- **Default**: Auto-detected (usually 0 or 1)
- **Purpose**: Where column names are located

### Data Start Row
- **Min**: `headerRow + 1` (must be after header)
- **Default**: Auto-detected (usually `headerRow + 1`)
- **Purpose**: Where test case data begins
- **Validation**: Automatically enforced via `min` attribute

## Example Scenarios

### Scenario 1: Standard Layout
```
Excel:
Row 1: ID | Title | Steps       ← Header
Row 2: TC-001 | Login | ...     ← Data

Settings:
- headerRow = 0 (Excel Row 1)
- dataStartRow = 1 (Excel Row 2)
```

### Scenario 2: Title + Empty Rows
```
Excel:
Row 1: "Test Case Report"       ← Title
Row 2: ID | Title | Steps        ← Header
Row 3: (empty)                   ← Skip
Row 4: TC-001 | Login | ...      ← Data

User adjusts:
- headerRow = 1 (Excel Row 2)
- dataStartRow = 3 (Excel Row 4)
```

### Scenario 3: Complex Structure
```
Excel:
Row 1: "Project X"               ← Title
Row 2: "Version 2.0"             ← Subtitle
Row 3: (empty)
Row 4: ID | Title | Steps        ← Header
Row 5: "Test Cases Below"        ← Description
Row 6: (empty)
Row 7: TC-001 | Login | ...      ← Data

User adjusts:
- headerRow = 3 (Excel Row 4)
- dataStartRow = 6 (Excel Row 7)
```

## Code Changes

### Updated Component Props
```tsx
interface MappingStepProps {
  preview: ExcelPreviewResponse;
  mappings: Record<string, string>;
  headerRow: number;              // NEW
  dataStartRow: number;
  isValid: boolean;
  missingFields: string[];
  suggestions: string[];
  onMappingChange: (excelColumn: string, systemField: string) => void;
  onHeaderRowChange: (row: number) => void;   // NEW
  onDataStartRowChange: (row: number) => void;
  onNext: () => void;
  onBack: () => void;
}
```

### UI Controls
```tsx
{/* Header Row Input */}
<input
  type="number"
  min={0}
  value={headerRow}
  onChange={(e) => onHeaderRowChange(parseInt(e.target.value) || 0)}
  className="px-3 py-2 border border-gray-300 rounded-lg w-32"
/>

{/* Data Start Row Input */}
<input
  type="number"
  min={headerRow + 1}  // Must be after header
  value={dataStartRow}
  onChange={(e) => onDataStartRowChange(parseInt(e.target.value) || headerRow + 1)}
  className="px-3 py-2 border border-gray-300 rounded-lg w-32"
/>
```

### Visual Feedback
```tsx
<div className="text-xs text-blue-700 font-mono bg-white rounded p-2">
  {/* Shows which row is header */}
  <div className="font-bold text-blue-900">
    Row {headerRow} (Excel Row {headerRow + 1}): 
    {preview.columns.join(' | ')} ← Header Row
  </div>
  
  {/* Shows which row is data start */}
  <div className="font-bold text-green-700">
    Row {dataStartRow} (Excel Row {dataStartRow + 1}): 
    {preview.previewData[0] data...} ← Data Start Row
  </div>
</div>
```

## Benefits

1. **User Control**: Manual override when auto-detection is wrong
2. **Visual Feedback**: See exactly which rows will be used
3. **Validation**: Prevents invalid combinations (data before header)
4. **Clear Labels**: 0-based vs Excel numbering explained
5. **Smart Defaults**: Auto-detection works most of the time
6. **Flexibility**: Handles any Excel structure

## User Experience

### Good Auto-Detection
- User uploads file
- Values are pre-filled correctly
- User just clicks "Next" ✅

### Wrong Auto-Detection
- User uploads file with complex structure
- Auto-detection guesses wrong
- User sees visual preview doesn't match
- User adjusts numbers
- Visual preview updates
- User sees correct preview
- User clicks "Next" ✅

## Accessibility Features

- Number inputs with min/max validation
- Clear labels with explanations
- Visual preview for confirmation
- Help text explaining 0-based indexing
- Color coding (blue = header, green = data)

## Files Modified

1. `frontend/src/components/testcases/TestCaseUploadWizard.tsx`
   - Added headerRow prop to MappingStep
   - Added UI controls for both headerRow and dataStartRow
   - Added visual preview section
   - Added validation (min values)
   - Added help text and examples

## Testing Checklist

- ✅ Default values auto-populated correctly
- ✅ Manual adjustment works for both fields
- ✅ Validation prevents data start before header
- ✅ Visual preview updates when values change
- ✅ 0-based numbering explained clearly
- ✅ Excel row conversion shown (Row 0 = Excel Row 1)
- ✅ Preview data displays correctly
- ✅ Import uses user-specified values
- ✅ No linting errors

## Screenshots of UI

### Row Settings Section
```
┌──────────────────────────────────────────────────────────┐
│ Excel Row Settings                                       │
├────────────────────────┬─────────────────────────────────┤
│ Header Row (0-based):  │ Data Start Row (0-based):       │
│ [ 1 ]                  │ [ 3 ]                           │
│ Row containing column  │ First row containing data       │
│ headers (usually 0/1)  │ (must be after header)          │
└────────────────────────┴─────────────────────────────────┘

ℹ️ Note: Row numbers are 0-based. Row 0 = Excel Row 1
┌──────────────────────────────────────────────────────────┐
│ Row 0: "Test Case Report"                                │
│ Row 1: ID | Title | Steps ← Header Row                   │
│ Row 3: TC-001 | Login Test | ... ← Data Start Row        │
└──────────────────────────────────────────────────────────┘
```

## Future Enhancements

### 1. Visual Row Selector
- Show all rows from Excel
- Click to select header row
- Click to select data start row
- Highlight selected rows

### 2. Smart Suggestions
- Show multiple header row candidates
- Highlight likely data start rows
- Allow quick selection from suggestions

### 3. Range Preview
- Show rows between header and data start
- Indicate which will be skipped
- Allow excluding specific rows

### 4. Multiple Sheets
- Support selecting which sheet to import
- Different row settings per sheet
- Batch import from multiple sheets

## Example Validation

### Valid Combinations
- ✅ headerRow = 0, dataStartRow = 1
- ✅ headerRow = 1, dataStartRow = 2
- ✅ headerRow = 1, dataStartRow = 5 (skips rows 2-4)
- ✅ headerRow = 3, dataStartRow = 10

### Invalid Combinations
- ❌ headerRow = 1, dataStartRow = 1 (data can't be same as header)
- ❌ headerRow = 2, dataStartRow = 1 (data can't be before header)
- ❌ headerRow = -1 (negative not allowed)

The validation automatically prevents invalid combinations through the `min` attribute on the data start row input.

## Impact

This feature makes the upload wizard much more flexible and user-friendly. Users can now:
- Import Excel files with any structure
- Manually override auto-detection when needed
- See visual confirmation of their choices
- Understand the relationship between header and data rows

No more "failed to import" errors due to assumed row structures! 🎉

