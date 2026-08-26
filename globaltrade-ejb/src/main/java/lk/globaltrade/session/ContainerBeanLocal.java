// Path: globaltrade-ejb/src/main/java/lk/globaltrade/session/ContainerBeanLocal.java
package lk.globaltrade.session;

import jakarta.ejb.Local;
import lk.globaltrade.entities.Container;

import java.util.List;

/**
 * Local business interface for {@link ContainerBean}.
 *
 * Signature is frozen by CONTRACTS.md §3 — do not add, remove, or
 * reorder parameters.
 */
@Local
public interface ContainerBeanLocal {

    List<Container> findAll();

    /**
     * Creates a new container in status AVAILABLE.
     *
     * @param containerNumber unique container number, e.g. "MSCU1000201"
     */
    Container create(String containerNumber);

    void updateStatus(int containerId, Container.Status newStatus);
}