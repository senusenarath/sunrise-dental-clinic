<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%--
    dashboard.jsp - Executive Analytics & Reports
    Location: /WEB-INF/views/report/dashboard.jsp
--%>

<%@ include file="../common/header.jsp" %>
<%@ include file="../common/navbar.jsp" %>

<!-- ═══════════════════════════════════════════════════════════════
     KEY METRICS
     ═══════════════════════════════════════════════════════════════ -->
<div class="metrics-grid">
    <div class="metric-card metric-green">
        <div class="metric-icon">💰</div>
        <div class="metric-body">
            <div class="metric-value">
                LKR <fmt:formatNumber value="${totalRevenue}" pattern="#,##0"/>
            </div>
            <div class="metric-label">Total Revenue</div>
            <div class="metric-sub">All time</div>
        </div>
    </div>
    <div class="metric-card metric-teal">
        <div class="metric-icon">📅</div>
        <div class="metric-body">
            <div class="metric-value">
                LKR <fmt:formatNumber value="${monthlyRevenue}" pattern="#,##0"/>
            </div>
            <div class="metric-label">Monthly Revenue</div>
            <div class="metric-sub">This month</div>
        </div>
    </div>
    <div class="metric-card metric-gold">
        <div class="metric-icon">⏳</div>
        <div class="metric-body">
            <div class="metric-value">
                LKR <fmt:formatNumber value="${pendingAmount}" pattern="#,##0"/>
            </div>
            <div class="metric-label">Pending Receivables</div>
            <div class="metric-sub">${pendingBills} unpaid bills</div>
        </div>
    </div>
    <div class="metric-card metric-blue">
        <div class="metric-icon">👥</div>
        <div class="metric-body">
            <div class="metric-value">${totalPatients}</div>
            <div class="metric-label">Total Patients</div>
            <div class="metric-sub">+${newPatientsMonth} this month</div>
        </div>
    </div>
</div>

<!-- ═══════════════════════════════════════════════════════════════
     TWO COLUMN LAYOUT
     ═══════════════════════════════════════════════════════════════ -->
<div class="two-col-layout">

    <!-- Appointment Status Distribution -->
    <div class="section-card">
        <div class="section-header">
            <h3 class="section-title">📊 Appointment Status</h3>
        </div>

        <div class="report-stat-block">
            <div class="report-stat-row">
                <div class="report-stat-info">
                    <span class="report-stat-label">📋 Scheduled</span>
                    <span class="report-stat-val">${scheduledCount}</span>
                </div>
                <div class="progress-bar-wrap">
                    <div class="progress-bar bar-blue"
                         style="width: ${totalAppointments > 0 ?
                                (scheduledCount * 100 / totalAppointments) : 0}%">
                    </div>
                </div>
                <span class="progress-pct">
                    <fmt:formatNumber
                        value="${totalAppointments > 0 ?
                               (scheduledCount * 100.0 / totalAppointments) : 0}"
                        pattern="0.0"/>%
                </span>
            </div>

            <div class="report-stat-row">
                <div class="report-stat-info">
                    <span class="report-stat-label">✅ Completed</span>
                    <span class="report-stat-val">${completedCount}</span>
                </div>
                <div class="progress-bar-wrap">
                    <div class="progress-bar bar-green"
                         style="width: ${totalAppointments > 0 ?
                                (completedCount * 100 / totalAppointments) : 0}%">
                    </div>
                </div>
                <span class="progress-pct">
                    <fmt:formatNumber
                        value="${totalAppointments > 0 ?
                               (completedCount * 100.0 / totalAppointments) : 0}"
                        pattern="0.0"/>%
                </span>
            </div>

            <div class="report-stat-row">
                <div class="report-stat-info">
                    <span class="report-stat-label">📆 This Month</span>
                    <span class="report-stat-val">${thisMonthCount}</span>
                </div>
                <div class="progress-bar-wrap">
                    <div class="progress-bar bar-teal"
                         style="width: ${totalAppointments > 0 ?
                                (thisMonthCount * 100 / totalAppointments) : 0}%">
                    </div>
                </div>
                <span class="progress-pct">
                    <fmt:formatNumber
                        value="${totalAppointments > 0 ?
                               (thisMonthCount * 100.0 / totalAppointments) : 0}"
                        pattern="0.0"/>%
                </span>
            </div>

            <div class="report-stat-row">
                <div class="report-stat-info">
                    <span class="report-stat-label">🕐 Today</span>
                    <span class="report-stat-val">${todayCount}</span>
                </div>
                <div class="progress-bar-wrap">
                    <div class="progress-bar bar-gold"
                         style="width: ${totalAppointments > 0 ?
                                (todayCount * 100 / totalAppointments) : 0}%">
                    </div>
                </div>
                <span class="progress-pct">
                    <fmt:formatNumber
                        value="${totalAppointments > 0 ?
                               (todayCount * 100.0 / totalAppointments) : 0}"
                        pattern="0.0"/>%
                </span>
            </div>
        </div>

        <div class="report-total">
            Total Appointments: <strong>${totalAppointments}</strong>
        </div>
    </div>

    <!-- Staff Summary -->
    <div class="section-card">
        <div class="section-header">
            <h3 class="section-title">👤 Staff Summary</h3>
        </div>

        <div class="report-stat-block">
            <div class="report-stat-row">
                <div class="report-stat-info">
                    <span class="report-stat-label">👑 Administrators</span>
                    <span class="report-stat-val">
                        ${totalStaff - totalDentists}
                    </span>
                </div>
                <div class="progress-bar-wrap">
                    <div class="progress-bar bar-purple"
                         style="width: ${totalStaff > 0 ?
                                ((totalStaff - totalDentists) * 100 / totalStaff) : 0}%">
                    </div>
                </div>
            </div>
            <div class="report-stat-row">
                <div class="report-stat-info">
                    <span class="report-stat-label">🩺 Dentists</span>
                    <span class="report-stat-val">${totalDentists}</span>
                </div>
                <div class="progress-bar-wrap">
                    <div class="progress-bar bar-teal"
                         style="width: ${totalStaff > 0 ?
                                (totalDentists * 100 / totalStaff) : 0}%">
                    </div>
                </div>
            </div>
        </div>

        <!-- Dentist List -->
        <h4 class="sub-section-title">🩺 Dentist Roster</h4>
        <c:forEach var="d" items="${dentists}">
            <c:set var="workload" value="${dentistWorkloads[d.id]}"/>
            <c:set var="busyUntil" value="${dentistAvailability[d.id]}"/>
            <div class="dentist-row">
                <div class="dentist-info">
                    <strong><c:out value="${d.fullName}"/></strong>
                    <c:choose>
                        <c:when test="${not empty busyUntil}">
                            <span class="badge badge-cancelled">
                                🔴 With a Patient (until <c:out value="${busyUntil}"/>)
                            </span>
                        </c:when>
                        <c:otherwise>
                            <span class="badge badge-active">
                                🟢 Available Now
                            </span>
                        </c:otherwise>
                    </c:choose>
                    <span class="text-muted text-sm">
                        <c:out value="${d.specialization}"/>
                    </span>
                    <c:if test="${not empty workload}">
                        <div class="text-muted text-sm">
                            📅 <c:out value="${workload.totalAppointments}"/> this month
                            (<c:out value="${workload.completed}"/> completed)
                            &bull; LKR <fmt:formatNumber
                                           value="${workload.revenueGenerated}"
                                           pattern="#,##0"/> generated
                        </div>
                    </c:if>
                </div>
                <div class="dentist-fee">
                    LKR <fmt:formatNumber
                             value="${d.consultFee}"
                             pattern="#,##0.00"/>
                    <span class="text-muted">/consult</span>
                </div>
            </div>
        </c:forEach>
    </div>

