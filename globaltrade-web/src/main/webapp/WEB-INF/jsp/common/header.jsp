<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>GlobalTrade Logistics</title>
    <style>
        * { box-sizing: border-box; }
        body {
            font-family: "Segoe UI", Arial, sans-serif;
            margin: 0;
            background: #f4f6f8;
            color: #1f2937;
        }
        header {
            background: #0b3d63;
            color: #fff;
            padding: 0.75rem 1.5rem;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }
        header .brand { font-weight: 600; font-size: 1.1rem; }
        header nav a {
            color: #dce8f2;
            text-decoration: none;
            margin-left: 1.25rem;
            font-size: 0.92rem;
        }
        header nav a:hover { color: #fff; text-decoration: underline; }
        main { max-width: 1080px; margin: 1.5rem auto; padding: 0 1.5rem 3rem; }
        h1 { font-size: 1.4rem; margin-top: 0; }
        table {
            width: 100%;
            border-collapse: collapse;
            background: #fff;
            box-shadow: 0 1px 3px rgba(0,0,0,0.08);
            margin-bottom: 1.5rem;
        }
        th, td {
            text-align: left;
            padding: 0.55rem 0.75rem;
            border-bottom: 1px solid #e5e7eb;
            font-size: 0.9rem;
        }
        th { background: #eef2f6; font-weight: 600; }
        .card {
            background: #fff;
            border-radius: 6px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.08);
            padding: 1.25rem;
            margin-bottom: 1.5rem;
        }
        .error {
            background: #fdecea;
            border: 1px solid #f5c2c0;
            color: #92251f;
            padding: 0.6rem 0.9rem;
            border-radius: 4px;
            margin-bottom: 1rem;
            font-size: 0.9rem;
        }
        .success {
            background: #eaf7ee;
            border: 1px solid #bfe6c9;
            color: #1e6b34;
            padding: 0.6rem 0.9rem;
            border-radius: 4px;
            margin-bottom: 1rem;
            font-size: 0.9rem;
        }
        .badge {
            display: inline-block;
            padding: 0.15rem 0.55rem;
            border-radius: 10px;
            font-size: 0.78rem;
            font-weight: 600;
            background: #e5e7eb;
            color: #374151;
        }
        form.inline { display: inline; }
        label { display: block; font-size: 0.85rem; margin: 0.5rem 0 0.2rem; font-weight: 600; }
        input, select, button {
            font-size: 0.9rem;
            padding: 0.4rem 0.5rem;
            border: 1px solid #cbd5e1;
            border-radius: 4px;
        }
        button {
            background: #0b3d63;
            color: #fff;
            border: none;
            cursor: pointer;
            padding: 0.45rem 0.9rem;
            margin-top: 0.75rem;
        }
        button:hover { background: #0f4d7a; }
        .muted { color: #6b7280; font-size: 0.85rem; }
    </style>
</head>
<body>
<header>
    <span class="brand">GlobalTrade Logistics</span>
    <nav>
        <c:if test="${pageContext.request.userPrincipal != null}">
            <span class="muted" style="color:#cbd8e4;">
                ${pageContext.request.userPrincipal.name}
            </span>
            <c:if test="${pageContext.request.isUserInRole('CUSTOMER')}">
                <a href="${pageContext.request.contextPath}/customer/dashboard">My Shipments</a>
            </c:if>
            <c:if test="${pageContext.request.isUserInRole('COORDINATOR')}">
                <a href="${pageContext.request.contextPath}/coordinator/dashboard">Active Shipments</a>
            </c:if>
            <c:if test="${pageContext.request.isUserInRole('ADMIN')}">
                <a href="${pageContext.request.contextPath}/admin/containers">Containers</a>
                <a href="${pageContext.request.contextPath}/admin/ships">Ships</a>
                <a href="${pageContext.request.contextPath}/admin/users">Users</a>
                <a href="${pageContext.request.contextPath}/admin/auditLog">Audit Log</a>
                <a href="${pageContext.request.contextPath}/admin/performance">Performance</a>
            </c:if>
            <a href="${pageContext.request.contextPath}/logout">Log out</a>
        </c:if>
    </nav>
</header>
<main>
