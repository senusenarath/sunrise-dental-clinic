package lk.sunrise.dental.dao;

import lk.sunrise.dental.model.User;
import lk.sunrise.dental.util.DBConnection;
import lk.sunrise.dental.util.SecurityUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ================================================================
 * UserDAO.java
 * Data Access Object for User/Staff operations
 *
 * Handles all database operations related to staff accounts.
 * Package : lk.sunrise.dental.dao
 * ================================================================
 */
public class UserDAO {

    // ──────────────────────────────────────────────────────────────
    // AUTHENTICATION
    // ──────────────────────────────────────────────────────────────

    /**
     * Authenticate user by username and password.
     * Updates last_login timestamp on successful login.
     *
     * @param username plain username
     * @param password plain password (will be hashed for comparison)
     * @return User object if valid credentials, null otherwise
     */
    public User authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND is_active = TRUE";

        try (Connection conn = DBConnection.getConn();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");

                    // Verify password
                    if (SecurityUtil.verifyPassword(password, storedHash)) {

                        // Map user BEFORE closing ResultSet
                        User user = mapUser(rs);

                        // Update last login AFTER mapping
                        updateLastLogin(user.getId());

                        return user;
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("[UserDAO] authenticate error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Update last_login timestamp for user.
     */
    private void updateLastLogin(int userId) {
        String sql = "UPDATE users SET last_login = NOW() WHERE id = ?";
        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("[UserDAO] updateLastLogin error: " + e.getMessage());
        }
    }

    // ──────────────────────────────────────────────────────────────
    // READ OPERATIONS
    // ──────────────────────────────────────────────────────────────

    /**
     * Get all active users/staff members.
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY role, full_name";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapUser(rs));
            }

        } catch (Exception e) {
            System.err.println("[UserDAO] getAllUsers error: " + e.getMessage());
        }
        return users;
    }

    /**
     * Get all active dentists only.
     * Used for appointment booking dropdown.
     */
    public List<User> getAllDentists() {
        List<User> dentists = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'DENTIST' AND is_active = TRUE ORDER BY full_name";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                dentists.add(mapUser(rs));
            }

        } catch (Exception e) {
            System.err.println("[UserDAO] getAllDentists error: " + e.getMessage());
        }
        return dentists;
    }

    /**
     * Get user by ID.
     */
    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapUser(rs);
            }

        } catch (Exception e) {
            System.err.println("[UserDAO] getUserById error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get user by username.
     */
    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapUser(rs);
            }

        } catch (Exception e) {
            System.err.println("[UserDAO] getUserByUsername error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Check if username already exists.
     */
    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            System.err.println("[UserDAO] usernameExists error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Check if username exists for another user (for update validation).
     */
    public boolean usernameExistsForOther(String username, int excludeId) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ? AND id != ?";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setInt(2, excludeId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            System.err.println("[UserDAO] usernameExistsForOther error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get next user code (auto-increment).
     * Format: USR-001, USR-002, etc.
     */
    public String generateUserCode() {
        String sql = "SELECT COUNT(*) FROM users";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                int count = rs.getInt(1) + 1;
                return String.format("USR-%03d", count);
            }

        } catch (Exception e) {
            System.err.println("[UserDAO] generateUserCode error: " + e.getMessage());
        }
        return "USR-001";
    }

    // ──────────────────────────────────────────────────────────────
    // CREATE OPERATIONS
    // ──────────────────────────────────────────────────────────────

    /**
     * Create a new staff account.
     *
     * @param user User object with all details
     * @return true if created successfully
     */
    public boolean createUser(User user) {
        String sql = """
                INSERT INTO users
                (user_code, username, password_hash, full_name, email,
                 contact, role, specialization, consult_fee, is_active)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE)
                """;

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUserCode());
            ps.setString(2, user.getUsername());
            ps.setString(3, SecurityUtil.hashPassword(user.getPasswordHash())); // hash on save
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getContact());
            ps.setString(7, user.getRole());
            ps.setString(8, user.getSpecialization());
            ps.setDouble(9, user.getConsultFee());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("[UserDAO] createUser error: " + e.getMessage());
        }
        return false;
    }

    // ──────────────────────────────────────────────────────────────
    // UPDATE OPERATIONS
    // ──────────────────────────────────────────────────────────────

    /**
     * Update existing staff account details.
     */
    public boolean updateUser(User user) {
        String sql = """
                UPDATE users SET
                    full_name      = ?,
                    email          = ?,
                    contact        = ?,
                    role           = ?,
                    specialization = ?,
                    consult_fee    = ?,
                    is_active      = ?
                WHERE id = ?
                """;

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getContact());
            ps.setString(4, user.getRole());
            ps.setString(5, user.getSpecialization());
            ps.setDouble(6, user.getConsultFee());
            ps.setBoolean(7, user.isActive());
            ps.setInt(8, user.getId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("[UserDAO] updateUser error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Update user password.
     *
     * @param userId      user ID
     * @param newPassword plain new password (will be hashed)
     * @return true if updated successfully
     */
    public boolean updatePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, SecurityUtil.hashPassword(newPassword));
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("[UserDAO] updatePassword error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Toggle user active status (enable/disable account).
     */
    public boolean toggleUserStatus(int userId) {
        String sql = "UPDATE users SET is_active = NOT is_active WHERE id = ?";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("[UserDAO] toggleUserStatus error: " + e.getMessage());
        }
        return false;
    }

    // ──────────────────────────────────────────────────────────────
    // STATISTICS
    // ──────────────────────────────────────────────────────────────

    /**
     * Get total count of active staff.
     */
    public int getTotalActiveStaff() {
        String sql = "SELECT COUNT(*) FROM users WHERE is_active = TRUE";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (Exception e) {
            System.err.println("[UserDAO] getTotalActiveStaff error: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Get total count of active dentists.
     */
    public int getTotalDentists() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'DENTIST' AND is_active = TRUE";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (Exception e) {
            System.err.println("[UserDAO] getTotalDentists error: " + e.getMessage());
        }
        return 0;
    }

    // ──────────────────────────────────────────────────────────────
    // RESULT SET MAPPER
    // ──────────────────────────────────────────────────────────────

    /**
     * Map ResultSet row to User object.
     */
    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUserCode(rs.getString("user_code"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setContact(rs.getString("contact"));
        user.setRole(rs.getString("role"));
        user.setSpecialization(rs.getString("specialization"));
        user.setConsultFee(rs.getDouble("consult_fee"));
        user.setActive(rs.getBoolean("is_active"));

        // Handle nullable timestamp
        Timestamp lastLogin = rs.getTimestamp("last_login");
        if (lastLogin != null) {
            user.setLastLogin(lastLogin.toLocalDateTime());
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return user;
    }
}