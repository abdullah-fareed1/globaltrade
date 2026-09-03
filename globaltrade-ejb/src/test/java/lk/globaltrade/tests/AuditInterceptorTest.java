package lk.globaltrade.tests;

import jakarta.ejb.SessionContext;
import jakarta.interceptor.InvocationContext;
import lk.globaltrade.entities.Shipment;
import lk.globaltrade.exception.NoContainerAvailableException;
import lk.globaltrade.interceptor.AuditInterceptor;
import lk.globaltrade.session.AuditLogWriterBeanLocal;
import lk.globaltrade.session.UserAccountBeanLocal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditInterceptorTest {

    @Mock private SessionContext sessionContext;
    @Mock private UserAccountBeanLocal userAccountBean;
    @Mock private AuditLogWriterBeanLocal auditLogWriter;
    @Mock private InvocationContext ctx;

    private AuditInterceptor interceptor;

    @BeforeEach
    void setUp() throws Exception {
        interceptor = new AuditInterceptor();
        inject(interceptor, "sessionContext", sessionContext);
        inject(interceptor, "userAccountBean", userAccountBean);
        inject(interceptor, "auditLogWriter", auditLogWriter);
        when(sessionContext.getCallerPrincipal()).thenReturn(() -> "ANONYMOUS");
    }

    @Test
    void success_logsOnce_actionOK() throws Exception {
        Method m = SampleTarget.class.getMethod("bookShipment", int.class, int.class, int.class, int.class);
        when(ctx.getMethod()).thenReturn(m);
        Shipment result = new Shipment(); result.setId(42);
        when(ctx.proceed()).thenReturn(result);

        Object out = interceptor.audit(ctx);

        assertSame(result, out);
        verify(auditLogWriter, times(1))
                .writeLog(isNull(), eq("CREATE_SHIPMENT"), eq("Shipment"), eq(42), eq("OK"));
        verify(auditLogWriter, never()).writeLog(any(), contains("_FAILED"), any(), any(), any());
    }

    @Test
    void failure_logsFailedRow_thenRethrows() throws Exception {
        Method m = SampleTarget.class.getMethod("bookShipment", int.class, int.class, int.class, int.class);
        when(ctx.getMethod()).thenReturn(m);
        NoContainerAvailableException ex = new NoContainerAvailableException("only 1 available");
        when(ctx.proceed()).thenThrow(ex);

        Exception thrown = assertThrows(NoContainerAvailableException.class,
                () -> interceptor.audit(ctx));

        assertSame(ex, thrown);
        verify(auditLogWriter, times(1))
                .writeLog(isNull(), eq("CREATE_SHIPMENT_FAILED"), eq("Shipment"), isNull(), eq("only 1 available"));
    }

    interface SampleTarget {
        Shipment bookShipment(int a, int b, int c, int d);
    }

    private static void inject(Object target, String field, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }
}