<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%--
    register.jsp - Book New Appointment Form
    Location: /WEB-INF/views/appointment/register.jsp
--%>

<%@ include file="../common/header.jsp" %>
<%@ include file="../common/navbar.jsp" %>

<div class="section-card form-card">

    <div class="form-card-header">
        <h3>📅 Book New Appointment</h3>
        <p class="form-subtitle">
            Schedule a patient visit with a dentist
        </p>
    </div>

    <!-- Error Alert -->
    <c:if test="${not empty errorMsg}">
        <div class="alert alert-error">
            <span>❌</span>
            <span><c:out value="${errorMsg}"/></span>
        </div>
    </c:if>

    <form action="${pageContext.request.contextPath}/appointments/register"
          method="post"
          id="appointmentForm">

        <!-- ── PATIENT SELECTION ────────────────────────────────── -->
        <div class="form-section">
            <h4 class="form-section-title">👥 Patient Selection</h4>
            <div class="form-grid form-grid-1">
                <div class="form-group required">
                    <label for="patientId">Select Patient</label>
                    <select id="patientId"
                            name="patientId"
                            class="form-control"
                            required>
                        <option value="">-- Select Patient --</option>
                        <c:forEach var="p" items="${patients}">
                            <option value="${p.id}"
                                <c:if test="${p.id == selPatientId ||
                                             p.id == preselectedPatientId}">
                                    selected
                                </c:if>>
                                <c:out value="${p.patientCode}"/> -
                                <c:out value="${p.fullName}"/>
                                (<c:out value="${p.contact}"/>)
                            </option>
                        </c:forEach>
                    </select>
                    <span class="form-hint">
                        Can't find patient?
                        <a href="${pageContext.request.contextPath}/patients/add"
                           target="_blank">
                            Register new patient →
                        </a>
                    </span>
                </div>
            </div>
        </div>

        <!-- ── DENTIST & TREATMENT ─────────────────────────────── -->
        <div class="form-section">
            <h4 class="form-section-title">🩺 Dentist & Treatment</h4>
            <div class="form-grid form-grid-2">

                <!-- Dentist -->
                <div class="form-group required">
                    <label for="dentistId">Select Dentist</label>
                    <select id="dentistId"
                            name="dentistId"
                            class="form-control"
                            required
                            onchange="updateConsultFee(this)">
                        <option value="">-- Select Dentist --</option>
                        <c:forEach var="d" items="${dentists}">
                            <c:set var="busyUntil" value="${dentistAvailability[d.id]}"/>
                            <option value="${d.id}"
                                    data-fee="${d.consultFee}"
                                    data-spec="${d.specialization}"
                                <c:if test="${d.id == selDentistId}">selected</c:if>>
                                <c:choose>
                                    <c:when test="${not empty busyUntil}">🔴</c:when>
                                    <c:otherwise>🟢</c:otherwise>
                                </c:choose>
                                <c:out value="${d.fullName}"/>
                                <c:if test="${not empty d.specialization}">
                                    (<c:out value="${d.specialization}"/>)
                                </c:if>
                                - LKR <fmt:formatNumber
                                           value="${d.consultFee}"
                                           pattern="#,##0.00"/>
                                <c:if test="${not empty busyUntil}">
                                    (busy until <c:out value="${busyUntil}"/>)
                                </c:if>
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <!-- Treatment -->
                <div class="form-group required">
                    <label for="treatmentId">Treatment Type</label>
                    <select id="treatmentId"
                            name="treatmentId"
                            class="form-control"
                            required
                            onchange="updateTreatmentFee(this)">
                        <option value="">-- Select Treatment --</option>
                        <c:forEach var="t" items="${treatments}">
                            <option value="${t.id}"
                                    data-cost="${t.baseCost}"
                                    data-duration="${t.durationMins}"
                                <c:if test="${t.id == selTreatmentId}">selected</c:if>>
                                <c:out value="${t.name}"/>
                                - LKR <fmt:formatNumber
                                           value="${t.baseCost}"
                                           pattern="#,##0.00"/>
                                (<c:out value="${t.durationMins}"/> mins)
                            </option>
                        </c:forEach>
                    </select>
                </div>

            </div>
        </div>

        <!-- ── DATE & TIME ─────────────────────────────────────── -->
        <div class="form-section">
            <h4 class="form-section-title">🕐 Schedule</h4>
            <div class="form-grid form-grid-2">

                <div class="form-group required">
                    <label for="aptDate">Appointment Date</label>
                    <input type="date"
                           id="aptDate"
                           name="aptDate"
                           class="form-control"
                           required
                           min="<%= java.time.LocalDate.now() %>"
                           value="<c:out value='${selAptDate}'/>">
                </div>

                <div class="form-group required">
                    <label for="aptTime">Appointment Time</label>
                    <select id="aptTime"
                            name="aptTime"
                            class="form-control"
                            required>
                        <option value="">-- Select Time Slot --</option>
                        <c:forEach var="hour" begin="8" end="17">
                            <c:forEach var="min" items="${['00','30']}">
                                <c:set var="timeVal"
                                       value="${hour < 10 ? '0' : ''}${hour}:${min}"/>
                                <option value="${timeVal}"
                                    <c:if test="${selAptTime == timeVal}">selected</c:if>>
                                    ${timeVal}
                                    ${hour < 12 ? 'AM' : 'PM'}
                                </option>
                            </c:forEach>
                        </c:forEach>
                    </select>
                    <span class="form-hint">
                        Clinic hours: 08:00 AM - 06:00 PM
                    </span>
                </div>

            </div>
        </div>

        <!-- ── COST PREVIEW ────────────────────────────────────── -->
        <div class="form-section">
            <div class="cost-preview-box" id="costPreview">
                <h4 class="cost-title">💰 Estimated Cost</h4>
                <div class="cost-rows">
                    <div class="cost-row">
                        <span>Treatment Fee:</span>
                        <span id="treatmentFeeDisplay">LKR 0.00</span>
                    </div>
                    <div class="cost-row">
                        <span>Consultation Fee:</span>
                        <span id="consultFeeDisplay">LKR 0.00</span>
                    </div>
                    <div class="cost-row cost-total">
                        <span>Estimated Total:</span>
                        <span id="totalDisplay">LKR 0.00</span>
                    </div>
                </div>
                <p class="cost-note">
                    * Final amount calculated at billing stage
                </p>
            </div>
        </div>

        <!-- ── NOTES ───────────────────────────────────────────── -->
        <div class="form-section">
            <h4 class="form-section-title">📝 Notes</h4>
            <div class="form-group">
                <label for="notes">Clinical Notes (Optional)</label>
                <textarea id="notes"
                          name="notes"
                          class="form-control"
                          rows="3"
                          placeholder="Any additional notes for the appointment..."><c:out value='${selNotes}'/></textarea>
            </div>
        </div>

        <!-- ── FORM ACTIONS ────────────────────────────────────── -->
        <div class="form-actions">
            <a href="${pageContext.request.contextPath}/appointments"
               class="btn btn-secondary">← Cancel</a>
            <button type="submit"
                    class="btn btn-primary"
                    id="submitBtn">
                📅 Confirm Booking
            </button>
        </div>

    </form>
