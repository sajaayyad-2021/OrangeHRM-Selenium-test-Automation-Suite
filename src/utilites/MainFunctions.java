package utilites;

import java.io.File;
import java.time.Duration;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import POM.POMlogin;
import controller.loginCtrl;
import controller.PIMCtrl;
import controller.leaveCtrl;
import controller.LogoutCtrl;

public class MainFunctions {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String baseUrl;

    private static final String LOGIN_PATH = "/web/index.php/auth/login";

    // Allow navigation to login page when true
    public boolean allowLoginNavigation = false;

    public MainFunctions(WebDriver driver, Config config) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        this.baseUrl = config.getBaseURL();
    }

    // =============================================================
    // SAFE NAVIGATION LAYER
    // =============================================================
    private void navigateTo(String path) {
        driver.get(baseUrl + path);
        System.out.println("[navigate] → " + (baseUrl + path));
    }



   


    // =============================================================
    // LOGIN (WITH LOGOUT)
    // =============================================================
    public void performLogin(Config config) {

        allowLoginNavigation = true;
        navigateTo(LOGIN_PATH);
        allowLoginNavigation = false;

        POMlogin.waitForLoginPage(driver);

        loginCtrl.fillUsername(driver, config.getAuth().getUserName());
        loginCtrl.fillPassword(driver, config.getAuth().getPassWord());
        loginCtrl.clickLogin(driver);

        if (loginCtrl.waitForDashboard(driver, 5)) {
            performLogout(driver, wait);
        }
    }

    // =============================================================
    // LOGIN WITHOUT LOGOUT — used before PIM/Leave suite
    // =============================================================
    public void performLoginWithoutLogout(Config config) {

        try {
            // NEW FIX: Clear session cookies before login
            driver.manage().deleteAllCookies();
            Thread.sleep(800);

            String loginURL = config.getBaseURL() + LOGIN_PATH;

            driver.get(loginURL);

            // Wait for login page ONLY if we are NOT already logged in
            if (!driver.getCurrentUrl().contains("/dashboard")) {
                POMlogin.waitForLoginPage(driver);
                loginCtrl.fillUsername(driver, config.getAuth().getUserName());
                loginCtrl.fillPassword(driver, config.getAuth().getPassWord());
                loginCtrl.clickLogin(driver);
            }

            // Always wait for dashboard after login
            loginCtrl.waitForDashboard(driver, 10);

        } catch (Exception e) {
            System.out.println("[LOGIN] ERROR: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // =============================================================
    // STATE CHECKERS
    // =============================================================
    public boolean isDashboard() { return POMlogin.isDashboard(driver); }
    public boolean hasInvalidCredentialsError() { return POMlogin.isInvalidCredentials(driver); }
    public boolean hasRequiredValidation() { return POMlogin.requiredMessages(driver).size() >= 1; }
    public boolean isOnLoginPage() { return driver.getCurrentUrl().contains("/auth/login"); }
    public String getCurrentURL() { return driver.getCurrentUrl(); }

    // =============================================================
    // LOGOUT
    // =============================================================
    public static void performLogout(WebDriver driver, WebDriverWait wait) {
        try {
            LogoutCtrl.openUserMenu(driver, wait);
            LogoutCtrl.clickLogout(driver, wait);
        } catch (Exception ignored) {}
    }

    // =============================================================
    // FILE DELETE
    // =============================================================
    public static void deleteFiles(String folderPath) {
        try {
            File folder = new File(folderPath);
            if (!folder.exists()) return;

            for (File f : folder.listFiles()) {
                if (f.isFile()) f.delete();
            }
        } catch (Exception ignored) {}
    }

    // =============================================================
    // PIM ACTIONS
    // =============================================================
    public void performAddEmployee(Config cfg) {
        try {
            PIMCtrl.openPIMpage(driver);
            PIMCtrl.clickAddEmployee(driver);

            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//h6[text()='Add Employee']")
            ));

            Thread.sleep(800);

            String first = cfg.getDefaults().getFirstName();
            String middle = cfg.getDefaults().getMiddleName();
            String last = cfg.getDefaults().getLastName();

            if (first != null) PIMCtrl.fillfirstname(driver, first);
            if (middle != null) PIMCtrl.fillmiddlename(driver, middle);
            if (last != null) PIMCtrl.filllastname(driver, last);

            String empId = CustomFunction.generateRandomEmployeeId();
            PIMCtrl.fillemplyeeId(driver, empId);

            driver.findElement(By.xpath("//button[normalize-space()='Save']")).click();
            Thread.sleep(2000);

        } catch (Exception e) {
            System.err.println("[PIM] Add employee failed: " + e.getMessage());
        }
    }

    public void performSearchEmployee(Config cfg) {
        try {
            PIMCtrl.openPIMpage(driver);

            String type = cfg.getDefaults().getFirstName();
            String value = cfg.getDefaults().getLastName();

            if ("name".equalsIgnoreCase(type))
                PIMCtrl.fillEmployeeName(driver, value);
            else if ("id".equalsIgnoreCase(type))
                PIMCtrl.fillEmployeeId(driver, value);

            PIMCtrl.clickSearchButton(driver);
            Thread.sleep(2000);

        } catch (Exception e) {
            System.err.println("[PIM] Search failed:" + e.getMessage());
        }
    }

    // =============================================================
    // LEAVE ACTIONS
    // =============================================================
    public void performLeaveSearch(Config cfg) {
    	System.out.println("==== Entering LeaveSuite ====");

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            leaveCtrl.openLeaveList(driver, wait);

            if (cfg.getLeaveSearch().isResetFilters()) {
                leaveCtrl.clickReset(driver, wait);
                Thread.sleep(1000);
            }

            String from = cfg.getLeaveSearch().getFromDate();
            String to = cfg.getLeaveSearch().getToDate();
            String name = cfg.getLeaveSearch().getEmployeeName();
            String status = cfg.getLeaveSearch().getStatus();
            String type = cfg.getLeaveSearch().getLeaveType();
            String subUnit = cfg.getLeaveSearch().getSubUnit();

            if (from != null && !from.isEmpty())
                leaveCtrl.fillFromDate(driver, wait, from);

            if (to != null && !to.isEmpty())
                leaveCtrl.fillToDate(driver, wait, to);

            if (name != null && !name.isEmpty())
                leaveCtrl.fillEmployeeName(driver, wait, name);

            if (status != null && !status.isEmpty())
                leaveCtrl.selectStatus(driver, wait, status);

            if (type != null && !type.isEmpty())
                leaveCtrl.selectLeaveType(driver, wait, type);

            if (subUnit != null && !subUnit.isEmpty())
                leaveCtrl.selectSubUnit(driver, wait, subUnit);

            leaveCtrl.clickSearch(driver, wait);
            Thread.sleep(2000);

        } catch (Exception e) {
            System.err.println("[LEAVE] Error:" + e.getMessage());
        }
    }
}
