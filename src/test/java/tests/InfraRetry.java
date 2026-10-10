package tests;

import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.WebDriverException;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Repeats a test only when the environment broke it, never when a check failed.
 *
 * SauceDemo is a public demo stand: a page may not render in time, a browser session may not
 * start, or WebDriver may report a renderer problem. Such runs are repeated once. Assertion
 * errors are never retried, so a real defect still fails the build and cannot be hidden.
 */
public class InfraRetry implements IRetryAnalyzer {

    private static final int MAX_ATTEMPTS = 2;

    private int attempts = 1;

    @Override
    public boolean retry(ITestResult result) {
        Throwable error = result.getThrowable();
        if (error == null || attempts >= MAX_ATTEMPTS || !isInfrastructureFailure(error)) {
            return false;
        }
        attempts++;
        System.out.println("[retry] инфраструктурный сбой в тесте '" + result.getName() + "' ("
                + error.getClass().getSimpleName() + "), повторный запуск");
        return true;
    }

    private boolean isInfrastructureFailure(Throwable error) {
        return error instanceof WebDriverException || error instanceof SessionNotCreatedException
                || error instanceof IllegalStateException;
    }
}