</div>

<!-- ═══════════════════════════════════════════════════════════════
     REVENUE TREND CHART
     ═══════════════════════════════════════════════════════════════ -->
<div class="section-card">
    <div class="section-header">
        <h3 class="section-title">📈 Revenue Trend (Last 6 Months)</h3>
    </div>

    <c:choose>
        <c:when test="${empty revenueTrend}">
            <div class="empty-state">
                <p class="empty-title">No paid revenue recorded in the last 6 months</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="revenue-chart">
                <c:forEach var="point" items="${revenueTrend}">
                    <div class="revenue-bar-col">
                        <div class="revenue-bar-value">
                            <fmt:formatNumber value="${point.revenue}" pattern="#,##0"/>
                        </div>
                        <div class="revenue-bar-track">
                            <div class="revenue-bar-fill"
                                 style="height: ${maxMonthlyRevenue > 0 ? (point.revenue * 150 / maxMonthlyRevenue) : 0}px">
                            </div>
                        </div>
                        <div class="revenue-bar-label">
                            <c:out value="${point.monthLabel}"/>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<!-- ═══════════════════════════════════════════════════════════════
     TREATMENT STATISTICS TABLE
     ═══════════════════════════════════════════════════════════════ -->
<div class="section-card">
    <div class="section-header">
        <h3 class="section-title">🦷 Treatment Procedure Analytics</h3>
        <a href="${pageContext.request.contextPath}/reports?export=pdf" class="btn btn-secondary no-print">
            📄 Download PDF
        </a>
    </div>

    <c:choose>
        <c:when test="${empty treatmentStats}">
            <div class="empty-state">
                <p class="empty-title">No treatment data available</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="table-wrapper">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Treatment Procedure</th>
                            <th>Base Cost (LKR)</th>
                            <th>Appointments</th>
                            <th>Revenue Generated (LKR)</th>
                            <th>Popularity</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:set var="maxCount" value="1"/>
                        <c:forEach var="stat" items="${treatmentStats}">
                            <c:if test="${stat.appointmentCount > maxCount}">
                                <c:set var="maxCount" value="${stat.appointmentCount}"/>
                            </c:if>
                        </c:forEach>

                        <c:forEach var="stat"
                                   items="${treatmentStats}"
                                   varStatus="loop">
                            <tr>
                                <td class="text-muted">
                                    <c:out value="${loop.count}"/>
                                </td>
                                <td>
                                    <strong>
                                        <c:out value="${stat.name}"/>
                                    </strong>
                                </td>
                                <td>
                                    <fmt:formatNumber
                                        value="${stat.baseCost}"
                                        pattern="#,##0.00"/>
                                </td>
                                <td>
                                    <span class="badge badge-info">
                                        <c:out value="${stat.appointmentCount}"/>
                                    </span>
                                </td>
                                <td>
                                    <fmt:formatNumber
                                        value="${stat.totalRevenue}"
                                        pattern="#,##0.00"/>
                                </td>
                                <td>
                                    <div class="mini-progress">
                                        <div class="mini-bar bar-blue"
                                             style="width: ${maxCount > 0 ?
                                                    (stat.appointmentCount * 100 / maxCount) : 0}%">
                                        </div>
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

<%@ include file="../common/footer.jsp" %>