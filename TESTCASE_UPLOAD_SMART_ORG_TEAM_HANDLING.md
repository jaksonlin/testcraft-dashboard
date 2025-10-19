# Test Case Upload - Smart Organization & Team Handling

## Problem

When the database was reset or empty, users couldn't import test cases via Excel because:
1. The organization dropdown in the Preview step loaded existing organizations from the database
2. If no organizations existed (empty database), the dropdown was empty
3. Organization selection was required, blocking the import

### User Confusion
The issue was confusing because there are TWO different organization concepts in the upload wizard:
1. **Mapping Step**: Maps an Excel column to the "Organization" system field (defines WHERE the org value comes from in Excel)
2. **Preview Step**: Selects which organization to assign to ALL test cases (previously overrode Excel values)

This caused confusion when users mapped organization in Excel but then had to select it again in the Preview step.

## Solution

Implemented **smart conditional logic** that respects Excel mappings:

### Priority 1: Excel Column Mapping (Preferred)
If user mapped "Organization" or "Team" columns in the Mapping step → Use those Excel values

### Priority 2: Database Selection + Manual Entry (Fallback)
If user did NOT map those columns → Show selector to choose from database or type new value

### Changes Made

#### Frontend: `PreviewStep.tsx`

##### 1. Smart Detection Logic
```tsx
// Check if organization/team are mapped in Excel
const hasOrganizationMapping = Object.values(mappings).includes('organization');
const hasTeamMapping = Object.values(mappings).includes('team');

// Clear organization/team if they're mapped in Excel (use Excel values instead)
useEffect(() => {
  if (hasOrganizationMapping && organization) {
    onOrganizationChange('');
  }
  if (hasTeamMapping && teamId) {
    onTeamIdChange('');
  }
}, [hasOrganizationMapping, hasTeamMapping, ...]);
```

##### 2. Conditional UI Rendering
```tsx
{hasOrganizationMapping ? (
  // Show green info box - values come from Excel
  <div className="...bg-green-50...">
    ✓ Using values from Excel
  </div>
) : (
  // Show text input with autocomplete
  <input type="text" list="organizations-list" ... />
  <datalist id="organizations-list">
    {organizations.map(org => <option key={org} value={org} />)}
  </datalist>
)}
```

##### 3. Smart Import Button Validation
```tsx
// Only require organization if NOT mapped in Excel
disabled={importing || (!hasOrganizationMapping && !organization)}
```

#### User Feedback Enhancement
Added context-aware help text based on state:
- **If organization mapped in Excel**: Green checkmark + "Using values from Excel"
- **If NOT mapped and empty**: Red error "Organization is required"
- **If NOT mapped and typed (empty DB)**: Blue info "New organization '{name}' will be created"
- **If NOT mapped and typed (existing DB)**: No additional message

## User Experience

### Scenario 1: Excel Has Organization Column (Priority 1 - Preferred)
**Organization values come from Excel - no manual input needed!**

1. User uploads Excel with "Dept" column containing: "Engineering", "QA", "Marketing"
2. **Mapping Step**: User maps "Dept" → "Organization"
3. **Preview Step**: 
   - Organization field shows: ✅ "Using values from Excel" (green box)
   - Import button is enabled (no manual selection required)
4. User clicks Import
5. **Result**: 
   - Test case 1 → organization = "Engineering"
   - Test case 2 → organization = "QA"
   - Test case 3 → organization = "Marketing"
   - Each test case preserves its own organization from Excel!

### Scenario 2: No Excel Mapping - Empty Database
**Manual entry required when starting fresh**

1. User uploads Excel (no organization column mapped)
2. **Mapping Step**: User maps only ID, Title, Steps (org not mapped)
3. **Preview Step**: 
   - Organization field shows: Text input "Type organization name"
   - Import button is disabled until org is entered
4. User types "Engineering"
5. Helper text shows: "New organization 'Engineering' will be created"
6. User clicks Import
7. **Result**: ALL test cases get organization = "Engineering"

### Scenario 3: No Excel Mapping - Database Has Organizations
**Select from existing or create new**

1. User uploads Excel (no organization column mapped)
2. Database already has: ["Engineering", "QA", "Marketing"]
3. **Preview Step**: 
   - Organization field shows: Text input with autocomplete
   - User can click to see dropdown suggestions
4. User can either:
   - **Select existing**: Click "QA" from dropdown
   - **Create new**: Type "Operations"
5. User clicks Import
6. **Result**: ALL test cases get the selected/typed organization

### Scenario 4: Partial Mapping (Org from Excel, Team Manual)
**Mix and match - use Excel for org, manual for team**

