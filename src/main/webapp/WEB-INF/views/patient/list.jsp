<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%--
    list.jsp - Patient Directory
    Location: /WEB-INF/views/patient/list.jsp
--%>

<%@ include file="../common/header.jsp" %>
<%@ include file="../common/navbar.jsp" %>

<!-- ═══════════════════════════════════════════════════════════════
     SEARCH BAR
     ═══════════════════════════════════════════════════════════════ -->
<div class="section-card">
    <form action="${pageContext.request.contextPath}/patients"
          method="get"
          class="search-form">
        <div class="search-bar">
            <input type="text"
                   name="search"
                   class="search-input"
                   placeholder="🔍 Search by patient name, contact, email or patient code..."
                   value="<c:out value='${searchKeyword}'/>">
            <button type="submit" class="btn btn-primary">Search</button>
            <c:if test="${not empty searchKeyword}">
                <a href="${pageContext.request.contextPath}/patients"
                   class="btn btn-secondary">Clear</a>
            </c:if>
        </div>
    </form>

    <!-- Search Result Info -->
    <c:if test="${not empty searchKeyword}">
        <div class="search-result-info">
            <span>
                Found <strong>${searchCount}</strong> result(s)
                for "<strong><c:out value="${searchKeyword}"/></strong>"
            </span>
        </div>
    </c:if>
</div>

<!-- ═══════════════════════════════════════════════════════════════
     STATS ROW
     ═══════════════════════════════════════════════════════════════ -->
<div class="stats-row">
    <div class="stat-pill stat-blue">
        👥 Total Patients: <strong>${totalPatients}</strong>
    </div>
    <c:if test="${not empty searchKeyword}">
        <div class="stat-pill stat-teal">
            🔍 Search Results: <strong>${searchCount}</strong>
        </div>
    </c:if>
</div>

<!-- ═══════════════════════════════════════════════════════════════
     PATIENT TABLE
     ═══════════════════════════════════════════════════════════════ -->
<div class="section-card">
    <div class="section-header">
        <h3 class="section-title">
            👥 Patient Directory
        </h3>
        <div class="action-btns">
            <a href="${pageContext.request.contextPath}/patients?export=csv<c:if test="${not empty searchKeyword}">&search=${searchKeyword}</c:if>"
               class="btn btn-secondary">
                📥 Download CSV
            </a>
            <c:if test="${loggedInUser.role != 'DENTIST'}">
                <a href="${pageContext.request.contextPath}/patients/add"
                   class="btn btn-primary">
                    ➕ Register New Patient
                </a>
            </c:if>
        </div>
    </div>

    <c:choose>
        <c:when test="${empty patients}">
            <div class="empty-state">
                <div class="empty-icon">👥</div>
                <p class="empty-title">No patients found</p>
                <c:choose>
                    <c:when test="${not empty searchKeyword}">
                        <p class="empty-sub">
                            No results for "<c:out value="${searchKeyword}"/>".
                            Try a different search term.
                        </p>
                        <a href="${pageContext.request.contextPath}/patients"
                           class="btn btn-secondary">View All Patients</a>
                    </c:when>
                    <c:otherwise>
                        <p class="empty-sub">
                            No patients registered yet. Register your first patient!
                        </p>
                        <c:if test="${loggedInUser.role != 'DENTIST'}">
                            <a href="${pageContext.request.contextPath}/patients/add"
                               class="btn btn-primary">Register Patient</a>
                        </c:if>
                    </c:otherwise>
                </c:choose>
            </div>
        </c:when>
        <c:otherwise>
            <div class="table-wrapper">
                <table class="data-table" id="patientTable">
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Patient Code</th>
                            <th>Full Name</th>
                            <th>Contact</th>
                            <th>Gender</th>
                            <th>Age</th>
                            <th>Blood Type</th>
                            <th>Registered</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="p" items="${patients}" varStatus="loop">
                            <tr>
                                <td class="text-muted">
                                    <c:out value="${loop.count}"/>
                                </td>
                                <td>
                                    <span class="code-tag">
                                        <c:out value="${p.patientCode}"/>
                                    </span>
                                </td>
                                <td>
                                    <a href="${pageContext.request.contextPath}/patients/view?id=${p.id}"
                                       class="patient-link">
                                        <strong><c:out value="${p.fullName}"/></strong>
                                    </a>
                                </td>
                                <td><c:out value="${p.contact}"/></td>
                                <td>
                                    <c:out value="${p.genderIcon}"/>
                                    <c:out value="${p.gender}"/>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${p.age > 0}">
                                            <c:out value="${p.age}"/> yrs
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-muted">N/A</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <span class="badge ${p.bloodTypeBadgeClass}">
                                        <c:out value="${p.bloodType}"/>
                                    </span>
                                </td>
                                <td class="text-muted text-sm">
                                    <c:out value="${p.createdAt}"/>
                                </td>
                                <td>
                                    <div class="action-btns">
                                        <a href="${pageContext.request.contextPath}/patients/view?id=${p.id}"
                                           class="btn btn-xs btn-info"
                                           title="View Profile">
                                            👁 View
                                        </a>
                                        <c:if test="${loggedInUser.role != 'DENTIST'}">
                                            <a href="${pageContext.request.contextPath}/patients/edit?id=${p.id}"
                                               class="btn btn-xs btn-warning"
                                               title="Edit Patient">
                                                ✏️ Edit
                                            </a>
                                        </c:if>
                                        <c:if test="${loggedInUser.role != 'DENTIST'}">
                                            <a href="${pageContext.request.contextPath}/appointments/register?patientId=${p.id}"
                                               class="btn btn-xs btn-primary"
                                               title="Book Appointment">
                                                📅 Book
                                            </a>
                                        </c:if>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>

            <!-- Table Footer Info -->
            <div class="table-footer">
                Showing <strong>${patients.size()}</strong> patient(s)
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="../common/footer.jsp" %>