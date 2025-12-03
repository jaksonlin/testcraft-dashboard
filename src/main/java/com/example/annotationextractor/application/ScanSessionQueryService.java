package com.example.annotationextractor.application;

import com.example.annotationextractor.domain.model.ScanSession;
import com.example.annotationextractor.domain.port.ScanSessionPort;

import java.util.List;
import java.util.Optional;

public class ScanSessionQueryService {

    private final ScanSessionPort scanSessionPort;

    public ScanSessionQueryService(ScanSessionPort scanSessionPort) {
        this.scanSessionPort = scanSessionPort;
    }

    public List<ScanSession> recent(int limit) {
        return scanSessionPort.findRecent(limit);
    }

    public Optional<ScanSession> getLatestCompleted() {
        return scanSessionPort.findLatestCompleted();
    }

    public List<ScanSession> listAll() {
        return scanSessionPort.findAll();
    }

    public long count() {
        return scanSessionPort.count();
    }

    public Optional<Long> getLatestScanSessionIdForRepository(Long repositoryId) {
        return scanSessionPort.findLatestScanSessionIdForRepository(repositoryId);
    }
}
