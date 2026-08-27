<%@ page contentType="text/html;charset=UTF-8" %>
<%
    // Sends every visit to the context root through the protected
    // /home URL, so an unauthenticated visitor is naturally routed
    // into FORM auth (login.jsp) with /home saved as the resource to
    // return to on success -- exactly the flow CONTRACTS.md Sec11
    // describes, without the visitor needing to already know that URL.
    response.sendRedirect(request.getContextPath() + "/home");
%>
