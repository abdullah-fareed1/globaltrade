package lk.globaltrade.web.security;

import jakarta.annotation.security.DeclareRoles;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.authentication.mechanism.http.FormAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;
import jakarta.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;

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
}