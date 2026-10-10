package utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Fills the "Environment" widget of the Allure report: OS, JDK, library versions,
 * browser and the way drivers are resolved.
 *
 * The file is written into target/allure-results before the first test runs,
 * so it is picked up by {@code mvn allure:report}.
 */
public final class AllureEnvironmentWriter {

    private static final Path RESULTS_DIR = Path.of("target", "allure-results");
    private static final Path ENVIRONMENT_FILE = RESULTS_DIR.resolve("environment.properties");

    private AllureEnvironmentWriter() {
    }

    public static void write() {
        Properties properties = new Properties();
        properties.setProperty("os.name", System.getProperty("os.name"));
        properties.setProperty("os.version", System.getProperty("os.version"));
        properties.setProperty("os.arch", System.getProperty("os.arch"));
        properties.setProperty("jdk.version", System.getProperty("java.version"));
        properties.setProperty("selenium.version", TestConfig.SELENIUM_VERSION);
        properties.setProperty("testng.version", TestConfig.TESTNG_VERSION);
        properties.setProperty("browser", TestConfig.browser());
        properties.setProperty("headless", String.valueOf(TestConfig.headless()));
        properties.setProperty("driver.manager", "Selenium Manager");
        properties.setProperty("base.url", TestConfig.BASE_URL);

        try {
            Files.createDirectories(RESULTS_DIR);
            try (OutputStream out = Files.newOutputStream(ENVIRONMENT_FILE)) {
                properties.store(out, "Allure environment: written by AllureEnvironmentWriter");
            }
            System.out.println("[allure] environment written to " + ENVIRONMENT_FILE.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("[allure] could not write environment.properties: " + e.getMessage());
        }
    }
}
