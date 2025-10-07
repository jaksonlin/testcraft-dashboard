package com.example.annotationextractor;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Examples demonstrating different ways to use @UnittestCaseInfo for test case connection.
 * 
 * These examples show the migration from heavy annotation usage to lightweight test case linking.
 */
public class TestCaseConnectionExamples {

    // ============================================================================
    // NEW APPROACH - Lightweight Test Case Linking (RECOMMENDED)
    // ============================================================================

    /**
     * Example 1: Minimal - Just link test case ID
     * This is the RECOMMENDED approach for most tests.
     */
    @Test
    @UnittestCaseInfo(testCaseIds = {"TC-1001"})
    public void shouldValidateEmailFormat() {
        String email = "user@example.com";
        assertTrue(email.contains("@"));
    }

    /**
     * Example 2: Multiple test case IDs
     * When one test method covers multiple test case scenarios
     */
    @Test
    @UnittestCaseInfo(testCaseIds = {"TC-1002", "TC-1003", "TC-1004"})
    public void shouldHandleMultiplePasswordValidationRules() {
        // This test validates:
        // TC-1002: Password minimum length
        // TC-1003: Password contains uppercase
        // TC-1004: Password contains number
        String password = "SecurePass123";
        assertTrue(password.length() >= 8);
        assertTrue(password.matches(".*[A-Z].*"));
        assertTrue(password.matches(".*\\d.*"));
    }

    /**
     * Example 3: With minimal context
     * Add title and author if helpful for your team
     */
    @Test
    @UnittestCaseInfo(
        testCaseIds = {"TC-1005"},
        title = "User Registration Flow",
        author = "Jane Doe"
    )
    public void shouldRegisterNewUser() {
        // Test implementation
        assertTrue(true); // Placeholder
    }

    // ============================================================================
    // BACKWARD COMPATIBILITY - Using Tags (Legacy)
    // ============================================================================

    /**
     * Example 4: Old approach using tags
     * This still works! The system extracts TC-2001 from tags.
     */
    @Test
    @UnittestCaseInfo(
        tags = {"TC-2001", "integration", "critical"}
    )
    public void shouldProcessPayment_LegacyApproach() {
        // Old code using tags still works
        assertTrue(true); // Placeholder
    }

    /**
     * Example 5: Tags with mixed content
     * System extracts only TC-2002 and ID-5678 (matching pattern XX-123)
     */
    @Test
    @UnittestCaseInfo(
        tags = {"TC-2002", "integration", "ID-5678", "smoke-test"}
    )
    public void shouldSendEmailNotification_MixedTags() {
        // Extracted IDs: TC-2002, ID-5678
        // Other tags: integration, smoke-test
        assertTrue(true); // Placeholder
    }

    // ============================================================================
    // MIGRATION EXAMPLE - Both Old and New
    // ============================================================================

    /**
     * Example 6: During migration - both fields present
     * When testCaseIds is present, it takes priority over tags
     */
    @Test
    @UnittestCaseInfo(
        testCaseIds = {"TC-3001"},  // This will be used
        tags = {"TC-9999", "old-id", "critical"}  // IDs here will be ignored
    )
    public void shouldCalculateShippingCost_MigrationExample() {
        // The system uses TC-3001 from testCaseIds field
        // Tags are still available for other purposes (filtering, categorization)
        assertTrue(true); // Placeholder
    }

    // ============================================================================
    // FULL ANNOTATION - For Comprehensive Documentation
    // ============================================================================

    /**
     * Example 7: Complete annotation with all fields
     * Use when you want to document everything in code (though this is heavy)
     */
    @Test
    @UnittestCaseInfo(
        author = "John Smith",
        title = "Complex Business Rule Validation",
        testCaseIds = {"TC-4001", "TC-4002"},
        description = "Validates complex business rules for order processing",
        targetClass = "OrderService",
        targetMethod = "processOrder",
        testPoints = {
            "Order total must be positive",
            "Discount cannot exceed order total",
            "Shipping address must be valid"
        },
        tags = {"business-logic", "critical", "regression"},
        status = "PASSED",
        relatedRequirements = {"REQ-100", "REQ-101"},
        relatedDefects = {"BUG-500"},
        relatedTestcases = {"TC-4003", "TC-4004"}
    )
    public void shouldValidateComplexOrderRules() {
        // Complex test implementation
        assertTrue(true); // Placeholder
    }

    // ============================================================================
    // NO ANNOTATION - When Test Case Link Not Needed
    // ============================================================================

    /**
     * Example 8: No annotation needed
     * Some tests don't need test case links (exploratory, refactoring, etc.)
     */
    @Test
    public void shouldHandleEdgeCaseFoundDuringBugFix() {
        // This is a regression test for a specific bug
        // No formal test case design exists for it
        assertTrue(true); // Placeholder
    }

    // ============================================================================
    // DIFFERENT ID FORMATS
    // ============================================================================

    /**
     * Example 9: Different test case ID formats
     * All these formats are recognized by the system
     */
    @Test
    @UnittestCaseInfo(testCaseIds = {"TC-5001"})  // Test Case
    public void exampleWithTC() {
        assertTrue(true);
    }

    @Test
    @UnittestCaseInfo(testCaseIds = {"ID-5002"})  // Generic ID
    public void exampleWithID() {
        assertTrue(true);
    }

    @Test
    @UnittestCaseInfo(testCaseIds = {"REQ-5003"})  // Requirement ID
    public void exampleWithREQ() {
        assertTrue(true);
    }

    @Test
    @UnittestCaseInfo(testCaseIds = {"TS-5004"})  // Test Scenario
    public void exampleWithTS() {
        assertTrue(true);
    }

    @Test
    @UnittestCaseInfo(testCaseIds = {"CASE-5005"})  // Case (4 letters)
    public void exampleWithCASE() {
        assertTrue(true);
    }
}

