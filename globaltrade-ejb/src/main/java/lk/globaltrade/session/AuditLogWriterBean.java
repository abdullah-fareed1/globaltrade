package lk.globaltrade.session;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.globaltrade.entities.AuditLog;
import lk.globaltrade.entities.User;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class AuditLogWriterBean implements AuditLogWriterBeanLocal {

    @PersistenceContext(unitName = "globaltradePU")
    private EntityManager em;

    @Override
    public void writeLog(User user, String action, String entityType, Integer entityId, String details) {
        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        em.persist(log);
    }
}