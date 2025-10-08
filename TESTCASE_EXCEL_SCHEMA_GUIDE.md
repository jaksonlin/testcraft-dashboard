# Test Case Excel Import - Schema Guide

## Legal Notice

This schema is based on **industry-standard testing practices** (ISTQB, IEEE 829, ISO/IEC/IEEE 29119) and is **NOT derived from any proprietary system**. It represents common testing terminology used across the software testing industry.

---

## Supported Excel Structures

Our system is designed to be **schema-agnostic** and can import test cases from ANY Excel format through **user-defined column mapping**.

---

## Industry-Standard Field Categories

### **Required Fields (Minimum)**

These are the absolute minimum fields needed to store a test case:

| Field | Description | Examples |
|-------|-------------|----------|
| **ID** | Unique identifier | TC-001, TEST-1234, 00001 |
| **Title/Name** | Brief description | "User Login Test", "Payment Validation" |
| **Steps/Procedure** | Test execution steps | "1. Open app\n2. Enter credentials\n3. Click login" |

### **Commonly Used Optional Fields**

Based on industry practices, these fields are commonly found:

| Field Category | Common Names | Purpose |
|----------------|--------------|---------|
| **Precondition** | Setup, Precondition, Prerequisites, Given | What must be true before test |
| **Expected Result** | Expected, Result, Then, Verification | What should happen |
| **Postcondition** | Teardown, Cleanup, Postcondition | What to do after test |
| **Priority** | Priority, Importance, Severity | High/Medium/Low, P0/P1/P2 |
| **Type** | Type, Category, Test Type | Functional, Integration, Regression |
| **Status** | Status, State | Active, Deprecated, Draft |
| **Tags** | Tags, Labels, Keywords | smoke, regression, critical |
| **Requirements** | Requirements, Req ID, Story | REQ-001, US-123 |
| **Author** | Author, Created By, Owner | john.doe@company.com |
| **Last Updated** | Updated, Modified, Date | 2024-01-15 |

---

## Example Format 1: Minimal

```
| ID     | Title              | Steps                        |
|--------|--------------------|------------------------------|
| TC-001 | User Login         | 1. Open app 2. Login         |
| TC-002 | User Logout        | 1. Click logout 2. Confirm   |
```

## Example Format 2: Standard

```
| Test ID | Test Name          | Precondition | Steps            | Expected Result | Priority |
|---------|--------------------|--------------|--------------------|-----------------|----------|
| TC-001  | User Login         | User exists  | 1. Open app...   | Dashboard shown | High     |
| TC-002  | Invalid Password   | User exists  | 1. Enter wrong.. | Error message   | High     |
```

## Example Format 3: Comprehensive

```
| ID   | Title | Type    | Priority | Status | Precondition | Steps | Expected | Postcondition | Tags      | Requirements |
|------|-------|---------|----------|--------|--------------|-------|----------|---------------|-----------|--------------|
| 001  | Login | Func    | High     | Active | Logged out   | ...   | Success  | Clear session | smoke,auth| REQ-001      |
```

## Example Format 4: Agile/BDD Style

```
| Story ID | Scenario       | Given           | When               | Then                | Priority |
|----------|----------------|-----------------|--------------------|---------------------|----------|
| US-123   | User Login     | User registered | Enter credentials  | Dashboard displayed | Must     |
| US-123   | Invalid Login  | User registered | Wrong password     | Error shown         | Must     |
```

## Example Format 5: Test Management Tool Export

Many tools export in this format:

```
| Key    | Summary          | Description              | Status | Assignee | Type    |
|--------|------------------|--------------------------|--------|----------|---------|
| TEST-1 | Login Validation | Steps: 1. Open 2. Login  | Pass   | John     | Manual  |
| TEST-2 | Logout Test      | Steps: 1. Click logout   | Pass   | Jane     | Manual  |
```

---

## Column Name Variations (Auto-Detection)

Our system recognizes these common variations:

### For **ID**:
- id, ID, test_id, testid, case_id, caseid, number, #, tc_id, key

### For **Title**:
- title, name, test_name, testname, summary, description, scenario, case_name

### For **Steps**:
- steps, test_steps, procedure, actions, how_to_test, when, execution

### For **Precondition**:
- precondition, pre-condition, setup, prerequisites, given, before

### For **Expected Result**:
- expected, expected_result, result, verification, then, should

### For **Postcondition**:
- postcondition, post-condition, teardown, cleanup, after

