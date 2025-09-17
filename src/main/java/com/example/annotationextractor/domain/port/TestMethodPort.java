package com.example.annotationextractor.domain.port;

import com.example.annotationextractor.domain.model.TestMethod;
import java.util.List;
import java.util.Optional;

/**
 * Read-only port for accessing test methods and annotation metadata.
 */
public interface TestMethodPort {
    Optional<TestMethod> findById(Long id);
    List<TestMethod> findByTestClassId(Long testClassId);
    List<TestMethod> findByScanSessionId(Long scanSessionId);
    List<TestMethod> findAnnotatedByRepository(Long repositoryId);
    long count();
}


