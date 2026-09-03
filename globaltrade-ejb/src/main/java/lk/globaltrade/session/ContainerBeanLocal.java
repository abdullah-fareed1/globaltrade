package lk.globaltrade.session;

import jakarta.ejb.Local;
import lk.globaltrade.entities.Container;

import java.util.List;

@Local
public interface ContainerBeanLocal {

    List<Container> findAll();

    Container create(String containerNumber);

    void updateStatus(int containerId, Container.Status newStatus);
}