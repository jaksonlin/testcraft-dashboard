# Test Case Upload - Smart Logic Flow Diagram

## Complete Upload Flow with Smart Organization/Team Handling

```
┌─────────────────────────────────────────────────────────────────────┐
│                         STEP 1: UPLOAD                              │
│                     User selects Excel file                         │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       STEP 2: MAPPING                               │
│                                                                     │
│  User maps Excel columns to system fields:                         │
│  ┌───────────────────────────────────────────────────────────┐    │
│  │ Excel Column      →    System Field                       │    │
│  │ ─────────────────────────────────────────────────────     │    │
│  │ "Test ID"         →    ID *                               │    │
│  │ "Description"     →    Title *                            │    │
│  │ "Steps"           →    Steps *                            │    │
│  │ "Dept"            →    Organization  ◄─── KEY DECISION!   │    │
│  │ "Owner Team"      →    Team          ◄─── KEY DECISION!   │    │
│  └───────────────────────────────────────────────────────────┘    │
│                                                                     │
│  🔑 If user maps "Organization" or "Team" here,                   │
│     those Excel values will be used in Preview step!               │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      STEP 3: PREVIEW                                │
│                                                                     │
│  Smart Logic Checks Mappings:                                      │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │ hasOrganizationMapping = "organization" in mappings?        │  │
│  │ hasTeamMapping = "team" in mappings?                        │  │
│  └─────────────────────────────────────────────────────────────┘  │
│                                                                     │
│  ┌─ Organization Field ──────────────────────────────────────┐    │
│  │                                                            │    │
│  │  IF mapped in Excel:                                       │    │
│  │  ┌──────────────────────────────────────────────────┐    │    │
│  │  │ ✓ Using values from Excel     [GREEN BOX]       │    │    │
│  │  └──────────────────────────────────────────────────┘    │    │
│  │  → Each row gets its own organization from Excel          │    │
│  │                                                            │    │
│  │  IF NOT mapped in Excel:                                   │    │
│  │  ┌──────────────────────────────────────────────────┐    │    │
│  │  │ [Type organization name...] [TEXT INPUT]        │    │    │
│  │  └──────────────────────────────────────────────────┘    │    │
│  │  → All rows get the same organization (typed value)       │    │
│  │                                                            │    │
│  └────────────────────────────────────────────────────────────┘    │
│                                                                     │
│  ┌─ Team Field ──────────────────────────────────────────────┐    │
│  │                                                            │    │
│  │  IF mapped in Excel:                                       │    │
│  │  ┌──────────────────────────────────────────────────┐    │    │
│  │  │ ✓ Using values from Excel     [GREEN BOX]       │    │    │
│  │  └──────────────────────────────────────────────────┘    │    │
│  │  → Each row gets its own team from Excel                  │    │
│  │                                                            │    │
│  │  IF NOT mapped in Excel:                                   │    │
│  │  ┌──────────────────────────────────────────────────┐    │    │
│  │  │ [Select team...] [DROPDOWN] (optional)          │    │    │
│  │  └──────────────────────────────────────────────────┘    │    │
│  │  → All rows get the same team (selected value)            │    │
│  │                                                            │    │
│  └────────────────────────────────────────────────────────────┘    │
│                                                                     │
│  Import Button Validation:                                         │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │ Enabled if:                                                 │  │
│  │  - Organization is mapped in Excel, OR                      │  │
│  │  - Organization is typed in input field                     │  │
│  └─────────────────────────────────────────────────────────────┘  │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     STEP 4: IMPORT                                  │
│                                                                     │
│  Backend receives:                                                  │
│  - Excel file                                                       │
│  - Column mappings                                                  │
│  - organization (empty string if mapped in Excel)                   │
│  - teamId (null if mapped in Excel)                                 │
│                                                                     │
│  Backend logic:                                                     │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │ For each test case row:                                     │  │
│  │                                                              │  │
│  │   // Organization priority                                  │  │
│  │   if (organization != null && !organization.isEmpty()) {    │  │
│  │     testCase.setOrganization(organization); // UI override  │  │
│  │   }                                                          │  │
│  │   else if (testCase.getOrganization() == null) {            │  │
│  │     testCase.setOrganization("default");    // Fallback    │  │
│  │   }                                                          │  │
│  │   // else: keep Excel organization value                    │  │
│  │                                                              │  │
│  │   // Team priority                                          │  │
│  │   if (teamId != null) {                                     │  │
│  │     testCase.setTeamId(teamId);            // UI override  │  │
│  │   }                                                          │  │
│  │   else if (testCase.getTeamName() != null) {                │  │
│  │     Long lookupId = findTeamIdByName(...);                  │  │
│  │     if (lookupId != null) {                                 │  │
│  │       testCase.setTeamId(lookupId);        // Excel lookup │  │
│  │     }                                                        │  │
│  │   }                                                          │  │
│  │   // else: no team assigned                                 │  │
│  │                                                              │  │
│  └─────────────────────────────────────────────────────────────┘  │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      STEP 5: COMPLETE                               │
│               Show import results and close wizard                  │
└─────────────────────────────────────────────────────────────────────┘
```

