package lk.globaltrade.session;

import jakarta.ejb.Local;
import lk.globaltrade.entities.Shipment;
import lk.globaltrade.exception.NoContainerAvailableException;

@Local
public interface ShipmentBookingBeanLocal {

    Shipment bookShipment(int customerId, int originPortId, int destinationPortId, int containerCount)
            throws NoContainerAvailableException;
}