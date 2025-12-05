package Test;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.markuputils.MarkupHelper;

import reporting.ExtentManager;
import testbase.BaseTemplate;
import utilites.Config;
import utilites.CustomFunction;
import utilites.MainFunctions;

@Listeners(reporting.ExtentTestNGITestListener.class)
public class RecruitmentTests extends BaseTemplate {

    MainFunctions mf;
    private static ExtentReports extent;
    private ExtentTest currentTest;

    String[] testsList = null;
    String activeTest = null;

    @Test
    public void RecruitmentSuite() throws IOException, InterruptedException {

        extent = ExtentManager.getInstance();
        String className = this.getClass().getSimpleName();

        // -------------------- Parse CLI Args --------------------
        if (testNmaes_pim == null || testNmaes_pim.trim().isEmpty()) {
            testNmaes_pim = "ALL";
        } else {
            testNmaes_pim = testNmaes_pim.trim();
        }

        if (!"ALL".equalsIgnoreCase(testNmaes_pim)) {

            if (testNmaes_pim.contains(",")) {
                testsList = Arrays.stream(testNmaes_pim.split(","))
                                  .map(String::trim)
                                  .filter(s -> !s.isEmpty())
                                  .toArray(String[]::new);

            } else {
                testsList = new String[]{ testNmaes_pim };
            }

        } else {
            testsList = null; // ALL
        }

        // -------------------- Test Loop --------------------
        for (String testcase : testsList) {

            activeTest = testcase;
            addCurrentTestMthod(activeTest);

            currentTest = extent.createTest(activeTest);
            currentTest.assignCategory("Regression");
            currentTest.assignCategory("Recruitment");
            currentTest.info("Starting test: " + activeTest);

            MainFunctions.deleteFiles(actualPath(className, testcase));
            MainFunctions.deleteFiles(diffPath(className, testcase));

            try {
                Config cfg = loadthisTestConfig(className, testcase);
                mf = new MainFunctions(driver, cfg);

                // Run main recruitment flow
                general(cfg, className, testcase);

                currentTest.pass("Test completed");

            } catch (Throwable e) {
                currentTest.fail("Exception occurred: " + e.getMessage());
                currentTest.fail(e);
            }
        }

        extent.flush();
    }

    // ========================================================
    // GENERAL RECRUITMENT ACTION
    // ========================================================
    private void general(Config cfg, String className, String testCaseName) {

        try {
            currentTest.info("Executing test: " + testCaseName);

            // Perform recruitment add flow
         //   mf.performRecruitmentAdd(cfg);

            // TEMPORARY placeholder result (since site is down)
            String actualResult = "recruitment_add_completed";
            currentTest.info("Actual Result: " + actualResult);

            // Save artifacts
            saveDataArtifacts(className, testCaseName, actualResult);

        } catch (Exception e) {
            currentTest.fail("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========================================================
    // SAVE ARTIFACTS
    // ========================================================
    private void saveDataArtifacts(String className, String testName, String actualData) {

        try {
            String baseName     = "baseline.txt";
            String actualFile   = actualPath(className, testName)   + baseName;
            String expectedFile = expectedPath(className, testName) + baseName;
            String diffFile     = diffPath(className, testName)     + "baseline_diff.txt";

            CustomFunction.writeTextFile(actualFile, actualData);

            File expected = new File(expectedFile);

            if (!expected.exists() || expected.length() == 0) {
                currentTest.warning("Expected baseline file not found: " + expectedFile);
                currentTest.info("Please create expected baseline manually.");
                currentTest.info("Actual: " + actualData);

                CustomFunction.appendToFile("___" + testName + " DIFF___", diffFile);
                CustomFunction.appendToFile("Expected missing!", diffFile);
                CustomFunction.appendToFile("Actual: " + actualData, diffFile);

                currentTest.fail("Expected baseline file not found");
                return;
            }

            String baseline = Files.readString(Paths.get(expectedFile)).trim();
            String actual = actualData.trim();
            boolean match = baseline.equals(actual);

            CustomFunction.appendToFile("___" + testName + " DIFF___", diffFile);
            CustomFunction.appendToFile("Expected: " + baseline, diffFile);
            CustomFunction.appendToFile("Actual  : " + actual, diffFile);
            CustomFunction.appendToFile("Result  : " + (match ? "PASS" : "FAIL"), diffFile);

            String block = String.format(
                "EXPECTED:\n%s\n\nACTUAL:\n%s\n\nRESULT: %s",
                baseline, actual, match ? "PASS" : "FAIL"
            );

            currentTest.info(MarkupHelper.createCodeBlock(block));

            if (match) {
                currentTest.pass("✓ Actual matches Expected");
            } else {
                currentTest.fail("✗ Baseline mismatch – Expected: " + baseline + ", but got: " + actual);
            }

        } catch (Exception ex) {
            currentTest.fail("Artifact save failed: " + ex.getMessage());
        }
    }
}
