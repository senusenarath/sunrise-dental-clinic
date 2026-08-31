<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%--
    manage.jsp - Staff Management Page (Admin Only)
    Location: /WEB-INF/views/staff/manage.jsp
--%>

<%@ include file="../common/header.jsp" %>
<%@ include file="../common/navbar.jsp" %>

<!-- ═══════════════════════════════════════════════════════════════
     STAFF LIST VIEW
     ═══════════════════════════════════════════════════════════════ -->
<c:if test="${formMode == 'list'}">

    <!-- Stats Row -->
    <div class="metrics-grid">
        <div class="metric-card metric-blue">
            <div class="metric-icon">👤</div>
            <div class="metric-body">
                <div class="metric-value">${totalStaff}</div>
                <div class="metric-label">Active Staff</div>
            </div>
        </div>
        <div class="metric-card metric-teal">
            <div class="metric-icon">🩺</div>
            <div class="metric-body">
                <div class="metric-value">${totalDentists}</div>
                <div class="metric-label">Dentists</div>
            </div>
        </div>
        <div class="metric-card metric-green">
            <div class="metric-icon">🖥️</div>
            <div class="metric-body">
                <div class="metric-value">
                    ${staffList.size() - totalDentists}
                </div>
                <div class="metric-label">Admin & Reception</div>
            </div>
        </div>
    </div>

    <!-- Staff Table -->
    <div class="section-card">
        <div class="section-header">
            <h3 class="section-title">
                👤 Staff Directory
                <span class="badge badge-info">${staffList.size()}</span>
            </h3>
            <a href="${pageContext.request.contextPath}/staff/add"
               class="btn btn-primary">
                ➕ Add New Staff
            </a>
        </div>

        <c:choose>
            <c:when test="${empty staffList}">
                <div class="empty-state">
                    <div class="empty-icon">👤</div>
                    <p class="empty-title">No staff accounts found</p>
                    <a href="${pageContext.request.contextPath}/staff/add"
                       class="btn btn-primary">Add First Staff Member</a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="table-wrapper">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Code</th>
                                <th>Full Name</th>
                                <th>Username</th>
                                <th>Role</th>
                                <th>Specialization</th>
                                <th>Consult Fee</th>
                                <th>Contact</th>
                                <th>Status</th>
                                <th>Last Login</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="s" items="${staffList}" varStatus="loop">
                                <tr class="${!s.active ? 'row-inactive' : ''}">
                                    <td class="text-muted">
                                        <c:out value="${loop.count}"/>
                                    </td>
                                    <td>
                                        <span class="code-tag">
                                            <c:out value="${s.userCode}"/>
                                        </span>
                                    </td>
                                    <td>
                                        <strong>
                                            <c:out value="${s.fullName}"/>
                                        </strong>
                                        <c:if test="${!s.active}">
                                            <span class="badge badge-inactive">
                                                Inactive
                                            </span>
                                        </c:if>
                                    </td>
                                    <td>
                                        <c:out value="${s.username}"/>
                                    </td>
                                    <td>
                                        <span class="badge ${s.roleBadgeClass}">
                                            <c:out value="${s.roleDisplay}"/>
                                        </span>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty s.specialization}">
                                                <c:out value="${s.specialization}"/>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="text-muted">—</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${s.consultFee > 0}">
                                                LKR <fmt:formatNumber
                                                         value="${s.consultFee}"
                                                         pattern="#,##0.00"/>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="text-muted">—</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty s.contact}">
                                                <c:out value="${s.contact}"/>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="text-muted">—</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <span class="badge ${s.active ? 'badge-active' : 'badge-inactive'}">
                                            ${s.active ? '✅ Active' : '⛔ Inactive'}
                                        </span>
                                    </td>
                                    <td class="text-muted text-sm">
                                        <c:choose>
                                            <c:when test="${not empty s.lastLogin}">
                                                <c:out value="${s.lastLogin}"/>
                                            </c:when>
                                            <c:otherwise>Never</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <div class="action-btns">
                                            <a href="${pageContext.request.contextPath}/staff/edit?id=${s.id}"
                                               class="btn btn-xs btn-warning">
                                                ✏️ Edit
                                            </a>

                                            <!-- Toggle Status -->
                                            <c:if test="${s.id != loggedInUser.id}">
                                                <form action="${pageContext.request.contextPath}/staff/toggle"
                                                      method="post"
                                                      style="display:inline;"
                                                      onsubmit="return confirm('${s.active ? 'Deactivate' : 'Activate'} account for ${s.fullName}?')">
                                                    <input type="hidden"
                                                           name="userId"
                                                           value="${s.id}">
                                                    <button type="submit"
                                                            class="btn btn-xs ${s.active ? 'btn-danger' : 'btn-success'}">
                                                        ${s.active ? '⛔ Disable' : '✅ Enable'}
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
                    Showing <strong>${staffList.size()}</strong> staff member(s)
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- Role Permission Matrix -->
    <div class="section-card">
        <div class="section-header">
            <h3 class="section-title">🔐 Role Permission Matrix</h3>
        </div>
        <div class="table-wrapper">
            <table class="data-table permission-table">
                <thead>
                    <tr>
                        <th>Permission</th>
                        <th class="text-center">👑 Admin</th>
                        <th class="text-center">🖥️ Receptionist</th>
                        <th class="text-center">🩺 Dentist</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>View Dashboard</td>
                        <td class="text-center perm-yes">✅</td>
                        <td class="text-center perm-yes">✅</td>
                        <td class="text-center perm-yes">✅</td>
                    </tr>
                    <tr>
                        <td>Register Patients</td>
                        <td class="text-center perm-yes">✅</td>
                        <td class="text-center perm-yes">✅</td>
                        <td class="text-center perm-no">❌</td>
                    </tr>
                    <tr>
                        <td>Book Appointments</td>
                        <td class="text-center perm-yes">✅</td>
                        <td class="text-center perm-yes">✅</td>
                        <td class="text-center perm-no">❌</td>
                    </tr>
                    <tr>
                        <td>Update Treatment Status</td>
                        <td class="text-center perm-yes">✅</td>
                        <td class="text-center perm-no">❌</td>
                        <td class="text-center perm-yes">✅</td>
                    </tr>
                    <tr>
                        <td>Generate Invoices</td>
                        <td class="text-center perm-yes">✅</td>
                        <td class="text-center perm-yes">✅</td>
                        <td class="text-center perm-no">❌</td>
                    </tr>
                    <tr>
                        <td>View Reports & Analytics</td>
                        <td class="text-center perm-yes">✅</td>
                        <td class="text-center perm-no">❌</td>
                        <td class="text-center perm-no">❌</td>
                    </tr>
                    <tr>
                        <td>Manage Staff Accounts</td>
                        <td class="text-center perm-yes">✅</td>
                        <td class="text-center perm-no">❌</td>
                        <td class="text-center perm-no">❌</td>
                    </tr>
                    <tr>
                        <td>Cancel Bills</td>
                        <td class="text-center perm-yes">✅</td>
                        <td class="text-center perm-no">❌</td>
                        <td class="text-center perm-no">❌</td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>

