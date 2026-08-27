<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<h1>Active Shipments</h1>

<c:if test="${not empty error}">
    <div class="error">${error}</div>
</c:if>

<div class="card">
    <table>
        <thead>
        <tr>
            <th>ID</th>
            <th>Customer</th>
            <th>Route</th>
            <th>Status</th>
            <th>ETA</th>
            <th>Ship</th>
            <th>Update status</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="s" items="${shipments}">
            <tr>
                <td>#${s.id}</td>
                <td>${s.customer.name}</td>
                <td>${s.originPort.code} &rarr; ${s.destinationPort.code}</td>
                <td><span class="badge">${s.status}</span></td>
                <td>${not empty s.eta ? s.eta : '—'}</td>
                <td>${not empty s.ship ? s.ship.name : '—'}</td>
                <td>
                    <form class="inline" action="${pageContext.request.contextPath}/coordinator/dashboard" method="post">
                        <input type="hidden" name="shipmentId" value="${s.id}">
                        <select name="newStatus" style="padding:0.25rem;">
                            <c:forEach var="st" items="${statuses}">
                                <option value="${st}" ${st == s.status ? 'selected' : ''}>${st}</option>
                            </c:forEach>
                        </select>
                        <button type="submit" style="padding:0.3rem 0.6rem; margin-top:0;">Apply</button>
                    </form>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty shipments}">
            <tr><td colspan="7" class="muted">No active shipments.</td></tr>
        </c:if>
        </tbody>
    </table>
    <p class="muted">
        Legal transitions: PENDING&rarr;CONFIRMED/DELAYED · CONFIRMED&rarr;IN_TRANSIT/DELAYED ·
        IN_TRANSIT&rarr;DELIVERED/DELAYED · DELAYED&rarr;IN_TRANSIT/DELIVERED · DELIVERED is terminal.
        Moving to IN_TRANSIT requires a ship AT_PORT at the origin — some origin ports may have none available.
    </p>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
