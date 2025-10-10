# Test Case INSERT vs UPDATE Logic

## 🎯 How the System Decides: INSERT or UPDATE?

The test case system uses a **composite unique key** to determine whether a test case should be inserted (new) or updated (existing).

---

## 🔑 The Unique Key: `external_id` + `organization`

### Database Constraint
```sql
-- From V3__refactor_test_case_id_to_internal.sql
ALTER TABLE test_cases 
  ADD CONSTRAINT test_cases_external_id_org_unique 
  UNIQUE (external_id, organization);
```

### What This Means
A test case is considered **unique** by the combination of:
- **`external_id`** - The test case ID from your Excel file (e.g., "TC-001", "TEST-1234")
- **`organization`** - The organization name (set during import, default: "default")

**Same external_id** can exist in **different organizations** ✓  
**Same external_id** cannot exist in **same organization** ✗

---

## 📊 Decision Logic

### Scenario 1: New Test Case (INSERT)
```
Excel Import:
  external_id = "TC-001"
  organization = "Engineering"

Database Check:
  SELECT internal_id FROM test_cases 
  WHERE external_id = 'TC-001' AND organization = 'Engineering'
  
Result: Not found → INSERT ✓

Action: New record created with auto-generated internal_id
```

### Scenario 2: Existing Test Case (UPDATE)
```
Excel Import:
  external_id = "TC-001"
  organization = "Engineering"

Database Check:
  SELECT internal_id FROM test_cases 
  WHERE external_id = 'TC-001' AND organization = 'Engineering'
  
Result: Found (internal_id = 5) → UPDATE ✓

Action: Existing record updated, internal_id stays the same
```

### Scenario 3: Same ID, Different Organization (INSERT)
```
Org A Import:
  external_id = "TC-001"
  organization = "Frontend Team"
  → INSERT (new record, internal_id = 1)

Org B Import:
  external_id = "TC-001"  (same ID!)
  organization = "Backend Team"  (different org!)
  → INSERT (new record, internal_id = 2) ✓

Result: Two separate test cases with same external_id
```

---

## 💻 Code Implementation

### Repository: `TestCaseRepository.save()`

```java
/**
 * Internal save method with connection
 * Uses external_id + organization for conflict resolution
 * Returns true if new record was created, false if existing record was updated
 */
private boolean save(Connection conn, TestCase testCase) throws SQLException {
    // Step 1: Check if test case already exists
    boolean exists = false;
    String checkSql = "SELECT internal_id FROM test_cases 
                       WHERE external_id = ? AND organization = ?";
    try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
        checkStmt.setString(1, testCase.getExternalId());
        checkStmt.setString(2, testCase.getOrganization());
        try (ResultSet rs = checkStmt.executeQuery()) {
            exists = rs.next();
        }
    }
    
    // Step 2: Perform UPSERT using PostgreSQL's ON CONFLICT
    String sql = "INSERT INTO test_cases (...) VALUES (...) " +
                 "ON CONFLICT (external_id, organization) DO UPDATE SET " +
                 "title = EXCLUDED.title, steps = EXCLUDED.steps, ... " +
                 "RETURNING internal_id";
    
    // Step 3: Execute and return
    return !exists; // true = INSERT, false = UPDATE
}
```

### PostgreSQL UPSERT (INSERT ... ON CONFLICT)

```sql
INSERT INTO test_cases (
    external_id, title, steps, ... organization
) VALUES (
    'TC-001', 'Login Test', '1. Open app...', ... 'Engineering'
)
ON CONFLICT (external_id, organization) DO UPDATE SET
    title = EXCLUDED.title,
    steps = EXCLUDED.steps,
    priority = EXCLUDED.priority,
    team_id = EXCLUDED.team_id,
    team_name = EXCLUDED.team_name,
    updated_date = CURRENT_TIMESTAMP
RETURNING internal_id;
```

**How it works**:
1. Try to INSERT with new values
2. If conflict on (external_id, organization) → UPDATE instead
3. Return the internal_id (new or existing)

---

## 📈 Import Results

### Service Layer: `TestCaseService.saveAll()`

The service tracks INSERT vs UPDATE counts:

```java
public SaveResult saveAll(List<TestCase> testCases) {
    int created = 0;
    int updated = 0;
    
    for (TestCase testCase : testCases) {
        boolean isNew = save(conn, testCase);
        if (isNew) {
            created++;
        } else {
            updated++;
        }
    }
    
    return new SaveResult(created, updated);
}
```

### Import Response
After import, you get:
```json
{
  "success": true,
  "imported": 10,
  "created": 7,    // New test cases
  "updated": 3,    // Existing test cases updated
  "skipped": 0
}
```

---

