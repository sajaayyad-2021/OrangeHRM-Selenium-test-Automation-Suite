package test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import org.testng.SkipException;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.aventstack.extentreports.*;
import com.aventstack.extentreports.markuputils.MarkupHelper;

import reporting.ExtentManager;
import testbase.BaseTemplate;
import utilites.Config;
import utilites.CustomFunction;
import utilites.MainFunctions;

@Listeners(reporting.ExtentTestNGITestListener.class)
public class LeaveTests extends BaseTemplate {

    MainFunctions mf;
    private static ExtentReports extent;
    private ExtentTest currentTest;

    String[] testsList = null;
    String activeTest = null;
    
    private static final String SUITE_NAME = "LeaveTests";

    @Test
    public void LeaveSuite() throws IOException, InterruptedException {

        if (testNmaes_leave == null || testNmaes_leave.trim().isEmpty()) {
            throw new SkipException("Skipping LeaveTests — no -testNmaes_leave argument provided.");
        }

        extent = ExtentManager.getInstance();
        String className = this.getClass().getSimpleName();

        testNmaes_leave = testNmaes_leave.trim();

        if ("ALL".equalsIgnoreCase(testNmaes_leave)) {
            testsList = discoverTestCases(className);
        } else {
            testsList = Arrays.stream(testNmaes_leave.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toArray(String[]::new);
        }

        // ===================== LOGIN ONCE =====================
        Config loginCfg;
        
        if (isDatabaseMode()) {
            System.out.println("[DATABASE MODE] Loading login config from database");
            loginCfg = getDbChecker().getTestConfiguration("LoginTests", "TC_LOG_001_validLogin");
        } else {
            System.out.println("[FILE MODE] Loading login config from file");
            loginCfg = loadthisTestConfig("LoginTests", "TC_LOG_001_validLogin");
        }
        
        mf = new MainFunctions(driver, loginCfg);
        mf.performLoginWithoutLogout(loginCfg);

        System.out.println("[LeaveSuite] Logged in successfully");

        // ===================== Test Loop =====================
        for (String tc : testsList) {

            activeTest = tc;
            addCurrentTestMthod(activeTest);

            currentTest = extent.createTest(activeTest);
            currentTest.assignCategory("Regression");
            currentTest.assignCategory("Leave");
            currentTest.info("Starting test: " + activeTest);

            // Delete old artifacts (if file mode)
            if (!isDatabaseMode()) {
                MainFunctions.deleteFiles(actualPath(className, tc));
                MainFunctions.deleteFiles(diffPath(className, tc));
            }

            try {
                // ========================================================
                // LOAD CONFIG
                // ========================================================
                Config cfg;
                
                if (isDatabaseMode()) {
                    // NEW: Load from database
                    System.out.println("[DATABASE MODE] Loading config from database");
                    cfg = getDbChecker().getTestConfiguration(SUITE_NAME, tc);
                    
                    if (cfg == null) {
                        currentTest.fail("Configuration not found in database for: " + tc);
                        continue;
                    }
                } else {
                    // OLD: Load from file
                    System.out.println("[FILE MODE] Loading config from file");
                    cfg = loadthisTestConfig(className, tc);
                }

                mf = new MainFunctions(driver, cfg);

                // ========================================================
                // EXECUTE TEST
                // ========================================================
                executeLeaveTest(cfg, className, tc);

                currentTest.pass("Test completed");

            } catch (Throwable e) {
                currentTest.fail("Exception occurred: " + e.getMessage());
                currentTest.fail(e);
                e.printStackTrace();
                
                // Save error to database if enabled
                if (isDatabaseMode()) {
                    getDbChecker().saveTestError(
                        SUITE_NAME, 
                        tc, 
                        e.getMessage(), 
                        getStackTrace(e), 
                        0, 
                        null
                    );
                }
            }
        }

        extent.flush();
    }

    // =================================================================
    // EXECUTE LEAVE TEST
    // =================================================================
    private void executeLeaveTest(Config cfg, String className, String testCaseName) {

        try {
            currentTest.info("Executing: " + testCaseName);

            mf.performLeaveSearch(cfg);

            String actualResult = mf.getCurrentURL();
            currentTest.info("Actual: " + actualResult);

            // ========================================================
            // SAVE & COMPARE
            // ========================================================
            if (isDatabaseMode()) {
                // NEW: Database comparison
                compareWithDatabase(testCaseName, actualResult);
            } else {
                // OLD: File comparison
                saveDataArtifacts(className, testCaseName, actualResult);
            }

        } catch (Exception e) {
            currentTest.fail("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

   
    // DATABASE 
    
    private void compareWithDatabase(String testCaseName, String actualResult) {
        try {
            // Determine baseline type
            String baselineType = "url"; // Leave tests usually check URLs
            
            currentTest.info("Baseline Type: " + baselineType);
            
            // Compare with database
            String result = getDbChecker().compareWithBaseline(
                SUITE_NAME,
                testCaseName,
                actualResult,
                baselineType
            );

            // Log to ExtentReport
            currentTest.info(MarkupHelper.createCodeBlock(
                "TEST CASE: " + testCaseName + "\n" +
                "ACTUAL: " + actualResult + "\n" +
                "RESULT: " + result
            ));

            if ("PASS".equals(result)) {
                currentTest.pass("✓ Actual matches expected");
            } else if ("FAIL".equals(result)) {
                currentTest.fail("✗ Baseline mismatch");
            } else {
                currentTest.warning("⚠ " + result);
            }

        } catch (Exception ex) {
            currentTest.fail("Database comparison failed: " + ex.getMessage());
        }
    }

    // =================================================================
    // AUTO DISCOVERY
    // =================================================================
    private String[] discoverTestCases(String className) {
        try {
            String root = "artifacts/TestCases/" + className;
            File dir = new File(root);
            if (!dir.exists()) return new String[0];

            List<String> list = new ArrayList<>();
            for (File f : dir.listFiles(File::isDirectory)) {
                File input = new File(f, "Input/input.json");
                if (input.exists()) list.add(f.getName());
            }

            Collections.sort(list);
            return list.toArray(new String[0]);

        } catch (Exception e) {
            return new String[0];
        }
    }

    // =================================================================
    // FILE COMPARISON (OLD - KEPT FOR BACKWARD COMPATIBILITY)
    // =================================================================
    private void saveDataArtifacts(String className, String testName, String actualData) {

        try {
            String file = "baseline.txt";

            String actualFile = actualPath(className, testName) + file;
            String expectedFile = expectedPath(className, testName) + file;
            String diffFile = diffPath(className, testName) + "baseline_diff.txt";

            CustomFunction.writeTextFile(actualFile, actualData);

            File expected = new File(expectedFile);

            if (!expected.exists()) {
                currentTest.warning("Expected baseline missing: " + expectedFile);
                CustomFunction.appendToFile("Expected missing!\nActual: " + actualData, diffFile);
                currentTest.fail("Missing baseline");
                return;
            }

            String baseline = Files.readString(Paths.get(expectedFile)).trim();
            boolean match = baseline.equals(actualData.trim());

            CustomFunction.appendToFile(
                    "EXPECTED: " + baseline +
                            "\nACTUAL: " + actualData +
                            "\nRESULT: " + (match ? "PASS" : "FAIL"),
                    diffFile );

            currentTest.info(MarkupHelper.createCodeBlock(
                    "EXPECTED:\n" + baseline +
                            "\n\nACTUAL:\n" + actualData +
                            "\n\nRESULT: " + (match ? "PASS" : "FAIL")
            ));

            if (match) currentTest.pass("✓ Actual matches expected");
            else currentTest.fail("✗ Baseline mismatch");

        } catch (Exception ex) {
            currentTest.fail("Artifact error: " + ex.getMessage());
        }
    }
    
    // =================================================================
    // HELPER: Get Stack Trace
    // =================================================================
    private String getStackTrace(Throwable e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}
