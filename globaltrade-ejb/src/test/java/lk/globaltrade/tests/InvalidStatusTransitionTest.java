package lk.globaltrade.tests;

import jakarta.persistence.EntityManager;
import lk.globaltrade.entities.Shipment;
import lk.globaltrade.exception.InvalidShipmentStateException;
import lk.globaltrade.session.ShipmentOperationsBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvalidStatusTransitionTest {

    @Mock private EntityManager em;
    private ShipmentOperationsBean bean;

    @BeforeEach
    void setUp() throws Exception {
        bean = new ShipmentOperationsBean();
        Field f = ShipmentOperationsBean.class.getDeclaredField("em");
        f.setAccessible(true);
        f.set(bean, em);
    }

    @Test
    void delivered_toInTransit_isIllegal() {
        Shipment s = new Shipment(); s.setId(1); s.setStatus(Shipment.Status.DELIVERED);
        when(em.find(Shipment.class, 1)).thenReturn(s);

        assertThrows(InvalidShipmentStateException.class,
                () -> bean.updateStatus(1, Shipment.Status.IN_TRANSIT));
    }

    @Test
    void pending_toDelivered_isIllegal() {
        Shipment s = new Shipment(); s.setId(2); s.setStatus(Shipment.Status.PENDING);
        when(em.find(Shipment.class, 2)).thenReturn(s);

        assertThrows(InvalidShipmentStateException.class,
                () -> bean.updateStatus(2, Shipment.Status.DELIVERED));
    }

    @Test
    void pending_toConfirmed_isLegal() throws InvalidShipmentStateException {
        Shipment s = new Shipment(); s.setId(3); s.setStatus(Shipment.Status.PENDING);
        when(em.find(Shipment.class, 3)).thenReturn(s);

        bean.updateStatus(3, Shipment.Status.CONFIRMED);

        assertEquals(Shipment.Status.CONFIRMED, s.getStatus());
    }

}