## 🔍 Examples

### Example 1: First Import
```
Excel File: 5 test cases
Organization: "QA Team"

Result:
  created: 5   ← All new
  updated: 0
  total: 5
```

### Example 2: Re-import Same File
```
Excel File: Same 5 test cases (modified steps)
Organization: "QA Team"

Result:
  created: 0
  updated: 5   ← All existing, data refreshed
  total: 5
```

### Example 3: Import with New + Existing
```
Excel File: 10 test cases
  - 7 existing IDs (will be updated)
  - 3 new IDs (will be inserted)
Organization: "QA Team"

Result:
  created: 3
  updated: 7
  total: 10
```

### Example 4: Multi-Org Import
```
Import 1:
  external_id: "TC-001"
  organization: "Frontend"
  Result: INSERT (internal_id = 1)

Import 2:
  external_id: "TC-001"  (same!)
  organization: "Backend"  (different!)
  Result: INSERT (internal_id = 2)

Database now has TWO test cases with external_id "TC-001"
```

---

## ⚠️ Important Considerations

### 1. Organization Matters!
```
Same ID + Same Org = UPDATE
Same ID + Different Org = INSERT (separate records)
```

### 2. Default Organization
If you don't specify an organization during import:
```java
// TestCaseService.importTestCases()
testCase.setOrganization(organization); // defaults to "default"
```

### 3. What Gets Updated?
When updating, these fields are refreshed:
- ✅ title, steps, setup, teardown, expected_result
- ✅ priority, type, status
- ✅ tags, requirements
- ✅ custom_fields
- ✅ team_id, team_name
- ✅ updated_date
- ❌ **NOT updated**: internal_id, external_id, organization, created_date, created_by

### 4. Internal ID is Permanent
The `internal_id` (auto-generated primary key) **never changes**:
- First import: internal_id = 1
- Re-import same test case: internal_id = 1 (stays same)
- Used for foreign keys, URLs, references

---

## 🎛️ Controlling Behavior

### During Import
You can control whether to replace existing test cases:

```typescript
// Frontend API call
await importTestCases(
  file,
  mappings,
  headerRow,
  dataStartRow,
  replaceExisting: true,  // ← Controls update behavior
  createdBy: 'user@example.com',
  organization: 'Engineering'
);
```

- `replaceExisting = true` → UPDATE existing test cases (default)
- `replaceExisting = false` → Currently still updates (could be enhanced)

---

## 📋 Best Practices

### 1. Use Consistent External IDs
✅ Good:
- TC-001, TC-002, TC-003 (consistent format)
- Keep IDs stable across imports

❌ Bad:
- Changing IDs between imports
- Non-unique IDs within same Excel file

### 2. Use Meaningful Organizations
✅ Good:
- "Frontend Team", "Backend Team", "QA Team"
- One organization per team/department

❌ Bad:
- Always using "default"
- Mixing different teams in same organization

### 3. Regular Re-imports
- Re-import to update test case details
- External IDs stay same → data gets updated
- No duplicates created

### 4. Check Import Results
```typescript
const result = await importTestCases(...);
console.log(`Created: ${result.created}, Updated: ${result.updated}`);
```

---

## 🔄 Data Flow Diagram

```
┌─────────────────────────────────────────────────────┐
│ Excel Import                                        │
│ external_id = "TC-001"                              │
│ organization = "Engineering"                        │
│ title = "Updated Login Test"                       │
└───────────────────┬─────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────┐
│ Database Check                                      │
│ SELECT internal_id FROM test_cases                  │
│ WHERE external_id = 'TC-001'                        │
│   AND organization = 'Engineering'                  │
└───────────┬───────────────────────────┬─────────────┘
            │                           │
    Found? YES                      Found? NO
            │                           │
            ▼                           ▼
┌─────────────────────┐    ┌─────────────────────────┐
│ UPDATE              │    │ INSERT                  │
│ - Keep internal_id  │    │ - Generate internal_id  │
│ - Update fields     │    │ - Set all fields        │
│ - Bump updated_date │    │ - Set created_date      │
└─────────────────────┘    └─────────────────────────┘
            │                           │
            └───────────┬───────────────┘
                        ▼
            ┌─────────────────────┐
            │ Return SaveResult   │
            │ created: X          │
            │ updated: Y          │
            └─────────────────────┘
```

---

## 📚 Summary

**Unique Key**: `external_id` + `organization`

**INSERT** when: Test case with this external_id + organization doesn't exist

**UPDATE** when: Test case with this external_id + organization already exists

**Mechanism**: PostgreSQL `ON CONFLICT` clause (UPSERT)

**Tracking**: Service returns counts of created vs updated records

**Date**: October 10, 2025

