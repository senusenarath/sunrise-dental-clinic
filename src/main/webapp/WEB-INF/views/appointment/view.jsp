<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%--
    view.jsp - Appointment Detail
    Location: /WEB-INF/views/appointment/view.jsp

    Satisfies the "Display Appointment Details - search using the
    appointment number, show complete patient and appointment
    information" requirement.
--%>

<%@ include file="../common/header.jsp" %>
<%@ include file="../common/navbar.jsp" %>

<!-- ═══════════════════════════════════════════════════════════════
     APPOINTMENT HEADER
     ═══════════════════════════════════════════════════════════════ -->
<div class="profile-header-card">
    <div class="profile-avatar">
        <span class="avatar-icon">
            <c:out value="${appointment.statusIcon}"/>
        </span>
    </div>
    <div class="profile-info">
        <h2 class="profile-name">
            <span class="code-tag"><c:out value="${appointment.aptCode}"/></span>
        </h2>
        <div class="profile-meta">
            <span class="meta-item">
                👤 <c:out value="${appointment.patientName}"/>
            </span>
            <span class="meta-sep">•</span>
            <span class="meta-item">
                <span class="badge ${appointment.statusBadgeClass}">
                    <c:out value="${appointment.statusIcon}"/>
                    <c:out value="${appointment.status}"/>
                </span>
            </span>
        </div>
    </div>
    <div class="profile-actions">
        <c:if test="${(loggedInUser.role == 'ADMIN' || loggedInUser.role == 'DENTIST')
                       && appointment.status != 'Cancelled'
                       && appointment.status != 'Completed'}">
            <a href="${pageContext.request.contextPath}/appointments/update-treatment?id=${appointment.id}"
               class="btn btn-primary">
                ⚕️ Update Treatment
            </a>
        </c:if>
        <c:if test="${appointment.status == 'Scheduled' && loggedInUser.role != 'DENTIST'}">
            <a href="${pageContext.request.contextPath}/appointments/reschedule?id=${appointment.id}"
               class="btn btn-warning">
                🔄 Reschedule
            </a>
        </c:if>
        <c:if test="${appointment.hasBill}">
            <a href="${pageContext.request.contextPath}/bills/view?id=${appointment.id}"
               class="btn btn-success">
                🧾 View Bill
            </a>
        </c:if>
        <c:if test="${!appointment.hasBill && appointment.status == 'Completed'
                       && loggedInUser.role != 'DENTIST'}">
            <a href="${pageContext.request.contextPath}/bills/create?aptId=${appointment.id}"
               class="btn btn-success">
                🧾 Generate Bill
            </a>
        </c:if>
        <a href="${pageContext.request.contextPath}/appointments"
           class="btn btn-secondary">
            ← Back to Schedule
        </a>
    </div>
</div>

<!-- Success Alert -->
<c:if test="${not empty successMsg}">
    <div class="alert alert-success">
        <span>✅</span>
        <span><c:out value="${successMsg}"/></span>
    </div>
</c:if>

<!-- ═══════════════════════════════════════════════════════════════
     APPOINTMENT + PATIENT DETAILS
     ═══════════════════════════════════════════════════════════════ -->
<div class="two-col-layout">

    <!-- Appointment Details -->
    <div class="section-card">
        <div class="section-header">
            <h3 class="section-title">📅 Appointment Details</h3>
        </div>
        <div class="detail-list">
            <div class="detail-item">
                <span class="detail-label">Appointment Code</span>
                <span class="detail-value">
                    <span class="code-tag"><c:out value="${appointment.aptCode}"/></span>
                </span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Date</span>
                <span class="detail-value"><c:out value="${appointment.aptDate}"/></span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Time</span>
                <span class="detail-value"><c:out value="${appointment.aptTime}"/></span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Dentist</span>
                <span class="detail-value"><c:out value="${appointment.dentistName}"/></span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Treatment</span>
                <span class="detail-value"><c:out value="${appointment.treatmentName}"/></span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Treatment Cost</span>
                <span class="detail-value">
                    LKR <fmt:formatNumber value="${appointment.treatmentCost}" pattern="#,##0.00"/>
                </span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Consultation Fee</span>
                <span class="detail-value">
                    LKR <fmt:formatNumber value="${appointment.consultFee}" pattern="#,##0.00"/>
                </span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Total Cost</span>
                <span class="detail-value">
                    <strong>LKR <fmt:formatNumber value="${appointment.totalCost}" pattern="#,##0.00"/></strong>
                </span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Status</span>
                <span class="detail-value">
                    <span class="badge ${appointment.statusBadgeClass}">
                        <c:out value="${appointment.statusIcon}"/>
                        <c:out value="${appointment.status}"/>
                    </span>
                </span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Notes</span>
                <span class="detail-value">
                    <c:choose>
                        <c:when test="${not empty appointment.notes}">
                            <c:out value="${appointment.notes}"/>
                        </c:when>
                        <c:otherwise>
                            <span class="text-muted">No notes recorded.</span>
                        </c:otherwise>
                    </c:choose>
                </span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Booked By</span>
                <span class="detail-value text-muted text-sm">
                    <c:out value="${appointment.createdByName}"/>
                </span>
            </div>
        </div>
    </div>

    <!-- Patient Details -->
    <div class="section-card">
        <div class="section-header">
            <h3 class="section-title">👤 Patient</h3>
        </div>
        <div class="detail-list">
            <div class="detail-item">
                <span class="detail-label">Patient Code</span>
                <span class="detail-value">
                    <span class="code-tag"><c:out value="${appointment.patientCode}"/></span>
                </span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Full Name</span>
                <span class="detail-value">
                    <a href="${pageContext.request.contextPath}/patients/view?id=${appointment.patientId}"
                       class="patient-link">
                        <strong><c:out value="${appointment.patientName}"/></strong>
                    </a>
                </span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Contact</span>
                <span class="detail-value"><c:out value="${appointment.patientContact}"/></span>
            </div>
        </div>

        <!-- Cancel -->
        <c:if test="${loggedInUser.role != 'DENTIST' && appointment.isCancellable()}">
            <div class="danger-zone">
                <h4 class="danger-title">⚠️ Cancel Appointment</h4>
                <p class="danger-text">
                    This will mark the appointment as cancelled and free up the time slot.
                </p>
                <form action="${pageContext.request.contextPath}/appointments/cancel"
                      method="post"
                      onsubmit="return confirm('Cancel appointment ${appointment.aptCode}?')">
                    <input type="hidden" name="appointmentId" value="${appointment.id}">
                    <button type="submit" class="btn btn-danger">
                        ❌ Cancel Appointment
                    </button>
                </form>
            </div>
        </c:if>
    </div>

</div>

<%@ include file="../common/footer.jsp" %>
