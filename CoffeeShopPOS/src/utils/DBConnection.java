package utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBConnection {
  
    private static final String URL = "jdbc:mysql://localhost:3306/pos_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "Azerty123@2003";
    
   
    private static final List<Connection> connectionPool = new ArrayList<>();
    private static final int MAX_POOL_SIZE = 10;
    
    static {
        initializePool();
    }
    
    private static void initializePool() {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL JDBC Driver loaded successfully");
            
            // Test initial connection
            Connection testConn = createNewConnection();
            releaseConnection(testConn);
            System.out.println("Database connection test: SUCCESS");
            
            // Initialize pool with connections
            for (int i = 0; i < 3; i++) { // Reduced to 3 for stability
                connectionPool.add(createNewConnection());
            }
            System.out.println("Connection pool initialized with " + connectionPool.size() + " connections");
            
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
            throw new RuntimeException("Failed to load MySQL JDBC Driver", e);
        } catch (SQLException e) {
            System.err.println("Failed to initialize connection pool: " + e.getMessage());
            
            // Don't throw exception - allow application to start in demo mode
            System.err.println("Application will run in demo mode without database");
        }
    }
    
    private static Connection createNewConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            return conn;
        } catch (SQLException e) {
            System.err.println("Failed to create new database connection:");
            System.err.println("URL: " + URL);
            System.err.println("User: " + USER);
            System.err.println("Error: " + e.getMessage());
            throw e;
        }
    }
    
    public static Connection getConnection() throws SQLException {
        synchronized (connectionPool) {
            if (!connectionPool.isEmpty()) {
                Connection conn = connectionPool.remove(0);
                // Test if connection is still valid
                try {
                    if (conn != null && !conn.isClosed() && conn.isValid(2)) {
                        return conn;
                    }
                } catch (SQLException e) {
                    // Connection is invalid, create a new one
                }
            }
            
            // If pool is empty or connections are invalid, create new one
            return createNewConnection();
        }
    }
    
    public static void releaseConnection(@SuppressWarnings("exports") Connection connection) {
        if (connection == null) return;
        
        synchronized (connectionPool) {
            try {
                if (!connection.isClosed() && connectionPool.size() < MAX_POOL_SIZE) {
                    // Reset connection state before returning to pool
                    if (!connection.getAutoCommit()) {
                        connection.setAutoCommit(true);
                    }
                    connectionPool.add(connection);
                } else {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error releasing connection: " + e.getMessage());
                try {
                    if (!connection.isClosed()) {
                        connection.close();
                    }
                } catch (SQLException ex) {
                    // Ignore close error
                }
            }
        }
    }
    
    // Utility method for safe closure
    @SuppressWarnings("exports")
	public static void closeResources(Connection conn, PreparedStatement pst, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
            if (conn != null) releaseConnection(conn);
        } catch (SQLException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }
    
    /**
     * Test database connection (for debugging)
     */
    public static boolean testConnection() {
        Connection conn = null;
        try {
            conn = getConnection();
            System.out.println("Database connection test: SUCCESS");
            return true;
        } catch (SQLException e) {
            System.err.println("Database connection test: FAILED - " + e.getMessage());
            return false;
        } finally {
            releaseConnection(conn);
        }
    }
    
    /**
     * Check if database is available
     */
    public static boolean isDatabaseAvailable() {
        return !connectionPool.isEmpty() || testConnection();
    }
}