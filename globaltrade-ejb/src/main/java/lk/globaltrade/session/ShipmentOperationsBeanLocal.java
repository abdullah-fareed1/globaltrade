package lk.globaltrade.session;

import jakarta.ejb.Local;
import lk.globaltrade.entities.Shipment;
import lk.globaltrade.exception.InvalidShipmentStateException;
import lk.globaltrade.exception.UnauthorizedShipmentAccessException;

import java.util.List;

@Local
public interface ShipmentOperationsBeanLocal {

    List<Shipment> viewActiveShipments();

    void updateStatus(int shipmentId, Shipment.Status newStatus) throws InvalidShipmentStateException;

    List<Shipment> getOwnShipments();

    Shipment getShipmentById(int shipmentId) throws UnauthorizedShipmentAccessException;
}