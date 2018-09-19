package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;

public class ExtentReport {

    public static ExtentHtmlReporter extentHtmlReporter;
    public static ExtentReports extentReports;
    public static ExtentTest extentTest;

    public static void init() throws Exception {
        String path = getLocalLocation();
        extentHtmlReporter = new ExtentHtmlReporter(getLocalLocation());
        extentReports = new ExtentReports();
        extentReports.attachReporter(extentHtmlReporter);
    }

    private static String getLocalLocation() throws Exception {
        String location = "";
        switch (OsCheck.getOperatingSystemType()) {
            case Windows:
                location = ".\\reports\\report.html";
                break;

            case Mac:
                location = "./reports/report.html";
                break;

            case Linux:
                location = "./reports/report.html";
                break;
        }

        return location;
    }
}