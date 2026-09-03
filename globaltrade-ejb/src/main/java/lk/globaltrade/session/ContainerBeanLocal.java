package lk.globaltrade.session;

import jakarta.ejb.Local;
import lk.globaltrade.entities.Container;
import lk.globaltrade.exception.DuplicateContainerException;

import java.util.List;

@Local
public interface ContainerBeanLocal {

    List<Container> findAll();

    Container create(String containerNumber) throws DuplicateContainerException;

    void updateStatus(int containerId, Container.Status newStatus);
}