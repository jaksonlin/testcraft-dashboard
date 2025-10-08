# Test Case Connection Guide

## Overview

This guide explains how to link your test methods to test case designs using the `@UnittestCaseInfo` annotation.

## Why Link Test Cases?

- **Prevents Developer Bias**: Ensures testing follows test case designs, not just what developers think to test
- **Requirements Traceability**: Track which test cases are automated vs manual
- **Gap Analysis**: Identify untested scenarios
- **Metrics**: Measure test case coverage, not just code coverage

---

## Quick Start - New Lightweight Approach (RECOMMENDED)

### Option 1: Just Test Case IDs (Minimal Effort)

```java
@Test
@UnittestCaseInfo(testCaseIds = {"TC-1234"})
public void shouldValidateUserInput() {
    // Your test code
}

// Multiple test cases
@Test
@UnittestCaseInfo(testCaseIds = {"TC-1234", "TC-1235", "TC-1236"})
public void shouldHandleComplexUserScenario() {
    // Your test code
}
```

**This is the recommended approach!** Just add one line with the test case IDs.

### Option 2: With Minimal Context (Slightly More Info)

```java
@Test
@UnittestCaseInfo(
    testCaseIds = {"TC-1234"},
    title = "User Input Validation Test",
    author = "John Doe"
)
public void shouldValidateUserInput() {
    // Your test code
}
```

---

## Backward Compatibility - Using Tags (Legacy)

If you're already using tags to store test case IDs, **it still works!** The system will automatically extract IDs from tags.

```java
@Test
@UnittestCaseInfo(
    tags = {"TC-1234", "integration", "critical"}
)
public void shouldValidateUserInput() {
    // Your test code
}
```

The system will automatically recognize `TC-1234` as a test case ID (matches pattern `XX-123`).

**Migration Path**: 
- Old tests with IDs in `tags` will continue working
- New tests should use `testCaseIds` field
- Gradually migrate old tests when you touch them

---

## Full Annotation (For Comprehensive Documentation)

You can still use the full annotation if you want to document everything in code:

```java
@Test
@UnittestCaseInfo(
    author = "John Doe",
    title = "User Input Validation Test",
    testCaseIds = {"TC-1234", "TC-1235"},  // Link to test cases
    description = "Validates that user input is properly sanitized",
    targetClass = "UserService",
    targetMethod = "validateInput",
    testPoints = {
        "Empty input returns error",
        "SQL injection attempts are blocked",
        "XSS attempts are sanitized"
    },
    tags = {"security", "input-validation"},
    status = "PASSED",
    relatedRequirements = {"REQ-001", "REQ-002"}
)
public void shouldValidateUserInput() {
    // Your test code
}
```

---

## Test Case ID Formats Supported

The system recognizes these patterns automatically:

- `TC-123` (Test Case)
- `ID-456` (Generic ID)
- `REQ-789` (Requirement)
- `TS-012` (Test Scenario)
- Any pattern: `XX-123` to `XXXX-123` (2-4 uppercase letters, hyphen, digits)

---

## How It Works

1. **You design test cases** (in Excel or test management tool)
2. **You write test code** and add `@UnittestCaseInfo(testCaseIds = {"TC-XXX"})`
3. **System scans your code** and extracts the links
4. **Dashboard shows**:
   - Which test cases are automated
   - Which test cases are still manual
   - Coverage percentage
   - Gaps in testing

---

## Examples

### Unit Test
```java
@Test
@UnittestCaseInfo(testCaseIds = {"TC-101"})
public void shouldCalculateTotalPrice() {
    // Test implementation
}
```

### Integration Test
```java
@Test
@UnittestCaseInfo(testCaseIds = {"TC-201", "TC-202"})
public void shouldProcessPaymentEndToEnd() {
    // Test implementation
}
```

### Component Test
```java
@Test
@UnittestCaseInfo(testCaseIds = {"TC-301"})
public void shouldRenderUserProfile() {
    // Test implementation
}
```

---

## FAQ

### Q: Do I have to fill out all the annotation fields?
**A: No!** Just use `testCaseIds` field. That's all you need for test case linking.

### Q: I already have test case IDs in the tags field. Do I need to change?
**A: No, but recommended.** Old code will continue working. Use `testCaseIds` for new tests.

### Q: What if one test method covers multiple test cases?
**A: Use an array:** `testCaseIds = {"TC-1", "TC-2", "TC-3"}`

### Q: Can I skip the annotation for some tests?
**A: Yes.** Some tests (exploratory, regression, refactoring) don't need test case links. Only link tests that verify specific test case designs.

### Q: Isn't this extra work?
**A: Minimal work, huge value.** Copy-paste one line with the test case ID. In return, you get:
- Confidence that requirements are tested
- Visibility into test coverage
- Protection against testing bias

---

## Code Extraction Example

The system automatically extracts test case IDs:

```java
// From dedicated field (preferred)
@UnittestCaseInfo(testCaseIds = {"TC-1234"})
→ Extracted: ["TC-1234"]

// From tags (backward compatibility)
@UnittestCaseInfo(tags = {"TC-1234", "integration"})
→ Extracted: ["TC-1234"]

// Both present (dedicated field takes priority)
@UnittestCaseInfo(
    testCaseIds = {"TC-1234"},
    tags = {"TC-5678", "critical"}
)
→ Extracted: ["TC-1234"]
```

---

## Summary

| Approach | Effort | Use Case |
|----------|--------|----------|
| **`testCaseIds` only** | ⭐ Minimal | **Recommended for most cases** |
| **`testCaseIds` + title + author** | ⭐⭐ Low | When you want basic context |
| **Full annotation** | ⭐⭐⭐⭐⭐ High | When documenting complex test cases |
| **Tags (legacy)** | ⭐ Minimal | Backward compatibility only |

**Best Practice**: Start with `testCaseIds` only. Add more fields only if they provide real value to your team.

