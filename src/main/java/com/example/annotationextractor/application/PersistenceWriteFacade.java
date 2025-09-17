package com.example.annotationextractor.application;

import com.example.annotationextractor.casemodel.TestCollectionSummary;

/**
 * Simple facade for write use-cases.
 */
public class PersistenceWriteFacade {
    private final PersistScanResultsUseCase persistScanResultsUseCase;

    public PersistenceWriteFacade() {
        this.persistScanResultsUseCase = new PersistScanResultsUseCase();
    }

    public long persistScanSession(TestCollectionSummary summary, long scanDurationMs) throws java.sql.SQLException {
        return persistScanResultsUseCase.persist(summary, scanDurationMs);
    }

    public long persistScanSessionShadow(TestCollectionSummary summary, long scanDurationMs) throws java.sql.SQLException {
        return persistScanResultsUseCase.persistToShadow(summary, scanDurationMs);
    }
}


