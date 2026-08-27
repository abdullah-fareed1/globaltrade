// Path: globaltrade-web/src/main/java/lk/globaltrade/web/LogoutServlet.java
package lk.globaltrade.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * {@code /logout}. Not constrained by any security-constraint (nothing
 * needs authentication to reach it), but a logged-out visitor gains
 * nothing by calling it either.
 *
 * request.logout() clears the Jakarta Security caller identity;
 * session.invalidate() additionally drops any application session
 * state. Both, in that order, per CONTRACTS.md Sec11.
 */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.logout();

        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
