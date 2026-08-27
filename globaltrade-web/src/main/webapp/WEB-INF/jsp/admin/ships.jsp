<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<h1>Ships</h1>

<c:if test="${not empty error}">
    <div class="error">${error}</div>
</c:if>

<div class="card">
    <table>
        <thead>
        <tr><th>ID</th><th>Name</th><th>Capacity</th><th>Status</th><th>Current port</th><th>Update status</th></tr>
        </thead>
        <tbody>
        <c:forEach var="sh" items="${ships}">
            <tr>
                <td>#${sh.id}</td>
                <td>${sh.name}</td>
                <td>${sh.capacity}</td>
                <td><span class="badge">${sh.status}</span></td>
                <td>${not empty sh.currentPort ? sh.currentPort.code : '— at sea —'}</td>
                <td>
                    <form class="inline" action="${pageContext.request.contextPath}/admin/ships" method="post">
                        <input type="hidden" name="action" value="updateStatus">
                        <input type="hidden" name="shipId" value="${sh.id}">
                        <select name="newStatus" style="padding:0.25rem;">
                            <c:forEach var="st" items="${statuses}">
                                <option value="${st}" ${st == sh.status ? 'selected' : ''}>${st}</option>
                            </c:forEach>
                        </select>
                        <button type="submit" style="padding:0.3rem 0.6rem; margin-top:0;">Apply</button>
                    </form>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    <p class="muted">Current port is written only by the scheduled timer as ships move between ports — it is not editable here by design.</p>
</div>

<div class="card" style="max-width: 420px;">
    <h2 style="margin-top:0; font-size:1.05rem;">Add ship</h2>
    <form action="${pageContext.request.contextPath}/admin/ships" method="post">
        <input type="hidden" name="action" value="create">
        <label for="name">Name</label>
        <input type="text" id="name" name="name" placeholder="MV Example" required>

        <label for="capacity">Capacity</label>
        <input type="number" id="capacity" name="capacity" min="1" required>

        <label for="initialPortId">Initial port (AT_PORT)</label>
        <select id="initialPortId" name="initialPortId" required>
            <c:forEach var="p" items="${ports}">
                <option value="${p.id}">${p.name} (${p.code})</option>
            </c:forEach>
        </select>

        <button type="submit">Create</button>
    </form>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
