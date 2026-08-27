package lk.globaltrade.tests;

import jakarta.interceptor.InvocationContext;
import lk.globaltrade.interceptor.PerformanceInterceptor;
import lk.globaltrade.monitor.PerformanceMonitorBeanLocal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerformanceInterceptorTest {

    @Mock private PerformanceMonitorBeanLocal monitor;
    @Mock private InvocationContext ctx;

    private PerformanceInterceptor interceptor;

    @BeforeEach
    void setUp() throws Exception {
        interceptor = new PerformanceInterceptor();
        Field f = PerformanceInterceptor.class.getDeclaredField("performanceMonitor");
        f.setAccessible(true);
        f.set(interceptor, monitor);
    }

    @Test
    void recordsCallOnceWithPositiveDuration() throws Exception {
        Method m = String.class.getMethod("trim");
        when(ctx.getMethod()).thenReturn(m);
        when(ctx.proceed()).thenReturn("result");

        Object out = interceptor.measure(ctx);

        assertEquals("result", out);
        ArgumentCaptor<Long> durationCaptor = ArgumentCaptor.forClass(Long.class);
        verify(monitor, times(1)).recordCall(eq("trim"), durationCaptor.capture());
        assertTrue(durationCaptor.getValue() >= 0);
    }

    @Test
    void recordsCallEvenWhenProceedThrows() throws Exception {
        Method m = String.class.getMethod("trim");
        when(ctx.getMethod()).thenReturn(m);
        when(ctx.proceed()).thenThrow(new RuntimeException("boom"));

        assertThrows(RuntimeException.class, () -> interceptor.measure(ctx));

        verify(monitor, times(1)).recordCall(eq("trim"), anyLong());
    }
}