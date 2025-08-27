package com.example.annotationextractor;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import org.junit.Test;
import static org.junit.Assert.*;

import com.example.annotationextractor.casemodel.UnittestCaseInfoData;
import com.example.annotationextractor.casemodel.UnittestCaseInfoExtractor;

/**
 * Test class for UnittestCaseInfoExtractor
 */
public class UnittestCaseInfoExtractorTest {

    @Test
    public void testExtractAnnotationValues() {
        // Sample Java code with UnittestCaseInfo annotation
        String javaCode = 
            "public class TestClass {\n" +
            "    @UnittestCaseInfo(\n" +
            "        author = \"John Doe\",\n" +
            "        title = \"Test User Login\",\n" +
            "        targetClass = \"UserService\",\n" +
            "        targetMethod = \"login\",\n" +
            "        testPoints = {\"authentication\", \"authorization\"},\n" +
            "        description = \"Test user login functionality\",\n" +
            "        tags = {\"login\", \"user\", \"auth\"},\n" +
            "        status = UnittestCaseStatus.IN_PROGRESS,\n" +
            "        relatedRequirements = {\"REQ-001\", \"REQ-002\"},\n" +
            "        relatedDefects = {\"BUG-001\"},\n" +
            "        relatedTestcases = {\"TC-001\", \"TC-002\"},\n" +
            "        lastUpdateTime = \"2024-01-15\",\n" +
            "        lastUpdateAuthor = \"Jane Smith\",\n" +
            "        methodSignature = \"public boolean login(String username, String password)\"\n" +
            "    )\n" +
            "    public void testUserLogin() {\n" +
            "        // Test implementation\n" +
            "    }\n" +
            "}";

        try {
            // Parse the Java code
            JavaParser parser = new JavaParser();
            CompilationUnit cu = parser.parse(javaCode).getResult().get();
            
            // Find the method with annotation
            MethodDeclaration method = cu.findFirst(MethodDeclaration.class).get();
            
            // Get the annotation
            AnnotationExpr annotation = method.getAnnotationByName("UnittestCaseInfo").get();
            
            // Extract values using our extractor
            UnittestCaseInfoData extractedData = UnittestCaseInfoExtractor.extractAnnotationValues(annotation);
            
            // Verify extracted values
            assertNotNull("Extracted data should not be null", extractedData);
            assertEquals("Author should match", "John Doe", extractedData.getAuthor());
            assertEquals("Title should match", "Test User Login", extractedData.getTitle());
            assertEquals("Target class should match", "UserService", extractedData.getTargetClass());
            assertEquals("Target method should match", "login", extractedData.getTargetMethod());
            assertEquals("Description should match", "Test user login functionality", extractedData.getDescription());
            assertEquals("Last update time should match", "2024-01-15", extractedData.getLastUpdateTime());
            assertEquals("Last update author should match", "Jane Smith", extractedData.getLastUpdateAuthor());
            assertEquals("Method signature should match", "public boolean login(String username, String password)", extractedData.getMethodSignature());
            
            // Verify arrays
            assertArrayEquals("Test points should match", new String[]{"authentication", "authorization"}, extractedData.getTestPoints());
            assertArrayEquals("Tags should match", new String[]{"login", "user", "auth"}, extractedData.getTags());
            assertArrayEquals("Related requirements should match", new String[]{"REQ-001", "REQ-002"}, extractedData.getRelatedRequirements());
            assertArrayEquals("Related defects should match", new String[]{"BUG-001"}, extractedData.getRelatedDefects());
            assertArrayEquals("Related test cases should match", new String[]{"TC-001", "TC-002"}, extractedData.getRelatedTestcases());
            
            // Verify status
            assertTrue("Status should contain UnittestCaseStatus", extractedData.getStatus().contains("UnittestCaseStatus.IN_PROGRESS"));
            
            System.out.println("Extracted data: " + extractedData);
            
        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    public void testExtractAnnotationWithSingleValue() {
        // Test single member annotation
        String javaCode = 
            "public class TestClass {\n" +
            "    @UnittestCaseInfo(\"Simple Test\")\n" +
            "    public void simpleTest() {\n" +
            "        // Test implementation\n" +
            "    }\n" +
            "}";

        try {
            JavaParser parser = new JavaParser();
            CompilationUnit cu = parser.parse(javaCode).getResult().get();
            
            MethodDeclaration method = cu.findFirst(MethodDeclaration.class).get();
            AnnotationExpr annotation = method.getAnnotationByName("UnittestCaseInfo").get();
            
            UnittestCaseInfoData extractedData = UnittestCaseInfoExtractor.extractAnnotationValues(annotation);
            
            assertNotNull("Extracted data should not be null", extractedData);
            assertEquals("Title should match single value", "Simple Test", extractedData.getTitle());
            
            System.out.println("Single value extracted data: " + extractedData);
            
        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    public void testExtractAnnotationWithDefaults() {
        // Test annotation with only required fields
        String javaCode = 
            "public class TestClass {\n" +
            "    @UnittestCaseInfo(\n" +
            "        author = \"Test Author\",\n" +
            "        title = \"Test Title\"\n" +
            "    )\n" +
            "    public void testWithDefaults() {\n" +
            "        // Test implementation\n" +
            "    }\n" +
            "}";

        try {
            JavaParser parser = new JavaParser();
            CompilationUnit cu = parser.parse(javaCode).getResult().get();
            
            MethodDeclaration method = cu.findFirst(MethodDeclaration.class).get();
            AnnotationExpr annotation = method.getAnnotationByName("UnittestCaseInfo").get();
            
            UnittestCaseInfoData extractedData = UnittestCaseInfoExtractor.extractAnnotationValues(annotation);
            
            assertNotNull("Extracted data should not be null", extractedData);
            assertEquals("Author should match", "Test Author", extractedData.getAuthor());
            assertEquals("Title should match", "Test Title", extractedData.getTitle());
            
            // Default values should be empty strings or empty arrays
            assertEquals("Target class should be empty string", "", extractedData.getTargetClass());
            assertEquals("Target method should be empty string", "", extractedData.getTargetMethod());
            assertEquals("Description should be empty string", "", extractedData.getDescription());
            assertEquals("Last update time should be empty string", "", extractedData.getLastUpdateTime());
            assertEquals("Last update author should be empty string", "", extractedData.getLastUpdateAuthor());
            assertEquals("Method signature should be empty string", "", extractedData.getMethodSignature());
            
            assertArrayEquals("Test points should be empty array", new String[0], extractedData.getTestPoints());
            assertArrayEquals("Tags should be empty array", new String[0], extractedData.getTags());
            assertArrayEquals("Related requirements should be empty array", new String[0], extractedData.getRelatedRequirements());
            assertArrayEquals("Related defects should be empty array", new String[0], extractedData.getRelatedDefects());
            assertArrayEquals("Related test cases should be empty array", new String[0], extractedData.getRelatedTestcases());
            
            // Status should default to TODO
            assertEquals("Status should default to TODO", "TODO", extractedData.getStatus());
            
            System.out.println("Default values extracted data: " + extractedData);
            
        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }
}
