package utils;

import java.time.Duration;

/**
 * Single place for the test configuration: URL, demo accounts, timeouts and browser settings.
 *
 * Values can be overridden from the command line, for example:
 *   mvn test -Dbrowser=firefox -Dheadless=false -Dtimeout.seconds=30
 */
public final class TestConfig {

    private TestConfig() {
    }

    public static final String BASE_URL = "https://www.saucedemo.com";

    /** Demo accounts of Swag Labs: one shared password for all of them. */
    public static final String STANDARD_USER = "standard_user";
    public static final String LOCKED_OUT_USER = "locked_out_user";
    public static final String PASSWORD = "secret_sauce";

    /** Product used by the cart and checkout scenarios. */
    public static final String BACKPACK_SLUG = "sauce-labs-backpack";
    public static final String BACKPACK_NAME = "Sauce Labs Backpack";
    public static final String BACKPACK_PRICE = "$29.99";

    public static final String PRODUCTS_TITLE = "Products";
    public static final String ORDER_COMPLETE_HEADER = "Thank you for your order!";

    public static final Duration TIMEOUT = Duration.ofSeconds(
            Integer.parseInt(System.getProperty("timeout.seconds", "20")));

    /** SauceDemo is a public stand, so the page load limit is more forgiving than the waits. */
    public static final Duration PAGE_LOAD_TIMEOUT = Duration.ofSeconds(
            Integer.parseInt(System.getProperty("page.load.timeout.seconds", "60")));

    /** Keep in sync with pom.xml — these values are published into the Allure environment block. */
    public static final String SELENIUM_VERSION = "4.48.0";
    public static final String TESTNG_VERSION = "7.10.2";

    /** Browser for a plain {@code mvn test}: chrome, firefox or edge. */
    public static String browser() {
        return System.getProperty("browser", "chrome").trim().toLowerCase();
    }

    public static boolean headless() {
        return !"false".equalsIgnoreCase(System.getProperty("headless", "true"));
    }

    /**
     * Optional explicit path to the Firefox binary.
     * Needed when Firefox is installed as a snap (Ubuntu) or outside the PATH:
     * {@code mvn test -Dbrowser=firefox -Dfirefox.binary=/path/to/firefox}
     */
    public static String firefoxBinary() {
        return System.getProperty("firefox.binary", "").trim();
    }
}
