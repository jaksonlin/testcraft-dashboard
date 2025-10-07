package com.example.annotationextractor;

import org.junit.Test;

/**
 * Examples demonstrating the flexible plugin-based annotation system.
 * 
 * This class shows that the system supports MULTIPLE annotation formats
 * for test case linking, not just one fixed design.
 * 
 * Teams can choose which annotation style fits their workflow!
 */
public class MultiAnnotationExamples {

    // ============================================================================
    // OPTION 1: Heavy Annotation (Current - Full Metadata)
    // ============================================================================

    /**
     * Using @UnittestCaseInfo with comprehensive metadata.
     * Use when: You want everything documented in code
     */
    @Test
    @UnittestCaseInfo(
        testCaseIds = {"TC-1001"},
        title = "User Login Validation",
        author = "John Doe",
        description = "Validates user login with correct credentials",
        targetClass = "UserService",
        targetMethod = "login",
        testPoints = {"authentication", "session creation"},
        tags = {"security", "critical"},
        status = "PASSED"
    )
    public void shouldLoginWithValidCredentials_Heavy() {
        assertTrue(true); // Placeholder
    }

    /**
     * Using @UnittestCaseInfo with ONLY test case IDs (recommended migration path).
     * Use when: You want backward compatibility but minimal effort
     */
    @Test
    @UnittestCaseInfo(testCaseIds = {"TC-1002"})
    public void shouldLogoutSuccessfully_Minimal() {
        assertTrue(true); // Placeholder
    }

    /**
     * Legacy: Using @UnittestCaseInfo with test case IDs in tags.
     * This still works for backward compatibility!
     */
    @Test
    @UnittestCaseInfo(
        tags = {"TC-1003", "integration", "smoke"},
        title = "Password Reset Flow"
    )
    public void shouldResetPassword_Legacy() {
        // System extracts: ["TC-1003"]
        assertTrue(true); // Placeholder
    }

    // ============================================================================
    // OPTION 2: Lightweight Annotation (Future - Test Case ID Only)
    // ============================================================================

    /**
     * Using @TestCaseId with single test case.
     * Use when: You want MINIMAL effort - just the ID!
     */
    @Test
    @TestCaseId("TC-2001")
    public void shouldValidateEmail_Lightweight() {
        assertTrue(true); // Placeholder
    }

    /**
     * Using @TestCaseId with multiple test cases.
     * One test method can cover multiple test case designs.
     */
    @Test
    @TestCaseId({"TC-2002", "TC-2003", "TC-2004"})
    public void shouldValidatePasswordRules_MultipleTestCases() {
        // This test covers:
        // TC-2002: Password minimum length
        // TC-2003: Password complexity
        // TC-2004: Password special characters
        assertTrue(true); // Placeholder
    }

    // ============================================================================
    // OPTION 3: Standard JUnit @Tag (Standard - No Custom Annotation)
    // NOTE: @Tag is JUnit 5. Examples commented out since project uses JUnit 4.
    //       The extractor still works for JUnit 5 projects!
    // ============================================================================

    /**
     * Using JUnit 5 @Tag for test case linking.
     * Use when: You want to use standard JUnit annotations
     * 
     * Example (JUnit 5):
     * @Tag("TC-3001")
     * public void shouldCreateUser_JUnitTag() { ... }
     */
    @Test
    public void shouldCreateUser_JUnitTag_Example() {
        // In JUnit 5, you would use: @Tag("TC-3001")
        // System would extract: ["TC-3001"]
        assertTrue(true); // Placeholder
    }

    /**
     * Using multiple @Tag annotations.
     * Mix test case IDs with regular tags.
     * 
     * Example (JUnit 5):
     * @Tag("TC-3002")
     * @Tag("TC-3003")
     * @Tag("integration")  // Regular tag
     * @Tag("critical")     // Regular tag
     * public void shouldUpdateUser_MultipleTags() { ... }
     */
    @Test
    public void shouldUpdateUser_MultipleTags_Example() {
        // System extracts: ["TC-3002", "TC-3003"]
        // Ignores: "integration", "critical" (not matching test case pattern)
        assertTrue(true); // Placeholder
    }

    // ============================================================================
    // MIXING ANNOTATIONS (All Work Together!)
    // ============================================================================

    /**
     * You can even use MULTIPLE annotation types on the same method!
     * System extracts test case IDs from all of them.
     * 
     * Example (JUnit 5):
     * @TestCaseId("TC-4001")
     * @Tag("TC-4002")
     * @UnittestCaseInfo(testCaseIds = {"TC-4003"})
     * public void shouldProcessPayment_MixedAnnotations() { ... }
     */
    @Test
    @TestCaseId("TC-4001")
    @UnittestCaseInfo(testCaseIds = {"TC-4003"})
    public void shouldProcessPayment_MixedAnnotations() {
        // With @Tag, system would extract: ["TC-4001", "TC-4002", "TC-4003"]
        // Currently extracts: ["TC-4001", "TC-4003"]
        assertTrue(true); // Placeholder
    }

    /**
     * Duplicates are automatically removed.
     * 
     * Example (JUnit 5):
     * @TestCaseId("TC-4004")
     * @Tag("TC-4004")  // Same ID
     * public void shouldRefundPayment_NoDuplicates() { ... }
     */
    @Test
    @TestCaseId("TC-4004")
    @UnittestCaseInfo(testCaseIds = {"TC-4004"})  // Same ID
    public void shouldRefundPayment_NoDuplicates() {
        // System extracts: ["TC-4004"] (only once, duplicates removed)
        assertTrue(true); // Placeholder
    }

    // ============================================================================
    // DIFFERENT TEAMS, DIFFERENT STYLES (All Work in Same Codebase!)
    // ============================================================================

    /**
     * Team A's style: Prefer lightweight
     */
    @Test
    @TestCaseId("TC-5001")
    public void teamA_prefersLightweight() {
        assertTrue(true);
    }

    /**
     * Team B's style: Prefer JUnit standard (JUnit 5 @Tag)
     * Example: @Tag("TC-5002")
     */
    @Test
    @TestCaseId("TC-5002")  // In JUnit 5 projects, could use @Tag instead
    public void teamB_prefersJUnitStandard() {
        assertTrue(true);
    }

    /**
     * Team C's style: Prefer comprehensive metadata
     */
    @Test
    @UnittestCaseInfo(
        testCaseIds = {"TC-5003"},
        title = "Team C's Test",
        author = "Team C",
        description = "We like detailed documentation"
    )
    public void teamC_prefersComprehensive() {
        assertTrue(true);
    }

    // All three teams' tests work in the same system!

    // ============================================================================
    // TESTS WITHOUT TEST CASE LINKING (Also Fine!)
    // ============================================================================

    /**
     * Not all tests need test case IDs.
     * Exploratory tests, regression tests, etc. don't need formal test cases.
     */
    @Test
    public void shouldHandleEdgeCaseFoundDuringDebugging() {
        // No test case ID needed - this is an exploratory test
        assertTrue(true);
    }

    /**
     * Regression test for a specific bug.
     * In JUnit 5, you might use: @Tag("bug-12345") @Tag("regression")
     */
    @Test
    public void shouldHandleNullPointerInPaymentService_BugFix() {
        // No test case ID - this tests a specific bug fix
        // @Tag annotations (if used) wouldn't be extracted since they don't match test case ID pattern
        assertTrue(true);
    }

    // ============================================================================
    // HELPER METHOD (since this is just an example)
    // ============================================================================

    private void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Assertion failed");
        }
    }
}

