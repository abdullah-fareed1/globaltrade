package lk.globaltrade.web.security;

import jakarta.inject.Inject;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters.withParams;

@WebServlet("/authenticate")
public class AuthenticationServlet extends HttpServlet {

    private static final String LOGIN_VIEW = "/login.jsp";

    @Inject
    private SecurityContext securityContext;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.sendRedirect(request.getContextPath() + LOGIN_VIEW);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (isBlank(email) || isBlank(password)) {
            rejectBackToForm(request, response, "Enter both your email and your password.");
            return;
        }

        AuthenticationStatus status = securityContext.authenticate(
                request,
                response,
                withParams()
                        .credential(new UsernamePasswordCredential(email.trim(), password))
                        .newAuthentication(true)
        );

       if (status == AuthenticationStatus.SEND_CONTINUE || response.isCommitted()) {
            return;
        }

        if (status == AuthenticationStatus.SUCCESS) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }
        rejectBackToForm(request, response, "Incorrect email or password.");
    }

    private void rejectBackToForm(HttpServletRequest request, HttpServletResponse response,
                                  String message) throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        request.setAttribute("error", message);
        request.getRequestDispatcher(LOGIN_VIEW).forward(request, response);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
