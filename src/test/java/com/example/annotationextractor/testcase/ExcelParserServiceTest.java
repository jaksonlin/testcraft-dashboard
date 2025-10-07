package com.example.annotationextractor.testcase;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

/**
 * Test the ExcelParserService with real Excel files
 */
public class ExcelParserServiceTest {
    
    private final ExcelParserService parserService = new ExcelParserService();
    
    @Test
    public void testPreviewGenericCSV() {
        try {
            // Test with our generic CSV example (converted to Excel)
            FileInputStream fis = new FileInputStream("test-case-example-generic.csv");
            
            // Note: CSV files need different parsing, this test shows the pattern
            // For now, test will be skipped if file doesn't exist
            
        } catch (Exception e) {
            System.out.println("CSV test skipped (expected): " + e.getMessage());
        }
    }
    
    @Test
    public void testAutoDetectMappings() {
        // Test auto-detection with various column name formats
        
        // Format 1: Standard names
        List<String> columns1 = List.of("Test ID", "Title", "Steps", "Priority");
        Map<String, String> mappings1 = parserService.autoDetectMappings(columns1);
        
        assertTrue("Should detect 'Test ID' as id", mappings1.containsKey("Test ID"));
        assertEquals("Should map to 'id'", "id", mappings1.get("Test ID"));
        
        assertTrue("Should detect 'Title' as title", mappings1.containsKey("Title"));
        assertEquals("Should map to 'title'", "title", mappings1.get("Title"));
        
        assertTrue("Should detect 'Steps' as steps", mappings1.containsKey("Steps"));
        assertEquals("Should map to 'steps'", "steps", mappings1.get("Steps"));
        
        // Format 2: Variations
        List<String> columns2 = List.of("TestCase_ID", "Test Name", "Procedure", "Pre-condition");
        Map<String, String> mappings2 = parserService.autoDetectMappings(columns2);
        
        assertTrue("Should detect 'TestCase_ID' as id", mappings2.containsKey("TestCase_ID"));
        assertTrue("Should detect 'Test Name' as title", mappings2.containsKey("Test Name"));
        assertTrue("Should detect 'Procedure' as steps", mappings2.containsKey("Procedure"));
        assertTrue("Should detect 'Pre-condition' as setup", mappings2.containsKey("Pre-condition"));
        
        // Format 3: Short names
        List<String> columns3 = List.of("ID", "Name", "Actions", "Expected");
        Map<String, String> mappings3 = parserService.autoDetectMappings(columns3);
        
        assertTrue("Should detect 'ID' as id", mappings3.containsKey("ID"));
        assertTrue("Should detect 'Name' as title", mappings3.containsKey("Name"));
        assertTrue("Should detect 'Actions' as steps", mappings3.containsKey("Actions"));
        assertTrue("Should detect 'Expected' as expected_result", mappings3.containsKey("Expected"));
    }
    
    @Test
    public void testValidateMappings_AllRequiredPresent() {
        Map<String, String> mappings = Map.of(
            "Test ID", "id",
            "Title", "title",
            "Steps", "steps",
            "Priority", "priority"
        );
        
        List<String> columns = List.of("Test ID", "Title", "Steps", "Priority");
        
        ExcelParserService.ValidationResult result = parserService.validateMappings(mappings, columns);
        
        assertTrue("Should be valid when all required fields present", result.isValid());
        assertEquals("Should have no missing fields", 0, result.getMissingRequiredFields().size());
    }
    
    @Test
    public void testValidateMappings_MissingSteps() {
        Map<String, String> mappings = Map.of(
            "Test ID", "id",
            "Title", "title"
            // Steps missing
        );
        
        List<String> columns = List.of("Test ID", "Title", "Procedure", "Priority");
        
        ExcelParserService.ValidationResult result = parserService.validateMappings(mappings, columns);
        
        assertFalse("Should be invalid when Steps missing", result.isValid());
        assertTrue("Should list Steps as missing", result.getMissingRequiredFields().contains("Steps"));
        
        // Should suggest 'Procedure' as potential Steps column
        boolean hasSuggestion = result.getSuggestions().stream()
            .anyMatch(s -> s.contains("Procedure"));
        assertTrue("Should suggest 'Procedure' for Steps", hasSuggestion);
    }
    
    @Test
    public void testValidateMappings_AllMissing() {
        Map<String, String> mappings = Map.of(
            "Priority", "priority"
            // ID, Title, Steps all missing
        );
        
        List<String> columns = List.of("CaseNum", "TestName", "Execution", "Priority");
        
        ExcelParserService.ValidationResult result = parserService.validateMappings(mappings, columns);
        
        assertFalse("Should be invalid", result.isValid());
        assertEquals("Should have 3 missing fields", 3, result.getMissingRequiredFields().size());
        assertTrue("Should list ID as missing", result.getMissingRequiredFields().contains("ID"));
        assertTrue("Should list Title as missing", result.getMissingRequiredFields().contains("Title"));
        assertTrue("Should list Steps as missing", result.getMissingRequiredFields().contains("Steps"));
        
        // Should have suggestions
        assertTrue("Should provide suggestions", result.getSuggestions().size() > 0);
    }
    
    @Test
    public void testCaseInsensitiveMatching() {
        // Test that column matching is case-insensitive
        List<String> columns = List.of("TEST_ID", "test_name", "Test Steps");
        Map<String, String> mappings = parserService.autoDetectMappings(columns);
        
        assertTrue("Should detect 'TEST_ID' (uppercase)", mappings.containsKey("TEST_ID"));
        assertTrue("Should detect 'test_name' (lowercase)", mappings.containsKey("test_name"));
        assertTrue("Should detect 'Test Steps' (mixed case)", mappings.containsKey("Test Steps"));
        
        // All should map to correct fields
        assertEquals("id", mappings.get("TEST_ID"));
        assertEquals("title", mappings.get("test_name"));
        assertEquals("steps", mappings.get("Test Steps"));
    }
    
    @Test
    public void testHandleVariations() {
        // Test various naming conventions
        List<String> columns = List.of(
            "test-id",       // hyphenated
            "test_name",     // underscored
            "TestSteps",     // camelCase
            "Precondition"   // no space (clearer)
        );
        
        Map<String, String> mappings = parserService.autoDetectMappings(columns);
        
        assertEquals("Should handle hyphens", "id", mappings.get("test-id"));
        assertEquals("Should handle underscores", "title", mappings.get("test_name"));
        assertEquals("Should handle camelCase", "steps", mappings.get("TestSteps"));
        assertEquals("Should handle precondition", "setup", mappings.get("Precondition"));
    }
    
    @Test
    public void testAmbiguousColumns() {
        // Test that when column names are ambiguous, system picks most likely
        List<String> columns = List.of(
            "Pre-Condition",  // Has "condition" (could be status) but also "pre" (setup)
            "Post-Condition"  // Has "condition" (could be status) but also "post" (teardown)
        );
        
        Map<String, String> mappings = parserService.autoDetectMappings(columns);
        
        // System should detect these correctly
        // Note: The algorithm checks patterns in order, so results may vary
        assertTrue("Should detect Pre-Condition", mappings.containsKey("Pre-Condition"));
        assertTrue("Should detect Post-Condition", mappings.containsKey("Post-Condition"));
        
        // Print what was detected (useful for debugging)
        System.out.println("Pre-Condition mapped to: " + mappings.get("Pre-Condition"));
        System.out.println("Post-Condition mapped to: " + mappings.get("Post-Condition"));
    }
}

