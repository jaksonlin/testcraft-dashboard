# ✅ Test Case Excel Import - Team & Organization Support

## 🎉 Overview

The Excel import feature now supports **automatic mapping and import of Team and Organization fields**!

When you import test cases from Excel, you can now include columns for:
- **Organization** - The organization/department/company that owns the test case
- **Team** - The team name that owns the test case (automatically looked up to team_id)

---

## 📋 Supported Field Mappings

### Organization Field
The system will auto-detect columns named:
- `organization`
- `organisation` (British spelling)
- `org`
- `company`
- `department`
- `dept`

### Team Field  
The system will auto-detect columns named:
- `team`
- `team_name`
- `teamname`
- `owner`
- `owned_by`
- `squad`
- `group`

---

## 🔧 How It Works

### 1. Auto-Detection
When you upload an Excel file, the system:
1. Scans column headers
2. Automatically suggests mappings based on column names
3. Matches variations (case-insensitive, with underscores, etc.)

### 2. Team Name Lookup
When importing test cases with team names:
1. The team name is extracted from the Excel column
2. System looks up the team in the `teams` table (case-insensitive)
3. If found, the `team_id` is automatically set
4. If not found, the team name is still stored for display (can be mapped later)

### 3. Graceful Handling
- **Team not found?** Import continues; team assignment can be done later
- **No team column?** No problem; test cases import without team
- **Invalid team name?** Warning logged but import succeeds

---

## 📝 Example Excel Formats

### Format 1: With Organization and Team
```
| ID      | Title              | Steps              | Organization | Team         | Priority |
|---------|--------------------|--------------------|--------------|--------------|----------|
| TC-001  | User Login Test    | 1. Open app...     | Engineering  | Frontend     | High     |
| TC-002  | Payment Test       | 1. Add to cart...  | Engineering  | Backend      | Critical |
| TC-003  | Email Test         | 1. Send email...   | Marketing    | Growth       | Medium   |
```

### Format 2: With Team Only
```
| Test ID | Test Name          | Test Steps         | Team Name    | Type         |
|---------|--------------------|--------------------|--------------|--------------|
| 001     | User Login         | 1. Navigate...     | QA Team      | Functional   |
| 002     | API Test           | 1. Send request... | API Squad    | Integration  |
```

### Format 3: Alternative Column Names
```
| #  | Description        | Procedure          | Dept         | Owner        |
|----|--------------------|--------------------|--------------|--------------|
| 1  | Login Flow         | Step 1: ...        | IT           | Dev Team     |
| 2  | Checkout Flow      | Step 1: ...        | Sales        | Commerce     |
```

All of these will be automatically detected and mapped!

---

## 🚀 Usage Guide

### Step 1: Prepare Your Excel File
Add columns for organization and/or team:
```excel
ID | Title | Steps | Organization | Team | Priority | Type
```

### Step 2: Upload via UI
1. Go to Test Cases view
2. Click "Upload Test Cases"
3. Select your Excel file
4. Review auto-detected mappings
5. Adjust mappings if needed (system shows suggestions)
6. Click Import

### Step 3: Verify Import
After import:
- Organization appears in the Organization column
- Team name appears in the Team column
- If team was found in database, filtering by team will work immediately

---

## 🔍 Team Lookup Details

### How Team Names are Matched
The system uses **case-insensitive matching**:
- Excel: `"Frontend Team"` → Matches DB: `"frontend team"` or `"Frontend Team"`
- Excel: `"backend"` → Matches DB: `"Backend"` or `"BACKEND"`
- Whitespace is trimmed automatically

### What If Team Doesn't Exist?
The import will:
1. ✅ Continue successfully
2. ✅ Store the team name (for display)
3. ⚠️ Log a warning message
4. ❌ NOT set team_id (can be assigned later via UI or API)

**Example Warning**:
```
Warning: Could not lookup team 'New Team': No team found with that name
```

### Adding Teams for Import
**Before importing**, ensure teams exist in your database:

```sql
-- Add teams to your database
INSERT INTO teams (team_name, team_code, department) 
VALUES 
  ('Frontend Team', 'FE', 'Engineering'),
  ('Backend Team', 'BE', 'Engineering'),
  ('QA Team', 'QA', 'Quality Assurance');
```

Or use the Team Management feature to load teams from CSV.

---

## 📊 Benefits

### For Test Managers
- **Bulk Import**: Import test cases with team assignments in one go
- **Organization**: Automatically organize test cases by team during import
- **No Manual Work**: Team lookup happens automatically

