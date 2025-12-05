package POM;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class POMlogin {
    
    // ========== BASIC ELEMENTS ==========
    public static WebElement usernameField(WebDriver driver) {
        return driver.findElement(By.name("username"));
    }
    
    public static WebElement passwordField(WebDriver driver) {
        return driver.findElement(By.name("password"));
    }
    
    public static WebElement loginButton(WebDriver driver) {
        return driver.findElement(By.cssSelector("button[type='submit']"));
    }
    
    public static java.util.List<WebElement> requiredMessages(WebDriver driver) {
        return driver.findElements(By.cssSelector("span.oxd-input-field-error-message"));
    }
    
    // ========== INVALID CREDENTIALS ==========
    private static final By invalidCredBox =
            By.xpath("//p[contains(@class,'oxd-alert-content-text') and contains(.,'Invalid')]");
    
    public static boolean isInvalidCredentials(WebDriver driver) {
        try {
            return driver.findElement(invalidCredBox).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    public static String getInvalidMessage(WebDriver driver) {
        try {
            return driver.findElement(invalidCredBox).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }
    
    // ========== USERNAME/PASSWORD REQUIRED ==========
    public static boolean isUsernameInvalid(WebDriver driver) {
        try {
            return driver.findElement(
                    By.xpath("//span[contains(text(),'Username')]")
            ).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    public static boolean isPasswordInvalid(WebDriver driver) {
        try {
            return driver.findElement(
                    By.xpath("//span[contains(text(),'Password')]")
            ).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    // ========== LOGIN PAGE DETECTION ==========
    public static boolean isLoginPage(WebDriver driver) {
        try {
            return usernameField(driver).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    // ========== DASHBOARD DETECTION ==========
    public static boolean isDashboard(WebDriver driver) {
        try {
            String url = driver.getCurrentUrl();
            if (url != null && url.contains("/dashboard")) return true;
            return driver.getPageSource().toLowerCase().contains("dashboard");
        } catch (Exception e) {
            return false;
        }
    }
    
    // ========== WAIT HELPERS ==========
    public static void waitForLoginPage(WebDriver driver) {
        new WebDriverWait(driver, java.time.Duration.ofSeconds(5))  // Reduced from 10
                .until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
    }
    
    public static void waitForInvalidCredentials(WebDriver driver) {
        new WebDriverWait(driver, java.time.Duration.ofSeconds(5))  // Reduced from 10
                .until(ExpectedConditions.visibilityOfElementLocated(invalidCredBox));
    }
    
    public static void waitForDashboard(WebDriver driver, int timeoutSeconds) {
        new WebDriverWait(driver, java.time.Duration.ofSeconds(timeoutSeconds))
                .until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("/dashboard"),
                    ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//h6[normalize-space()='Dashboard']")
                    )
                ));
    }
}