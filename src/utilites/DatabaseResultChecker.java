package utilites;

import database.TestResultDAO;
import org.json.JSONObject;

/**
 * Database-based result checker
 * Replaces file-based ResultChecker
 */
public class DatabaseResultChecker {
    
    private TestResultDAO dao;
    private int currentExecutionId = -1;
    private int currentResultId = -1;
    private long testStartTime = 0;
    
    public DatabaseResultChecker() {
        this.dao = new TestResultDAO();
    }
    
    /**
     * Initialize execution session
     * Call in BaseTemplate @BeforeSuite
     */
    public int initializeExecution(String browser, String baseUrl, String cliArgs) {
        String executionName = "Regression_" + System.currentTimeMillis();
        
        JSONObject metadata = new JSONObject();
        metadata.put("browser", browser);
        metadata.put("url", baseUrl);
        metadata.put("cliArgs", cliArgs);
        metadata.put("testNmaes_login", testbase.BaseTemplate.testNmaes_login);
        metadata.put("testNmaes_pim", testbase.BaseTemplate.testNmaes_pim);
        metadata.put("testNmaes_leave", testbase.BaseTemplate.testNmaes_leave);
        
        currentExecutionId = dao.startExecution(executionName, browser, baseUrl, metadata.toString());
        
        System.out.println("═══════════════════════════════════════════");
        System.out.println("DATABASE EXECUTION STARTED - ID: " + currentExecutionId);
        System.out.println("═══════════════════════════════════════════");
        
        return currentExecutionId;
    }
    
    /**
     * Finalize execution session
     * Call in BaseTemplate @AfterSuite
     */
    public void finalizeExecution(int passed, int failed, int skipped) {
        String status = (failed == 0) ? "COMPLETED" : "COMPLETED_WITH_FAILURES";
        dao.completeExecution(currentExecutionId, passed, failed, skipped, status);
        
        System.out.println("═══════════════════════════════════════════");
        System.out.println("EXECUTION COMPLETED - ID: " + currentExecutionId);
        System.out.println("Passed: " + passed + " | Failed: " + failed + " | Skipped: " + skipped);
        System.out.println("═══════════════════════════════════════════");
    }
    
    /**
     * Start timing for a test
     */
    public void startTest() {
        testStartTime = System.currentTimeMillis();
    }
    
    /**
     * Get elapsed time
     */
    private long getTestDuration() {
        return System.currentTimeMillis() - testStartTime;
    }
    
    /**
     * Compare with baseline - MAIN METHOD
     * 
     * @param suiteName - "LoginTests"
     * @param testCaseName - "TC_LOG_001_validLogin"
     * @param actualValue - actual URL or result
     * @param baselineType - "url", "validation", etc.
     * @return "PASS" or "FAIL"
     */
    public String compareWithBaseline(String suiteName, String testCaseName, 
                                      String actualValue, String baselineType) {
        
        System.out.println("\n--- Comparing Baseline: " + testCaseName + " ---");
        
        // 1. Get test case ID
        int testCaseId = dao.getTestCaseId(suiteName, testCaseName);
        if (testCaseId == -1) {
            String errorMsg = "Test case not found: " + suiteName + "/" + testCaseName;
            System.err.println("ERROR: " + errorMsg);
            saveTestError(suiteName, testCaseName, errorMsg, "", getTestDuration(), null);
            return "ERROR";
        }
        
        // 2. Get expected baseline
        String expectedValue = dao.getExpectedBaseline(testCaseId, baselineType);
        if (expectedValue == null) {
            String errorMsg = "Baseline not found: " + testCaseName + " (type: " + baselineType + ")";
            System.err.println("ERROR: " + errorMsg);
            System.out.println("HINT: Run migration script or create baseline manually");
            saveTestError(suiteName, testCaseName, errorMsg, "", getTestDuration(), null);
            return "ERROR";
        }
        
        // 3. Compare
        boolean matches = actualValue.trim().equals(expectedValue.trim());
        String comparisonResult = matches ? "PASS" : "FAIL";
        
        // 4. Create diff
        String diffDetails = createDiffReport(testCaseName, expectedValue, actualValue, comparisonResult);
        
        // 5. Save result
        int baselineId = dao.getBaselineIdForTest(testCaseId, baselineType);
        currentResultId = dao.saveTestResult(
            currentExecutionId, 
            testCaseId, 
            baselineId,
            actualValue,
            expectedValue,
            comparisonResult,
            matches ? null : "Baseline mismatch",
            getTestDuration(),
            null
        );
        
        // 6. Save comparison
        if (baselineId > 0) {
            dao.saveBaselineComparison(
                currentResultId,
                baselineId,
                expectedValue,
                actualValue,
                diffDetails,
                matches ? "MATCH" : "MISMATCH"
            );
        }
        
        // 7. Print
        System.out.println(diffDetails);
        
        return comparisonResult;
    }
    
    /**
     * Save test error
     */
    public void saveTestError(String suiteName, String testCaseName, 
                              String errorMessage, String stackTrace, 
                              long durationMs, String screenshotPath) {
        
        int testCaseId = dao.getTestCaseId(suiteName, testCaseName);
        if (testCaseId == -1) {
            System.err.println("Cannot save error - test case not found: " + suiteName + "/" + testCaseName);
            return;
        }
        
        currentResultId = dao.saveTestResult(
            currentExecutionId,
            testCaseId,
            -1,
            null,
            null,
            "ERROR",
            errorMessage + (stackTrace.isEmpty() ? "" : "\n" + stackTrace),
            durationMs,
            screenshotPath
        );
    }
    
