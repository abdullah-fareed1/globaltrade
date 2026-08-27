// Path: globaltrade-web/src/main/java/lk/globaltrade/web/AdminUserServlet.java
package lk.globaltrade.web;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.globaltrade.entities.User;
import lk.globaltrade.session.UserAccountBeanLocal;

import java.io.IOException;

/**
 * {@code /admin/users} -- CONTRACTS.md Sec11. Role management only;
 * registration of new accounts is the public {@code /register} flow
 * (RegisterServlet), not something an admin does on a user's behalf
 * here.
 */
@WebServlet("/admin/users")
public class AdminUserServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/jsp/admin/users.jsp";

    @EJB
    private UserAccountBeanLocal userAccountBean;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        showList(request, response, null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            int userId = Integer.parseInt(request.getParameter("userId"));
            User.Role newRole = User.Role.valueOf(request.getParameter("newRole"));
            userAccountBean.updateRole(userId, newRole);
        } catch (IllegalArgumentException e) {
            showList(request, response, "Invalid user or role.");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/admin/users");
    }

    private void showList(HttpServletRequest request, HttpServletResponse response, String error)
            throws ServletException, IOException {
        request.setAttribute("users", userAccountBean.findAll());
        request.setAttribute("roles", User.Role.values());
        if (error != null) {
            request.setAttribute("error", error);
        }
        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
