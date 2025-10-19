# Bulk Delete Test Cases - Quick Guide

## ⚡ Quick Reference

**Endpoint:** `DELETE /api/testcases?[filters]&confirm=true`

---

## 🚀 Common Examples

### Delete by Organization
```bash
# Preview first
GET /api/testcases?organization=OldOrg
→ Shows 500 test cases

# Delete
DELETE /api/testcases?organization=OldOrg&confirm=true
→ Deletes 500 test cases
```

### Delete by Team
```bash
DELETE /api/testcases?teamId=8&confirm=true
```

### Delete Inactive Test Cases
```bash
DELETE /api/testcases?status=inactive&confirm=true
```

### Delete by Combined Filters
```bash
DELETE /api/testcases?organization=ACME&status=archived&priority=low&confirm=true
```

---

## ⚠️ Safety Rules

### Rule 1: ALWAYS Preview First ✅
```bash
# 1. Preview (GET)
GET /api/testcases?organization=ACME&status=inactive
→ Count: 45 test cases

# 2. Delete (DELETE) - same filters!
DELETE /api/testcases?organization=ACME&status=inactive&confirm=true
→ Deletes: 45 test cases
```

### Rule 2: ALWAYS Export Backup ✅
```bash
# Export before deleting (just in case!)
GET /api/testcases?organization=ACME&status=inactive
→ Export to CSV → Save backup

# Then delete
DELETE /api/testcases?organization=ACME&status=inactive&confirm=true
```

### Rule 3: Requires Confirmation ✅
```bash
# ❌ REJECTED
DELETE /api/testcases?organization=ACME
→ Error: "Requires confirmation"

# ✅ ACCEPTED
DELETE /api/testcases?organization=ACME&confirm=true
```

### Rule 4: Requires Filter ✅
```bash
# ❌ REJECTED (would delete EVERYTHING!)
DELETE /api/testcases?confirm=true
→ Error: "Requires at least one filter"

# ✅ ACCEPTED
DELETE /api/testcases?status=inactive&confirm=true
```

---

## 💻 Frontend Usage

```typescript
import { deleteAllTestCases } from '../lib/testCaseApi';

// Delete all inactive test cases in ACME org
const result = await deleteAllTestCases(
  {
    organization: 'ACME',
    status: 'inactive'
  },
  true  // confirm
);

console.log(`Deleted ${result.deleted} test cases`);
```

---

## 🎯 Available Filters

| Filter | Parameter | Example |
|--------|-----------|---------|
| Organization | `organization` | `ACME` |
| Team | `teamId` | `5` |
| Type | `type` | `integration` |
| Priority | `priority` | `low` |
| Status | `status` | `inactive` |
| Search | `search` | `old-test` |

**Combine multiple filters for precision!**

---

## ⚠️ DANGER ZONE

### What NOT to Do

```bash
# ❌ NEVER: No filters (even with confirm)
DELETE /api/testcases?confirm=true
→ REJECTED by server ✅

# ❌ NEVER: Delete without preview
DELETE /api/testcases?organization=ACME&confirm=true
→ What if 10,000 test cases match?

# ❌ NEVER: Delete in production without testing
→ Test in dev environment first!

# ❌ NEVER: Delete without backup
→ Export CSV first!
```

---

## ✅ Safe Workflow

```
1. Apply filters in UI
2. Verify count: "Showing X of Y filtered results"
3. Export to CSV (backup)
4. Run: DELETE /api/testcases?[same filters]&confirm=true
5. Verify: GET /api/testcases?[same filters] → Returns 0
6. Done! ✅
```

---

**Remember:** This is **PERMANENT DELETION** - no undo!  
**Always:** Preview + Export + Delete  
**Never:** Delete without filters or confirmation