    /**
     * Log test step
     */
    public void logTestStep(int stepNumber, String stepDescription, String status, String stepData) {
        if (currentResultId > 0) {
            dao.saveTestStep(currentResultId, stepNumber, stepDescription, status, stepData, null);
        }
    }
    
    /**
     * Get test configuration from database
     * REPLACES: loadthisTestConfig()
     */
    public Config getTestConfiguration(String suiteName, String testCaseName) {
        int testCaseId = dao.getTestCaseId(suiteName, testCaseName);
        if (testCaseId == -1) {
            System.err.println("Test case not found: " + suiteName + "/" + testCaseName);
            return null;
        }
        
        JSONObject configJson = dao.getTestConfiguration(testCaseId);
        if (configJson == null) {
            System.err.println("Configuration not found: " + testCaseName);
            return null;
        }
        
        return jsonToConfig(configJson);
    }
    
    /**
     * Convert JSON to Config object
     */
    private Config jsonToConfig(JSONObject json) {
        Config config = new Config();
        
        // Base URL
        if (json.has("baseURL")) {
            config.setBaseURL(json.getString("baseURL"));
        }
        
        // Auth
        if (json.has("auth")) {
            JSONObject auth = json.getJSONObject("auth");
            String userName = auth.optString("userName", "");
            String passWord = auth.optString("passWord", "");
            config.setAuth(userName, passWord);
        }
        
        // Defaults (PIM)
        if (json.has("defaults")) {
            JSONObject defaults = json.getJSONObject("defaults");
            String firstName = defaults.optString("firstName", "");
            String middleName = defaults.optString("middleName", "");
            String lastName = defaults.optString("lastName", "");
            config.setDefaults(firstName, middleName, lastName);
        }
        
        // Leave Search
        if (json.has("leaveSearch")) {
            JSONObject leave = json.getJSONObject("leaveSearch");
            String fromDate = leave.optString("fromDate", "");
            String toDate = leave.optString("toDate", "");
            String employeeName = leave.optString("employeeName", "");
            String status = leave.optString("status", "");
            String leaveType = leave.optString("leaveType", "");
            String subUnit = leave.optString("subUnit", "");
            boolean resetFilters = leave.optBoolean("resetFilters", false);
            config.setLeaveSearch(fromDate, toDate, employeeName, status, leaveType, subUnit, resetFilters);
        }
        
        // Recruitment
        if (json.has("recruitment")) {
            JSONObject recruit = json.getJSONObject("recruitment");
            String candidateFirstName = recruit.optString("candidateFirstName", "");
            String candidateMiddleName = recruit.optString("candidateMiddleName", "");
            String candidateLastName = recruit.optString("candidateLastName", "");
            String vacancy = recruit.optString("vacancy", "");
            String email = recruit.optString("email", "");
            String contactNumber = recruit.optString("contactNumber", "");
            String resumePath = recruit.optString("resumePath", "");
            String keywords = recruit.optString("keywords", "");
            String dateOfApplication = recruit.optString("dateOfApplication", "");
            String notes = recruit.optString("notes", "");
            boolean consent = recruit.optBoolean("consent", false);
            
            config.setRecruitment(candidateFirstName, candidateMiddleName, candidateLastName, 
                                 vacancy, email, contactNumber, resumePath, keywords, 
                                 dateOfApplication, notes, consent);
        }
        
        return config;
    }
    
    /**
     * Create baseline (for setup)
     */
    public void createBaseline(String suiteName, String testCaseName, 
                               String baselineType, String expectedValue, 
                               String description) {
        
        int testCaseId = dao.getTestCaseId(suiteName, testCaseName);
        if (testCaseId == -1) {
            System.err.println("Cannot create baseline - test case not found: " + suiteName + "/" + testCaseName);
            return;
        }
        
        int baselineId = dao.saveBaseline(testCaseId, baselineType, expectedValue, description, "System");
        System.out.println("✓ Baseline created: " + testCaseName + " (ID: " + baselineId + ")");
    }
    
    /**
     * Create diff report
     */
    private String createDiffReport(String testCaseName, String expected, String actual, String result) {
        StringBuilder diff = new StringBuilder();
        
        diff.append("\n═══════════════════════════════════════════════════════\n");
        diff.append("TEST CASE: ").append(testCaseName).append("\n");
        diff.append("═══════════════════════════════════════════════════════\n\n");
        
        diff.append("EXPECTED:\n");
        diff.append(expected).append("\n\n");
        
        diff.append("ACTUAL:\n");
        diff.append(actual).append("\n\n");
        
        diff.append("RESULT: ").append(result).append("\n");
        
        if (!result.equals("PASS")) {
            diff.append("\n MISMATCH DETECTED\n");
        } else {
            diff.append("\n BASELINE MATCH\n");
        }
        
        diff.append("═══════════════════════════════════════════════════════\n");
        
        return diff.toString();
    }
    
    // Getters
    public int getCurrentExecutionId() {
        return currentExecutionId;
    }
    
    public int getCurrentResultId() {
        return currentResultId;
    }
}