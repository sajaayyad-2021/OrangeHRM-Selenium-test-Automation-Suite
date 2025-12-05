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

@Listeners(reporting.ExtentTestNGITestListener.class)
public class LeaveTests extends BaseTemplate {

    MainFunctions mf;
    private static ExtentReports extent;
    private ExtentTest currentTest;

    String[] testsList = null;
    String activeTest = null;

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
            testsList = testNmaes_leave.split(",");
        }

        // FIXED: Correct login before leave suite
        Config loginCfg = loadthisTestConfig("LoginTests", "TC_LOG_001_validLogin");
        mf = new MainFunctions(driver, loginCfg);
        mf.performLoginWithoutLogout(loginCfg);

        System.out.println("[LeaveSuite] Logged in successfully");

        for (String tc : testsList) {

            activeTest = tc;
            addCurrentTestMthod(activeTest);

            currentTest = extent.createTest(activeTest);
            currentTest.assignCategory("Regression");
            currentTest.assignCategory("Leave");

            Config cfg = loadthisTestConfig(className, tc);
            mf = new MainFunctions(driver, cfg);

            general(cfg, className, tc);

            currentTest.pass("Test completed");
        }

        extent.flush();
    }

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

    private void general(Config cfg, String className, String testCaseName) {

        try {
            currentTest.info("Executing: " + testCaseName);

            mf.performLeaveSearch(cfg);

            String actualResult = mf.getCurrentURL();
            currentTest.info("Actual: " + actualResult);

            saveDataArtifacts(className, testCaseName, actualResult);

        } catch (Exception e) {
            currentTest.fail("Exception: " + e.getMessage());
        }
    }

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
}
