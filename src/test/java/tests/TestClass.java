package tests;

import browser.Browser;
import com.aventstack.extentreports.Status;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import pages.Pages;
import utils.ExtentReport;
import utils.MongoDB;
import utils.Utils;

import java.io.*;

public class TestClass {

    @BeforeTest
    public void setup() throws Exception {
        ExtentReport.init();
        MongoDB.Init();
        Utils.getCurrentPlatform();
        Browser.open();
    }

    @AfterMethod
    public void afterEachTest(ITestResult result) throws IOException {
        if (result.getStatus() == ITestResult.FAILURE) {
            String testName = result.getName();

            // Test fail, generate screenshot.
            Browser.captureScreenshot(testName);
            ExtentReport.extentTest.addScreenCaptureFromPath(Utils.getCurrentRootLocation() + Utils.getScreenshotDirName() + testName + ".png");
            ExtentReport.extentTest.log(Status.FAIL, result.getThrowable());
        }
    }

    @Test(priority = 1)
    public void search() throws Exception {
        ExtentReport.extentTest = ExtentReport.extentReports.createTest("Search", "Search inside GitHub");
        Pages.HomePage().goTo();
        Pages.HomePage().search();
        MongoDB.addDbRecordsToReport();
        ExtentReport.extentTest.log(Status.PASS, "Test successfully passed");
    }

    @AfterTest
    public void cleanup() throws Exception {
        ExtentReport.extentTest = ExtentReport.extentReports.createTest("Cleanup", "Clean system");
        Browser.close();
        MongoDB.clearDB();
        MongoDB.stopMongo();
        Utils.cmdKill();
        ExtentReport.extentReports.flush();
    }
}
