package lk.sunrise.dental.service;

import lk.sunrise.dental.dao.UserDAO;
import lk.sunrise.dental.model.User;
import lk.sunrise.dental.util.ValidationUtil;

import java.util.List;

/**
 * ================================================================
 * UserService.java
 * Business Logic Layer for User/Staff operations
 *
 * Sits between Servlet (controller) and UserDAO (data layer).
 * Handles validation, business rules and orchestration.
 * Package : lk.sunrise.dental.service
 * ================================================================
 */
public class UserService {

    private final UserDAO userDAO = new UserDAO();

    // ──────────────────────────────────────────────────────────────
    // AUTHENTICATION
    // ──────────────────────────────────────────────────────────────

    /**
     * Authenticate staff login.
     * Validates inputs then delegates to DAO.
     *
     * @param username plain username
     * @param password plain password
     * @return User object if valid, null if invalid
     */
    public User login(String username, String password) {
        // Basic null/empty check
        if (username == null || username.trim().isEmpty()) return null;
        if (password == null || password.trim().isEmpty()) return null;

        // Delegate to DAO for DB verification
        return userDAO.authenticate(username.trim(), password);
    }

    // ──────────────────────────────────────────────────────────────
    // READ OPERATIONS
    // ──────────────────────────────────────────────────────────────

    /**
     * Get all staff members.
     */
    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    /**
     * Get all active dentists.
     * Used for appointment booking dropdown.
     */
    public List<User> getAllDentists() {
        return userDAO.getAllDentists();
    }

    /**
     * Get staff member by ID.
     */
    public User getUserById(int id) {
        if (id <= 0) return null;
        return userDAO.getUserById(id);
    }

    /**
     * Get total active staff count.
     */
    public int getTotalActiveStaff() {
        return userDAO.getTotalActiveStaff();
    }

    /**
     * Get total dentist count.
     */
    public int getTotalDentists() {
        return userDAO.getTotalDentists();
    }

    // ──────────────────────────────────────────────────────────────
    // CREATE OPERATIONS
    // ──────────────────────────────────────────────────────────────

    /**
     * Register a new staff account.
     * Validates inputs and checks for duplicate username.
     *
     * @param username       login username
     * @param fullName       staff full name
     * @param password       plain password
     * @param role           ADMIN / RECEPTIONIST / DENTIST
     * @param email          email address
     * @param contact        phone number
     * @param specialization dentist specialization (optional)
     * @param consultFee     consultation fee (dentists only)
     * @return ServiceResult with success flag and message
     */
    public ServiceResult createUser(String username, String fullName,
                                     String password, String role,
                                     String email, String contact,
                                     String specialization, double consultFee) {
        // Validate inputs
        List<String> errors = ValidationUtil.validateUser(
                username, fullName, password, role, contact, email
        );
        if (!errors.isEmpty()) {
            return ServiceResult.failure(String.join(" | ", errors));
        }

        // Check duplicate username
        if (userDAO.usernameExists(username.trim())) {
            return ServiceResult.failure(
                    "Username '" + username + "' is already taken. Please choose another."
            );
        }

        // Build User object
        User user = new User();
        user.setUserCode(userDAO.generateUserCode());
        user.setUsername(username.trim());
        user.setPasswordHash(password);   // DAO will hash before saving
        user.setFullName(fullName.trim());
        user.setEmail(email != null ? email.trim() : null);
        user.setContact(contact != null ? contact.trim() : null);
        user.setRole(role.toUpperCase().trim());
        user.setSpecialization(specialization != null ? specialization.trim() : null);
        user.setConsultFee(consultFee);
        user.setActive(true);

        // Save to database
        boolean created = userDAO.createUser(user);
        if (created) {
            return ServiceResult.success(
                    "Staff account for '" + fullName + "' created successfully."
            );
        }

        return ServiceResult.failure("Failed to create staff account. Please try again.");
    }

    // ──────────────────────────────────────────────────────────────
    // UPDATE OPERATIONS
    // ──────────────────────────────────────────────────────────────

    /**
     * Update existing staff account details.
     *
     * @param userId         ID of user to update
     * @param fullName       updated full name
     * @param email          updated email
     * @param contact        updated contact
     * @param role           updated role
     * @param specialization updated specialization
     * @param consultFee     updated consultation fee
     * @param isActive       account active status
     * @return ServiceResult with success flag and message
     */
    public ServiceResult updateUser(int userId, String fullName, String email,
                                     String contact, String role,
                                     String specialization, double consultFee,
                                     boolean isActive) {
        // Check user exists
        User existing = userDAO.getUserById(userId);
        if (existing == null) {
            return ServiceResult.failure("Staff account not found.");
        }

        // Validate inputs
        List<String> errors = ValidationUtil.validateUser(
                existing.getUsername(), fullName, "placeholder", role, contact, email
        );
        if (!errors.isEmpty()) {
            return ServiceResult.failure(String.join(" | ", errors));
        }

        // Update fields
        existing.setFullName(fullName.trim());
        existing.setEmail(email != null ? email.trim() : null);
        existing.setContact(contact != null ? contact.trim() : null);
        existing.setRole(role.toUpperCase().trim());
        existing.setSpecialization(specialization != null ? specialization.trim() : null);
        existing.setConsultFee(consultFee);
        existing.setActive(isActive);

        boolean updated = userDAO.updateUser(existing);
        if (updated) {
            return ServiceResult.success(
                    "Staff account for '" + fullName + "' updated successfully."
            );
        }

        return ServiceResult.failure("Failed to update staff account. Please try again.");
    }

    /**
     * Change staff account password.
     *
     * @param userId      ID of user
     * @param newPassword plain new password
     * @return ServiceResult with success flag and message
     */
    public ServiceResult changePassword(int userId, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            return ServiceResult.failure("Password must be at least 6 characters.");
        }

        boolean updated = userDAO.updatePassword(userId, newPassword);
        if (updated) {
            return ServiceResult.success("Password changed successfully.");
        }

        return ServiceResult.failure("Failed to change password. Please try again.");
    }

    /**
     * Toggle staff account active/inactive status.
     *
     * @param userId ID of user to toggle
     * @return ServiceResult with success flag and message
     */
    public ServiceResult toggleStatus(int userId) {
        User user = userDAO.getUserById(userId);
        if (user == null) {
            return ServiceResult.failure("Staff account not found.");
        }

        // Prevent deactivating your own account
        boolean toggled = userDAO.toggleUserStatus(userId);
        if (toggled) {
            String newStatus = user.isActive() ? "deactivated" : "activated";
            return ServiceResult.success(
                    "Account for '" + user.getFullName() + "' has been " + newStatus + "."
            );
        }

        return ServiceResult.failure("Failed to update account status.");
    }
}