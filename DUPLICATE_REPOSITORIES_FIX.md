# Duplicate Repositories Issue - Root Cause and Solution

## 🔍 **Problem Description**

The database contains duplicate repository records with the same name but different paths, as shown in the database screenshot:

```
id | repository_name        | repository_path
---+------------------------+----------------------------------------
1  | annotation-extractor  | D:\dev-docker-images\annotation-extractor\...
2  | db2h2                 | d:\testlab\db2h2
3  | testcraft-dashboard   | d:\testlab\testcraft-dashboard
20 | testcraft-dashboard   | D:\testlab\testcraft-dashboard
21 | db2h2                 | D:\testlab\db2h2
```

**Issues Identified:**
- `db2h2` appears twice (IDs 2 and 21)
- `testcraft-dashboard` appears twice (IDs 3 and 20)
- Path differences are due to case sensitivity: `d:\` vs `D:\`

## 🚨 **Root Causes**

### 1. **Inadequate Unique Constraint**
- **Old Constraint**: `UNIQUE(repository_name, repository_path)`
- **Problem**: Same repository name can exist multiple times if paths differ
- **Result**: Duplicates allowed when paths vary due to case sensitivity

### 2. **Windows Path Case Sensitivity**
- Windows file system is case-insensitive
- `d:\testlab\` and `D:\testlab\` are the same directory
- Database constraint treats them as different paths
- Multiple scans from different locations create duplicates

### 3. **Path Normalization Missing**
- No path normalization before database insertion
- Raw paths stored as-is from file system
- Case variations preserved in database

## 🛠️ **Solution Implemented**

### 1. **Database Schema Changes**

#### **Before (Problematic):**
```sql
CREATE TABLE repositories (
    id BIGSERIAL PRIMARY KEY,
    repository_name VARCHAR(255) NOT NULL,
    repository_path VARCHAR(500) NOT NULL,
    -- ... other fields ...
    UNIQUE(repository_name, repository_path)  -- ❌ Allows duplicates
);
```

#### **After (Fixed):**
```sql
CREATE TABLE repositories (
    id BIGSERIAL PRIMARY KEY,
    repository_name VARCHAR(255) NOT NULL UNIQUE,  -- ✅ Only name must be unique
    repository_path VARCHAR(500) NOT NULL,
    -- ... other fields ...
);
```

### 2. **Path Normalization**

Added `normalizePath()` method in `DataPersistenceService`:

```java
private static String normalizePath(String path) {
    if (path == null || path.isEmpty()) {
        return path;
    }
    
    // Convert to lowercase to normalize case sensitivity
    String normalized = path.toLowerCase();
    
    // Handle Windows drive letters consistently
    if (normalized.length() > 1 && normalized.charAt(1) == ':') {
        return path.substring(0, 1).toLowerCase() + path.substring(1).toLowerCase();
    }
    
    return normalized;
}
```

**Benefits:**
- `d:\testlab\` and `D:\testlab\` become `d:\testlab\`
- Consistent path storage regardless of input case
- Prevents future duplicates due to case sensitivity

### 3. **Updated Data Persistence Logic**

#### **Before:**
```sql
ON CONFLICT (repository_name, repository_path) DO UPDATE SET ...
```

#### **After:**
```sql
ON CONFLICT (repository_name) DO UPDATE SET 
    repository_path = EXCLUDED.repository_path,  -- Update path if changed
    -- ... other fields ...
```

**Benefits:**
- Single repository per name, regardless of path changes
- Path updates when repository is scanned from different location
- Maintains data integrity

## 🔧 **Migration Process**

### **Automatic Migration Service**

Created `DatabaseMigrationService` to handle the transition:

1. **Detect Duplicates**: Find repositories with duplicate names
2. **Clean Up**: Keep most recent, delete older duplicates
3. **Update Schema**: Drop old constraint, add new constraint
4. **Preserve Data**: Maintain all test data from kept repositories

### **Migration Steps**

```java
// Step 1: Clean up duplicate repositories
cleanupDuplicateRepositories(conn);

// Step 2: Drop the old constraint if it exists
dropOldConstraint(conn);

// Step 3: Add the new unique constraint on repository_name
addNewConstraint(conn);
```

### **Data Preservation Strategy**

- **Keep**: Most recent repository (based on `last_scan_date`)
- **Delete**: Older duplicates and their related test data
- **Maintain**: All test classes and methods from kept repositories
- **Clean**: Foreign key relationships properly handled

## 📋 **How to Fix Existing Duplicates**

### **Option 1: Use Migration Scripts**

#### **Windows:**
```cmd
fix-duplicate-repositories.bat
```

#### **Linux/Mac:**
```bash
./fix-duplicate-repositories.sh
```

### **Option 2: Manual Migration**

```bash
# Build the project
mvn clean package

# Run migration manually
java -cp target/annotation-extractor-1.0.0.jar \
     com.example.annotationextractor.database.DatabaseMigrationRunner \
     localhost 5432 test_analytics postgres
```

### **Option 3: Programmatic Migration**

```java
import com.example.annotationextractor.database.DatabaseMigrationService;

// Check if migration is needed
if (DatabaseMigrationService.isMigrationNeeded()) {
    // Run the migration
    DatabaseMigrationService.migrateToFixDuplicateRepositories();
}
```

## ✅ **Verification**

### **Before Migration:**
```sql
SELECT repository_name, COUNT(*) as count 
FROM repositories 
GROUP BY repository_name 
HAVING COUNT(*) > 1;
```

**Expected Output:**
```
repository_name        | count
----------------------+-------
db2h2                 | 2
testcraft-dashboard   | 2
```

### **After Migration:**
```sql
SELECT repository_name, COUNT(*) as count 
FROM repositories 
GROUP BY repository_name 
HAVING COUNT(*) > 1;
```

**Expected Output:**
```
repository_name | count
----------------+-------
(0 rows)
```

## 🚀 **Prevention of Future Duplicates**

### 1. **Unique Constraint**
- `repository_name` must be unique
- Database enforces this at the constraint level
- Impossible to insert duplicates

### 2. **Path Normalization**
- All paths normalized to lowercase before storage
- Consistent path representation
- Eliminates case sensitivity issues

### 3. **Upsert Logic**
- `ON CONFLICT (repository_name)` handles existing repositories
- Updates path and other data if repository is rescanned
- Maintains single record per repository

## 📊 **Impact Analysis**

### **Data Loss Risk: LOW**
- Only duplicate repositories are affected
- Test data from kept repositories is preserved
- Migration is transactional (rollback on failure)

### **Performance Impact: MINIMAL**
- One-time migration process
- Future scans use optimized constraint
- No ongoing performance degradation

### **Compatibility: FULL**
- Existing code continues to work
- API unchanged
- Reports and queries unaffected

## 🔮 **Future Enhancements**

### **Potential Improvements:**
1. **Repository Fingerprinting**: Use git commit hash for unique identification
2. **Path Deduplication**: Detect and merge repositories with different paths but same content
3. **Migration Logging**: Track all changes for audit purposes
4. **Automated Cleanup**: Periodic duplicate detection and cleanup

## 📝 **Summary**

The duplicate repositories issue was caused by:
1. **Inadequate unique constraint** allowing same name with different paths
2. **Windows path case sensitivity** creating perceived path differences
3. **Missing path normalization** preserving case variations

**Solution implemented:**
1. **Updated schema** with unique constraint on repository name only
2. **Added path normalization** to prevent case sensitivity issues
3. **Created migration service** to clean up existing duplicates
4. **Updated persistence logic** to handle conflicts properly

**Result:** No more duplicate repositories, consistent data, and improved data integrity.
