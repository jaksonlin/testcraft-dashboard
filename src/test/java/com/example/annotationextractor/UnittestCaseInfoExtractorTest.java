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

    @Test
    public void testExtractTestCaseIds() {
        // Test the new testCaseIds field
        String javaCode = 
            "public class TestClass {\n" +
            "    @UnittestCaseInfo(\n" +
            "        testCaseIds = {\"TC-1234\", \"TC-5678\"},\n" +
            "        title = \"Test with Case IDs\"\n" +
            "    )\n" +
            "    public void testWithCaseIds() {\n" +
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
            assertArrayEquals("Test case IDs should match", new String[]{"TC-1234", "TC-5678"}, extractedData.getTestCaseIds());
            assertArrayEquals("getAllTestCaseIds should return testCaseIds", new String[]{"TC-1234", "TC-5678"}, extractedData.getAllTestCaseIds());
            
            System.out.println("Test case IDs extracted: " + extractedData);
            
        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    public void testBackwardCompatibility_TagsWithTestCaseIds() {
        // Test backward compatibility: extracting test case IDs from tags
        String javaCode = 
            "public class TestClass {\n" +
            "    @UnittestCaseInfo(\n" +
            "        tags = {\"TC-1234\", \"integration\", \"ID-5678\", \"smoke\"},\n" +
            "        title = \"Test with IDs in Tags\"\n" +
            "    )\n" +
            "    public void testWithIdsInTags() {\n" +
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
            assertArrayEquals("Tags should include all values", new String[]{"TC-1234", "integration", "ID-5678", "smoke"}, extractedData.getTags());
            assertArrayEquals("TestCaseIds field should be empty", new String[0], extractedData.getTestCaseIds());
            
            // getAllTestCaseIds should extract TC-1234 and ID-5678 from tags
            String[] allIds = extractedData.getAllTestCaseIds();
            assertEquals("Should extract 2 test case IDs from tags", 2, allIds.length);
            assertTrue("Should contain TC-1234", arrayContains(allIds, "TC-1234"));
            assertTrue("Should contain ID-5678", arrayContains(allIds, "ID-5678"));
            
            System.out.println("Backward compatibility test - IDs from tags: " + extractedData);
            
        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    public void testPriority_TestCaseIdsOverTags() {
        // Test that testCaseIds takes priority over tags
        String javaCode = 
            "public class TestClass {\n" +
            "    @UnittestCaseInfo(\n" +
            "        testCaseIds = {\"TC-1234\"},\n" +
            "        tags = {\"TC-9999\", \"integration\"},\n" +
            "        title = \"Test Priority\"\n" +
            "    )\n" +
            "    public void testPriority() {\n" +
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
            assertArrayEquals("TestCaseIds should be TC-1234", new String[]{"TC-1234"}, extractedData.getTestCaseIds());
            assertArrayEquals("Tags should include TC-9999", new String[]{"TC-9999", "integration"}, extractedData.getTags());
            
            // getAllTestCaseIds should return testCaseIds, ignoring tags
            String[] allIds = extractedData.getAllTestCaseIds();
            assertArrayEquals("Should use testCaseIds field, not tags", new String[]{"TC-1234"}, allIds);
            
            System.out.println("Priority test - testCaseIds over tags: " + extractedData);
            
        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    public void testTestCaseIdPatternRecognition() {
        // Test various ID patterns
        String javaCode = 
            "public class TestClass {\n" +
            "    @UnittestCaseInfo(\n" +
            "        tags = {\"TC-123\", \"ID-456\", \"REQ-789\", \"TS-012\", \"CASE-555\", \"invalid-id\", \"test123\", \"TC123\"},\n" +
            "        title = \"Test Pattern Recognition\"\n" +
            "    )\n" +
            "    public void testPatterns() {\n" +
            "        // Test implementation\n" +
            "    }\n" +
            "}";

        try {
            JavaParser parser = new JavaParser();
            CompilationUnit cu = parser.parse(javaCode).getResult().get();
            
            MethodDeclaration method = cu.findFirst(MethodDeclaration.class).get();
            AnnotationExpr annotation = method.getAnnotationByName("UnittestCaseInfo").get();
            
            UnittestCaseInfoData extractedData = UnittestCaseInfoExtractor.extractAnnotationValues(annotation);
            
            String[] allIds = extractedData.getAllTestCaseIds();
            
            // Should recognize: TC-123, ID-456, REQ-789, TS-012, CASE-555
            // Should NOT recognize: invalid-id, test123, TC123
            assertEquals("Should extract 5 valid IDs", 5, allIds.length);
            assertTrue("Should contain TC-123", arrayContains(allIds, "TC-123"));
            assertTrue("Should contain ID-456", arrayContains(allIds, "ID-456"));
            assertTrue("Should contain REQ-789", arrayContains(allIds, "REQ-789"));
            assertTrue("Should contain TS-012", arrayContains(allIds, "TS-012"));
            assertTrue("Should contain CASE-555", arrayContains(allIds, "CASE-555"));
            assertFalse("Should NOT contain invalid-id", arrayContains(allIds, "invalid-id"));
            assertFalse("Should NOT contain test123", arrayContains(allIds, "test123"));
            assertFalse("Should NOT contain TC123", arrayContains(allIds, "TC123"));
            
            System.out.println("Pattern recognition test: " + extractedData);
            
        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    public void testEmptyTestCaseIds() {
        // Test when neither testCaseIds nor tags have test case IDs
        String javaCode = 
            "public class TestClass {\n" +
            "    @UnittestCaseInfo(\n" +
            "        tags = {\"integration\", \"smoke\"},\n" +
            "        title = \"Test Without IDs\"\n" +
            "    )\n" +
            "    public void testNoIds() {\n" +
            "        // Test implementation\n" +
            "    }\n" +
            "}";

        try {
            JavaParser parser = new JavaParser();
            CompilationUnit cu = parser.parse(javaCode).getResult().get();
            
            MethodDeclaration method = cu.findFirst(MethodDeclaration.class).get();
            AnnotationExpr annotation = method.getAnnotationByName("UnittestCaseInfo").get();
            
            UnittestCaseInfoData extractedData = UnittestCaseInfoExtractor.extractAnnotationValues(annotation);
            
            String[] allIds = extractedData.getAllTestCaseIds();
            assertEquals("Should return empty array when no IDs present", 0, allIds.length);
            
            System.out.println("Empty IDs test: " + extractedData);
            
        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    // Helper method to check if array contains a value
    private boolean arrayContains(String[] array, String value) {
        for (String item : array) {
            if (item.equals(value)) {
                return true;
            }
        }
        return false;
    }
}
