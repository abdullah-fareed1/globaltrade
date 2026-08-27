// Path: globaltrade-web/src/main/java/lk/globaltrade/web/CustomerServlet.java
package lk.globaltrade.web;

import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.globaltrade.entities.Shipment;
import lk.globaltrade.entities.User;
import lk.globaltrade.exception.NoContainerAvailableException;
import lk.globaltrade.session.PortBeanLocal;
import lk.globaltrade.session.ShipmentBookingBeanLocal;
import lk.globaltrade.session.ShipmentOperationsBeanLocal;
import lk.globaltrade.session.UserAccountBeanLocal;

import java.io.IOException;
import java.util.List;

/**
 * {@code /customer/dashboard} -- CONTRACTS.md Sec11. Mapped to a single
 * path prefix, not {@code /*}, and every forward target lives under
 * {@code /WEB-INF/jsp/} so a forward can never re-enter this servlet's
 * own mapping (build plan FIX 9 / Step 6B).
 */
@WebServlet("/customer/dashboard")
public class CustomerServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/jsp/customer/dashboard.jsp";

    @EJB
    private ShipmentOperationsBeanLocal shipmentOperationsBean;

    @EJB
    private ShipmentBookingBeanLocal shipmentBookingBean;

    @EJB
    private UserAccountBeanLocal userAccountBean;

    @EJB
    private PortBeanLocal portBean;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        showDashboard(request, response, null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User caller = CurrentUser.resolve(request, userAccountBean);
        if (caller == null) {
            // security-constraint on /customer/* already guarantees an
            // authenticated CUSTOMER reached this point; this is a
            // defensive fallback only.
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        int originPortId;
        int destinationPortId;
        int containerCount;
        try {
            originPortId = Integer.parseInt(request.getParameter("originPortId"));
            destinationPortId = Integer.parseInt(request.getParameter("destinationPortId"));
            containerCount = Integer.parseInt(request.getParameter("containerCount"));
        } catch (NumberFormatException e) {
            showDashboard(request, response, "Please fill in the booking form correctly.");
            return;
        }

        if (containerCount < 1) {
            showDashboard(request, response, "Container count must be at least 1.");
            return;
        }
        if (originPortId == destinationPortId) {
            showDashboard(request, response, "Origin and destination ports must differ.");
            return;
        }

        try {
            shipmentBookingBean.bookShipment(caller.getId(), originPortId, destinationPortId, containerCount);
        } catch (NoContainerAvailableException e) {
            // Application exception -- arrives as itself (CONTRACTS.md
            // Sec10). The failed booking still produced a
            // BOOK_SHIPMENT_FAILED audit row via AuditInterceptor's
            // REQUIRES_NEW write, even though nothing here was
            // persisted -- that row is visible at /admin/auditLog.
            showDashboard(request, response, e.getMessage());
            return;
        } catch (EJBException e) {
            // SupplyChainSystemException is a SYSTEM exception (no
            // @ApplicationException) -> the container always wraps it
            // in EJBException; catching SupplyChainSystemException
            // directly here would never match (CONTRACTS.md Sec10).
            showDashboard(request, response, "System error, please try again.");
            return;
        }

        // PRG: redirect after a successful POST so a page refresh never
        // re-submits the booking.
        response.sendRedirect(request.getContextPath() + "/customer/dashboard");
    }

    private void showDashboard(HttpServletRequest request, HttpServletResponse response, String error)
            throws ServletException, IOException {

        List<Shipment> ownShipments = shipmentOperationsBean.getOwnShipments();
        request.setAttribute("shipments", ownShipments);
        request.setAttribute("ports", portBean.findAll());
        if (error != null) {
            request.setAttribute("error", error);
        }
        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
