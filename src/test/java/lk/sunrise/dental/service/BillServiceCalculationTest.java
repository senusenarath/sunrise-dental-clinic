package lk.sunrise.dental.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ================================================================
 * BillServiceCalculationTest.java
 * Unit tests for the pure bill total calculation (no database
 * access is exercised here - see BillDAO for persistence logic).
 * ================================================================
 */
class BillServiceCalculationTest {

    private final BillService billService = new BillService();

    @Test
    void calculateTotal_addsFeesAndSubtractsDiscount() {
        double total = billService.calculateTotal(4000.00, 1500.00, 500.00);
        assertEquals(5000.00, total, 0.001);
    }

    @Test
    void calculateTotal_neverGoesNegative() {
        double total = billService.calculateTotal(1000.00, 500.00, 5000.00);
        assertEquals(0.0, total, 0.001);
    }

    @Test
    void calculateTotal_withNoDiscount() {
        double total = billService.calculateTotal(2500.00, 1500.00, 0.0);
        assertEquals(4000.00, total, 0.001);
    }
}
