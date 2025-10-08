package com.example.annotationextractor;

import com.example.annotationextractor.casemodel.*;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test the plugin-based annotation system that supports ANY annotation for test case linking.
 */
public class TestCaseIdExtractorRegistryTest {
    
    private TestCaseIdExtractorRegistry registry;
    
    @Before
    public void setUp() {
        registry = new TestCaseIdExtractorRegistry();
    }
    
    // ============================================================================
    // Test @UnittestCaseInfo (Current Heavy Annotation)
    // ============================================================================
    
    @Test
    public void testUnittestCaseInfo_WithTestCaseIdsField() {
        String javaCode = 
            "public class TestClass {\n" +
            "    @UnittestCaseInfo(testCaseIds = {\"TC-1234\", \"TC-5678\"})\n" +
            "    public void testMethod() {}\n" +
            "}";
        
        String[] ids = extractIds(javaCode, "UnittestCaseInfo");
        
        assertArrayEquals("Should extract from testCaseIds field", 
            new String[]{"TC-1234", "TC-5678"}, ids);
    }
    
    @Test
    public void testUnittestCaseInfo_WithTagsField_BackwardCompatibility() {
        String javaCode = 
            "public class TestClass {\n" +
            "    @UnittestCaseInfo(tags = {\"TC-1234\", \"integration\", \"ID-5678\"})\n" +
            "    public void testMethod() {}\n" +
            "}";
        
        String[] ids = extractIds(javaCode, "UnittestCaseInfo");
        
        assertEquals("Should extract 2 test case IDs from tags", 2, ids.length);
        assertTrue("Should contain TC-1234", arrayContains(ids, "TC-1234"));
        assertTrue("Should contain ID-5678", arrayContains(ids, "ID-5678"));
    }
    
    @Test
    public void testUnittestCaseInfo_PriorityTestCaseIdsOverTags() {
        String javaCode = 
            "public class TestClass {\n" +
            "    @UnittestCaseInfo(\n" +
            "        testCaseIds = {\"TC-1234\"},\n" +
            "        tags = {\"TC-9999\"}\n" +
            "    )\n" +
            "    public void testMethod() {}\n" +
            "}";
        
        String[] ids = extractIds(javaCode, "UnittestCaseInfo");
        
        assertArrayEquals("Should use testCaseIds, not tags", 
            new String[]{"TC-1234"}, ids);
    }
    
    // ============================================================================
    // Test @TestCaseId (Future Lightweight Annotation)
    // ============================================================================
    
    @Test
    public void testTestCaseId_SingleValue() {
        String javaCode = 
            "public class TestClass {\n" +
            "    @TestCaseId(\"TC-1234\")\n" +
            "    public void testMethod() {}\n" +
            "}";
        
        String[] ids = extractIds(javaCode, "TestCaseId");
        
        assertArrayEquals("Should extract single ID", 
            new String[]{"TC-1234"}, ids);
    }
    
    @Test
    public void testTestCaseId_MultipleValues() {
        String javaCode = 
            "public class TestClass {\n" +
            "    @TestCaseId({\"TC-1234\", \"TC-5678\", \"ID-9999\"})\n" +
            "    public void testMethod() {}\n" +
            "}";
        
        String[] ids = extractIds(javaCode, "TestCaseId");
        
        assertArrayEquals("Should extract multiple IDs", 
            new String[]{"TC-1234", "TC-5678", "ID-9999"}, ids);
    }
    
    @Test
    public void testTestCaseId_WithExplicitValue() {
        String javaCode = 
            "public class TestClass {\n" +
            "    @TestCaseId(value = {\"TC-1234\"})\n" +
            "    public void testMethod() {}\n" +
            "}";
        
        String[] ids = extractIds(javaCode, "TestCaseId");
        
        assertArrayEquals("Should extract from explicit value", 
            new String[]{"TC-1234"}, ids);
    }
    
