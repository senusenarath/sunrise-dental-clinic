<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%--
    detail.jsp - Patient Clinical Profile
    Location: /WEB-INF/views/patient/detail.jsp
--%>

<%@ include file="../common/header.jsp" %>
<%@ include file="../common/navbar.jsp" %>

<!-- ═══════════════════════════════════════════════════════════════
     PATIENT PROFILE HEADER
     ═══════════════════════════════════════════════════════════════ -->
<div class="profile-header-card">
    <div class="profile-avatar">
        <span class="avatar-icon">
            <c:out value="${patient.genderIcon}"/>
        </span>
    </div>
    <div class="profile-info">
        <h2 class="profile-name">
            <c:out value="${patient.fullName}"/>
        </h2>
        <div class="profile-meta">
            <span class="meta-item">
                🆔 <c:out value="${patient.patientCode}"/>
            </span>
            <span class="meta-sep">•</span>
            <span class="meta-item">
                📞 <c:out value="${patient.contact}"/>
            </span>
            <c:if test="${not empty patient.email}">
                <span class="meta-sep">•</span>
                <span class="meta-item">
                    📧 <c:out value="${patient.email}"/>
                </span>
            </c:if>
            <span class="meta-sep">•</span>
            <span class="meta-item">
                <span class="badge ${patient.bloodTypeBadgeClass}">
                    🩸 <c:out value="${patient.bloodType}"/>
                </span>
            </span>
        </div>
    </div>
    <div class="profile-actions">
        <c:if test="${loggedInUser.role != 'DENTIST'}">
            <a href="${pageContext.request.contextPath}/patients/edit?id=${patient.id}"
               class="btn btn-warning">
                ✏️ Edit Profile
            </a>
            <a href="${pageContext.request.contextPath}/appointments/register?patientId=${patient.id}"
               class="btn btn-primary">
                📅 Book Appointment
            </a>
        </c:if>
        <a href="${pageContext.request.contextPath}/patients"
           class="btn btn-secondary">
            ← Back to Directory
        </a>
    </div>
</div>

<!-- ═══════════════════════════════════════════════════════════════
     STAT CARDS ROW
     ═══════════════════════════════════════════════════════════════ -->
<div class="metrics-grid metrics-grid-4">
    <div class="metric-card metric-blue">
        <div class="metric-icon">📅</div>
        <div class="metric-body">
            <div class="metric-value">${patient.totalAppointments}</div>
            <div class="metric-label">Total Consultations</div>
        </div>
    </div>
    <div class="metric-card metric-green">
        <div class="metric-icon">✅</div>
        <div class="metric-body">
            <div class="metric-value">${patient.completedTreatments}</div>
            <div class="metric-label">Completed Treatments</div>
        </div>
    </div>
    <div class="metric-card metric-teal">
        <div class="metric-icon">⏳</div>
        <div class="metric-body">
            <div class="metric-value">${patient.activeBookings}</div>
            <div class="metric-label">Active Bookings</div>
        </div>
    </div>
    <div class="metric-card metric-gold">
        <div class="metric-icon">💰</div>
        <div class="metric-body">
            <div class="metric-value">${bills.size()}</div>
            <div class="metric-label">Total Invoices</div>
        </div>
    </div>
</div>

<!-- ═══════════════════════════════════════════════════════════════
     PATIENT DETAILS + MEDICAL INFO
     ═══════════════════════════════════════════════════════════════ -->
