# Test Case Tables Migration - Instructions

## Issue

The `test_cases` tables haven't been created yet, causing 500 errors when accessing the Test Cases page.

**Error**: `relation "test_cases" does not exist`

---

## ✅ Solution Options

### Option 1: Run Migration Script (Easiest)

```bash
# Windows
run-testcase-migration.bat

# Or manually:
psql -U postgres -d test_analytics -f src\main\resources\db\migration\V2__create_test_cases_tables.sql
```

### Option 2: Restart Spring Boot (Automatic)

Since Flyway is now enabled in `application.yml`, just restart the backend:

```bash
# Stop any running Spring Boot instance
# Then restart:
mvn spring-boot:run

# Or use the run script:
run-dashboard.bat
```

Spring Boot will automatically run the V2 migration on startup.

### Option 3: Configure Flyway in pom.xml

Add Flyway configuration to `pom.xml`:

```xml
<plugin>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-maven-plugin</artifactId>
    <version>10.15.0</version>
    <configuration>
        <url>jdbc:postgresql://localhost:5432/test_analytics</url>
        <user>postgres</user>
        <password>postgres</password>
        <locations>
            <location>filesystem:src/main/resources/db/migration</location>
        </locations>
    </configuration>
</plugin>
```

Then run:
```bash
mvn flyway:migrate
```

---

## 🔍 What the Migration Creates

**3 Tables**:
1. `test_cases` - Stores test case definitions
2. `test_case_coverage` - Links test cases to test methods
3. `test_case_import_templates` - Saves column mapping templates

**Indexes**:
- Performance indexes on organization, type, priority, status
- GIN indexes for arrays (tags, requirements)
- GIN index for JSONB (custom_fields)

**Functions**:
- Auto-update trigger for `updated_date`

---

## ✅ Verification

After running the migration, verify:

```sql
-- Check tables exist
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN ('test_cases', 'test_case_coverage', 'test_case_import_templates');

-- Should return 3 rows
```

Or restart the backend and check the page:
```
http://localhost:5173/testcases
```

Should show "0 test cases" instead of errors ✅

---

## 🚀 Recommended Approach

**Use Option 1 (run-testcase-migration.bat)**:

1. Run the migration script
2. Restart Spring Boot backend
3. Refresh browser
4. Test Cases page should work!

---

**Need help running the migration? Let me know which option you prefer!**