## Decision Tree

```
User uploads Excel
       │
       ├─ Has "Organization" column in Excel?
       │  │
       │  ├─ YES → User maps it in Mapping step
       │  │        │
       │  │        └─ Preview step: ✓ "Using values from Excel"
       │  │           Backend: Uses Excel org values per row
       │  │
       │  └─ NO → User doesn't map it
       │           │
       │           └─ Preview step: Shows text input
       │              │
       │              ├─ DB empty? → User types new org name
       │              │                Backend: All rows get typed org
       │              │
       │              └─ DB has orgs? → User selects/types org
       │                                 Backend: All rows get selected org
       │
       ├─ Has "Team" column in Excel?
       │  │
       │  ├─ YES → User maps it in Mapping step
       │  │        │
       │  │        └─ Preview step: ✓ "Using values from Excel"
       │  │           Backend: Uses Excel team values per row (via lookup)
       │  │
       │  └─ NO → User doesn't map it
       │           │
       │           └─ Preview step: Shows dropdown (optional)
       │              │
       │              ├─ User selects team? → Backend: All rows get that team
       │              └─ User leaves empty? → Backend: No team assigned
       │
       └─ Import Button
          │
          ├─ Enabled if: Org mapped in Excel OR Org typed in input
          └─ Disabled if: No org mapping AND input is empty
```

## Key Principles

1. **Excel Mapping Takes Priority**: If user maps org/team in Excel, those values are used
2. **Manual Override is Fallback**: Only show manual selectors if not mapped in Excel
3. **Visual Clarity**: Green checkmarks clearly show when Excel values will be used
4. **Smart Validation**: Import button knows when org is already handled by Excel
5. **Flexible**: Can mix and match (org from Excel, team manual override)

## Examples

### Example 1: Full Excel Mapping
```
Excel columns: ID, Title, Steps, Dept, Owner
Mapping: Dept→Organization, Owner→Team
Preview: Both show ✓ "Using values from Excel"
Result: Each row gets its own org + team from Excel
```

### Example 2: Partial Excel Mapping
```
Excel columns: ID, Title, Steps, Dept
Mapping: Dept→Organization (no team mapping)
Preview: Org shows ✓ green box, Team shows dropdown
User selects: "Frontend Team" from dropdown
Result: Each row gets org from Excel, all rows get Frontend Team
```

### Example 3: No Excel Mapping (Empty DB)
```
Excel columns: ID, Title, Steps (no org/team)
Mapping: Only required fields
Preview: Org shows text input (empty DB)
User types: "MyCompany"
Result: All rows get org = "MyCompany", no team
```

### Example 4: No Excel Mapping (DB Has Data)
```
Excel columns: ID, Title, Steps (no org/team)
Database has: ["Engineering", "QA", "Marketing"]
Preview: Org shows text input with autocomplete
User selects: "QA" from autocomplete dropdown
Result: All rows get org = "QA", no team
```

