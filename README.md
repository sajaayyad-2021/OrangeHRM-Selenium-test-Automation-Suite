\# 🚀 OrangeHRM Test Automation Framework



Selenium-based test automation framework for OrangeHRM with Database integration.



\## 📋 Features



\- ✅ Selenium WebDriver automation

\- ✅ TestNG framework

\- ✅ Page Object Model (POM)

\- ✅ ExtentReports for reporting

\- ✅ MySQL database integration

\- ✅ File-based and Database-driven modes

\- ✅ Support for Login, PIM, and Leave modules



\## 🛠️ Tech Stack



\- \*\*Language:\*\* Java 21

\- \*\*Automation:\*\* Selenium WebDriver 4.x

\- \*\*Framework:\*\* TestNG

\- \*\*Build Tool:\*\* Maven

\- \*\*Database:\*\* MySQL 8.x

\- \*\*Reporting:\*\* ExtentReports



\## 📦 Project Structure

```

OrangeHRM-Automation-Suite/

├── src/

│   ├── controller/

│   ├── database/

│   │   ├── DatabaseManager.java

│   │   └── TestResultDAO.java

│   ├── Driver/

│   ├── migration/

│   │   └── DatabaseMigrationScript.java

│   ├── POM/

│   ├── reporting/

│   ├── test/

│   │   ├── LoginTests.java

│   │   ├── PIMTests.java

│   │   └── LeaveTests.java

│   └── utilities/

│       ├── Config.java

│       ├── DatabaseResultChecker.java

│       └── MainFunctions.java

├── artifacts/

│   └── TestCases/

├── database/

│   └── create\_database.sql

├── pom.xml

├── .gitignore

└── README.md

```



\## 🚀 Setup



\### Prerequisites

\- Java 21+

\- Maven 3.x

\- MySQL 8.x

\- Chrome Browser



\### 1. Clone Repository

```bash

git clone https://github.com/YOUR\_USERNAME/OrangeHRM-Automation-Suite.git

cd OrangeHRM-Automation-Suite

```



\### 2. Install Dependencies

```bash

mvn clean install

```



\### 3. Database Setup

```bash

\# Login to MySQL

mysql -u root -p



\# Run the database script

source database/create\_database.sql



\# Exit MySQL

exit

```



\### 4. Run Migration Script

```bash

java -cp "target/classes;lib/\*" migration.DatabaseMigrationScript

```



\## ▶️ Running Tests



\### Run Single Test (Database Mode)

```bash

mvn clean test -Dtest=LoginTests -DtestNmaes\_login=TC\_LOG\_001\_validLogin -DdatabaseMode=true

```



\### Run Multiple Tests

```bash

mvn clean test -Dtest=LoginTests -DtestNmaes\_login=TC\_LOG\_001\_validLogin,TC\_LOG\_003\_emptyFields -DdatabaseMode=true

```



\### Run All Tests in Suite

```bash

mvn clean test -Dtest=LoginTests -DtestNmaes\_login=ALL -DdatabaseMode=true

```



\### Run All Suites

```bash

mvn clean test -DtestNmaes\_login=ALL -DtestNmaes\_pim=ALL -DtestNmaes\_leave=ALL -DdatabaseMode=true

```



\### File Mode (Legacy)

```bash

mvn clean test -Dtest=LoginTests -DtestNmaes\_login=TC\_LOG\_001\_validLogin -DdatabaseMode=false

```



\## 📊 Database Schema



\### Tables (8):

1\. \*\*test\_suites\*\* - Test suites (LoginTests, PIMTests, LeaveTests)

2\. \*\*test\_cases\*\* - Individual test cases

3\. \*\*test\_configurations\*\* - Test configurations (input data from input.json)

4\. \*\*baselines\*\* - Expected results (from baseline.txt)

5\. \*\*test\_executions\*\* - Test run sessions

6\. \*\*test\_results\*\* - Test results (actual vs expected)

7\. \*\*baseline\_comparisons\*\* - Comparison details

8\. \*\*test\_steps\*\* - Detailed test steps (optional)



\### View Results

```sql

-- Latest execution summary

SELECT \* FROM test\_executions ORDER BY execution\_id DESC LIMIT 1;



-- Test results

SELECT tc.test\_case\_name, tr.comparison\_result, tr.duration\_ms

FROM test\_results tr

JOIN test\_cases tc ON tr.test\_case\_id = tc.test\_case\_id

WHERE tr.execution\_id = (SELECT MAX(execution\_id) FROM test\_executions);



-- Pass rate by suite

SELECT 

&nbsp;   ts.suite\_name,

&nbsp;   COUNT(\*) as total,

&nbsp;   SUM(CASE WHEN tr.comparison\_result = 'PASS' THEN 1 ELSE 0 END) as passed,

&nbsp;   ROUND(AVG(CASE WHEN tr.comparison\_result = 'PASS' THEN 100 ELSE 0 END), 2) as pass\_rate

FROM test\_results tr

JOIN test\_cases tc ON tr.test\_case\_id = tc.test\_case\_id

JOIN test\_suites ts ON tc.suite\_id = ts.suite\_id

GROUP BY ts.suite\_name;

```



\## 📈 Reports



\- \*\*ExtentReports:\*\* `target/extent-reports/index.html`

\- \*\*Screenshots:\*\* `screenshots/` (on failure)

\- \*\*Logs:\*\* `logs/`



\## 🔄 Workflow

```

1\. @BeforeSuite  → Initialize database session

2\. @Test         → Load config from DB → Execute → Compare → Save results

3\. @AfterSuite   → Finalize session with pass/fail counts

4\. View Results  → Query database or check ExtentReports

```



\##  Contributing



1\. Fork the repository

2\. Create your feature branch (`git checkout -b feature/AmazingFeature`)

3\. Commit your changes (`git commit -m 'Add some AmazingFeature'`)

4\. Push to the branch (`git push origin feature/AmazingFeature`)

5\. Open a Pull Request



\## 📝 License



This project is for educational purposes.



\## 👤 Author



\*\*Saja Ayyad\*\*



\##



For issues or questions, please open an issue on GitHub.

