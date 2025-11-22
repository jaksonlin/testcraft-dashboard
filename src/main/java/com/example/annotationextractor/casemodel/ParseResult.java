package com.example.annotationextractor.casemodel;

import java.util.ArrayList;
import java.util.List;

/**
 * Result object returned by TestClassParser containing both test classes and helper classes
 */
public class ParseResult {
    private final TestClassInfo testClassInfo;
    private final List<TestHelperClassInfo> helperClasses;

    public ParseResult(TestClassInfo testClassInfo, List<TestHelperClassInfo> helperClasses) {
        this.testClassInfo = testClassInfo;
        this.helperClasses = helperClasses;
    }

    public TestClassInfo getTestClassInfo() {
        return testClassInfo;
    }

    public List<TestHelperClassInfo> getHelperClasses() {
        return helperClasses != null ? helperClasses : new ArrayList<>();
    }
}

