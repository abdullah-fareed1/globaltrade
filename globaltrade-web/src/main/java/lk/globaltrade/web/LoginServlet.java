package lk.globaltrade.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/home")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (request.isUserInRole("ADMIN")) {
            response.sendRedirect(request.getContextPath() + "/admin/performance");
        } else if (request.isUserInRole("COORDINATOR")) {
            response.sendRedirect(request.getContextPath() + "/coordinator/dashboard");
        } else if (request.isUserInRole("CUSTOMER")) {
            response.sendRedirect(request.getContextPath() + "/customer/dashboard");
        } else {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
        }
    }
}
