package lk.globaltrade.session;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Status;
import jakarta.transaction.UserTransaction;
import lk.globaltrade.entities.User;
import lk.globaltrade.exception.SupplyChainSystemException;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class UserAccountBean implements UserAccountBeanLocal {

    private static final int BCRYPT_COST = 10;

    @PersistenceContext(unitName = "globaltradePU")
    private EntityManager em;

    @Resource
    private UserTransaction ut;

    @Override
    public User register(String name, String email, String rawPassword) {
        try {
            ut.begin();
            String hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt(BCRYPT_COST));
            User user = new User(name, email, hash, User.Role.CUSTOMER);
            em.persist(user);
            ut.commit();
            return user;
        } catch (Exception e) {
            rollbackQuietly();
            throw new SupplyChainSystemException("Failed to register user", e);
        }
    }

    @Override
    public User authenticate(String email, String rawPassword) {
        User user = findByEmail(email);
        if (user == null) {
            return null;
        }
        return BCrypt.checkpw(rawPassword, user.getPassword()) ? user : null;
    }

    @Override
    public void updateRole(int userId, User.Role newRole) {
        try {
            ut.begin();
            User user = em.find(User.class, userId);
            if (user != null) {
                user.setRole(newRole);
            }
            ut.commit();
        } catch (Exception e) {
            rollbackQuietly();
            throw new SupplyChainSystemException("Failed to update user role", e);
        }
    }

    @Override
    public User findByEmail(String email) {
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<User> findAll() {
        return em.createQuery("SELECT u FROM User u", User.class).getResultList();
    }

    private void rollbackQuietly() {
        try {
            if (ut.getStatus() != Status.STATUS_NO_TRANSACTION) {
                ut.rollback();
            }
        } catch (Exception ignored) {
        }
    }
}