</div>

<script>
    let treatmentFee = 0;
    let consultFee   = 0;

    function updateTreatmentFee(select) {
        const opt = select.options[select.selectedIndex];
        treatmentFee = parseFloat(opt.getAttribute('data-cost')) || 0;
        updateCostPreview();
    }

    function updateConsultFee(select) {
        const opt = select.options[select.selectedIndex];
        consultFee = parseFloat(opt.getAttribute('data-fee')) || 0;
        updateCostPreview();
    }

    function updateCostPreview() {
        const fmt = (n) => 'LKR ' + n.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',');
        document.getElementById('treatmentFeeDisplay').textContent = fmt(treatmentFee);
        document.getElementById('consultFeeDisplay').textContent   = fmt(consultFee);
        document.getElementById('totalDisplay').textContent        = fmt(treatmentFee + consultFee);
    }

    document.getElementById('appointmentForm').addEventListener('submit', function(e) {
        const patient   = document.getElementById('patientId').value;
        const dentist   = document.getElementById('dentistId').value;
        const treatment = document.getElementById('treatmentId').value;
        const date      = document.getElementById('aptDate').value;
        const time      = document.getElementById('aptTime').value;

        if (!patient || !dentist || !treatment || !date || !time) {
            e.preventDefault();
            alert('Please fill in all required fields before booking.');
            return;
        }

        document.getElementById('submitBtn').textContent = '⏳ Booking...';
        document.getElementById('submitBtn').disabled = true;
    });
</script>

<%@ include file="../common/footer.jsp" %>