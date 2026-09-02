package lk.sunrise.dental.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ================================================================
 * ServiceResultTest.java
 * Unit tests for the standardized service layer response object.
 * ================================================================
 */
class ServiceResultTest {

    @Test
    void success_withoutId_reportsSuccessAndNoGeneratedId() {
        ServiceResult result = ServiceResult.success("Done");

        assertTrue(result.isSuccess());
        assertFalse(result.isFailure());
        assertEquals("Done", result.getMessage());
        assertFalse(result.hasGeneratedId());
        assertEquals(-1, result.getGeneratedId());
    }

    @Test
    void success_withId_reportsGeneratedId() {
        ServiceResult result = ServiceResult.success("Created", 7);

        assertTrue(result.isSuccess());
        assertTrue(result.hasGeneratedId());
        assertEquals(7, result.getGeneratedId());
    }

    @Test
    void failure_reportsFailureWithMessage() {
        ServiceResult result = ServiceResult.failure("Something went wrong");

        assertFalse(result.isSuccess());
        assertTrue(result.isFailure());
        assertEquals("Something went wrong", result.getMessage());
        assertFalse(result.hasGeneratedId());
    }
}
