# Team Management for Annotation Extractor

This document explains how to use the new team-based organization features in the Annotation Extractor.

## Overview

The system now supports organizing repositories by teams, allowing you to:
- Group repositories by team ownership
- Generate team-based reports
- Track team performance metrics
- Organize test method details by team

## Database Schema Requirements

Before using team features, ensure your database has the following tables:

**Note**: The `team_code` field should match the team codes used in your DevOps system (e.g., Azure DevOps, Jira, GitHub Teams) for seamless integration and reporting consistency.

### Teams Table
```sql
CREATE TABLE teams (
    id SERIAL PRIMARY KEY,
    team_name VARCHAR(255) NOT NULL,
    team_code VARCHAR(50) NOT NULL UNIQUE,
    department VARCHAR(255),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Update Repositories Table
```sql
ALTER TABLE repositories ADD COLUMN team_id INTEGER REFERENCES teams(id);
```

## Quick Start

### 1. Generate CSV Template
```bash
# Windows
run-team-management-demo.bat

# Linux/Mac
./run-team-management-demo.sh
```

This creates a `team-assignments.csv` file with the format:
```csv
git_url,team_name,team_code
https://github.com/company/frontend-app,Frontend Team,FE
https://github.com/company/backend-api,Backend Team,BE
```

### 2. Edit the CSV File
- Replace the example URLs with your actual repository URLs
- Set appropriate team names and owners
- Remove example lines when ready

### 3. Load Team Assignments
```bash
java -cp target/annotation-extractor-1.0.0.jar com.example.annotationextractor.team.TeamManager loadTeamAssignmentsFromCSV team-assignments.csv
```

### 4. Validate Assignments
```bash
java -cp target/annotation-extractor-1.0.0.jar com.example.annotationextractor.team.TeamManager validateTeamAssignments
```

## Report Structure

With team management enabled, your weekly report will now include:

### Team Summary Sheet
- **Team**: Team name
- **Team Code**: DevOps system team code (e.g., FE, BE, MOB)
- **Repositories**: Number of repositories owned by the team
- **Test Classes**: Total test classes across team repositories
- **Test Methods**: Total test methods across team repositories
- **Annotated Methods**: Number of methods with annotations
- **Coverage %**: Team's overall annotation coverage rate
- **Status**: Visual status indicator (🟢 Excellent, 🟡 Good, 🟠 Fair, 🔴 Needs Improvement)

### Test Method Details Sheet
Now includes team information in the first columns:
- **Team**: Team name
- **Team Code**: DevOps system team code
- **Repository**: Repository name
- **Git URL**: Repository URL
- **Class**: Test class name
- **Method**: Test method name
- ... (remaining columns as before)

## Team Manager Utility Class

The `TeamManager` class provides several useful methods:

### Core Methods
- `loadTeamAssignmentsFromCSV(String csvFilePath)` - Load team assignments from CSV
- `assignRepositoryToTeam(String gitUrl, String teamName, String teamCode)` - Assign single repository
- `validateTeamAssignments()` - Check current team assignment status
- `generateCSVTemplate(String outputPath)` - Create sample CSV template

### Helper Methods
- `getTeamRepositoryCounts()` - Get repository counts per team
- `getUnassignedRepositories()` - Find repositories without team assignments

## Benefits

✅ **Clear Ownership**: Teams know which repositories they're responsible for
✅ **Better Reporting**: Team leads can see their coverage metrics
✅ **Easier Navigation**: Users can quickly find their team's data
✅ **Management Insights**: Compare team performance and coverage
✅ **Resource Allocation**: Identify teams needing more testing support

## Example Workflow

1. **Initial Setup**
   ```bash
   # Generate template
   ./run-team-management-demo.sh
   
   # Edit team-assignments.csv with your data
   # Load assignments
   java -cp target/annotation-extractor-1.0.0.jar com.example.annotationextractor.team.TeamManager loadTeamAssignmentsFromCSV team-assignments.csv
   ```

2. **Regular Usage**
   ```bash
   # Validate assignments
   java -cp target/annotation-extractor-1.0.0.jar com.example.annotationextractor.team.TeamManager validateTeamAssignments
   
   # Generate weekly report (now includes team organization)
   java -cp target/annotation-extractor-1.0.0.jar com.example.annotationextractor.reporting.ExcelReportGenerator generateWeeklyReport weekly_report.xlsx
   ```

3. **Team Updates**
   - Edit `team-assignments.csv` when team structure changes
   - Re-run the load command to update assignments
   - Generate new reports to see updated organization

## Troubleshooting

### Common Issues

1. **"No team data available" message**
   - Ensure repositories are assigned to teams
   - Check that `team_id` column exists in repositories table
   - Verify teams table exists and has data

2. **Repository not found warnings**
   - Check that git URLs in CSV match exactly with database
   - Ensure repositories have been scanned at least once

3. **Permission errors**
   - Ensure database user has INSERT/UPDATE permissions on teams and repositories tables

### Validation Commands
```bash
# Check current team status
java -cp target/annotation-extractor-1.0.0.jar com.example.annotationextractor.team.TeamManager validateTeamAssignments

# Check database connection
java -cp target/annotation-extractor-1.0.0.jar com.example.annotationextractor.database.DatabaseConfig
```

## Best Practices

1. **Team Naming**: Use consistent, descriptive team names
2. **Ownership**: Assign team owners who can be contacted for questions
3. **Regular Updates**: Review and update team assignments quarterly
4. **Documentation**: Keep team structure changes documented
5. **Validation**: Always validate assignments after making changes

## Support

For issues or questions about team management features:
1. Check the validation output for clues
2. Verify database schema matches requirements
3. Ensure all repositories have been scanned
4. Check CSV format matches expected structure
