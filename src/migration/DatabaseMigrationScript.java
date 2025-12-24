package migration;

import java.io.File;
import java.nio.file.Files;
import java.sql.*;
import com.google.gson.*;
import database.DatabaseManager;

/**
 * Migration script to move from file-based to database
 * 
 * USAGE: Run this once after creating database tables
 */
public class DatabaseMigrationScript {

    private static final String BASE_PATH = "artifacts/TestCases/";
    private static final String[] SUITES = {"LoginTests", "PIMTests", "LeaveTests"};
    
    private static DatabaseManager dbManager;
    private static Connection conn;
    private static Gson gson = new Gson();
    
    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════════╗");
        System.out.println("║     ORANGEHRM DATABASE MIGRATION SCRIPT               ║");
        System.out.println("║     File-Based → Database-Driven                      ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝\n");
        
        try {
            // 1. Initialize database
            System.out.println("[1/4] Initializing database connection...");
            dbManager = DatabaseManager.getInstance();
            conn = dbManager.getConnection();
            
            if (conn == null || conn.isClosed()) {
                System.err.println("✗ Failed to connect to database!");
                System.err.println("Make sure MySQL is running and credentials are correct");
                return;
            }
            System.out.println("✓ Database connected\n");
            
            // 2. Migrate test suites
            System.out.println("[2/4] Migrating test suites...");
            for (String suite : SUITES) {
                migrateTestSuite(suite);
            }
            System.out.println("✓ Test suites migrated\n");
            
            // 3. Migrate test data
            System.out.println("[3/4] Migrating test data...");
            for (String suite : SUITES) {
                migrateTestData(suite);
            }
            System.out.println("✓ Test data migrated\n");
            
            // 4. Verify
            System.out.println("[4/4] Verifying migration...");
            verifyMigration();
            System.out.println("✓ Migration verified\n");
            
            System.out.println("╔═══════════════════════════════════════════════════════╗");
            System.out.println("║          MIGRATION COMPLETED SUCCESSFULLY!            ║");
            System.out.println("╚═══════════════════════════════════════════════════════╝\n");
            
            System.out.println("Next steps :)");
            System.out.println("1. Run a test: -testNmaes_login TC_TESTNAME");
            System.out.println("2. Check database: SELECT * FROM test_results;");
            
        } catch (Exception e) {
            System.err.println("\n✗ Migration failed!");
            e.printStackTrace();
        } finally {
            dbManager.closeConnection();
        }
    }
    
    /**
     * Migrate test suite
     */
    private static void migrateTestSuite(String suiteName) throws SQLException {
        String checkSql = "SELECT suite_id FROM test_suites WHERE suite_name = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkSql);
        checkStmt.setString(1, suiteName);
        ResultSet rs = checkStmt.executeQuery();
        
        if (rs.next()) {
            System.out.println("   Suite already exists: " + suiteName);
            return;
        }
        
        String insertSql = "INSERT INTO test_suites (suite_name, description) VALUES (?, ?)";
        PreparedStatement stmt = conn.prepareStatement(insertSql);
        stmt.setString(1, suiteName);
        stmt.setString(2, "Test suite for " + suiteName);
        stmt.executeUpdate();
        
        System.out.println("  ✓ Migrated suite: " + suiteName);
    }
    
    /**
     * Migrate test data
     */
    private static void migrateTestData(String suiteName) throws Exception {
        File suiteDir = new File(BASE_PATH + suiteName);
        if (!suiteDir.exists() || !suiteDir.isDirectory()) {
            System.out.println("   No folder found for: " + suiteName);
            return;
        }
        
        int suiteId = getSuiteId(suiteName);
        if (suiteId == -1) {
            System.err.println("  ✗ Suite not found: " + suiteName);
            return;
        }
        
        File[] testCaseDirs = suiteDir.listFiles();
        if (testCaseDirs == null) return;
        
        for (File testCaseDir : testCaseDirs) {
            if (!testCaseDir.isDirectory()) continue;
            
            String testCaseName = testCaseDir.getName();
            System.out.println("  Processing: " + testCaseName);
            
            try {
                // 1. Create test case
                int testCaseId = createTestCase(suiteId, testCaseName);
                
                // 2. Migrate input.json
                File inputFile = new File(testCaseDir, "Input/input.json");
                if (inputFile.exists()) {
                    migrateConfiguration(testCaseId, inputFile);
                    System.out.println("    ✓ Configuration migrated");
                } else {
                    System.out.println("    ⓘ No input.json found");
                }
                
                // 3. Migrate baseline.txt
                File baselineFile = new File(testCaseDir, "Expected/baseline.txt");
                if (baselineFile.exists()) {
                    migrateBaseline(testCaseId, testCaseName, baselineFile);
                    System.out.println(" Baseline migrated");
                } else {
                    System.out.println(" No baseline.txt found");
                }
                
            } catch (Exception e) {
                System.err.println("  Error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Get suite ID
     */
    private static int getSuiteId(String suiteName) throws SQLException {
        String sql = "SELECT suite_id FROM test_suites WHERE suite_name = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, suiteName);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            return rs.getInt("suite_id");
        }
        return -1;
    }
    
    /**
     * Create test case
     */
    private static int createTestCase(int suiteId, String testCaseName) throws SQLException {
        // Check if exists
        String checkSql = "SELECT test_case_id FROM test_cases WHERE suite_id = ? AND test_case_name = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkSql);
        checkStmt.setInt(1, suiteId);
        checkStmt.setString(2, testCaseName);
        ResultSet rs = checkStmt.executeQuery();
        
        if (rs.next()) {
            return rs.getInt("test_case_id");
        }
        
        // Insert new
        String insertSql = "INSERT INTO test_cases (suite_id, test_case_name, description) VALUES (?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
        stmt.setInt(1, suiteId);
        stmt.setString(2, testCaseName);
        stmt.setString(3, "Auto-migrated: " + testCaseName);
        stmt.executeUpdate();
        
        ResultSet keys = stmt.getGeneratedKeys();
        if (keys.next()) {
            return keys.getInt(1);
        }
        return -1;
    }
    
    /**
     * Migrate configuration
     */
    private static void migrateConfiguration(int testCaseId, File inputFile) throws Exception {
        String jsonContent = new String(Files.readAllBytes(inputFile.toPath()));
        JsonObject jsonObj = gson.fromJson(jsonContent, JsonObject.class);
        
        String baseUrl = jsonObj.has("baseURL") ? jsonObj.get("baseURL").getAsString() : "";
        
        // Check if exists
        String checkSql = "SELECT config_id FROM test_configurations WHERE test_case_id = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkSql);
        checkStmt.setInt(1, testCaseId);
        ResultSet rs = checkStmt.executeQuery();
        
        if (rs.next()) {
            return; // Already exists
        }
        
        // Insert
        String insertSql = "INSERT INTO test_configurations (test_case_id, base_url, config_data) VALUES (?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(insertSql);
        stmt.setInt(1, testCaseId);
        stmt.setString(2, baseUrl);
        stmt.setString(3, jsonContent);
        stmt.executeUpdate();
    }
    
    /**
     * Migrate baseline
     */
    private static void migrateBaseline(int testCaseId, String testCaseName, File baselineFile) throws Exception {
        String expectedValue = new String(Files.readAllBytes(baselineFile.toPath())).trim();
        
        String baselineType = determineBaselineType(testCaseName, expectedValue);
        
        // Check if exists
        String checkSql = "SELECT baseline_id FROM baselines WHERE test_case_id = ? AND baseline_type = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkSql);
        checkStmt.setInt(1, testCaseId);
        checkStmt.setString(2, baselineType);
        ResultSet rs = checkStmt.executeQuery();
        
        if (rs.next()) {
            return; // Already exists
        }
        
        // Insert
        String insertSql = "INSERT INTO baselines (test_case_id, baseline_type, expected_value, description, created_by) " +
                           "VALUES (?, ?, ?, ?, 'Migration')";
        PreparedStatement stmt = conn.prepareStatement(insertSql);
        stmt.setInt(1, testCaseId);
        stmt.setString(2, baselineType);
        stmt.setString(3, expectedValue);
        stmt.setString(4, "Migrated from Expected/baseline.txt");
        stmt.executeUpdate();
    }
    
    /**
     * Determine baseline type
     */
    private static String determineBaselineType(String testCaseName, String content) {
        if (content.startsWith("http")) {
            return "url";
        } else if (testCaseName.contains("empty") || testCaseName.contains("validation") || content.contains("REQUIRED")) {
            return "validation_message";
        } else if (testCaseName.contains("invalid") || content.contains("INVALID")) {
            return "alert_message";
        } else if (testCaseName.contains("search")) {
            return "search_result";
        } else {
            return "text";
        }
    }
    
    /**
     * Verify migration
     */
    private static void verifyMigration() throws SQLException {
        System.out.println("\n  Database Statistics:");
        
        // Count suites
        String sql1 = "SELECT COUNT(*) as count FROM test_suites";
        Statement stmt1 = conn.createStatement();
        ResultSet rs1 = stmt1.executeQuery(sql1);
        if (rs1.next()) {
            System.out.println("    Test Suites:       " + rs1.getInt("count"));
        }
        
        // Count test cases
        String sql2 = "SELECT COUNT(*) as count FROM test_cases";
        Statement stmt2 = conn.createStatement();
        ResultSet rs2 = stmt2.executeQuery(sql2);
        if (rs2.next()) {
            System.out.println("    Test Cases:        " + rs2.getInt("count"));
        }
        
        // Count configurations
        String sql3 = "SELECT COUNT(*) as count FROM test_configurations";
        Statement stmt3 = conn.createStatement();
        ResultSet rs3 = stmt3.executeQuery(sql3);
        if (rs3.next()) {
            System.out.println("    Configurations:    " + rs3.getInt("count"));
        }
        
        // Count baselines
        String sql4 = "SELECT COUNT(*) as count FROM baselines";
        Statement stmt4 = conn.createStatement();
        ResultSet rs4 = stmt4.executeQuery(sql4);
        if (rs4.next()) {
            System.out.println("    Baselines:         " + rs4.getInt("count"));
        }
        
        System.out.println();
    }
}
/*AFTER MARGE:
 * MySQL Database: orangehrm_test_db
├── test_suites (3 records)
├── test_cases (19 records)
├── test_configurations (19 records)
├── baselines (19 records)
├── test_executions (grows with each run)
├── test_results (grows with each run)
├── baseline_comparisons (grows with each run)
└── test_steps (optional, grows with each run)*/
