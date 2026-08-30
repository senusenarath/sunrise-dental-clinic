package lk.sunrise.dental.service;

import lk.sunrise.dental.dao.AppointmentDAO;
import lk.sunrise.dental.dao.BillDAO;
import lk.sunrise.dental.dao.PatientDAO;
import lk.sunrise.dental.model.Appointment;
import lk.sunrise.dental.model.Bill;
import lk.sunrise.dental.model.Patient;
import lk.sunrise.dental.service.event.EmailNotificationListener;
import lk.sunrise.dental.service.event.NotificationListener;
import lk.sunrise.dental.util.ValidationUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ================================================================
 * BillService.java
 * Business Logic Layer for Billing operations
 *
 * Handles invoice creation, settlement rules,
 * payment processing and financial calculations.
 *
 * Design Pattern : Observer - notifies registered NotificationListeners
 * when a bill is created or settled (see AppointmentService for the
 * same pattern applied to appointment events).
 * Package : lk.sunrise.dental.service
 * ================================================================
 */
public class BillService {

    private final BillDAO        billDAO        = new BillDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PatientDAO     patientDAO     = new PatientDAO();
    private final List<NotificationListener> listeners = new ArrayList<>();

    // Standard consultation fee (added to every bill)
    private static final double DEFAULT_CONSULT_FEE = 1500.00;

    public BillService() {
        listeners.add(new EmailNotificationListener());
    }

    /** Register an additional observer (e.g. for tests, or a future SMS channel). */
    public void addListener(NotificationListener listener) {
        listeners.add(listener);
    }

    // ──────────────────────────────────────────────────────────────
    // READ OPERATIONS
    // ──────────────────────────────────────────────────────────────

    /**
     * Get all bills.
     */
    public List<Bill> getAllBills() {
        return billDAO.getAllBills();
    }

    /**
     * Get bill by ID.
     */
    public Bill getBillById(int id) {
        if (id <= 0) return null;
        return billDAO.getBillById(id);
    }

    /**
     * Get bill by appointment ID.
     */
    public Bill getBillByAppointmentId(int appointmentId) {
        return billDAO.getBillByAppointmentId(appointmentId);
    }

    /**
     * Get bills filtered by status.
     *
     * @param status filter or null/empty for all
     */
    public List<Bill> getBillsByStatus(String status) {
        if (status == null || status.trim().isEmpty() || status.equals("All")) {
            return billDAO.getAllBills();
        }
        return billDAO.getBillsByStatus(status);
    }

    /**
     * Get bills for specific patient.
     */
    public List<Bill> getBillsByPatient(int patientId) {
        return billDAO.getBillsByPatient(patientId);
    }

    // ── Financial Statistics ───────────────────────────────────────

    public double getTotalRevenue()    { return billDAO.getTotalRevenue(); }
    public double getMonthlyRevenue()  { return billDAO.getMonthlyRevenue(); }
    public double getPendingAmount()   { return billDAO.getPendingAmount(); }
    public int    getPendingCount()    { return billDAO.getPendingBillsCount(); }

    /** Paid revenue grouped by month for the last N months, oldest first. */
    public List<Map<String, Object>> getMonthlyRevenueTrend(int months) {
        return billDAO.getMonthlyRevenueTrend(months);
    }

    // ──────────────────────────────────────────────────────────────
    // CREATE OPERATIONS
    // ──────────────────────────────────────────────────────────────

    /**
     * Generate a new invoice for a completed/scheduled appointment.
     * Auto-calculates costs from treatment and doctor consultation fee.
     *
     * @param appointmentIdStr appointment ID string from form
     * @param paymentMethod    selected payment method
     * @param discountStr      discount amount string
     * @param createdBy        ID of staff creating the bill
     * @return ServiceResult with success flag, message and bill ID
     */
    public ServiceResult createBill(String appointmentIdStr, String paymentMethod,
                                     String discountStr, int createdBy) {
        // Validate inputs
        List<String> errors = ValidationUtil.validateBill(
                appointmentIdStr, paymentMethod, discountStr
        );
        if (!errors.isEmpty()) {
            return ServiceResult.failure(String.join(" | ", errors));
        }

        int appointmentId = ValidationUtil.parseIntSafe(appointmentIdStr, 0);

        // Check appointment exists
        Appointment apt = appointmentDAO.getAppointmentById(appointmentId);
        if (apt == null) {
            return ServiceResult.failure("Selected appointment not found.");
        }

        // Check appointment is not cancelled
        if (apt.isCancelled()) {
            return ServiceResult.failure(
                    "Cannot create a bill for a cancelled appointment."
            );
        }

        // Check bill does not already exist
        if (billDAO.billExistsForAppointment(appointmentId)) {
            return ServiceResult.failure(
                    "A bill already exists for appointment " + apt.getAptCode() +
                    ". Please search for the existing bill."
            );
        }

        // Calculate costs
        double treatmentFee = apt.getTreatmentCost();
        double consultFee   = apt.getConsultFee() > 0
                              ? apt.getConsultFee()
                              : DEFAULT_CONSULT_FEE;
        double discount     = ValidationUtil.parseDoubleSafe(discountStr, 0.0);

        // Validate discount does not exceed total
        if (discount > (treatmentFee + consultFee)) {
            return ServiceResult.failure(
                    "Discount (LKR " + discount + ") cannot exceed " +
                    "total amount (LKR " + (treatmentFee + consultFee) + ")."
            );
        }

        double total = Math.max(0, (treatmentFee + consultFee) - discount);

        // Build Bill object
        Bill bill = new Bill();
        bill.setBillCode(billDAO.generateBillCode());
        bill.setAppointmentId(appointmentId);
        bill.setTreatmentFee(treatmentFee);
        bill.setConsultFee(consultFee);
        bill.setDiscount(discount);
        bill.setTotalAmount(total);
        bill.setPaymentMethod(paymentMethod);
        bill.setStatus("Pending");
        bill.setCreatedBy(createdBy);

        // Save to database
        int newId = billDAO.createBill(bill);
        if (newId > 0) {
            notifyCreated(newId, apt.getPatientId());
            return ServiceResult.success(
                    "Invoice " + bill.getBillCode() + " generated successfully. " +
                    "Total amount: LKR " + String.format("%.2f", total),
                    newId
            );
        }

        return ServiceResult.failure("Failed to generate invoice. Please try again.");
    }

