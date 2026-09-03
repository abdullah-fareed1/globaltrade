package lk.globaltrade.session;

import jakarta.ejb.Local;
import lk.globaltrade.entities.Ship;

import java.util.List;

@Local
public interface ShipBeanLocal {

    List<Ship> findAll();

    Ship create(String name, int capacity, int initialPortId);

    void updateStatus(int shipId, Ship.Status newStatus);
}