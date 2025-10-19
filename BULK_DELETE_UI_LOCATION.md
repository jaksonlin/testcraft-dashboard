# Bulk Delete UI Location Guide

## 📍 Where to Find Bulk Delete in the UI

The bulk delete feature is in the **Actions menu** in the Test Cases view header (top-right).

---

## 🎨 Visual Location

### Navigation Path
```
1. Click "Test Cases" in sidebar
2. Click "Actions" button in header (top-right)
3. Select "Delete Filtered Test Cases" from dropdown
```

### UI Layout
```
┌─────────────────────────────────────────────────────────────┐
│ TestCraft Dashboard                                         │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  📊 Test Cases               [Actions ▼] [Upload]  ← HERE!  │
│                                   ↓                         │
│                              Click to open menu             │
│  ══════════════════════════════════════════════════════════ │
│                                                             │
│  [Stats Cards: Total | Automated | Gaps]                   │
│                                                             │
│  [Tabs: List | Coverage | Gaps]  ← You're on "List"        │
│  ══════════════════════════════════════════════════════════ │
│                                                             │
│  All Test Cases                                             │
│  View and manage all imported test cases                    │
│                                                             │
│  [Filters: Organization ▼] [Team ▼] [Status ▼]             │
│                                                             │
│  [Test Cases Table...]  ← CLEAN, NO RED BOX ✅              │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 🎯 Actions Menu States

### Menu with Filters Active (Enabled)
```
Click [Actions ▼] button:

┌──────────────────────────────┐
│ Export All (Coming Soon)     │
│ ─────────────────            │
│ 🗑️ Delete Filtered           │ ← ENABLED (red text)
│    Test Cases                │
└──────────────────────────────┘
```

### Menu without Filters (Disabled)
```
Click [Actions ▼] button:

┌──────────────────────────────┐
│ Export All (Coming Soon)     │
│ ─────────────────            │
│ 🗑️ Delete Filtered           │ ← DISABLED (gray text)
│    Test Cases                │
│                              │
│ Apply filters to enable      │ ← Helper text
│ bulk delete                  │
└──────────────────────────────┘
```

---

## 🖱️ How to Use

### Step-by-Step Guide

**1. Navigate to Test Cases**
```
Sidebar → Click "Test Cases"
```

**2. Apply Filters**
```
Examples:
- Organization: "default" (to clean up)
- Status: "inactive" (to remove old)
- Team: Select deprecated team
- Any combination
```

**3. Click Actions Menu**
```
Top-right header:
[Actions ▼] ← Click this

Dropdown appears:
┌──────────────────────────────┐
│ Export All                   │
│ ─────────────────            │
│ 🗑️ Delete Filtered           │ ← Click this
│    Test Cases                │
└──────────────────────────────┘
```

**4. First Confirmation Dialog**
```
⚠️ WARNING: PERMANENT DELETION

You are about to DELETE approximately 150 test cases matching:

Organization: default
Status: inactive

THIS CANNOT BE UNDONE!

Click OK to continue, or Cancel to abort.

[Cancel] [OK]
```

**5. Second Confirmation Dialog (Safety)**
```
🚨 FINAL CONFIRMATION

This will PERMANENTLY DELETE test cases from the database.

Are you ABSOLUTELY SURE?

Click OK to DELETE, or Cancel to abort.

[Cancel] [OK]
```

**6. Success Toast**
```
✅ Successfully deleted 150 test case(s)
```

**7. Data Refreshes Automatically**
```
Table reloads showing remaining test cases
```

---

## 🎨 Visual States

### State 1: No Filters (Button Disabled in Menu)

```
┌───────────────────────────────────────┐
│ Test Cases    [Actions ▼] [Upload]   │
│                    ↓                  │
│ [Filters: Empty]   Menu appears:     │
│                    ┌─────────────┐   │
│ [Table...]         │ Export      │   │
│ ID  │ Title        │ ─────       │   │
│ TC-1│ Login        │ 🗑️ Delete   │   │ ← GRAYED OUT
│ TC-2│ Logout       │  (disabled) │   │
│ ...                └─────────────┘   │
└───────────────────────────────────────┘

Clean view, delete not prominent ✅
```

### State 2: Filters Active (Button Enabled in Menu)

```
┌───────────────────────────────────────┐
│ Test Cases    [Actions ▼] [Upload]   │
│                    ↓                  │
│ [Filters: Org=default ✓]             │
│                    Menu:              │
│ [Filtered Table]   ┌─────────────┐   │
│ ID   │ Title       │ Export      │   │
│ TC-99│ Old         │ ─────       │   │
│ TC-100│Legacy      │ 🗑️ Delete ✓ │   │ ← ENABLED (red)
│ ...                └─────────────┘   │
└───────────────────────────────────────┘