</c:if>

<!-- ═══════════════════════════════════════════════════════════════
     ADD / EDIT STAFF FORM
     ═══════════════════════════════════════════════════════════════ -->
<c:if test="${formMode == 'add' || formMode == 'edit'}">

<div class="section-card form-card">
    <div class="form-card-header">
        <h3>
            <c:choose>
                <c:when test="${formMode == 'edit'}">
                    ✏️ Edit Staff Account
                </c:when>
                <c:otherwise>
                    ➕ Add New Staff Member
                </c:otherwise>
            </c:choose>
        </h3>
    </div>

    <c:if test="${not empty errorMsg}">
        <div class="alert alert-error">
            <span>❌</span>
            <span><c:out value="${errorMsg}"/></span>
        </div>
    </c:if>

    <form action="${pageContext.request.contextPath}/staff/${formMode}"
          method="post"
          id="staffForm">

        <c:if test="${formMode == 'edit'}">
            <input type="hidden" name="userId" value="${editUser.id}">
        </c:if>

        <div class="form-grid form-grid-2">

            <!-- Full Name -->
            <div class="form-group required">
                <label for="fullName">Full Name</label>
                <input type="text"
                       id="fullName"
                       name="fullName"
                       class="form-control"
                       required
                       placeholder="e.g. Dr. Priya Sharma"
                       value="<c:out value='${not empty editUser ? editUser.fullName : fFullName}'/>">
            </div>

            <!-- Username (add only) -->
            <c:if test="${formMode == 'add'}">
                <div class="form-group required">
                    <label for="username">Username</label>
                    <input type="text"
                           id="username"
                           name="username"
                           class="form-control"
                           required
                           placeholder="e.g. dr.sharma"
                           value="<c:out value='${fUsername}'/>">
                    <span class="form-hint">
                        Used for login. Cannot be changed later.
                    </span>
                </div>
            </c:if>

            <!-- Password (add only) -->
            <c:if test="${formMode == 'add'}">
                <div class="form-group required">
                    <label for="password">Password</label>
                    <input type="password"
                           id="password"
                           name="password"
                           class="form-control"
                           required
                           placeholder="Minimum 6 characters">
                </div>
            </c:if>

            <!-- Role -->
            <div class="form-group required">
                <label for="role">Role</label>
                <select id="role"
                        name="role"
                        class="form-control"
                        required
                        onchange="toggleDentistFields(this.value)">
                    <option value="">-- Select Role --</option>
                    <option value="ADMIN"
                        <c:if test="${editUser.role == 'ADMIN' || fRole == 'ADMIN'}">
                            selected
                        </c:if>>
                        👑 Administrator
                    </option>
                    <option value="RECEPTIONIST"
                        <c:if test="${editUser.role == 'RECEPTIONIST' || fRole == 'RECEPTIONIST'}">
                            selected
                        </c:if>>
                        🖥️ Receptionist
                    </option>
                    <option value="DENTIST"
                        <c:if test="${editUser.role == 'DENTIST' || fRole == 'DENTIST'}">
                            selected
                        </c:if>>
                        🩺 Dentist
                    </option>
                </select>
            </div>

            <!-- Email -->
            <div class="form-group">
                <label for="email">Email Address</label>
                <input type="email"
                       id="email"
                       name="email"
                       class="form-control"
                       placeholder="staff@sunrisedental.lk"
                       value="<c:out value='${not empty editUser ? editUser.email : fEmail}'/>">
            </div>

            <!-- Contact -->
            <div class="form-group">
                <label for="contact">Contact Number</label>
                <input type="tel"
                       id="contact"
                       name="contact"
                       class="form-control"
                       placeholder="07XXXXXXXX"
                       value="<c:out value='${not empty editUser ? editUser.contact : fContact}'/>">
            </div>

            <!-- Dentist Fields (shown when role = DENTIST) -->
            <div id="dentistFields"
                 class="form-col-full"
                 style="display: ${(editUser.role == 'DENTIST' || fRole == 'DENTIST') ? 'block' : 'none'}">
                <div class="form-grid form-grid-2">
                    <div class="form-group">
                        <label for="specialization">Specialization</label>
                        <input type="text"
                               id="specialization"
                               name="specialization"
                               class="form-control"
                               placeholder="e.g. Orthodontics, Periodontics"
                               value="<c:out value='${not empty editUser ? editUser.specialization : fSpecialization}'/>">
                    </div>
                    <div class="form-group">
                        <label for="consultFee">Consultation Fee (LKR)</label>
                        <input type="number"
                               id="consultFee"
                               name="consultFee"
                               class="form-control"
                               min="0"
                               step="0.01"
                               placeholder="1500.00"
                               value="<c:out value='${not empty editUser ? editUser.consultFee : (not empty fConsultFee ? fConsultFee : 1500.00)}'/>">
                    </div>
                </div>
            </div>

            <!-- Active Status (edit only) -->
            <c:if test="${formMode == 'edit'}">
                <div class="form-group">
                    <label for="isActive">Account Status</label>
                    <select id="isActive"
                            name="isActive"
                            class="form-control">
                        <option value="true"
                            <c:if test="${editUser.active}">selected</c:if>>
                            ✅ Active
                        </option>
                        <option value="false"
                            <c:if test="${!editUser.active}">selected</c:if>>
                            ⛔ Inactive
                        </option>
                    </select>
                </div>
            </c:if>

        </div>

        <!-- Form Actions -->
        <div class="form-actions">
            <a href="${pageContext.request.contextPath}/staff"
               class="btn btn-secondary">← Cancel</a>
            <button type="submit" class="btn btn-primary">
                <c:choose>
                    <c:when test="${formMode == 'edit'}">
                        💾 Update Staff
                    </c:when>
                    <c:otherwise>
                        ✅ Create Account
                    </c:otherwise>
                </c:choose>
            </button>
        </div>

    </form>

    <!-- Change Password Section (edit only) -->
    <c:if test="${formMode == 'edit'}">
        <div class="password-section">
            <h4 class="section-divider-title">🔑 Change Password</h4>
            <form action="${pageContext.request.contextPath}/staff/password"
                  method="post"
                  id="pwdForm">
                <input type="hidden" name="userId" value="${editUser.id}">
                <div class="form-grid form-grid-2">
                    <div class="form-group">
                        <label for="newPassword">New Password</label>
                        <input type="password"
                               id="newPassword"
                               name="newPassword"
                               class="form-control"
                               placeholder="Minimum 6 characters"
                               required>
                    </div>
                    <div class="form-group">
                        <label for="confirmPassword">Confirm Password</label>
                        <input type="password"
                               id="confirmPassword"
                               name="confirmPassword"
                               class="form-control"
                               placeholder="Re-enter new password"
                               required>
                    </div>
                </div>
                <button type="submit"
                        class="btn btn-warning"
                        onclick="return validatePasswords()">
                    🔑 Change Password
                </button>
            </form>
        </div>
    </c:if>

</div>
</c:if>

<script>
    function toggleDentistFields(role) {
        const fields = document.getElementById('dentistFields');
        if (fields) {
            fields.style.display = role === 'DENTIST' ? 'block' : 'none';
        }
    }

    function validatePasswords() {
        const p1 = document.getElementById('newPassword').value;
        const p2 = document.getElementById('confirmPassword').value;
        if (p1 !== p2) {
            alert('Passwords do not match. Please try again.');
            return false;
        }
        if (p1.length < 6) {
            alert('Password must be at least 6 characters.');
            return false;
        }
        return true;
    }
</script>

<%@ include file="../common/footer.jsp" %>