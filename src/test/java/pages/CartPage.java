package pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

/**
 * Page Object for the shopping cart (https://www.saucedemo.com/cart.html).
 */
public class CartPage extends BasePage {

    private static final By PAGE_TITLE = By.cssSelector(".title");
    private static final By ITEM_NAMES = By.cssSelector(".inventory_item_name");
    private static final By ITEM_PRICES = By.cssSelector(".inventory_item_price");
    private static final By CHECKOUT_BUTTON = By.id("checkout");

    public CartPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    /** Waits until the cart is opened; works for an empty cart as well. */
    public CartPage waitForLoad() {
        wait.until(ExpectedConditions.urlContains("/cart.html"));
        visible(PAGE_TITLE);
        return this;
    }

    public String getTitle() {
        return text(PAGE_TITLE);
    }

    public List<String> getItemNames() {
        return texts(ITEM_NAMES);
    }

    public List<String> getItemPrices() {
        return texts(ITEM_PRICES);
    }

    public int getItemsCount() {
        return driver.findElements(ITEM_NAMES).size();
    }

    public boolean isItemInCart(String itemName) {
        return getItemNames().contains(itemName);
    }

    @Step("Удалить из корзины товар '{itemSlug}'")
    public CartPage removeItem(String itemSlug) {
        click(By.id("remove-" + itemSlug));
        return this;
    }

    @Step("Перейти к оформлению заказа")
    public CheckoutPage goToCheckout() {
        click(CHECKOUT_BUTTON);
        return new CheckoutPage(driver, wait).waitForShippingStep();
    }
}
