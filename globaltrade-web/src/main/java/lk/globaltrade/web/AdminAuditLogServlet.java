// Path: globaltrade-web/src/main/java/lk/globaltrade/web/AdminAuditLogServlet.java
package lk.globaltrade.web;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.globaltrade.session.AuditLogBeanLocal;

import java.io.IOException;

/**
 * {@code /admin/auditLog} -- CONTRACTS.md Sec11. Read-only: this is the
 * page where the REQUIRES_NEW / "audit survives a rolled-back
 * transaction" demonstration is actually observed -- a failed booking
 * (NoContainerAvailableException) still shows a *_FAILED row here even
 * though the Shipment/Container changes it attempted were rolled back.
 */
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
