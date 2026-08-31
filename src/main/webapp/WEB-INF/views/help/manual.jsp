<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
    manual.jsp - Staff Training Manual & Help Guide
    Location: /WEB-INF/views/help/manual.jsp
--%>

<%@ include file="../common/header.jsp" %>
<%@ include file="../common/navbar.jsp" %>

<!-- Print Button -->
<div class="help-actions no-print">
    <button onclick="window.print()" class="btn btn-primary">
        🖨️ Print Training Manual
    </button>
</div>

<!-- ═══════════════════════════════════════════════════════════════
     HELP TABS
     ═══════════════════════════════════════════════════════════════ -->
<div class="help-tabs no-print">
    <button class="help-tab active" onclick="showTab('getting-started')">
        🚀 Getting Started
    </button>
    <button class="help-tab" onclick="showTab('patients')">
        👥 Patients
    </button>
    <button class="help-tab" onclick="showTab('appointments')">
        📅 Appointments
    </button>
    <button class="help-tab" onclick="showTab('billing')">
        💰 Billing
    </button>
    <button class="help-tab" onclick="showTab('faq')">
        ❓ FAQ
    </button>
</div>

<!-- ═══════════════════════════════════════════════════════════════
     CHAPTER 1: GETTING STARTED
     ═══════════════════════════════════════════════════════════════ -->
<div id="getting-started" class="help-chapter">
    <div class="section-card">
        <div class="help-chapter-header">
            <h2>🚀 Chapter 1: Getting Started</h2>
            <p>System overview, login and navigation guide</p>
        </div>

        <!-- System Overview -->
        <div class="help-section">
            <h3>📋 System Overview</h3>
            <p>
                The <strong>Sunrise Dental Clinic Management System</strong> is a
                web-based platform designed to replace manual paper records with a
                secure, efficient digital system.
            </p>
            <div class="help-feature-grid">
                <div class="help-feature">
                    <span class="help-feature-icon">👥</span>
                    <strong>Patient Registry</strong>
                    <p>Digital patient profiles with medical history</p>
                </div>
                <div class="help-feature">
                    <span class="help-feature-icon">📅</span>
                    <strong>Appointment Booking</strong>
                    <p>Schedule with double-booking prevention</p>
                </div>
                <div class="help-feature">
                    <span class="help-feature-icon">💰</span>
                    <strong>Billing System</strong>
                    <p>Auto-calculated invoices with 4 payment methods</p>
                </div>
                <div class="help-feature">
                    <span class="help-feature-icon">📈</span>
                    <strong>Analytics</strong>
                    <p>Executive reports and revenue tracking</p>
                </div>
            </div>
        </div>

        <!-- Login Guide -->
        <div class="help-section">
            <h3>🔐 How to Login</h3>
            <ol class="help-steps">
                <li>Open your browser and navigate to the clinic system URL</li>
                <li>Enter your <strong>Username</strong> in the first field</li>
                <li>Enter your <strong>Password</strong> in the second field</li>
                <li>Click <strong>"Sign In to System"</strong></li>
                <li>You will be redirected to your role-specific dashboard</li>
            </ol>
            <div class="help-note">
                <strong>⚠️ Security Note:</strong>
                Your session expires after 30 minutes of inactivity.
                Always click "Logout" when leaving your workstation.
            </div>
        </div>

        <!-- Navigation -->
        <div class="help-section">
            <h3>🧭 Navigation Guide</h3>
            <div class="help-nav-grid">
                <div class="help-nav-item">
                    <strong>📊 Dashboard</strong>
                    <p>Daily KPIs, today's schedule, quick actions</p>
                </div>
                <div class="help-nav-item">
                    <strong>👥 Patients</strong>
                    <p>Search, register and view patient profiles</p>
                </div>
                <div class="help-nav-item">
                    <strong>📅 Appointments</strong>
                    <p>Book, view and manage appointment schedule</p>
                </div>
                <div class="help-nav-item">
                    <strong>💰 Billing</strong>
                    <p>Generate invoices and process payments</p>
                </div>
                <div class="help-nav-item">
                    <strong>👤 Staff</strong>
                    <p>Admin only: manage staff accounts and roles</p>
                </div>
                <div class="help-nav-item">
                    <strong>📈 Reports</strong>
                    <p>Admin only: analytics and financial reports</p>
                </div>
            </div>
        </div>

    </div>