<div class="two-col-layout">

    <!-- Personal Details -->
    <div class="section-card">
        <div class="section-header">
            <h3 class="section-title">👤 Personal Details</h3>
        </div>
        <div class="detail-list">
            <div class="detail-item">
                <span class="detail-label">Patient Code</span>
                <span class="detail-value">
                    <span class="code-tag">
                        <c:out value="${patient.patientCode}"/>
                    </span>
                </span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Full Name</span>
                <span class="detail-value">
                    <strong><c:out value="${patient.fullName}"/></strong>
                </span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Gender</span>
                <span class="detail-value">
                    <c:out value="${patient.genderIcon}"/>
                    <c:out value="${patient.gender}"/>
                </span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Date of Birth</span>
                <span class="detail-value">
                    <c:choose>
                        <c:when test="${not empty patient.dateOfBirth}">
                            <c:out value="${patient.dateOfBirth}"/>
                            (<c:out value="${patient.age}"/> years old)
                        </c:when>
                        <c:otherwise>
                            <span class="text-muted">Not provided</span>
                        </c:otherwise>
                    </c:choose>
                </span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Blood Type</span>
                <span class="detail-value">
                    <span class="badge ${patient.bloodTypeBadgeClass}">
                        <c:out value="${patient.bloodType}"/>
                    </span>
                </span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Contact</span>
                <span class="detail-value">
                    <c:out value="${patient.contact}"/>
                </span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Email</span>
                <span class="detail-value">
                    <c:choose>
                        <c:when test="${not empty patient.email}">
                            <c:out value="${patient.email}"/>
                        </c:when>
                        <c:otherwise>
                            <span class="text-muted">Not provided</span>
                        </c:otherwise>
                    </c:choose>
                </span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Address</span>
                <span class="detail-value">
                    <c:choose>
                        <c:when test="${not empty patient.address}">
                            <c:out value="${patient.address}"/>
                        </c:when>
                        <c:otherwise>
                            <span class="text-muted">Not provided</span>
                        </c:otherwise>
                    </c:choose>
                </span>
            </div>
            <div class="detail-item">
                <span class="detail-label">Registered</span>
                <span class="detail-value text-muted text-sm">
                    <c:out value="${patient.createdAt}"/>
                </span>
            </div>
        </div>
    </div>

    <!-- Medical Information -->
    <div class="section-card">
        <div class="section-header">
            <h3 class="section-title">🏥 Medical Information</h3>
        </div>

        <!-- Allergies -->
        <div class="medical-section">
            <h4 class="medical-label">
                ⚠️ Known Allergies
            </h4>
            <c:choose>
                <c:when test="${patient.hasAllergies()}">
                    <div class="allergy-box">
                        <c:out value="${patient.allergies}"/>
                    </div>
                </c:when>
                <c:otherwise>
                    <p class="text-muted">No known allergies recorded.</p>
                </c:otherwise>
            </c:choose>
        </div>

        <!-- Medical Notes -->
        <div class="medical-section">
            <h4 class="medical-label">
                📋 Clinical Notes
            </h4>
            <c:choose>
                <c:when test="${patient.hasMedicalNotes()}">
                    <div class="notes-box">
                        <c:out value="${patient.medicalNotes}"/>
                    </div>
                </c:when>
                <c:otherwise>
                    <p class="text-muted">No clinical notes recorded.</p>
                </c:otherwise>
            </c:choose>
        </div>

        <!-- Delete (Admin only) -->
        <c:if test="${loggedInUser.role == 'ADMIN'}">
            <div class="danger-zone">
                <h4 class="danger-title">⚠️ Danger Zone</h4>
                <p class="danger-text">
                    Removing a patient will hide them from the active directory.
                    This action can be reversed by the database administrator.
                </p>
                <form action="${pageContext.request.contextPath}/patients/delete"
                      method="post"
                      onsubmit="return confirm('Are you sure you want to remove this patient from active records?')">
                    <input type="hidden" name="patientId" value="${patient.id}">
                    <button type="submit" class="btn btn-danger">
                        🗑️ Remove Patient
                    </button>
                </form>
            </div>
        </c:if>
    </div>

</div>

<!-- ═══════════════════════════════════════════════════════════════
     APPOINTMENT HISTORY
     ═══════════════════════════════════════════════════════════════ -->
<div class="section-card">
    <div class="section-header">
        <h3 class="section-title">
            📅 Appointment History
            <span class="badge badge-info">${patient.totalAppointments}</span>
        </h3>
        <c:if test="${loggedInUser.role != 'DENTIST'}">
            <a href="${pageContext.request.contextPath}/appointments/register?patientId=${patient.id}"
               class="btn btn-primary btn-sm">
                ➕ Book New
            </a>
        </c:if>
    </div>

    <c:choose>
        <c:when test="${empty appointments}">
            <div class="empty-state">
                <div class="empty-icon">📭</div>
                <p class="empty-title">No appointments found</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="table-wrapper">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Code</th>
                            <th>Date</th>
                            <th>Time</th>
                            <th>Dentist</th>
                            <th>Treatment</th>
                            <th>Status</th>
                            <th>Notes</th>
                            <th>Bill</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="apt" items="${appointments}">
                            <tr>
                                <td>
                                    <span class="code-tag">
                                        <c:out value="${apt.aptCode}"/>
                                    </span>
                                </td>
                                <td><c:out value="${apt.aptDate}"/></td>
                                <td><c:out value="${apt.aptTime}"/></td>
                                <td><c:out value="${apt.dentistName}"/></td>
                                <td><c:out value="${apt.treatmentName}"/></td>
                                <td>
                                    <span class="badge ${apt.statusBadgeClass}">
                                        <c:out value="${apt.statusIcon}"/>
                                        <c:out value="${apt.status}"/>
                                    </span>
                                </td>
                                <td class="text-sm text-muted">
                                    <c:choose>
                                        <c:when test="${not empty apt.notes}">
                                            <c:out value="${apt.notes}"/>
                                        </c:when>
                                        <c:otherwise>—</c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${apt.hasBill}">
                                            <span class="badge badge-paid">
                                                ✅ Billed
                                            </span>
                                        </c:when>
                                        <c:when test="${apt.status == 'Completed' && loggedInUser.role != 'DENTIST'}">
                                            <a href="${pageContext.request.contextPath}/bills/create?aptId=${apt.id}"
                                               class="btn btn-xs btn-success">
                                                🧾 Bill Now
                                            </a>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-muted">—</span>
                                        </c:otherwise>
                                    </c:choose>
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