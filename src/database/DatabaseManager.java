package database;

import java.sql.*;
import java.util.Properties;


public class DatabaseManager {
    
 
	private static final String DB_URL = "jdbc:mysql://localhost:3306/OrangeHRM_DB";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root1234"; 
    
    private static DatabaseManager instance;
    private Connection connection;
    
   
    private DatabaseManager() {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Connection properties
            Properties props = new Properties();
            props.setProperty("user", DB_USER);
            props.setProperty("password", DB_PASSWORD);
            props.setProperty("useSSL", "false");
            props.setProperty("serverTimezone", "UTC");
            props.setProperty("allowPublicKeyRetrieval", "true");
            
            // Establish connection
            connection = DriverManager.getConnection(DB_URL, props);
            System.out.println(" Database connection established");
            
        } catch (ClassNotFoundException e) {
            System.err.println(" MySQL JDBC Driver not found!");
            e.printStackTrace();
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        } catch (SQLException e) {
            System.err.println("✗ Failed to connect to database!");
            System.err.println("  URL: " + DB_URL);
            System.err.println("  User: " + DB_USER);
            e.printStackTrace();
            throw new RuntimeException("Database connection failed", e);
        }
    }
    
    
    //  Get  instance
     
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    
     // Get database connection
     
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
    
   
    // Close database connection
     
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✓ Database connection closed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
   
   //Test if connection is alive
     
    public boolean testConnection() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}