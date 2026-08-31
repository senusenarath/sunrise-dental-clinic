<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
    login.jsp - Split layout login page
    Left: Dental image | Right: Login form
    Location: /WEB-INF/views/auth/login.jsp
--%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login | Sunrise Dental Clinic</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <style>
        html, body {
            margin: 0;
            padding: 0;
            height: 100vh;
            overflow: hidden;
            font-family: 'Segoe UI', sans-serif;
            background: #0d1117;
        }

        /* ── Split Layout ──────────────────────────────────── */
        .login-split {
            display: grid;
            grid-template-columns: 1fr 1fr;
            height: 100vh;
            overflow: hidden;
        }

        @media (max-width: 900px) {
            .login-split {
                grid-template-columns: 1fr;
            }
            .login-image-side { display: none; }
        }

        /* ── Left Side - Image ─────────────────────────────── */
        .login-image-side {
            position: relative;
            background:
                linear-gradient(
                    135deg,
                    rgba(14,165,233,0.8),
                    rgba(20,184,166,0.6)
                ),
                url('https://images.unsplash.com/photo-1629909613654-28e377c37b09?w=800')
                center / cover no-repeat;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            padding: 32px 40px;
            height: 100vh;
            overflow-y: auto;
            box-sizing: border-box;
        }

        .image-content {
            text-align: center;
            color: white;
            max-width: 420px;
        }

        .image-logo {
            font-size: 3.5rem;
            margin-bottom: 12px;
            filter: drop-shadow(0 0 30px rgba(255,255,255,0.3));
        }

        .image-title {
            font-size: 1.9rem;
            font-weight: 800;
            margin: 0 0 10px;
            text-shadow: 0 2px 10px rgba(0,0,0,0.3);
        }

        .image-subtitle {
            font-size: 1rem;
            opacity: 0.9;
            margin: 0 0 20px;
            line-height: 1.5;
        }

        .image-features {
            display: flex;
            flex-direction: column;
            gap: 8px;
            text-align: left;
        }

        .image-feature {
            display: flex;
            align-items: center;
            gap: 12px;
            background: rgba(255,255,255,0.15);
            backdrop-filter: blur(10px);
            padding: 10px 16px;
            border-radius: 10px;
            font-size: 0.85rem;
        }

        .image-feature-icon {
            font-size: 1.3rem;
            flex-shrink: 0;
        }

        /* ── Right Side - Form ─────────────────────────────── */
        .login-form-side {
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 24px 40px;
            background: #0d1117;
            height: 100vh;
            overflow-y: auto;
            box-sizing: border-box;
        }

        .login-form-wrapper {
            width: 100%;
            max-width: 400px;
        }

        .login-header {
            margin-bottom: 20px;
        }

        .login-header-icon {
            font-size: 2.5rem;
            display: block;
            margin-bottom: 16px;
        }

        .login-header h2 {
            color: #ffffff;
            font-size: 1.6rem;
            font-weight: 700;
            margin: 0 0 8px;
        }

        .login-header p {
            color: rgba(255,255,255,0.4);
            font-size: 0.88rem;
            margin: 0;
        }

        /* Form Styles */
        .login-form .form-group {
            margin-bottom: 14px;
        }

        .login-form label {
            display: block;
            color: rgba(255,255,255,0.6);
            font-size: 0.8rem;
            font-weight: 600;
            margin-bottom: 8px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .login-form input[type="text"],
        .login-form input[type="password"] {
            width: 100%;
            padding: 14px 16px;
            background: rgba(255,255,255,0.06);
            border: 1px solid rgba(255,255,255,0.12);
            border-radius: 10px;
            color: #ffffff;
            font-size: 0.95rem;
            transition: all 0.3s ease;
            box-sizing: border-box;
        }

        .login-form input:focus {
            outline: none;
            border-color: #0ea5e9;
            background: rgba(14,165,233,0.08);
            box-shadow: 0 0 0 3px rgba(14,165,233,0.15);
        }

        .login-form input::placeholder {
            color: rgba(255,255,255,0.25);
        }

        /* Remember Me */
        .remember-row {
            display: flex;
            align-items: center;
            gap: 10px;
            margin-bottom: 14px;
        }

        .remember-row input[type="checkbox"] {
            width: 16px;
            height: 16px;
            cursor: pointer;
            accent-color: #0ea5e9;
        }

        .remember-row label {
            color: rgba(255,255,255,0.5);
            font-size: 0.85rem;
            cursor: pointer;
            text-transform: none;
            letter-spacing: 0;
            margin: 0;
        }

        /* Login Button */
        .btn-login {
            width: 100%;
            padding: 15px;
            background: linear-gradient(135deg, #0ea5e9, #0284c7);
            color: white;
            border: none;
            border-radius: 10px;
            font-size: 1rem;
            font-weight: 700;
            cursor: pointer;
            transition: all 0.3s ease;
            letter-spacing: 0.5px;
        }

        .btn-login:hover {
            background: linear-gradient(135deg, #0284c7, #0369a1);
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(14,165,233,0.35);
        }

        /* Alerts */
        .login-alert {
            padding: 12px 16px;
            border-radius: 10px;
            margin-bottom: 20px;
            font-size: 0.85rem;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .login-alert.error {
            background: rgba(239,68,68,0.12);
            border: 1px solid rgba(239,68,68,0.25);
            color: #fca5a5;
        }

        .login-alert.success {
            background: rgba(34,197,94,0.12);
            border: 1px solid rgba(34,197,94,0.25);
            color: #86efac;
        }

        /* Demo Buttons */
        .demo-section {
            margin-top: 16px;
            padding-top: 16px;
            border-top: 1px solid rgba(255,255,255,0.08);
        }

        .demo-label {
            color: rgba(255,255,255,0.35);
            font-size: 0.72rem;
            text-transform: uppercase;
            letter-spacing: 1px;
            text-align: center;
            margin-bottom: 12px;
        }

        .demo-btns {
            display: flex;
            gap: 8px;
        }

        .demo-btn {
            flex: 1;
            padding: 12px;
            border: 1px solid rgba(255,255,255,0.1);
            border-radius: 10px;
            background: rgba(255,255,255,0.04);
            color: rgba(255,255,255,0.7);
            font-size: 0.85rem;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.2s ease;
            text-align: center;
        }

        .demo-btn:hover {
            background: rgba(255,255,255,0.1);
            border-color: rgba(255,255,255,0.25);
            transform: translateY(-2px);
        }

        .demo-btn .demo-icon {
            display: block;
            font-size: 1.5rem;
            margin-bottom: 6px;
        }

        .demo-btn .demo-name {
            display: block;
            font-size: 0.8rem;
        }

        /* Footer */
        .login-footer {
            text-align: center;
            margin-top: 14px;
            color: rgba(255,255,255,0.2);
            font-size: 0.75rem;
        }
    </style>
</head>
<body>

<div class="login-split">

    <!-- ── LEFT SIDE: Image + Clinic Info ──────────────────── -->
    <div class="login-image-side">
        <div class="image-content">
            <div class="image-logo">🦷</div>
            <h1 class="image-title">Sunrise Dental Clinic</h1>
            <p class="image-subtitle">
                Advanced Patient & Appointment Management System
                for Colombo's premier dental care facility
            </p>

            <div class="image-features">
                <div class="image-feature">
                    <span class="image-feature-icon">📅</span>
                    <span>Smart appointment scheduling with double-booking prevention</span>
                </div>
                <div class="image-feature">
                    <span class="image-feature-icon">👥</span>
                    <span>Complete digital patient medical records</span>
                </div>
                <div class="image-feature">
                    <span class="image-feature-icon">💰</span>
                    <span>Automated billing with 4 payment channels</span>
                </div>
                <div class="image-feature">
                    <span class="image-feature-icon">📧</span>
                    <span>Email notifications for every appointment</span>
                </div>
            </div>
        </div>
    </div>

    <!-- ── RIGHT SIDE: Login Form ─────────────────────────── -->
    <div class="login-form-side">
        <div class="login-form-wrapper">

            <!-- Header -->
            <div class="login-header">
                <span class="login-header-icon">🔐</span>
                <h2>Welcome Back</h2>
                <p>Sign in to access the management system</p>
            </div>

            <!-- Error Alert -->
            <c:if test="${not empty errorMsg}">
                <div class="login-alert error">
                    <span>⚠️</span>
                    <span><c:out value="${errorMsg}"/></span>
                </div>
            </c:if>

            <!-- Success Alert -->
            <c:if test="${not empty successMsg}">
                <div class="login-alert success">
                    <span>✅</span>
                    <span><c:out value="${successMsg}"/></span>
                </div>
            </c:if>

            <!-- Login Form -->
            <form action="${pageContext.request.contextPath}/login"
                  method="post"
                  class="login-form"
                  id="loginForm">

                <div class="form-group">
                    <label for="username">👤 Username</label>
                    <input type="text"
                           id="username"
                           name="username"
                           placeholder="Enter your username"
                           value="${not empty rememberedUsername ? rememberedUsername :
                                   not empty username ? username : ''}"
                           required
                           autocomplete="username">
                </div>

                <div class="form-group">
                    <label for="password">🔒 Password</label>
                    <input type="password"
                           id="password"
                           name="password"
                           placeholder="Enter your password"
                           required
                           autocomplete="current-password">
                </div>

                <div class="remember-row">
                    <input type="checkbox"
                           id="rememberMe"
                           name="rememberMe"
                           ${not empty rememberChecked ? 'checked' : ''}>
                    <label for="rememberMe">🍪 Remember me for 7 days</label>
                </div>

                <button type="submit" class="btn-login">
                    🚀 Sign In
                </button>
            </form>

            <!-- Demo Login Buttons -->
            <div class="demo-section">
                <div class="demo-label">⚡ Quick Demo Login</div>
                <div class="demo-btns">
                    <button class="demo-btn"
                            onclick="fillLogin('admin','admin123')">
                        <span class="demo-icon">👑</span>
                        <span class="demo-name">Admin</span>
                    </button>
                    <button class="demo-btn"
                            onclick="fillLogin('receptionist','reception123')">
                        <span class="demo-icon">🖥️</span>
                        <span class="demo-name">Receptionist</span>
                    </button>
                    <button class="demo-btn"
                            onclick="fillLogin('dentist','dentist123')">
                        <span class="demo-icon">🩺</span>
                        <span class="demo-name">Dentist</span>
                    </button>
                </div>
            </div>

            <!-- Footer -->
            <div class="login-footer">
                <p>🔐 Secure access &bull; Session expires after 30 minutes</p>
            </div>

        </div>
    </div>

</div>

<script>
    function fillLogin(username, password) {
        document.getElementById('username').value = username;
        document.getElementById('password').value = password;
        document.getElementById('loginForm').submit();
    }
</script>

</body>
</html>