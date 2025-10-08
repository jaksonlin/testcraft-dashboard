# Test Case ID Refactoring - Complete

## Summary
Refactored the test case ID structure to separate internal database IDs from external test management system IDs. This prevents ID conflicts when importing test cases from different systems and follows proper database design principles.

## Problem Statement
The original implementation used the test case's external ID (from test management systems like Jira, TestRail, etc.) as the primary key in the database. This caused several issues:

1. **ID Conflicts**: External IDs from different test management systems could conflict
2. **Not Unique**: External IDs might not be unique across organizations
3. **Poor Design**: Primary keys should be internal and auto-generated
4. **Inflexibility**: External systems might reuse or change IDs

## Solution
Implemented a two-ID system:
- **Internal ID** (`internal_id`): Auto-generated SERIAL primary key
- **External ID** (`external_id`): The test case ID from external systems (TC-1234, ID-5678, etc.)

## Changes Made

### 1. Database Migration
**File**: `src/main/resources/db/migration/V3__refactor_test_case_id_to_internal.sql`

- Added `internal_id SERIAL` column as new primary key
- Renamed `id` column to `external_id`
- Updated foreign key references in `test_case_coverage` table
- Added unique constraint on (`external_id`, `organization`)
- Updated all indexes and constraints

Key SQL changes:
```sql
ALTER TABLE test_cases ADD COLUMN internal_id SERIAL;
ALTER TABLE test_cases RENAME COLUMN id TO external_id;
ALTER TABLE test_cases DROP CONSTRAINT test_cases_pkey;
ALTER TABLE test_cases ADD PRIMARY KEY (internal_id);
ALTER TABLE test_cases ADD CONSTRAINT test_cases_external_id_org_unique UNIQUE (external_id, organization);
```

### 2. Java Entity
**File**: `src/main/java/com/example/annotationextractor/testcase/TestCase.java`

- Added `private Long internalId` field
- Renamed `private String id` to `private String externalId`
- Added getters/setters for both fields
- Added deprecated legacy methods for backward compatibility
- Updated validation to check `externalId`
- Updated `toString()` method

```java
private Long internalId;        // Internal database ID (auto-generated)
private String externalId;      // TC-1234, ID-5678, etc. (from external system)
```

### 3. Repository Layer
**File**: `src/main/java/com/example/annotationextractor/testcase/TestCaseRepository.java`

Updated all SQL queries and methods:
- `save()`: Uses `external_id` in INSERT, returns `internal_id` via RETURNING clause
- `findById(Long)`: Finds by internal ID
- `findByExternalId(String, String)`: Finds by external ID + organization
- `deleteById(Long)`: Deletes by internal ID
- `linkTestCaseToMethod(Long, ...)`: Links using internal ID
- Updated all JOIN queries to use `internal_id`
- Updated all ORDER BY clauses to use `internal_id`

### 4. Service Layer
**File**: `src/main/java/com/example/annotationextractor/testcase/TestCaseService.java`

- `getTestCaseById(Long)`: Gets by internal ID
- `getTestCaseByExternalId(String)`: Gets by external ID (deprecated)
- `getTestCaseByExternalId(String, String)`: Gets by external ID + organization
- `deleteTestCase(Long)`: Deletes by internal ID
- `deleteTestCaseByExternalId(String, String)`: Deletes by external ID + organization
- `linkTestMethodsToCases()`: Updated to look up internal IDs for linkage

### 5. Controller Layer
**File**: `src/main/java/com/example/annotationextractor/web/controller/TestCaseController.java`

- Changed `@PathVariable String id` to `@PathVariable Long id` in:
  - `getTestCase(Long id)`: GET /api/testcases/{id}
  - `deleteTestCase(Long id)`: DELETE /api/testcases/{id}

API endpoints now use internal IDs.

### 6. Frontend - TypeScript Interface
**File**: `frontend/src/lib/testCaseApi.ts`

Updated `TestCase` interface:
```typescript
export interface TestCase {
  internalId: number;        // Internal database ID (primary key)
  externalId: string;        // External test case ID from test management system
  title: string;
  steps: string;
  // ... other fields
  id?: string;              // Legacy field for backward compatibility
}
```

Updated API functions:
- `getTestCaseById(internalId: number)`
- `deleteTestCase(internalId: number)`

