package lk.globaltrade.tests;

import jakarta.ejb.SessionContext;
import jakarta.persistence.EntityManager;
import lk.globaltrade.entities.Shipment;
import lk.globaltrade.entities.User;
import lk.globaltrade.exception.UnauthorizedShipmentAccessException;
import lk.globaltrade.session.ShipmentOperationsBean;
import lk.globaltrade.session.UserAccountBeanLocal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.security.Principal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityAccessTest {

    @Mock private EntityManager em;
    @Mock private SessionContext sessionContext;
    @Mock private UserAccountBeanLocal userAccountBean;

    private ShipmentOperationsBean bean;

    @BeforeEach
    void setUp() throws Exception {
        bean = new ShipmentOperationsBean();
        inject(bean, "em", em);
        inject(bean, "sessionContext", sessionContext);
        inject(bean, "userAccountBean", userAccountBean);
    }

    @Test
    void nonOwner_isDenied() {
        User owner = new User(); owner.setId(1);
        User caller = new User(); caller.setId(2);

        Shipment shipment = new Shipment();
        shipment.setId(5);
        shipment.setCustomer(owner);

        when(em.find(Shipment.class, 5)).thenReturn(shipment);
        when(sessionContext.getCallerPrincipal()).thenReturn(() -> "caller@x.com");
        when(userAccountBean.findByEmail("caller@x.com")).thenReturn(caller);

        assertThrows(UnauthorizedShipmentAccessException.class,
                () -> bean.getShipmentById(5));
    }

    @Test
    void owner_getsShipment() throws UnauthorizedShipmentAccessException {
        User owner = new User(); owner.setId(1);
        Shipment shipment = new Shipment();
        shipment.setId(5);
        shipment.setCustomer(owner);

        when(em.find(Shipment.class, 5)).thenReturn(shipment);
        when(sessionContext.getCallerPrincipal()).thenReturn(() -> "owner@x.com");
        when(userAccountBean.findByEmail("owner@x.com")).thenReturn(owner);

        assertEquals(shipment, bean.getShipmentById(5));
    }

    private static void inject(Object target, String field, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }
}