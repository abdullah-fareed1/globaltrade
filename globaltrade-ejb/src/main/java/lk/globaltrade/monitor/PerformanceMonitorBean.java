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


@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class PerformanceMonitorBean implements PerformanceMonitorBeanLocal {

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

        Map<String, MethodStats> copy = new HashMap<>();
        for (Map.Entry<String, MethodStats> entry : stats.entrySet()) {
            copy.put(entry.getKey(), entry.getValue().copyOf());
        }
        return Collections.unmodifiableMap(copy);
    }

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