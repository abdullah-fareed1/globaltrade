package lk.globaltrade.web;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.globaltrade.session.AuditLogBeanLocal;

import java.io.IOException;

@WebServlet("/admin/auditLog")
public class AdminAuditLogServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/jsp/admin/auditLog.jsp";

    @EJB
    private AuditLogBeanLocal auditLogBean;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("logs", auditLogBean.findAllNewestFirst());
        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
