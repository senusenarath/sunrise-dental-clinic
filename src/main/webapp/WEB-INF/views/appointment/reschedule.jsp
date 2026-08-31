<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%--
    reschedule.jsp - Reschedule Appointment Form
    Location: /WEB-INF/views/appointment/reschedule.jsp
--%>

<%@ include file="../common/header.jsp" %>
<%@ include file="../common/navbar.jsp" %>

<div class="two-col-layout">

    <!-- ── CURRENT APPOINTMENT DETAILS ───────────────────────────── -->
    <div class="section-card">
        <div class="section-header">
            <h3 class="section-title">📋 Current Appointment</h3>
        </div>

        <div class="detail-list">
            <div class="detail-item">
                <span class="detail-label">Apt Code</span>
                <span class="detail-value">
                    <span class="code-tag"><c:out value="${appointment.aptCode}"/></span>
                </span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Patient</span>
                <span class="detail-value">
                    <strong><c:out value="${appointment.patientName}"/></strong>
                </span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Treatment</span>
                <span class="detail-value"><c:out value="${appointment.treatmentName}"/></span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Current Dentist</span>
                <span class="detail-value"><c:out value="${appointment.dentistName}"/></span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Current Date &amp; Time</span>
                <span class="detail-value">
                    <c:out value="${appointment.aptDate}"/> at
                    <strong><c:out value="${appointment.aptTime}"/></strong>
                </span>
            </div>
        </div>
    </div>

    <!-- ── RESCHEDULE FORM ────────────────────────────────────────── -->
    <div class="section-card form-card">
        <div class="form-card-header">
            <h3>🔄 Reschedule Appointment</h3>
            <p class="form-subtitle">
                Choose a new dentist, date and time slot
            </p>
        </div>

        <c:if test="${not empty errorMsg}">
            <div class="alert alert-error">
                <span>❌</span>
                <span><c:out value="${errorMsg}"/></span>
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/appointments/reschedule"
              method="post"
              id="rescheduleForm">

            <input type="hidden" name="appointmentId" value="${appointment.id}">

            <!-- Dentist -->
            <div class="form-group required">
                <label for="dentistId">Dentist</label>
                <select id="dentistId" name="dentistId" class="form-control" required>
                    <option value="">-- Select Dentist --</option>
                    <c:forEach var="d" items="${dentists}">
                        <c:set var="busyUntil" value="${dentistAvailability[d.id]}"/>
                        <option value="${d.id}"
                            <c:if test="${d.id == appointment.dentistId}">selected</c:if>>
                            <c:choose>
                                <c:when test="${not empty busyUntil}">🔴</c:when>
                                <c:otherwise>🟢</c:otherwise>
                            </c:choose>
                            <c:out value="${d.fullName}"/>
                            <c:if test="${not empty d.specialization}">
                                (<c:out value="${d.specialization}"/>)
                            </c:if>
                            <c:if test="${not empty busyUntil}">
                                (busy until <c:out value="${busyUntil}"/>)
                            </c:if>
                        </option>
                    </c:forEach>
                </select>
            </div>

            <!-- Date -->
            <div class="form-group required">
                <label for="aptDate">New Date</label>
                <input type="date"
                       id="aptDate"
                       name="aptDate"
                       class="form-control"
                       required
                       min="<%= java.time.LocalDate.now() %>"
                       value="<c:out value='${appointment.aptDate}'/>">
            </div>

            <!-- Time -->
            <div class="form-group required">
                <label for="aptTime">New Time Slot</label>
                <select id="aptTime" name="aptTime" class="form-control" required>
                    <option value="">-- Select Time Slot --</option>
                    <c:forEach var="hour" begin="8" end="17">
                        <c:forEach var="min" items="${['00','30']}">
                            <c:set var="timeVal"
                                   value="${hour < 10 ? '0' : ''}${hour}:${min}"/>
                            <option value="${timeVal}"
                                <c:if test="${appointment.aptTime == timeVal}">selected</c:if>>
                                ${timeVal} ${hour < 12 ? 'AM' : 'PM'}
                            </option>
                        </c:forEach>
                    </c:forEach>
                </select>
                <span class="form-hint">Clinic hours: 08:00 AM - 06:00 PM</span>
            </div>

            <!-- Form Actions -->
            <div class="form-actions">
                <a href="${pageContext.request.contextPath}/appointments/view?id=${appointment.id}"
                   class="btn btn-secondary">← Cancel</a>
                <button type="submit" class="btn btn-primary" id="rescheduleBtn">
                    🔄 Confirm Reschedule
                </button>
            </div>

        </form>
    </div>

</div>

<script>
document.getElementById('rescheduleForm').addEventListener('submit', function() {
    var btn = document.getElementById('rescheduleBtn');
    btn.textContent = '⏳ Saving...';
    btn.disabled = true;
});
</script>

<%@ include file="../common/footer.jsp" %>
