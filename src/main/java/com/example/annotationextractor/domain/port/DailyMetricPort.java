package com.example.annotationextractor.domain.port;

import com.example.annotationextractor.domain.model.DailyMetric;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Read-only port for daily_metrics aggregate.
 */
public interface DailyMetricPort {
    Optional<DailyMetric> findByDate(LocalDate date);
    List<DailyMetric> findRange(LocalDate startInclusive, LocalDate endInclusive);
    List<DailyMetric> findRecent(int limit);
    long count();
}


