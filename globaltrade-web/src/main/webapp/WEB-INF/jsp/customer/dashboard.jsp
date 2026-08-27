<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<h1>My Shipments</h1>

<c:if test="${not empty error}">
    <div class="error">${error}</div>
</c:if>

<div class="card">
    <table>
        <thead>
        <tr>
            <th>ID</th>
            <th>Route</th>
            <th>Status</th>
            <th>ETA</th>
            <th>Est. Cost</th>
            <th>Containers</th>
            <th>Created</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="s" items="${shipments}">
            <tr>
                <td>#${s.id}</td>
                <td>${s.originPort.code} &rarr; ${s.destinationPort.code}</td>
                <td><span class="badge">${s.status}</span></td>
                <td>${not empty s.eta ? s.eta : '—'}</td>
                <td><fmt:formatNumber value="${s.estimatedCost}" type="currency" currencySymbol="$"/></td>
                <td>${s.containers.size()}</td>
                <td>${s.createdAt}</td>
            </tr>
        </c:forEach>
        <c:if test="${empty shipments}">
            <tr><td colspan="7" class="muted">You have no shipments yet — book one below.</td></tr>
        </c:if>
        </tbody>
    </table>
</div>

<div class="card" style="max-width: 420px;">
    <h2 style="margin-top:0; font-size:1.05rem;">Book a shipment</h2>
    <form action="${pageContext.request.contextPath}/customer/dashboard" method="post">
        <label for="originPortId">Origin port</label>
        <select id="originPortId" name="originPortId" required>
            <c:forEach var="p" items="${ports}">
                <option value="${p.id}">${p.name} (${p.code})</option>
            </c:forEach>
        </select>

        <label for="destinationPortId">Destination port</label>
        <select id="destinationPortId" name="destinationPortId" required>
            <c:forEach var="p" items="${ports}">
                <option value="${p.id}">${p.name} (${p.code})</option>
            </c:forEach>
        </select>

        <label for="containerCount">Number of containers</label>
        <input type="number" id="containerCount" name="containerCount" min="1" max="20" value="1" required>

        <button type="submit">Book shipment</button>
    </form>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
