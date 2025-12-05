package controller;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import POM.POMlogin;

import java.time.Duration;

public class loginCtrl {

    // Fill username field
    public static void fillUsername(WebDriver driver, String username) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));  // Reduced from 12
        WebElement userField = wait.until(ExpectedConditions
                .elementToBeClickable(POMlogin.usernameField(driver)));
        try {
            userField.clear();
        } catch (InvalidElementStateException ignored) {}
        userField.sendKeys(username);
    }

    // Fill password field
    public static void fillPassword(WebDriver driver, String password) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));  // Reduced from 12
        WebElement passField = wait.until(ExpectedConditions
                .elementToBeClickable(POMlogin.passwordField(driver)));
        try {
            passField.clear();
        } catch (InvalidElementStateException ignored) {}
        passField.sendKeys(password);
    }

    // Click login button
    public static void clickLogin(WebDriver driver) {
        WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(5))  // Reduced from 10
                .until(ExpectedConditions.elementToBeClickable(POMlogin.loginButton(driver)));
        btn.click();
    }

    // Wait for dashboard (returns true if successful, false if timeout)
    public static boolean waitForDashboard(WebDriver driver, int timeoutSeconds) {
        try {
            POMlogin.waitForDashboard(driver, timeoutSeconds);
            return true;
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }
}