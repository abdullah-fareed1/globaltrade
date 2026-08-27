<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!--
    Public page, must stay OUTSIDE WEB-INF (CONTRACTS.md Sec11): the
    container serves this directly when FORM authentication kicks in
    for an unauthenticated request to a constrained resource. It is
    also linked to directly as the application's entry point.

    The form below posts to j_security_check with j_username /
    j_password, per the Jakarta Security FORM mechanism registered by
    @FormAuthenticationMechanismDefinition on SecurityConfig (see
    DEVIATION_LOG_SECURITY.md) -- loginPage="/login.jsp" there must
    match this file's location exactly.

    Linking to /home (a PROTECTED url) is deliberate (build plan
    FIX 11): hitting /home unauthenticated is what triggers FORM auth in
    the first place, and on success the container returns the browser
    to /home, which is where the role-based dashboard routing lives
    (LoginServlet).
-->
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Log in — GlobalTrade Logistics</title>
    <style>
        body {
            font-family: "Segoe UI", Arial, sans-serif;
            background: #f4f6f8;
            margin: 0;
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
        }
        .box {
            background: #fff;
            padding: 2rem 2.25rem;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            width: 320px;
        }
        h1 { font-size: 1.25rem; margin: 0 0 0.25rem; color: #0b3d63; }
        p.tagline { margin: 0 0 1.25rem; color: #6b7280; font-size: 0.85rem; }
        label { display: block; font-size: 0.85rem; font-weight: 600; margin: 0.75rem 0 0.25rem; }
        input {
            width: 100%;
            padding: 0.5rem 0.6rem;
            border: 1px solid #cbd5e1;
            border-radius: 4px;
            font-size: 0.9rem;
        }
        button {
            width: 100%;
            margin-top: 1.25rem;
            padding: 0.55rem;
            background: #0b3d63;
            color: #fff;
            border: none;
            border-radius: 4px;
            font-size: 0.95rem;
            cursor: pointer;
        }
        button:hover { background: #0f4d7a; }
        .error {
            background: #fdecea;
            border: 1px solid #f5c2c0;
            color: #92251f;
            padding: 0.5rem 0.7rem;
            border-radius: 4px;
            font-size: 0.85rem;
            margin-bottom: 0.75rem;
        }
        .success {
            background: #eaf7ee;
            border: 1px solid #bfe6c9;
            color: #1e6b34;
            padding: 0.5rem 0.7rem;
            border-radius: 4px;
            font-size: 0.85rem;
            margin-bottom: 0.75rem;
        }
        .foot { margin-top: 1rem; font-size: 0.82rem; text-align: center; color: #6b7280; }
        .foot a { color: #0b3d63; }
    </style>
</head>
<body>
<div class="box">
    <h1>GlobalTrade Logistics</h1>
    <p class="tagline">Sign in to continue</p>

    <c:if test="${param.registered == 'true'}">
        <div class="success">Account created — you can log in now.</div>
    </c:if>

    <form action="${pageContext.request.contextPath}/j_security_check" method="post">
        <label for="j_username">Email</label>
        <input type="email" id="j_username" name="j_username" required autofocus>

        <label for="j_password">Password</label>
        <input type="password" id="j_password" name="j_password" required>

        <button type="submit">Log in</button>
    </form>

    <div class="foot">
        No account? <a href="${pageContext.request.contextPath}/register">Register</a>
    </div>
</div>
</body>
</html>