    /** Notify observers that a new bill was created. */
    private void notifyCreated(int billId, int patientId) {
        Bill    created = billDAO.getBillById(billId);
        Patient patient = patientDAO.getPatientById(patientId);
        for (NotificationListener listener : listeners) {
            try {
                listener.onBillCreated(created, patient);
            } catch (Exception e) {
                System.err.println("[BillService] Listener error (created): " + e.getMessage());
            }
        }
    }

    // ──────────────────────────────────────────────────────────────
    // UPDATE OPERATIONS
    // ──────────────────────────────────────────────────────────────

    /**
     * Settle (pay) a pending bill.
     * Immutability lock: once paid, bill cannot be changed.
     *
     * @param billId        bill ID to settle
     * @param paymentMethod payment method used
     * @param settledBy     ID of staff processing payment
     * @return ServiceResult with success flag and message
     */
    public ServiceResult settleBill(int billId, String paymentMethod, int settledBy) {
        // Check bill exists
        Bill bill = billDAO.getBillById(billId);
        if (bill == null) {
            return ServiceResult.failure("Bill not found.");
        }

        // Check bill is still pending
        if (!bill.isPending()) {
            return ServiceResult.failure(
                    "This bill is already " + bill.getStatus().toLowerCase() +
                    " and cannot be modified."
            );
        }

        // Validate payment method
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            return ServiceResult.failure("Please select a payment method.");
        }

        boolean settled = billDAO.settleBill(billId, paymentMethod, settledBy);
        if (settled) {
            notifySettled(billId, bill.getAppointmentId(), paymentMethod);
            return ServiceResult.success(
                    "Payment of LKR " + String.format("%.2f", bill.getTotalAmount()) +
                    " received via " + paymentMethod +
                    ". Bill " + bill.getBillCode() + " marked as PAID. ✓"
            );
        }

        return ServiceResult.failure("Failed to process payment. Please try again.");
    }

    /** Notify observers that a bill was settled (marked paid). */
    private void notifySettled(int billId, int appointmentId, String paymentMethod) {
        Bill        settledBill = billDAO.getBillById(billId);
        Appointment apt         = appointmentDAO.getAppointmentById(appointmentId);
        Patient     patient     = apt != null ? patientDAO.getPatientById(apt.getPatientId()) : null;
        for (NotificationListener listener : listeners) {
            try {
                listener.onBillSettled(settledBill, patient, paymentMethod);
            } catch (Exception e) {
                System.err.println("[BillService] Listener error (settled): " + e.getMessage());
            }
        }
    }

    /**
     * Cancel a pending bill.
     *
     * @param billId   bill ID to cancel
     * @param userRole role of user cancelling
     * @return ServiceResult with success flag and message
     */
    public ServiceResult cancelBill(int billId, String userRole) {
        // Only admins can cancel bills
        if (!"ADMIN".equalsIgnoreCase(userRole)) {
            return ServiceResult.failure(
                    "Access denied. Only administrators can cancel bills."
            );
        }

        Bill bill = billDAO.getBillById(billId);
        if (bill == null) {
            return ServiceResult.failure("Bill not found.");
        }

        if (!bill.isPending()) {
            return ServiceResult.failure(
                    "Only pending bills can be cancelled. " +
                    "This bill is already " + bill.getStatus().toLowerCase() + "."
            );
        }

        boolean cancelled = billDAO.cancelBill(billId);
        if (cancelled) {
            return ServiceResult.success(
                    "Bill " + bill.getBillCode() + " has been cancelled."
            );
        }

        return ServiceResult.failure("Failed to cancel bill.");
    }

    /**
     * Calculate preview of bill total before saving.
     * Used for AJAX live preview in the billing form.
     *
     * @param treatmentFee treatment base cost
     * @param consultFee   doctor consultation fee
     * @param discount     discount amount
     * @return calculated total
     */
    public double calculateTotal(double treatmentFee, double consultFee, double discount) {
        return Math.max(0, (treatmentFee + consultFee) - discount);
    }
}