package com.example.annotationextractor.domain.port;

import com.example.annotationextractor.domain.model.RepositoryRecord;
import com.example.annotationextractor.domain.model.RepositoryDetailRecord;
import java.util.List;
import java.util.Optional;

/**
 * Read-only port for accessing repositories.
 */
public interface RepositoryRecordPort {
    Optional<RepositoryRecord> findById(Long id);
    Optional<RepositoryRecord> findByGitUrl(String gitUrl);
    List<RepositoryRecord> findAll();
    List<RepositoryRecord> findByTeamId(Long teamId);
    List<RepositoryDetailRecord> findRepositoryDetails();
    long count();
}