Clean view, delete opt-in only ✅
```

### State 3: After Deletion (Success)

```
┌───────────────────────────────────────┐
│ ┌─────────────────────────────────┐   │
│ │ ✅ Success                       │   │ ← Toast appears
│ │ Successfully deleted 150 test   │   │
│ │ case(s)                         │   │
│ └─────────────────────────────────┘   │
│                                       │
│ All Test Cases                        │
│                                       │
│ [Filters: Organization=default ✓]    │
│                                       │
│ [Test Cases Table - Empty]            │
│ No test cases found matching filters  │ ← Data deleted!
│                                       │
└───────────────────────────────────────┘
```

---

## 💡 Use Case Examples

### Use Case 1: Clean Up "default" Organization

**Scenario:** Remove orphaned test cases with organization="default"

```
Step 1: Navigate to Test Cases view
Step 2: Filter by Organization: "default"
Step 3: See: "⚠️ Bulk Actions Available - 200 test cases"
Step 4: Click: [🗑️ Delete All Filtered Test Cases]
Step 5: Confirm twice
Step 6: ✅ 200 test cases deleted
```

### Use Case 2: Remove Deprecated Team Data

**Scenario:** Team "Legacy QA" (ID: 8) was disbanded

```
Step 1: Navigate to Test Cases view
Step 2: Filter by Team: Select "Legacy QA"
Step 3: See: "⚠️ Bulk Actions Available - 850 test cases"
Step 4: Export data first (click export in header)
Step 5: Click: [🗑️ Delete All Filtered Test Cases]
Step 6: Confirm twice
Step 7: ✅ 850 test cases deleted
```

### Use Case 3: Remove Inactive Test Cases

**Scenario:** Archive old inactive test cases

```
Step 1: Navigate to Test Cases view
Step 2: Filter by Status: "inactive"
Step 3: See: "⚠️ Bulk Actions Available - 1,200 test cases"
Step 4: Export to CSV (backup)
Step 5: Click: [🗑️ Delete All Filtered Test Cases]
Step 6: Confirm twice
Step 7: ✅ 1,200 test cases deleted
```

---

## 🔔 What Happens When You Click

### Click Sequence

```
1. You click [🗑️ Delete All Filtered Test Cases]
   ↓
2. First dialog appears:
   ┌──────────────────────────────────────┐
   │ ⚠️ WARNING: PERMANENT DELETION       │
   │                                      │
   │ You are about to DELETE ~150 test   │
   │ cases matching:                      │
   │                                      │
   │ Organization: default                │
   │ Status: inactive                     │
   │                                      │
   │ THIS CANNOT BE UNDONE!               │
   │                                      │
   │          [Cancel] [OK]               │
   └──────────────────────────────────────┘
   ↓
3. If you click OK, second dialog appears:
   ┌──────────────────────────────────────┐
   │ 🚨 FINAL CONFIRMATION                │
   │                                      │
   │ This will PERMANENTLY DELETE test   │
   │ cases from the database.             │
   │                                      │
   │ Are you ABSOLUTELY SURE?             │
   │                                      │
   │          [Cancel] [OK]               │
   └──────────────────────────────────────┘
   ↓
4. If you click OK again:
   - API call executes
   - Server deletes matching test cases
   - Success toast appears
   - Data automatically refreshes
   ✅ Done!
```

---

## ⚠️ Important UI Behaviors

### Behavior 1: Button Only Shows with Filters ✅

```
No filters → No button (safe!)
Any filter → Button appears (ready for action)
```

**Why:** Prevents accidental access to bulk delete when viewing all data

### Behavior 2: Double Confirmation Required ✅

```
First confirmation → Shows filter summary
Second confirmation → Final warning
Only then → Deletion executes
```

**Why:** Prevents accidental clicks

### Behavior 3: Count Shown ✅

```
Before deletion: "150 test cases match current filters"
In dialog: "DELETE approximately 150 test cases"
After deletion: "Successfully deleted 150 test case(s)"
```

**Why:** User always knows impact before confirming

### Behavior 4: Auto-Refresh ✅

```
After deletion:
- Data reloads automatically
- Statistics update
- Table shows remaining data
- Filters remain active (so you can verify)
```

**Why:** Immediate feedback on deletion result

---

## 🎨 Button Styling

**Colors indicate danger:**
- Red background: `bg-red-600`
- Red border on alert box: `border-red-200`
- Red warning text: `text-red-600`
- Hover effect: Darker red

**Icons:**
- 🗑️ Trash icon (lucide-react `Trash2`)
- ⚠️ Warning symbols

**Text:**
- Bold warnings
- Clear count display
- Explicit "cannot be undone" message

---

## 🧪 Testing Guide

### Safe Test in UI

1. **Create test data**
   ```
   - Upload Excel with organization="TestOrg"
   - Import 100 test cases
   ```

2. **Navigate to Test Cases view**
   ```
   Sidebar → Test Cases
   ```

3. **Apply filter**
   ```
   Organization dropdown → Select "TestOrg"
   ```

4. **Bulk delete section appears**
   ```
   Red alert box with button appears ✅
   Shows: "100 test cases match current filters"
   ```

5. **Click Delete button**
   ```
   Two confirmation dialogs appear ✅
   ```

6. **Confirm both**
   ```
   Data deleted ✅
   Success toast appears ✅
   Table refreshes ✅
   ```

7. **Verify**
   ```
   Filter still active: organization="TestOrg"
   Result: "No test cases found" ✅
   ```

---

## 📱 Screenshots Description

### Screenshot 1: Button Hidden (No Filters)
```
┌─────────────────────────────────────┐
│ All Test Cases                      │
│                                     │
│ [Empty Filters]                     │
│                                     │
│ [Test Cases Table]                  │ ← Normal view
│ ...                                 │
└─────────────────────────────────────┘

