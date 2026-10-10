package tests;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.InventoryPage;
import pages.LoginPage;
import utils.TestConfig;

/**
 * Scenario 1-2: authentication.
 *
 * Positive case with {@code standard_user} and negative case with {@code locked_out_user}.
 */
@Epic("SauceDemo")
@Feature("Авторизация")
@Owner("Умаров Тимур")
public class LoginTest extends BaseTest {

    @Test(description = "Позитив: standard_user входит и попадает на страницу товаров")
    @Story("Успешный вход")
    @Severity(SeverityLevel.BLOCKER)
    public void shouldLoginWithStandardUser() {
        LoginPage loginPage = new LoginPage(driver, wait).openPage();
        InventoryPage inventoryPage = loginPage.loginAsStandardUser().waitForLoad();

        System.out.println("[check] URL: " + driver.getCurrentUrl());
        System.out.println("[check] title: " + inventoryPage.getTitle());

        Assert.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "Ожидался переход на /inventory.html, фактический URL: " + driver.getCurrentUrl());
        Assert.assertEquals(inventoryPage.getTitle(), TestConfig.PRODUCTS_TITLE,
                "Ожидался заголовок 'Products', фактически: " + inventoryPage.getTitle());
    }

    @Test(description = "Негатив: locked_out_user получает сообщение о блокировке")
    @Story("Заблокированный пользователь")
    @Severity(SeverityLevel.CRITICAL)
    public void shouldShowErrorForLockedOutUser() {
        LoginPage loginPage = new LoginPage(driver, wait).openPage();
        loginPage.login(TestConfig.LOCKED_OUT_USER, TestConfig.PASSWORD);

        String error = loginPage.getErrorMessage();
        System.out.println("[check] error: " + error);

        Assert.assertTrue(error.startsWith("Epic sadface"),
                "Ожидалось сообщение 'Epic sadface...', фактически: " + error);
        Assert.assertTrue(error.contains("locked out"),
                "Ожидалось упоминание блокировки, фактически: " + error);
        Assert.assertTrue(loginPage.isLoginButtonDisplayed(),
                "После неудачного входа форма авторизации должна остаться на странице");
    }
}
