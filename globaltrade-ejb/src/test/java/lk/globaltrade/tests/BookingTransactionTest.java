package lk.globaltrade.tests;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lk.globaltrade.entities.*;
import lk.globaltrade.exception.NoContainerAvailableException;
import lk.globaltrade.session.ShipmentBookingBean;
import lk.globaltrade.timer.ShipmentAlertTimerBean;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingTransactionTest {

    @Mock private EntityManager em;
    @Mock private TypedQuery<Container> query;
    @Mock private ShipmentAlertTimerBean alertTimer;

    private ShipmentBookingBean bean;

    @BeforeEach
    void setUp() throws Exception {
        bean = new ShipmentBookingBean();
        inject(bean, "em", em);
        inject(bean, "shipmentAlertTimer", alertTimer);
    }

    @Test
    void insufficientContainers_throwsAndPersistsNothing() {
        when(em.createQuery(anyString(), eq(Container.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        Container c1 = new Container("MSCU1", Container.Status.AVAILABLE);
        when(query.getResultList()).thenReturn(List.of(c1));

        assertThrows(NoContainerAvailableException.class,
                () -> bean.bookShipment(1, 1, 2, 3));
        assertEquals(Container.Status.AVAILABLE, c1.getStatus());
        verify(em, never()).persist(any());
        verifyNoInteractions(alertTimer);
    }

    @Test
    void happyPath_reservesContainersAndPersistsShipment() throws Exception {
        when(em.createQuery(anyString(), eq(Container.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        Container c1 = new Container("MSCU1", Container.Status.AVAILABLE);
        Container c2 = new Container("MSCU2", Container.Status.AVAILABLE);
        when(query.getResultList()).thenReturn(List.of(c1, c2));

        when(em.find(eq(User.class), anyInt())).thenReturn(new User());
        when(em.find(eq(Port.class), anyInt())).thenReturn(new Port("LKCMB", "Colombo", "Sri Lanka"));

       doAnswer(invocation -> {
            Shipment s = invocation.getArgument(0);
            s.setId(99);
            return null;
        }).when(em).persist(any(Shipment.class));

        Shipment result = bean.bookShipment(1, 1, 2, 2);

        assertEquals(Container.Status.RESERVED, c1.getStatus());
        assertEquals(Container.Status.RESERVED, c2.getStatus());
        assertEquals(Shipment.Status.PENDING, result.getStatus());
        assertEquals(2000.0, result.getEstimatedCost());
        assertNull(result.getEta());
        verify(em).persist(result);
        verify(alertTimer).scheduleReadinessCheck(eq(99), eq(30L));
    }

    private static void inject(Object target, String field, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }
}