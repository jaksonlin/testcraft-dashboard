package com.example.annotationextractor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Map;

/**
 * Performance monitoring utility for large-scale scanning operations
 * Tracks timing, memory usage, and database performance metrics
 */
public class PerformanceMonitor {
    
    private static final Map<String, Long> startTimes = new ConcurrentHashMap<>();
    private static final Map<String, Long> endTimes = new ConcurrentHashMap<>();
    private static final Map<String, AtomicLong> counters = new ConcurrentHashMap<>();
    private static final Map<String, Long> memorySnapshots = new ConcurrentHashMap<>();
    
    /**
     * Start timing an operation
     */
    public static void startOperation(String operationName) {
        startTimes.put(operationName, System.currentTimeMillis());
        memorySnapshots.put(operationName + "_start", getCurrentMemoryUsage());
        System.out.println("🚀 Starting: " + operationName);
    }
    
    /**
     * End timing an operation and report results
     */
    public static void endOperation(String operationName) {
        long endTime = System.currentTimeMillis();
        endTimes.put(operationName, endTime);
        
        Long startTime = startTimes.get(operationName);
        if (startTime != null) {
            long duration = endTime - startTime;
            long memoryStart = memorySnapshots.getOrDefault(operationName + "_start", 0L);
            long memoryEnd = getCurrentMemoryUsage();
            long memoryDelta = memoryEnd - memoryStart;
            
            System.out.println("✅ Completed: " + operationName + 
                             " in " + duration + "ms" +
                             " (Memory: " + formatMemory(memoryDelta) + ")");
        }
    }
    
    /**
     * Increment a counter
     */
    public static void incrementCounter(String counterName) {
        counters.computeIfAbsent(counterName, k -> new AtomicLong(0)).incrementAndGet();
    }
    
    /**
     * Get counter value
     */
    public static long getCounter(String counterName) {
        AtomicLong counter = counters.get(counterName);
        return counter != null ? counter.get() : 0;
    }
    
    /**
     * Report counter values
     */
    public static void reportCounters() {
        System.out.println("\n📊 Performance Counters:");
        System.out.println("========================");
        for (Map.Entry<String, AtomicLong> entry : counters.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue().get());
        }
        System.out.println();
    }
    
    /**
     * Report timing summary
     */
    public static void reportTimingSummary() {
        System.out.println("\n⏱️ Timing Summary:");
        System.out.println("==================");
        for (String operation : startTimes.keySet()) {
            Long startTime = startTimes.get(operation);
            Long endTime = endTimes.get(operation);
            if (startTime != null && endTime != null) {
                long duration = endTime - startTime;
                System.out.println(operation + ": " + duration + "ms");
            }
        }
        System.out.println();
    }
    
    /**
     * Get current memory usage in bytes
     */
    private static long getCurrentMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
    
    /**
     * Format memory size in human-readable format
     */
    private static String formatMemory(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
    
    /**
     * Clear all monitoring data
     */
    public static void clear() {
        startTimes.clear();
        endTimes.clear();
        counters.clear();
        memorySnapshots.clear();
    }
    
    /**
     * Get operation duration
     */
    public static long getOperationDuration(String operationName) {
        Long startTime = startTimes.get(operationName);
        Long endTime = endTimes.get(operationName);
        if (startTime != null && endTime != null) {
            return endTime - startTime;
        }
        return -1;
    }
}
