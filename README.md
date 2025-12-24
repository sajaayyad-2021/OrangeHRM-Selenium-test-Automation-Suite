# 🚀 OrangeHRM Test Automation Framework

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Selenium](https://img.shields.io/badge/Selenium-4.x-green.svg)](https://www.selenium.dev/)
[![TestNG](https://img.shields.io/badge/TestNG-7.x-red.svg)](https://testng.org/)
[![Maven](https://img.shields.io/badge/Maven-3.9-blue.svg)](https://maven.apache.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.x-blue.svg)](https://www.mysql.com/)
[![Jenkins](https://img.shields.io/badge/Jenkins-CI%2FCD-red.svg)](https://www.jenkins.io/)

Professional Selenium-based test automation framework for OrangeHRM with MySQL database integration and Jenkins CI/CD pipeline.

---

## 📋 Features

### 🎯 Core Features
- ✅ Selenium WebDriver 4.x automation
- ✅ TestNG framework with parallel execution
- ✅ Page Object Model (POM) design pattern
- ✅ ExtentReports with rich HTML reporting
- ✅ Screenshot capture on failure
- ✅ Detailed logging and error tracking

### 🗄️ Database Features
- ✅ MySQL 8.x integration
- ✅ Dual mode: File-based & Database-driven
- ✅ Automated test data migration
- ✅ Historical test execution tracking
- ✅ Real-time result comparison
- ✅ Advanced analytics and reporting

### 🔧 CI/CD Features
- ✅ Jenkins pipeline integration
- ✅ Parameterized builds
- ✅ Automated test execution
- ✅ Multi-browser support
- ✅ Scheduled test runs
- ✅ Email notifications

### 📦 Test Coverage
- ✅ Login module (5 test cases)
- ✅ PIM module (7 test cases)
- ✅ Leave management (6 test cases)
- ✅ Custom validation framework
- ✅ Data-driven testing
- ✅ Baseline comparison

---

## 🛠️ Tech Stack

**Language:** Java 21  
**Automation:** Selenium WebDriver 4.x  
**Framework:** TestNG 7.x  
**Build Tool:** Maven 3.9  
**Database:** MySQL 8.x  
**Reporting:** ExtentReports 5.x  
**CI/CD:** Jenkins Pipeline  
**Version Control:** Git & GitHub

---

## 📁 Project Structure
```
OrangeHRM-Automation-Suite/
│
├── src/
│   ├── controller/              # Test controllers
│   ├── database/                # Database management
│   │   ├── DatabaseManager.java
│   │   └── TestResultDAO.java
│   ├── Driver/                  # WebDriver setup
│   ├── migration/               # Database migration
│   │   └── DatabaseMigrationScript.java
│   ├── POM/                     # Page Object Models
│   │   ├── BasePage.java
│   │   ├── LoginPage.java
│   │   ├── DashboardPage.java
│   │   └── PIMPage.java
│   ├── reporting/               # ExtentReports configuration
│   ├── test/                    # Test classes
│   │   ├── LoginTests.java
│   │   ├── PIMTests.java
│   │   └── LeaveTests.java
│   ├── testbase/                # Base test template
│   └── utilities/               # Utility classes
│       ├── Config.java
│       ├── DatabaseResultChecker.java
│       └── MainFunctions.java
│
├── artifacts/                   # Test artifacts
│   └── TestCases/
│       ├── LoginTests/
│       ├── PIMTests/
│       └── LeaveTests/
│
├── database/                    # Database scripts
│   └── create_database.sql
│
├── test-output/                 # Test execution reports
├── screenshots/                 # Failure screenshots
├── pom.xml                      # Maven configuration
├── testng.xml                   # TestNG suite configuration
├── Jenkinsfile                  # Jenkins pipeline
├── .gitignore
└── README.md
```

---

## 🚀 Quick Start

### Prerequisites

Before you begin, ensure you have:

| Tool | Version | Download Link |
|------|---------|---------------|
| Java JDK | 21+ | [Download](https://adoptium.net/) |
| Maven | 3.9+ | [Download](https://maven.apache.org/download.cgi) |
| MySQL | 8.x | [Download](https://dev.mysql.com/downloads/) |
| Chrome | Latest | [Download](https://www.google.com/chrome/) |
| Jenkins | Latest (optional) | [Download](https://www.jenkins.io/download/) |

### Installation Steps

#### 1. Clone Repository
```bash
git clone https://github.com/sajaayyad-2021/OrangeHRM-Automation-Suite.git
cd OrangeHRM-Automation-Suite
```

#### 2. Install Dependencies
```bash
mvn clean install
```

#### 3. Database Setup
```bash
# Login to MySQL
mysql -u root -p

# Create database
source database/create_database.sql

# Exit MySQL
exit
```

#### 4. Run Migration Script
```bash
# Windows
java -cp "target/classes;lib/*" migration.DatabaseMigrationScript

# Mac/Linux
java -cp "target/classes:lib/*" migration.DatabaseMigrationScript
```

Expected Output:
```
MIGRATION COMPLETED SUCCESSFULLY!

Database Statistics:
  Test Suites:       3
  Test Cases:        19
  Configurations:    19
  Baselines:         19
```

---

## 💻 Usage

### Running Tests - Database Mode (Recommended)

#### Single Test
```bash
mvn clean test \
  -Dtest=LoginTests \
  -DtestNmaes_login=TC_LOG_001_validLogin \
  -DdatabaseMode=true
```

#### Multiple Tests
```bash
mvn clean test \
  -Dtest=LoginTests \
  -DtestNmaes_login=TC_LOG_001_validLogin,TC_LOG_003_emptyFields \
  -DdatabaseMode=true
```

#### All Tests in Suite
```bash
mvn clean test \
  -Dtest=LoginTests \
  -DtestNmaes_login=ALL \
  -DdatabaseMode=true
```

#### All Suites
```bash
mvn clean test \
  -DtestNmaes_login=ALL \
  -DtestNmaes_pim=ALL \
  -DtestNmaes_leave=ALL \
  -DdatabaseMode=true
```

### File Mode (Legacy)
```bash
mvn clean test \
  -Dtest=LoginTests \
  -DtestNmaes_login=TC_LOG_001_validLogin \
  -DdatabaseMode=false
```

---

## 🗄️ Database Architecture

### Schema Overview

**Database:** orangehrm_test_db

**Tables:**
- test_suites → Test suite definitions
- test_cases → Individual test cases
- test_configurations → Test input data (JSON)
- baselines → Expected results
- test_executions → Execution sessions
- test_results → Actual test results
- baseline_comparisons → Comparison details
- test_steps → Step-by-step logs (optional)

### Key Tables

| Table | Purpose | Key Fields |
|-------|---------|------------|
| test_suites | Groups test cases | suite_id, suite_name |
| test_cases | Individual tests | test_case_id, suite_id, test_case_name |
| test_configurations | Input data (from input.json) | config_id, test_case_id, config_data (JSON) |
| baselines | Expected results (from baseline.txt) | baseline_id, test_case_id, expected_value |
| test_executions | Test run sessions | execution_id, started_at, passed_tests, failed_tests |
| test_results | Actual results | result_id, execution_id, actual_value, comparison_result |
| baseline_comparisons | Comparison details | comparison_id, result_id, match_status |
| test_steps | Detailed step logs | step_id, result_id, step_description, status |

### Useful Queries

#### Latest Execution Summary
```sql
SELECT 
    execution_id,
    execution_name,
    browser,
    started_at,
    completed_at,
    TIMESTAMPDIFF(SECOND, started_at, completed_at) as duration_sec,
    total_tests,
    passed_tests,
    failed_tests,
    ROUND(passed_tests * 100.0 / total_tests, 2) as pass_rate
FROM test_executions 
ORDER BY execution_id DESC 
LIMIT 1;
```

#### Test Results for Latest Execution
```sql
SELECT 
    tc.test_case_name,
    tr.comparison_result,
    tr.duration_ms,
    tr.started_at
FROM test_results tr
JOIN test_cases tc ON tr.test_case_id = tc.test_case_id
WHERE tr.execution_id = (SELECT MAX(execution_id) FROM test_executions)
ORDER BY tr.started_at;
```

#### Pass Rate by Suite
```sql
SELECT 
    ts.suite_name,
    COUNT(*) as total_runs,
    SUM(CASE WHEN tr.comparison_result = 'PASS' THEN 1 ELSE 0 END) as passed,
    SUM(CASE WHEN tr.comparison_result = 'FAIL' THEN 1 ELSE 0 END) as failed,
    ROUND(AVG(CASE WHEN tr.comparison_result = 'PASS' THEN 100 ELSE 0 END), 2) as pass_rate
FROM test_results tr
JOIN test_cases tc ON tr.test_case_id = tc.test_case_id
JOIN test_suites ts ON tc.suite_id = ts.suite_id
GROUP BY ts.suite_name;
```

#### Find Flaky Tests
```sql
SELECT 
    tc.test_case_name,
    SUM(CASE WHEN tr.comparison_result = 'PASS' THEN 1 ELSE 0 END) as passes,
    SUM(CASE WHEN tr.comparison_result = 'FAIL' THEN 1 ELSE 0 END) as fails,
    COUNT(*) as total_runs,
    ROUND(SUM(CASE WHEN tr.comparison_result = 'PASS' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as stability_rate
FROM test_results tr
JOIN test_cases tc ON tr.test_case_id = tc.test_case_id
GROUP BY tc.test_case_name
HAVING passes > 0 AND fails > 0
ORDER BY stability_rate ASC;
```

---

## 🔄 CI/CD Integration

### Jenkins Pipeline

This project includes a complete Jenkins pipeline for automated testing.

#### Jenkinsfile
```groovy
pipeline {
    agent any
    
    tools {
        maven 'OrangeHRMSeleniumtestAutomationSuite'
        jdk 'JDK-21'
    }
    
    parameters {
        choice(
            name: 'BROWSER', 
            choices: ['chrome', 'firefox', 'edge'], 
            description: 'Select browser for test execution'
        )
        string(
            name: 'URL', 
            defaultValue: '', 
            description: 'Application URL (optional - leave empty for default)'
        )
        string(
            name: 'XML_FILE', 
            defaultValue: 'testng.xml', 
            description: 'TestNG XML suite file'
        )
        choice(
            name: 'DATABASE_MODE',
            choices: ['true', 'false'],
            description: 'Enable database mode for test results'
        )
    }
    
    stages {
        stage('Setup') {
            steps {
                script {
                    ws('C:\\Users\\hp\\eclipse-workspace\\Automationsuite4') {
                        echo "OrangeHRM Automation Pipeline Started"
                        echo "Project: ${env.JOB_NAME}"
                        echo "Build Number: ${env.BUILD_NUMBER}"
                        echo "Workspace: ${pwd()}"
                        echo "Browser: ${params.BROWSER}"
                        echo "Database Mode: ${params.DATABASE_MODE}"
                        
                        bat 'dir'
                        bat 'mvn --version'
                        bat 'java -version'
                        bat 'if exist pom.xml (echo pom.xml found!) else (echo pom.xml NOT found!)'
                    }
                }
            }
        }
        
        stage('Clean') {
            steps {
                script {
                    ws('C:\\Users\\hp\\eclipse-workspace\\Automationsuite4') {
                        echo "Cleaning previous build artifacts..."
                        bat 'mvn clean'
                    }
                }
            }
        }
        
        stage('Compile') {
            steps {
                script {
                    ws('C:\\Users\\hp\\eclipse-workspace\\Automationsuite4') {
                        echo "Compiling source code..."
                        bat 'mvn compile'
                    }
                }
            }
        }
        
        stage('Run Tests') {
            steps {
                script {
                    ws('C:\\Users\\hp\\eclipse-workspace\\Automationsuite4') {
                        def mvnCommand = "mvn test -DxmlFile=${params.XML_FILE} -Dbrowser=${params.BROWSER} -DdatabaseMode=${params.DATABASE_MODE}"
                        
                        if (params.URL?.trim()) {
                            mvnCommand += " -Durl=${params.URL}"
                        }
                        
                        echo "Executing Test Suite"
                        echo "Command: ${mvnCommand}"
                        
                        bat mvnCommand
                    }
                }
            }
        }
        
        stage('Package') {
            steps {
                script {
                    ws('C:\\Users\\hp\\eclipse-workspace\\Automationsuite4') {
                        echo "Packaging application..."
                        bat 'mvn package -DskipTests'
                    }
                }
            }
        }
        
        stage('Publish Reports') {
            steps {
                script {
                    ws('C:\\Users\\hp\\eclipse-workspace\\Automationsuite4') {
                        // Publish TestNG results
                        publishHTML([
                            allowMissing: false,
                            alwaysLinkToLastBuild: true,
                            keepAll: true,
                            reportDir: 'test-output',
                            reportFiles: 'index.html',
                            reportName: 'TestNG Report'
                        ])
                        
                        // Publish ExtentReports (if available)
                        publishHTML([
                            allowMissing: true,
                            alwaysLinkToLastBuild: true,
                            keepAll: true,
                            reportDir: 'target/extent-reports',
                            reportFiles: 'index.html',
                            reportName: 'Extent Report'
                        ])
                    }
                }
            }
        }
    }
    
    post {
        always {
            echo "Pipeline Execution Completed"
        }
        success {
            echo "Build completed successfully!"
            echo "Check reports for detailed results"
        }
        failure {
            echo "Build failed!"
            echo "Check console logs for errors"
        }
    }
}
```

### Setting Up Jenkins

#### 1. Install Jenkins Plugins
- Git plugin
- Maven Integration plugin
- TestNG Results plugin
- HTML Publisher plugin

#### 2. Configure Jenkins

Global Tool Configuration:
- Maven: OrangeHRMSeleniumtestAutomationSuite → Maven 3.9.11
- JDK: JDK-21 → Java 21

#### 3. Create Pipeline Job

1. New Item → Pipeline
2. Name: `OrangeHRM-Automation`
3. Pipeline → Definition: `Pipeline script from SCM`
4. SCM: Git
5. Repository URL: `https://github.com/sajaayyad-2021/OrangeHRM-Automation-Suite.git`
6. Script Path: `Jenkinsfile`

#### 4. Run Pipeline

- Click **Build with Parameters**
- Select browser, database mode, etc.
- Click **Build**

---

##  Reports

### ExtentReports

Beautiful HTML reports with:
- Test execution summary
- Pass/Fail statistics
- Screenshots on failure
- Step-by-step logs
- Execution timeline

**Location:** `target/extent-reports/index.html`
<img width="1360" height="643" alt="image" src="https://github.com/user-attachments/assets/18c91fea-c967-401c-83bd-02c692b5cb3b" />

### TestNG Reports

Standard TestNG HTML reports:
- Test results
- Suite summary
- Failed tests details

**Location:** `test-output/index.html`

### Database Reports

Query database for custom reports:
- Historical trends
- Flaky test detection
- Performance analysis
- Cross-execution comparison

---

## 🔄 Workflow

**Test Execution Flow:**

**1. @BeforeSuite**
- Initialize database session (if database mode)
- Setup WebDriver
- Create execution record

**2. @Test (for each test)**
- Load configuration from database/file
- Execute Selenium actions
- Capture actual result
- Compare with baseline
- Save result to database
- Log steps (optional)
- Capture screenshot on failure

**3. @AfterSuite**
- Finalize execution session
- Update pass/fail counts
- Generate reports
- Close WebDriver

**4. View Results**
- ExtentReports (HTML)
- TestNG Reports (HTML)
- Database queries (SQL)
- Jenkins dashboard

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. **Fork** the repository
2. **Create** your feature branch
```bash
   git checkout -b feature/AmazingFeature
```
3. **Commit** your changes
```bash
   git commit -m 'Add some AmazingFeature'
```
4. **Push** to the branch
```bash
   git push origin feature/AmazingFeature
```
5. **Open** a Pull Request

---

## 📝 License

This project is for **educational purposes** only.

---

## 👤 Author

**Saja Ayyad**

- GitHub: [@sajaayyad-2021](https://github.com/sajaayyad-2021)
- Email: sajayaser085@gmail.com

---

## 🙏 Acknowledgments

- OrangeHRM for the demo application
- Selenium community
- TestNG framework
- ExtentReports team

---

## Support

For issues or questions:
- Open an [Issue](https://github.com/sajaayyad-2021/OrangeHRM-Automation-Suite/issues)
- Start a [Discussion](https://github.com/sajaayyad-2021/OrangeHRM-Automation-Suite/discussions)

---

** Star this repository if you find it helpful!**

Made  by Saja Ayyad
