// Path: globaltrade-ejb/src/main/java/lk/globaltrade/interceptor/PerformanceInterceptor.java
package lk.globaltrade.interceptor;

import jakarta.ejb.EJB;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import lk.globaltrade.monitor.PerformanceMonitorBeanLocal;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wraps ctx.proceed() in try/finally (not try/catch-only-on-success) so
 * that a failed business call is still timed and recorded — matching
 * CONTRACTS.md §7's same principle for AuditInterceptor: the
 * performance data must not silently disappear on the failure path.
 *
 * Bound in ejb-jar.xml (not @Interceptors), second in the
 * Security -> Performance -> Audit order, so timing starts only after
 * SecurityInterceptor has already let the call through.
 */
public class PerformanceInterceptor {

    private static final Logger LOG = Logger.getLogger(PerformanceInterceptor.class.getName());

    /** Matches PerformanceMonitorBean.SLOW_THRESHOLD_NANOS (500 ms). */
    private static final long SLOW_THRESHOLD_NANOS = 500_000_000L;

    @EJB
    private PerformanceMonitorBeanLocal performanceMonitor;

    @AroundInvoke
    public Object measure(InvocationContext ctx) throws Exception {
        long start = System.nanoTime();
        try {
            return ctx.proceed();
        } finally {
            long durationNanos = System.nanoTime() - start;
            performanceMonitor.recordCall(ctx.getMethod().getName(), durationNanos);

            if (durationNanos > SLOW_THRESHOLD_NANOS) {
                LOG.log(Level.WARNING, "Slow call: {0}.{1} took {2} ms",
                        new Object[]{
                                ctx.getTarget().getClass().getSimpleName(),
                                ctx.getMethod().getName(),
                                durationNanos / 1_000_000.0
                        });
            }
        }
    }
}