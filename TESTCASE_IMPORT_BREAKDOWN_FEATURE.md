# Test Case Import Breakdown Feature

## Summary
Enhanced the test case import wizard to show detailed breakdown of created vs updated test cases, providing better visibility into what happened during the import process.

## Problem
Previously, the import wizard only showed:
- ✅ Total imported count
- ⚠️ Skipped count

Users couldn't tell whether test cases were newly created or updated from existing records.

## Solution
Now the import wizard displays:
- 🆕 **Created** - New test cases added to the database
- 🔄 **Updated** - Existing test cases that were updated
- ⚠️ **Skipped** - Invalid entries that couldn't be imported

## Changes Made

### 1. Backend - Repository Layer
**File**: `src/main/java/com/example/annotationextractor/testcase/TestCaseRepository.java`

#### Added SaveResult Class
```java
public static class SaveResult {
    private final int created;
    private final int updated;
    
    public SaveResult(int created, int updated) {
        this.created = created;
        this.updated = updated;
    }
    
    public int getCreated() { return created; }
    public int getUpdated() { return updated; }
    public int getTotal() { return created + updated; }
}
```

#### Updated save() Method
- Modified `save()` to return `boolean` indicating if record was created (true) or updated (false)
- Checks if test case exists before UPSERT operation
- Returns true for new records, false for updates

#### Updated saveAll() Method
- Now returns `SaveResult` instead of just count
- Tracks creates and updates separately as it processes each test case
- Wrapped in transaction for data integrity

### 2. Backend - Service Layer
**File**: `src/main/java/com/example/annotationextractor/testcase/TestCaseService.java`

#### Updated ImportResult Class
```java
public static class ImportResult {
    private final boolean success;
    private final int created;     // NEW
    private final int updated;     // NEW
    private final int skipped;
    private final List<String> errors;
    private final List<String> suggestions;
    
    public int getImported() { 
        return created + updated; // Backward compatibility
    }
}
```

#### Updated importTestCases() Method
- Uses `SaveResult` from repository
- Populates `created` and `updated` counts separately
- Maintains `imported` as sum for backward compatibility

### 3. Backend - Controller Layer
**File**: `src/main/java/com/example/annotationextractor/web/controller/TestCaseController.java`

Updated response to include breakdown:
```java
return ResponseEntity.ok(Map.of(
    "success", true,
    "imported", result.getImported(),
    "created", result.getCreated(),     // NEW
    "updated", result.getUpdated(),     // NEW
    "skipped", result.getSkipped(),
    "message", "Successfully imported X test cases (Y created, Z updated)"
));
```

### 4. Frontend - API Interface
**File**: `frontend/src/lib/testCaseApi.ts`

Updated TypeScript interface:
```typescript
export interface ImportResponse {
  success: boolean;
  imported: number;
  created: number;     // NEW
  updated: number;     // NEW
  skipped: number;
  message?: string;
  errors?: string[];
  suggestions?: string[];
}
```

### 5. Frontend - Upload Wizard
**File**: `frontend/src/components/testcases/TestCaseUploadWizard.tsx`

#### Enhanced Complete Step Display
Replaced single "Imported" card with three-card breakdown:

```tsx
<div className="grid grid-cols-3 gap-4 max-w-2xl mx-auto">
  {/* Created Card */}
  <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
    <div className="text-3xl font-bold text-blue-600">{result.created || 0}</div>
    <div className="text-sm text-gray-600 font-medium">Created</div>
    <div className="text-xs text-gray-500 mt-1">New test cases</div>
  </div>
  
  {/* Updated Card */}
  <div className="bg-green-50 border border-green-200 rounded-lg p-4">
    <div className="text-3xl font-bold text-green-600">{result.updated || 0}</div>
    <div className="text-sm text-gray-600 font-medium">Updated</div>
    <div className="text-xs text-gray-500 mt-1">Existing test cases</div>
  </div>
  
  {/* Skipped Card (if any) */}
  {result.skipped > 0 && (
    <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
      <div className="text-3xl font-bold text-yellow-600">{result.skipped}</div>
      <div className="text-sm text-gray-600 font-medium">Skipped</div>
      <div className="text-xs text-gray-500 mt-1">Invalid entries</div>
    </div>
  )}
</div>
```

