// Path: globaltrade-ejb/src/main/java/lk/globaltrade/monitor/PerformanceMonitorBean.java
package lk.globaltrade.monitor;

import jakarta.ejb.ConcurrencyManagement;
import jakarta.ejb.ConcurrencyManagementType;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Container-wide performance counter, one entry per intercepted method
 * name. Populated exclusively by PerformanceInterceptor (Phase 3).
 *
 * Locking (CONTRACTS.md §4 / BUILD_PLAN Phase 1):
 * - recordCall()  -> @Lock(WRITE): every intercepted business call
 *   serialises behind this method. That contention is intentional and
 *   is measured/discussed in the performance-analysis deliverable, not
 *   silently optimised away.
 * - getSnapshot() -> @Lock(READ): concurrent reads allowed, blocked
 *   only while a write is in progress.
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class PerformanceMonitorBean implements PerformanceMonitorBeanLocal {

    /** 500 ms — calls slower than this increment MethodStats.slowCount. */
    private static final long SLOW_THRESHOLD_NANOS = 500_000_000L;

    private final Map<String, MethodStats> stats = new ConcurrentHashMap<>();

    @Override
    @Lock(LockType.WRITE)
    public void recordCall(String methodName, long durationNanos) {
        MethodStats current = stats.computeIfAbsent(methodName, k -> new MethodStats());
        current.callCount++;
        current.totalNanos += durationNanos;
        if (durationNanos < current.minNanos) {
            current.minNanos = durationNanos;
        }
        if (durationNanos > current.maxNanos) {
            current.maxNanos = durationNanos;
        }
        if (durationNanos > SLOW_THRESHOLD_NANOS) {
            current.slowCount++;
        }
    }

    @Override
    @Lock(LockType.READ)
    public Map<String, MethodStats> getSnapshot() {
        // Defensive copy: neither the map nor the MethodStats instances
        // inside it are the live, mutable ones.
        Map<String, MethodStats> copy = new HashMap<>();
        for (Map.Entry<String, MethodStats> entry : stats.entrySet()) {
            copy.put(entry.getKey(), entry.getValue().copyOf());
        }
        return Collections.unmodifiableMap(copy);
    }

    /**
     * Per-method aggregate stats. MUST stay public static: it is read
     * from performance.jsp in the web module (a different module,
     * across the EAR boundary) via JSP EL, which needs public,
     * no-underscore getters on a publicly reachable, statically nested
     * type. Non-public or non-static here means EL silently renders
     * blank cells with no error — a defect that is very easy to miss.
     */
    public static class MethodStats implements Serializable {
        private long callCount;
        private long totalNanos;
        private long slowCount;
        private long minNanos = Long.MAX_VALUE;
        private long maxNanos;

        public long getCallCount() {
            return callCount;
        }

        public long getSlowCount() {
            return slowCount;
        }

        public long getMinNanos() {
            return minNanos == Long.MAX_VALUE ? 0 : minNanos;
        }

        public long getMaxNanos() {
            return maxNanos;
        }

        public double getAvgMillis() {
            return callCount == 0 ? 0.0 : (totalNanos / (double) callCount) / 1_000_000.0;
        }

        private MethodStats copyOf() {
            MethodStats copy = new MethodStats();
            copy.callCount = this.callCount;
            copy.totalNanos = this.totalNanos;
            copy.slowCount = this.slowCount;
            copy.minNanos = this.minNanos;
            copy.maxNanos = this.maxNanos;
            return copy;
        }
    }
}