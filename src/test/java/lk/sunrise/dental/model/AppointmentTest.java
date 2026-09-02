package lk.sunrise.dental.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ================================================================
 * AppointmentTest.java
 * Unit tests for Appointment status/business-rule helper methods.
 * ================================================================
 */
class AppointmentTest {

    private Appointment appointmentWithStatus(String status) {
        Appointment apt = new Appointment();
        apt.setStatus(status);
        return apt;
    }

    @Test
    void isCancellable_trueForScheduledAndInProgress() {
        assertTrue(appointmentWithStatus("Scheduled").isCancellable());
        assertTrue(appointmentWithStatus("In Progress").isCancellable());
    }

    @Test
    void isCancellable_falseForCompletedAndCancelled() {
        assertFalse(appointmentWithStatus("Completed").isCancellable());
        assertFalse(appointmentWithStatus("Cancelled").isCancellable());
    }

    @Test
    void isEditable_onlyTrueForScheduled() {
        assertTrue(appointmentWithStatus("Scheduled").isEditable());
        assertFalse(appointmentWithStatus("In Progress").isEditable());
        assertFalse(appointmentWithStatus("Completed").isEditable());
    }

    @Test
    void getTotalCost_sumsTreatmentAndConsultFee() {
        Appointment apt = new Appointment();
        apt.setTreatmentCost(4000.00);
        apt.setConsultFee(1500.00);
        assertEquals(5500.00, apt.getTotalCost(), 0.001);
    }

    @Test
    void statusChecks_areCaseInsensitive() {
        assertTrue(appointmentWithStatus("scheduled").isScheduled());
        assertTrue(appointmentWithStatus("CANCELLED").isCancelled());
    }
}
