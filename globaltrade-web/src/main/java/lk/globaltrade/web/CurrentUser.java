// Path: globaltrade-web/src/main/java/lk/globaltrade/web/CurrentUser.java
package lk.globaltrade.web;

import jakarta.servlet.http.HttpServletRequest;
import lk.globaltrade.entities.User;
import lk.globaltrade.session.UserAccountBeanLocal;

/**
 * Web-layer-only helper. Not part of any frozen interface in
 * CONTRACTS.md -- purely to avoid repeating the same
 * "principal name (email) -> User entity" lookup in every servlet that
 * needs the caller's database id (CustomerServlet.doPost needs
 * customerId for bookShipment(); a couple of admin pages want to show
 * "logged in as").
 *
 * Mirrors CONTRACTS.md Sec5's currentUser() rule exactly: the JAAS
 * principal name is the caller's email, never their id.
 */
final class CurrentUser {

    private CurrentUser() {
    }

    /**
     * @return the User entity for the authenticated caller, or
     *         {@code null} if unauthenticated or not found.
     */
    static User resolve(HttpServletRequest request, UserAccountBeanLocal userAccountBean) {
        var principal = request.getUserPrincipal();
        if (principal == null) {
            return null;
        }
        return userAccountBean.findByEmail(principal.getName());
    }
}
