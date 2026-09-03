package lk.globaltrade.web;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.globaltrade.monitor.PerformanceMonitorBeanLocal;

import java.io.IOException;

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