## User Experience

### Before
```
✅ Import Complete!
   Successfully imported 50 test cases
   
   [50 Imported] [5 Skipped]
```

### After
```
✅ Import Complete!
   Successfully processed 50 test cases
   
   [30 Created] [20 Updated] [5 Skipped]
   New test cases | Existing test cases | Invalid entries
   
   📝 Successfully imported 50 test cases (30 created, 20 updated)
```

## Example Scenarios

### Scenario 1: First Import
Upload Excel with 100 test cases (all new)
```
Result:
- Created: 100
- Updated: 0
- Skipped: 0
```

### Scenario 2: Re-import Same File
Upload same Excel file again
```
Result:
- Created: 0
- Updated: 100  (all test cases already exist)
- Skipped: 0
```

### Scenario 3: Updated Test Cases
Upload Excel with 50 existing + 30 new test cases
```
Result:
- Created: 30   (new test cases)
- Updated: 50   (existing test cases updated)
- Skipped: 5    (invalid entries)
```

### Scenario 4: Mixed with Errors
Upload Excel with some invalid rows
```
Result:
- Created: 20
- Updated: 30
- Skipped: 10   (missing required fields)
```

## Technical Details

### How Create/Update Detection Works
1. **Before UPSERT**: Check if test case exists with same `(external_id, organization)`
2. **Perform UPSERT**: Execute `INSERT ... ON CONFLICT ... DO UPDATE`
3. **Return Result**: `true` if didn't exist (created), `false` if existed (updated)

### Database Operation
```sql
-- Check existence
SELECT internal_id FROM test_cases 
WHERE external_id = ? AND organization = ?

-- Upsert
INSERT INTO test_cases (...) VALUES (...)
ON CONFLICT (external_id, organization) 
DO UPDATE SET ... 
RETURNING internal_id
```

### Transaction Safety
All saves happen in a single transaction:
- If any save fails, entire batch rolls back
- Consistent create/update counts
- Data integrity maintained

## Benefits

1. **Clear Visibility**: Users know exactly what happened during import
2. **Better Understanding**: Distinguish between new and updated test cases
3. **Audit Trail**: Can track import history better
4. **Error Detection**: Easier to spot issues (e.g., all updates, no creates might indicate wrong file)
5. **User Confidence**: Detailed feedback builds trust in the system

## Testing Checklist

- ✅ First-time import shows all as "Created"
- ✅ Re-import shows all as "Updated"
- ✅ Mixed import shows correct breakdown
- ✅ Skipped cases display when present
- ✅ Message includes breakdown text
- ✅ No linting errors in backend
- ✅ No linting errors in frontend
- ✅ Transaction rollback works on error
- ✅ Counts are accurate

## Files Modified

### Backend (3 files)
1. `src/main/java/com/example/annotationextractor/testcase/TestCaseRepository.java`
2. `src/main/java/com/example/annotationextractor/testcase/TestCaseService.java`
3. `src/main/java/com/example/annotationextractor/web/controller/TestCaseController.java`

### Frontend (2 files)
1. `frontend/src/lib/testCaseApi.ts`
2. `frontend/src/components/testcases/TestCaseUploadWizard.tsx`

## API Response Example

```json
{
  "success": true,
  "imported": 50,
  "created": 30,
  "updated": 20,
  "skipped": 5,
  "message": "Successfully imported 50 test cases (30 created, 20 updated)"
}
```

## Backward Compatibility

- `imported` field maintained as sum of created + updated
- Existing API consumers still work
- New consumers can use detailed breakdown

## Future Enhancements

Potential additions:
- Show list of created test case IDs
- Show list of updated test case IDs
- Export import log as CSV
- Email notification with breakdown
- Dashboard chart showing import history
- Highlight what changed in updated test cases

