package com.example.annotationextractor.domain.port;

import com.example.annotationextractor.domain.model.TestMethod;
import com.example.annotationextractor.domain.model.TestMethodDetailRecord;
import java.util.List;
import java.util.Optional;

/**
 * Read-only port for accessing test methods and annotation metadata.
 */
public interface TestMethodPort {
    Optional<TestMethod> findById(Long id);
    List<TestMethod> findByTestClassId(Long testClassId);
    List<TestMethod> findByScanSessionId(Long scanSessionId);
    List<TestMethod> findAnnotatedByRepositoryAndScanSessionId(Long repositoryId, Long scanSessionId);
    List<TestMethodDetailRecord> findTestMethodDetailsByTeamIdAndScanSessionId(Long teamId, Long scanSessionId, Integer limit);
    long countByTeamIdAndScanSessionId(Long teamId, Long scanSessionId);
    List<TestMethodDetailRecord> findTestMethodDetailsByRepositoryIdAndScanSessionId(Long repositoryId, Long scanSessionId, Integer limit);
    long countByRepositoryIdAndScanSessionId(Long repositoryId, Long scanSessionId);
    List<TestMethodDetailRecord> findTestMethodDetailsByClassId(Long classId, Integer limit);
    long countByClassId(Long classId);
    List<TestMethodDetailRecord> findTestMethodDetailsByScanSessionId(Long scanSessionId, Integer limit);
    long countByScanSessionId(Long scanSessionId);
}


