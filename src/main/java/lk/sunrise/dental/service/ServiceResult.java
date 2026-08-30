package lk.sunrise.dental.service;

/**
 * ================================================================
 * ServiceResult.java
 * Standardized Service Layer Response Object
 *
 * All service methods return a ServiceResult to provide
 * consistent success/failure responses with messages.
 *
 * Usage:
 *   ServiceResult result = patientService.registerPatient(...);
 *   if (result.isSuccess()) {
 *       int newId = result.getGeneratedId();
 *   } else {
 *       String error = result.getMessage();
 *   }
 *
 * Package : lk.sunrise.dental.service
 * ================================================================
 */
public class ServiceResult {

    // ── Fields ─────────────────────────────────────────────────────
    private final boolean success;
    private final String  message;
    private final int     generatedId;  // ID of newly created record (if applicable)

    // ── Private Constructor ────────────────────────────────────────
    private ServiceResult(boolean success, String message, int generatedId) {
        this.success     = success;
        this.message     = message;
        this.generatedId = generatedId;
    }

    // ── Static Factory Methods ─────────────────────────────────────

    /**
     * Create a SUCCESS result without generated ID.
     *
     * @param message success message to display
     */
    public static ServiceResult success(String message) {
        return new ServiceResult(true, message, -1);
    }

    /**
     * Create a SUCCESS result WITH generated ID.
     * Used when a new record is created.
     *
     * @param message     success message to display
     * @param generatedId ID of the newly created record
     */
    public static ServiceResult success(String message, int generatedId) {
        return new ServiceResult(true, message, generatedId);
    }

    /**
     * Create a FAILURE result.
     *
     * @param message error message explaining what went wrong
     */
    public static ServiceResult failure(String message) {
        return new ServiceResult(false, message, -1);
    }

    // ── Getters ────────────────────────────────────────────────────

    /**
     * Check if operation succeeded.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Check if operation failed.
     */
    public boolean isFailure() {
        return !success;
    }

    /**
     * Get the result message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the generated ID of newly created record.
     * Returns -1 if no record was created.
     */
    public int getGeneratedId() {
        return generatedId;
    }

    /**
     * Check if a new record was generated.
     */
    public boolean hasGeneratedId() {
        return generatedId > 0;
    }

    // ── toString ───────────────────────────────────────────────────

    @Override
    public String toString() {
        return "ServiceResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", generatedId=" + generatedId +
                '}';
    }
}