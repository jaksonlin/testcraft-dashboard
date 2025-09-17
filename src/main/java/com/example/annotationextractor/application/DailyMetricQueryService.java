package com.example.annotationextractor.application;

import com.example.annotationextractor.domain.model.DailyMetric;
import com.example.annotationextractor.domain.port.DailyMetricPort;

import java.time.LocalDate;
import java.util.List;

public class DailyMetricQueryService {

    private final DailyMetricPort dailyMetricPort;

    public DailyMetricQueryService(DailyMetricPort dailyMetricPort) {
        this.dailyMetricPort = dailyMetricPort;
    }

    public List<DailyMetric> recent(int limit) { return dailyMetricPort.findRecent(limit); }

    public List<DailyMetric> range(LocalDate startInclusive, LocalDate endInclusive) {
        return dailyMetricPort.findRange(startInclusive, endInclusive);
    }

    public long count() { return dailyMetricPort.count(); }
}


