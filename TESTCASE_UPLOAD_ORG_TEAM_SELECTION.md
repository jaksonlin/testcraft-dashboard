# Test Case Upload Organization & Team Selection Feature

## Problem
When uploading test cases via Excel, users had no way to specify which organization and team the test cases belong to. The organization and team were hardcoded to 'default' and 'system', which would cause issues with the new backend team filtering feature.

## Solution
Added organization and team selection fields to the upload wizard's Preview step, allowing users to:
1. **Select Organization (Required)**: Choose which organization these test cases belong to
2. **Select Team (Optional)**: Optionally assign all test cases to a specific team, which overrides any team specified in the Excel file

## Changes Made

### Backend Changes

#### 1. TestCaseController.java
- **Updated endpoint**: `POST /api/testcases/upload/import`
  - Added optional parameter: `@RequestParam(value = "teamId", required = false) Long teamId`
  - Passes teamId to service layer

#### 2. TestCaseService.java
- **Updated method signature**: `importTestCases()` now accepts `Long teamId` parameter
- **Enhanced team assignment logic**:
  - If `teamId` is provided (from UI), it assigns that team to ALL test cases (overrides Excel)
  - If `teamId` is NOT provided, falls back to looking up team by name from Excel
  - This gives users flexibility to either bulk-assign a team or use per-row teams from Excel

### Frontend Changes

#### 1. testCaseApi.ts
- **Updated function**: `importTestCases()` now accepts optional `teamId?: number` parameter
- Only sends teamId to backend if it's provided

#### 2. useTestCaseUpload.ts (Upload Hook)
- **Added state**:
  - `organization: string` - Selected organization
  - `teamId: string` - Selected team ID
  - `createdBy: string` - Creator (defaults to 'system')
- **Added validation**: Import button disabled and alerts if organization not selected
- **Updated import call**: Passes organization and teamId to API

#### 3. TestCaseUploadWizard.tsx
- **Passes new props** to PreviewStep:
  - `organization`, `teamId`
  - `onOrganizationChange`, `onTeamIdChange`

#### 4. PreviewStep.tsx
- **Added imports**: `Building`, `Users` icons, API functions
- **Added state**: 
  - `organizations: string[]` - Loaded from backend
  - `teams: Team[]` - Loaded from backend
  - `loading: boolean` - Loading state
- **Added UI section**: "Test Case Metadata" with two dropdowns
  - Organization dropdown (required, shows error if not selected)
  - Team dropdown (optional, with helpful description)
- **Loads data on mount**: Fetches organizations and teams from backend
- **Disabled import button**: If organization is not selected

#### 5. types.ts
- **Updated PreviewStepProps** to include:
  - `organization: string`
  - `teamId: string`
  - `onOrganizationChange: (value: string) => void`
  - `onTeamIdChange: (value: string) => void`
- **Fixed type imports**: Properly imported `ExcelPreviewResponse` and `ImportResponse`

## User Experience

### Upload Flow
1. **Upload Step**: User selects Excel file
2. **Mapping Step**: User maps columns to system fields
3. **Preview Step**: 
   - ✨ **NEW**: User selects Organization (required)
   - ✨ **NEW**: User optionally selects Team
   - User reviews mapped data
   - Import button disabled until organization is selected
4. **Complete Step**: Shows import results

### Organization Selection
- **Required field** marked with red asterisk
- Shows error message if not selected
- Populated from backend organizations
- Applies to all test cases in the upload

### Team Selection
- **Optional field** marked as "(optional)"
- Two options:
  1. **Select a team**: Assigns that team to ALL test cases (overrides Excel team column)
  2. **Leave empty**: Teams will be taken from Excel team column (if present) or left unassigned
- Helper text explains the behavior
- Populated from backend teams

## Technical Details

### Data Flow
1. User reaches Preview step
2. Frontend loads organizations and teams from:
   - `GET /api/testcases/organizations`
   - `GET /api/testcases/teams`
3. User makes selections
4. On import, frontend sends:
   - `organization` (required, validated)
   - `teamId` (optional, only if selected)
