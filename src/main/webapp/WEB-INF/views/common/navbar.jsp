<%@ page contentType="text/html;charset=UTF-8" %>
<%--
    navbar.jsp
    Page title bar with breadcrumb and action buttons
    Location: /WEB-INF/views/common/navbar.jsp
--%>

<%
    lk.sunrise.dental.model.User navUser =
        (lk.sunrise.dental.model.User) session.getAttribute("loggedInUser");
    String ctx = request.getContextPath();
%>

<!-- ═══════════════════════════════════════════════════════════════
     PAGE TITLE BAR
     ═══════════════════════════════════════════════════════════════ -->
<div class="page-title-bar no-print">
    <div class="page-title-left">
        <h1 class="page-title">${pageTitle}</h1>
        <nav class="breadcrumb">
            <a href="<%= ctx %>/dashboard" class="breadcrumb-item">🏠 Home</a>
            <span class="breadcrumb-sep">›</span>
            <span class="breadcrumb-item active">${pageTitle}</span>
        </nav>
    </div>

    <div class="page-title-right">
        <% String uri = request.getRequestURI(); %>

        <% if (uri.contains("/patients") && !uri.contains("/add")
               && !uri.contains("/edit") && !uri.contains("/view")) { %>
            <a href="<%= ctx %>/patients/add" class="btn btn-primary">
                ➕ Register Patient
            </a>
        <% } %>

        <% if (uri.contains("/appointments") && !uri.contains("/register")
               && !uri.contains("/update") && !uri.contains("/view")
               && navUser != null && !navUser.isDentist()) { %>
            <a href="<%= ctx %>/appointments/register" class="btn btn-primary">
                📅 Book Appointment
            </a>
        <% } %>

        <% if (uri.contains("/bills") && !uri.contains("/create")
               && !uri.contains("/view")
               && navUser != null && !navUser.isDentist()) { %>
            <a href="<%= ctx %>/bills/create" class="btn btn-primary">
                🧾 Generate Invoice
            </a>
        <% } %>

        <% if (uri.contains("/staff") && !uri.contains("/add")
               && !uri.contains("/edit")
               && navUser != null && navUser.isAdmin()) { %>
            <a href="<%= ctx %>/staff/add" class="btn btn-primary">
                👤 Add Staff
            </a>
        <% } %>

        <% if (uri.contains("/reports")) { %>
            <a href="<%= ctx %>/reports?export=csv" class="btn btn-success">
                📥 Export CSV
            </a>
            <a href="<%= ctx %>/reports?export=pdf" class="btn btn-secondary">
                📄 Download PDF
            </a>
        <% } %>
    </div>
</div>

<!-- ═══════════════════════════════════════════════════════════════
     URL PARAM ALERTS
     ═══════════════════════════════════════════════════════════════ -->
<% String successParam = request.getParameter("success");
   String errorParam   = request.getParameter("error"); %>

<% if (successParam != null && !successParam.trim().isEmpty()) { %>
<div class="alert alert-success">
    <span>✅</span>
    <span><%= java.net.URLDecoder.decode(successParam, "UTF-8") %></span>
</div>
<% } %>

<% if (errorParam != null && !errorParam.trim().isEmpty()) { %>
<div class="alert alert-error">
    <span>❌</span>
    <span><%= java.net.URLDecoder.decode(errorParam, "UTF-8") %></span>
</div>
<% } %>