/**
 * ================================================================
 * app.js
 * Sunrise Dental Clinic - Main JavaScript
 *
 * Handles: Toast notifications, form validation,
 * table search, UI interactions
 * ================================================================
 */

"use strict";

// ── AUTO-HIDE TOAST ─────────────────────────────────────────────
(function initToast() {
  const toast = document.getElementById("globalToast");
  if (!toast) return;

  // Auto-hide after 5 seconds
  setTimeout(() => {
    toast.style.transition = "opacity 0.5s ease, transform 0.5s ease";
    toast.style.opacity = "0";
    toast.style.transform = "translateY(20px)";
    setTimeout(() => toast.remove(), 500);
  }, 5000);
})();

// ── CLIENT-SIDE TABLE SEARCH ─────────────────────────────────────
(function initTableSearch() {
  const input = document.getElementById("tableSearch");
  if (!input) return;

  input.addEventListener("input", function () {
    const keyword = this.value.toLowerCase().trim();
    const rows = document.querySelectorAll(".data-table tbody tr");
    let visible = 0;

    rows.forEach((row) => {
      const text = row.textContent.toLowerCase();
      const matches = text.includes(keyword);
      row.style.display = matches ? "" : "none";
      if (matches) visible++;
    });

    // Update counter
    const counter = document.getElementById("tableCounter");
    if (counter) counter.textContent = visible;
  });
})();

// ── CONFIRM DELETE ───────────────────────────────────────────────
function confirmAction(message) {
  return window.confirm(message || "Are you sure?");
}

// ── FORMAT CURRENCY ──────────────────────────────────────────────
function formatLKR(amount) {
  return (
    "LKR " +
    parseFloat(amount)
      .toFixed(2)
      .replace(/\B(?=(\d{3})+(?!\d))/g, ",")
  );
}

// ── HIGHLIGHT TODAY'S APPOINTMENTS ──────────────────────────────
(function highlightToday() {
  const today = new Date().toISOString().split("T")[0];
  const dateCells = document.querySelectorAll("[data-date]");

  dateCells.forEach((cell) => {
    if (cell.getAttribute("data-date") === today) {
      cell.closest("tr")?.classList.add("today-row");
    }
  });
})();

// ── AUTO DISMISS ALERTS ──────────────────────────────────────────
(function autoDismissAlerts() {
  const alerts = document.querySelectorAll(".alert");
  alerts.forEach((alert) => {
    setTimeout(() => {
      alert.style.transition = "opacity 0.5s ease";
      alert.style.opacity = "0";
      setTimeout(() => alert.remove(), 500);
    }, 6000);
  });
})();

// ── FORM SUBMIT LOADING STATE ────────────────────────────────────
(function initFormLoading() {
  const forms = document.querySelectorAll("form:not([data-no-loading])");

  forms.forEach((form) => {
    form.addEventListener("submit", function () {
      const btn = this.querySelector('[type="submit"]');
      if (btn && !btn.disabled) {
        setTimeout(() => {
          btn.disabled = true;
          btn.style.opacity = "0.7";
          const original = btn.textContent;
          btn.textContent = "⏳ Processing...";

          // Re-enable after 10s failsafe
          setTimeout(() => {
            btn.disabled = false;
            btn.style.opacity = "1";
            btn.textContent = original;
          }, 10000);
        }, 0);
      }
    });
  });
})();

// ── PRINT INVOICE ────────────────────────────────────────────────
function printInvoice() {
  window.print();
}

// ── NUMBER INPUT VALIDATION ──────────────────────────────────────
(function initNumberInputs() {
  document.querySelectorAll('input[type="number"]').forEach((input) => {
    input.addEventListener("input", function () {
      if (parseFloat(this.value) < 0) this.value = 0;
    });
  });
})();

// ── ACTIVE NAV HIGHLIGHT ─────────────────────────────────────────
(function highlightActiveNav() {
  const currentPath = window.location.pathname;
  document.querySelectorAll(".nav-link").forEach((link) => {
    if (
      link.getAttribute("href") &&
      currentPath.includes(link.getAttribute("href").split("?")[0])
    ) {
      link.classList.add("active");
    }
  });
})();

// ── SESSION EXPIRY WARNING ───────────────────────────────────────
(function sessionWarning() {
  // Warn user 5 minutes before 30-min session expires
  const warningTime = (30 - 5) * 60 * 1000; // 25 minutes

  setTimeout(() => {
    const toast = document.createElement("div");
    toast.className = "toast toast-error";
    toast.id = "sessionWarnToast";
    toast.innerHTML = `
            <span class="toast-icon">⏰</span>
            <span class="toast-msg">
                Your session will expire in 5 minutes.
                Please save your work.
            </span>
            <button class="toast-close"
                    onclick="this.closest('.toast').remove()">✕</button>
        `;
    document.body.appendChild(toast);

    // Auto-remove after 30 seconds
    setTimeout(() => toast.remove(), 30000);
  }, warningTime);
})();

// ── KEYBOARD SHORTCUTS ───────────────────────────────────────────
document.addEventListener("keydown", function (e) {
  // Alt+P = Print
  if (e.altKey && e.key === "p") {
    e.preventDefault();
    window.print();
  }

  // Escape = Close modals/toasts
  if (e.key === "Escape") {
    document.querySelectorAll(".toast").forEach((t) => t.remove());
  }
});

// ── INIT LOG ─────────────────────────────────────────────────────
console.log(
  "%c🦷 Sunrise Dental Clinic",
  "color:#0ea5e9;font-size:16px;font-weight:bold;",
);
console.log("%cManagement System - Ready", "color:#8b949e;font-size:12px;");
