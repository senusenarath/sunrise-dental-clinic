package lk.sunrise.dental.model;

import java.time.LocalDateTime;

/**
 * ================================================================
 * User.java
 * Model class representing a staff member (Admin/Receptionist/Dentist)
 * Package : lk.sunrise.dental.model
 * ================================================================
 */
public class User {

    // ── Fields ─────────────────────────────────────────────────────
    private int           id;
    private String        userCode;
    private String        username;
    private String        passwordHash;
    private String        fullName;
    private String        email;
    private String        contact;
    private String        role;           // ADMIN, RECEPTIONIST, DENTIST
    private String        specialization; // Only for DENTIST
    private double        consultFee;
    private boolean       isActive;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Constructors ───────────────────────────────────────────────

    /** Default constructor */
    public User() {}

    /** Constructor for login verification */
    public User(int id, String username, String fullName, String role) {
        this.id       = id;
        this.username = username;
        this.fullName = fullName;
        this.role     = role;
    }

    /** Full constructor */
    public User(int id, String userCode, String username, String passwordHash,
                String fullName, String email, String contact, String role,
                String specialization, double consultFee, boolean isActive,
                LocalDateTime lastLogin, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id             = id;
        this.userCode       = userCode;
        this.username       = username;
        this.passwordHash   = passwordHash;
        this.fullName       = fullName;
        this.email          = email;
        this.contact        = contact;
        this.role           = role;
        this.specialization = specialization;
        this.consultFee     = consultFee;
        this.isActive       = isActive;
        this.lastLogin      = lastLogin;
        this.createdAt      = createdAt;
        this.updatedAt      = updatedAt;
    }

    // ── Role Check Helpers ─────────────────────────────────────────

    /** Check if user is Administrator */
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(this.role);
    }

    /** Check if user is Receptionist */
    public boolean isReceptionist() {
        return "RECEPTIONIST".equalsIgnoreCase(this.role);
    }

    /** Check if user is Dentist */
    public boolean isDentist() {
        return "DENTIST".equalsIgnoreCase(this.role);
    }

    /**
     * Get role display label with icon for UI.
     */
    public String getRoleDisplay() {
        if (isAdmin())        return "👑 Administrator";
        if (isReceptionist()) return "🖥️ Receptionist";
        if (isDentist())      return "🩺 Dentist";
        return role;
    }

    /**
     * Get role badge CSS class for styling.
     */
    public String getRoleBadgeClass() {
        if (isAdmin())        return "badge-admin";
        if (isReceptionist()) return "badge-receptionist";
        if (isDentist())      return "badge-dentist";
        return "badge-default";
    }

    /**
     * Get display name - full name with Dr. prefix for dentists.
     */
    public String getDisplayName() {
        if (isDentist() && fullName != null && !fullName.startsWith("Dr.")) {
            return "Dr. " + fullName;
        }
        return fullName;
    }

    // ── Getters ────────────────────────────────────────────────────

    public int           getId()             { return id; }
    public String        getUserCode()       { return userCode; }
    public String        getUsername()       { return username; }
    public String        getPasswordHash()   { return passwordHash; }
    public String        getFullName()       { return fullName; }
    public String        getEmail()          { return email; }
    public String        getContact()        { return contact; }
    public String        getRole()           { return role; }
    public String        getSpecialization() { return specialization; }
    public double        getConsultFee()     { return consultFee; }
    public boolean       isActive()          { return isActive; }
    public LocalDateTime getLastLogin()      { return lastLogin; }
    public LocalDateTime getCreatedAt()      { return createdAt; }
    public LocalDateTime getUpdatedAt()      { return updatedAt; }

    // ── Setters ────────────────────────────────────────────────────

    public void setId(int id)                          { this.id             = id; }
    public void setUserCode(String userCode)           { this.userCode       = userCode; }
    public void setUsername(String username)           { this.username       = username; }
    public void setPasswordHash(String passwordHash)   { this.passwordHash   = passwordHash; }
    public void setFullName(String fullName)           { this.fullName       = fullName; }
    public void setEmail(String email)                 { this.email          = email; }
    public void setContact(String contact)             { this.contact        = contact; }
    public void setRole(String role)                   { this.role           = role; }
    public void setSpecialization(String spec)         { this.specialization = spec; }
    public void setConsultFee(double consultFee)       { this.consultFee     = consultFee; }
    public void setActive(boolean isActive)            { this.isActive       = isActive; }
    public void setLastLogin(LocalDateTime lastLogin)  { this.lastLogin      = lastLogin; }
    public void setCreatedAt(LocalDateTime createdAt)  { this.createdAt      = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)  { this.updatedAt      = updatedAt; }

    // ── toString ───────────────────────────────────────────────────

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", userCode='" + userCode + '\'' +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role='" + role + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}