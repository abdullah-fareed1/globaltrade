package lk.globaltrade.session;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.globaltrade.entities.Port;
import lk.globaltrade.entities.Ship;

import java.util.List;

@Stateless
public class ShipBean implements ShipBeanLocal {

    @PersistenceContext(unitName = "globaltradePU")
    private EntityManager em;

    @Override
    public List<Ship> findAll() {
        return em.createQuery("SELECT s FROM Ship s", Ship.class)
                .getResultList();
    }

    @Override
    public Ship create(String name, int capacity, int initialPortId) {
        Port initialPort = em.find(Port.class, initialPortId);
        Ship ship = new Ship(name, capacity, Ship.Status.AT_PORT, initialPort);
        em.persist(ship);
        return ship;
    }

    @Override
    public void updateStatus(int shipId, Ship.Status newStatus) {
        Ship ship = em.find(Ship.class, shipId);
        if (ship != null) {
            ship.setStatus(newStatus);
        }
    }
}