package testbase;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import com.aventstack.extentreports.ExtentReports;

// Allure imports
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import io.qameta.allure.Step;

import database.DatabaseManager;
import reporting.ExtentManager;
import utilites.Config;
import utilites.CustomFunction;
import utilites.DatabaseResultChecker;

public class BaseTemplate {

    // -------------------- WebDriver & Wait --------------------
    public static WebDriver driver;
    protected static WebDriverWait wait;

    // -------------------- Database Integration --------------------
    protected static DatabaseResultChecker dbChecker;
    protected static int executionId = -1;
    private static boolean useDatabaseMode = true;
    
    // -------------------- Config / CLI properties --------------------
    protected static String jsoninput;      
    protected static String browser;        
    protected static String url = "";       
    protected static String xml;

    // -------------------- ROOT PATH  --------------------
    public static String SuitePath;
    protected static ExtentReports extent;
    protected static String XML_DIR;
    
    // Flags
    public static String testNmaes_login;
    public static String testNmaes_pim; 
    public static String testNmaes_leave;

    // -------------------- Test tracking --------------------
    protected static final Set<String> CURRENT_TEST_NAME = new LinkedHashSet<>();

    // ========================================================================
    // Static initializer
    static {
        initFromSystemProperties();
    }

    private static void initFromSystemProperties() {
        SuitePath       = System.getProperty("suitePath", "artifacts");
        browser         = System.getProperty("browser", "chrome");
        url             = System.getProperty("url", "");
        xml             = System.getProperty("xmlFile", "testng.xml");
        
        testNmaes_login = System.getProperty("testNmaes_login", "ALL");
        testNmaes_pim   = System.getProperty("testNmaes_pim", "ALL");
        testNmaes_leave = System.getProperty("testNmaes_leave", "ALL");
        
        jsoninput       = System.getProperty("cfg", SuitePath + "\\TestCases\\Tests\\Input\\input.json");
        
        System.out.println("=== BaseTemplate Configuration ===");
        System.out.println("SuitePath: " + SuitePath);
        System.out.println("Browser: " + browser);
        System.out.println("URL: " + url);
        System.out.println("XML File: " + xml);
        System.out.println("testNmaes_login: " + testNmaes_login);
        System.out.println("testNmaes_pim: " + testNmaes_pim);
        System.out.println("testNmaes_leave: " + testNmaes_leave);
        System.out.println("Database Mode: " + (useDatabaseMode ? "ENABLED" : "DISABLED"));
        System.out.println("==================================");
    }

    public static void Setargs(String[] args) {
        jsoninput       = getArgs(args, "-cfg",             System.getProperty("cfg", "artifacts\\TestCases\\Tests\\Input\\input.json"));
        SuitePath       = getArgs(args, "-out",             System.getProperty("suitePath", "artifacts"));
        browser         = getArgs(args, "-browser",         System.getProperty("browser", "chrome"));
        
        testNmaes_login = getArgs(args, "-testNmaes_login", System.getProperty("testNmaes_login", "ALL"));
        testNmaes_pim   = getArgs(args, "-testNmaes_pim",   System.getProperty("testNmaes_pim", "ALL"));
        testNmaes_leave = getArgs(args, "-testNmaes_leave", System.getProperty("testNmaes_leave", "ALL"));
        
        xml             = getArgs(args, "-xml",             System.getProperty("xmlFile", "testng.xml"));
        url             = getArgs(args, "-url",             System.getProperty("url", ""));
    }