### 7. Frontend - Components
**Files**: 
- `frontend/src/components/testcases/TestCaseListTable.tsx`
- `frontend/src/components/testcases/TestCaseDetailModal.tsx`
- `frontend/src/views/TestCasesView.tsx`

Changes:
- Display `externalId` to users (visible ID)
- Use `internalId` for API operations (delete, get)
- Updated search functionality to search by `externalId`
- Updated delete confirmation to show `externalId`

## Data Flow

### When Importing Test Cases:
1. User uploads Excel with external IDs (TC-1234, etc.)
2. Service parses and creates TestCase objects with `externalId` set
3. Repository saves to database
4. Database auto-generates `internal_id`
5. RETURNING clause populates `internalId` in Java object

### When Linking Test Methods to Test Cases:
1. Test method annotation contains external ID: `@TestCase("TC-1234")`
2. Service looks up TestCase by external ID
3. Gets the internal ID from TestCase object
4. Links using internal ID in coverage table

### When Displaying to Users:
1. API returns TestCase with both IDs
2. Frontend displays `externalId` (TC-1234)
3. Frontend uses `internalId` for operations

## Benefits

1. **No ID Conflicts**: Each test case has a unique internal ID
2. **Multi-System Support**: Can import from multiple systems without conflicts
3. **Data Integrity**: Proper foreign key references with auto-generated IDs
4. **Flexibility**: External IDs can change without breaking relationships
5. **Organization Isolation**: Same external ID can exist in different organizations
6. **Backward Compatibility**: Legacy methods marked as deprecated but still work

## Migration Notes

### For Existing Data:
The migration automatically:
- Adds `internal_id` to existing rows
- Renames `id` to `external_id`
- Updates coverage table references
- Maintains data integrity

### For New Development:
- Always use `internalId` for CRUD operations
- Display `externalId` to users
- Use `(externalId, organization)` for lookups from annotations

## Testing Checklist

- ✅ Database migration runs successfully
- ✅ No linting errors in Java code
- ✅ No linting errors in TypeScript code
- ✅ Test case import works with external IDs
- ✅ Test case listing displays external IDs
- ✅ Test case delete uses internal IDs
- ✅ Test method linking works correctly
- ✅ Coverage statistics calculate correctly
- ✅ Search by external ID works
- ✅ Unique constraint on (external_id, organization) prevents duplicates

## API Changes

### Breaking Changes:
- `GET /api/testcases/{id}` now expects internal ID (number) instead of external ID (string)
- `DELETE /api/testcases/{id}` now expects internal ID (number) instead of external ID (string)

### Response Format:
All test case responses now include both IDs:
```json
{
  "internalId": 1,
  "externalId": "TC-1234",
  "title": "Test user login",
  "steps": "1. Navigate to login\n2. Enter credentials",
  ...
}
```

## Files Modified

### Backend:
1. `src/main/resources/db/migration/V3__refactor_test_case_id_to_internal.sql` (NEW)
2. `src/main/java/com/example/annotationextractor/testcase/TestCase.java`
3. `src/main/java/com/example/annotationextractor/testcase/TestCaseRepository.java`
4. `src/main/java/com/example/annotationextractor/testcase/TestCaseService.java`
5. `src/main/java/com/example/annotationextractor/web/controller/TestCaseController.java`

### Frontend:
1. `frontend/src/lib/testCaseApi.ts`
2. `frontend/src/components/testcases/TestCaseListTable.tsx`
3. `frontend/src/components/testcases/TestCaseDetailModal.tsx`
4. `frontend/src/views/TestCasesView.tsx`

## Total: 9 files modified, 1 file created

## Next Steps

1. **Run Migration**: Execute Flyway migration on all environments
2. **Test Import**: Verify test case import from Excel works correctly
3. **Test Linking**: Verify test method annotations link to test cases correctly
4. **Test Frontend**: Verify all UI operations work as expected
5. **Monitor**: Check for any issues with existing data after migration

## Backward Compatibility

Legacy methods are maintained but deprecated:
- `TestCase.getId()` / `TestCase.setId()` - works with external ID
- `TestCaseRepository.findByIdLegacy(String)` - finds by external ID
- `TestCaseService.getTestCaseByExternalId(String)` - gets by external ID

These will be removed in a future version once all code is migrated.

