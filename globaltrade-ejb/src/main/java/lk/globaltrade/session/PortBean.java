package lk.globaltrade.session;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.globaltrade.entities.Port;

import java.util.List;

@Stateless
public class PortBean implements PortBeanLocal {

    @PersistenceContext(unitName = "globaltradePU")
    private EntityManager em;

    @Override
    public List<Port> findAll() {
        return em.createQuery("SELECT p FROM Port p ORDER BY p.name", Port.class)
                .getResultList();
    }
}
