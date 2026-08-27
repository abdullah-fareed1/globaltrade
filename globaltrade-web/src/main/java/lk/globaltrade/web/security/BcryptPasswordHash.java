package lk.globaltrade.web.security;

import jakarta.security.enterprise.identitystore.PasswordHash;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Map;

/**
 * Standard Jakarta EE 10 Security SPI implementation — NOT a Payara-
 * internal class. This is the "proper" replacement for the
 * com.sun.appserv.security.AppservRealm / AppservPasswordLoginModule
 * approach that was previously attempted (Phase 5 in the original build
 * plan). Those two classes are Payara/GlassFish-internal legacy APIs
 * that were never published as a stable Maven artifact — that's why
 * they wouldn't resolve. jakarta.security.enterprise.identitystore
 * .PasswordHash IS a standard, spec-defined interface, part of Jakarta
 * Security 3.0 (Jakarta EE 10), and comes for free with the
 * jakarta.jakartaee-api dependency every module already has.
 *
 * Wired in via SecurityConfig's @DatabaseIdentityStoreDefinition
 * (hashAlgorithm = BcryptPasswordHash.class). The container instantiates
 * this class itself and calls verify()/generate() — no CDI annotation
 * needed on the class itself, per the Jakarta Security spec.
 *
 * ENTITIES.md / seed_data.sql FIX 3 still applies: stored hashes must be
 * "$2a$" (jBCrypt's native prefix). BCrypt.checkpw() throws
 * IllegalArgumentException rather than returning false on an
 * incompatible "$2b$" hash, so that's caught here and turned into a
 * clean "verification failed" rather than an uncaught exception
 * bubbling out of the container's authentication path.
 */
public class BcryptPasswordHash implements PasswordHash {

    private static final int BCRYPT_COST = 10;

    @Override
    public void initialize(Map<String, String> parameters) {
        // No configurable parameters needed — jBCrypt encodes its own
        // cost factor and salt inside the hash string itself
        // ($2a$<cost>$<22-char-salt><31-char-hash>), so verify() never
        // needs external parameters to check a hash it didn't generate
        // with a known cost. hashAlgorithmParameters is intentionally
        // left unset on the @DatabaseIdentityStoreDefinition annotation.
    }

    /**
     * Only used if this class is ever asked to generate a new hash
     * (e.g. if UserAccountBean.register() were rewritten to call this
     * instead of BCrypt directly — currently it isn't; it calls jBCrypt
     * inline, per CONTRACTS.md §9's BMT design). Implemented anyway to
     * satisfy the interface contract and keep the class usable as a
     * single source of bcrypt truth if that changes later.
     */
    @Override
    public String generate(char[] password) {
        return BCrypt.hashpw(new String(password), BCrypt.gensalt(BCRYPT_COST));
    }

    @Override
    public boolean verify(char[] password, String hashedPassword) {
        try {
            return BCrypt.checkpw(new String(password), hashedPassword);
        } catch (IllegalArgumentException e) {
            // Stored value isn't a valid bcrypt hash (e.g. wrong prefix,
            // corrupt data). Fail closed rather than letting the
            // exception escape into the container's auth machinery.
            return false;
        }
    }
}