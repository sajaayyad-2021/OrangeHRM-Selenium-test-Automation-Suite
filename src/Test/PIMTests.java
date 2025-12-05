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
public class PIMTests extends BaseTemplate {

    MainFunctions mf;
    ResultChecker resultCheck = new ResultChecker();
    private static ExtentReports extent;
    private ExtentTest currentTest;

    String[] testsList = null;
    String activeTest = null;

    @Test
    public void PIMSuite() throws IOException, InterruptedException {

        // ===================== Skip if no CLI arg =====================
        if (testNmaes_pim == null || testNmaes_pim.trim().isEmpty()) {
            throw new SkipException("Skipping PIMTests — no -testNmaes_pim argument provided.");
        }

        extent = ExtentManager.getInstance();
        String className = this.getClass().getSimpleName();

        // ===================== Parse argument =====================
        testNmaes_pim = testNmaes_pim.trim();

        if ("ALL".equalsIgnoreCase(testNmaes_pim)) {
            testsList = discoverTestCases(className);
        } else {
            testsList = Arrays.stream(testNmaes_pim.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toArray(String[]::new);
        }

        // ===================== No tests found =====================
        if (testsList == null || testsList.length == 0) {
            throw new SkipException("No PIM test cases found.");
        }

        // ===================== LOGIN ONCE =====================
        Config loginCfg = loadthisTestConfig("LoginTests", "TC_LOG_001_validLogin");
        mf = new MainFunctions(driver, loginCfg);
        mf.performLoginWithoutLogout(loginCfg);  
        System.out.println("[PIMSuite] Logged in successfully");

        // ===================== Test Loop =====================
        for (String tc : testsList) {

            activeTest = tc;
            addCurrentTestMthod(activeTest);

            currentTest = extent.createTest(activeTest);
            currentTest.assignCategory("Regression");
            currentTest.assignCategory("PIM");
            currentTest.info("Starting test: " + activeTest);

            MainFunctions.deleteFiles(actualPath(className, tc));
            MainFunctions.deleteFiles(diffPath(className, tc));

            try {
                Config cfg = loadthisTestConfig(className, tc);
                mf = new MainFunctions(driver, cfg);

                general(cfg, className, tc);

                currentTest.pass("Test completed");

            } catch (Throwable e) {
                currentTest.fail("Exception occurred: " + e.getMessage());
                currentTest.fail(e);
            }
        }

        extent.flush();
    }


    // =================================================================
    // GENERAL PIM ACTION
    // =================================================================
    private void general(Config cfg, String className, String testCaseName) {

        try {
            currentTest.info("Executing test: " + testCaseName);

            String actionType = determineActionType(testCaseName);
            currentTest.info("Action type: " + actionType);

            if (actionType.equals("addEmployee")) {
                mf.performAddEmployee(cfg);
            } else if (actionType.equals("searchEmployee")) {
                mf.performSearchEmployee(cfg);
            }

            String actualResult = mf.getCurrentURL();
            currentTest.info("Actual Result: " + actualResult);

            saveDataArtifacts(className, testCaseName, actualResult);

        } catch (Exception e) {
            currentTest.fail("Exception: " + e.getMessage());
        }
    }

    private String determineActionType(String testName) {

        String lower = testName.toLowerCase();

        if (lower.contains("addemployee"))
            return "addEmployee";
        if (lower.contains("search"))
            return "searchEmployee";

        return "unknown";
    }


    // =================================================================
    // AUTO DISCOVERY
    // =================================================================
    private String[] discoverTestCases(String className) {

        try {
            String root = "artifacts/TestCases/" + className;
            File dir = new File(root);

            if (!dir.exists() || !dir.isDirectory())
                return new String[0];

            List<String> list = new ArrayList<>();

            for (File f : dir.listFiles(File::isDirectory)) {
                File input = new File(f, "Input/input.json");
                if (input.exists()) list.add(f.getName());
            }

            Collections.sort(list);
            return list.toArray(new String[0]);

        } catch (Exception e) {
            e.printStackTrace();
            return new String[0];
        }
    }


    // =================================================================
    // ARTIFACT MANAGEMENT
    // =================================================================
    private void saveDataArtifacts(String className, String testName, String actualData) {
        try {
            String baseName = "baseline.txt";

            String actualFile = actualPath(className, testName) + baseName;
            String expectedFile = expectedPath(className, testName) + baseName;
            String diffFile = diffPath(className, testName) + "baseline_diff.txt";

            CustomFunction.writeTextFile(actualFile, actualData);

            File expected = new File(expectedFile);

            if (!expected.exists() || expected.length() == 0) {
                currentTest.warning("Expected baseline file not found.");
                CustomFunction.appendToFile("Expected missing!", diffFile);
                CustomFunction.appendToFile("Actual: " + actualData, diffFile);
                currentTest.fail("Expected baseline missing.");
                return;
            }

            String expectedText = Files.readString(Paths.get(expectedFile)).trim();
            String actualText = actualData.trim();
            boolean match = expectedText.equals(actualText);

            CustomFunction.appendToFile("Expected: " + expectedText, diffFile);
            CustomFunction.appendToFile("Actual  : " + actualText, diffFile);
            CustomFunction.appendToFile("Result  : " + (match ? "PASS" : "FAIL"), diffFile);

            currentTest.info(MarkupHelper.createCodeBlock(
                    "EXPECTED:\n" + expectedText +
                            "\n\nACTUAL:\n" + actualText +
                            "\n\nRESULT: " + (match ? "PASS" : "FAIL")
            ));

            if (match) currentTest.pass("✓ Actual matches Expected");
            else currentTest.fail("✗ Baseline mismatch");

        } catch (Exception ex) {
            currentTest.fail("Artifact save failed: " + ex.getMessage());
        }
    }
}
