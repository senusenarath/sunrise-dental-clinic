package lk.sunrise.dental.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * ================================================================
 * ConnectionFactory.java
 * Factory Design Pattern for Database Connections
 *
 * Design Pattern : Factory Pattern
 * Purpose        : Creates different types of DB connections
 *                  based on connection type requested
 * Package        : lk.sunrise.dental.util
 * ================================================================
 */
public class ConnectionFactory {

    // ── Connection Types ────────────────────────────────────────
    public enum ConnectionType {
        READ_ONLY,      // For SELECT queries (reports)
        READ_WRITE,     // For INSERT/UPDATE/DELETE
        TRANSACTION     // For multi-step operations
    }

    // ── Database Config ─────────────────────────────────────────
    // Loaded from app.properties (see AppConfig) instead of being
    // hardcoded, so there is one place credentials are configured.
    private static final String DRIVER;
    private static final String BASE_URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        AppConfig config = AppConfig.getInstance();
        DRIVER   = config.get("db.driver", "com.mysql.cj.jdbc.Driver");
        BASE_URL = config.get("db.url");
        USER     = config.get("db.user");
        PASSWORD = config.get("db.password");
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("[ConnectionFactory] Driver error: " + e.getMessage());
        }
    }

    /**
     * Factory Method - Creates connection based on type
     *
     * @param type ConnectionType enum value
     * @return configured Connection object
     */
    public static Connection getConnection(ConnectionType type) throws SQLException {
        Connection conn = DriverManager.getConnection(BASE_URL, USER, PASSWORD);

        switch (type) {
            case READ_ONLY -> {
                // Read-only connection for reports
                conn.setReadOnly(true);
                conn.setAutoCommit(true);
                System.out.println("[ConnectionFactory] 📖 READ_ONLY connection created");
            }
            case READ_WRITE -> {
                // Standard read-write connection
                conn.setReadOnly(false);
                conn.setAutoCommit(true);
                System.out.println("[ConnectionFactory] ✏️ READ_WRITE connection created");
            }
            case TRANSACTION -> {
                // Transaction connection - auto commit OFF
                conn.setReadOnly(false);
                conn.setAutoCommit(false);
                System.out.println("[ConnectionFactory] 🔄 TRANSACTION connection created");
            }
        }
        return conn;
    }

    /**
     * Convenience method - default READ_WRITE connection
     * Same as DBConnection.getConn()
     */
    public static Connection getConnection() throws SQLException {
        return getConnection(ConnectionType.READ_WRITE);
    }

    /**
     * Get read-only connection for reports
     */
    public static Connection getReadOnlyConnection() throws SQLException {
        return getConnection(ConnectionType.READ_ONLY);
    }

    /**
     * Get transaction connection
     */
    public static Connection getTransactionConnection() throws SQLException {
        return getConnection(ConnectionType.TRANSACTION);
    }

    /**
     * Safely close connection
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("[ConnectionFactory] Close error: " + e.getMessage());
            }
        }
    }

    /**
     * Rollback transaction safely
     */
    public static void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
                System.out.println("[ConnectionFactory] 🔄 Transaction rolled back");
            } catch (SQLException e) {
                System.err.println("[ConnectionFactory] Rollback error: " + e.getMessage());
            }
        }
    }
}