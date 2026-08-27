// Path: globaltrade-web/src/main/java/lk/globaltrade/web/LoginServlet.java
package lk.globaltrade.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Mapped to the PROTECTED URL {@code /home} (CONTRACTS.md Sec11 / build
 * plan FIX 11) -- not {@code /login}.
 *
 * FORM authentication posts to {@code j_security_check} directly from
 * login.jsp; there is no application code in that path at all. The
 * container intercepts an unauthenticated request for any constrained
 * resource, shows login.jsp, and on success redirects the browser back
 * to the ORIGINALLY REQUESTED resource -- not to some fixed "welcome"
 * URL. login.jsp's link/action targets {@code /home}, so that is the
 * resource the container remembers and returns the user to.
 *
 * A servlet mapped to {@code /login} would only ever run if an
 * already-authenticated user navigated there by hand; the role-based
 * dashboard routing would never fire on the actual login path. Mapping
 * the router to a protected URL is what makes it run automatically
 * right after authentication.
 *
 * doGet() is genuinely the only method needed: /home carries no request
 * body semantics of its own, it just routes.
 */
@WebServlet("/home")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // request.isUserInRole(...) only returns true for a role that is
        // both declared as a <security-role> in web.xml AND held by the
        // authenticated caller -- so this is safe even though a user
        // only ever has exactly one role in this schema.
        if (request.isUserInRole("ADMIN")) {
            response.sendRedirect(request.getContextPath() + "/admin/performance");
        } else if (request.isUserInRole("COORDINATOR")) {
            response.sendRedirect(request.getContextPath() + "/coordinator/dashboard");
        } else if (request.isUserInRole("CUSTOMER")) {
            response.sendRedirect(request.getContextPath() + "/customer/dashboard");
        } else {
            // Authenticated (the security-constraint on /home already
            // guarantees that) but held no recognised role -- defensive
            // fallback only; should not be reachable with this app's
            // fixed CUSTOMER/COORDINATOR/ADMIN role set.
            response.sendRedirect(request.getContextPath() + "/login.jsp");
        }
    }
}
