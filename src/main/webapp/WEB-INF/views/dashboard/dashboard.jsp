<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%--
    dashboard.jsp
    Role-specific command center with KPIs and today's schedule
    Location: /WEB-INF/views/dashboard/dashboard.jsp
--%>

<%@ include file="../common/header.jsp" %>
<%@ include file="../common/navbar.jsp" %>

<!-- ═══════════════════════════════════════════════════════════════
     WELCOME BANNER
     ═══════════════════════════════════════════════════════════════ -->
<div class="welcome-banner">
    <div class="welcome-left">
        <h2 class="welcome-title">
            Good
            <% int hour = java.time.LocalTime.now().getHour();
               if (hour < 12) out.print("Morning");
               else if (hour < 17) out.print("Afternoon");
               else out.print("Evening"); %>,
            <c:out value="${loggedInUser.fullName}"/> 👋
        </h2>
        <p class="welcome-sub">
            <c:out value="${loggedInUser.roleDisplay}"/> &bull;
            <%= java.time.LocalDate.now().format(
                java.time.format.DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")) %>
        </p>
    </div>
    <div class="welcome-right">
        <div class="welcome-stat">
            <span class="welcome-stat-num">${todayAppointments}</span>
            <span class="welcome-stat-label">Today's Appointments</span>
        </div>
    </div>
</div>

<!-- ═══════════════════════════════════════════════════════════════
     KPI METRIC CARDS
     ═══════════════════════════════════════════════════════════════ -->
<div class="metrics-grid">

    <!-- Total Patients -->
    <div class="metric-card metric-blue">
        <div class="metric-icon">👥</div>
        <div class="metric-body">
            <div class="metric-value">${totalPatients}</div>
            <div class="metric-label">Total Patients</div>
            <div class="metric-sub">+${newPatientsMonth} this month</div>
        </div>
    </div>

    <!-- Today's Appointments -->
    <div class="metric-card metric-teal">
        <div class="metric-icon">📅</div>
        <div class="metric-body">
            <div class="metric-value">${todayAppointments}</div>
            <div class="metric-label">Today's Schedule</div>
            <div class="metric-sub">${scheduledCount} scheduled total</div>
        </div>
    </div>

    <!-- Completed Treatments -->
    <div class="metric-card metric-green">
        <div class="metric-icon">✅</div>
        <div class="metric-body">
            <div class="metric-value">${completedCount}</div>
            <div class="metric-label">Completed Treatments</div>
            <div class="metric-sub">${thisMonthApts} this month</div>
        </div>
    </div>

    <!-- Financial (Admin + Receptionist only) -->
    <c:if test="${loggedInUser.role != 'DENTIST'}">
        <div class="metric-card metric-gold">
            <div class="metric-icon">💰</div>
            <div class="metric-body">
                <div class="metric-value">
                    LKR <fmt:formatNumber value="${totalRevenue}"
                                          pattern="#,##0.00"/>
                </div>
                <div class="metric-label">Total Revenue</div>
                <div class="metric-sub">${pendingBills} pending bills</div>
            </div>
        </div>
    </c:if>

    <!-- Staff Count (Admin only) -->
    <c:if test="${loggedInUser.role == 'ADMIN'}">
        <div class="metric-card metric-purple">
            <div class="metric-icon">👤</div>
            <div class="metric-body">
                <div class="metric-value">${totalStaff}</div>
                <div class="metric-label">Active Staff</div>
                <div class="metric-sub">${totalDentists} dentists</div>
            </div>
        </div>
    </c:if>

</div>

<!-- ═══════════════════════════════════════════════════════════════
     QUICK ACTION BUTTONS
     ═══════════════════════════════════════════════════════════════ -->
<div class="section-card">
    <div class="section-header">
        <h3 class="section-title">⚡ Quick Actions</h3>
    </div>
    <div class="quick-actions">

        <c:if test="${loggedInUser.role != 'DENTIST'}">
            <a href="${pageContext.request.contextPath}/patients/add"
               class="quick-action-btn">
                <span class="qa-icon">👥</span>
                <span class="qa-label">Register Patient</span>
            </a>
        </c:if>

        <c:if test="${loggedInUser.role != 'DENTIST'}">
            <a href="${pageContext.request.contextPath}/appointments/register"
               class="quick-action-btn">
                <span class="qa-icon">📅</span>
                <span class="qa-label">Book Appointment</span>
            </a>
        </c:if>

        <a href="${pageContext.request.contextPath}/appointments"
           class="quick-action-btn">
            <span class="qa-icon">📋</span>
            <span class="qa-label">View Schedule</span>
        </a>

        <c:if test="${loggedInUser.role != 'DENTIST'}">
            <a href="${pageContext.request.contextPath}/bills/create"
               class="quick-action-btn">
                <span class="qa-icon">🧾</span>
                <span class="qa-label">Generate Invoice</span>
            </a>
        </c:if>

        <c:if test="${loggedInUser.role != 'DENTIST'}">
            <form action="${pageContext.request.contextPath}/appointments/send-reminders"
                  method="post"
                  onsubmit="return confirm('Send a reminder email to every patient with an appointment tomorrow?')">
                <button type="submit" class="quick-action-btn">
                    <span class="qa-icon">📨</span>
                    <span class="qa-label">Send Reminders</span>
                </button>
            </form>
        </c:if>

        <a href="${pageContext.request.contextPath}/patients"
           class="quick-action-btn">
            <span class="qa-icon">🔍</span>
            <span class="qa-label">Find Patient</span>
        </a>

        <c:if test="${loggedInUser.role == 'ADMIN'}">
            <a href="${pageContext.request.contextPath}/reports"
               class="quick-action-btn">
                <span class="qa-icon">📈</span>
                <span class="qa-label">View Reports</span>
            </a>
        </c:if>

        <a href="${pageContext.request.contextPath}/help"
           class="quick-action-btn">
            <span class="qa-icon">❓</span>
            <span class="qa-label">Help Manual</span>
        </a>

    </div>
</div>

<!-- ═══════════════════════════════════════════════════════════════
     TODAY'S APPOINTMENTS TABLE
     ═══════════════════════════════════════════════════════════════ -->
<div class="section-card">
    <div class="section-header">
        <h3 class="section-title">
            📅 Today's Appointments
            <span class="badge badge-info">${todayAppointments}</span>
        </h3>
        <a href="${pageContext.request.contextPath}/appointments"
           class="btn btn-sm btn-secondary">
            View All →
        </a>
    </div>

    <c:choose>
        <c:when test="${empty todayAptList}">
            <div class="empty-state">
                <div class="empty-icon">📭</div>
                <p class="empty-title">No appointments scheduled for today</p>
                <p class="empty-sub">Click "Book Appointment" to schedule one</p>
                <c:if test="${loggedInUser.role != 'DENTIST'}">
                    <a href="${pageContext.request.contextPath}/appointments/register"
                       class="btn btn-primary">
                        📅 Book Appointment
                    </a>
                </c:if>
            </div>
        </c:when>
        <c:otherwise>
            <div class="table-wrapper">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Apt Code</th>
                            <th>Patient</th>
                            <th>Dentist</th>
                            <th>Treatment</th>
                            <th>Time</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="apt" items="${todayAptList}">
                            <tr>
                                <td>
                                    <span class="code-tag">
                                        <c:out value="${apt.aptCode}"/>
                                    </span>
                                </td>
                                <td>
                                    <strong>
                                        <c:out value="${apt.patientName}"/>
                                    </strong>
                                    <br>
                                    <small class="text-muted">
                                        <c:out value="${apt.patientContact}"/>
                                    </small>
                                </td>
                                <td><c:out value="${apt.dentistName}"/></td>
                                <td><c:out value="${apt.treatmentName}"/></td>
                                <td>
                                    <strong>
                                        <c:out value="${apt.aptTime}"/>
                                    </strong>
                                </td>
                                <td>
                                    <span class="badge ${apt.statusBadgeClass}">
                                        <c:out value="${apt.statusIcon}"/>
                                        <c:out value="${apt.status}"/>
                                    </span>
                                </td>
                                <td>
                                    <div class="action-btns">
                                        <c:if test="${loggedInUser.role == 'ADMIN' ||
                                                      loggedInUser.role == 'DENTIST'}">
                                            <a href="${pageContext.request.contextPath}/appointments/update-treatment?id=${apt.id}"
                                               class="btn btn-xs btn-primary">
                                                Update
                                            </a>
                                        </c:if>
                                        <c:if test="${loggedInUser.role != 'DENTIST' && !apt.hasBill && apt.status == 'Completed'}">
                                            <a href="${pageContext.request.contextPath}/bills/create?aptId=${apt.id}"
                                               class="btn btn-xs btn-success">
                                                Bill
                                            </a>
                                        </c:if>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<!-- ═══════════════════════════════════════════════════════════════
     PENDING BILLS ALERT (Admin + Receptionist)
     ═══════════════════════════════════════════════════════════════ -->
<c:if test="${loggedInUser.role != 'DENTIST' && pendingBills > 0}">
    <div class="alert-banner alert-warning">
        <span class="alert-icon">⚠️</span>
        <div class="alert-body">
            <strong>Pending Bills Alert:</strong>
            You have <strong>${pendingBills}</strong> unpaid invoice(s)
            totalling <strong>LKR
            <fmt:formatNumber value="${pendingAmount}" pattern="#,##0.00"/>
            </strong>
        </div>
        <a href="${pageContext.request.contextPath}/bills?status=Pending"
           class="btn btn-sm btn-warning">
            View Pending Bills →
        </a>
    </div>
</c:if>

<%@ include file="../common/footer.jsp" %>