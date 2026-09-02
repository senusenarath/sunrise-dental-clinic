package lk.sunrise.dental.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ================================================================
 * SecurityUtilTest.java
 * Unit tests for password hashing, sanitization and format checks.
 * ================================================================
 */
class SecurityUtilTest {

    @Test
    void hashPassword_producesSixtyFourCharacterHexHash() {
        String hash = SecurityUtil.hashPassword("admin123");

        assertEquals(64, hash.length());
        assertTrue(hash.matches("^[0-9a-f]{64}$"));
    }

    @Test
    void hashPassword_isDeterministic() {
        assertEquals(SecurityUtil.hashPassword("dentist123"),
                     SecurityUtil.hashPassword("dentist123"));
    }

    @Test
    void hashPassword_rejectsNullOrEmpty() {
        assertThrows(IllegalArgumentException.class, () -> SecurityUtil.hashPassword(null));
        assertThrows(IllegalArgumentException.class, () -> SecurityUtil.hashPassword("   "));
    }

    @Test
    void verifyPassword_matchesCorrectPassword() {
        String stored = SecurityUtil.hashPassword("reception123");
        assertTrue(SecurityUtil.verifyPassword("reception123", stored));
    }

    @Test
    void verifyPassword_rejectsWrongPassword() {
        String stored = SecurityUtil.hashPassword("reception123");
        assertFalse(SecurityUtil.verifyPassword("wrong-password", stored));
    }

    @Test
    void verifyPassword_handlesNullsSafely() {
        assertFalse(SecurityUtil.verifyPassword(null, "abc"));
        assertFalse(SecurityUtil.verifyPassword("abc", null));
    }

    @Test
    void isValidContact_acceptsSriLankanFormats() {
        assertTrue(SecurityUtil.isValidContact("0771234567"));
        assertTrue(SecurityUtil.isValidContact("+94771234567"));
    }

    @Test
    void isValidContact_rejectsBadFormats() {
        assertFalse(SecurityUtil.isValidContact("123456"));
        assertFalse(SecurityUtil.isValidContact("07712345678")); // too long
        assertFalse(SecurityUtil.isValidContact(null));
    }

    @Test
    void isValidEmail_optionalButValidatedWhenPresent() {
        assertTrue(SecurityUtil.isValidEmail(null));      // optional
        assertTrue(SecurityUtil.isValidEmail(""));        // optional
        assertTrue(SecurityUtil.isValidEmail("a@b.com")); // valid
        assertFalse(SecurityUtil.isValidEmail("not-an-email"));
    }

    @Test
    void sanitize_escapesHtmlSpecialCharacters() {
        String result = SecurityUtil.sanitize("<script>alert('x')</script>");
        assertFalse(result.contains("<"));
        assertFalse(result.contains(">"));
        assertTrue(result.contains("&lt;script&gt;"));
    }
}
