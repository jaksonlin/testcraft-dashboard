package com.example.annotationextractor.domain.port;

import com.example.annotationextractor.domain.model.TestClass;
import java.util.List;
import java.util.Optional;

/**
 * Read-only port for accessing test classes.
 */
public interface TestClassPort {
    Optional<TestClass> findById(Long id);
    List<TestClass> findByRepositoryIdAndScanSessionId(Long repositoryId, Long scanSessionId);
    Optional<TestClass> findByRepositoryIdAndScanSessionIdAndFilePath(Long repositoryId, Long scanSessionId, String filePath);
    List<TestClass> findAllByScanSessionId(Long scanSessionId);
    long countAllByScanSessionId(Long scanSessionId);
}


