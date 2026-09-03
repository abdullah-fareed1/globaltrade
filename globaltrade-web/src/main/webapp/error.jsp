<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Error — GlobalTrade Logistics</title>
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
            width: 360px;
            text-align: center;
        }
        h1 { font-size: 1.15rem; color: #92251f; margin: 0 0 0.5rem; }
        p { color: #4b5563; font-size: 0.9rem; }
        a {
            display: inline-block;
            margin-top: 1rem;
            color: #fff;
            background: #0b3d63;
            padding: 0.5rem 1.1rem;
            border-radius: 4px;
            text-decoration: none;
            font-size: 0.9rem;
        }
        a:hover { background: #0f4d7a; }
    </style>
</head>
<body>
<div class="box">
    <h1>Something went wrong</h1>
    <p>The system could not complete that request. The problem has been
        recorded. Please go back and try again.</p>
    <a href="<%= request.getContextPath() %>/home">Return to the dashboard</a>
</div>
</body>
</html>