No red alert box = Safe, can't accidentally delete
```

### Screenshot 2: Button Visible (Filters Active)
```
┌─────────────────────────────────────┐
│ All Test Cases                      │
│                                     │
│ [Filters: Organization=default ✓]  │
│                                     │
│ ╔═══════════════════════════════╗   │
│ ║ ⚠️ Bulk Actions Available     ║   │ ← RED ALERT BOX
│ ║ 150 test cases match filters  ║   │
│ ║                               ║   │
│ ║ [🗑️ Delete All Filtered]      ║   │ ← RED BUTTON
│ ║                               ║   │
│ ║ ⚠️ Warning: Permanent deletion║   │
│ ╚═══════════════════════════════╝   │
│                                     │
│ [Test Cases Table]                  │
│ ...                                 │
└─────────────────────────────────────┘

Red alert box appears = Bulk delete available
```

### Screenshot 3: Confirmation Dialog
```
┌─────────────────────────────────────┐
│  ╔═════════════════════════════╗    │
│  ║ ⚠️ WARNING: PERMANENT       ║    │
│  ║    DELETION                 ║    │
│  ║                             ║    │
│  ║ You are about to DELETE     ║    │
│  ║ ~150 test cases matching:   ║    │
│  ║                             ║    │
│  ║ Organization: default       ║    │
│  ║ Status: inactive            ║    │
│  ║                             ║    │
│  ║ THIS CANNOT BE UNDONE!      ║    │
│  ║                             ║    │
│  ║     [Cancel] [OK]           ║    │
│  ╚═════════════════════════════╝    │
└─────────────────────────────────────┘
```

---

## 🎯 Quick Access Tips

### Tip 1: Use Bookmarkable URLs

```
Bookmark this URL:
http://localhost:5173/testcases?org=default&status=inactive

When you open it:
- Filters automatically applied ✅
- Bulk delete button ready ✅
- One click to delete ✅
```

### Tip 2: Export Before Deleting

```
Workflow:
1. Apply filters
2. Click [Export] in header (top right)
3. Download CSV backup
4. Then click bulk delete button
5. Safe! Have backup if needed ✅
```

### Tip 3: Start with Narrow Filters

```
Start specific:
  Search: "test-case-123" 
  → Deletes 1 test case (safe to test)

Then broader:
  Status: "inactive"
  → Deletes many (after testing works)
```

---

## 🔐 Safety Features in UI

### Feature 1: Requires Filters ✅
```
No filters → No button
User can't accidentally access bulk delete
```

### Feature 2: Shows Count ✅
```
"150 test cases match current filters"
User sees impact before clicking
```

### Feature 3: Double Confirmation ✅
```
Dialog 1: Shows filters and count
Dialog 2: Final warning
Both must be confirmed
```

### Feature 4: Color-Coded Danger ✅
```
Red background: Danger zone
Warning symbols: ⚠️ 🚨
Trash icon: 🗑️
User can't miss the danger
```

### Feature 5: Explicit Warning Text ✅
```
"Warning: This action is permanent and cannot be undone"
"Always export data before bulk deletion"
Clear consequences stated
```

---

## 📋 Checklist Before Using

Before clicking bulk delete in production:

- [ ] Applied correct filters
- [ ] Verified count makes sense
- [ ] Exported data as backup (click Export button)
- [ ] Tested in development environment first
- [ ] Ready to confirm twice
- [ ] Understand this is permanent
- [ ] Have database backup (just in case)

---

## 🎊 Summary

**Location:** Test Cases view → Apply filters → Red alert box appears  

**Button:** `[🗑️ Delete All Filtered Test Cases]` in red

**Safety:** 
- ✅ Only visible with filters
- ✅ Shows count before deletion
- ✅ Double confirmation required
- ✅ Auto-refreshes after deletion

**Perfect for:**
- Cleaning up "default" organization data
- Removing deprecated team test cases
- Deleting inactive/archived test cases
- Bulk data management

**Ready to use!** 🚀

