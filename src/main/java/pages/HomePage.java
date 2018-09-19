package pages;

import browser.Browser;
import com.aventstack.extentreports.Status;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import utils.ExtentReport;
import utils.MongoDB;
import utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomePage {
    private String url = "https://github.com/";

    public String Url() {
        return url;
    }

    private WebElement searchTextBox() {
        return Browser.webDriverWait30Sec().until(ExpectedConditions.visibilityOfElementLocated(By.name("q")));
    }

    private List<WebElement> searchResults() {
        return Browser.webDriverWait30Sec().until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("div.repo-list-item.d-flex.flex-column.flex-md-row.flex-justify-start.py-4.public.source")));
    }

    private String getTags(WebElement webElement) {
        ArrayList<String> tags = new ArrayList<String>();
        try {
            List<WebElement> list = webElement.findElements(By.cssSelector("div.topics-row-container.col-12.col-md-9.d-inline-flex.flex-wrap.flex-items-center.f6.my-1 a"));
            for (int i = 0; i < list.size(); i++)
                tags.add(list.get(i).getText());
        } catch (Exception ex) {
            return null;
        }

        return parseTags(tags);
    }

    private String parseTags(ArrayList<String> arr) {

        // In case no tags found.
        if (arr.size() == 0)
            return "N/A";

        String tags = "";
        for (String value : arr)
            tags += value + ", ";

        return tags.substring(0, tags.length() - 1);
    }

    private String getTitle(WebElement webElement) {
        WebElement element = webElement.findElement(By.cssSelector("h3 a"));
        return element.getText();
    }

    private String getDescription(WebElement webElement) {
        try {
            WebElement element = webElement.findElement(By.cssSelector("p.col-12.col-md-9.d-inline-block.text-gray.mb-2.pr-4"));
            return element.getText();
        } catch (Exception ex) {
            return "N/A";
        }
    }

    private String getTime(WebElement webElement) {
        try {
            WebElement element = webElement.findElement(By.cssSelector("div.d-flex.flex-wrap relative-time"));
            return element.getText();
        } catch (Exception ex) {
            return "N/A";
        }
    }

    private WebElement getHrefLink(WebElement webElement) {
        return webElement.findElement(By.cssSelector("h3 a"));
    }

    private String getHrefURL(WebElement webElement) {
        return webElement.getAttribute("href");
    }

    private String getLanguage(WebElement webElement) {
        WebElement element = webElement.findElement(By.cssSelector("div.text-gray.flex-auto.min-width-0"));
        return element.getText();
    }

    private WebElement getPageHeadElement() {
        try {
            return Browser.webDriverWait30Sec().until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.signup-prompt-bg.rounded-1")));
        } catch (TimeoutException ex) {
            return null;
        }
    }

    private WebElement nextPageLink() {
        return Browser.webDriverWait30Sec().until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.next_page")));
    }

    private boolean isNextPageExist() {
        try {
            return nextPageLink() != null;
        } catch (TimeoutException ex) {
            return false;
        }
    }

    private String getStars(WebElement webElement) {

        return webElement.findElement(By.cssSelector("a.muted-link")).getText();
    }

    public void goTo() {
        Browser.driver().navigate().to(url);
        ExtentReport.extentTest.log(Status.INFO, String.format("Navigate to '%s'", url));
    }

    public void search() throws Exception {

        int numberOfResults = Integer.parseInt(Utils.getProperty("numberofresults"));
        if (numberOfResults <= 0)
            throw new Exception(String.format("Number of results value need to be positive value and bigger then 0 (current value is %s)", numberOfResults));

        long start = System.currentTimeMillis();
        String query = Utils.getProperty("query");

        searchTextBox().clear();
        searchTextBox().sendKeys(query);
        searchTextBox().sendKeys(Keys.ENTER);
        ExtentReport.extentTest.log(Status.INFO, String.format("Search for '%s'", query));
        ExtentReport.extentTest.log(Status.INFO,String.format("Take the first %s results...", numberOfResults));

        List<WebElement> results = searchResults();
        ExtentReport.extentTest.log(Status.INFO, String.format("Search return '%s' results at this page", results.size()));

        long finish = System.currentTimeMillis();
        long output = finish - start;

        ExtentReport.extentTest.log(Status.INFO, String.format("Search query time takes %s milliseconds", output));

        int i = 0;
        int handles = 0;

        while (isNextPageExist()) {

            // Get details for each search result.
            String title = getTitle(results.get(i));
            String description = getDescription(results.get(i));
            String language = getLanguage(results.get(i));
            String stars = getStars(results.get(i));
            String tags = getTags(results.get(i));
            String time = getTime(results.get(i));
            WebElement hrefLink = getHrefLink(results.get(i));
            String url = getHrefURL(hrefLink);

            // Create log table.
            writeResultIntoDB(title, description, language, stars, tags, time, pageLoadElapsedTime(hrefLink, url));

            // Close current tab.
            Browser.closeLastTab();

            // Increase current search counter.
            i++;
            handles++;

            // In case we reach number of results we want.
            if (handles == numberOfResults)
                break;

            // Get next page.
            if (i == 10 && numberOfResults > 10) {
                i = 0;
                nextPageLink().click();
                Thread.sleep(3000);
                results = searchResults();
            }
        }
    }

    private String pageLoadElapsedTime(WebElement hrefLink, String href) {
        // start measure time until page load.
        long start = System.currentTimeMillis();

        // Open link in a new tab.
        Browser.openLinkInNewTab(hrefLink);

        // Wait until page load.
        if (Browser.isPageLoad(href)) {
            // Page successfully load, stop measure time.
            return Long.toString(System.currentTimeMillis() - start) + " (milliseconds)";
        } else {
            //Fail.
            return "BROKEN LINK";
        }
    }

    private void writeResultIntoDB(String title, String description, String language, String stars, String tags, String time, String pageLoadElapsedTime) {
        MongoDB.addRecord(title, description, language, stars, tags, time, pageLoadElapsedTime);
    }
}
