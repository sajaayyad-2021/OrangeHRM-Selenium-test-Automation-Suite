# OrangeHRM Test Automation Framework

>  **PROJECT STATUS**: This is a **training/learning project** and is currently **under development**. The framework is **NOT production-ready** and is being built as part of automation testing practice using the OrangeHRM open-source demo application.

A comprehensive **Selenium + TestNG** automation framework for OrangeHRM, designed with **Page Object Model (POM)** architecture and baseline validation approach.

---

##  About This Project

This project is:
-  **Educational/Training purpose** - Built for learning test automation concepts
-  **Work in Progress** - Actively under development
-  **Open Source** - Using OrangeHRM demo site for practice
-  **Not Complete** - Some features may be incomplete or in testing phase
-  **Experimental** - Used for exploring automation patterns and best practices

**Target Audience**: QA Engineers, Automation Testers, and Students learning Selenium WebDriver with Java.

---

##  Table of Contents

- [Overview](#overview)
- [Framework Architecture](#framework-architecture)
- [Project Structure](#project-structure)
- [Running Tests](#running-tests)
- [Test Suites](#test-suites)
- [Configuration](#configuration)
- [Test Case Management](#test-case-management)
- [Reports](#reports)
- [Key Features](#key-features)

---

## 🎯 Overview

This framework automates functional testing for the **OrangeHRM** system, covering:

- **Login** functionality (valid/invalid credentials, field validations)
- **PIM (Personal Information Management)** - Add/Search employees
- **Leave Management** - Search leave records with various filters
- **Recruitment** - Add candidates with resume upload

**Validation Approach**: Baseline comparison - each test captures actual results and compares them against expected baseline files.

###  Current Development Status

| Module | Status | Test Cases | Completion |
|--------|--------|-----------|-----------|
| Login Tests |  Functional | 5 cases | ~90% |
| PIM Tests |  In Progress | 8 cases | ~70% |
| Leave Tests | In Progress | 6 cases | ~60% |
| Recruitment Tests |  Planned | 0 cases | ~10% |
| Reporting |  Functional | - | ~75% |


**Test Coverage**: 19 test cases across 3 modules

**Known Limitations**:
- Some test cases may have incomplete validations
- Error handling is still being improved
- Not all edge cases are covered yet
- Recruitment module is in early development stage
- Performance optimization pending
- Some features are experimental

---

## Framework Architecture

```
┌─────────────────────────────────────────┐
│         RegressionDriver.java           │
│     (Entry point - CLI arguments)       │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│         BaseTemplate.java               │
│(TestNG annotations,WebDrivermanagement) │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│    LoginTests / PIMTests / LeaveTests   │
│        (Test Suite Classes)             │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│         MainFunctions.java              │
│    (Main test functions workflows)      │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│    Controllers (loginCtrl, PIMCtrl...)  │
│     ( logic & actions)                  │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│          PAGE OBGECT MODEL 
 POM (POMlogin, POMPIM...)                │
│    (Locators & element methods)         │
└─────────────────────────────────────────┘
```

**Design Pattern**: Page Object Model (POM)  
**Reporting**: ExtentReports with screenshots  
**Configuration**: JSON-based per test case

---

##  Project Structure

```
orangehrm-automation/
│
├── Driver/
│   └── RegressionDriver.java          # Main entry point
│
├── testbase/
│   └── BaseTemplate.java              # TestNG base class
│
├── Test/
│   ├── LoginTests.java                # Login test suite
│   ├── PIMTests.java                  # PIM test suite
│   ├── LeaveTests.java                # Leave test suite
│   └── RecruitmentTests.java          # Recruitment test suite
│
├── utilites/
│   ├── MainFunctions.java             # Test workflow orchestration
│   ├── Config.java                    # Configuration POJO
│   ├── CustomFunction.java            # Helper utilities
│   └── ResultChecker.java             # Validation logic
│
├── controller/
│   ├── loginCtrl.java                 # Login actions
│   ├── PIMCtrl.java                   # PIM actions
│   ├── leaveCtrl.java                 # Leave actions
│   ├── LogoutCtrl.java                # Logout actions
│   └── RecruitmentCtrl.java           # Recruitment actions
│
├── POM/
│   ├── POMlogin.java                  # Login page locators
│   ├── POMPIM.java                    # PIM page locators
│   ├── POMLeave.java                  # Leave page locators
│   └── POMRecruitment.java            # Recruitment page locators
│
├── reporting/
│   ├── ExtentManager.java             # ExtentReports setup
│   └── ExtentTestNGITestListener.java # TestNG listener
│
├── artifacts/
│   ├── TestCases/
│   │   ├── LoginTests/
│   │   │   ├── TC_LOG_001_validLogin/
│   │   │   │   ├── Input/input.json
│   │   │   │   ├── Expected/baseline.txt
│   │   │   │   ├── Actual/baseline.txt
│   │   │   │   └── Diff/baseline_diff.txt
│   │   │   ├── TC_LOG_003_emptyFields/
│   │   │   └── TC_LOG_004_emptyPasswordOnly/
│   │   ├── PIMTests/
│   │   └── LeaveTests/
│   └── test-output/
│       └── Reports.html
│
└── testng.xml                         # TestNG suite configuration
```

---


##  Setup Instructions

### 1. Clone Repository

```bash
git clone <repository-url>
cd orangehrm-automation
```

### 2. Create Test Case Structure

Each test case requires this folder structure:

```
artifacts/TestCases/<TestSuiteClass>/<TestCaseName>/
├── Input/
│   └── input.json          # Test configuration
├── Expected/
│   └── baseline.txt        # Expected result (baseline)
├── Actual/                 # Auto-generated during test run
└── Diff/                   # Auto-generated comparison report
```

**Example**: For `TC_LOG_001_validLogin`

```
artifacts/TestCases/LoginTests/TC_LOG_001_validLogin/
├── Input/input.json
└── Expected/baseline.txt
```

**input.json** example:
```json
{
  "baseURL": "https://opensource-demo.orangehrmlive.com",
  "auth": {
    "userName": "Admin",
    "passWord": "admin123"
  }
}
```
**baseline.txt** example:
```
https://opensource-demo.orangehrmlive.com/web/index.php/dashboard/index
```

### 4. Configure RegressionDriver

**Using IDE **

1. Open `RegressionDriver.java`
2. Right-click → **Run As** → **Run Configurations**
3. In **Arguments** tab, paste:
```
-out artifacts -browser chrome -testNmaes_login TC_LOG_001_validLogin,TC_LOG_003_emptyFields,TC_LOG_004_emptyPasswordOnly -url https://opensource-demo.orangehrmlive.com/web/index.php/auth/login
```
4. Click **Apply** → **Run**

### 6. Verify Setup

Run a simple test to verify everything works:

```
-out artifacts -browser chrome -testNmaes_login TC_LOG_001_validLogin -url https://opensource-demo.orangehrmlive.com/web/index.php/auth/login
```

Check the output:
- Browser should open automatically
- Login should execute
- ExtentReport should be generated at: `artifacts/test-output/ExtentReport.html`

---

## Running Tests

### Option 1: Run via Java Program Arguments (Recommended)

**In Eclipse/IntelliJ IDEA**:

1. **Right-click** on `RegressionDriver.java` → **Run As** → **Run Configurations**
2. Go to **Arguments** tab
3. Paste the following in **Program Arguments**:

```
-out artifacts -browser chrome -testNmaes_login TC_LOG_001_validLogin,TC_LOG_003_emptyFields,TC_LOG_004_emptyPasswordOnly -url https://opensource-demo.orangehrmlive.com/web/index.php/auth/login
```

4. Click **Apply** → **Run**

**Visual Guide**:

```
Run Configurations
├── Main Tab
│   └── Main class: Driver.RegressionDriver
└── Arguments Tab
    └── Program arguments: -out artifacts -browser chrome ...
```



**For Windows (Command Prompt)**:
```cmd
java -cp "bin;lib/*" Driver.RegressionDriver -out artifacts -browser chrome -testNmaes_login TC_LOG_001_validLogin,TC_LOG_003_emptyFields,TC_LOG_004_emptyPasswordOnly -url https://opensource-demo.orangehrmlive.com/web/index.php/auth/login
```



##  Test Suites

### CLI Parameters

| Parameter | Description | Default | Example |
|-----------|-------------|---------|---------|
| `-out` | Output directory | `artifacts` | `-out artifacts` |
| `-browser` | Browser type | `chrome` | `-browser chrome` |
| `-testNmaes_login` | Login tests to run | `ALL` | `-testNmaes_login TC_LOG_001_validLogin,TC_LOG_003_emptyFields` |
| `-testNmaes_pim` | PIM tests to run | `ALL` | `-testNmaes_pim TC_PIM_001_addEmployee` |
| `-testNmaes_leave` | Leave tests to run | `ALL` | `-testNmaes_leave TC_LEAVE_001_searchByDate` |
| `-url` | Base URL | (from config) | `-url https://opensource-demo.orangehrmlive.com/web/index.php/auth/login` |
| `-xml` | TestNG XML file | `testng.xml` | `-xml testng.xml` |

### Test Selection Examples

**Run ALL Login tests**:
```
-testNmaes_login ALL
```

**Run specific Login tests**:
```
-testNmaes_login TC_LOG_001_validLogin,TC_LOG_003_emptyFields,TC_LOG_004_emptyPasswordOnly
```

**Run single test**:
```
-testNmaes_login TC_LOG_001_validLogin
```

**Run multiple test suites**:
```
-testNmaes_login TC_LOG_001_validLogin,TC_LOG_003_emptyFields -testNmaes_pim TC_PIM_001_addEmployee -testNmaes_leave ALL
```

### Complete Command Examples

**Example 1: Run specific Login tests (3 cases)**
```
-out artifacts -browser chrome -testNmaes_login TC_LOG_001_validLogin,TC_LOG_003_emptyFields,TC_LOG_004_emptyPasswordOnly -url https://opensource-demo.orangehrmlive.com/web/index.php/auth/login
```

**Example 2: Run ALL Login tests (5 cases)**
```
-out artifacts -browser chrome -testNmaes_login ALL -url https://opensource-demo.orangehrmlive.com/web/index.php/auth/login
```


**Example 3: Run ALL PIM tests (8 cases)**
```
-out artifacts -browser chrome -testNmaes_pim ALL -url https://opensource-demo.orangehrmlive.com/web/index.php/auth/login
```

**Example 5: Run specific Leave tests**
```
-out artifacts -browser chrome -testNmaes_leave TC_LEAVE_001_basicSearch,TC_LEAVE_004_selectLeaveStatus,TC_LEAVE_006_resetButton -url https://opensource-demo.orangehrmlive.com/web/index.php/auth/login
```

**Example 6: Run ALL Leave tests (6 cases)**
```
-out artifacts -browser chrome -testNmaes_leave ALL -url https://opensource-demo.orangehrmlive.com/web/index.php/auth/login
```

**Example 7: Run ALL tests across all modules (19 cases)**
```
-out artifacts -browser chrome -testNmaes_login ALL -testNmaes_pim ALL -testNmaes_leave ALL -url https://opensource-demo.orangehrmlive.com/web/index.php/auth/login
```

**Example 8: Run mixed test cases from different modules**
```
-out artifacts -browser chrome -testNmaes_login TC_LOG_001_validLogin,TC_LOG_008_invalidBoth -testNmaes_pim TC_PIM_001_addEmployeeValid,TC_PIM_002_addEmployeeNoMiddleName -testNmaes_leave TC_LEAVE_001_basicSearch -url https://opensource-demo.orangehrmlive.com/web/index.php/auth/login
```

##  Test Case Management

### Login Test Cases

| Test Case ID | Description | Input Fields | Expected Result |
|-------------|-------------|--------------|-----------------|
| `TC_LOG_001_validLogin` | Valid credentials login | Admin/admin123 | Dashboard URL |
| `TC_LOG_003_emptyFields` | Empty username & password | (empty)/(empty) | Validation messages |
| `TC_LOG_004_emptyPasswordOnly` | Empty password field only | Admin/(empty) | Password required message |
| `TC_LOG_008_invalidBoth` | Invalid username & password | invalid/invalid | Invalid credentials alert |
| `TC_LOG_009_logoutRedirect` | Logout and redirect | Valid login → logout | Redirect to login page |

**Status**: 5 test cases implemented ✅

---

### PIM Test Cases

| Test Case ID | Description | Test Scenario | Expected Result |
|-------------|-------------|---------------|-----------------|
| `TC_PIM_001_addEmployeeValid` | Add employee with all fields | First, Middle, Last name | Employee created successfully |
| `TC_PIM_002_addEmployeeNoMiddleName` | Add employee without middle name | First & Last name only | Employee created |
| `TC_PIM_003_addEmployeeMissingFirstName` | Missing first name validation | Empty first name | Validation error shown |
| `TC_PIM_004_addEmployeeMissingLastName` | Missing last name validation | Empty last name | Validation error shown |
| `TC_PIM_005_addEmployeeMissingBoth` | Missing both names | Empty first & last | Validation errors shown |
| `TC_PIM_006_searchByValidName` | Search employee by name | Valid employee name | Employee found in results |
| `TC_PIM_007_searchByInvalidName` | Search with invalid name | Non-existent name | No records found |
| `TC_PIM_008_searchByInvalidId` | Search with invalid ID | Non-existent ID | No records found |

**Status**: 8 test cases implemented 

---

### Leave Test Cases

| Test Case ID | Description | Filters Used | Expected Result |
|-------------|-------------|--------------|-----------------|
| `TC_LEAVE_001_basicSearch` | Basic leave search | Default filters | Leave records displayed |
| `TC_LEAVE_002_searchWithEmployeeName` | Search by employee name | Employee name | Filtered leave records |
| `TC_LEAVE_003_invalidDateToDate` | Invalid date range | To date < From date | Error or no records |
| `TC_LEAVE_004_selectLeaveStatus` | Filter by leave status | Status (Approved/Pending) | Filtered by status |
| `TC_LEAVE_005_selectLeaveType` | Filter by leave type | Leave type dropdown | Filtered by type |
| `TC_LEAVE_006_resetButton` | Reset all filters | Click reset button | All filters cleared |

**Status**: 6 test cases implemented 

---

### Test Case Summary

| Test Suite | Total Cases | Status |
|-----------|-------------|--------|
| **Login** | 5 |  Complete |
| **PIM** | 8 |  In Progress |
| **Leave** | 6 |  In Progress |
| **Recruitment** | 0 |  Planned |
| **TOTAL** | **19** | **~65% Complete** |

---

##  Configuration

### Per-Test Configuration (`input.json`)

```json
{
  "baseURL": "https://opensource-demo.orangehrmlive.com",
  
  "auth": {
    "userName": "Admin",
    "passWord": "admin123"
  },

  "defaults": {
    "firstName": "Raghad",
    "middleName": "N",
    "lastName": "Hamad"
  },

  "leaveSearch": {
    "fromDate": "2024-05-01",
    "toDate": "2024-12-30",
    "employeeName": "Linda Anderson",
    "status": "Taken",
    "leaveType": "CAN - Personal",
    "subUnit": "QA",
    "resetFilters": true
  },

  "recruitment": {
    "candidateFirstName": "Layan",
    "candidateMiddleName": "S",
    "candidateLastName": "Awad",
    "vacancy": "Software QA Engineer",
    "email": "layan.qa+" ,
    "contactNumber": "0591234567",
    "resumePath": "C:\\resumes\\layan_cv.pdf",
    "keywords": "quality, testing, automation",
    "dateOfApplication": "2024-11-27",
    "notes": "Has strong background in manual + automation testing",
    "consent": true
  }
}

```

---
## 📊 Test Reports (ExtentReports)

After running the tests, an HTML report is generated here:
---

##  Sample Execution Log

---

<img width="1363" height="623" alt="image" src="https://github.com/user-attachments/assets/b5a0ed5e-858a-4e83-8c21-7ad576b61228" />

### Report Includes
- Test pass/fail status  
- Step-by-step execution logs  
- Screenshots on failure  
- Execution timestamps  
- Test categories (Login, PIM, Leave, Regression)  
- Summary dashboard with statistics  


## Baseline Comparison
EXPECTED:
https://opensource-demo.orangehrmlive.com/web/index.php/pim/addEmployee

ACTUAL:
https://opensource-demo.orangehrmlive.com/web/index.php/pim/addEmployee

RESULT: PASS

---

##  Baseline Files
Each test case generates:

| File | Description |
|------|-------------|
| `Actual/baseline.txt` | Actual output |
| `Expected/baseline.txt` | Expected baseline |
| `Diff/baseline_diff.txt` | Comparison result |

### Example Diff
TC_LOG_001_validLogin DIFF
Expected: /dashboard
Actual : /dashboard
Result : PASS


---

## 📈 Test Summary
| Test Case                          | Module | Duration     | Status |
|------------------------------------|--------|--------------|--------|
| TC_PIM_001_addEmployeeValid        | PIM    | 00:00:08.465 | PASS   |
| TC_PIM_002_addEmployeeNoMiddleName | PIM    | 00:00:08.241 | PASS   |

**Pass Rate:** 100%



## 🌟 Key Features

### 1. **Modular Architecture**
- Separation of concerns (POM, Controller, Test)
- Reusable components across test suites

### 2. **Flexible Test Execution**
- Run ALL tests or specific test cases via CLI
- Supports parallel execution (TestNG)

### 3. **Baseline Validation**
- Automatic comparison of actual vs expected results
- Human-readable diff reports

### 4. **Comprehensive Reporting**
- ExtentReports with rich UI
- Embedded logs and screenshots
- Test categorization (Regression)

### 5. **Smart Waits**
- Explicit waits for dynamic elements

### 6. **Auto-Cleanup**
- Deletes Actual/Diff folders before each run
- delete reports .
- Fresh results every execution

---

##  Troubleshooting

### Issue: Tests fail with "Element not found"

**Solution**: Increase wait time in `BaseTemplate.java`

```java
wait = new WebDriverWait(driver, Duration.ofSeconds(20));
```

### Issue: ChromeDriver version mismatch

**Solution**: Update Chrome browser or use Selenium Manager (auto-handles driver)

### Issue: Baseline file not found

**Error**: `Expected baseline file not found`

**Solution**: Create `Expected/baseline.txt` manually with expected result

```bash
mkdir -p artifacts/TestCases/LoginTests/TC_LOG_001_validLogin/Expected
echo "https://...dashboard..." > artifacts/TestCases/LoginTests/TC_LOG_001_validLogin/Expected/baseline.txt
```



### Issue: Tests hang on logout

**Solution**: Increase logout wait timeout in `LogoutCtrl.java`

---

## Adding New Test Cases

### Step 1: Create Test Case Folder

```bash
artifacts/TestCases/LoginTests/TC_LOG_NEW_TEST/{Input,Expected}
```

### Step 2: Create input.json

```json
{
  "baseURL": "https://opensource-demo.orangehrmlive.com",
  "auth": {
    "userName": "testuser",
    "passWord": "testpass"
  }
}
```

### Step 3: Create baseline.txt

```
https://expected-url-after-action
```

### Step 4: Run Test

```bash
java -cp target/classes Driver.RegressionDriver \
  -testNmaes_login TC_LOG_NEW_TEST \
  -url https://...
```

---
##  Project Navigation Flow
This video shows the basic navigation flow of the automation project before adding any test cases.
It demonstrates only:

How the framework handles page navigation

How the driver moves through the UI

How the Page Object Model (POM) structure works

How the main test class performs navigation functions

Navigation across the core OrangeHRM pages:

Login

Dashboard

PIM

Leave

Recruitment

Note: This is not a test case.
It is only a demonstration of the framework’s navigation flow using POM + Controllers.

https://github.com/sajaayyad-2021/OrangeHRM-Selenium-test-Automation-Suite/issues/1
---

##  Contributing

This is an **open learning project**! Contributions are welcome from:
- 🎓 Students learning automation
- 👨‍💻 QA professionals improving their skills
- 🌟 Anyone interested in test automation

---

##  NOTES

- This framework uses the **OrangeHRM open-source demo** application
- Tests may fail if the demo site is down or updated
- This is **NOT** intended for production use


---

