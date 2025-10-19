# Test Case Team Backend Filtering Implementation

## Overview
Implemented proper backend filtering for the team dropdown in the test cases view. Previously, team filter options were extracted from loaded test cases (client-side filtering), and the team filter was not being sent to the backend API.

## Changes Made

### Backend Changes

#### 1. TestCaseController.java
- **Added new endpoint**: `GET /api/testcases/teams`
  - Returns a list of all teams with their ID and name
  - Format: `[{id: number, name: string}, ...]`
  - Similar to the existing `/api/testcases/organizations` endpoint

#### 2. TestCaseService.java
- **Added method**: `getAllTeams()`
  - Fetches all teams from the repository
  - Returns `List<Map<String, Object>>` with team id and name

#### 3. TestCaseRepository.java
- **Added method**: `findAllTeams()`
  - Executes SQL query: `SELECT id, team_name FROM teams ORDER BY team_name`
  - Returns teams as a list of maps with id and name fields

### Frontend Changes

#### 1. testCaseApi.ts
- **Added interface**: `Team { id: number; name: string; }`
- **Added function**: `getTeams()`
  - Fetches teams from `GET /api/testcases/teams`
  - Returns `Promise<Team[]>`

#### 2. TestCaseListTable.tsx
- **Updated filter interface**: Changed `team: string` to `teamId: string`
- **Added prop**: `teams: Team[]` to receive teams from parent component
- **Updated team dropdown**:
  - Changed from extracting team names from loaded test cases
  - Now uses teams passed as props from backend
  - Value is now team ID instead of team name
  - Options are populated from backend teams list

#### 3. TestCasesView.tsx
- **Added state**: `teams: Team[]`
- **Updated imports**: Added `Team` interface and `getTeams` function
- **Updated filter state**: Changed `team: ''` to `teamId: ''`
- **Enhanced data loading**: 
  - Now loads both organizations and teams on mount using `Promise.all`
  - Teams are fetched from backend API
- **Updated filter conversion**:
  - Converts `teamId` string to number when sending to backend
  - Now includes: `if (newFilters.teamId) backendFilters.teamId = Number(newFilters.teamId);`
- **Passed teams prop**: Added `teams={teams}` to both `TestCaseListTable` components

## How It Works

### Data Flow
1. **On component mount**:
   - Frontend fetches organizations from `/api/testcases/organizations`
   - Frontend fetches teams from `/api/testcases/teams`
   
2. **When user selects a team**:
   - Team dropdown shows team names from backend
   - Selected value is the team ID (number)
   - UI filter state is updated with teamId
   
3. **Filter conversion**:
   - UI filter `teamId` (string) is converted to number
   - Added to backend filter object
   
4. **API request**:
   - Backend filter with teamId is passed to `getAllTestCases({ ..., teamId: number })`
   - Axios sends it as query parameter: `/api/testcases?teamId=123`
   
5. **Backend processing**:
   - Controller receives `@RequestParam(required = false) Long teamId`
   - Service passes teamId to repository
   - Repository filters: `AND tc.team_id = ?`
   - Returns only test cases for selected team

## Benefits

1. **True backend filtering**: Team filter now properly filters at the database level
2. **Consistent with other filters**: Works the same way as organization, priority, type, and status filters
3. **Better performance**: No need to load all test cases to populate team dropdown
4. **Accurate dropdown options**: Shows all teams in the system, not just teams with loaded test cases
5. **Proper pagination**: Team filtering works correctly with pagination (filtered count is accurate)

## API Documentation

### GET /api/testcases/teams
Returns all teams for filter dropdown.

**Response:**
```json
[
  {"id": 1, "name": "Team Alpha"},
  {"id": 2, "name": "Team Beta"}
]
```

### GET /api/testcases
Existing endpoint now properly uses the teamId query parameter for filtering.

**Query Parameters:**
- `teamId` (optional): Filter by team ID
- `organization` (optional): Filter by organization
- `type` (optional): Filter by type
- `priority` (optional): Filter by priority
- `status` (optional): Filter by status
- `search` (optional): Search in ID or title
- `page` (optional): Page number (0-based)
- `size` (optional): Page size

## Testing

To test the implementation:

1. Start the backend server
2. Start the frontend development server
3. Navigate to Test Cases view
4. Observe team dropdown is populated from backend
5. Select a team from the dropdown
6. Verify only test cases for that team are displayed
7. Check browser network tab to confirm `teamId` parameter is sent in the request
8. Verify pagination count updates correctly with team filter applied

## Files Modified

**Backend:**
- `src/main/java/com/example/annotationextractor/web/controller/TestCaseController.java`
- `src/main/java/com/example/annotationextractor/testcase/TestCaseService.java`
- `src/main/java/com/example/annotationextractor/testcase/TestCaseRepository.java`

**Frontend:**
- `frontend/src/lib/testCaseApi.ts`
- `frontend/src/components/testcases/TestCaseListTable.tsx`
- `frontend/src/views/TestCasesView.tsx`

## Notes

- The backend already supported team filtering via `teamId` parameter, but the frontend was not using it
- Team dropdown values now come from the `teams` table in the database
- Filter conversion happens in `TestCasesView.tsx` before passing to the data hook
- The implementation maintains backward compatibility with existing test cases

