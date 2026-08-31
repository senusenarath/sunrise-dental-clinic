<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%--
    list.jsp - Appointment Schedule & Directory
    Location: /WEB-INF/views/appointment/list.jsp
--%>

<%@ include file="../common/header.jsp" %>
<%@ include file="../common/navbar.jsp" %>

<!-- ═══════════════════════════════════════════════════════════════
     STATS ROW
     ═══════════════════════════════════════════════════════════════ -->
<div class="metrics-grid">
    <div class="metric-card metric-blue">
        <div class="metric-icon">📅</div>
        <div class="metric-body">
            <div class="metric-value">${totalAppointments}</div>
            <div class="metric-label">Total Appointments</div>
        </div>
    </div>
    <div class="metric-card metric-teal">
        <div class="metric-icon">🕐</div>
        <div class="metric-body">
            <div class="metric-value">${todayCount}</div>
            <div class="metric-label">Today's Schedule</div>
        </div>
    </div>
    <div class="metric-card metric-gold">
        <div class="metric-icon">📋</div>
        <div class="metric-body">
            <div class="metric-value">${scheduledCount}</div>
            <div class="metric-label">Scheduled</div>
        </div>
    </div>
    <div class="metric-card metric-green">
        <div class="metric-icon">✅</div>
        <div class="metric-body">
            <div class="metric-value">${completedCount}</div>
            <div class="metric-label">Completed</div>
        </div>
    </div>
</div>

<!-- ═══════════════════════════════════════════════════════════════
     FILTER & SEARCH BAR
     ═══════════════════════════════════════════════════════════════ -->
<div class="section-card">
    <div class="filter-bar">

        <!-- Search -->
        <form action="${pageContext.request.contextPath}/appointments"
              method="get"
              class="search-form-inline">
            <input type="text"
                   name="search"
                   class="search-input"
                   placeholder="🔍 Search by patient name or appointment code..."
                   value="<c:out value='${searchKeyword}'/>">
            <button type="submit" class="btn btn-primary">Search</button>
        </form>

        <!-- Status Filter -->
        <div class="filter-tabs">
            <a href="${pageContext.request.contextPath}/appointments"
               class="filter-tab ${empty statusFilter && empty searchKeyword ? 'active' : ''}">
                All
            </a>
            <a href="${pageContext.request.contextPath}/appointments?status=Scheduled"
               class="filter-tab ${statusFilter == 'Scheduled' ? 'active' : ''}">
                📋 Scheduled
            </a>
            <a href="${pageContext.request.contextPath}/appointments?status=In+Progress"
               class="filter-tab ${statusFilter == 'In Progress' ? 'active' : ''}">
                ⚙️ In Progress
            </a>
            <a href="${pageContext.request.contextPath}/appointments?status=Completed"
               class="filter-tab ${statusFilter == 'Completed' ? 'active' : ''}">
                ✅ Completed
            </a>
            <a href="${pageContext.request.contextPath}/appointments?status=Cancelled"
               class="filter-tab ${statusFilter == 'Cancelled' ? 'active' : ''}">
                ❌ Cancelled
            </a>
        </div>

    </div>
</div>

<!-- ═══════════════════════════════════════════════════════════════
     APPOINTMENTS TABLE
     ═══════════════════════════════════════════════════════════════ -->
