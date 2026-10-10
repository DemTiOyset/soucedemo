package utils;

import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

import java.io.ByteArrayInputStream;

/**
 * Attachments for the Allure report. They are added only when a test fails,
 * so the report stays readable.
 */
public final class AllureUtils {

    private AllureUtils() {
    }

    public static void attachScreenshot(WebDriver driver) {
        try {
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment("Screenshot on failure", "image/png",
                    new ByteArrayInputStream(screenshot), "png");
        } catch (RuntimeException e) {
            Allure.addAttachment("Screenshot on failure", "text/plain",
                    "Screenshot is not available: " + e.getMessage());
        }
    }

    public static void attachPageSource(WebDriver driver) {
        try {
            Allure.addAttachment("Page source on failure", "text/html", driver.getPageSource(), ".html");
        } catch (RuntimeException e) {
            Allure.addAttachment("Page source on failure", "text/plain",
                    "Page source is not available: " + e.getMessage());
        }
    }

    /**
     * Attaches the browser console. Client-side JavaScript errors from this log explain
     * why a page may stop reacting to input.
     */
    public static void attachBrowserLogs(WebDriver driver) {
        try {
            LogEntries entries = driver.manage().logs().get(LogType.BROWSER);
            StringBuilder text = new StringBuilder();
            for (LogEntry entry : entries) {
                text.append(entry.getLevel()).append(" | ").append(entry.getMessage()).append('\n');
            }
            System.out.println("[browser log]\n" + text);
            Allure.addAttachment("Browser console on failure", "text/plain", text.toString(), ".log");
        } catch (RuntimeException e) {
            System.out.println("[browser log] недоступен: " + e.getMessage());
        }
    }
}
