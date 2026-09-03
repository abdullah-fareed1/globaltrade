package lk.globaltrade.web;

import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.globaltrade.entities.Container;
import lk.globaltrade.exception.DuplicateContainerException;
import lk.globaltrade.session.ContainerBeanLocal;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


@WebServlet("/admin/containers")
public class AdminContainerServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(AdminContainerServlet.class.getName());

    private static final String VIEW = "/WEB-INF/jsp/admin/containers.jsp";

    @EJB
    private ContainerBeanLocal containerBean;

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
            String containerNumber = request.getParameter("containerNumber");
            if (containerNumber == null || containerNumber.trim().isEmpty()) {
                showList(request, response, "Container number is required.");
                return;
            }

            try {
                containerBean.create(containerNumber.trim());

            } catch (DuplicateContainerException e) {
                showList(request, response, e.getMessage());
                return;

            } catch (EJBException e) {
                LOG.log(Level.WARNING, "Container create failed", e);
                showList(request, response,
                        "Could not create that container. The number may already be in use.");
                return;
            }

        } else if ("updateStatus".equals(action)) {
            try {
                int containerId = Integer.parseInt(request.getParameter("containerId"));
                Container.Status newStatus = Container.Status.valueOf(request.getParameter("newStatus"));
                containerBean.updateStatus(containerId, newStatus);
            } catch (IllegalArgumentException e) {
                showList(request, response, "Invalid container or status.");
                return;
            }
        }

        response.sendRedirect(request.getContextPath() + "/admin/containers");
    }

    private void showList(HttpServletRequest request, HttpServletResponse response, String error)
            throws ServletException, IOException {
        request.setAttribute("containers", containerBean.findAll());
        request.setAttribute("statuses", Container.Status.values());
        if (error != null) {
            request.setAttribute("error", error);
        }
        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}