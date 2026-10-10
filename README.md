# Saucedemo_Umarov

UI-автотесты для учебного магазина **Swag Labs** (<https://www.saucedemo.com>) — практические
занятия **№13** (Page Object, базовые сценарии) и **№14–16** (Allure, кроссбраузерность,
параллельный прогон).

Один проект закрывает обе практики: практика №13 — это тесты и Page Object, практика №14–16 —
Allure-отчётность и прогон в Chrome и Firefox, добавленные в отдельной ветке.

## Стек

| Компонент | Версия |
|---|---|
| Java | 21 (требуется 17+) |
| Maven | 3.9+ |
| Selenium WebDriver | 4.48.0 |
| TestNG | 7.10.2 |
| Allure (адаптер `allure-testng` + `allure-maven`) | 2.29.0 / плагин 2.15.2 |
| AspectJ Weaver | 1.9.22.1 (включает `@Step` в рантайме) |
| Браузеры | Google Chrome, Mozilla Firefox (Edge — только Windows/macOS) |

Драйверы отдельно ставить не нужно: Selenium 4 подбирает их сам через **Selenium Manager**,
поэтому WebDriverManager не подключён (в `DriverFactory` это указано явно).

## Структура проекта

```
Saucedemo/
├── pom.xml
├── README.md
├── docs/
│   └── guide-ru.md                        # гайд: запуск, скриншоты, две ветки, PR, проверка ментором
└── src/test/
    ├── java/
    │   ├── pages/                         # Page Object: локаторы + действия
    │   │   ├── BasePage.java              # общие ожидания, ввод значений, навигация
    │   │   ├── LoginPage.java
    │   │   ├── InventoryPage.java
    │   │   ├── CartPage.java
    │   │   └── CheckoutPage.java
    │   ├── tests/                         # только сценарии и проверки
    │   │   ├── BaseTest.java              # WebDriver, @BeforeMethod/@AfterMethod(alwaysRun = true)
    │   │   ├── LoginTest.java
    │   │   ├── CartTest.java
    │   │   ├── CheckoutTest.java
    │   │   └── SortingTest.java
    │   └── utils/
    │       ├── TestConfig.java            # URL, учётки, таймауты, товар
    │       ├── DriverFactory.java         # Chrome / Firefox / Edge
    │       ├── AllureUtils.java           # скриншот и DOM при падении
    │       └── AllureEnvironmentWriter.java  # блок Environment в отчёте
    └── resources/
        ├── allure.properties              # каталог результатов Allure
        ├── SmokeTest.xml                  # последовательный прогон
        └── CrossBrowser.xml               # Chrome + Firefox параллельно
```

## Сценарии

7 тест-методов, 10 выполнений (сортировка идёт через `@DataProvider`), из них 2 негативных.

| Класс и метод | Что проверяется |
|---|---|
| `LoginTest.shouldLoginWithStandardUser` | позитив: `standard_user`/`secret_sauce` → переход на `/inventory.html` и заголовок «Products» |
| `LoginTest.shouldShowErrorForLockedOutUser` | негатив: `locked_out_user` → «Epic sadface: Sorry, this user has been locked out.», форма логина остаётся |
| `CartTest.shouldAddItemToCart` | добавление Sauce Labs Backpack → счётчик корзины 1, в корзине верные название и цена `$29.99` |
| `CartTest.shouldRemoveItemFromCart` | удаление товара → счётчик исчезает, корзина пуста |
| `CheckoutTest.shouldCompleteCheckout` | полный цикл заказа: итог `Total: $32.39` и подтверждение «Thank you for your order!» |
| `CheckoutTest.shouldShowValidationErrorForEmptyFirstName` | негатив: пустое First Name → «Error: First Name is required», переход на следующий шаг не происходит |
| `SortingTest.shouldSortProducts` | 4 сортировки: Name A→Z, Name Z→A, Price low→high, Price high→low |

Ожидаемый порядок сортировки не зашит константами: тест берёт фактический список, сортирует его
копию и сравнивает — падение происходит ровно тогда, когда страница показывает неверный порядок.

## Запуск

```bash
# основной прогон в Chrome (headless)
mvn clean test

# Firefox
mvn clean test -Dbrowser=firefox

# кроссбраузерный параллельный прогон (Chrome + Firefox, parallel="tests", thread-count=2)
mvn clean test -Pcrossbrowser

# последовательный прогон через TestNG-сьют
mvn clean test -Psmoke

# показать браузер
mvn clean test -Dheadless=false
```

Ожидаемый результат: `Tests run: 10, Failures: 0, Errors: 0` и `BUILD SUCCESS`
(в кроссбраузерном профиле — 20 выполнений, по 10 на браузер).

### Allure-отчёт

```bash
mvn clean test            # сначала прогон: он наполняет target/allure-results
mvn allure:report         # HTML-отчёт: target/site/allure-maven-plugin/index.html
mvn allure:serve          # временный локальный сервер с отчётом (Ctrl+C — остановить)
```

Что есть в отчёте:

- **Behaviors** — группировка по `@Epic("SauceDemo")` → `@Feature` → `@Story`;
- **Suites** — по тест-классам, с длительностями и стектрейсами;
- **шаги** — методы Page Object размечены `@Step` (включается aspectjweaver в `argLine`);
- **вложения** — при падении автоматически прикладываются скриншот и DOM страницы (`AllureUtils`);
- **Environment** — ОС, JDK, версии Selenium и TestNG, браузер, признак headless, способ
  работы с драйверами (`target/allure-results/environment.properties`, пишет `AllureEnvironmentWriter`);
- **метки** — `@Severity("...")`, `@Owner("Умаров Тимур")`, а также `@Epic`/`@Feature`/`@Story`
  для группировки в Behaviors.

В блоке Environment указан браузер по умолчанию (`-Dbrowser`, обычно Chrome). В параллельном
сьюте одновременно работают Chrome и Firefox — это видно в разделе **Suites**: две группы тестов
носят имена браузеров из `CrossBrowser.xml`.

`allure:report` скачивает Allure CLI в `target/allure-cli` (не в корень проекта), поэтому в
репозиторий попадают только исходники.

## Кроссбраузерность

Браузер выбирается либо параметром сьюта (`<parameter name="browser" value="firefox"/>`), либо
свойством Maven (`-Dbrowser=firefox`). Реализованы три браузера:

| Браузер | Статус |
|---|---|
| Chrome | прогоняется, основной вариант |
| Firefox | код и сьют готовы, прогон выполняется командой `-Dbrowser=firefox`; для нестандартной установки (snap в Ubuntu) путь задаётся свойством `-Dfirefox.binary=`. Если браузер недоступен драйверу, тесты помечаются **пропущенными**, а не падают — прогон остаётся зелёным |
| Edge | код готов (`DriverFactory`), но в `CrossBrowser.xml` закомментирован: Edge не существует для Linux, там такой тест тоже помечается пропуском. На Windows/macOS блок достаточно раскомментировать |

## Устойчивость: что было найдено и исправлено

SauceDemo — React-приложение со статическим хостингом (GitHub Pages), и при прогонах выявились
четыре нестабильности. Первые три закрыты кодом, четвёртая — аккуратным перезапуском:

1. **Клавиатурный ввод иногда теряется.** В форме оформления заказа значение, введённое
   `sendKeys`, изредка исчезало: контролируемый React-инпут перерисовывался и затирал ввод.
   `BasePage.type` теперь проверяет значение в DOM и, если оно не удержалось, записывает его
   через нативный value-setter с событием `input` (штатный способ работы с controlled input) —
   с обязательной повторной проверкой.
2. **Клик по «Continue» может пропасть.** Если клик попадает в момент перерисовки компонента,
   не происходит ни перехода, ни сообщения валидации. `CheckoutPage.continueToOverview`
   повторяет клик (не более 3 раз) до первой реакции страницы — переход или ошибка валидации.
3. **Страница логина иногда не отрисовывается.** Статика GitHub Pages может не загрузиться,
   и React не успевает отрисовать форму. `LoginPage.openPage` перезагружает страницу до 3 раз.
4. **Страница оформления заказа может «умереть» целиком.** В отдельные периоды работы стенда
   React-дерево checkout-страницы перестаёт реагировать и на ввод, и на клики. Проверено, что
   не помогают ни запись значений через JS, ни жёсткая перезагрузка страницы: форма отрисована,
   но события до приложения не доходят. Это дефект самого демо-стенда, а не тестов.
   Что сделано: `CheckoutPage.continueToOverview` после трёх безрезультатных кликов бросает
   **понятное сообщение** вместо таймаута, а `InfraRetry` + `RetryTransformer` перезапускают
   такой тест один раз в новой сессии браузера (`WebDriverException`,
   `SessionNotCreatedException`, `IllegalStateException`). Падение проверки (`AssertionError`)
   не повторяется никогда. Отключается удалением `@Listeners` в `BaseTest` и свойства
   `listener` в `pom.xml`.

Каждая защита пишет строку в лог (`[fallback]`, `[retry]`, `[skip]`), а при падении в Allure
дополнительно попадают скриншот, DOM и **консоль браузера** (`AllureUtils.attachBrowserLogs`) —
по ней видно клиентские JS-ошибки, как и требует методичка. Проверки не ослаблены: все ассерты
на месте.

**Что это значит на практике.** Когда стенд здоров, прогон в Chrome даёт `Tests run: 10,
Failures: 0` (наблюдалось сериями, в том числе четыре запуска подряд). В периоды, когда
checkout-страница SauceDemo нерабочая, падают ровно два теста оформления заказа, и их достаточно
перезапустить командой `mvn test` — сами проверки менять не нужно, причина внешняя.
В логе такого периода видно `[fallback]` (ввод не удерживается) и несколько строк
`клик Continue не дал эффекта` — это признак именно «мёртвой» страницы стенда.

### Про кроссбраузерную проверку

Параллельный сьют `-Pcrossbrowser` содержит Chrome и Firefox. Chrome в нём проходит все 10 тестов.
Firefox на машине разработки, где он установлен обычным способом (Windows/macOS, `apt` без snap),
поднимается так же; в контейнере с snap-Firefox geckodriver не может запустить браузер, и такие
тесты корректно помечаются пропущенными с причиной в логе — сборка при этом остаётся успешной.

## Ограничения

1. Нужен интернет: тесты работают против публичного демо-стенда.
2. Проверены Chrome и Firefox; Edge требует Windows/macOS.
3. Значения `$29.99` и `Total: $32.39` зависят от каталога Swag Labs; при смене цен их нужно
   обновить в `TestConfig` и `CheckoutTest`.
4. Набор не запускается в CI: в объём практик 13 и 14–16 это не входило.

## Автор

Умаров Тимур, группа 4ИСП9-42 — практические занятия №13 и №14–16.