</div>

<!-- ═══════════════════════════════════════════════════════════════
     CHAPTER 2: PATIENTS
     ═══════════════════════════════════════════════════════════════ -->
<div id="patients" class="help-chapter" style="display:none;">
    <div class="section-card">
        <div class="help-chapter-header">
            <h2>👥 Chapter 2: Patient Management</h2>
            <p>Registering, searching and updating patient records</p>
        </div>

        <div class="help-section">
            <h3>➕ Registering a New Patient</h3>
            <ol class="help-steps">
                <li>Click <strong>"Patients"</strong> in the navigation bar</li>
                <li>Click <strong>"Register New Patient"</strong> button</li>
                <li>Fill in the <strong>Full Name</strong> (required)</li>
                <li>Enter <strong>Date of Birth</strong> (optional)</li>
                <li>Select <strong>Gender</strong> (required)</li>
                <li>Enter <strong>Contact Number</strong> in format
                    <code>07XXXXXXXX</code> (required)</li>
                <li>Fill in optional fields: Email, Address, Blood Type,
                    Allergies, Medical Notes</li>
                <li>Click <strong>"Register Patient"</strong></li>
                <li>Patient code is auto-generated (e.g. PAT-2025-0001)</li>
            </ol>
            <div class="help-note help-note-success">
                ✅ The system automatically checks for duplicate contact numbers
                to prevent double registration.
            </div>
        </div>

        <div class="help-section">
            <h3>🔍 Searching for a Patient</h3>
            <ol class="help-steps">
                <li>Go to <strong>Patients</strong> page</li>
                <li>Type in the search box: name, contact number,
                    email or patient code</li>
                <li>Click <strong>"Search"</strong> or press Enter</li>
                <li>Click patient name to view full profile</li>
            </ol>
        </div>

        <div class="help-section">
            <h3>✏️ Editing Patient Details</h3>
            <ol class="help-steps">
                <li>Find the patient in the directory</li>
                <li>Click <strong>"Edit"</strong> button on patient row</li>
                <li>Update the required fields</li>
                <li>Click <strong>"Update Patient"</strong></li>
            </ol>
        </div>

    </div>
</div>

<!-- ═══════════════════════════════════════════════════════════════
     CHAPTER 3: APPOINTMENTS
     ═══════════════════════════════════════════════════════════════ -->
<div id="appointments" class="help-chapter" style="display:none;">
    <div class="section-card">
        <div class="help-chapter-header">
            <h2>📅 Chapter 3: Appointment Management</h2>
            <p>Booking, managing and updating appointments</p>
        </div>

        <div class="help-section">
            <h3>📅 Booking a New Appointment</h3>
            <ol class="help-steps">
                <li>Click <strong>"Appointments"</strong> in navigation</li>
                <li>Click <strong>"Book Appointment"</strong></li>
                <li>Select the <strong>Patient</strong> from dropdown</li>
                <li>Select the <strong>Dentist</strong></li>
                <li>Select <strong>Treatment Type</strong>
                    (cost auto-displayed)</li>
                <li>Choose <strong>Appointment Date</strong>
                    (cannot be past date)</li>
                <li>Choose <strong>Time Slot</strong>
                    (08:00 AM - 06:00 PM)</li>
                <li>Add optional notes</li>
                <li>Click <strong>"Confirm Booking"</strong></li>
            </ol>
            <div class="help-note">
                <strong>🛡️ Double Booking Protection:</strong>
                The system automatically prevents booking the same dentist
                at the same date and time.
            </div>
        </div>

        <div class="help-section">
            <h3>⚕️ Updating Treatment Status (Dentist/Admin)</h3>
            <ol class="help-steps">
                <li>Find appointment in the schedule</li>
                <li>Click <strong>"Update"</strong> button</li>
                <li>Change status:
                    <ul>
                        <li><strong>Scheduled</strong> → Patient is booked</li>
                        <li><strong>In Progress</strong> → Treatment ongoing</li>
                        <li><strong>Completed</strong> → Treatment finished</li>
                        <li><strong>Cancelled</strong> → Visit cancelled</li>
                    </ul>
                </li>
                <li>Add clinical notes and findings</li>
                <li>Click <strong>"Update Status"</strong></li>
            </ol>
        </div>

        <div class="help-section">
            <h3>❌ Cancelling an Appointment</h3>
            <ol class="help-steps">
                <li>Find the appointment in the schedule</li>
                <li>Click <strong>"Cancel"</strong> button</li>
                <li>Confirm the cancellation in the popup</li>
            </ol>
            <div class="help-note help-note-warning">
                ⚠️ Only Scheduled and In Progress appointments can be cancelled.
                Completed appointments cannot be cancelled.
            </div>
        </div>

    </div>
