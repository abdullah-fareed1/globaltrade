package lk.globaltrade.session;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.globaltrade.entities.Container;

import java.util.List;


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