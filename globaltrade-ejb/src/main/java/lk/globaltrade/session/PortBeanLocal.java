package lk.globaltrade.session;

import jakarta.ejb.Local;
import lk.globaltrade.entities.Port;

import java.util.List;

@Local
public interface PortBeanLocal {

    List<Port> findAll();
}
