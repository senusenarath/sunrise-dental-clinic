<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
    form.jsp - Patient Registration / Edit Form
    Location: /WEB-INF/views/patient/form.jsp
--%>

<%@ include file="../common/header.jsp" %>
<%@ include file="../common/navbar.jsp" %>

<div class="section-card form-card">

    <div class="form-card-header">
        <h3>
            <c:choose>
                <c:when test="${formMode == 'edit'}">
                    ✏️ Edit Patient Record
                </c:when>
                <c:otherwise>
                    ➕ Register New Patient
                </c:otherwise>
            </c:choose>
        </h3>
        <p class="form-subtitle">
            <c:choose>
                <c:when test="${formMode == 'edit'}">
                    Update patient profile for
                    <strong><c:out value="${patient.fullName}"/></strong>
                </c:when>
                <c:otherwise>
                    Complete all required fields to register a new patient
                </c:otherwise>
            </c:choose>
        </p>
    </div>

    <!-- Error Alert -->
    <c:if test="${not empty errorMsg}">
        <div class="alert alert-error">
            <span>❌</span>
            <span><c:out value="${errorMsg}"/></span>
        </div>
    </c:if>

    <!-- Patient Form -->
    <form action="${pageContext.request.contextPath}/patients/${formMode}"
          method="post"
          id="patientForm"
          novalidate>

        <!-- Hidden ID for edit mode -->
        <c:if test="${formMode == 'edit'}">
            <input type="hidden" name="patientId" value="${patient.id}">
        </c:if>

        <!-- ── PERSONAL INFORMATION ─────────────────────────────── -->
        <div class="form-section">
            <h4 class="form-section-title">👤 Personal Information</h4>
            <div class="form-grid form-grid-2">

                <!-- Full Name -->
                <div class="form-group required">
                    <label for="fullName">Full Name</label>
                    <input type="text"
                           id="fullName"
                           name="fullName"
                           class="form-control"
                           placeholder="e.g. Nimal Perera"
                           maxlength="100"
                           required
                           value="<c:out value='${not empty patient ? patient.fullName : fullName}'/>">
                    <span class="form-hint">Enter patient's full legal name</span>
                </div>

                <!-- Date of Birth -->
                <div class="form-group">
                    <label for="dateOfBirth">Date of Birth</label>
                    <input type="date"
                           id="dateOfBirth"
                           name="dateOfBirth"
                           class="form-control"
                           max="<%= java.time.LocalDate.now() %>"
                           value="<c:out value='${not empty patient.dateOfBirth ? patient.dateOfBirth : dateOfBirth}'/>">
                </div>

                <!-- Gender -->
                <div class="form-group required">
                    <label for="gender">Gender</label>
                    <select id="gender" name="gender" class="form-control" required>
                        <option value="">-- Select Gender --</option>
                        <option value="Male"
                            <c:if test="${patient.gender == 'Male' || gender == 'Male'}">selected</c:if>>
                            ♂ Male
                        </option>
                        <option value="Female"
                            <c:if test="${patient.gender == 'Female' || gender == 'Female'}">selected</c:if>>
                            ♀ Female
                        </option>
                        <option value="Other"
                            <c:if test="${patient.gender == 'Other' || gender == 'Other'}">selected</c:if>>
                            ⚥ Other
                        </option>
                    </select>
                </div>

                <!-- Blood Type -->
                <div class="form-group">
                    <label for="bloodType">Blood Type</label>
                    <select id="bloodType" name="bloodType" class="form-control">
                        <option value="Unknown">Unknown</option>
                        <c:forEach var="bt" items="${['A+','A-','B+','B-','AB+','AB-','O+','O-']}">
                            <option value="${bt}"
                                <c:if test="${patient.bloodType == bt || bloodType == bt}">selected</c:if>>
                                ${bt}
                            </option>
                        </c:forEach>
                    </select>
                </div>

            </div>
        </div>

        <!-- ── CONTACT INFORMATION ─────────────────────────────── -->
        <div class="form-section">
            <h4 class="form-section-title">📞 Contact Information</h4>
            <div class="form-grid form-grid-2">

                <!-- Contact Number -->
                <div class="form-group required">
                    <label for="contact">Contact Number</label>
                    <input type="tel"
                           id="contact"
                           name="contact"
                           class="form-control"
                           placeholder="e.g. 0771234567"
                           pattern="^(\+94|0)[0-9]{9}$"
                           required
                           maxlength="15"
                           value="<c:out value='${not empty patient ? patient.contact : contact}'/>">
                    <span class="form-hint">Sri Lankan format: 07XXXXXXXX</span>
                </div>

                <!-- Email -->
                <div class="form-group">
                    <label for="email">Email Address</label>
                    <input type="email"
                           id="email"
                           name="email"
                           class="form-control"
                           placeholder="e.g. patient@email.com"
                           maxlength="100"
                           value="<c:out value='${not empty patient ? patient.email : email}'/>">
                    <span class="form-hint">Optional</span>
                </div>

                <!-- Address -->
                <div class="form-group form-col-full">
                    <label for="address">Home Address</label>
                    <input type="text"
                           id="address"
                           name="address"
                           class="form-control"
                           placeholder="e.g. 12 Galle Road, Colombo 03"
                           maxlength="255"
                           value="<c:out value='${not empty patient ? patient.address : address}'/>">
                </div>

            </div>
        </div>

        <!-- ── MEDICAL INFORMATION ─────────────────────────────── -->
        <div class="form-section">
            <h4 class="form-section-title">🏥 Medical Information</h4>
            <div class="form-grid form-grid-1">

                <!-- Allergies -->
                <div class="form-group">
                    <label for="allergies">Known Allergies</label>
                    <textarea id="allergies"
                              name="allergies"
                              class="form-control"
                              rows="2"
                              placeholder="e.g. Penicillin, Latex (leave blank if none)"><c:out value='${not empty patient ? patient.allergies : allergies}'/></textarea>
                </div>

                <!-- Medical Notes -->
                <div class="form-group">
                    <label for="medicalNotes">Medical Notes</label>
                    <textarea id="medicalNotes"
                              name="medicalNotes"
                              class="form-control"
                              rows="3"
                              placeholder="e.g. Diabetic patient, high blood pressure (optional)"><c:out value='${not empty patient ? patient.medicalNotes : medicalNotes}'/></textarea>
                </div>

            </div>
        </div>

        <!-- ── FORM ACTIONS ────────────────────────────────────── -->
        <div class="form-actions">
            <a href="${pageContext.request.contextPath}/patients"
               class="btn btn-secondary">
                ← Cancel
            </a>
            <button type="reset" class="btn btn-ghost">
                🔄 Reset Form
            </button>
            <button type="submit" class="btn btn-primary" id="submitBtn">
                <c:choose>
                    <c:when test="${formMode == 'edit'}">
                        💾 Update Patient
                    </c:when>
                    <c:otherwise>
                        ✅ Register Patient
                    </c:otherwise>
                </c:choose>
            </button>
        </div>

    </form>
</div>

<script>
/**
 * Patient form validation
 */
document.getElementById('patientForm').addEventListener('submit', function(e) {
    const fullName = document.getElementById('fullName').value.trim();
    const contact  = document.getElementById('contact').value.trim();
    const gender   = document.getElementById('gender').value;

    if (!fullName) {
        e.preventDefault();
        alert('Please enter the patient full name.');
        document.getElementById('fullName').focus();
        return;
    }

    if (!contact) {
        e.preventDefault();
        alert('Please enter a contact number.');
        document.getElementById('contact').focus();
        return;
    }

    const phoneRegex = /^(\+94|0)[0-9]{9}$/;
    if (!phoneRegex.test(contact)) {
        e.preventDefault();
        alert('Please enter a valid Sri Lankan contact number (e.g. 0771234567).');
        document.getElementById('contact').focus();
        return;
    }

    if (!gender) {
        e.preventDefault();
        alert('Please select a gender.');
        document.getElementById('gender').focus();
        return;
    }

    // Show loading state
    document.getElementById('submitBtn').textContent = '⏳ Saving...';
    document.getElementById('submitBtn').disabled = true;
});
</script>

<%@ include file="../common/footer.jsp" %>