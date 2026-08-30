<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<%--
    403.jsp - Access Denied Error
    Location: /WEB-INF/views/error/403.jsp
--%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>403 - Access Denied | Sunrise Dental</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/style.css">
    <style>
        body {
            background: linear-gradient(135deg, #0f2027, #203a43, #2c5364);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            font-family: 'Segoe UI', sans-serif;
        }
        .error-card {
            background: rgba(255,255,255,0.05);
            border: 1px solid rgba(255,255,255,0.1);
            border-radius: 20px;
            padding: 60px 48px;
            text-align: center;
            max-width: 500px;
        }
        .error-code {
            font-size: 6rem;
            font-weight: 900;
            color: #f87171;
            line-height: 1;
            margin-bottom: 16px;
        }
        .error-icon { font-size: 3rem; margin-bottom: 16px; }
        .error-title {
            color: #ffffff;
            font-size: 1.5rem;
            font-weight: 700;
            margin-bottom: 12px;
        }
        .error-msg {
            color: rgba(255,255,255,0.5);
            margin-bottom: 32px;
            line-height: 1.6;
        }
        .error-btns { display: flex; gap: 12px; justify-content: center; }
        .btn-back, .btn-home {
            padding: 12px 24px;
            border-radius: 10px;
            font-weight: 600;
            text-decoration: none;
            transition: all 0.3s ease;
        }
        .btn-back {
            background: rgba(255,255,255,0.1);
            color: #ffffff;
            border: 1px solid rgba(255,255,255,0.2);
        }
        .btn-home {
            background: linear-gradient(135deg, #0ea5e9, #0284c7);
            color: white;
        }
        .btn-back:hover { background: rgba(255,255,255,0.2); }
        .btn-home:hover { opacity: 0.9; }
    </style>
</head>
<body>
    <div class="error-card">
        <div class="error-icon">🔒</div>
        <div class="error-code">403</div>
        <h1 class="error-title">Access Denied</h1>
        <p class="error-msg">
            You do not have permission to view this page.
            If you believe this is a mistake, please contact a system
            administrator.
        </p>
        <div class="error-btns">
            <a href="javascript:history.back()" class="btn-back">
                ← Go Back
            </a>
            <a href="${pageContext.request.contextPath}/dashboard"
               class="btn-home">
                🏠 Dashboard
            </a>
        </div>
    </div>
</body>
</html>
