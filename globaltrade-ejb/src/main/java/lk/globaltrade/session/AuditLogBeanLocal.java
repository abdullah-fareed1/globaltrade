package lk.globaltrade.session;

import jakarta.ejb.Local;
import lk.globaltrade.entities.AuditLog;

import java.util.List;

@Local
public interface AuditLogBeanLocal {

    List<AuditLog> findAllNewestFirst();
}
