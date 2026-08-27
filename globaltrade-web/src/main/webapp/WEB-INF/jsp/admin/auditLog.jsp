<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<h1>Audit Log</h1>

<div class="card">
    <p class="muted">
        Rows written with no user shown are system/timer actions (ENTITIES.md §6).
        A <code>*_FAILED</code> action means AuditInterceptor caught an exception from
        the audited call — that row is written by AuditLogWriterBean under
        <code>REQUIRES_NEW</code>, so it commits even though the caller's own
        transaction rolled back. Book more containers than are AVAILABLE as a
        customer, then look for the matching <code>BOOK_SHIPMENT_FAILED</code>
        row here — that is the observable proof of the REQUIRES_NEW behaviour.
    </p>
    <table>
        <thead>
        <tr><th>ID</th><th>When</th><th>User</th><th>Action</th><th>Entity</th><th>Entity ID</th><th>Details</th></tr>
        </thead>
        <tbody>
        <c:forEach var="log" items="${logs}">
            <tr>
                <td>#${log.id}</td>
                <td>${log.createdAt}</td>
                <td>${not empty log.user ? log.user.email : 'SYSTEM'}</td>
                <td>
                    <span class="badge" style="${fn:contains(log.action, 'FAILED') ? 'background:#fdecea;color:#92251f;' : ''}">
                        ${log.action}
                    </span>
                </td>
                <td>${log.entityType}</td>
                <td>${not empty log.entityId ? log.entityId : '—'}</td>
                <td>${log.details}</td>
            </tr>
        </c:forEach>
        <c:if test="${empty logs}">
            <tr><td colspan="7" class="muted">No audit log entries yet.</td></tr>
        </c:if>
        </tbody>
    </table>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
