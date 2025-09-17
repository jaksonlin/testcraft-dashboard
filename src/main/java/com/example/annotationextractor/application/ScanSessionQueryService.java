package com.example.annotationextractor.application;

import com.example.annotationextractor.domain.model.ScanSession;
import com.example.annotationextractor.domain.port.ScanSessionPort;

import java.util.List;

public class ScanSessionQueryService {

    private final ScanSessionPort scanSessionPort;

    public ScanSessionQueryService(ScanSessionPort scanSessionPort) {
        this.scanSessionPort = scanSessionPort;
    }

    public List<ScanSession> recent(int limit) {
        return scanSessionPort.findRecent(limit);
    }

    public List<ScanSession> listAll() {
        return scanSessionPort.findAll();
    }

    public long count() { return scanSessionPort.count(); }
}


