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
public class LoginTests extends BaseTemplate {

    MainFunctions mf;
    private static ExtentReports extent;
    private ExtentTest currentTest;

    String[] testsList = null;
    String activeTest = null;
    
    private static final String SUITE_NAME = "LoginTests";

    // ============================================================
    // LOGIN SUITE 
    // ============================================================
    @Test
    public void LoginSuite() throws IOException, InterruptedException {

        if (testNmaes_login == null || testNmaes_login.trim().isEmpty()) {
            throw new SkipException("Skipping LoginTests — no -testNmaes_login argument provided.");
        }

        extent = ExtentManager.getInstance();
        String className = this.getClass().getSimpleName();

        testNmaes_login = testNmaes_login.trim();

        // ========================================================
        // AUTO DISCOVERY (ALL)
        // ========================================================
        if ("ALL".equalsIgnoreCase(testNmaes_login)) {
            testsList = discoverTestCases(className);

            if (testsList == null || testsList.length == 0) {
                System.err.println("[ERROR] No login test cases found");
                throw new SkipException("Skipping LoginTests — No test folders found.");
            }

            System.out.println("[LoginTests] Discovered tests: " + Arrays.toString(testsList));

        } else {
            // ========================================================
            // SPECIFIC TESTS FROM CLI
            // ========================================================
            testsList = Arrays.stream(testNmaes_login.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toArray(String[]::new);

            System.out.println("[LoginTests] Running selected tests: " + Arrays.toString(testsList));
        }

        // ========================================================
        // TEST LOOP
        // ========================================================
        for (String testcase : testsList) {

            activeTest = testcase;
            addCurrentTestMthod(activeTest);

            currentTest = extent.createTest(activeTest);
            currentTest.assignCategory("Regression");
            currentTest.assignCategory("Login");
            currentTest.info("Starting test: " + activeTest);

            // Delete old artifacts (if file mode)
            if (!isDatabaseMode()) {
                MainFunctions.deleteFiles(actualPath(className, testcase));
                MainFunctions.deleteFiles(diffPath(className, testcase));
            }

            try {
                // ========================================================
                // LOAD CONFIG
                // ========================================================
                Config cfg;
                
                if (isDatabaseMode()) {
                    // NEW: Load from database
                    System.out.println("[DATABASE MODE] Loading config from database");
                    cfg = getDbChecker().getTestConfiguration(SUITE_NAME, testcase);
                    
                    if (cfg == null) {
                        currentTest.fail("Configuration not found in database for: " + testcase);
                        continue;
                    }
                } else {
                    // OLD: Load from file
                    System.out.println("[FILE MODE] Loading config from file");
                    cfg = loadthisTestConfig(className, testcase);
                }

                mf = new MainFunctions(driver, cfg);

                // ========================================================
                // EXECUTE TEST
                // ========================================================
                executeLoginTest(cfg, className, testcase);

                currentTest.pass("Test completed");

            } catch (Throwable e) {
                currentTest.fail("Exception: " + e.getMessage());
                currentTest.fail(e);
                e.printStackTrace();
                
                // Save error to database if enabled
                if (isDatabaseMode()) {
                    getDbChecker().saveTestError(
                        SUITE_NAME, 
                        testcase, 
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

    // ============================================================
    // DISCOVERY
    // ============================================================
    private String[] discoverTestCases(String className) {
        try {
            String path = "artifacts/TestCases/" + className;
            File root = new File(path);

            if (!root.exists()) return new String[0];

            List<String> found = new ArrayList<>();

            for (File dir : root.listFiles(File::isDirectory)) {
                File input = new File(dir, "Input/input.json");
                if (input.exists()) {
                    found.add(dir.getName());
                    System.out.println("[LoginTests] Found: " + dir.getName());
                }
            }

            Collections.sort(found);
            return found.toArray(new String[0]);

        } catch (Exception e) {
            e.printStackTrace();
            return new String[0];
        }
    }

    // ============================================================
    // EXECUTE LOGIN TEST
    // ============================================================
    private void executeLoginTest(Config cfg, String className, String testCaseName) {

        try {
            currentTest.info("Executing: " + testCaseName);

            // Perform login
            mf.performLogin(cfg);

            // Get actual result
            String actualResult = getActualLoginResult();
            currentTest.info("Actual Result: " + actualResult);

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

    // ============================================================
    // DATABASE COMPARISON (NEW)
    // ============================================================
    private void compareWithDatabase(String testCaseName, String actualResult) {
        try {
            // Determine baseline type based on result
            String baselineType = determineBaselineType(actualResult);
            
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
                currentTest.pass("✓ Actual matches Expected");
            } else if ("FAIL".equals(result)) {
                currentTest.fail("✗ Baseline mismatch");
            } else {
                currentTest.warning("⚠ " + result);
            }

        } catch (Exception ex) {
            currentTest.fail("Database comparison failed: " + ex.getMessage());
        }
    }

    // ============================================================
    // DETERMINE BASELINE TYPE
    // ============================================================
    private String determineBaselineType(String actualResult) {
        if (actualResult.startsWith("http")) {
            return "url";
        } else if (actualResult.contains("VALIDATION") || actualResult.contains("REQUIRED")) {
            return "validation_message";
        } else if (actualResult.contains("ERROR") || actualResult.contains("INVALID")) {
            return "alert_message";
        } else {
            return "text";
        }
    }

    // ============================================================
    // GET ACTUAL RESULT
    // ============================================================
    private String getActualLoginResult() {
        try {
            if (mf.isDashboard()) return mf.getCurrentURL();
            if (mf.hasInvalidCredentialsError()) return "INVALID_CREDENTIALS_ERROR";
            if (mf.hasRequiredValidation()) return "REQUIRED_FIELD_VALIDATION";
            if (mf.isOnLoginPage()) return "REMAINED_ON_LOGIN_PAGE";

            return mf.getCurrentURL();

        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    // ============================================================
    // FILE COMPARISON (OLD - KEPT FOR BACKWARD COMPATIBILITY)
    // ============================================================
    private void saveDataArtifacts(String className, String testName, String actualData) {
        try {
            String file = "baseline.txt";

            String actualFile = actualPath(className, testName) + file;
            String expectedFile = expectedPath(className, testName) + file;
            String diffFile = diffPath(className, testName) + "baseline_diff.txt";

            CustomFunction.writeTextFile(actualFile, actualData);

            File expected = new File(expectedFile);

            if (!expected.exists()) {
                currentTest.warning("Expected baseline missing!");
                CustomFunction.appendToFile("Expected missing!\nActual: " + actualData, diffFile);
                currentTest.fail("Baseline missing. Create: " + expectedFile);
                return;
            }

            String baseline = Files.readString(Paths.get(expectedFile)).trim();
            boolean match = baseline.equals(actualData.trim());

            CustomFunction.appendToFile(
                    "EXPECTED: " + baseline +
                    "\nACTUAL: " + actualData +
                    "\nRESULT: " + (match ? "PASS" : "FAIL"),
                    diffFile
            );

            currentTest.info(MarkupHelper.createCodeBlock(
                    "EXPECTED:\n" + baseline +
                    "\n\nACTUAL:\n" + actualData +
                    "\n\nRESULT: " + (match ? "PASS" : "FAIL")
            ));

            if (match) currentTest.pass("✓ Actual matches Expected");
            else currentTest.fail("✗ Baseline mismatch");

        } catch (Exception ex) {
            currentTest.fail("Artifact save failed: " + ex.getMessage());
        }
    }
    
    // ============================================================
    // HELPER: Get Stack Trace
    // ============================================================
    private String getStackTrace(Throwable e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}