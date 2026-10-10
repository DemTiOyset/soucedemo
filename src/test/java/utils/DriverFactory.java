package utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.SessionNotCreatedException;
import org.testng.SkipException;

import java.util.Locale;
import java.util.Map;

/**
 * Creates the browser for the requested name, which makes the suite cross-browser.
 *
 * Drivers are resolved by the built-in Selenium Manager, so no WebDriverManager is needed.
 */
public final class DriverFactory {

    private DriverFactory() {
    }

    public static WebDriver create(String browser) {
        try {
            return switch (browser.toLowerCase(Locale.ROOT)) {
                case "chrome" -> new ChromeDriver(chromeOptions());
                case "firefox" -> new FirefoxDriver(firefoxOptions());
                case "edge" -> new EdgeDriver(edgeOptions());
                default -> throw new IllegalArgumentException(
                        "Unknown browser: " + browser + ". Supported values: chrome, firefox, edge.");
            };
        } catch (SessionNotCreatedException e) {
            // A browser may be missing or installed in a way the driver cannot use
            // (for example the Firefox snap on Ubuntu). Such a run is skipped, not failed.
            String reason = "Браузер '" + browser + "' не запустился на этой машине: "
                    + firstLine(e.getMessage())
                    + ". Для Firefox путь к бинарю задаётся свойством -Dfirefox.binary=/путь/к/firefox";
            System.out.println("[skip] " + reason);
            throw new SkipException(reason, e);
        }
    }

    private static String firstLine(String message) {
        if (message == null) {
            return "нет описания";
        }
        int end = message.indexOf('\n');
        return end < 0 ? message : message.substring(0, end);
    }

    private static ChromeOptions chromeOptions() {
        ChromeOptions options = new ChromeOptions();
        if (TestConfig.headless()) {
            options.addArguments("--headless=new");
        }
        options.addArguments(
                "--window-size=1920,1080",
                "--disable-gpu",
                "--no-sandbox",
                "--disable-dev-shm-usage");
        // Browser console is collected so that client-side JavaScript errors can be analysed
        // from the Allure report when a test fails.
        options.setCapability("goog:loggingPrefs", Map.of("browser", "ALL"));
        return options;
    }

    private static FirefoxOptions firefoxOptions() {
        FirefoxOptions options = new FirefoxOptions();

        // Needed when Firefox is installed as a snap (Ubuntu) or into a custom directory:
        // the snap wrapper is a shell script and the driver cannot start it.
        String binary = TestConfig.firefoxBinary();
        System.out.println("[driver] firefox binary: "
                + (binary.isEmpty() ? "по умолчанию (PATH)" : binary));
        if (!binary.isEmpty()) {
            options.setBinary(binary);
        }

        if (TestConfig.headless()) {
            options.addArguments("-headless");
        }
        options.addArguments("--width=1920", "--height=1080");
        return options;
    }

    private static EdgeOptions edgeOptions() {
        if (System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("linux")) {
            // Edge does not exist for Linux: the test is skipped instead of failing.
            throw new SkipException(
                    "Microsoft Edge is not available on Linux. Use -Dbrowser=chrome or -Dbrowser=firefox, "
                            + "or run the Edge profile on Windows/macOS.");
        }
        EdgeOptions options = new EdgeOptions();
        if (TestConfig.headless()) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1920,1080");
        return options;
    }
}
