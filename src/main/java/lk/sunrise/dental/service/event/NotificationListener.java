package lk.sunrise.dental.service.event;

import lk.sunrise.dental.model.Appointment;
import lk.sunrise.dental.model.Bill;
import lk.sunrise.dental.model.Patient;

/**
 * ================================================================
 * NotificationListener.java
 * Observer Pattern - Subject/Observer contract for domain events
 *
 * Design Pattern : Observer
 * Purpose        : Decouples the service layer (the "subject", which
 *                  raises events when patients register, appointments
 *                  are booked/cancelled, or bills are created/settled)
 *                  from whatever reacts to those events (the
 *                  "observer" - currently email, but a future SMS or
 *                  push-notification channel could implement this
 *                  same interface without touching the services).
 *
 * Because AppointmentService/BillService/PatientService notify
 * listeners rather than calling EmailService directly, EVERY caller
 * of those services - the JSP-backed servlets AND the REST API
 * servlets - gets the same notification behaviour for free, with no
 * duplicated email-sending code scattered across controllers.
 *
 * Default (no-op) method bodies let an implementer override only the
 * events it actually cares about.
 * Package : lk.sunrise.dental.service.event
 * ================================================================
 */
public interface NotificationListener {

    default void onPatientRegistered(Patient patient) {}

    default void onAppointmentBooked(Appointment appointment, Patient patient) {}

    default void onAppointmentCancelled(Appointment appointment, Patient patient) {}

    default void onBillCreated(Bill bill, Patient patient) {}

    default void onBillSettled(Bill bill, Patient patient, String paymentMethod) {}
}
