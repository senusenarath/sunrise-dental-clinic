<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<%--
    500.jsp - Internal Server Error
    Location: /WEB-INF/views/error/500.jsp
--%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>500 - Server Error | Sunrise Dental</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/style.css">
    <style>
        body {
            background: linear-gradient(135deg, #1a0a0a, #2d1515, #3d1515);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            font-family: 'Segoe UI', sans-serif;
        }
        .error-card {
            background: rgba(255,255,255,0.05);
            border: 1px solid rgba(239,68,68,0.2);
            border-radius: 20px;
            padding: 60px 48px;
            text-align: center;
            max-width: 520px;
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
            margin-bottom: 24px;
            line-height: 1.6;
        }
        .error-detail {
            background: rgba(239,68,68,0.1);
            border: 1px solid rgba(239,68,68,0.2);
            border-radius: 8px;
            padding: 12px 16px;
            color: #fca5a5;
            font-size: 0.82rem;
            text-align: left;
            margin-bottom: 28px;
            font-family: monospace;
            word-break: break-all;
        }
        .error-btns {
            display: flex;
            gap: 12px;
            justify-content: center;
        }
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
            background: linear-gradient(135deg, #dc2626, #b91c1c);
            color: white;
        }
        .btn-back:hover { background: rgba(255,255,255,0.2); }
        .btn-home:hover { opacity: 0.9; }
    </style>
</head>
<body>
    <div class="error-card">
        <div class="error-icon">⚠️</div>
        <div class="error-code">500</div>
        <h1 class="error-title">Internal Server Error</h1>
        <p class="error-msg">
            Something went wrong on our end. The system encountered
            an unexpected error. Please try again or contact the
            system administrator.
        </p>

        <%-- Show error details only in development --%>
        <% if (exception != null) { %>
        <div class="error-detail">
            <strong>Error:</strong>
            <%= exception.getMessage() != null
                ? exception.getMessage()
                : "Unknown error" %>
        </div>
        <% } %>

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