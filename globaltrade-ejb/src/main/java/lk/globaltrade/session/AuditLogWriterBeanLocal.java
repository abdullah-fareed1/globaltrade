package lk.globaltrade.session;

import jakarta.ejb.Local;
import lk.globaltrade.entities.User;

@Local
public interface AuditLogWriterBeanLocal {

    void writeLog(User user, String action, String entityType, Integer entityId, String details);
}