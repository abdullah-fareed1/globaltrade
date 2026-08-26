// Path: globaltrade-ejb/src/main/java/lk/globaltrade/session/UserAccountBeanLocal.java
package lk.globaltrade.session;

import jakarta.ejb.Local;
import lk.globaltrade.entities.User;

import java.util.List;

/**
 * Local business interface for {@link UserAccountBean}.
 *
 * Signature is frozen by CONTRACTS.md §3 — do not add, remove, or
 * reorder parameters.
 */
@Local
public interface UserAccountBeanLocal {

    /**
     * Registers a new CUSTOMER. Hashes {@code rawPassword} with jBCrypt
     * before persisting — never persists a plaintext or pre-hashed
     * password. Runs under bean-managed transaction demarcation.
     */
    User register(String name, String email, String rawPassword);

    /**
     * Returns the matching User if {@code rawPassword} checks out
     * against the stored bcrypt hash, otherwise {@code null}.
     */
    User authenticate(String email, String rawPassword);

    void updateRole(int userId, User.Role newRole);

    /**
     * Used by SecurityInterceptor / AuditInterceptor (Phase 3) and
     * ShipmentOperationsBean's caller-identity resolution (CONTRACTS.md
     * §5) to look up the currently authenticated user by JAAS principal
     * name (email). Returns {@code null} if not found.
     */
    User findByEmail(String email);

    /** Used by AdminUserServlet. */
    List<User> findAll();
}