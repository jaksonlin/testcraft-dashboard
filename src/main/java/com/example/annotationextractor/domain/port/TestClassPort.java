package com.example.annotationextractor.domain.port;

import com.example.annotationextractor.domain.model.TestClass;
import java.util.List;
import java.util.Optional;

/**
 * Read-only port for accessing test classes.
 */
public interface TestClassPort {
    Optional<TestClass> findById(Long id);
    List<TestClass> findByRepositoryId(Long repositoryId);
    Optional<TestClass> findByRepositoryIdAndFilePath(Long repositoryId, String filePath);
    List<TestClass> findAll();
    long count();
}


