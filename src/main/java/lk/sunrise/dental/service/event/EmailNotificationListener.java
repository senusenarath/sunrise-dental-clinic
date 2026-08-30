package lk.sunrise.dental.service.event;

import lk.sunrise.dental.model.Appointment;
import lk.sunrise.dental.model.Bill;
import lk.sunrise.dental.model.Patient;
import lk.sunrise.dental.util.EmailService;

/**
 * ================================================================
 * EmailNotificationListener.java
 * Observer Pattern - concrete observer that emails patients
 *
 * Reacts to domain events raised by the service layer by sending
 * the matching email through the existing EmailService singleton.
 * Every email is sent on a background thread so a slow/unreachable
 * SMTP server never blocks the request that triggered it, and a
 * failure here must never fail the underlying business operation.
 * Package : lk.sunrise.dental.service.event
 * ================================================================
 */
public class EmailNotificationListener implements NotificationListener {

    @Override
    public void onPatientRegistered(Patient patient) {
        if (patient == null || isBlank(patient.getEmail())) return;

        final String email       = patient.getEmail();
        final String name        = patient.getFullName();
        final String patientCode = patient.getPatientCode();

        runAsync(() -> EmailService.getInstance()
                .sendWelcomeEmail(email, name, patientCode));
    }

    @Override
    public void onAppointmentBooked(Appointment appointment, Patient patient) {
        if (patient == null || isBlank(patient.getEmail())) return;

        final String email     = patient.getEmail();
        final String name      = patient.getFullName();
        final String code      = appointment.getAptCode();
        final String dentist   = appointment.getDentistName();
        final String treatment = appointment.getTreatmentName();
        final String date      = String.valueOf(appointment.getAptDate());
        final String time      = String.valueOf(appointment.getAptTime());

        runAsync(() -> EmailService.getInstance()
                .sendAppointmentConfirmation(email, name, code, dentist, treatment, date, time));
    }

    @Override
    public void onAppointmentCancelled(Appointment appointment, Patient patient) {
        if (patient == null || isBlank(patient.getEmail())) return;

        final String email = patient.getEmail();
        final String name  = patient.getFullName();
        final String code  = appointment.getAptCode();
        final String date  = String.valueOf(appointment.getAptDate());
        final String time  = String.valueOf(appointment.getAptTime());

        runAsync(() -> EmailService.getInstance()
                .sendCancellationNotification(email, name, code, date, time));
    }

    @Override
    public void onBillCreated(Bill bill, Patient patient) {
        sendBillEmail(bill, patient, bill.getPaymentMethod());
    }

    @Override
    public void onBillSettled(Bill bill, Patient patient, String paymentMethod) {
        sendBillEmail(bill, patient, paymentMethod);
    }

    private void sendBillEmail(Bill bill, Patient patient, String paymentMethod) {
        if (patient == null || isBlank(patient.getEmail())) return;

        final String email     = patient.getEmail();
        final String name      = patient.getFullName();
        final String billCode  = bill.getBillCode();
        final String treatment = bill.getTreatmentName();
        final String total     = "LKR " + String.format("%.2f", bill.getTotalAmount());
        final String payment   = paymentMethod;

        runAsync(() -> EmailService.getInstance()
                .sendBillNotification(email, name, billCode, treatment, total, payment));
    }

    private void runAsync(Runnable task) {
        new Thread(() -> {
            try {
                task.run();
            } catch (Exception e) {
                System.err.println("[EmailNotificationListener] Failed to send email: " + e.getMessage());
            }
        }).start();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
