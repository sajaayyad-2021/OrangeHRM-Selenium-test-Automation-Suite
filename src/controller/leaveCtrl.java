package controller;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import POM.POMLeave;

public class leaveCtrl {

    // ========== 1) OPEN LEAVE LIST PAGE ==========
	public static void openLeaveList(WebDriver driver, WebDriverWait wait) {

	    System.out.println("[LEAVE] Opening Leave List...");

	    // 1. Ensure we are on dashboard
	    driver.get("https://opensource-demo.orangehrmlive.com/web/index.php/dashboard/index");

	    wait.until(ExpectedConditions.visibilityOfElementLocated(
	            By.xpath("//span[normalize-space()='Dashboard']")
	    ));

	    // 2. Wait for the sidebar to load
	    wait.until(ExpectedConditions.visibilityOfElementLocated(
	            By.xpath("//aside[contains(@class,'oxd-sidepanel')]")
	    ));

	    // 3. Click Leave tab
	    WebElement leaveTab = wait.until(ExpectedConditions.elementToBeClickable(
	            By.xpath("//span[normalize-space()='Leave']")
	    ));
	    leaveTab.click();

	    System.out.println("[LEAVE] Leave tab clicked");

	    // 4. Wait for the Leave List page
	    wait.until(ExpectedConditions.visibilityOfElementLocated(
	            By.xpath("//h5[normalize-space()='Leave List']")
	    ));
	}


    // ========== 2) FILL FROM DATE ==========
    public static void fillFromDate(WebDriver driver, WebDriverWait wait, String fromDate) {
        WebElement fromIcon = wait.until(ExpectedConditions.elementToBeClickable(
            POMLeave.fromDateIcon(driver)
        ));
        safeClickWithScroll(driver, wait, fromIcon);

        selectDateWithPicker(driver, wait, fromDate);
        System.out.println("[leave] from date picked: " + fromDate);

        wait.until(ExpectedConditions.invisibilityOfElementLocated(POMLeave.datePicker()));
    }

    // ========== 3) FILL TO DATE ==========
    public static void fillToDate(WebDriver driver, WebDriverWait wait, String toDate) {
        WebElement toIcon = wait.until(ExpectedConditions.elementToBeClickable(
            POMLeave.toDateIcon(driver)
        ));
        safeClickWithScroll(driver, wait, toIcon);

        selectDateWithPicker(driver, wait, toDate);
        System.out.println("[leave] to date picked: " + toDate);

        wait.until(ExpectedConditions.invisibilityOfElementLocated(POMLeave.datePicker()));
    }

    // ========== HELPER: SELECT DATE WITH PICKER ==========
    private static void selectDateWithPicker(WebDriver driver, WebDriverWait wait, String date) {
        String[] parts = date.split("-");
        String year = parts[0];
        String month = parts[1];
        String day = parts[2];

        String monthName = monthNumberToName(month);
        String dayZero = String.valueOf(Integer.parseInt(day));

        wait.until(ExpectedConditions.visibilityOfElementLocated(POMLeave.datePicker()));

        // Select year
        try {
            WebElement yearSelector = POMLeave.yearSelector(driver);
            yearSelector.click();

            WebElement targetYear = wait.until(ExpectedConditions.elementToBeClickable(
                POMLeave.yearOption(driver, year)
            ));
            targetYear.click();
        } catch (WebDriverException e) {
            System.out.println("[leave] Year dropdown not found, skipped year selection.");
        }

        // Select month
        try {
            WebElement monthSelector = POMLeave.monthSelector(driver);
            monthSelector.click();

            WebElement targetMonth = wait.until(ExpectedConditions.elementToBeClickable(
                POMLeave.monthOption(driver, monthName)
            ));
            targetMonth.click();
        } catch (WebDriverException e) {
            System.out.println("[leave] Month dropdown not found, skipped month selection.");
        }

        // Select day
        WebElement dayButton = wait.until(ExpectedConditions.elementToBeClickable(
            POMLeave.dayButton(driver, dayZero)
        ));
        dayButton.click();
    }

