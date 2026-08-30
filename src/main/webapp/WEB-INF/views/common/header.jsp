<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
    header.jsp
    Top header bar with brand, hamburger toggle and user info
    Location: /WEB-INF/views/common/header.jsp
--%>

<%
    lk.sunrise.dental.model.User currentUser =
        (lk.sunrise.dental.model.User) session.getAttribute("loggedInUser");
    String contextPath = request.getContextPath();
    String currentURI  = request.getRequestURI();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${pageTitle}"/> | Sunrise Dental Clinic</title>
    <link rel="stylesheet" href="<%= contextPath %>/assets/css/style.css">
    <link rel="stylesheet" href="<%= contextPath %>/assets/css/print.css" media="print">
</head>
<body>

<!-- ═══════════════════════════════════════════════════════════════
     TOP HEADER BAR
     ═══════════════════════════════════════════════════════════════ -->
<header class="top-header no-print">
    <div class="header-inner">

        <!-- Brand + Hamburger -->
        <div class="header-left">
            <a href="<%= contextPath %>/dashboard" class="header-brand">
                <span class="brand-icon">🦷</span>
                <div class="brand-text">
                    <span class="brand-name">Sunrise Dental</span>
                    <span class="brand-sub">Clinic Management System</span>
                </div>
            </a>
            <button class="hamburger" id="sidebarToggle" onclick="toggleSidebar()">
                <span class="hamburger-line"></span>
                <span class="hamburger-line"></span>
                <span class="hamburger-line"></span>
            </button>
        </div>

        <!-- User Info -->
        <div class="header-right">
            <% if (currentUser != null) { %>
            <div class="header-user">
                <span class="user-avatar">
                    <%= currentUser.isAdmin() ? "👑" :
                        currentUser.isDentist() ? "🩺" : "🖥️" %>
                </span>
                <div class="user-details">
                    <span class="user-name">
                        <c:out value="${loggedInUser.displayName}"/>
                    </span>
                    <span class="user-role-tag">
                        <c:out value="${loggedInUser.role}"/>
                    </span>
                </div>
            </div>
            <% } %>
        </div>

    </div>
</header>

<!-- ═══════════════════════════════════════════════════════════════
     SIDEBAR OVERLAY (for mobile)
     ═══════════════════════════════════════════════════════════════ -->
<div class="sidebar-overlay" id="sidebarOverlay" onclick="closeSidebar()"></div>

<!-- ═══════════════════════════════════════════════════════════════
     SIDEBAR NAVIGATION
     ═══════════════════════════════════════════════════════════════ -->
<aside class="sidebar" id="sidebar">

    <!-- Navigation Links -->
    <nav class="sidebar-nav">

        <div class="nav-section">
            <span class="nav-section-label">MAIN</span>
        </div>

        <a href="<%= contextPath %>/dashboard"
           class="sidebar-link <%= currentURI.contains("/dashboard") ? "active" : "" %>">
            <span class="sidebar-icon">📊</span>
            <span class="sidebar-text">Dashboard</span>
        </a>

        <a href="<%= contextPath %>/patients"
           class="sidebar-link <%= currentURI.contains("/patients") ? "active" : "" %>">
            <span class="sidebar-icon">👥</span>
            <span class="sidebar-text">Patients</span>
        </a>

        <a href="<%= contextPath %>/appointments"
           class="sidebar-link <%= currentURI.contains("/appointments") ? "active" : "" %>">
            <span class="sidebar-icon">📅</span>
            <span class="sidebar-text">Appointments</span>
        </a>

        <% if (currentUser != null && !currentUser.isDentist()) { %>
        <a href="<%= contextPath %>/bills"
           class="sidebar-link <%= currentURI.contains("/bills") ? "active" : "" %>">
            <span class="sidebar-icon">💰</span>
            <span class="sidebar-text">Billing</span>
        </a>
        <% } %>

        <% if (currentUser != null && currentUser.isAdmin()) { %>

        <div class="nav-section">
            <span class="nav-section-label">ADMIN</span>
        </div>

        <a href="<%= contextPath %>/staff"
           class="sidebar-link <%= currentURI.contains("/staff") ? "active" : "" %>">
            <span class="sidebar-icon">👤</span>
            <span class="sidebar-text">Staff</span>
        </a>

        <a href="<%= contextPath %>/reports"
           class="sidebar-link <%= currentURI.contains("/reports") ? "active" : "" %>">
            <span class="sidebar-icon">📈</span>
            <span class="sidebar-text">Reports</span>
        </a>
        <% } %>

        <div class="nav-section">
            <span class="nav-section-label">SUPPORT</span>
        </div>

        <a href="<%= contextPath %>/help"
           class="sidebar-link <%= currentURI.contains("/help") ? "active" : "" %>">
            <span class="sidebar-icon">❓</span>
            <span class="sidebar-text">Help & Manual</span>
        </a>

    </nav>

    <!-- Logout - pinned to the bottom of the sidebar -->
    <div class="sidebar-footer">
        <form action="<%= contextPath %>/logout" method="post">
            <button type="submit" class="sidebar-link sidebar-logout-btn">
                <span class="sidebar-icon">🚪</span>
                <span class="sidebar-text">Logout</span>
            </button>
        </form>
    </div>

</aside>

<!-- ═══════════════════════════════════════════════════════════════
     MAIN CONTENT WRAPPER
     ═══════════════════════════════════════════════════════════════ -->
<main class="main-content" id="mainContent">
<div class="container">