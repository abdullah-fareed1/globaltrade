package lk.globaltrade.tests;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lk.globaltrade.entities.*;
import lk.globaltrade.session.AuditLogWriterBeanLocal;
import lk.globaltrade.timer.ShipmentTimerBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentTimerBeanTest {

    @Mock private EntityManager em;
    @Mock private TypedQuery<Shipment> query;
    @Mock private AuditLogWriterBeanLocal auditLogWriter;

    private ShipmentTimerBean timerBean;

    @BeforeEach
    void setUp() throws Exception {
        timerBean = new ShipmentTimerBean();
        inject(timerBean, "em", em);
        inject(timerBean, "auditLogWriter", auditLogWriter);
    }

    @Test
    void pastEta_advancesToDelivered_releasesContainers_movesShip() {
        Port destination = new Port("NLRTM", "Rotterdam", "Netherlands");
        Ship ship = new Ship("MV Test", 1000, Ship.Status.IN_TRANSIT, null);
        Container c1 = new Container("MSCU1", Container.Status.IN_TRANSIT);

        Shipment due = new Shipment();
        due.setId(1);
        due.setStatus(Shipment.Status.IN_TRANSIT);
        due.setEta(LocalDate.now().minusDays(1));
        due.setDestinationPort(destination);
        due.setShip(ship);
        due.setContainers(new HashSet<>(List.of(c1)));

        when(em.createQuery(anyString(), eq(Shipment.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(due));

        timerBean.advanceShipments();

        assertEquals(Shipment.Status.DELIVERED, due.getStatus());
        assertEquals(Container.Status.AVAILABLE, c1.getStatus());
        assertEquals(Ship.Status.AT_PORT, ship.getStatus());
        assertSame(destination, ship.getCurrentPort());
        verify(auditLogWriter).writeLog(isNull(), eq("TIMER_STATUS_UPDATE"), eq("Shipment"), eq(1), anyString());
    }

    private static void inject(Object target, String field, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }
}