1. Excel has "Department" column mapped to "Organization"
2. Excel has NO team column (or user didn't map it)
3. **Preview Step**:
   - Organization: ✅ "Using values from Excel" (green box)
   - Team: Dropdown to select a team (optional)
4. User selects "Frontend Team" from team dropdown
5. User clicks Import
6. **Result**: 
   - Each test case gets its own organization from Excel
   - ALL test cases get teamId = Frontend Team (override)

## Technical Details

### How Combo Box Works
```html
<input type="text" list="organizations-list" ... />
<datalist id="organizations-list">
  <option value="Engineering" />
  <option value="QA" />
  <option value="Dev" />
</datalist>
```

- `<input>` accepts any text input
- `list="organizations-list"` links to the datalist
- `<datalist>` provides autocomplete suggestions
- User can type freely OR select from suggestions
- Works in all modern browsers

### Backend Handling
The backend already handles new organizations correctly:
```java
// TestCaseService.java (lines 104-110)
if (organization != null && !organization.trim().isEmpty()) {
    testCase.setOrganization(organization);
}
else if (testCase.getOrganization() == null || testCase.getOrganization().trim().isEmpty()) {
    testCase.setOrganization("default");
}
```

Organizations are stored as plain strings in the `test_cases` table. No pre-registration is required.

## Benefits

✅ **Respects Excel mappings**: If user mapped org/team in Excel, those values are used  
✅ **Fixes empty database issue**: Users can type organization when database is empty  
✅ **Eliminates confusion**: Clear distinction between Excel mapping vs manual override  
✅ **Flexible fallback**: Can still manually assign org/team if not in Excel  
✅ **Visual clarity**: Green boxes show when Excel values will be used  
✅ **Better UX**: Smart, context-aware behavior based on user's mapping choices  
✅ **No backend changes needed**: Frontend-only fix  

## Future Enhancement Ideas

### Option 1: Smart Default from Excel
If user mapped an "Organization" column in Excel, could extract unique values and suggest them:
```tsx
// Extract unique organizations from Excel preview data
const excelOrganizations = [...new Set(dataRows.map(row => row.organization).filter(Boolean))];
```

### Option 2: Optional Organization
Make organization optional in Preview step if already mapped in Excel:
- If Excel has "Organization" column → Use Excel values
- If Excel doesn't have "Organization" column → Require selection in Preview

### Option 3: Separate Organization Management
Add a dedicated "Organizations" management page to:
- Pre-create organizations
- Set metadata (description, owner, etc.)
- Manage organization hierarchy

For now, the current fix (combo box) is simple and solves the immediate problem.

## Testing

### Test Case 1: Excel Organization Mapping (Primary Path)
1. Create Excel with columns: "ID", "Title", "Steps", "Dept"
   - Row 1: "TC-001", "Login Test", "Open app...", "Engineering"
   - Row 2: "TC-002", "Logout Test", "Click logout...", "QA"
2. Upload Excel
3. **Mapping Step**: Map "Dept" → "Organization"
4. **Preview Step**: Verify organization field shows ✅ "Using values from Excel" (green box)
5. Verify Import button is enabled (no manual org required)
6. Click Import
7. **Verify**: 
   - TC-001 has organization = "Engineering"
   - TC-002 has organization = "QA"

### Test Case 2: Empty Database + No Excel Mapping
1. Reset database (`init-database.bat`)
2. Create Excel with columns: "ID", "Title", "Steps" (NO org column)
3. Upload Excel
4. **Mapping Step**: Map only required fields (no org mapping)
5. **Preview Step**: 
   - Verify organization field shows text input "Type organization name"
   - Verify Import button is disabled
6. Type "MyOrg" in organization field
7. Verify helper text: "New organization 'MyOrg' will be created"
8. Click Import
9. **Verify**: All test cases have organization = "MyOrg"

### Test Case 3: Database Has Orgs + No Excel Mapping
1. Database has existing orgs: ["Engineering", "QA"]
2. Create Excel without org column
3. Upload Excel
4. **Mapping Step**: No org mapping
5. **Preview Step**: 
   - Verify organization field shows text input with autocomplete
   - Click field, verify dropdown shows "Engineering", "QA"
6. Select "QA" from dropdown
7. Click Import
8. **Verify**: All test cases have organization = "QA"

### Test Case 4: Type New Org (Database Not Empty)
1. Database has orgs: ["Engineering", "QA"]
2. Upload Excel without org column
3. **Preview Step**: Type "Operations" (new org)
4. Click Import
5. **Verify**: 
   - All test cases have organization = "Operations"
   - "Operations" now appears in filter dropdowns

### Test Case 5: Mixed Behavior (Org from Excel, Team Manual)
1. Excel has "Department" column with values
2. Upload and map "Department" → "Organization"
3. **Preview Step**:
   - Org field: ✅ "Using values from Excel"
   - Team field: Select "Frontend Team" manually
4. Click Import
5. **Verify**:
   - Each test case has its own organization from Excel
   - All test cases have teamId = Frontend Team

## Related Documentation
- `TESTCASE_UPLOAD_ORG_TEAM_SELECTION.md` - Original org/team selection feature
- `FRONTEND_TEAM_ORG_MAPPING.md` - Excel column mapping for org/team fields
- `TESTCASE_UPLOAD_FEATURE_COMPLETE.md` - Complete upload wizard guide

