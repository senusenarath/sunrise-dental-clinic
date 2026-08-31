<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%--
    invoice.jsp - Invoice Generation & Printable Receipt
    Location: /WEB-INF/views/billing/invoice.jsp
--%>

<%@ include file="../common/header.jsp" %>
<%@ include file="../common/navbar.jsp" %>

<!-- ═══════════════════════════════════════════════════════════════
     INVOICE GENERATION FORM (shown when no bill loaded)
     ═══════════════════════════════════════════════════════════════ -->
<c:if test="${empty bill}">
<div class="section-card form-card">
    <div class="form-card-header">
        <h3>🧾 Generate New Invoice</h3>
        <p class="form-subtitle">
            Create an invoice for a completed appointment
        </p>
    </div>

    <c:if test="${not empty errorMsg}">
        <div class="alert alert-error">
            <span>❌</span>
            <span><c:out value="${errorMsg}"/></span>
        </div>
    </c:if>

    <form action="${pageContext.request.contextPath}/bills/create"
          method="post"
          id="billForm">

        <!-- Appointment Selection -->
        <div class="form-section">
            <h4 class="form-section-title">📅 Select Appointment</h4>
            <div class="form-group required">
                <label for="appointmentId">Completed Appointment</label>
                <select id="appointmentId"
                        name="appointmentId"
                        class="form-control"
                        required
                        onchange="loadAppointmentCost(this)">
                    <option value="">-- Select Appointment --</option>
                    <c:forEach var="apt" items="${unbilledAppointments}">
                        <c:if test="${!apt.hasBill}">
                            <option value="${apt.id}"
                                    data-treatment="${apt.treatmentCost}"
                                    data-consult="${apt.consultFee}"
                                <c:if test="${apt.id == preselectedApt.id}">
                                    selected
                                </c:if>>
                                <c:out value="${apt.aptCode}"/> -
                                <c:out value="${apt.patientName}"/> |
                                <c:out value="${apt.treatmentName}"/> |
                                <c:out value="${apt.aptDate}"/>
                            </option>
                        </c:if>
                    </c:forEach>
                </select>
            </div>
        </div>

        <!-- Cost Preview -->
        <div class="form-section">
            <div class="cost-preview-box">
                <h4 class="cost-title">💰 Cost Breakdown</h4>
                <div class="cost-rows">
                    <div class="cost-row">
                        <span>Treatment Fee:</span>
                        <span id="tFee">LKR 0.00</span>
                    </div>
                    <div class="cost-row">
                        <span>Consultation Fee:</span>
                        <span id="cFee">LKR 0.00</span>
                    </div>
                    <div class="cost-row">
                        <span>Discount:</span>
                        <span id="discDisplay" class="text-success">-LKR 0.00</span>
                    </div>
                    <div class="cost-row cost-total">
                        <span>Total Amount:</span>
                        <span id="totalAmt">LKR 0.00</span>
                    </div>
                </div>
            </div>
        </div>

        <!-- Payment Details -->
        <div class="form-section">
            <h4 class="form-section-title">💳 Payment Details</h4>
            <div class="form-grid form-grid-2">

                <div class="form-group required">
                    <label for="paymentMethod">Payment Method</label>
                    <select id="paymentMethod"
                            name="paymentMethod"
                            class="form-control"
                            required>
                        <option value="">-- Select Method --</option>
                        <option value="Cash">💵 Cash</option>
                        <option value="Card">💳 Card</option>
                        <option value="Online Transfer">🌐 Online Transfer</option>
                        <option value="QR Payment">📱 QR Payment</option>
                    </select>
                </div>

                <div class="form-group">
                    <label for="discount">Discount Amount (LKR)</label>
                    <input type="number"
                           id="discount"
                           name="discount"
                           class="form-control"
                           min="0"
                           step="0.01"
                           value="0"
                           placeholder="0.00"
                           oninput="recalcTotal()">
                </div>

            </div>
        </div>

        <!-- Form Actions -->
        <div class="form-actions">
            <a href="${pageContext.request.contextPath}/bills"
               class="btn btn-secondary">← Cancel</a>
            <button type="submit"
                    class="btn btn-primary"
                    id="genBtn">
                🧾 Generate Invoice
            </button>
        </div>

    </form>
