// Path: globaltrade-ejb/src/main/java/lk/globaltrade/monitor/PerformanceMonitorBeanLocal.java
package lk.globaltrade.monitor;

import jakarta.ejb.Local;

import java.util.Map;

/**
 * Local business interface for {@link PerformanceMonitorBean}.
 *
 * Signature is frozen by CONTRACTS.md §3 — do not add, remove, or
 * reorder parameters.
 */
@Local
public interface PerformanceMonitorBeanLocal {

    /**
     * Records one intercepted method invocation. Called by
     * PerformanceInterceptor (Phase 3) from a try/finally block, so it
     * must be called for both successful and failed invocations.
     *
     * @param methodName    simple name of the intercepted business method
     * @param durationNanos wall-clock duration of the call, in nanoseconds
     */
    void recordCall(String methodName, long durationNanos);

    /**
     * Returns a defensive copy of the current stats snapshot. Never
     * returns the live map — callers (e.g. AdminPerformanceServlet /
     * performance.jsp) must not be able to mutate monitor state.
     */
    Map<String, PerformanceMonitorBean.MethodStats> getSnapshot();
}