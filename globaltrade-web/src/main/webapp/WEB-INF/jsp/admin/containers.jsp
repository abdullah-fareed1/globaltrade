<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<h1>Containers</h1>

<c:if test="${not empty error}">
    <div class="error">${error}</div>
</c:if>

<div class="card">
    <table>
        <thead>
        <tr><th>ID</th><th>Number</th><th>Status</th><th>Update status</th></tr>
        </thead>
        <tbody>
        <c:forEach var="ctn" items="${containers}">
            <tr>
                <td>#${ctn.id}</td>
                <td>${ctn.containerNumber}</td>
                <td><span class="badge">${ctn.status}</span></td>
                <td>
                    <form class="inline" action="${pageContext.request.contextPath}/admin/containers" method="post">
                        <input type="hidden" name="action" value="updateStatus">
                        <input type="hidden" name="containerId" value="${ctn.id}">
                        <select name="newStatus" style="padding:0.25rem;">
                            <c:forEach var="st" items="${statuses}">
                                <option value="${st}" ${st == ctn.status ? 'selected' : ''}>${st}</option>
                            </c:forEach>
                        </select>
                        <button type="submit" style="padding:0.3rem 0.6rem; margin-top:0;">Apply</button>
                    </form>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>

<div class="card" style="max-width: 380px;">
    <h2 style="margin-top:0; font-size:1.05rem;">Add container</h2>
    <form action="${pageContext.request.contextPath}/admin/containers" method="post">
        <input type="hidden" name="action" value="create">
        <label for="containerNumber">Container number</label>
        <input type="text" id="containerNumber" name="containerNumber" placeholder="MSCU1000201" required>
        <button type="submit">Create</button>
    </form>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
