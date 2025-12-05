package POM;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class POMLeave {

    // ========== MENU & NAVIGATION ==========
    public static WebElement leaveMenu(WebDriver driver) {
        return driver.findElement(
            By.xpath("//span[normalize-space()='Leave' or contains(@class,'oxd-topbar-body-nav-tab-item')]")
        );
    }

    public static By leavePageHeader() {
        return By.xpath("//h6[contains(@class,'oxd-topbar-header-breadcrumb-module') and normalize-space()='Leave']");
    }

    public static WebElement leaveListTab(WebDriver driver) {
        return driver.findElement(
            By.xpath(
                "//a[normalize-space()='Leave List' or contains(@href,'viewLeaveList')] | " +
                "//button[normalize-space()='Leave List']"
            )
        );
    }

    public static By fromDateLabel() {
        return By.xpath("//label[normalize-space()='From Date']");
    }

    // ========== DATE INPUTS ==========
    public static WebElement fromDateInput(WebDriver driver) {
        return driver.findElement(
            By.xpath(
                "//label[text()='From Date']" +
                "/ancestor::div[contains(@class,'oxd-input-group')]" +
                "//input"
            )
        );
    }

    public static WebElement fromDateIcon(WebDriver driver) {
        return driver.findElement(
            By.xpath(
                "//label[text()='From Date']" +
                "/ancestor::div[contains(@class,'oxd-input-group')]" +
                "//i[contains(@class,'bi-calendar')]"
            )
        );
    }

    public static WebElement toDateInput(WebDriver driver) {
        return driver.findElement(
            By.xpath(
                "//label[text()='To Date']" +
                "/ancestor::div[contains(@class,'oxd-input-group')]" +
                "//input"
            )
        );
    }

    public static WebElement toDateIcon(WebDriver driver) {
        return driver.findElement(
            By.xpath(
                "//label[text()='To Date']" +
                "/ancestor::div[contains(@class,'oxd-input-group')]" +
                "//i[contains(@class,'bi-calendar')]"
            )
        );
    }

    // ========== DATE PICKER ELEMENTS ==========
    public static By datePicker() {
        return By.xpath("//div[contains(@class,'oxd-date-input-calendar')]");
    }

    public static WebElement yearSelector(WebDriver driver) {
        return driver.findElement(
            By.xpath(".//div[contains(@class,'oxd-calendar-selector-year')]")
        );
    }

    public static WebElement yearOption(WebDriver driver, String year) {
        return driver.findElement(
            By.xpath(
                "//li[contains(@class,'oxd-calendar-dropdown--option') and normalize-space()='" + year + "']"
            )
        );
    }

    public static WebElement monthSelector(WebDriver driver) {
        return driver.findElement(
            By.xpath(".//div[contains(@class,'oxd-calendar-selector-month')]")
        );
    }

    public static WebElement monthOption(WebDriver driver, String monthName) {
        return driver.findElement(
            By.xpath(
                "//li[contains(@class,'oxd-calendar-dropdown--option') and normalize-space()='" + monthName + "']"
            )
        );
    }

    public static WebElement dayButton(WebDriver driver, String day) {
        return driver.findElement(
            By.xpath(
                "//div[contains(@class,'oxd-calendar-date') or contains(@class,'oxd-calendar-day')]" +
                "[normalize-space()='" + day + "']"
            )
        );
    }

    // ========== EMPLOYEE NAME ==========
    public static WebElement employeeNameInput(WebDriver driver) {
        return driver.findElement(
            By.xpath(
                "//label[normalize-space()='Employee Name']" +
                "/ancestor::div[contains(@class,'oxd-input-group')]" +
                "//input"
            )
        );
    }

    // ========== DROPDOWN ICONS ==========
    public static WebElement statusDropdownIcon(WebDriver driver) {
        return driver.findElement(
            By.xpath(
                "//label[text()='Show Leave with Status']" +
                "/ancestor::div[contains(@class,'oxd-input-group')]" +
                "//i[contains(@class,'oxd-icon')]"
            )
        );
    }

    public static WebElement leaveTypeDropdownIcon(WebDriver driver) {
        return driver.findElement(
            By.xpath(
                "//label[text()='Leave Type']" +
                "/ancestor::div[contains(@class,'oxd-input-group')]" +
                "//i[contains(@class,'oxd-icon')]"
            )
        );
    }

    public static WebElement subUnitDropdownIcon(WebDriver driver) {
        return driver.findElement(
            By.xpath(
                "//label[text()='Sub Unit']" +
                "/ancestor::div[contains(@class,'oxd-input-group')]" +
                "//i[contains(@class,'oxd-icon')]"
            )
        );
    }

    // ========== DROPDOWN OPTIONS ==========
    public static WebElement dropdownOption(WebDriver driver, String optionText) {
        return driver.findElement(
            By.xpath(
                "//div[contains(@class,'oxd-select-option')]//span[normalize-space()='" + optionText + "']"
            )
        );
    }

    // ========== ACTION BUTTONS ==========
    public static WebElement searchButton(WebDriver driver) {
        return driver.findElement(
            By.xpath("//button[normalize-space()='Search' and contains(@class,'oxd-button')]")
        );
    }

    public static WebElement resetButton(WebDriver driver) {
        return driver.findElement(
            By.xpath("//button[normalize-space()='Reset' and contains(@class,'oxd-button')]")
        );
    }
}