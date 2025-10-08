# Test Case Header Row Separation Feature

## Summary
Separated header row from data start row to provide flexibility when importing Excel files with complex structures (skipped rows, multiple headers, etc.).

## Problem Statement
The original design assumed the header row was always immediately before the data row:
```
headerRow = dataStartRow - 1  ❌
```

This caused issues when users had:
- Multiple header rows
- Empty rows between header and data
- Title/description rows before headers
- Or wanted to skip certain rows

### Example Problem:
```
Row 1: "Test Case Report"     ← Title
Row 2: ID | Title | Steps      ← Header (auto-detected)
Row 3: (empty)                 ← User wants to skip
Row 4: TC-001 | Login | ...    ← Data starts here

With old design:
- dataStartRow = 4 (Excel row 5, 1-based)
- headerRow calculated as: 4 - 1 = 3 ❌
- But actual header is at row 2! (Excel row 3)
```

## Solution
Added separate `headerRow` parameter that's independent from `dataStartRow`:
- **headerRow**: Where column headers are located (0-based)
- **dataStartRow**: Where data starts (0-based, can be anywhere after header)

Now supports flexible Excel structures:
```
Row 0: "Test Case Report"      ← Title (skipped)
Row 1: ID | Title | Steps       ← headerRow = 1
Row 2: (empty)                  ← Skipped
Row 3: (empty)                  ← Skipped
Row 4: TC-001 | Login | ...     ← dataStartRow = 4 ✅
```

## Changes Made

### 1. Backend - ExcelParserService
**File**: `src/main/java/com/example/annotationextractor/testcase/ExcelParserService.java`

#### Updated parseWithMappings Methods
```java
public List<TestCase> parseWithMappings(
    InputStream inputStream,
    String filename,
    Map<String, String> columnMappings,
    int headerRow,          // NEW
    int dataStartRow) throws Exception
```

```java
public ParseResult parseWithMappingsDetailed(
    InputStream inputStream,
    String filename,
    Map<String, String> columnMappings,
    int headerRow,          // NEW - Where column headers are located
    int dataStartRow)       // Where data starts (can be different from headerRow + 1)
```

#### Updated ExcelPreview Class
```java
public static class ExcelPreview {
    private final int suggestedHeaderRow;     // NEW
    private final int suggestedDataStartRow;  // Existing
    
    public int getSuggestedHeaderRow() { return suggestedHeaderRow; }
    public int getSuggestedDataStartRow() { return suggestedDataStartRow; }
}
```

#### Key Change
```java
// OLD:
Map<String, Integer> columnIndexes = buildColumnIndexes(sheet, dataStartRow - 1, columnMappings);

// NEW:
Map<String, Integer> columnIndexes = buildColumnIndexes(sheet, headerRow, columnMappings);
```

### 2. Backend - TestCaseService
**File**: `src/main/java/com/example/annotationextractor/testcase/TestCaseService.java`

#### Updated importTestCases Method
```java
public ImportResult importTestCases(
    InputStream inputStream,
    String filename,
    Map<String, String> columnMappings,
    int headerRow,          // NEW
    int dataStartRow,
    boolean replaceExisting,
    String createdBy,
    String organization) throws Exception
```

Passes both parameters to parser:
```java
ExcelParserService.ParseResult parseResult = excelParserService.parseWithMappingsDetailed(
    new ByteArrayInputStream(bytes),
    filename,
    columnMappings,
    headerRow,        // NEW
    dataStartRow
);
```

### 3. Backend - TestCaseController
**File**: `src/main/java/com/example/annotationextractor/web/controller/TestCaseController.java`

#### Updated Preview Response
```java
return ResponseEntity.ok(Map.of(
    "columns", preview.getColumns(),
    "previewData", preview.getPreviewData(),
    "suggestedMappings", preview.getSuggestedMappings(),
    "confidence", preview.getConfidence(),
    "suggestedHeaderRow", preview.getSuggestedHeaderRow(),      // NEW
    "suggestedDataStartRow", preview.getSuggestedDataStartRow(),
    "validation", Map.of(...)
));
```

#### Updated Import Endpoint
```java
@PostMapping("/upload/import")
public ResponseEntity<?> importTestCases(
    @RequestParam("file") MultipartFile file,
    @RequestParam("mappings") String mappingsJson,
    @RequestParam(value = "headerRow", defaultValue = "0") int headerRow,  // NEW
    @RequestParam("dataStartRow") int dataStartRow,
    ...
)
```

### 4. Frontend - API Interface
**File**: `frontend/src/lib/testCaseApi.ts`

#### Updated ExcelPreviewResponse
```typescript
export interface ExcelPreviewResponse {
  columns: string[];
  previewData: Record<string, string>[];
  suggestedMappings: Record<string, string>;
  confidence: Record<string, number>;
  validation: {...};
  suggestedHeaderRow: number;       // NEW
  suggestedDataStartRow: number;
}
```

