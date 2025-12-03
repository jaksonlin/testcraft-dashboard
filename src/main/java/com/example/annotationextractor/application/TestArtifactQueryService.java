package com.example.annotationextractor.application;

import com.example.annotationextractor.domain.model.TestClass;
import com.example.annotationextractor.domain.model.TestMethod;
import com.example.annotationextractor.domain.model.TestMethodDetailRecord;
import com.example.annotationextractor.domain.port.TestClassPort;
import com.example.annotationextractor.domain.port.TestMethodPort;

import java.util.List;
import java.util.Optional;

public class TestArtifactQueryService {

    private final TestClassPort testClassPort;
    private final TestMethodPort testMethodPort;

    public TestArtifactQueryService(TestClassPort testClassPort, TestMethodPort testMethodPort) {
        this.testClassPort = testClassPort;
        this.testMethodPort = testMethodPort;
    }

    public List<TestClass> listClassesByRepositoryIdAndScanSessionId(Long repositoryId, Long scanSessionId) {
        return testClassPort.findByRepositoryIdAndScanSessionId(repositoryId, scanSessionId);
    }

    public Optional<TestClass> listClassByRepositoryIdAndScanSessionIdAndFilePath(Long repositoryId, Long scanSessionId,
            String filePath) {
        return testClassPort.findByRepositoryIdAndScanSessionIdAndFilePath(repositoryId, scanSessionId, filePath);
    }

    public List<TestClass> listClassesByScanSessionId(Long scanSessionId) {
        return testClassPort.findAllByScanSessionId(scanSessionId);
    }

    public long countClassesByScanSessionId(Long scanSessionId) {
        return testClassPort.countAllByScanSessionId(scanSessionId);
    }

    public Optional<TestClass> getTestClassById(Long classId) {
        return testClassPort.findById(classId);
    }

    public List<TestMethod> listMethodsByTestClassId(Long testClassId) {
        return testMethodPort.findByTestClassId(testClassId);
    }

    public List<TestMethod> listMethodsByScanSessionId(Long scanSessionId) {
        return testMethodPort.findByScanSessionId(scanSessionId);
    }

    public List<TestMethod> listAnnotatedMethodsByRepositoryIdAndScanSessionId(Long repositoryId, Long scanSessionId) {
        return testMethodPort.findAnnotatedByRepositoryAndScanSessionId(repositoryId, scanSessionId);
    }

    public List<TestMethodDetailRecord> listTestMethodDetailsByTeamIdAndScanSessionId(Long teamId, Long scanSessionId,
            Integer limit) {
        return testMethodPort.findTestMethodDetailsByTeamIdAndScanSessionId(teamId, scanSessionId, limit);
    }

    public long countTestMethodDetailsByTeamIdAndScanSessionId(Long teamId, Long scanSessionId) {
        return testMethodPort.countByTeamIdAndScanSessionId(teamId, scanSessionId);
    }

    public List<TestMethodDetailRecord> listTestMethodDetailsByRepositoryIdAndScanSessionId(Long repositoryId,
            Long scanSessionId, Integer limit) {
        return testMethodPort.findTestMethodDetailsByRepositoryIdAndScanSessionId(repositoryId, scanSessionId, limit);
    }

    public long countTestMethodDetailsByRepositoryIdAndScanSessionId(Long repositoryId, Long scanSessionId) {
        return testMethodPort.countByRepositoryIdAndScanSessionId(repositoryId, scanSessionId);
    }

    public List<TestMethodDetailRecord> listTestMethodDetailsByClassId(Long classId, Integer limit) {
        return testMethodPort.findTestMethodDetailsByClassId(classId, limit);
    }

    public long countTestMethodDetailsByClassId(Long classId) {
        return testMethodPort.countByClassId(classId);
    }

    public List<TestMethodDetailRecord> listTestMethodDetailsByScanSessionId(Long scanSessionId, Integer limit) {
        return testMethodPort.findTestMethodDetailsByScanSessionId(scanSessionId, limit);
    }

    public long countTestMethodDetailsByScanSessionId(Long scanSessionId) {
        return testMethodPort.countByScanSessionId(scanSessionId);
    }

    /**
     * List test method details with DATABASE-level filtering (no client-side
     * filtering)
     * All filters are applied via SQL WHERE clauses for optimal performance
     */
    public List<TestMethodDetailRecord> listTestMethodDetailsWithFilters(
            Long scanSessionId,
            String teamName,
            String repositoryName,
            String packageName,
            String className,
            Boolean annotated,
            String searchTerm,
            String codePattern,
            Integer offset,
            Integer limit) {
        // Cast to concrete implementation to access new method
        if (testMethodPort instanceof com.example.annotationextractor.adapters.persistence.jdbc.JdbcTestMethodAdapter) {
            com.example.annotationextractor.adapters.persistence.jdbc.JdbcTestMethodAdapter adapter = (com.example.annotationextractor.adapters.persistence.jdbc.JdbcTestMethodAdapter) testMethodPort;
            return adapter.findTestMethodDetailsWithFilters(
                    scanSessionId, teamName, repositoryName, packageName, className, annotated, searchTerm, codePattern,
                    offset, limit);
        }
        // Fallback for other implementations (shouldn't happen)
        return List.of();
    }

