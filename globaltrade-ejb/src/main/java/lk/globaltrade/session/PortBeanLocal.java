// Path: globaltrade-ejb/src/main/java/lk/globaltrade/session/PortBeanLocal.java
package lk.globaltrade.session;

import jakarta.ejb.Local;
import lk.globaltrade.entities.Port;

import java.util.List;

/**
 * Local business interface for {@link PortBean}.
 *
 * GAP FILL — Phase 6. Ports are pure reference data (ENTITIES.md Sec1:
 * "seeded once, never created through the UI") and no phase of the
 * build plan ever needed to read them back out -- until now. The
 * customer booking form (CustomerServlet / dashboard.jsp) needs an
 * origin/destination port dropdown, and nothing in
 * ShipmentBookingBeanLocal's frozen signature (CONTRACTS.md Sec3)
 * exposes a way to list them.
 *
 * Read-only by design: there is deliberately no create()/update() here,
 * matching "seeded once, never created through the UI" -- adding write
 * methods would misrepresent that reference data is meant to be
 * curated, not managed by end users.
 */
@Local
public interface PortBeanLocal {

    List<Port> findAll();
}