</div>

<!-- ═══════════════════════════════════════════════════════════════
     CHAPTER 4: BILLING
     ═══════════════════════════════════════════════════════════════ -->
<div id="billing" class="help-chapter" style="display:none;">
    <div class="section-card">
        <div class="help-chapter-header">
            <h2>💰 Chapter 4: Billing & Invoices</h2>
            <p>Generating invoices and processing payments</p>
        </div>

        <div class="help-section">
            <h3>🧾 Generating an Invoice</h3>
            <ol class="help-steps">
                <li>Click <strong>"Billing"</strong> in navigation</li>
                <li>Click <strong>"Generate Invoice"</strong></li>
                <li>Select the completed appointment from dropdown</li>
                <li>Costs are auto-calculated:
                    <ul>
                        <li>Treatment Fee (from treatment type)</li>
                        <li>Consultation Fee (from dentist profile)</li>
                    </ul>
                </li>
                <li>Enter discount amount if applicable</li>
                <li>Select <strong>Payment Method</strong>:
                    💵 Cash, 💳 Card, 🌐 Online Transfer, 📱 QR Payment</li>
                <li>Click <strong>"Generate Invoice"</strong></li>
            </ol>
        </div>

        <div class="help-section">
            <h3>✅ Processing Payment (Mark as Paid)</h3>
            <ol class="help-steps">
                <li>Open the invoice (click bill code or "View")</li>
                <li>Select payment method from dropdown</li>
                <li>Click <strong>"Mark as Paid"</strong></li>
                <li>Confirm the action</li>
                <li>Invoice is permanently locked as PAID</li>
            </ol>
            <div class="help-note help-note-warning">
                ⚠️ Once marked as PAID, the bill cannot be modified.
                This prevents unauthorized changes.
            </div>
        </div>

        <div class="help-section">
            <h3>🖨️ Printing an Invoice</h3>
            <ol class="help-steps">
                <li>Open the invoice view</li>
                <li>Click <strong>"Print Invoice"</strong> button</li>
                <li>Your browser print dialog will open</li>
                <li>Select your printer and click Print</li>
            </ol>
        </div>

    </div>
</div>

<!-- ═══════════════════════════════════════════════════════════════
     FAQ
     ═══════════════════════════════════════════════════════════════ -->