    public List<TestMethodDetailRecord> listTestMethodDetailsWithFilters(
            List<Long> scanSessionIds,
            String teamName,
            String repositoryName,
            String packageName,
            String className,
            Boolean annotated,
            String searchTerm,
            String codePattern,
            Integer offset,
            Integer limit) {
        return testMethodPort.findTestMethodDetailsWithFilters(
                scanSessionIds, teamName, repositoryName, packageName, className, annotated, searchTerm, codePattern,
                offset, limit);
    }

    /**
     * Count test method details with filters for pagination
     */
    public long countTestMethodDetailsWithFilters(
            Long scanSessionId,
            String teamName,
            String repositoryName,
            String packageName,
            String className,
            Boolean annotated,
            String searchTerm,
            String codePattern) {
        // Cast to concrete implementation to access new method
        if (testMethodPort instanceof com.example.annotationextractor.adapters.persistence.jdbc.JdbcTestMethodAdapter) {
            com.example.annotationextractor.adapters.persistence.jdbc.JdbcTestMethodAdapter adapter = (com.example.annotationextractor.adapters.persistence.jdbc.JdbcTestMethodAdapter) testMethodPort;
            return adapter.countTestMethodDetailsWithFilters(
                    scanSessionId, teamName, repositoryName, packageName, className, annotated, searchTerm,
                    codePattern);
        }
        // Fallback for other implementations (shouldn't happen)
        return 0;
    }

    public long countTestMethodDetailsWithFilters(
            List<Long> scanSessionIds,
            String teamName,
            String repositoryName,
            String packageName,
            String className,
            Boolean annotated,
            String searchTerm,
            String codePattern) {
        return testMethodPort.countTestMethodDetailsWithFilters(
                scanSessionIds, teamName, repositoryName, packageName, className, annotated, searchTerm, codePattern);
    }

    public Optional<TestMethod> getTestMethodById(Long methodId) {
        return testMethodPort.findById(methodId);
    }

    /**
     * Get hierarchical summary grouped by teams
     */
    public List<java.util.Map<String, Object>> getHierarchyByTeam(Long scanSessionId) {
        if (testMethodPort instanceof com.example.annotationextractor.adapters.persistence.jdbc.JdbcTestMethodAdapter) {
            com.example.annotationextractor.adapters.persistence.jdbc.JdbcTestMethodAdapter adapter = (com.example.annotationextractor.adapters.persistence.jdbc.JdbcTestMethodAdapter) testMethodPort;
            return adapter.getHierarchyByTeam(scanSessionId);
        }
        return java.util.List.of();
    }

    public List<java.util.Map<String, Object>> getHierarchyByTeam(List<Long> scanSessionIds) {
        return testMethodPort.getHierarchyByTeam(scanSessionIds);
    }

    /**
     * Get hierarchical summary grouped by packages within a team
     */
    public List<java.util.Map<String, Object>> getHierarchyByPackage(Long scanSessionId, String teamName) {
        if (testMethodPort instanceof com.example.annotationextractor.adapters.persistence.jdbc.JdbcTestMethodAdapter) {
            com.example.annotationextractor.adapters.persistence.jdbc.JdbcTestMethodAdapter adapter = (com.example.annotationextractor.adapters.persistence.jdbc.JdbcTestMethodAdapter) testMethodPort;
            return adapter.getHierarchyByPackage(scanSessionId, teamName);
        }
        return java.util.List.of();
    }

    public List<java.util.Map<String, Object>> getHierarchyByPackage(List<Long> scanSessionIds, String teamName) {
        return testMethodPort.getHierarchyByPackage(scanSessionIds, teamName);
    }

    /**
     * Get hierarchical summary grouped by classes within a package
     */
    public List<java.util.Map<String, Object>> getHierarchyByClass(Long scanSessionId, String teamName,
            String packageName) {
        if (testMethodPort instanceof com.example.annotationextractor.adapters.persistence.jdbc.JdbcTestMethodAdapter) {
            com.example.annotationextractor.adapters.persistence.jdbc.JdbcTestMethodAdapter adapter = (com.example.annotationextractor.adapters.persistence.jdbc.JdbcTestMethodAdapter) testMethodPort;
            return adapter.getHierarchyByClass(scanSessionId, teamName, packageName);
        }
        return java.util.List.of();
    }

    public List<java.util.Map<String, Object>> getHierarchyByClass(List<Long> scanSessionIds, String teamName,
            String packageName) {
        return testMethodPort.getHierarchyByClass(scanSessionIds, teamName, packageName);
    }

}
