package lk.globaltrade.web;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.globaltrade.entities.Ship;
import lk.globaltrade.session.PortBeanLocal;
import lk.globaltrade.session.ShipBeanLocal;

import java.io.IOException;

@WebServlet("/admin/ships")
public class AdminShipServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/jsp/admin/ships.jsp";

    @EJB
    private ShipBeanLocal shipBean;

    @EJB
    private PortBeanLocal portBean;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        showList(request, response, null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if ("create".equals(action)) {
            try {
                String name = request.getParameter("name");
                int capacity = Integer.parseInt(request.getParameter("capacity"));
                int initialPortId = Integer.parseInt(request.getParameter("initialPortId"));
                if (name == null || name.trim().isEmpty()) {
                    showList(request, response, "Ship name is required.");
                    return;
                }
                shipBean.create(name.trim(), capacity, initialPortId);
            } catch (NumberFormatException e) {
                showList(request, response, "Capacity and initial port are required.");
                return;
            }
        } else if ("updateStatus".equals(action)) {
            try {
                int shipId = Integer.parseInt(request.getParameter("shipId"));
                Ship.Status newStatus = Ship.Status.valueOf(request.getParameter("newStatus"));
                shipBean.updateStatus(shipId, newStatus);
            } catch (IllegalArgumentException e) {
                showList(request, response, "Invalid ship or status.");
                return;
            }
        }

        response.sendRedirect(request.getContextPath() + "/admin/ships");
    }

    private void showList(HttpServletRequest request, HttpServletResponse response, String error)
            throws ServletException, IOException {
        request.setAttribute("ships", shipBean.findAll());
        request.setAttribute("ports", portBean.findAll());
        request.setAttribute("statuses", Ship.Status.values());
        if (error != null) {
            request.setAttribute("error", error);
        }
        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
