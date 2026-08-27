package lk.globaltrade.web.security;

import jakarta.annotation.security.DeclareRoles;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.authentication.mechanism.http.FormAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;
import jakarta.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;

/**
 * Replaces the entire Payara-specific "custom realm" approach (the
 * original build plan's Phase 5: GlobalTradeRealm / GlobalTradeLoginModule
 * / globaltrade-realm module / asadmin create-auth-realm / login.conf).
 *
 * Everything here is STANDARD Jakarta EE 10 Security (spec:
 * jakarta.security.enterprise), portable to any compliant server, not
 * Payara-specific. Two annotations do the entire job:
 *
 * 1. @DatabaseIdentityStoreDefinition — a container-provided
 *    IdentityStore backed directly by the users table. No custom
 *    JDBC/JNDI code needed (contrast with the old GlobalTradeLoginModule,
 *    which hand-rolled the same lookup).
 *    - callerQuery: fetches the stored hash for BcryptPasswordHash to
 *      check against.
 *    - groupsQuery: fetches the caller's role. Jakarta Security maps
 *      IdentityStore groups directly onto declared security-role names
 *      by default — this is also why the old plan's
 *      glassfish-application.xml <security-role-mapping> requirement
 *      (Phase 7, FIX 12) goes away entirely; there's no separate
 *      role-mapping descriptor to keep in sync anymore.
 *    - hashAlgorithm: BcryptPasswordHash, so verification goes through
 *      jBCrypt exactly as CONTRACTS.md §5 / ENTITIES.md §3 specify,
 *      instead of the built-in Pbkdf2PasswordHash default (which is
 *      NOT compatible with the "$2a$..." hashes already in seed_data.sql
 *      and produced by UserAccountBean.register()).
 *
 * 2. @FormAuthenticationMechanismDefinition — replaces web.xml's
 *    <login-config><auth-method>FORM</auth-method>...</login-config>
 *    block (CONTRACTS.md §11) with the equivalent annotation-driven
 *    config. login.jsp / error.jsp stay exactly where CONTRACTS.md §11
 *    already puts them (public, outside WEB-INF) — nothing about the
 *    JSP layout or the FORM-post-to-j_security_check contract changes,
 *    only how the mechanism is *registered*.
 *
 * dataSourceLookup uses the bare "jdbc/GlobalTradeDS" JNDI name —
 * CONTRACTS.md §0's frozen name, looked up the same way
 * persistence.xml's <jta-data-source> does, NOT the
 * "java:comp/env/jdbc/..." form some Jakarta Security tutorials show
 * (that form is for a component-scoped <resource-ref> indirection this
 * project doesn't use).
 *
 * @DeclareRoles documents the three roles for tooling that inspects
 * annotations directly; web.xml's <security-role> entries (Phase 6)
 * remain the authoritative declaration enforced by the container.
 */
@ApplicationScoped
@DatabaseIdentityStoreDefinition(
        dataSourceLookup = "jdbc/GlobalTradeDS",
        callerQuery = "SELECT password FROM users WHERE email = ?",
        groupsQuery = "SELECT role FROM users WHERE email = ?",
        hashAlgorithm = BcryptPasswordHash.class
)
@FormAuthenticationMechanismDefinition(
        loginToContinue = @LoginToContinue(
                loginPage = "/login.jsp",
                errorPage = "/error.jsp"
        )
)
@DeclareRoles({"CUSTOMER", "COORDINATOR", "ADMIN"})
public class SecurityConfig {
    // Marker/config class — no members. The container reads the
    // annotations at deployment time and registers the corresponding
    // CDI beans (an IdentityStore and an HttpAuthenticationMechanism)
    // automatically. Nothing here is ever called directly.
}