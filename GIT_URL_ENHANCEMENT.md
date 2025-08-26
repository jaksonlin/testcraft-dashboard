# Git URL Enhancement for Excel Reports

## Overview

This enhancement adds repository Git URLs to the Excel reports and database storage, providing better traceability and reference information for scanned repositories.

## Changes Implemented

### 1. Database Schema
- **Already existed**: The `repositories` table already had a `git_url` column (VARCHAR(500))
- **No schema changes needed**: The database was already prepared for this enhancement

### 2. Data Model Updates

#### RepositoryTestInfo Class
- **Added field**: `private String gitUrl;`
- **Added constructor**: `RepositoryTestInfo(String repositoryName, String repositoryPath, String gitUrl)`
- **Added getter/setter**: `getGitUrl()` and `setGitUrl(String gitUrl)`
- **Updated toString()**: Now includes git URL in string representation

### 3. Data Persistence Updates

#### DataPersistenceService
- **Updated SQL**: Modified `persistRepository()` method to include `git_url` column
- **Parameter mapping**: Added git URL as the 3rd parameter in INSERT/UPDATE statements
- **Conflict resolution**: Git URL is updated when repository already exists

**Before:**
```sql
INSERT INTO repositories (repository_name, repository_path, total_test_classes, ...)
VALUES (?, ?, ?, ...)
```

**After:**
```sql
INSERT INTO repositories (repository_name, repository_path, git_url, total_test_classes, ...)
VALUES (?, ?, ?, ?, ...)
```

### 4. Repository Scanning Updates

#### RepositoryHubScanner
- **Git URL assignment**: When scanning repositories in temporary clone mode, the git URL is automatically set for each repository
- **Data flow**: Git URL from the repository list is propagated to the scan results

```java
// Set git URL for all repositories in the scan summary
for (RepositoryTestInfo repo : scanSummary.getRepositories()) {
    repo.setGitUrl(gitUrl);
}
```

#### RepositoryScanner
- **Git URL extraction**: Added method to extract git URL from existing repository directories
- **Fallback support**: For repositories scanned from local directories, attempts to read git URL from `.git/config` file

```java
private static String extractGitUrlFromRepository(Path repoPath) {
    try {
        Path gitConfigPath = repoPath.resolve(".git").resolve("config");
        if (Files.exists(gitConfigPath)) {
            List<String> lines = Files.readAllLines(gitConfigPath);
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("url = ")) {
                    return line.substring(6).trim();
                }
            }
        }
    } catch (IOException e) {
        // Ignore errors reading git config
    }
    return null;
}
```

### 5. Excel Report Updates

#### Repository Details Sheet
- **New column**: Added "Git URL" column between "Path" and "Test Classes"
- **Column width**: Set to 6000 (wider) to accommodate long URLs
- **Data population**: Git URL is now displayed in the repository details

**Updated headers:**
```java
String[] headers = {"Repository", "Path", "Git URL", "Test Classes", "Test Methods", "Annotated", "Coverage %", "Last Scan"};
```

**Column widths:**
```java
sheet.setColumnWidth(0, 3000);  // Repository
sheet.setColumnWidth(1, 4000);  // Path
sheet.setColumnWidth(2, 6000);  // Git URL - wider for long URLs
sheet.setColumnWidth(3, 5000);  // Test Classes
// ... other columns
```

## Data Flow

### 1. Repository Hub Scanning (--temp-clone mode)
```
Repository List File → Git URL → Clone Repository → Scan → Set Git URL → Persist to DB → Generate Excel Report
```

### 2. Local Directory Scanning
```
Local Directory → Find Git Repos → Extract Git URL from .git/config → Scan → Set Git URL → Persist to DB → Generate Excel Report
```

### 3. Database Storage
```
RepositoryTestInfo → DataPersistenceService → repositories table (git_url column)
```

### 4. Excel Report Generation
```
Database Query → repositories table → Excel Report (Git URL column)
```

## Benefits

### 1. **Traceability**
- Easy to identify which Git repository each scan result came from
- Direct links to source repositories for further investigation

### 2. **Reference Information**
- Repository URLs available in reports for team members
- Quick access to source code for test analysis