</div>
</c:if>

<!-- ═══════════════════════════════════════════════════════════════
     PRINTABLE INVOICE VIEW (shown when bill is loaded)
     ═══════════════════════════════════════════════════════════════ -->
<c:if test="${not empty bill}">

<!-- Screen Action Buttons -->
<div class="invoice-actions no-print">
    <a href="${pageContext.request.contextPath}/bills"
       class="btn btn-secondary">← Back to Bills</a>

    <c:if test="${bill.isPending()}">
        <!-- Settle Form -->
        <form action="${pageContext.request.contextPath}/bills/settle"
              method="post"
              style="display:inline;"
              id="settleForm">
            <input type="hidden" name="billId" value="${bill.id}">
            <select name="paymentMethod" class="form-control-inline" required>
                <option value="">Select Payment Method</option>
                <option value="Cash">💵 Cash</option>
                <option value="Card">💳 Card</option>
                <option value="Online Transfer">🌐 Online Transfer</option>
                <option value="QR Payment">📱 QR Payment</option>
            </select>
            <button type="submit"
                    class="btn btn-success"
                    onclick="return confirm('Mark this bill as PAID?')">
                ✅ Mark as Paid
            </button>
        </form>
    </c:if>

    <a href="${pageContext.request.contextPath}/bills/view?id=${bill.id}&format=pdf"
       class="btn btn-primary">
        📄 Download PDF
    </a>
</div>

