package com.example.annotationextractor.domain.port;

import com.example.annotationextractor.domain.model.ScanSession;
import java.util.List;
import java.util.Optional;

/**
 * Read-only port for accessing scan sessions.
 */
public interface ScanSessionPort {
    Optional<ScanSession> findById(Long id);

    List<ScanSession> findAll();

    List<ScanSession> findRecent(int limit);

    Optional<ScanSession> findLatestCompleted();

    long count();

    Optional<Long> findLatestScanSessionIdForRepository(Long repositoryId);
}