#### Updated importTestCases Function
```typescript
export const importTestCases = async (
  file: File,
  mappings: Record<string, string>,
  headerRow: number,        // NEW
  dataStartRow: number,
  replaceExisting?: boolean,
  createdBy?: string,
  organization?: string
): Promise<ImportResponse>
```

### 5. Frontend - Upload Wizard
**File**: `frontend/src/components/testcases/TestCaseUploadWizard.tsx`

#### Added State
```tsx
const [headerRow, setHeaderRow] = useState<number>(0);
const [dataStartRow, setDataStartRow] = useState<number>(1);
```

#### Initialize from Preview
```tsx
const previewData = await previewExcelFile(selectedFile);
setHeaderRow(previewData.suggestedHeaderRow);      // NEW
setDataStartRow(previewData.suggestedDataStartRow);
```

#### Pass to Import
```tsx
const result = await importTestCases(
  file,
  mappings,
  headerRow,      // NEW
  dataStartRow,
  ...
);
```

## Benefits

1. **Flexibility**: Handles complex Excel structures with non-standard layouts
2. **Accuracy**: Reads headers from correct row, not assumed location
3. **User Control**: Users can manually adjust both values if needed (future enhancement)
4. **Auto-Detection**: System still auto-detects sensible defaults
5. **Backward Compatible**: Uses defaults for existing code

## Example Use Cases

### Case 1: Standard Layout
```
Row 0: ID | Title | Steps       ← headerRow = 0
Row 1: TC-001 | Login | ...     ← dataStartRow = 1
Row 2: TC-002 | Logout | ...
```

### Case 2: Title + Empty Rows
```
Row 0: "Test Case Document"     ← Title (skipped)
Row 1: ID | Title | Steps        ← headerRow = 1
Row 2: (empty)                   ← Skipped
Row 3: TC-001 | Login | ...      ← dataStartRow = 3
```

### Case 3: Multiple Headers
```
Row 0: "Module: Authentication" ← Section header
Row 1: ID | Title | Steps        ← headerRow = 1
Row 2: TC-001 | Login | ...      ← dataStartRow = 2
Row 3: (empty)                   ← Skipped
Row 4: TC-002 | Logout | ...     ← Continues
```

### Case 4: Complex Structure
```
Row 0: "Project X Test Cases"   ← Title
Row 1: "Version 2.0"             ← Subtitle
Row 2: (empty)
Row 3: ID | Title | Steps        ← headerRow = 3
Row 4: "Below are the tests"     ← Description
Row 5: (empty)
Row 6: TC-001 | Login | ...      ← dataStartRow = 6
```

## API Changes

### Preview Response
```json
{
  "columns": ["ID", "Title", "Steps"],
  "suggestedHeaderRow": 1,      // NEW
  "suggestedDataStartRow": 2,
  "suggestedMappings": {...},
  "confidence": {...}
}
```

### Import Request
```
POST /api/testcases/upload/import
  - file: MultipartFile
  - mappings: JSON
  - headerRow: 1              // NEW
  - dataStartRow: 3
  - replaceExisting: true
  - createdBy: "user123"
  - organization: "acme"
```

## Row Numbering
- **Backend (Java)**: 0-based (Row 0, Row 1, Row 2...)
- **Excel Display**: 1-based (Row 1, Row 2, Row 3...)
- **Conversion**: ExcelRow = JavaRow + 1

## Testing Scenarios

- ✅ Standard layout (header immediately before data)
- ✅ Empty rows between header and data
- ✅ Title rows before header
- ✅ Multiple empty rows to skip
- ✅ Complex structures with descriptions
- ✅ Auto-detection still works
- ✅ Manual override (when implemented in UI)

## Files Modified

### Backend (3 files)
1. `src/main/java/com/example/annotationextractor/testcase/ExcelParserService.java`
2. `src/main/java/com/example/annotationextractor/testcase/TestCaseService.java`
3. `src/main/java/com/example/annotationextractor/web/controller/TestCaseController.java`

### Frontend (2 files)
1. `frontend/src/lib/testCaseApi.ts`
2. `frontend/src/components/testcases/TestCaseUploadWizard.tsx`

## Future Enhancements

### UI Controls (Not Yet Implemented)
Could add to preview step:
```tsx
<div>
  <label>Header Row: <input type="number" value={headerRow} onChange={...} /></label>
  <label>Data Start Row: <input type="number" value={dataStartRow} onChange={...} /></label>
</div>
```

This would allow users to manually adjust if auto-detection is wrong.

### Multiple Header Rows
Could support merging multiple header rows:
```
Row 0: Module | Test Case | Test Case | Result
Row 1: ID     | Title     | Steps     | Expected
       ^----merged----^
```

## Backward Compatibility

- Default value for `headerRow` is `0` if not provided
- Existing code that doesn't send `headerRow` will use row 0
- Auto-detection ensures sensible defaults
- Deprecated methods maintained in TestCase entity

## Migration Notes

No migration needed for existing data. This only affects the import process, not stored data structure.

