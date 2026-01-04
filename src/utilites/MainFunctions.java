package utilites;

import java.io.File;
import java.time.Duration;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.qameta.allure.Step;
import io.qameta.allure.Allure;

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
    @Step("Navigating to: {path}")
    private void navigateTo(String path) {
        driver.get(baseUrl + path);
        System.out.println("[navigate] → " + (baseUrl + path));
        Allure.addAttachment("URL", baseUrl + path);
    }

    // =============================================================
    // LOGIN (WITH LOGOUT)
    // =============================================================
    @Step("Performing login with username: {config.auth.userName}")
    public void performLogin(Config config) {

        allowLoginNavigation = true;
        navigateTo(LOGIN_PATH);
        allowLoginNavigation = false;

        Allure.step("Wait for login page to load");
        POMlogin.waitForLoginPage(driver);

        Allure.step("Enter username: " + config.getAuth().getUserName(), () -> {
            loginCtrl.fillUsername(driver, config.getAuth().getUserName());
        });

        Allure.step("Enter password", () -> {
            loginCtrl.fillPassword(driver, config.getAuth().getPassWord());
        });

        Allure.step("Click login button", () -> {
            loginCtrl.clickLogin(driver);
        });

        if (loginCtrl.waitForDashboard(driver, 5)) {
            Allure.step("Dashboard loaded - performing logout");
            performLogout(driver, wait);
        }
    }

    // =============================================================
    // LOGIN WITHOUT LOGOUT — used before PIM/Leave suite
    // =============================================================
    @Step("Performing login without logout")
    public void performLoginWithoutLogout(Config config) {

        try {
            Allure.step("Clear session cookies", () -> {
                driver.manage().deleteAllCookies();
                Thread.sleep(800);
            });

            String loginURL = config.getBaseURL() + LOGIN_PATH;
            Allure.parameter("Login URL", loginURL);

            Allure.step("Navigate to login page", () -> {
                driver.get(loginURL);
            });

            // Wait for login page ONLY if we are NOT already logged in
            if (!driver.getCurrentUrl().contains("/dashboard")) {
                Allure.step("Fill login credentials", () -> {
                    POMlogin.waitForLoginPage(driver);
                    loginCtrl.fillUsername(driver, config.getAuth().getUserName());
                    loginCtrl.fillPassword(driver, config.getAuth().getPassWord());
                    loginCtrl.clickLogin(driver);
                });
            }

            // Always wait for dashboard after login
            Allure.step("Wait for dashboard");
            loginCtrl.waitForDashboard(driver, 10);

        } catch (Exception e) {
            System.out.println("[LOGIN] ERROR: " + e.getMessage());
            Allure.addAttachment("Login Error", e.getMessage());
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
    @Step("Performing logout")
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
    @Step("Adding employee: {cfg.defaults.firstName} {cfg.defaults.lastName}")
    public void performAddEmployee(Config cfg) {
        try {
            Allure.step("Open PIM page", () -> {
                PIMCtrl.openPIMpage(driver);
            });

            Allure.step("Click Add Employee button", () -> {
                PIMCtrl.clickAddEmployee(driver);
            });

            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//h6[text()='Add Employee']")
            ));

            Thread.sleep(800);

            String first = cfg.getDefaults().getFirstName();
            String middle = cfg.getDefaults().getMiddleName();
            String last = cfg.getDefaults().getLastName();

            Allure.step("Fill employee details", () -> {
                if (first != null) PIMCtrl.fillfirstname(driver, first);
                if (middle != null) PIMCtrl.fillmiddlename(driver, middle);
                if (last != null) PIMCtrl.filllastname(driver, last);
            });

            String empId = CustomFunction.generateRandomEmployeeId();
            Allure.parameter("Employee ID", empId);
            
            Allure.step("Fill employee ID: " + empId, () -> {
                PIMCtrl.fillemplyeeId(driver, empId);
            });

            Allure.step("Click Save button", () -> {
                driver.findElement(By.xpath("//button[normalize-space()='Save']")).click();
                Thread.sleep(2000);
            });

        } catch (Exception e) {
            System.err.println("[PIM] Add employee failed: " + e.getMessage());
            Allure.addAttachment("PIM Error", e.getMessage());
        }
    }

    @Step("Searching employee")
    public void performSearchEmployee(Config cfg) {
        try {
            Allure.step("Open PIM page");
            PIMCtrl.openPIMpage(driver);

            String type = cfg.getDefaults().getFirstName();
            String value = cfg.getDefaults().getLastName();

            Allure.parameter("Search Type", type);
            Allure.parameter("Search Value", value);

            if ("name".equalsIgnoreCase(type))
                PIMCtrl.fillEmployeeName(driver, value);
            else if ("id".equalsIgnoreCase(type))
                PIMCtrl.fillEmployeeId(driver, value);

            Allure.step("Click Search button");
            PIMCtrl.clickSearchButton(driver);
            Thread.sleep(2000);

        } catch (Exception e) {
            System.err.println("[PIM] Search failed:" + e.getMessage());
            Allure.addAttachment("Search Error", e.getMessage());
        }
    }

    // =============================================================
    // LEAVE ACTIONS
    // =============================================================
    @Step("Performing leave search")
    public void performLeaveSearch(Config cfg) {
        System.out.println("==== Entering LeaveSuite ====");

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            Allure.step("Open Leave List", () -> {
                leaveCtrl.openLeaveList(driver, wait);
            });

            if (cfg.getLeaveSearch().isResetFilters()) {
                Allure.step("Reset filters", () -> {
                    leaveCtrl.clickReset(driver, wait);
                    Thread.sleep(1000);
                });
            }

            String from = cfg.getLeaveSearch().getFromDate();
            String to = cfg.getLeaveSearch().getToDate();
            String name = cfg.getLeaveSearch().getEmployeeName();
            String status = cfg.getLeaveSearch().getStatus();
            String type = cfg.getLeaveSearch().getLeaveType();
            String subUnit = cfg.getLeaveSearch().getSubUnit();

            if (from != null && !from.isEmpty()) {
                Allure.step("Fill From Date: " + from, () -> {
                    leaveCtrl.fillFromDate(driver, wait, from);
                });
            }

            if (to != null && !to.isEmpty()) {
                Allure.step("Fill To Date: " + to, () -> {
                    leaveCtrl.fillToDate(driver, wait, to);
                });
            }

            if (name != null && !name.isEmpty()) {
                Allure.step("Fill Employee Name: " + name, () -> {
                    leaveCtrl.fillEmployeeName(driver, wait, name);
                });
            }

            if (status != null && !status.isEmpty()) {
                Allure.step("Select Status: " + status, () -> {
                    leaveCtrl.selectStatus(driver, wait, status);
                });
            }

            if (type != null && !type.isEmpty()) {
                Allure.step("Select Leave Type: " + type, () -> {
                    leaveCtrl.selectLeaveType(driver, wait, type);
                });
            }

            if (subUnit != null && !subUnit.isEmpty()) {
                Allure.step("Select Sub Unit: " + subUnit, () -> {
                    leaveCtrl.selectSubUnit(driver, wait, subUnit);
                });
            }

            Allure.step("Click Search button", () -> {
                leaveCtrl.clickSearch(driver, wait);
                Thread.sleep(2000);
            });

        } catch (Exception e) {
            System.err.println("[LEAVE] Error:" + e.getMessage());
            Allure.addAttachment("Leave Search Error", e.getMessage());
        }
    }
}