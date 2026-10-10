package tests;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.CartPage;
import pages.InventoryPage;
import pages.LoginPage;
import utils.TestConfig;

import java.util.List;

/**
 * Scenario 3: the shopping cart - adding an item and removing it again.
 */
@Epic("SauceDemo")
@Feature("Корзина")
@Owner("Умаров Тимур")
public class CartTest extends BaseTest {

    @Test(description = "Добавление товара: счётчик = 1, в корзине верные название и цена")
    @Story("Добавление товара в корзину")
    @Severity(SeverityLevel.CRITICAL)
    public void shouldAddItemToCart() {
        InventoryPage inventoryPage = new LoginPage(driver, wait).openPage().loginAsStandardUser().waitForLoad();

        Assert.assertEquals(inventoryPage.getCartBadgeCount(), 0,
                "Сразу после входа корзина должна быть пустой");

        inventoryPage.addItemToCart(TestConfig.BACKPACK_SLUG);
        wait.until(d -> inventoryPage.getCartBadgeCount() == 1);
        System.out.println("[check] счётчик корзины: " + inventoryPage.getCartBadgeCount());

        Assert.assertEquals(inventoryPage.getCartBadgeCount(), 1,
                "Ожидался счётчик корзины 1, фактически: " + inventoryPage.getCartBadgeCount());

        CartPage cartPage = inventoryPage.openCart();
        System.out.println("[check] товары в корзине: " + cartPage.getItemNames()
                + " " + cartPage.getItemPrices());

        Assert.assertTrue(cartPage.isItemInCart(TestConfig.BACKPACK_NAME),
                "Ожидался товар '" + TestConfig.BACKPACK_NAME + "', фактически: " + cartPage.getItemNames());
        Assert.assertEquals(cartPage.getItemPrices(), List.of(TestConfig.BACKPACK_PRICE),
                "Ожидалась цена " + TestConfig.BACKPACK_PRICE + ", фактически: " + cartPage.getItemPrices());
    }

    @Test(description = "Удаление товара: счётчик пропадает, корзина пуста")
    @Story("Удаление товара из корзины")
    @Severity(SeverityLevel.NORMAL)
    public void shouldRemoveItemFromCart() {
        InventoryPage inventoryPage = new LoginPage(driver, wait).openPage().loginAsStandardUser().waitForLoad();

        inventoryPage.addItemToCart(TestConfig.BACKPACK_SLUG);
        wait.until(d -> inventoryPage.getCartBadgeCount() == 1);

        inventoryPage.removeItemFromCart(TestConfig.BACKPACK_SLUG);
        wait.until(d -> !inventoryPage.isCartBadgeDisplayed());
        System.out.println("[check] счётчик после удаления: " + inventoryPage.getCartBadgeCount());

        Assert.assertEquals(inventoryPage.getCartBadgeCount(), 0,
                "После удаления счётчик корзины должен исчезнуть");

        CartPage cartPage = inventoryPage.openCart();
        Assert.assertEquals(cartPage.getItemsCount(), 0,
                "Корзина должна быть пустой, фактически товаров: " + cartPage.getItemsCount());
    }
}