<!-- PRINTABLE INVOICE -->
<div class="invoice-paper" id="invoicePrint">

    <!-- Invoice Header -->
    <div class="inv-header">
        <div class="inv-logo">
            <span class="inv-logo-icon">🦷</span>
            <div>
                <h1 class="inv-clinic-name">Sunrise Dental Clinic</h1>
                <p class="inv-clinic-address">
                    Colombo, Sri Lanka &bull;
                    Tel: 011-234-5678 &bull;
                    sunrisedental.lk
                </p>
            </div>
        </div>
        <div class="inv-title-box">
            <h2 class="inv-title">INVOICE</h2>
            <div class="inv-code">
                <c:out value="${bill.billCode}"/>
            </div>
            <div class="inv-status-box">
                <span class="badge ${bill.statusBadgeClass} badge-lg">
                    <c:out value="${bill.statusIcon}"/>
                    <c:out value="${bill.status}"/>
                </span>
            </div>
        </div>
    </div>

    <hr class="inv-divider">

    <!-- Patient & Appointment Info -->
    <div class="inv-info-grid">
        <div class="inv-info-block">
            <h4 class="inv-info-title">BILL TO:</h4>
            <p class="inv-patient-name">
                <c:out value="${bill.patientName}"/>
            </p>
            <p><c:out value="${bill.patientCode}"/></p>
            <p><c:out value="${bill.patientContact}"/></p>
            <c:if test="${not empty bill.patientAddress}">
                <p><c:out value="${bill.patientAddress}"/></p>
            </c:if>
        </div>
        <div class="inv-info-block">
            <h4 class="inv-info-title">APPOINTMENT:</h4>
            <p>
                <strong>Ref:</strong>
                <c:out value="${bill.aptCode}"/>
            </p>
            <p>
                <strong>Date:</strong>
                <c:out value="${bill.aptDate}"/>
            </p>
            <p>
                <strong>Time:</strong>
                <c:out value="${bill.aptTime}"/>
            </p>
            <p>
                <strong>Dentist:</strong>
                <c:out value="${bill.dentistName}"/>
            </p>
        </div>
        <div class="inv-info-block">
            <h4 class="inv-info-title">INVOICE DETAILS:</h4>
            <p>
                <strong>Invoice #:</strong>
                <c:out value="${bill.billCode}"/>
            </p>
            <p>
                <strong>Issued:</strong>
                <c:out value="${bill.createdAt}"/>
            </p>
            <c:if test="${bill.isPaid()}">
                <p>
                    <strong>Paid:</strong>
                    <c:out value="${bill.settledAt}"/>
                </p>
                <p>
                    <strong>Via:</strong>
                    <c:out value="${bill.paymentIcon}"/>
                    <c:out value="${bill.paymentMethod}"/>
                </p>
            </c:if>
        </div>
    </div>

    <!-- Line Items Table -->
    <table class="inv-items-table">
        <thead>
            <tr>
                <th>Description</th>
                <th>Details</th>
                <th class="text-right">Amount (LKR)</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>
                    <strong>
                        <c:out value="${bill.treatmentName}"/>
                    </strong>
                    <br>
                    <small>Dental Procedure Fee</small>
                </td>
                <td>1 session</td>
                <td class="text-right">
                    <fmt:formatNumber value="${bill.treatmentFee}"
                                      pattern="#,##0.00"/>
                </td>
            </tr>
            <tr>
                <td>
                    <strong>Consultation Fee</strong>
                    <br>
                    <small>
                        Dr. <c:out value="${bill.dentistName}"/>
                    </small>
                </td>
                <td>1 consultation</td>
                <td class="text-right">
                    <fmt:formatNumber value="${bill.consultFee}"
                                      pattern="#,##0.00"/>
                </td>
            </tr>
        </tbody>
        <tfoot>
            <tr class="inv-subtotal">
                <td colspan="2">Subtotal</td>
                <td class="text-right">
                    <fmt:formatNumber value="${bill.subtotal}"
                                      pattern="#,##0.00"/>
                </td>
            </tr>
            <c:if test="${bill.discount > 0}">
                <tr class="inv-discount">
                    <td colspan="2">Discount</td>
                    <td class="text-right text-success">
                        -<fmt:formatNumber value="${bill.discount}"
                                           pattern="#,##0.00"/>
                    </td>
                </tr>
            </c:if>
            <tr class="inv-total">
                <td colspan="2">
                    <strong>TOTAL AMOUNT DUE</strong>
                </td>
                <td class="text-right">
                    <strong>
                        LKR <fmt:formatNumber value="${bill.totalAmount}"
                                              pattern="#,##0.00"/>
                    </strong>
                </td>
            </tr>
        </tfoot>
    </table>

    <!-- Payment Status -->
    <div class="inv-payment-status">
        <c:choose>
            <c:when test="${bill.isPaid()}">
                <div class="inv-paid-stamp">
                    ✓ PAID — <c:out value="${bill.paymentIcon}"/>
                    <c:out value="${bill.paymentMethod}"/>
                </div>
                <p class="inv-settled-by">
                    Settled by:
                    <c:out value="${bill.settledByName}"/>
                    on <c:out value="${bill.settledAt}"/>
                </p>
            </c:when>
            <c:otherwise>
                <div class="inv-pending-stamp">
                    ⏳ PAYMENT PENDING
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- Invoice Footer -->
    <div class="inv-footer">
        <p>Thank you for choosing Sunrise Dental Clinic!</p>
        <p>For queries, call: 011-234-5678 | Email: info@sunrisedental.lk</p>
        <p class="inv-print-note">
            Generated by Sunrise Dental Clinic Management System
        </p>
    </div>

</div>
<!-- END INVOICE PAPER -->

</c:if>

<script>
    // Bill creation form scripts
    let tFee = 0, cFee = 0;

    function loadAppointmentCost(select) {
        const opt = select.options[select.selectedIndex];
        tFee = parseFloat(opt.getAttribute('data-treatment')) || 0;
        cFee = parseFloat(opt.getAttribute('data-consult'))   || 1500;
        recalcTotal();
    }

    function recalcTotal() {
        const disc    = parseFloat(document.getElementById('discount')?.value) || 0;
        const total   = Math.max(0, (tFee + cFee) - disc);
        const fmt     = (n) => 'LKR ' + n.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',');

        const tEl = document.getElementById('tFee');
        const cEl = document.getElementById('cFee');
        const dEl = document.getElementById('discDisplay');
        const aEl = document.getElementById('totalAmt');

        if (tEl) tEl.textContent = fmt(tFee);
        if (cEl) cEl.textContent = fmt(cFee);
        if (dEl) dEl.textContent = '-' + fmt(disc);
        if (aEl) aEl.textContent = fmt(total);
    }

    // Auto-load if preselected appointment
    window.addEventListener('load', function() {
        const sel = document.getElementById('appointmentId');
        if (sel && sel.value) loadAppointmentCost(sel);
    });
</script>

<%@ include file="../common/footer.jsp" %>