5. Backend processes:
   - Sets organization on all test cases
   - If teamId provided → sets that team on all test cases
   - If teamId NOT provided → looks up teams from Excel team column

### Backend Assignment Logic (Consistent Override Pattern)
```java
// Organization: UI selection always overrides Excel (if provided)
if (organization != null && !organization.trim().isEmpty()) {
    testCase.setOrganization(organization);
}

// Team: UI selection always overrides Excel (if provided)
if (teamId != null) {
    testCase.setTeamId(teamId);
}
// Fallback to Excel team if UI didn't specify
else if (testCase.getTeamName() != null && !testCase.getTeamName().trim().isEmpty()) {
    Long lookupTeamId = testCaseRepository.findTeamIdByName(testCase.getTeamName());
    if (lookupTeamId != null) {
        testCase.setTeamId(lookupTeamId);
    }
}
```

**Consistent Override Pattern:**
- UI selection **always takes priority** (both org and team)
- Excel values used only as fallback when UI doesn't specify
- Clear, predictable behavior

## Benefits

1. **Fixes the filtering issue**: Test cases are properly associated with organizations and teams
2. **User control**: Users explicitly choose where test cases belong
3. **Flexibility**: 
   - Can bulk-assign a team (common use case)
   - Can use per-row teams from Excel
   - Can leave unassigned
4. **Validation**: Prevents import without organization
5. **Better UX**: Clear labels, helpful text, and visual feedback

## API Documentation

### POST /api/testcases/upload/import
Import test cases from Excel file.

**Parameters:**
- `file` (required): MultipartFile - Excel file to import
- `mappings` (required): String - JSON string of column mappings
- `headerRow` (optional, default=0): int - Header row index
- `dataStartRow` (required): int - First data row index
- `replaceExisting` (optional, default=true): boolean - Replace on duplicate
- `createdBy` (optional, default='system'): String - Creator name
- `organization` (optional, default='default'): String - Organization name
- `teamId` (optional): Long - **NEW** - Team ID to assign to all test cases

**Returns:**
```json
{
  "success": true,
  "created": 50,
  "updated": 10,
  "skipped": 0,
  "imported": 60,
  "errors": [],
  "suggestions": []
}
```

## Migration Notes

- **Backward compatible**: teamId is optional, existing imports without it still work
- **No database changes**: Uses existing team_id column in test_cases table
- **No breaking changes**: All existing APIs remain unchanged

## Testing Checklist

- [ ] Upload Excel with organization selected
- [ ] Upload Excel with organization + team selected
- [ ] Upload Excel with organization only (teams from Excel)
- [ ] Try to import without selecting organization (should show error)
- [ ] Verify teamId overrides Excel team column when selected
- [ ] Verify Excel team column works when teamId not selected
- [ ] Check filtering works after import with assigned team
- [ ] Test with empty organizations list
- [ ] Test with empty teams list

## Files Modified

**Backend:**
- `src/main/java/com/example/annotationextractor/web/controller/TestCaseController.java`
- `src/main/java/com/example/annotationextractor/testcase/TestCaseService.java`

**Frontend:**
- `frontend/src/lib/testCaseApi.ts`
- `frontend/src/components/testcases/upload/useTestCaseUpload.ts`
- `frontend/src/components/testcases/upload/TestCaseUploadWizard.tsx`
- `frontend/src/components/testcases/upload/PreviewStep.tsx`
- `frontend/src/components/testcases/upload/types.ts`

## Related Features

- **Test Case Team Filtering**: This feature enables proper team-based filtering on the test cases list view
- **Team Management**: Teams are managed via the Teams view and populated in the dropdown
- **Organization Management**: Organizations are extracted from existing test cases

## Screenshots Description

### Preview Step - Before
- No organization or team selection
- Hardcoded to 'default' and 'system'

### Preview Step - After
- "Test Case Metadata" section with two dropdowns
- Organization dropdown (required, red asterisk)
- Team dropdown (optional, with helper text)
- Import button disabled until organization selected

