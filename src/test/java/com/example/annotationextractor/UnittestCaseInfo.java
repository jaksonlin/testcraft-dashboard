package com.example.annotationextractor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for tracking test case information and metadata.
 * This annotation is used to document test cases with comprehensive information
 * including author, target class/method, test points, requirements, and more.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface UnittestCaseInfo {
    
    /**
     * The author of the test case
     */
    String author() default "";
    
    /**
     * The title or name of the test case
     */
    String title() default "";
    
    /**
     * The target class being tested
     */
    String targetClass() default "";
    
    /**
     * The target method being tested
     */
    String targetMethod() default "";
    
    /**
     * Array of test points covered by this test case
     */
    String[] testPoints() default {};
    
    /**
     * Detailed description of what the test case validates
     */
    String description() default "";
    
    /**
     * Array of tags for categorizing the test case
     */
    String[] tags() default {};
    
    /**
     * Current status of the test case (TODO, IN_PROGRESS, PASSED, FAILED, etc.)
     */
    String status() default "TODO";
    
    /**
     * Array of related requirement IDs or descriptions
     */
    String[] relatedRequirements() default {};
    
    /**
     * Array of related defect IDs or descriptions
     */
    String[] relatedDefects() default {};
    
    /**
     * Array of related test case IDs or descriptions
     */
    String[] relatedTestcases() default {};
    
    /**
     * Timestamp of the last update
     */
    String lastUpdateTime() default "";
    
    /**
     * Author of the last update
     */
    String lastUpdateAuthor() default "";
    
    /**
     * Method signature for additional identification
     */
    String methodSignature() default "";
}
