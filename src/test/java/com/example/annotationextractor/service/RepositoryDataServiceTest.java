package com.example.annotationextractor.service;

import com.example.annotationextractor.application.PersistenceReadFacade;
import com.example.annotationextractor.domain.model.TestMethodDetailRecord;
import com.example.annotationextractor.web.dto.TestMethodDetailDto;
import com.example.annotationextractor.web.dto.McpSearchRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class RepositoryDataServiceTest {

    @Mock
    private PersistenceReadFacade persistenceReadFacade;

    private RepositoryDataService repositoryDataService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        repositoryDataService = new RepositoryDataService(Optional.of(persistenceReadFacade)) {
            @Override
            protected Map<Long, Long> getLatestScanSessionIds() {
                return Map.of(1L, 100L);
            }
        };
    }

    @Test
    public void searchCodePattern_ShouldReturnMatchingMethods() {
        // Arrange
        McpSearchRequest request = new McpSearchRequest("testPattern", null, 10);
        TestMethodDetailRecord record = new TestMethodDetailRecord(
                1L, "repo1", "TestClass", "testMethod", 10,
                "Title", "Author", "Status", "TargetClass", "TargetMethod",
                "Description", "Points", List.of("Tag"), List.of("Req"),
                List.of("TC1"), List.of("Defect"), null, "UpdateAuthor",
                "Team", "Code", "http://git.url");

        when(persistenceReadFacade.listTestMethodDetailsWithFilters(
                anyMap(), any(), any(), any(), any(), any(), any(), eq("testPattern"), anyInt(), eq(10)))
                .thenReturn(List.of(record));

        // Act
        List<TestMethodDetailDto> result = repositoryDataService.searchCodePattern(request);

        // Assert
        assertEquals(1, result.size());
        assertEquals("testMethod", result.get(0).getTestMethod());
        assertEquals("repo1", result.get(0).getRepository());
    }
}