### 3. **Audit Trail**
- Complete record of scanned repositories with their origins
- Historical tracking of repository scanning activities

### 4. **Team Collaboration**
- Developers can easily find and access the repositories mentioned in reports
- Better integration with development workflows

## Usage Examples

### 1. **Repository Hub Scanning**
```bash
java -jar target/annotation-extractor-1.0.0.jar D:\testlab\ D:\testlab\testrepo.txt --temp-clone --db-pass postgres
```

**Result**: Excel report will include Git URLs from the repository list file.

### 2. **Local Directory Scanning**
```bash
java -cp target/classes com.example.annotationextractor.TestCollectionRunner /path/to/repositories --generate-report
```

**Result**: Excel report will include Git URLs extracted from local `.git/config` files.

## Excel Report Layout

### Repository Details Sheet
| Column | Content | Width |
|--------|---------|-------|
| 1 | Repository | 3000 |
| 2 | Path | 4000 |
| 3 | **Git URL** | **6000** |
| 4 | Test Classes | 5000 |
| 5 | Test Methods | 3000 |
| 6 | Annotated | 3000 |
| 7 | Coverage % | 3000 |
| 8 | Last Scan | 3000 |

## Database Schema

### repositories table
```sql
CREATE TABLE repositories (
    id BIGSERIAL PRIMARY KEY,
    repository_name VARCHAR(255) NOT NULL,
    repository_path VARCHAR(500) NOT NULL,
    git_url VARCHAR(500),           -- ← Git URL column
    git_branch VARCHAR(100) DEFAULT 'main',
    technology_stack TEXT[],
    team_ownership VARCHAR(255),
    first_scan_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_scan_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_test_classes INT DEFAULT 0,
    total_test_methods INT DEFAULT 0,
    total_annotated_methods INT DEFAULT 0,
    annotation_coverage_rate DECIMAL(5,2) DEFAULT 0.00,
    UNIQUE(repository_name, repository_path)
);
```

## Testing

### 1. **Compilation**
- ✅ `mvn clean compile` - SUCCESS
- ✅ All source files compile without errors

### 2. **Unit Tests**
- ✅ `mvn test` - All 58 tests passed
- ✅ No regression in existing functionality

### 3. **Integration**
- ✅ Database persistence works with git URL
- ✅ Excel report generation includes git URL column
- ✅ Git URL extraction from local repositories works

## Backward Compatibility

### 1. **Database**
- ✅ Existing databases work (git_url column already exists)
- ✅ New scans will populate git_url column
- ✅ Old data remains accessible

### 2. **API**
- ✅ Existing constructors still work
- ✅ New constructors available for git URL support
- ✅ All existing methods continue to function

### 3. **Reports**
- ✅ Existing reports still generate
- ✅ New reports include git URL information
- ✅ No breaking changes to report structure

## Future Enhancements

### 1. **Git Branch Information**
- Extract and store current branch information
- Track branch changes over time

### 2. **Commit Information**
- Store latest commit hash and message
- Track repository changes between scans

### 3. **Repository Metadata**
- Extract repository description, topics, language
- Store repository size and last update information

### 4. **Enhanced URL Handling**
- Support for different Git hosting platforms
- URL validation and normalization
- Support for Git submodules

## Files Modified

1. **`src/main/java/com/example/annotationextractor/RepositoryTestInfo.java`**
   - Added gitUrl field, getter, setter, and constructor

2. **`src/main/java/com/example/annotationextractor/database/DataPersistenceService.java`**
   - Updated persistRepository method to include git_url

3. **`src/main/java/com/example/annotationextractor/RepositoryHubScanner.java`**
   - Added git URL assignment during scanning

4. **`src/main/java/com/example/annotationextractor/RepositoryScanner.java`**
   - Added git URL extraction from local repositories

5. **`src/main/java/com/example/annotationextractor/reporting/ExcelReportGenerator.java`**
   - Added Git URL column to repository details sheet

## Summary

This enhancement successfully adds Git URL support to the annotation extractor system, providing better traceability and reference information in Excel reports. The implementation is backward compatible, well-tested, and follows the existing code patterns. Users can now easily identify the source repositories for all scan results, improving the overall usability and value of the generated reports.
