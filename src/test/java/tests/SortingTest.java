package tests;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.InventoryPage;
import pages.LoginPage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Scenario 5: sorting of the product list.
 *
 * One data-driven test covers all four options of the dropdown.
 * The expected order is not hardcoded: the test takes the actual list, sorts a copy of it
 * and compares, so it fails exactly when the page shows a wrong order.
 */
@Epic("SauceDemo")
@Feature("Сортировка товаров")
@Owner("Умаров Тимур")
public class SortingTest extends BaseTest {

    private enum SortKey {
        NAME, PRICE
    }

    @DataProvider(name = "sortingOptions")
    public Object[][] sortingOptions() {
        return new Object[][]{
                {"Name (A to Z)", SortKey.NAME, true},
                {"Name (Z to A)", SortKey.NAME, false},
                {"Price (low to high)", SortKey.PRICE, true},
                {"Price (high to low)", SortKey.PRICE, false},
        };
    }

    @Test(dataProvider = "sortingOptions",
            description = "Сортировка списка товаров: A-Z, Z-A, цена по возрастанию и убыванию")
    @Story("Сортировка каталога")
    @Severity(SeverityLevel.NORMAL)
    public void shouldSortProducts(String option, SortKey sortKey, boolean ascending) {
        InventoryPage inventoryPage = new LoginPage(driver, wait).openPage().loginAsStandardUser().waitForLoad();

        inventoryPage.sortBy(option);
        wait.until(d -> isSorted(inventoryPage, sortKey, ascending));

        Assert.assertEquals(inventoryPage.getSelectedSortOption(), option,
                "Ожидалась выбранная опция '" + option + "', фактически: "
                        + inventoryPage.getSelectedSortOption());

        if (sortKey == SortKey.NAME) {
            List<String> actual = inventoryPage.getItemNames();
            System.out.println("[check] " + option + " -> " + actual);
            Assert.assertEquals(actual, sortedCopy(actual, ascending),
                    "Названия товаров должны идти по алфавиту (ascending=" + ascending + "), фактически: " + actual);
        } else {
            List<Double> actual = inventoryPage.getItemPrices();
            System.out.println("[check] " + option + " -> " + actual);
            Assert.assertEquals(actual, sortedCopy(actual, ascending),
                    "Цены товаров должны идти по возрастанию/убыванию (ascending=" + ascending
                            + "), фактически: " + actual);
        }
    }

    /** Reads the current list once and checks that it is already ordered as expected. */
    private boolean isSorted(InventoryPage inventoryPage, SortKey sortKey, boolean ascending) {
        if (sortKey == SortKey.NAME) {
            List<String> actual = inventoryPage.getItemNames();
            return !actual.isEmpty() && actual.equals(sortedCopy(actual, ascending));
        }
        List<Double> actual = inventoryPage.getItemPrices();
        return !actual.isEmpty() && actual.equals(sortedCopy(actual, ascending));
    }

    private static <T extends Comparable<T>> List<T> sortedCopy(List<T> source, boolean ascending) {
        List<T> copy = new ArrayList<>(source);
        copy.sort(ascending ? Comparator.naturalOrder() : Comparator.reverseOrder());
        return copy;
    }
}
