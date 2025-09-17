package com.example.annotationextractor.application;

import com.example.annotationextractor.domain.model.RepositoryRecord;
import com.example.annotationextractor.domain.port.RepositoryRecordPort;

import java.util.List;
import java.util.Optional;

public class RepositoryQueryService {

    private final RepositoryRecordPort repositoryRecordPort;

    public RepositoryQueryService(RepositoryRecordPort repositoryRecordPort) {
        this.repositoryRecordPort = repositoryRecordPort;
    }

    public Optional<RepositoryRecord> getByGitUrl(String gitUrl) {
        return repositoryRecordPort.findByGitUrl(gitUrl);
    }

    public List<RepositoryRecord> listByTeamId(Long teamId) {
        return repositoryRecordPort.findByTeamId(teamId);
    }

    public List<RepositoryRecord> listAll() {
        return repositoryRecordPort.findAll();
    }

    public long count() { return repositoryRecordPort.count(); }
}


