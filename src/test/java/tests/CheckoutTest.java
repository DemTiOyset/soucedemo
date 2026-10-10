package tests;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.CheckoutPage;
import pages.InventoryPage;
import pages.LoginPage;
import utils.TestConfig;

/**
 * Scenario 4: checkout - the happy path and the validation of an empty required field.
 */
@Epic("SauceDemo")
@Feature("Оформление заказа")
@Owner("Умаров Тимур")
public class CheckoutTest extends BaseTest {

    /** Item 29.99 + 8% tax = 32.39; the value is stable on the demo stand. */
    private static final String EXPECTED_TOTAL = "Total: $32.39";

    @Test(description = "Позитив: полный цикл заказа от корзины до 'Thank you for your order!'")
    @Story("Успешное оформление заказа")
    @Severity(SeverityLevel.BLOCKER)
    public void shouldCompleteCheckout() {
        CheckoutPage checkoutPage = openShippingForm()
                .fillShippingInfo("Timur", "Umarov", "12345")
                .continueToOverview()
                .waitForOverview();

        Assert.assertTrue(checkoutPage.getOrderItemNames().contains(TestConfig.BACKPACK_NAME),
                "В заказе ожидался '" + TestConfig.BACKPACK_NAME + "', фактически: "
                        + checkoutPage.getOrderItemNames());
        Assert.assertEquals(checkoutPage.getTotal(), EXPECTED_TOTAL,
                "Ожидался итог " + EXPECTED_TOTAL + ", фактически: " + checkoutPage.getTotal());

        checkoutPage.finishOrder();
        String header = checkoutPage.getCompleteHeader();
        System.out.println("[check] подтверждение заказа: " + header);

        Assert.assertEquals(header, TestConfig.ORDER_COMPLETE_HEADER,
                "Ожидалось подтверждение заказа, фактически: " + header);
    }

    @Test(description = "Негатив: пустое поле First Name не пропускает дальше оформления")
    @Story("Валидация формы доставки")
    @Severity(SeverityLevel.CRITICAL)
    public void shouldShowValidationErrorForEmptyFirstName() {
        CheckoutPage checkoutPage = openShippingForm()
                .fillShippingInfo("", "Umarov", "12345")
                .continueToOverview();

        Assert.assertTrue(checkoutPage.isErrorDisplayed(),
                "Ожидалось сообщение валидации для пустого поля First Name");

        String error = checkoutPage.getErrorMessage();
        System.out.println("[check] ошибка валидации: " + error);

        Assert.assertTrue(error.contains("First Name is required"),
                "Ожидался текст 'First Name is required', фактически: " + error);
        Assert.assertTrue(driver.getCurrentUrl().contains("/checkout-step-one.html"),
                "Тест должен остаться на первом шаге оформления, фактический URL: " + driver.getCurrentUrl());
    }

    /** Common precondition: log in, add the backpack to the cart and open the shipping form. */
    private CheckoutPage openShippingForm() {
        InventoryPage inventoryPage = new LoginPage(driver, wait).openPage().loginAsStandardUser().waitForLoad();
        inventoryPage.addItemToCart(TestConfig.BACKPACK_SLUG);
        wait.until(d -> inventoryPage.getCartBadgeCount() == 1);

        return inventoryPage.openCart().goToCheckout();
    }
}
