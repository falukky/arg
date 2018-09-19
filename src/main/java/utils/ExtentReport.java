package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;

public class ExtentReport {

    public static ExtentHtmlReporter extentHtmlReporter;
    public static ExtentReports extentReports;
    public static ExtentTest extentTest;

    public static void init() {
        String path = ".\\reports\\report.html";
        extentHtmlReporter = new ExtentHtmlReporter(".\\reports\\report.html");
        extentReports = new ExtentReports();
        extentReports.attachReporter(extentHtmlReporter);
    }
}