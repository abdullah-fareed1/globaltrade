// Path: globaltrade-ejb/src/main/java/lk/globaltrade/session/ContainerBean.java
package lk.globaltrade.session;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.globaltrade.entities.Container;

import java.util.List;

/**
 * Admin CRUD bean for containers. CMT with the container default
 * (REQUIRED) — no @TransactionAttribute needed.
 *
 * Deliberately un-intercepted: per CONTRACTS.md §8, the Security ->
 * Performance -> Audit interceptor chain is bound (in ejb-jar.xml, in
 * Phase 3) only to ShipmentBookingBean and ShipmentOperationsBean. This
 * class must NOT carry an @Interceptors annotation and must not appear
 * in the ejb-jar.xml <interceptor-order> list.
 */
@Stateless
public class ContainerBean implements ContainerBeanLocal {

    @PersistenceContext(unitName = "globaltradePU")
    private EntityManager em;

    @Override
    public List<Container> findAll() {
        return em.createQuery("SELECT c FROM Container c", Container.class)
                .getResultList();
    }

    @Override
    public Container create(String containerNumber) {
        Container container = new Container(containerNumber, Container.Status.AVAILABLE);
        em.persist(container);
        return container;
    }

    @Override
    public void updateStatus(int containerId, Container.Status newStatus) {
        Container container = em.find(Container.class, containerId);
        if (container != null) {
            container.setStatus(newStatus);
        }
    }
}