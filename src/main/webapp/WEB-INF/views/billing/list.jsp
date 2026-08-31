<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%--
    list.jsp - Billing & Invoice Directory
    Location: /WEB-INF/views/billing/list.jsp
--%>

<%@ include file="../common/header.jsp" %>
<%@ include file="../common/navbar.jsp" %>

<!-- ═══════════════════════════════════════════════════════════════
     FINANCIAL METRICS
     ═══════════════════════════════════════════════════════════════ -->
<div class="metrics-grid">
    <div class="metric-card metric-green">
        <div class="metric-icon">💰</div>
        <div class="metric-body">
            <div class="metric-value">
                LKR <fmt:formatNumber value="${totalRevenue}" pattern="#,##0"/>
            </div>
            <div class="metric-label">Total Revenue</div>
        </div>
    </div>
    <div class="metric-card metric-teal">
        <div class="metric-icon">📅</div>
        <div class="metric-body">
            <div class="metric-value">
                LKR <fmt:formatNumber value="${monthlyRevenue}" pattern="#,##0"/>
            </div>
            <div class="metric-label">This Month</div>
        </div>
    </div>
    <div class="metric-card metric-gold">
        <div class="metric-icon">⏳</div>
        <div class="metric-body">
            <div class="metric-value">
                LKR <fmt:formatNumber value="${pendingAmount}" pattern="#,##0"/>
            </div>
            <div class="metric-label">Pending Amount</div>
            <div class="metric-sub">${pendingCount} unpaid bills</div>
        </div>
    </div>
    <div class="metric-card metric-blue">
        <div class="metric-icon">🧾</div>
        <div class="metric-body">
            <div class="metric-value">${bills.size()}</div>
            <div class="metric-label">Showing Bills</div>
        </div>
    </div>
</div>

<!-- ═══════════════════════════════════════════════════════════════
     FILTER TABS
     ═══════════════════════════════════════════════════════════════ -->
<div class="section-card">
    <div class="filter-tabs">
        <a href="${pageContext.request.contextPath}/bills"
           class="filter-tab ${empty statusFilter ? 'active' : ''}">
            All Bills
        </a>
        <a href="${pageContext.request.contextPath}/bills?status=Pending"
           class="filter-tab ${statusFilter == 'Pending' ? 'active' : ''}">
            ⏳ Pending
        </a>
        <a href="${pageContext.request.contextPath}/bills?status=Paid"
           class="filter-tab ${statusFilter == 'Paid' ? 'active' : ''}">
            ✅ Paid
        </a>
        <a href="${pageContext.request.contextPath}/bills?status=Cancelled"
           class="filter-tab ${statusFilter == 'Cancelled' ? 'active' : ''}">
            ❌ Cancelled
        </a>
    </div>
</div>

<!-- ═══════════════════════════════════════════════════════════════
     BILLS TABLE
     ═══════════════════════════════════════════════════════════════ -->
<div class="section-card">
    <div class="section-header">
        <h3 class="section-title">
            🧾 Invoice Directory
            <span class="badge badge-info">${bills.size()}</span>
        </h3>
        <div class="action-btns">
            <a href="${pageContext.request.contextPath}/bills?export=csv<c:if test="${not empty statusFilter}">&status=${statusFilter}</c:if>"
               class="btn btn-secondary">
                📥 Download CSV
            </a>
            <a href="${pageContext.request.contextPath}/bills/create"
               class="btn btn-primary">
                ➕ Generate Invoice
            </a>
        </div>
    </div>

    <c:choose>
        <c:when test="${empty bills}">
            <div class="empty-state">
                <div class="empty-icon">🧾</div>
                <p class="empty-title">No bills found</p>
                <p class="empty-sub">
                    Generate an invoice for a completed appointment.
                </p>
                <a href="${pageContext.request.contextPath}/bills/create"
                   class="btn btn-primary">
                    Generate Invoice
                </a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="table-wrapper">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Bill Code</th>
                            <th>Patient</th>
                            <th>Treatment</th>
                            <th>Apt Date</th>
                            <th>Treatment Fee</th>
                            <th>Consult Fee</th>
                            <th>Discount</th>
                            <th>Total</th>
                            <th>Payment</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="bill" items="${bills}">
                            <tr>
                                <td>
                                    <a href="${pageContext.request.contextPath}/bills/view?id=${bill.id}"
                                       class="code-tag">
                                        <c:out value="${bill.billCode}"/>
                                    </a>
                                </td>
                                <td>
                                    <strong>
                                        <c:out value="${bill.patientName}"/>
                                    </strong>
                                    <br>
                                    <small class="text-muted">
                                        <c:out value="${bill.patientCode}"/>
                                    </small>
                                </td>
                                <td>
                                    <c:out value="${bill.treatmentName}"/>
                                </td>
                                <td>
                                    <c:out value="${bill.aptDate}"/>
                                </td>
                                <td>
                                    LKR <fmt:formatNumber
                                             value="${bill.treatmentFee}"
                                             pattern="#,##0.00"/>
                                </td>
                                <td>
                                    LKR <fmt:formatNumber
                                             value="${bill.consultFee}"
                                             pattern="#,##0.00"/>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${bill.discount > 0}">
                                            <span class="text-success">
                                                -LKR <fmt:formatNumber
                                                          value="${bill.discount}"
                                                          pattern="#,##0.00"/>
                                            </span>
                                        </c:when>
                                        <c:otherwise>—</c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <strong>
                                        LKR <fmt:formatNumber
                                                 value="${bill.totalAmount}"
                                                 pattern="#,##0.00"/>
                                    </strong>
                                </td>
                                <td>
                                    <c:out value="${bill.paymentIcon}"/>
                                    <c:out value="${bill.paymentMethod}"/>
                                </td>
                                <td>
                                    <span class="badge ${bill.statusBadgeClass}">
                                        <c:out value="${bill.statusIcon}"/>
                                        <c:out value="${bill.status}"/>
                                    </span>
                                </td>
                                <td>
                                    <div class="action-btns">
                                        <a href="${pageContext.request.contextPath}/bills/view?id=${bill.id}"
                                           class="btn btn-xs btn-info">
                                            👁 View
                                        </a>
                                        <c:if test="${bill.isPending() && loggedInUser.role == 'ADMIN'}">
                                            <form action="${pageContext.request.contextPath}/bills/cancel"
                                                  method="post"
                                                  style="display:inline;"
                                                  onsubmit="return confirm('Cancel bill ${bill.billCode}?')">
                                                <input type="hidden"
                                                       name="billId"
                                                       value="${bill.id}">
                                                <button type="submit"
                                                        class="btn btn-xs btn-danger">
                                                    ❌
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
                Showing <strong>${bills.size()}</strong> invoice(s)
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="../common/footer.jsp" %>