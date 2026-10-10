package pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

/**
 * Page Object for the product list (https://www.saucedemo.com/inventory.html).
 */
public class InventoryPage extends BasePage {

    private static final By PAGE_TITLE = By.cssSelector(".title");
    private static final By ITEM_NAMES = By.cssSelector(".inventory_item_name");
    private static final By ITEM_PRICES = By.cssSelector(".inventory_item_price");
    private static final By SORT_DROPDOWN = By.cssSelector(".product_sort_container");
    private static final By CART_BADGE = By.cssSelector(".shopping_cart_badge");
    private static final By CART_LINK = By.cssSelector(".shopping_cart_link");

    public InventoryPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    /** Waits until the product list is really opened. */
    public InventoryPage waitForLoad() {
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        visible(PAGE_TITLE);
        return this;
    }

    /** Page heading, expected to be "Products". */
    public String getTitle() {
        return text(PAGE_TITLE);
    }

    @Step("Добавить в корзину товар '{itemSlug}'")
    public InventoryPage addItemToCart(String itemSlug) {
        click(By.id("add-to-cart-" + itemSlug));
        return this;
    }

    @Step("Удалить из корзины товар '{itemSlug}'")
    public InventoryPage removeItemFromCart(String itemSlug) {
        click(By.id("remove-" + itemSlug));
        return this;
    }

    /** Number on the cart icon; 0 when the badge is not rendered (the cart is empty). */
    public int getCartBadgeCount() {
        List<WebElement> badges = driver.findElements(CART_BADGE);
        if (badges.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(badges.get(0).getText().trim());
    }

    public boolean isCartBadgeDisplayed() {
        return isPresent(CART_BADGE);
    }

    /** Product names in the order they are shown on the page. */
    public List<String> getItemNames() {
        return texts(ITEM_NAMES);
    }

    /** Product prices in the order they are shown on the page, for example [29.99, 9.99, ...]. */
    public List<Double> getItemPrices() {
        return texts(ITEM_PRICES).stream()
                .map(price -> Double.parseDouble(price.replace("$", "")))
                .toList();
    }

    @Step("Отсортировать товары: '{option}'")
    public InventoryPage sortBy(String option) {
        new Select(visible(SORT_DROPDOWN)).selectByVisibleText(option);
        return this;
    }

    public String getSelectedSortOption() {
        return new Select(visible(SORT_DROPDOWN)).getFirstSelectedOption().getText().trim();
    }

    @Step("Перейти в корзину")
    public CartPage openCart() {
        click(CART_LINK);
        return new CartPage(driver, wait).waitForLoad();
    }
}
