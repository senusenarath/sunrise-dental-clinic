<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%--
    update.jsp - Clinical Treatment Status Update Form
    Location: /WEB-INF/views/appointment/update.jsp
--%>

<%@ include file="../common/header.jsp" %>
<%@ include file="../common/navbar.jsp" %>

<div class="two-col-layout">

    <!-- ── APPOINTMENT DETAILS CARD ─────────────────────────────── -->
    <div class="section-card">
        <div class="section-header">
            <h3 class="section-title">📋 Appointment Details</h3>
        </div>

        <div class="detail-list">
            <div class="detail-item">
                <span class="detail-label">Apt Code</span>
                <span class="detail-value">
                    <span class="code-tag">
                        <c:out value="${appointment.aptCode}"/>
                    </span>
                </span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Patient</span>
                <span class="detail-value">
                    <strong>
                        <c:out value="${appointment.patientName}"/>
                    </strong>
                    <br>
                    <small class="text-muted">
                        <c:out value="${appointment.patientContact}"/>
                    </small>
                </span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Dentist</span>
                <span class="detail-value">
                    <c:out value="${appointment.dentistName}"/>
                </span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Treatment</span>
                <span class="detail-value">
                    <c:out value="${appointment.treatmentName}"/>
                </span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Date & Time</span>
                <span class="detail-value">
                    <c:out value="${appointment.aptDate}"/>
                    at
                    <strong><c:out value="${appointment.aptTime}"/></strong>
                </span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Current Status</span>
                <span class="detail-value">
                    <span class="badge ${appointment.statusBadgeClass}">
                        <c:out value="${appointment.statusIcon}"/>
                        <c:out value="${appointment.status}"/>
                    </span>
                </span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Treatment Cost</span>
                <span class="detail-value">
                    LKR <fmt:formatNumber
                             value="${appointment.treatmentCost}"
                             pattern="#,##0.00"/>
                </span>
            </div>
        </div>
    </div>

    <!-- ── UPDATE FORM ──────────────────────────────────────────── -->
    <div class="section-card form-card">
        <div class="form-card-header">
            <h3>⚕️ Update Treatment Status</h3>
            <p class="form-subtitle">
                Update clinical outcome and add consultation notes
            </p>
        </div>

        <c:if test="${not empty errorMsg}">
            <div class="alert alert-error">
                <span>❌</span>
                <span><c:out value="${errorMsg}"/></span>
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/appointments/update-treatment"
              method="post"
              id="updateForm">

            <input type="hidden"
                   name="appointmentId"
                   value="${appointment.id}">

            <!-- Status -->
            <div class="form-group required">
                <label for="status">Treatment Status</label>
                <select id="status"
                        name="status"
                        class="form-control"
                        required>
                    <option value="Scheduled"
                        <c:if test="${appointment.status == 'Scheduled'}">
                            selected
                        </c:if>>
                        📋 Scheduled
                    </option>
                    <option value="In Progress"
                        <c:if test="${appointment.status == 'In Progress'}">
                            selected
                        </c:if>>
                        ⚙️ In Progress
                    </option>
                    <option value="Completed"
                        <c:if test="${appointment.status == 'Completed'}">
                            selected
                        </c:if>>
                        ✅ Completed
                    </option>
                    <option value="Cancelled"
                        <c:if test="${appointment.status == 'Cancelled'}">
                            selected
                        </c:if>>
                        ❌ Cancelled
                    </option>
                </select>
            </div>

            <!-- Clinical Notes -->
            <div class="form-group">
                <label for="notes">Clinical Notes & Findings</label>
                <textarea id="notes"
                          name="notes"
                          class="form-control"
                          rows="6"
                          placeholder="Enter clinical findings, procedure details, follow-up instructions..."><c:out value="${appointment.notes}"/></textarea>
                <span class="form-hint">
                    Document treatment outcome, observations and next steps
                </span>
            </div>

            <!-- Form Actions -->
            <div class="form-actions">
                <a href="${pageContext.request.contextPath}/appointments"
                   class="btn btn-secondary">← Back</a>
                <button type="submit"
                        class="btn btn-primary"
                        id="updateBtn">
                    💾 Update Status
                </button>
            </div>

        </form>
    </div>

</div>

<%@ include file="../common/footer.jsp" %>