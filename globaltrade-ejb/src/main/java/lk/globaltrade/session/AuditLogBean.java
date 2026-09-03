package lk.globaltrade.session;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.globaltrade.entities.AuditLog;

import java.util.List;

@Stateless
public class AuditLogBean implements AuditLogBeanLocal {

    @PersistenceContext(unitName = "globaltradePU")
    private EntityManager em;

    @Override
    public List<AuditLog> findAllNewestFirst() {
        return em.createQuery(
                        "SELECT a FROM AuditLog a ORDER BY a.createdAt DESC, a.id DESC", AuditLog.class)
                .getResultList();
    }
}
