package pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.TestConfig;

/**
 * Page Object for the login page (https://www.saucedemo.com/).
 */
public class LoginPage extends BasePage {

    private static final By USERNAME = By.id("user-name");
    private static final By PASSWORD = By.id("password");
    private static final By LOGIN_BUTTON = By.id("login-button");
    private static final By ERROR_MESSAGE = By.cssSelector("h3[data-test='error']");

    public LoginPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    /**
     * Opens the login page and waits until the React application has rendered the form.
     *
     * SauceDemo is a static single-page application hosted on GitHub Pages, so a bundle request
     * can occasionally fail and the page stays empty. The page is reloaded in that case, which
     * keeps the run stable without touching the assertions.
     */
    @Step("Открыть страницу авторизации")
    public LoginPage openPage() {
        for (int attempt = 1; attempt <= 3; attempt++) {
            open("/");
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME));
                return this;
            } catch (TimeoutException e) {
                System.out.println("[retry] страница авторизации не отрисовалась, "
                        + "перезагрузка " + attempt + " из 3");
            }
        }
        throw new IllegalStateException(
                "Страница авторизации не отрисовалась за 3 попытки, текущий URL: " + currentUrl());
    }

    @Step("Войти как {username}")
    public InventoryPage login(String username, String password) {
        type(USERNAME, username);
        type(PASSWORD, password);
        click(LOGIN_BUTTON);
        return new InventoryPage(driver, wait);
    }

    /** Text of the red error banner, for example "Epic sadface: Sorry, this user has been locked out." */
    public String getErrorMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_MESSAGE)).getText().trim();
    }

    public boolean isErrorDisplayed() {
        return isPresent(ERROR_MESSAGE);
    }

    public boolean isLoginButtonDisplayed() {
        return isPresent(LOGIN_BUTTON);
    }

    public String getCurrentUrl() {
        return currentUrl();
    }

    /** Convenience method for the positive scenario. */
    public InventoryPage loginAsStandardUser() {
        return login(TestConfig.STANDARD_USER, TestConfig.PASSWORD);
    }
}
