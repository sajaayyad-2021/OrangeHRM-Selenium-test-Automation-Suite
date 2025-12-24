package reporting;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

public class ExtentManager {

    private static ExtentReports extent;
    private static ExtentSparkReporter spark;

    public static ExtentReports getInstance() {

        if (extent == null) {
            createInstance();
        }
        return extent;
    }
    private static void createInstance() {

        String reportDir = "test-output/Reports/";
        File folder = new File(reportDir);

        // ========== DELETE OLD REPORTS ==========
        if (folder.exists()) {
            for (File f : folder.listFiles()) {
                try {
                    f.delete();
                } catch (Exception ignored) {}
            }
        }

        folder.mkdirs();
        // =========================================

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String reportPath = reportDir + "Automation_Combined_Report_" + timestamp + ".html";

        spark = new ExtentSparkReporter(reportPath);

        // THEME
        spark.config().setTheme(Theme.STANDARD);
        spark.config().setDocumentTitle("Automation Test Report");
        spark.config().setReportName("OrangeHRM Regression Suite");
        spark.config().setEncoding("utf-8");

        extent = new ExtentReports();
        extent.attachReporter(spark);

        extent.setSystemInfo("Project", "OrangeHRM Automation");
        extent.setSystemInfo("Environment", "QA");
        extent.setSystemInfo("Author", "Saja");
    }

}