package lk.sunrise.dental.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ================================================================
 * UserTest.java
 * Unit tests for User role-checking and display helper methods.
 * ================================================================
 */
class UserTest {

    private User userWithRole(String role) {
        User user = new User();
        user.setRole(role);
        return user;
    }

    @Test
    void roleChecks_areCaseInsensitive() {
        assertTrue(userWithRole("admin").isAdmin());
        assertTrue(userWithRole("ADMIN").isAdmin());
        assertTrue(userWithRole("Dentist").isDentist());
        assertTrue(userWithRole("receptionist").isReceptionist());
    }

    @Test
    void roleChecks_areMutuallyExclusive() {
        User dentist = userWithRole("DENTIST");
        assertTrue(dentist.isDentist());
        assertFalse(dentist.isAdmin());
        assertFalse(dentist.isReceptionist());
    }

    @Test
    void getDisplayName_prefixesDrForDentistsOnly() {
        User dentist = userWithRole("DENTIST");
        dentist.setFullName("Priya Sharma");
        assertEquals("Dr. Priya Sharma", dentist.getDisplayName());

        User receptionist = userWithRole("RECEPTIONIST");
        receptionist.setFullName("Sarah Fernando");
        assertEquals("Sarah Fernando", receptionist.getDisplayName());
    }

    @Test
    void getDisplayName_doesNotDoublePrefixExistingDrTitle() {
        User dentist = userWithRole("DENTIST");
        dentist.setFullName("Dr. Kamal Perera");
        assertEquals("Dr. Kamal Perera", dentist.getDisplayName());
    }
}