    // ============================================================================
    // Test @Tag (JUnit 5 Standard Annotation)
    // ============================================================================
    
    @Test
    public void testJUnitTag_WithTestCaseId() {
        String javaCode = 
            "public class TestClass {\n" +
            "    @Tag(\"TC-1234\")\n" +
            "    public void testMethod() {}\n" +
            "}";
        
        String[] ids = extractIds(javaCode, "Tag");
        
        assertArrayEquals("Should extract from @Tag", 
            new String[]{"TC-1234"}, ids);
    }
    
    @Test
    public void testJUnitTag_WithNonTestCaseId() {
        String javaCode = 
            "public class TestClass {\n" +
            "    @Tag(\"integration\")\n" +
            "    public void testMethod() {}\n" +
            "}";
        
        String[] ids = extractIds(javaCode, "Tag");
        
        assertEquals("Should not extract non-test-case-id tag", 0, ids.length);
    }
    
    // ============================================================================
    // Test Multiple Annotations on Same Method
    // ============================================================================
    
    @Test
    public void testMultipleAnnotations_CombineIds() {
        String javaCode = 
            "public class TestClass {\n" +
            "    @TestCaseId(\"TC-1234\")\n" +
            "    @Tag(\"TC-5678\")\n" +
            "    @UnittestCaseInfo(testCaseIds = {\"TC-9999\"})\n" +
            "    public void testMethod() {}\n" +
            "}";
        
        try {
            JavaParser parser = new JavaParser();
            CompilationUnit cu = parser.parse(javaCode).getResult().get();
            MethodDeclaration method = cu.findFirst(MethodDeclaration.class).get();
            List<AnnotationExpr> annotations = method.getAnnotations();
            
            String[] ids = registry.extractTestCaseIds(annotations);
            
            assertEquals("Should extract from all annotations", 3, ids.length);
            assertTrue("Should contain TC-1234", arrayContains(ids, "TC-1234"));
            assertTrue("Should contain TC-5678", arrayContains(ids, "TC-5678"));
            assertTrue("Should contain TC-9999", arrayContains(ids, "TC-9999"));
            
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }
    
    @Test
    public void testMultipleAnnotations_NoDuplicates() {
        String javaCode = 
            "public class TestClass {\n" +
            "    @TestCaseId(\"TC-1234\")\n" +
            "    @Tag(\"TC-1234\")\n" +  // Same ID
            "    public void testMethod() {}\n" +
            "}";
        
        try {
            JavaParser parser = new JavaParser();
            CompilationUnit cu = parser.parse(javaCode).getResult().get();
            MethodDeclaration method = cu.findFirst(MethodDeclaration.class).get();
            List<AnnotationExpr> annotations = method.getAnnotations();
            
            String[] ids = registry.extractTestCaseIds(annotations);
            
            assertEquals("Should not have duplicates", 1, ids.length);
            assertEquals("Should be TC-1234", "TC-1234", ids[0]);
            
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }
    
    // ============================================================================
    // Test Custom Extractor Registration
    // ============================================================================
    
    @Test
    public void testCustomExtractor_Registration() {
        // Create custom extractor
        TestCaseIdExtractor customExtractor = new TestCaseIdExtractor() {
            @Override
            public boolean supports(AnnotationExpr annotation) {
                return "CustomAnnotation".equals(annotation.getNameAsString());
            }
            
            @Override
            public String[] extractTestCaseIds(AnnotationExpr annotation) {
                return new String[]{"CUSTOM-123"};
            }
            
            @Override
            public int getPriority() {
                return 10;
            }
        };
        
        registry.register(customExtractor);
        
        String javaCode = 
            "public class TestClass {\n" +
            "    @CustomAnnotation\n" +
            "    public void testMethod() {}\n" +
            "}";
        
        String[] ids = extractIds(javaCode, "CustomAnnotation");
        
        assertArrayEquals("Should use custom extractor", 
            new String[]{"CUSTOM-123"}, ids);
    }
    
    // ============================================================================
    // Test Extractor Priority
    // ============================================================================
    
    @Test
    public void testExtractorPriority() {
        List<TestCaseIdExtractor> extractors = registry.getExtractors();
        
        // Check that extractors are sorted by priority (highest first)
        assertTrue("Should have multiple extractors", extractors.size() >= 3);
        
        // UnittestCaseInfo should have highest priority (100)
        assertTrue("First should be UnittestCaseInfo", 
            extractors.get(0) instanceof UnittestCaseInfoTestCaseIdExtractor);
        
        // TestCaseId should be second (90)
        assertTrue("Second should be TestCaseId", 
            extractors.get(1) instanceof TestCaseIdAnnotationExtractor);
        
        // JUnit Tag should be third (50)
        assertTrue("Third should be JUnit Tag", 
            extractors.get(2) instanceof JUnitTagTestCaseIdExtractor);
    }
    
    // ============================================================================
    // Test getSupportingExtractors
    // ============================================================================
    
    @Test
    public void testGetSupportingExtractors() {
        String javaCode = 
            "public class TestClass {\n" +
            "    @UnittestCaseInfo(testCaseIds = {\"TC-1234\"})\n" +
            "    public void testMethod() {}\n" +
            "}";
        
        try {
            JavaParser parser = new JavaParser();
            CompilationUnit cu = parser.parse(javaCode).getResult().get();
            MethodDeclaration method = cu.findFirst(MethodDeclaration.class).get();
            AnnotationExpr annotation = method.getAnnotationByName("UnittestCaseInfo").get();
            
            List<TestCaseIdExtractor> supporting = registry.getSupportingExtractors(annotation);
            
            assertEquals("Should have 1 supporting extractor", 1, supporting.size());
            assertTrue("Should be UnittestCaseInfo extractor", 
                supporting.get(0) instanceof UnittestCaseInfoTestCaseIdExtractor);
            
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }
    
    // ============================================================================
    // Test Edge Cases
    // ============================================================================
    
    @Test
    public void testNullAnnotation() {
        String[] ids = registry.extractTestCaseIds((AnnotationExpr) null);
        assertEquals("Should return empty array for null", 0, ids.length);
    }
    
    @Test
    public void testNullAnnotationList() {
        String[] ids = registry.extractTestCaseIds((List<AnnotationExpr>) null);
        assertEquals("Should return empty array for null list", 0, ids.length);
    }
    
    @Test
    public void testUnsupportedAnnotation() {
        String javaCode = 
            "public class TestClass {\n" +
            "    @Test\n" +
            "    public void testMethod() {}\n" +
            "}";
        
        String[] ids = extractIds(javaCode, "Test");
        assertEquals("Should return empty for unsupported annotation", 0, ids.length);
    }
    
    @Test
    public void testEmptyAnnotation() {
        String javaCode = 
            "public class TestClass {\n" +
            "    @UnittestCaseInfo\n" +
            "    public void testMethod() {}\n" +
            "}";
        
        String[] ids = extractIds(javaCode, "UnittestCaseInfo");
        assertEquals("Should return empty for empty annotation", 0, ids.length);
    }
    
    // ============================================================================
    // Helper Methods
    // ============================================================================
    
    private String[] extractIds(String javaCode, String annotationName) {
        try {
            JavaParser parser = new JavaParser();
            CompilationUnit cu = parser.parse(javaCode).getResult().get();
            MethodDeclaration method = cu.findFirst(MethodDeclaration.class).get();
            AnnotationExpr annotation = method.getAnnotationByName(annotationName).orElse(null);
            
            if (annotation == null) {
                return new String[0];
            }
            
            return registry.extractTestCaseIds(annotation);
            
        } catch (Exception e) {
            fail("Failed to parse Java code: " + e.getMessage());
            return new String[0];
        }
    }
    
    private boolean arrayContains(String[] array, String value) {
        return Arrays.asList(array).contains(value);
    }
}

