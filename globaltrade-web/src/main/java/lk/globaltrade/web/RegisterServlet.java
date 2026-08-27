// Path: globaltrade-web/src/main/java/lk/globaltrade/web/RegisterServlet.java
package lk.globaltrade.web;

import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.globaltrade.session.UserAccountBeanLocal;

import java.io.IOException;

/**
 * {@code /register}, public (no security-constraint covers it). Drives
 * {@link UserAccountBeanLocal#register} -- the bean-managed-transaction
 * demonstration (CONTRACTS.md Sec2/Sec9).
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    @EJB
    private UserAccountBeanLocal userAccountBean;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (isBlank(name) || isBlank(email) || isBlank(password)) {
            request.setAttribute("error", "All fields are required.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        try {
            userAccountBean.register(name.trim(), email.trim(), password);
        } catch (EJBException e) {
            // SupplyChainSystemException is a SYSTEM exception (no
            // @ApplicationException) -> per CONTRACTS.md Sec10 the
            // container always wraps it in EJBException, including here
            // -- most commonly a duplicate email hitting the unique
            // constraint on users.email.
            request.setAttribute("error",
                    "Could not register that email. It may already be in use.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        // PRG: redirect to the login page rather than forwarding, so a
        // refresh of the resulting page never resubmits the form.
        response.sendRedirect(request.getContextPath() + "/login.jsp?registered=true");
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