### For **Priority**:
- priority, importance, severity, criticality

### For **Type**:
- type, category, test_type, kind

### For **Status**:
- status, state, condition

---

## Multi-Column Formats

Some organizations split test steps into multiple columns:

```
| ID     | Title | Step 1      | Step 2          | Step 3      |
|--------|-------|-------------|-----------------|-------------|
| TC-001 | Login | Open app    | Enter creds     | Click login |
```

**Our system handles this** by:
1. Detecting step columns (Step 1, Step 2, etc.)
2. Allowing user to map them as a sequence
3. Combining into single "steps" field with numbering

---

## Merged Cell Handling

Some Excel files use merged cells:

```
┌─────────────────────┬──────────────┐
│  Test Case Group    │              │
│  (merged)           │              │
├─────────────────────┼──────────────┤
│ TC-001              │ Login Test   │
│ TC-002              │ Logout Test  │
└─────────────────────┴──────────────┘
```

**Our system handles this** by:
- Detecting merged cells
- Letting user specify "data start row"
- Skipping header/group rows

---

## Multi-Sheet Support

Some organizations organize by:
- Sheet 1: Authentication Tests
- Sheet 2: Payment Tests
- Sheet 3: Reporting Tests

**Our system supports**:
1. Selecting which sheet to import
2. Different column mappings per sheet
3. Batch import from multiple sheets

---

## Custom Fields

Organizations may have custom fields like:
- "Automation Status"
- "Execution Time"
- "Environment"
- "Build Number"
- "Tester Name"
- etc.

**Our system stores these** in:
- `custom_fields` JSONB column
- Preserving all extra columns
- Searchable and filterable

---

## Data Start Row

Different formats start data at different rows:

### Format A (Row 1):
```
Row 1: ID | Title | Steps
Row 2: TC-001 | Test | Steps...
```

### Format B (Row 2):
```
Row 1: Test Case Catalog
Row 2: ID | Title | Steps
Row 3: TC-001 | Test | Steps...
```

### Format C (Row 3+):
```
Row 1: Company Name
Row 2: Project: XYZ
Row 3: 
Row 4: ID | Title | Steps
Row 5: TC-001 | Test | Steps...
```

**User specifies** which row has column headers, and which row data starts.

---

## Special Handling

### Date Formats
- ISO: 2024-01-15
- US: 01/15/2024
- EU: 15/01/2024
- Relative: "2 days ago"

### Lists/Arrays
- Comma: "tag1, tag2, tag3"
- Semicolon: "tag1; tag2; tag3"
- Newline: "tag1\ntag2\ntag3"
- Pipe: "tag1 | tag2 | tag3"

### Multi-line Text
- Newlines within cell
- Numbered lists: "1. Step one\n2. Step two"
- Bullet points: "• Item\n• Item"

---

## Import Flexibility

Our system is designed for **MAXIMUM FLEXIBILITY**:

1. ✅ No assumptions about column names
2. ✅ No required Excel structure
3. ✅ User-defined mapping for everything
4. ✅ Support any organization's format
5. ✅ Remember mappings for reuse
6. ✅ Handle variations in same organization

---

## Best Practices for Organizations

When creating test case Excel files, we recommend:

### ✅ **DO**:
- Use clear column headers
- Keep one test case per row
- Use consistent ID format
- Include at minimum: ID, Title, Steps
- Use standard terminology

### ❌ **AVOID**:
- Merged cells in data rows
- Multiple test cases per cell
- Complex formulas
- Hidden columns with data
- Inconsistent formatting

---

## Security & Privacy

When importing test cases:
- ✅ All data stays in your database
- ✅ No data sent to external services
- ✅ Excel files are processed locally
- ✅ No proprietary format lock-in
- ✅ Export back to Excel anytime

---

## Support for Testing Standards

This design aligns with:
- **ISTQB**: International Software Testing Qualifications Board
- **IEEE 829**: Standard for Software Test Documentation
- **ISO/IEC/IEEE 29119**: Software Testing Standard
- **Agile/BDD**: Gherkin-style scenarios

---

## Summary

**Our system is format-agnostic** and designed to import test cases from:
- ✅ Any Excel structure
- ✅ Any column naming convention
- ✅ Any organization's format
- ✅ Any testing methodology

**You control the mapping, we handle the import.**

This ensures **NO vendor lock-in** and **maximum flexibility** for all users.