    // ========== HELPER: MONTH NUMBER TO NAME ==========
    private static String monthNumberToName(String mm) {
        switch (mm) {
            case "01": return "January";
            case "02": return "February";
            case "03": return "March";
            case "04": return "April";
            case "05": return "May";
            case "06": return "June";
            case "07": return "July";
            case "08": return "August";
            case "09": return "September";
            case "10": return "October";
            case "11": return "November";
            case "12": return "December";
            default: return "January";
        }
    }

    // ========== 4) FILL EMPLOYEE NAME ==========
    public static void fillEmployeeName(WebDriver driver, WebDriverWait wait, String empName) {
        WebElement empInput = wait.until(ExpectedConditions.visibilityOf(
            POMLeave.employeeNameInput(driver)
        ));
        empInput.clear();
        empInput.sendKeys(empName);
        System.out.println("[leave] Employee name entered: " + empName);
    }

    // ========== 5) SELECT STATUS ==========
    public static void selectStatus(WebDriver driver, WebDriverWait wait, String statusText) {
        WebElement statusIcon = wait.until(ExpectedConditions.elementToBeClickable(
            POMLeave.statusDropdownIcon(driver)
        ));
        safeClickWithScroll(driver, wait, statusIcon);
        System.out.println("[leave] opened Status dropdown");

        selectDropdownOptionByText(driver, wait, statusText);
        System.out.println("[leave] Status selected: " + statusText);
    }

    // ========== 6) SELECT LEAVE TYPE ==========
    public static void selectLeaveType(WebDriver driver, WebDriverWait wait, String leaveTypeText) {
        WebElement leaveTypeIcon = wait.until(ExpectedConditions.elementToBeClickable(
            POMLeave.leaveTypeDropdownIcon(driver)
        ));
        safeClickWithScroll(driver, wait, leaveTypeIcon);
        System.out.println("[leave] opened Leave Type dropdown");

        selectDropdownOptionByText(driver, wait, leaveTypeText);
        System.out.println("[leave] Leave Type selected: " + leaveTypeText);
    }

    // ========== 7) SELECT SUB UNIT ==========
    public static void selectSubUnit(WebDriver driver, WebDriverWait wait, String subUnitText) {
        WebElement subUnitIcon = wait.until(ExpectedConditions.elementToBeClickable(
            POMLeave.subUnitDropdownIcon(driver)
        ));
        safeClickWithScroll(driver, wait, subUnitIcon);
        System.out.println("[leave] opened Sub Unit dropdown");

        selectDropdownOptionByText(driver, wait, subUnitText);
        System.out.println("[leave] Sub Unit selected: " + subUnitText);
    }

    // ========== HELPER: SELECT DROPDOWN OPTION ==========
    private static void selectDropdownOptionByText(WebDriver driver, WebDriverWait wait, String optionText) {
        WebElement option = wait.until(ExpectedConditions.elementToBeClickable(
            POMLeave.dropdownOption(driver, optionText)
        ));
        option.click();
    }

    // ========== 8) CLICK SEARCH BUTTON ==========
    public static void clickSearch(WebDriver driver, WebDriverWait wait) {
        WebElement searchBtn = wait.until(ExpectedConditions.elementToBeClickable(
            POMLeave.searchButton(driver)
        ));
        searchBtn.click();
        System.out.println("[leave] clicked Search button");
    }

    // ========== 9) CLICK RESET BUTTON ==========
    public static void clickReset(WebDriver driver, WebDriverWait wait) {
        WebElement resetBtn = wait.until(ExpectedConditions.elementToBeClickable(
            POMLeave.resetButton(driver)
        ));
        resetBtn.click();
        System.out.println("[leave] clicked Reset button");
    }

    // ========== HELPER: SAFE CLICK WITH SCROLL ==========
    private static void safeClickWithScroll(WebDriver driver, WebDriverWait wait, WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView({block: 'center'});", element
        );

        try {
            element.click();
        } catch (WebDriverException e) {
            System.out.println("[leave][safeClickWithScroll] Normal click failed, trying JS click: " 
                + e.getClass().getSimpleName());
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }
}