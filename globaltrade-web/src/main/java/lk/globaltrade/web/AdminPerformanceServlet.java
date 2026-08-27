// Path: globaltrade-web/src/main/java/lk/globaltrade/web/AdminPerformanceServlet.java
package lk.globaltrade.web;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.globaltrade.monitor.PerformanceMonitorBeanLocal;

import java.io.IOException;

/**
 * {@code /admin/performance} -- CONTRACTS.md Sec11.
 *
 * The rendered map's value type (PerformanceMonitorBean.MethodStats)
 * MUST stay public static per CONTRACTS.md Sec4 -- performance.jsp reads
 * it via JSP EL from the web module, across the EAR module boundary; a
 * non-public or non-static inner class means EL silently renders blank
 * cells with no deployment error.
 */
@WebServlet("/admin/performance")
public class AdminPerformanceServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/jsp/admin/performance.jsp";

    @EJB
    private PerformanceMonitorBeanLocal performanceMonitorBean;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("stats", performanceMonitorBean.getSnapshot());
        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
