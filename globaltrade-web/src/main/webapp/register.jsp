<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Register — GlobalTrade Logistics</title>
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
            width: 340px;
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
        .foot { margin-top: 1rem; font-size: 0.82rem; text-align: center; color: #6b7280; }
        .foot a { color: #0b3d63; }
        .note { font-size: 0.78rem; color: #6b7280; margin-top: 0.3rem; }
    </style>
</head>
<body>
<div class="box">
    <h1>Create an account</h1>
    <p class="tagline">Registers as a CUSTOMER</p>

    <c:if test="${not empty error}">
        <div class="error">${error}</div>
    </c:if>

    <form action="${pageContext.request.contextPath}/register" method="post">
        <label for="name">Full name</label>
        <input type="text" id="name" name="name" required autofocus>

        <label for="email">Email</label>
        <input type="email" id="email" name="email" required>

        <label for="password">Password</label>
        <input type="password" id="password" name="password" required minlength="6">
        <div class="note">Stored as a bcrypt hash — never in plain text.</div>

        <button type="submit">Register</button>
    </form>

    <div class="foot">
        Already have an account? <a href="${pageContext.request.contextPath}/login.jsp">Log in</a>
    </div>
</div>
</body>
</html>