<div id="faq" class="help-chapter" style="display:none;">
    <div class="section-card">
        <div class="help-chapter-header">
            <h2>❓ Frequently Asked Questions</h2>
        </div>

        <div class="faq-list">

            <div class="faq-item">
                <div class="faq-q"
                     onclick="toggleFaq(this)">
                    ❓ What happens if I book a dentist who is already busy?
                    <span class="faq-arrow">▼</span>
                </div>
                <div class="faq-a">
                    The system will display an error message and prevent the
                    double booking. You must select a different time slot or
                    a different dentist.
                </div>
            </div>

            <div class="faq-item">
                <div class="faq-q" onclick="toggleFaq(this)">
                    ❓ Can I edit a paid invoice?
                    <span class="faq-arrow">▼</span>
                </div>
                <div class="faq-a">
                    No. Once an invoice is marked as PAID it is permanently
                    locked. This is a security measure. Contact the system
                    administrator for corrections.
                </div>
            </div>

            <div class="faq-item">
                <div class="faq-q" onclick="toggleFaq(this)">
                    ❓ How do I reset a staff password?
                    <span class="faq-arrow">▼</span>
                </div>
                <div class="faq-a">
                    Only Administrators can reset passwords. Go to
                    Staff → Edit Staff → Change Password section.
                </div>
            </div>

            <div class="faq-item">
                <div class="faq-q" onclick="toggleFaq(this)">
                    ❓ What is the appointment code format?
                    <span class="faq-arrow">▼</span>
                </div>
                <div class="faq-a">
                    Appointment codes are auto-generated in format:
                    <code>APT-YYYYMMDD-XXXX</code>
                    e.g. <code>APT-20250115-0001</code>
                </div>
            </div>

            <div class="faq-item">
                <div class="faq-q" onclick="toggleFaq(this)">
                    ❓ Can a receptionist view financial reports?
                    <span class="faq-arrow">▼</span>
                </div>
                <div class="faq-a">
                    No. The Reports and Analytics section is restricted
                    to Administrators only.
                </div>
            </div>

            <div class="faq-item">
                <div class="faq-q" onclick="toggleFaq(this)">
                    ❓ How long before my session expires?
                    <span class="faq-arrow">▼</span>
                </div>
                <div class="faq-a">
                    Sessions expire after <strong>30 minutes</strong> of
                    inactivity. You will be redirected to the login page
                    automatically.
                </div>
            </div>

            <div class="faq-item">
                <div class="faq-q" onclick="toggleFaq(this)">
                    ❓ Can I book appointments for past dates?
                    <span class="faq-arrow">▼</span>
                </div>
                <div class="faq-a">
                    No. The system only allows booking for today or
                    future dates to prevent data errors.
                </div>
            </div>

            <div class="faq-item">
                <div class="faq-q" onclick="toggleFaq(this)">
                    ❓ What is the patient code format?
                    <span class="faq-arrow">▼</span>
                </div>
                <div class="faq-a">
                    Patient codes are auto-generated as:
                    <code>PAT-YYYY-XXXX</code>
                    e.g. <code>PAT-2025-0001</code>
                </div>
            </div>

            <div class="faq-item">
                <div class="faq-q" onclick="toggleFaq(this)">
                    ❓ Can dentists create invoices?
                    <span class="faq-arrow">▼</span>
                </div>
                <div class="faq-a">
                    No. Only Administrators and Receptionists can
                    generate and process invoices.
                </div>
            </div>

            <div class="faq-item">
                <div class="faq-q" onclick="toggleFaq(this)">
                    ❓ Who can delete patient records?
                    <span class="faq-arrow">▼</span>
                </div>
                <div class="faq-a">
                    Only Administrators can remove patients from the
                    active directory. Records are soft-deleted
                    (hidden, not permanently removed).
                </div>
            </div>

        </div>
    </div>
</div>

<script>
    function showTab(tabId) {
        // Hide all chapters
        document.querySelectorAll('.help-chapter')
                .forEach(ch => ch.style.display = 'none');

        // Deactivate all tabs
        document.querySelectorAll('.help-tab')
                .forEach(t => t.classList.remove('active'));

        // Show selected chapter
        document.getElementById(tabId).style.display = 'block';

        // Activate clicked tab
        event.target.classList.add('active');
    }

    function toggleFaq(el) {
        const answer = el.nextElementSibling;
        const arrow  = el.querySelector('.faq-arrow');
        const isOpen = answer.style.display === 'block';

        // Close all
        document.querySelectorAll('.faq-a')
                .forEach(a => a.style.display = 'none');
        document.querySelectorAll('.faq-arrow')
                .forEach(a => a.textContent = '▼');

        // Open clicked if was closed
        if (!isOpen) {
            answer.style.display = 'block';
            arrow.textContent = '▲';
        }
    }
</script>

<%@ include file="../common/footer.jsp" %>