<div class="section-card">
    <div class="section-header">
        <h3 class="section-title">
            📅 Appointment Schedule
            <span class="badge badge-info">${appointments.size()}</span>
        </h3>
        <div class="action-btns">
            <a href="${pageContext.request.contextPath}/appointments?export=csv<c:if test="${not empty searchKeyword}">&search=${searchKeyword}</c:if><c:if test="${not empty statusFilter}">&status=${statusFilter}</c:if>"
               class="btn btn-secondary">
                📥 Download CSV
            </a>
            <c:if test="${loggedInUser.role != 'DENTIST'}">
                <a href="${pageContext.request.contextPath}/appointments/register"
                   class="btn btn-primary">
                    ➕ Book Appointment
                </a>
            </c:if>
        </div>
    </div>

    <c:choose>
        <c:when test="${empty appointments}">
            <div class="empty-state">
                <div class="empty-icon">📭</div>
                <p class="empty-title">No appointments found</p>
                <c:choose>
                    <c:when test="${not empty searchKeyword}">
                        <p class="empty-sub">
                            No results for
                            "<strong><c:out value="${searchKeyword}"/></strong>"
                        </p>
                        <a href="${pageContext.request.contextPath}/appointments"
                           class="btn btn-secondary">View All</a>
                    </c:when>
                    <c:otherwise>
                        <p class="empty-sub">
                            No appointments scheduled yet.
                        </p>
                        <c:if test="${loggedInUser.role != 'DENTIST'}">
                            <a href="${pageContext.request.contextPath}/appointments/register"
                               class="btn btn-primary">Book First Appointment</a>
                        </c:if>
                    </c:otherwise>
                </c:choose>
            </div>
        </c:when>
        <c:otherwise>
            <div class="table-wrapper">
                <table class="data-table" id="aptTable">
                    <thead>
                        <tr>
                            <th>Apt Code</th>
                            <th>Patient</th>
                            <th>Dentist</th>
                            <th>Treatment</th>
                            <th>Date</th>
                            <th>Time</th>
                            <th>Status</th>
                            <th>Bill</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="apt" items="${appointments}">
                            <tr class="${apt.status == 'Cancelled' ? 'row-cancelled' : ''}">
                                <td>
                                    <span class="code-tag">
                                        <c:out value="${apt.aptCode}"/>
                                    </span>
                                </td>
                                <td>
                                    <a href="${pageContext.request.contextPath}/patients/view?id=${apt.patientId}"
                                       class="patient-link">
                                        <strong><c:out value="${apt.patientName}"/></strong>
                                    </a>
                                    <br>
                                    <small class="text-muted">
                                        <c:out value="${apt.patientContact}"/>
                                    </small>
                                </td>
                                <td>
                                    <c:out value="${apt.dentistName}"/>
                                </td>
                                <td>
                                    <c:out value="${apt.treatmentName}"/>
                                    <br>
                                    <small class="text-muted">
                                        LKR <fmt:formatNumber
                                                value="${apt.treatmentCost}"
                                                pattern="#,##0.00"/>
                                    </small>
                                </td>
                                <td>
                                    <c:out value="${apt.aptDate}"/>
                                </td>
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
                                    <c:choose>
                                        <c:when test="${apt.hasBill}">
                                            <span class="badge badge-paid">
                                                ✅ <c:out value="${apt.billStatus}"/>
                                            </span>
                                        </c:when>
                                        <c:when test="${apt.status == 'Completed'
                                                && loggedInUser.role != 'DENTIST'}">
                                            <a href="${pageContext.request.contextPath}/bills/create?aptId=${apt.id}"
                                               class="btn btn-xs btn-success">
                                                🧾 Bill
                                            </a>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-muted">—</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <div class="action-btns">
                                        <!-- Update (Dentist + Admin) -->
                                        <c:if test="${(loggedInUser.role == 'ADMIN' ||
                                                       loggedInUser.role == 'DENTIST')
                                                       && apt.status != 'Cancelled'
                                                       && apt.status != 'Completed'}">
                                            <a href="${pageContext.request.contextPath}/appointments/update-treatment?id=${apt.id}"
                                               class="btn btn-xs btn-primary">
                                                ⚕️ Update
                                            </a>
                                        </c:if>

                                        <!-- Cancel (Admin + Receptionist) -->
                                        <c:if test="${loggedInUser.role != 'DENTIST'
                                                       && apt.isCancellable()}">
                                            <form action="${pageContext.request.contextPath}/appointments/cancel"
                                                  method="post"
                                                  style="display:inline;"
                                                  onsubmit="return confirm('Cancel appointment ${apt.aptCode}?')">
                                                <input type="hidden"
                                                       name="appointmentId"
                                                       value="${apt.id}">
                                                <button type="submit"
                                                        class="btn btn-xs btn-danger">
                                                    ❌ Cancel
                                                </button>
                                            </form>
                                        </c:if>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>

            <div class="table-footer">
                Showing <strong>${appointments.size()}</strong> appointment(s)
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="../common/footer.jsp" %>