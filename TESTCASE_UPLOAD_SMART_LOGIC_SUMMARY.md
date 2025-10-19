# Test Case Upload - Smart Organization & Team Logic (Quick Summary)

## What Changed?

Implemented **intelligent conditional logic** for organization and team handling in the Excel upload wizard.

## The Smart Logic

### Priority 1: Excel Column Mapping ✅ (Preferred)
```
If user mapped "Organization" in Excel → Use Excel values per row
If user mapped "Team" in Excel → Use Excel values per row
```

### Priority 2: Manual Selection/Entry 📝 (Fallback)
```
If user did NOT map "Organization" → Show input field (select from DB or type new)
If user did NOT map "Team" → Show dropdown (select from DB or leave empty)
```

## Visual Behavior

### When Organization is Mapped in Excel
```
┌────────────────────────────────────────┐
│ Organization *                         │
│ ┌────────────────────────────────────┐ │
│ │ ✓ Using values from Excel          │ │
│ └────────────────────────────────────┘ │
│ Organization values will be taken     │
│ from your Excel column mapping        │
└────────────────────────────────────────┘
```

### When Organization is NOT Mapped
```
┌────────────────────────────────────────┐
│ Organization *                         │
│ ┌────────────────────────────────────┐ │
│ │ [Type organization name...]        │ │
│ └────────────────────────────────────┘ │
│ New organization "xxx" will be created│
└────────────────────────────────────────┘
```

## Key Benefits

✅ **Respects user's Excel mappings** - No more confusion about overriding  
✅ **Fixes empty database issue** - Can type new org when DB is empty  
✅ **Smart validation** - Import button only requires org if NOT mapped in Excel  
✅ **Clear visual feedback** - Green checkmark shows when Excel values will be used  
✅ **Flexible** - Mix and match (e.g., org from Excel, team manual override)  

## Example Scenarios

### Scenario A: User Maps Organization in Excel
- Excel: "Dept" column → Engineering, QA, Marketing
- Mapping: "Dept" → "Organization"
- **Preview**: Green box "✓ Using values from Excel"
- **Result**: Each test case gets its own org from Excel row

### Scenario B: User Doesn't Map Organization (Empty DB)
- Excel: No org column
- **Preview**: Text input "Type organization name"
- User types: "MyCompany"
- **Result**: All test cases get org = "MyCompany"

### Scenario C: Mix and Match
- Excel: "Dept" mapped → "Organization"
- Excel: No team column
- **Preview**: 
  - Org: ✓ "Using values from Excel"
  - Team: Dropdown - user selects "Frontend Team"
- **Result**: 
  - Each test case gets org from Excel
  - All test cases get same team (Frontend Team)

## Files Changed

- ✏️ `frontend/src/components/testcases/upload/PreviewStep.tsx`
  - Added smart detection: `hasOrganizationMapping`, `hasTeamMapping`
  - Conditional UI rendering (green box vs input field)
  - Smart validation (only require org if not mapped)

## Documentation

See `TESTCASE_UPLOAD_SMART_ORG_TEAM_HANDLING.md` for full details, test cases, and technical implementation.

---

**Status**: ✅ Complete - Ready for testing

