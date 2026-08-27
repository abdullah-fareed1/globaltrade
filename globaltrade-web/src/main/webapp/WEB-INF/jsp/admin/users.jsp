<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<h1>Users</h1>

<c:if test="${not empty error}">
    <div class="error">${error}</div>
</c:if>

<div class="card">
    <table>
        <thead>
        <tr><th>ID</th><th>Name</th><th>Email</th><th>Role</th><th>Change role</th></tr>
        </thead>
        <tbody>
        <c:forEach var="u" items="${users}">
            <tr>
                <td>#${u.id}</td>
                <td>${u.name}</td>
                <td>${u.email}</td>
                <td><span class="badge">${u.role}</span></td>
                <td>
                    <form class="inline" action="${pageContext.request.contextPath}/admin/users" method="post">
                        <input type="hidden" name="userId" value="${u.id}">
                        <select name="newRole" style="padding:0.25rem;">
                            <c:forEach var="r" items="${roles}">
                                <option value="${r}" ${r == u.role ? 'selected' : ''}>${r}</option>
                            </c:forEach>
                        </select>
                        <button type="submit" style="padding:0.3rem 0.6rem; margin-top:0;">Apply</button>
                    </form>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    <p class="muted">New accounts are created only via the public /register flow (self-service, always CUSTOMER); this page only changes role after the fact.</p>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
