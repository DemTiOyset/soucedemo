package tests;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import utils.AllureEnvironmentWriter;
import utils.AllureUtils;
import utils.DriverFactory;
import utils.TestConfig;

/**
 * Driver lifecycle for every test class.
 *
 * The browser is created before each test method and is always closed afterwards
 * ({@code alwaysRun = true}), so a failure never leaves a browser process behind.
 * On failure the screenshot and the page source are attached to the Allure report.
 *
 * {@link RetryTransformer} adds {@link InfraRetry}: a test is repeated once when the environment
 * broke it (page not rendered, browser session lost), while assertion failures are never retried.
 */
@Listeners(RetryTransformer.class)
public abstract class BaseTest {

    protected WebDriver driver;
    protected WebDriverWait wait;

    /** Writes the Allure "Environment" block once per suite. */
    @BeforeSuite(alwaysRun = true)
    public void prepareAllureEnvironment() {
        AllureEnvironmentWriter.write();
    }

    /**
     * Creates the browser. The name comes either from the TestNG suite
     * ({@code <parameter name="browser" value="firefox"/>}) or from the Maven property
     * {@code -Dbrowser=firefox}; Chrome is used by default.
     */
    @Parameters({"browser"})
    @BeforeMethod(alwaysRun = true)
    public void setUp(@Optional String browser) {
        String browserName = (browser == null || browser.isBlank()) ? TestConfig.browser() : browser.trim();

        driver = DriverFactory.create(browserName);
        driver.manage().window().setSize(new Dimension(1920, 1080));
        driver.manage().timeouts().pageLoadTimeout(TestConfig.PAGE_LOAD_TIMEOUT);
        wait = new WebDriverWait(driver, TestConfig.TIMEOUT);

        System.out.println("[setUp] " + browserName + " started, headless=" + TestConfig.headless());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        if (driver == null) {
            return;
        }
        if (!result.isSuccess()) {
            AllureUtils.attachScreenshot(driver);
            AllureUtils.attachPageSource(driver);
            AllureUtils.attachBrowserLogs(driver);
        }
        driver.quit();
        System.out.println("[tearDown] " + result.getName() + " -> "
                + (result.isSuccess() ? "PASSED" : "FAILED"));
    }
}
