package com.example.annotationextractor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Lightweight annotation for linking test methods to test case IDs.
 * 
 * This is a potential future annotation design where ONLY test case ID is mandatory.
 * All other metadata lives in the test case management system, not in code.
 * 
 * Examples:
 * - Single test case:   @TestCaseId("TC-1234")
 * - Multiple test cases: @TestCaseId({"TC-1234", "TC-5678"})
 * 
 * Design Philosophy:
 * - Minimal effort for developers (just the ID)
 * - Single source of truth (details in test management tool)
 * - Easy to maintain (no metadata in code)
 * - Clear purpose (linking only)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface TestCaseId {
    
    /**
     * Test case ID(s) that this test method covers.
     * Format: "TC-1234", "ID-5678", "REQ-789", etc.
     */
    String[] value();
}

