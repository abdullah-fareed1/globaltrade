package lk.globaltrade.monitor;

import jakarta.ejb.Local;

import java.util.Map;

@Local
public interface PerformanceMonitorBeanLocal {

    void recordCall(String methodName, long durationNanos);

    Map<String, PerformanceMonitorBean.MethodStats> getSnapshot();
}