    private static String getArgs(String[] args, String key, String def) {
        if (args == null)
            return def;

        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals(key)) {
                return args[i + 1];
            }
        }
        return def;
    }

    // Path helpers
    public static String testCaseRoot(String className, String testName) {
        return SuitePath + "\\TestCases\\" + className + "\\" + testName + "\\";
    }
    
    public static String inputPath(String className, String testName) {
        return testCaseRoot(className, testName) + "Input\\";
    }

    public static String actualPath(String className, String testName) {
        return testCaseRoot(className, testName) + "Actual\\";
    }

    public static String expectedPath(String className, String testName) {
        return testCaseRoot(className, testName) + "Expected\\";
    }

    public static String diffPath(String className, String testName) {
        return testCaseRoot(className, testName) + "Diff\\";
    }

    protected static void addCurrentTestMthod(String method) {
        CURRENT_TEST_NAME.add(method);
    }

    protected static String currentTestMethod() {
        String last = null;
        for (String m : CURRENT_TEST_NAME)
            last = m;
        return last == null ? "unknownTest" : last;
    }

    // Config
    protected static Config loadthisTestConfig(String className) {
        String configPath = SuitePath
                          + "\\TestCases\\"
                          + className
                          + "\\Input\\input.json";

        return CustomFunction.loadConfig(configPath);
    }
    
    protected static Config loadthisTestConfig(String className, String testName) {
        String configPath = testCaseRoot(className, testName) + "Input\\input.json";
        return CustomFunction.loadConfig(configPath);
    }

    // ========================================================================
    // TestNG Lifecycle with Allure
    // ========================================================================
    
    @BeforeSuite
    @Step("Setting up test suite")
    public void beforeSuiteSetup() {
        System.out.println("\n╔═══════════════════════════════════════════════════════╗");
        System.out.println("║          TEST EXECUTION STARTING                      ║");
        if (useDatabaseMode) {
            System.out.println("║          MODE: DATABASE-DRIVEN                        ║");
        } else {
            System.out.println("║          MODE: FILE-BASED                             ║");
        }
        System.out.println("╚═══════════════════════════════════════════════════════╝\n");
        
        // Allure parameters
        Allure.parameter("Browser", browser);
        Allure.parameter("Database Mode", useDatabaseMode);
        Allure.parameter("Suite Path", SuitePath);
        
        // 1. Initialize Database (if enabled)
        if (useDatabaseMode) {
            Allure.step("Initializing database connection", () -> {
                try {
                    DatabaseManager dbManager = DatabaseManager.getInstance();
                    if (dbManager.testConnection()) {
                        System.out.println("✓ Database connection established");
                        Allure.step("Database connection: SUCCESS");
                    } else {
                        System.err.println("✗ Database connection failed!");
                        System.err.println("⚠ Falling back to FILE-BASED mode");
                        useDatabaseMode = false;
                        Allure.step("Database connection: FAILED - Falling back to FILE mode");
                    }
                } catch (Exception e) {
                    System.err.println("✗ Database initialization error: " + e.getMessage());
                    System.err.println("⚠ Falling back to FILE-BASED mode");
                    useDatabaseMode = false;
                    Allure.addAttachment("Database Error", e.getMessage());
                }
            });
        }
        
        // 2. Initialize WebDriver
        Allure.step("Initializing WebDriver", () -> {
            driver = new ChromeDriver();
            driver.manage().window().maximize();
            wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            System.out.println("✓ WebDriver initialized");
        });
        
        // 3. Initialize ExtentReports
        Allure.step("Initializing ExtentReports", () -> {
            extent = ExtentManager.getInstance();
            System.out.println("✓ ExtentReports initialized");
        });
        
        // 4. Initialize Database Result Checker (if database mode)
        if (useDatabaseMode) {
            Allure.step("Starting database execution tracking", () -> {
                dbChecker = new DatabaseResultChecker();
                
                String cliArgs = String.format(
                    "-out %s -browser %s -testNmaes_login %s -testNmaes_pim %s -testNmaes_leave %s -url %s",
                    SuitePath, browser, testNmaes_login, testNmaes_pim, testNmaes_leave, url
                );
                
                executionId = dbChecker.initializeExecution(browser, url, cliArgs);
                System.out.println("✓ Database execution tracking started (ID: " + executionId + ")");
                Allure.parameter("Execution ID", executionId);
            });
        }
        
        // 5. Navigate to URL
        if (url != null && !url.isEmpty()) {
            Allure.step("Navigating to: " + url, () -> {
                driver.get(url);
                System.out.println("✓ Navigated to: " + url);
            });
        }
        
        System.out.println("\n═════════════════════════════════════════════════════════\n");
    }

    @BeforeMethod
    public void getMethodname(Method method) {
        addCurrentTestMthod(method.getName());
        
        // Start timing (if database mode)
        if (useDatabaseMode && dbChecker != null) {
            dbChecker.startTest();
        }
        
        // Allure test name
        Allure.getLifecycle().updateTestCase(testResult -> {
            testResult.setName(method.getName());
        });
        
        System.out.println("\n▶ Starting test: " + method.getName());
    }

    @AfterMethod
    public void afterMethod(ITestResult result) {
        
        if (result.getStatus() == ITestResult.FAILURE) {
            // Capture screenshot on failure
            attachScreenshot("Failure Screenshot");
            
            // Attach error details to Allure
            Allure.addAttachment("Error Message", result.getThrowable().getMessage());
            Allure.addAttachment("Stack Trace", getStackTrace(result.getThrowable()));
        }
    }

    @AfterSuite
    @Step("Tearing down test suite")
    public void afterSuiteCleanup() {
        System.out.println("\n╔═══════════════════════════════════════════════════════╗");
        System.out.println("║              TEST EXECUTION COMPLETED                 ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝\n");
        
        System.out.println("Test Results Summary:");
        System.out.println("  Check ExtentReports and Allure for detailed statistics");
        
        // Finalize database (if enabled)
        if (useDatabaseMode && dbChecker != null && executionId > 0) {
            Allure.step("Finalizing database execution", () -> {
                dbChecker.finalizeExecution(0, 0, 0);
                System.out.println("✓ Database execution record finalized");
            });
            
            Allure.step("Closing database connection", () -> {
                try {
                    DatabaseManager.getInstance().closeConnection();
                    System.out.println("✓ Database connection closed");
                } catch (Exception e) {
                    System.err.println("Warning: Error closing database: " + e.getMessage());
                }
            });
        }
        
        // Quit WebDriver
        Allure.step("Closing WebDriver", () -> {
            try {
                if (driver != null) {
                    driver.quit();
                    System.out.println("✓ WebDriver closed");
                }
            } catch (Exception e) {
                System.err.println("Warning: Error closing WebDriver: " + e.getMessage());
            }
        });
        
        System.out.println("\n═════════════════════════════════════════════════════════\n");
    }
    
    // ========================================================================
    // Allure Helper Methods
    // ========================================================================
    
    /**
     * Attach screenshot to Allure report
     */
    @Attachment(value = "{name}", type = "image/png")
    public byte[] attachScreenshot(String name) {
        if (driver != null) {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        }
        return new byte[0];
    }
    
    /**
     * Attach text to Allure report
     */
    @Attachment(value = "{name}", type = "text/plain")
    public String attachText(String name, String content) {
        return content;
    }
    
    /**
     * Get stack trace as string
     */
    private String getStackTrace(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
    
    // ========================================================================
    // Helper methods
    // ========================================================================
    
    public static boolean isDatabaseMode() {
        return useDatabaseMode;
    }
    
    public static DatabaseResultChecker getDbChecker() {
        return dbChecker;
    }
}