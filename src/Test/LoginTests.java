package Test;

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
import utilites.ResultChecker;

@Listeners(reporting.ExtentTestNGITestListener.class)
public class LoginTests extends BaseTemplate {

    MainFunctions mf;
    ResultChecker resultCheck = new ResultChecker();
    private static ExtentReports extent;
    private ExtentTest currentTest;

    String[] testsList = null;
    String activeTest = null;


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
                System.err.println("[ERROR] No login test cases found under: artifacts/TestCases/" + className);
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

            MainFunctions.deleteFiles(actualPath(className, testcase));
            MainFunctions.deleteFiles(diffPath(className, testcase));

            try {
                Config cfg = loadthisTestConfig(className, testcase);
                mf = new MainFunctions(driver, cfg);

                general(cfg, className, testcase);

                currentTest.pass("Test completed");

            } catch (Throwable e) {
                currentTest.fail("Exception: " + e.getMessage());
                currentTest.fail(e);
                e.printStackTrace();
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
    // LOGIN ACTION
    // ============================================================
    private void general(Config cfg, String className, String testCaseName) {

        try {
            currentTest.info("Executing: " + testCaseName);

            mf.performLogin(cfg);

            String actualResult = getActualLoginResult();
            currentTest.info("Actual Result: " + actualResult);

            saveDataArtifacts(className, testCaseName, actualResult);

        } catch (Exception e) {
            currentTest.fail("Exception: " + e.getMessage());
        }
    }


    // ============================================================
    // DETERMINE ACTUAL RESULT
    // ============================================================
    private String getActualLoginResult() {

        try {
            if (mf.isDashboard()) return "LOGIN_SUCCESS_DASHBOARD";
            if (mf.hasInvalidCredentialsError()) return "INVALID_CREDENTIALS_ERROR";
            if (mf.hasRequiredValidation()) return "REQUIRED_FIELD_VALIDATION";
            if (mf.isOnLoginPage()) return "REMAINED_ON_LOGIN_PAGE";

            return mf.getCurrentURL();

        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }


    // ============================================================
    // ARTIFACTS
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
}
