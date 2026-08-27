<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<h1>Performance</h1>

<div class="card">
    <p class="muted">
        One row per intercepted method name, populated by PerformanceInterceptor
        (bound only to ShipmentBookingBean / ShipmentOperationsBean per
        CONTRACTS.md §8). "Slow calls" counts invocations past the 500&nbsp;ms
        threshold. Recording itself is serialised behind a <code>@Lock(WRITE)</code>
        on PerformanceMonitorBean — a deliberate contention point discussed in the
        performance-analysis write-up, not something optimised away here.
    </p>
    <table>
        <thead>
        <tr><th>Method</th><th>Calls</th><th>Avg (ms)</th><th>Min (ns)</th><th>Max (ns)</th><th>Slow calls (&gt;500ms)</th></tr>
        </thead>
        <tbody>
        <c:forEach var="entry" items="${stats}">
            <tr>
                <td>${entry.key}</td>
                <td>${entry.value.callCount}</td>
                <td><fmt:formatNumber value="${entry.value.avgMillis}" maxFractionDigits="2"/></td>
                <td>${entry.value.minNanos}</td>
                <td>${entry.value.maxNanos}</td>
                <td>${entry.value.slowCount}</td>
            </tr>
        </c:forEach>
        <c:if test="${empty stats}">
            <tr><td colspan="6" class="muted">No calls recorded yet — book a shipment or update a status to generate data.</td></tr>
        </c:if>
        </tbody>
    </table>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
