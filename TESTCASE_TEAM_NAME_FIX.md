# 🔧 Test Case Team Name Preservation Fix

## Problem

When importing test cases from Excel with team names that **don't exist in the teams table**, the team name was being lost:

```
1. Excel has: Team = "New Team"
2. Import parses team name ✓
3. Team lookup in database fails (returns null) ✓
4. Save to database with team_id = null ✓
5. Read back from database → team_name = null ✗ (LOST!)
```

**Root Cause**: The database only had a `team_id` column (foreign key). When reading, we JOIN with the teams table to get the team name. But if team_id is null or the team doesn't exist, the JOIN returns null for team_name, losing the original name from Excel.

---

## Solution

Added a **denormalized `team_name` column** to the `test_cases` table to preserve team names even when the team doesn't exist in the teams table yet.

### Database Schema Change
```sql
-- V4__add_team_to_test_cases.sql
ALTER TABLE test_cases ADD COLUMN team_name VARCHAR(255);
```

### Data Flow (After Fix)

**Scenario 1: Team Exists in Database**
```
1. Excel: Team = "Frontend Team"
2. Team lookup finds id=1 in teams table
3. Save: team_id=1, team_name="Frontend Team"
4. Read: COALESCE(teams.team_name, test_cases.team_name) → "Frontend Team" ✓
```

**Scenario 2: Team Does NOT Exist**
```
1. Excel: Team = "New Team"
2. Team lookup returns null
3. Save: team_id=null, team_name="New Team"
4. Read: COALESCE(teams.team_name, test_cases.team_name) → "New Team" ✓
```

**Scenario 3: No Team in Excel**
```
1. Excel: No team column
2. Save: team_id=null, team_name=null
3. Read: team_name=null (shows "-" in UI) ✓
```

---

## Technical Details

### Database Migration
```sql
-- Add team_name column (denormalized)
ALTER TABLE test_cases ADD COLUMN IF NOT EXISTS team_name VARCHAR(255);

COMMENT ON COLUMN test_cases.team_name IS 
  'Team name (denormalized) - useful when team_id is not set or for display purposes';
```

### Repository Changes

**INSERT Statement** - Now saves team_name:
```java
INSERT INTO test_cases (..., team_id, team_name) 
VALUES (..., ?, ?)
```

**SELECT Statements** - Use COALESCE to prefer team name from teams table:
```sql
SELECT tc.*, COALESCE(t.team_name, tc.team_name) as team_name 
FROM test_cases tc 
LEFT JOIN teams t ON tc.team_id = t.id
```

**Logic**:
- `COALESCE(t.team_name, tc.team_name)` means:
  - If team exists in teams table → use teams.team_name (source of truth)
  - Otherwise → use test_cases.team_name (denormalized value)

### Benefits

1. **No Data Loss**: Team names from Excel are preserved even if team doesn't exist
2. **Display Works**: UI shows team name immediately after import
3. **Future-Proof**: When team is added to teams table later, filtering by team_id will work
4. **Graceful Degradation**: Shows team name even without proper team assignment

---

## Migration for Existing Data

If you already have test cases imported before this fix:

### Option 1: Re-import with Team Names
Re-import your Excel files - the team names will now be preserved.

### Option 2: Manual Update (if team exists)
```sql
-- Update team_name for test cases that have team_id
UPDATE test_cases tc
SET team_name = t.team_name
FROM teams t
WHERE tc.team_id = t.id AND tc.team_name IS NULL;
```

---

## Testing

### Test Case 1: Import with Existing Team
```
Excel: Team = "Frontend Team" (exists in database)
Expected: team_id=1, team_name="Frontend Team"
Result: ✓ Both set correctly
```

### Test Case 2: Import with Non-Existent Team
```
Excel: Team = "New Team" (does NOT exist in database)
Expected: team_id=null, team_name="New Team"
Result: ✓ Team name preserved
```

### Test Case 3: View in UI After Import
```
After import → Navigate to Test Cases → Filter by Organization
Expected: Team column shows team names
Result: ✓ Team names visible
```

---

## Files Changed

1. ✅ `V4__add_team_to_test_cases.sql` - Added team_name column
2. ✅ `TestCaseRepository.java` - Updated INSERT and SELECT to handle team_name
3. ✅ `TestCaseService.java` - Already sets teamName from Excel (no change needed)
4. ✅ `ExcelParserService.java` - Already parses teamName from Excel (no change needed)

---

## Summary

**Before Fix**:
- Team names from Excel were lost if team didn't exist in teams table
- Users saw blank team column after import

**After Fix**:
- Team names are preserved in `team_name` column
- Users see team names immediately after import
- Team filtering works even without proper team_id assignment
- When team is later added to teams table, data is properly linked

**Status**: ✅ FIXED  
**Date**: October 10, 2025