### For Teams
- **Clear Ownership**: Test cases show team assignment from day one
- **Immediate Filtering**: Filter by your team right after import
- **Accurate Data**: Team names matched to actual team records

### For Organizations
- **Multi-Org Support**: Import test cases for different organizations
- **Reporting**: Generate team-based reports immediately after import
- **Traceability**: Know which org/team owns each test case

---

## ⚙️ Technical Details

### Database Schema
```sql
-- Teams table (must exist before import)
CREATE TABLE teams (
    id BIGSERIAL PRIMARY KEY,
    team_name VARCHAR(255) NOT NULL,
    team_code VARCHAR(50) NOT NULL UNIQUE,
    department VARCHAR(255)
);

-- Test cases reference teams
CREATE TABLE test_cases (
    -- ... other fields ...
    organization VARCHAR(100),
    team_id BIGINT REFERENCES teams(id),
    -- ... other fields ...
);
```

### Team Lookup Query
```sql
SELECT id FROM teams 
WHERE LOWER(team_name) = LOWER(?)
```

### Import Process
1. Excel parsed → TestCase objects created
2. Team name extracted if present
3. For each test case with team name:
   - Look up `team_id` by name
   - Set `team_id` if found
   - Keep `team_name` for display
4. Save to database

---

## 🧪 Testing

### Test Cases
- ✅ Import with valid team name → team_id set correctly
- ✅ Import with invalid team name → import succeeds, warning logged
- ✅ Import without team column → import succeeds
- ✅ Import with empty team value → ignored, no error
- ✅ Case-insensitive team matching works
- ✅ Organization field imported correctly
- ✅ Both organization and team can be imported together

### Example Test
```java
// Before import: Create team
Team team = new Team(null, "Frontend Team", "FE", "Engineering", null, null);
teamRepository.save(team);

// Import Excel with column "Team" = "Frontend Team"
importTestCases(excelFile, mappings, ...);

// After import: Verify team_id is set
TestCase tc = testCaseRepository.findByExternalId("TC-001", "Engineering");
assertEquals(team.getId(), tc.getTeamId());
assertEquals("Frontend Team", tc.getTeamName());
```

---

## 🎯 Best Practices

### 1. Pre-Import Checklist
- [ ] Teams exist in database
- [ ] Team names match exactly (case-insensitive OK)
- [ ] Organization names are consistent
- [ ] Excel has required fields (ID, Title, Steps)

### 2. Column Naming
✅ **Good**:
- `Team`, `Team Name`, `Owner`, `Squad`
- `Organization`, `Org`, `Department`

❌ **Avoid**:
- `Team_ID_12345` (too specific)
- `Owner_Email` (not a team name)
- Abbreviated codes without context

### 3. Data Quality
- Use consistent team names across Excel files
- Keep team names updated in database
- Review import warnings for unmatched teams

### 4. After Import
- Check warning messages for unmatched teams
- Filter by team to verify correct assignment
- Update team assignments via UI if needed

---

## 🔄 Migration from Old Imports

### If You Previously Imported Without Teams
You can:

**Option 1: Re-import with teams**
- Add team column to Excel
- Re-import with `replaceExisting: true`
- Existing test cases will be updated with team info

**Option 2: Bulk update via SQL**
```sql
-- Assign all test cases from "default" org to "Frontend Team"
UPDATE test_cases 
SET team_id = (SELECT id FROM teams WHERE team_name = 'Frontend Team')
WHERE organization = 'default';
```

**Option 3: Update via UI**
- Filter test cases without team
- Manually assign teams (future feature)

---

## 📚 Related Documentation

- `TESTCASE_TEAM_FEATURE_COMPLETE.md` - Team association feature overview
- `TESTCASE_EXCEL_SCHEMA_GUIDE.md` - Excel schema and mapping guide
- `TEAM_MANAGEMENT_README.md` - Team management features
- `TESTCASE_UPLOAD_FEATURE_COMPLETE.md` - Test case upload overview

---

## 🎉 Summary

**Excel import now fully supports Organization and Team fields!**

✅ Auto-detects team and organization columns  
✅ Automatically looks up team_id by team name  
✅ Gracefully handles missing teams  
✅ Works with various column name formats  
✅ No breaking changes to existing imports  

Import your test cases with full team context in one simple upload!

**Status**: ✅ COMPLETE  
**Date**: October 10, 2025

