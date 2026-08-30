package lk.sunrise.dental.util;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * ================================================================
 * DBConnection.java
 * Database Connection Helper - Creates fresh connection each time
 *
 * Thin wrapper kept for the DAOs that already call DBConnection.getConn().
 * Delegates to ConnectionFactory (the Factory Method that actually
 * builds JDBC connections) so credentials are configured in exactly
 * one place - app.properties, via AppConfig.
 * ================================================================
 */
public class DBConnection {

    private DBConnection() {}

    /**
     * Get a NEW fresh connection every time.
     * Caller is responsible for closing it.
     */
    public static Connection getConn() throws SQLException {
        return ConnectionFactory.getConnection();
    }

    /**
     * Test connection on startup.
     */
    public static boolean testConnection() {
        try (Connection conn = getConn()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("[DBConnection] ✅ Database connection test passed!");
                return true;
            }
        } catch (Exception e) {
            System.err.println("[DBConnection] ❌ Connection failed: " + e.getMessage());
        }
        return false;
    }
}