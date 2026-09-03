package lk.globaltrade.web;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.globaltrade.entities.Shipment;
import lk.globaltrade.exception.InvalidShipmentStateException;
import lk.globaltrade.session.ShipmentOperationsBeanLocal;

import java.io.IOException;

@WebServlet("/coordinator/dashboard")
public class CoordinatorServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/jsp/coordinator/dashboard.jsp";

    @EJB
    private ShipmentOperationsBeanLocal shipmentOperationsBean;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        showDashboard(request, response, null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int shipmentId;
        Shipment.Status newStatus;
        try {
            shipmentId = Integer.parseInt(request.getParameter("shipmentId"));
            newStatus = Shipment.Status.valueOf(request.getParameter("newStatus"));
        } catch (IllegalArgumentException e) {
            showDashboard(request, response, "Invalid shipment or status selected.");
            return;
        }

        try {
            shipmentOperationsBean.updateStatus(shipmentId, newStatus);
        } catch (InvalidShipmentStateException e) {
            showDashboard(request, response, e.getMessage());
            return;
        }

        response.sendRedirect(request.getContextPath() + "/coordinator/dashboard");
    }

    private void showDashboard(HttpServletRequest request, HttpServletResponse response, String error)
            throws ServletException, IOException {

        request.setAttribute("shipments", shipmentOperationsBean.viewActiveShipments());
        request.setAttribute("statuses", Shipment.Status.values());
        if (error != null) {
            request.setAttribute("error", error);
        }
        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
