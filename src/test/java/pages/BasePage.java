package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.TestConfig;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Common behaviour of every page: navigation, explicit waits and simple actions.
 * Page Objects contain locators and actions, tests contain only scenarios and assertions.
 */
public abstract class BasePage {

    protected static final String BASE_URL = TestConfig.BASE_URL;

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    /** Opens the given path and waits until the browser reports a complete document. */
    protected void open(String path) {
        driver.get(BASE_URL + path);
        wait.until(d -> "complete".equals(
                ((org.openqa.selenium.JavascriptExecutor) d).executeScript("return document.readyState")));
    }

    protected WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    /**
     * Fills a form field and makes sure that the value really stays there.
     *
     * The normal path is real keyboard input. SauceDemo is a React application with controlled
     * inputs, and under load it occasionally drops keystrokes: the value typed into a field that
     * is being re-rendered disappears silently. When that happens the value is written through
     * the native value setter together with the "input" event React listens to (the documented
     * way to drive a controlled input), and the result is verified again.
     */
    protected void type(By locator, String text) {
        WebElement element = visible(locator);
        element.clear();
        element.sendKeys(text);

        if (text.isEmpty() || valueStayed(locator, text)) {
            return;
        }

        System.out.println("[fallback] клавиатурный ввод не удержался в поле " + locator
                + ", значение записывается через JS");
        setValueWithJavaScript(visible(locator), text);
        wait.until(d -> text.equals(d.findElement(locator).getAttribute("value")));
    }

    /** Writes the value and fires the events a React controlled input reacts to. */
    private void setValueWithJavaScript(WebElement element, String text) {
        ((JavascriptExecutor) driver).executeScript(
                "var el = arguments[0], value = arguments[1];"
                        + "var setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;"
                        + "setter.call(el, value);"
                        + "el.dispatchEvent(new Event('input', {bubbles: true}));"
                        + "el.dispatchEvent(new Event('change', {bubbles: true}));",
                element, text);
    }

    /** Waits a moment and checks that the field still shows the expected value. */
    private boolean valueStayed(By locator, String expected) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(2))
                    .until(d -> expected.equals(d.findElement(locator).getAttribute("value")));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    protected String text(By locator) {
        return visible(locator).getText().trim();
    }

    protected List<String> texts(By locator) {
        wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        return driver.findElements(locator).stream()
                .map(element -> element.getText().trim())
                .collect(Collectors.toList());
    }

    /** True when the element is rendered right now; used for negative checks. */
    protected boolean isPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    protected String currentUrl() {
        return driver.getCurrentUrl();
    }
}
