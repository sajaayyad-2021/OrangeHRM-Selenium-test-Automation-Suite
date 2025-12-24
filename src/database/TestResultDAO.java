package database;

import java.sql.*;
import org.json.JSONObject;

/**
 * Data Access Object for test results
 * Handles all database operations
 */
public class TestResultDAO {
    
    private DatabaseManager dbManager;
    
    public TestResultDAO() {
        this.dbManager = DatabaseManager.getInstance();//singl connection
    }
    
    
    public int startExecution(String executionName, String browser, String baseUrl, String metadata) {
        String sql = "INSERT INTO test_executions (execution_name, browser, base_url, execution_metadata) " +
                     "VALUES (?, ?, ?, ?)";
        
        
        
        //fill the placeholders
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, executionName);//first
           stmt.setString(2, browser);//seconed
            stmt.setString(3, baseUrl);//third
            stmt.setString(4, metadata);//fourth
           
            
            //excuite the QUERY
            stmt.executeUpdate();
            //GET THE EXECUTION_ID  
            ResultSet result = stmt.getGeneratedKeys();
            if (result.next()) {
                return result.getInt(1);
                //return ID of this execuation
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    //End of Execution
    
    public void completeExecution(int executionId, int passed, int failed, int skipped, String status) {
        String sql = "UPDATE test_executions SET " +
                     "completed_at = CURRENT_TIMESTAMP, " +
                     "passed_tests = ?, failed_tests = ?, skipped_tests = ?, " +
                     "total_tests = ?, status = ? " +
                     "WHERE execution_id = ?";
        /*what happen inside data base 
    * UPDATE test_executions SET 
    completed_at = CURRENT_TIMESTAMP,
    passed_tests = 15,
    failed_tests = 2,
    skipped_tests = 0,
    total_tests = 17,
    status = 'COMPLETED'
WHERE execution_id = 5;*/
        
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, passed);
            stmt.setInt(2, failed);
            stmt.setInt(3, skipped);
            stmt.setInt(4, passed + failed + skipped);
            stmt.setString(5, status);
            stmt.setInt(6, executionId);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // TEST CASE 
    //here to get the Id of testcase(suite,testcasename)
    public int getTestCaseId(String suiteName, String testCaseName) {
        String sql = "SELECT tc.test_case_id FROM test_cases tc " +
                     "JOIN test_suites ts ON tc.suite_id = ts.suite_id " +
                     "WHERE ts.suite_name = ? AND tc.test_case_name = ?";
        //search about test and suite name by testcase id
        //EXAMPLE:int testCaseId = dao.getTestCaseId("LoginTests", "TC_LOG_001_validLogin");
        
        /*
         * SELECT tc.test_case_id 
           FROM test_cases tc
           JOIN test_suites ts ON tc.suite_id = ts.suite_id
           WHERE ts.suite_name = 'LoginTests' 
           AND tc.test_case_name = 'TC_LOG_001_validLogin';
  */
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, suiteName);
            pstmt.setString(2, testCaseName);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("test_case_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    //READ CONFIGRATION
    public JSONObject getTestConfiguration(int testCaseId) {
        String sql = "SELECT config_data FROM test_configurations " +
                     "WHERE test_case_id = ? AND is_active = TRUE " +
                     "ORDER BY version DESC LIMIT 1";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, testCaseId);
            
            ResultSet result = pstmt.executeQuery();
            if (result.next()) {
                String jsonData = result.getString("config_data");
                return new JSONObject(jsonData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    //EXAMPLE:SELECT config_data 
   /* FROM test_configurations
    WHERE test_case_id = 1 
      AND is_active = TRUE
    ORDER BY version DESC 
    LIMIT 1;*/
    
    // ==================== BASELINE ====================
    //for read expected baseline -expected value
    public String getExpectedBaseline(int testCaseId, String baselineType) {
        String sql = "SELECT expected_value FROM baselines " +
                     "WHERE test_case_id = ? AND baseline_type = ? AND is_active = TRUE " +
                     "ORDER BY version DESC LIMIT 1";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, testCaseId);
            pstmt.setString(2, baselineType);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("expected_value");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    /*SELECT expected_value 
FROM baselines
WHERE test_case_id = 1 
  AND baseline_type = 'url'
  AND is_active = TRUE
ORDER BY version DESC 
LIMIT 1;*/
    
    
    //for get the baseline ID 
    //SAME EXPECTED IDEA BUT INSETED OF TO RETURN EXPECTED VALUE RETURN BASLINEID
    public int getBaselineIdForTest(int testCaseId, String baselineType) {
        String sql = "SELECT baseline_id FROM baselines " +
                     "WHERE test_case_id = ? AND baseline_type = ? AND is_active = TRUE " +
                     "ORDER BY version DESC LIMIT 1";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, testCaseId);
            pstmt.setString(2, baselineType);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("baseline_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    public int saveBaseline(int testCaseId, String baselineType, String expectedValue, String description, String createdBy) {
        String sql = "INSERT INTO baselines (test_case_id, baseline_type, expected_value, description, created_by) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, testCaseId);
            pstmt.setString(2, baselineType);
            pstmt.setString(3, expectedValue);
            pstmt.setString(4, description);
            pstmt.setString(5, createdBy);
            
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    /*SQL QUERY:INSERT INTO baselines (test_case_id, baseline_type, expected_value, description, created_by)
     VALUES (1, 'url', 'https://...', 'Expected dashboard URL...', 'Raghad');*/
    
    // ==================== TEST RESULT ====================
    /*
     * INSERT INTO test_results 
       (execution_id, test_case_id, baseline_id, actual_value, expected_value, 
       comparison_result, error_message, duration_ms, screenshot_path, completed_at)
       VALUES (5, 1, 1, 'https://...', 'https://...', 'PASS', NULL, 8465, NULL, CURRENT_TIMESTAMP);*/
    //----------------------------------------------------------
    public int saveTestResult(int executionId, int testCaseId, int baselineId, 
                              String actualValue, String expectedValue, 
                              String comparisonResult, String errorMessage, 
                              long durationMs, String screenshotPath) {
        String sql = "INSERT INTO test_results " +
                     "(execution_id, test_case_id, baseline_id, actual_value, expected_value, " +
                     "comparison_result, error_message, duration_ms, screenshot_path, completed_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, executionId);
            pstmt.setInt(2, testCaseId);
            
            if (baselineId > 0) {
                pstmt.setInt(3, baselineId);
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            
            pstmt.setString(4, actualValue);
            pstmt.setString(5, expectedValue);
            pstmt.setString(6, comparisonResult);
            pstmt.setString(7, errorMessage);
            pstmt.setLong(8, durationMs);
            pstmt.setString(9, screenshotPath);
            
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    /*INSERT INTO baseline_comparisons 
    (result_id, baseline_id, expected_value, actual_value, diff_details, match_status)
    VALUES (25, 1, 'https://...', 'https://...', 'EXPECTED: ...\nACTUAL: ...\nRESULT: PASS', 'MATCH');*/
    public void saveBaselineComparison(int resultId, int baselineId, 
                                       String expectedValue, String actualValue, 
                                       String diffDetails, String matchStatus) {
        String sql = "INSERT INTO baseline_comparisons " +
                     "(result_id, baseline_id, expected_value, actual_value, diff_details, match_status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, resultId);
            pstmt.setInt(2, baselineId);
            pstmt.setString(3, expectedValue);
            pstmt.setString(4, actualValue);
            pstmt.setString(5, diffDetails);
            pstmt.setString(6, matchStatus);
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void saveTestStep(int resultId, int stepNumber, String stepDescription, 
                             String status, String stepData, String screenshotPath) {
        String sql = "INSERT INTO test_steps " +
                     "(result_id, step_number, step_description, status, step_data, screenshot_path) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, resultId);
            pstmt.setInt(2, stepNumber);
            pstmt.setString(3, stepDescription);
            pstmt.setString(4, status);
            pstmt.setString(5, stepData);
            pstmt.setString(6, screenshotPath);
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


/*
 * // 1️- Start Execution
TestResultDAO dao = new TestResultDAO();
int executionId = dao.startExecution("Regression_20241224", "chrome", "https://...", "{}");

// 2️- For Each Test Case:
for (String testCaseName : testCases) {
    
    // Get test_case_id
    int testCaseId = dao.getTestCaseId("LoginTests", testCaseName);
    
    // Read Configuration
    JSONObject config = dao.getTestConfiguration(testCaseId);
    
    // Read Expected Baseline
    String expectedUrl = dao.getExpectedBaseline(testCaseId, "url");
    int baselineId = dao.getBaselineIdForTest(testCaseId, "url");
    
    // Execute the Test
    String actualUrl = performTest(config);  // example
    
    // Compare
    String result = actualUrl.equals(expectedUrl) ? "PASS" : "FAIL";
    
    // Save Result
    int resultId = dao.saveTestResult(
        executionId, testCaseId, baselineId,
        actualUrl, expectedUrl, result,
        null, 8465, null
    );
    
    // Save Comparison Details
    dao.saveBaselineComparison(
        resultId, baselineId,
        expectedUrl, actualUrl,
        "EXPECTED: " + expectedUrl + "\nACTUAL: " + actualUrl,
        actualUrl.equals(expectedUrl) ? "MATCH" : "MISMATCH"
    );
    
    // (Optional) Save Steps
    dao.saveTestStep(resultId, 1, "Navigate to login", "PASS", null, null);
    dao.saveTestStep(resultId, 2, "Fill credentials", "PASS", null, null);
}

// 3️-Complete Execution
dao.completeExecution(executionId, passedTests, failedTests, 0, "COMPLETED");*/


/* Flow explan:
Start - Create execution record
Loop - For each test: get ID → read config → get expected value → run test → compare → save results
End - Update execution with final counts (passed/failed/total)
*/