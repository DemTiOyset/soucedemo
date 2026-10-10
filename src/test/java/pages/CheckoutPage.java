package pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Page Object for the checkout flow: shipping form, order overview and the final confirmation.
 */
public class CheckoutPage extends BasePage {

    // Step one: shipping information
    private static final By FIRST_NAME = By.id("first-name");
    private static final By LAST_NAME = By.id("last-name");
    private static final By POSTAL_CODE = By.id("postal-code");
    private static final By CONTINUE_BUTTON = By.id("continue");
    private static final By ERROR_MESSAGE = By.cssSelector("h3[data-test='error']");

    // Step two: overview
    private static final By ITEM_NAMES = By.cssSelector(".inventory_item_name");
    private static final By SUMMARY_TOTAL = By.cssSelector(".summary_total_label");
    private static final By FINISH_BUTTON = By.id("finish");

    // Step three: confirmation
    private static final By COMPLETE_HEADER = By.cssSelector(".complete-header");

    public CheckoutPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public CheckoutPage waitForShippingStep() {
        wait.until(ExpectedConditions.urlContains("/checkout-step-one.html"));
        visible(FIRST_NAME);
        return this;
    }

    @Step("Заполнить данные доставки: {firstName} {lastName}, индекс {postalCode}")
    public CheckoutPage fillShippingInfo(String firstName, String lastName, String postalCode) {
        type(FIRST_NAME, firstName);
        type(LAST_NAME, lastName);
        type(POSTAL_CODE, postalCode);
        return this;
    }

    /**
     * SauceDemo renders the checkout form on the client side, and the first click on "Continue"
     * is occasionally swallowed: nothing happens at all - neither navigation nor a validation
     * message. The click is therefore repeated until the page really reacts, which removes that
     * instability without hiding a real assertion failure.
     */
    /**
     * SauceDemo renders the checkout form on the client side, and a click that lands while
     * the component is re-rendering can be swallowed: nothing happens at all - neither
     * navigation nor a validation message. The click is therefore repeated until the page
     * really reacts, which removes that instability without hiding a real assertion failure.
     */
    @Step("Продолжить оформление заказа")
    public CheckoutPage continueToOverview() {
        for (int attempt = 1; attempt <= 3; attempt++) {
            click(CONTINUE_BUTTON);
            try {
                new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.or(
                        ExpectedConditions.urlContains("/checkout-step-two.html"),
                        ExpectedConditions.visibilityOfElementLocated(ERROR_MESSAGE)));
                return this;
            } catch (TimeoutException e) {
                System.out.println("[retry] клик Continue не дал эффекта, попытка " + attempt + " из 3");
            }
        }
        // On the demo stand the checkout page occasionally stops reacting to any action at all:
        // the form is rendered, but neither typing nor clicking reaches the application.
        // A clear message is more useful here than a bare timeout.
        throw new IllegalStateException("Страница оформления заказа перестала реагировать: ни перехода, "
                + "ни сообщения валидации. Это известная нестабильность демо-стенда SauceDemo, "
                + "а не результат проверки. Повторите прогон. URL: " + currentUrl());
    }

    /** Waits until the overview page with the order summary is opened. */
    public CheckoutPage waitForOverview() {
        try {
            wait.until(ExpectedConditions.urlContains("/checkout-step-two.html"));
        } catch (TimeoutException e) {
            // A bare timeout is hard to diagnose, so report what the form said.
            if (isErrorDisplayed()) {
                throw new AssertionError("Форма доставки не была принята: " + getErrorMessage()
                        + " (URL: " + currentUrl() + ")");
            }
            throw e;
        }
        visible(SUMMARY_TOTAL);
        return this;
    }

    /** Validation message of the shipping form, for example "Error: First Name is required". */
    public String getErrorMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_MESSAGE)).getText().trim();
    }

    public boolean isErrorDisplayed() {
        return isPresent(ERROR_MESSAGE);
    }

    /** Order total together with the tax, for example "Total: $32.39". */
    public String getTotal() {
        return text(SUMMARY_TOTAL);
    }

    public List<String> getOrderItemNames() {
        return texts(ITEM_NAMES);
    }

    @Step("Завершить заказ")
    public CheckoutPage finishOrder() {
        click(FINISH_BUTTON);
        return this;
    }

    /** Waits for the confirmation page and returns its heading. */
    public String getCompleteHeader() {
        wait.until(ExpectedConditions.urlContains("/checkout-complete.html"));
        return text(COMPLETE_HEADER);
    }
}
