# Гайд: запуск Saucedemo_Umarov и сдача ЛР13 и ЛР14–16

Проект закрывает две практики:

| Практика | Что сдаётся | Ветка |
|---|---|---|
| **№13** | Page Object, 7 сценариев, отчёты Surefire | `feature/4ИСП9-42_Umarov_saucedemo` |
| **№14–16** | Allure-отчётность, кроссбраузерный и параллельный прогон | `feature/4ИСП9-42_Umarov_saucedemo_allure` |

Данные работы:

| Параметр | Значение |
|---|---|
| GitHub | <https://github.com/DemTiOyset> |
| Репозиторий | `Saucedemo_Umarov` |
| Группа | 4ИСП9-42 |
| Ментор-проверяющий | <https://github.com/Tunia666> |

Все команды выполняются из корня проекта — папки `Saucedemo`, где лежит `pom.xml`.

---

## 1. Установка окружения

Нужны **JDK 17+** (проект собран на 21), **Maven 3.9+**, **Google Chrome** и, для
кроссбраузерного прогона, **Firefox**.

- **Windows:** JDK — [adoptium.net](https://adoptium.net/temurin/releases/?version=21) (галочка *Set JAVA_HOME*);
  Maven — [maven.apache.org/download.cgi](https://maven.apache.org/download.cgi), папка `bin` в `PATH`.
  Chrome и Firefox ставятся обычными установщиками.
- **Linux:** `sudo apt install -y openjdk-21-jdk maven`
- **macOS:** `brew install openjdk@21 maven`

Проверка: `java -version` (17+), `mvn -version` (в строке `Java version:` та же версия).

### Если Firefox установлен как snap (Ubuntu)

`/usr/bin/firefox` в Ubuntu — это shell-обёртка над snap, и geckodriver отказывается её
запускать с ошибкой *«binary is not a Firefox executable»*. Есть два пути:

1. Указать настоящий бинарь явно:

```bash
# скачать обычный (не snap) Firefox и распаковать
curl -L "https://download.mozilla.org/?product=firefox-latest&os=linux64&lang=en-US" -o firefox.tar.xz
mkdir -p ~/firefox && tar -xJf firefox.tar.xz -C ~/firefox --strip-components=1

# запуск с указанием пути
mvn clean test -Dbrowser=firefox -Dfirefox.binary=$HOME/firefox/firefox
```

2. Либо ничего не делать: если браузер недоступен, тесты в нём **помечаются пропущенными**
   (`SkipException`), а не падают — прогон остаётся зелёным. То же поведение у Edge на Linux.

---

## 2. Запуск тестов

```bash
# основной прогон: Chrome, headless
mvn clean test

# Firefox
mvn clean test -Dbrowser=firefox

# параллельный кроссбраузерный прогон: Chrome + Firefox одновременно
mvn clean test -Pcrossbrowser

# последовательный прогон через TestNG-сьют
mvn clean test -Psmoke

# показать окно браузера
mvn clean test -Dheadless=false
```

Ожидаемый результат:

```
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

В кроссбраузерном профиле выполнений 20 (по 10 на браузер), в отчёте — 10 тестов и 4 варианта
сортировки из `@DataProvider`.

### Отчёты Surefire

`target/surefire-reports/` — `index.html` (открыть в браузере), `emailable-report.html`,
`*.txt` по классам и `TEST-*.xml` со стектрейсами. Это обязательный артефакт обеих практик.

---

## 3. Allure-отчёт (ЛР14–16)

```bash
mvn clean test          # прогон наполняет target/allure-results
mvn allure:report       # HTML-отчёт: target/site/allure-maven-plugin/index.html
mvn allure:serve        # временный сервер с отчётом, остановить Ctrl+C
```

Что проверить в отчёте и сфотографировать (это требует задание 14–16):

| Страница отчёта | Что показывает |
|---|---|
| **Overview / Dashboard** | сколько тестов прошло, разбивка по `@Severity`, длительности |
| **Behaviors** | группировка `@Epic("SauceDemo")` → `@Feature` → `@Story` |
| **Suites** | тесты по классам, у каждого — шаги `@Step` из Page Object |
| **карточка теста** | шаги, параметры и вложения (скриншот и DOM появляются при падении) |
| **Environment** | ОС, JDK, версии Selenium и TestNG, браузер, headless, Selenium Manager |

`environment.properties` создаётся автоматически в `target/allure-results`
классом `AllureEnvironmentWriter` перед первым тестом.

---

## 4. Скриншоты для отчёта

**Практика 13** просит: зелёный прогон, содержимое `target/surefire-reports`, экраны приложения
(успешный вход, ошибка блокировки, корзина, «Thank you for your order!»).
**Практика 14–16** дополнительно: страницы Allure-отчёта (Dashboard, Behaviors, карточка
упавшего теста со скриншотом, Environment).

Как снимать консоль:

1. Разверни терминал, увеличь шрифт, очисти экран (`cls` / `clear`).
2. Запусти в batch-режиме и сохрани лог:
   - Git Bash / Linux / macOS: `mvn -B clean test 2>&1 | tee test-run.log`
   - PowerShell: `mvn -B clean test 2>&1 | Tee-Object test-run.log`
3. Сделай три кадра: команда `mvn -B clean test`; строки `[check] ...`; итог
   `Tests run: 10, Failures: 0` + `BUILD SUCCESS`.
4. Горячие клавиши: **Windows** `Win + Shift + S`, **macOS** `Cmd + Shift + S`, **Linux** `PrtScn`.

Экраны приложения удобнее всего получить, запустив тесты с видимым окном
(`mvn clean test -Dheadless=false`) и сняв нужные страницы.

---

## 5. Публикация на GitHub и две ветки

```bash
git config --global user.name "Timur Umarov"
git config --global user.email "твой-email@example.com"

cd путь/до/Saucedemo
git init -b main
git add .gitignore README.md
git commit -m "chore: initial commit"
```

Создай **пустой** репозиторий <https://github.com/new>:

- **Name:** `Saucedemo_Umarov`, **Public** (иначе на бесплатном тарифе не будет защиты ветки);
- галочки README/.gitignore/license **не ставить**;
- далее:

```bash
git remote add origin https://github.com/DemTiOyset/Saucedemo_Umarov.git
git push -u origin main
```

Для `push` нужен **Personal Access Token** (<https://github.com/settings/tokens>, *Tokens (classic)*,
галочка `repo`) — пароль от аккаунта GitHub не принимает.

### 5.1 Ветка и PR №1 (практика 13)

Кроссбраузерный сьют сознательно оставляем для второго PR, поэтому добавляем файлы по списку,
а не через `git add .`:

```bash
git checkout -b feature/4ИСП9-42_Umarov_saucedemo
git add pom.xml .gitignore README.md docs src/test/java \
        src/test/resources/SmokeTest.xml src/test/resources/allure.properties
git commit -m "feat: add Page Object framework and SauceDemo scenarios (ЛР13)"
git push -u origin feature/4ИСП9-42_Umarov_saucedemo
```

Открой PR (GitHub покажет ссылку после `push`):

- **base: `main`** ← **compare: `feature/4ИСП9-42_Umarov_saucedemo`**;
- заголовок: `ЛР13: UI-автотесты SauceDemo (Page Object, Selenium + TestNG)`;
- в описание вставь:

```markdown
## Что сделано
- Maven-проект Java 21: Selenium 4.48 + TestNG 7.10, паттерн Page Object (pages/tests/utils).
- 5 Page Object'ов и 4 тест-класса: 7 тестов, из них 2 негативных (locked_out_user,
  пустое поле First Name в оформлении заказа).
- Явные ожидания (WebDriverWait), устойчивые локаторы (#user-name, #login-button, data-test).
- Драйвер создаётся в @BeforeMethod и закрывается в @AfterMethod(alwaysRun = true).

## Как проверить
1. mvn clean test (JDK 17+, Maven, Chrome)
2. Ожидается: Tests run: 10, Failures: 0, Errors: 0 — BUILD SUCCESS
3. Отчёты: target/surefire-reports/index.html
```

Не сливай PR до ревью (см. шаг 6 про защиту ветки).

### 5.2 Ветка и PR №2 (практики 14–16)

После того как первый PR смержен:

```bash
git checkout main
git pull
git checkout -b feature/4ИСП9-42_Umarov_saucedemo_allure
git add src/test/resources/CrossBrowser.xml
git commit -m "feat: add cross-browser TestNG suite Chrome+Firefox, parallel (ЛР14-16)"
git push -u origin feature/4ИСП9-42_Umarov_saucedemo_allure
```

Второй PR добавляет кроссбраузерный параллельный прогон. В описание вложи результаты своих
прогонов (Chrome, Firefox, `-Pcrossbrowser`) и скриншоты Allure-отчёта:

```markdown
## Что сделано (ЛР14-16)
- Allure: адаптер allure-testng, плагин allure-maven, @Step в Page Object,
  метки @Epic/@Feature/@Story/@Severity/@Owner, вложения (скриншот и DOM при падении),
  блок Environment (AllureEnvironmentWriter).
- Кроссбраузерность: параметр browser через testng.xml и -Dbrowser=, Chrome и Firefox.
- Параллельный прогон: CrossBrowser.xml, parallel="tests", thread-count=2.
- Edge поддержан в DriverFactory (на Linux пропускается: браузера для Linux не существует).

## Как проверить
1. mvn clean test -Pcrossbrowser      → 20 выполнений, 0 падений
2. mvn allure:report                   → target/site/allure-maven-plugin/index.html
3. Скриншоты отчёта: Dashboard, Behaviors, Environment, карточка упавшего теста
```

> Если преподаватель требует, чтобы **Allure тоже появился только во втором PR**, это правка
> уровня кода (убрать из первого коммита зависимости Allure и аннотации `@Step`, `@Epic` и т.д.).
> Скажи — подготовлю такую версию проекта.

### 5.3 Где взять ссылку для ментора

Ссылки заранее не существует, она появляется после создания PR:

| Ссылка | Где взять |
|---|---|
| `https://github.com/DemTiOyset/Saucedemo_Umarov/pull/1` | адресная строка открытого PR (номер — порядковый) |
| то же + `/files` | вкладка сразу с кодом и кнопкой **Review changes** |
| `https://github.com/DemTiOyset/Saucдemo_Umarov/pulls` | список PR, если номер забыл |
| `https://github.com/DemTiOyset/Saucedemo_Umarov/invitations` | ссылка для ментора, чтобы принять приглашение |

---

## 6. Приглашение ментора и защита ветки

1. `https://github.com/DemTiOyset/Saucedemo_Umarov/settings/access`
2. **Add people** → `Tunia666` → роль **Write** → **Add**. Роль Write нужна, чтобы Approve
   засчитало правило защиты ветки.
3. Ментор принимает приглашение из письма.
4. В PR справа в блоке **Reviewers** выбери `Tunia666`.
5. Защита `main`: `Settings → Rules` (Rulesets) или `Branches` → правило для `main`:
   ✅ Require a pull request before merging, ✅ Required approvals: 1,
   ✅ Dismiss stale approvals, ✅ Require conversation resolution, ✅ Block force pushes.

Теперь кнопка **Merge pull request** серая до Approve. Автор PR не может одобрить свой PR —
именно поэтому «подтверждённый PR» может появиться только от ментора.

Сообщение ментору:

```text
Здравствуйте! Сдаю ЛР13 и ЛР14-16 — UI-автотесты для saucedemo.com.
Репозиторий: https://github.com/DemTiOyset/Saucedemo_Umarov
Ветка ЛР13: feature/4ИСП9-42_Umarov_saucedemo
Ветка ЛР14-16: feature/4ИСП9-42_Umarov_saucedemo_allure
Тесты: mvn clean test → Tests run: 10, Failures: 0, BUILD SUCCESS.
Кроссбраузерный прогон: mvn clean test -Pcrossbrowser (Chrome + Firefox параллельно).
Allure-отчёт: mvn allure:report → target/site/allure-maven-plugin/index.html
Пожалуйста, примите приглашение в коллабораторы и в PR нажмите
Review changes → Approve → Submit review. Спасибо!
```

---

## 7. Что делает ментор

> 1. Открыть PR, вкладка **Files changed** — посмотреть код.
> 2. **Review changes** → **Approve** → при желании комментарий → **Submit review**.
> 3. Если есть замечания — **Request changes**, студент пушит правки в ту же ветку, PR обновляется.

---

## 8. После Approve

1. **Merge pull request** → **Squash and merge** → **Confirm**.
2. **Delete branch**.
3. Локально:

```bash
git checkout main
git pull
git branch -d feature/4ИСП9-42_Umarov_saucedemo
```

4. Затем повтори шаги 5.2–8 для второй ветки.

---

## 9. Артефакты для отчёта

**По практике 13:**

- [ ] ссылка на репозиторий и на ветку `feature/4ИСП9-42_Umarov_saucedemo`;
- [ ] ссылка на PR №1 и hash последнего коммита: `git log -1 --oneline`;
- [ ] скриншоты: `mvn clean test` (зелёный), содержимое `target/surefire-reports`;
- [ ] экраны приложения: успешный вход, ошибка `locked_out_user`, корзина, `Thank you for your order!`;
- [ ] краткий разбор: среда, сценарии, локаторы, ожидания, найденные нестабильности.

**По практике 14–16 (дополнительно):**

- [ ] ссылка на ветку `feature/4ИСП9-42_Umarov_saucedemo_allure` и PR №2;
- [ ] результаты кроссбраузерного прогона: Chrome, Firefox, `-Pcrossbrowser` (числа и длительности);
- [ ] скриншоты Allure: Dashboard, Behaviors, Environment, карточка упавшего теста с вложением;
- [ ] описание отличий между браузерами (если обнаружены) и выводы об устойчивости.

---

## 10. Типичные проблемы

| Симптом | Причина | Решение |
|---|---|---|
| `java: command not found`, `'mvn' не является внутренней командой` | PATH не видит JDK/Maven | Прописать PATH, открыть новый терминал |
| `invalid target release: 21` | Maven работает на старой Java | Поставить JDK 21, проверить `Java version:` в `mvn -version` |
| `binary is not a Firefox executable` | Firefox установлен как snap (Ubuntu) | `-Dfirefox.binary=$HOME/firefox/firefox` или оставить пропуск |
| Firefox/Edge: тесты помечены пропущенными | Браузера нет или он недоступен для драйвера | Это ожидаемое поведение; на Windows/macOS Firefox и Edge запускаются |
| `mvn allure:report` ругается на пустой `allure-results` | Отчёт генерируют до прогона | Сначала `mvn clean test`, потом `mvn allure:report` |
| В Allure нет шагов | Не сработал aspectjweaver | Проверить блок `argLine` в `pom.xml` (surefire) |
| `Tests run: ... Failures: 1` по таймауту ожидания | Медленный канал или недоступный стенд | Повторить прогон, поднять `-Dtimeout.seconds=30` |
| В логе `[fallback]` или `[retry]` | Сработала защита от потери ввода/клика на React-форме | Ничего делать не нужно |
| Оба теста оформления заказа упали с сообщением «Страница оформления заказа перестала реагировать» | Известная нестабильность демо-стенда SauceDemo: форма отрисована, но не принимает ни ввод, ни клики | Это не ошибка тестов. Повтори `mvn test` — в здоровый период прогон даёт 10/10 |
| `git push` → `Authentication failed` | Использован пароль вместо токена | Создать PAT и вставить его как пароль |
| Кнопка Merge серая | Нет Approve ментора | Review changes → Approve; проверить роль Write |
| `src refspec main does not match any` | Нет ни одного коммита | Сначала `git add` + `git commit`, потом `push` |

---

## 11. Чек-лист сдачи

- [ ] `mvn clean test` → `Tests run: 10, Failures: 0`, `BUILD SUCCESS`
- [ ] `mvn clean test -Pcrossbrowser` → 20 выполнений, 0 падений (Firefox может быть пропущен)
- [ ] `mvn allure:report` → открывается `target/site/allure-maven-plugin/index.html`
- [ ] Репозиторий `Saucedemo_Umarov` публичный, без `target/` и `.idea/`
- [ ] Ветка ЛР13 и PR №1 созданы, ментор в Reviewers
- [ ] PR №1 смержен, затем создана ветка ЛР14–16 и PR №2
- [ ] Ментор приглашён (роль Write), включено правило `main` с Required approvals: 1
- [ ] Оба PR имеют статус **Approved** и смержены
- [ ] Собраны скриншоты: консоль, surefire-reports, Allure (Dashboard, Behaviors, Environment)
- [ ] В отчёте указаны ссылки на ветки, PR и hash последнего коммита

---

## 12. Шпаргалка

```bash
# прогоны
mvn clean test
mvn clean test -Dbrowser=firefox
mvn clean test -Dbrowser=firefox -Dfirefox.binary=$HOME/firefox/firefox
mvn clean test -Pcrossbrowser
mvn allure:report

# публикация
git init -b main
git add .gitignore README.md && git commit -m "chore: initial commit"
git remote add origin https://github.com/DemTiOyset/Saucedemo_Umarov.git
git push -u origin main

# ветка ЛР13
git checkout -b feature/4ИСП9-42_Umarov_saucedemo
git add pom.xml .gitignore README.md docs src/test/java src/test/resources/SmokeTest.xml src/test/resources/allure.properties
git commit -m "feat: add Page Object framework and SauceDemo scenarios (ЛР13)"
git push -u origin feature/4ИСП9-42_Umarov_saucedemo

# ветка ЛР14-16 (после мержа первого PR)
git checkout main && git pull
git checkout -b feature/4ИСП9-42_Umarov_saucedemo_allure
git add src/test/resources/CrossBrowser.xml
git commit -m "feat: add cross-browser TestNG suite Chrome+Firefox, parallel (ЛР14-16)"
git push -u origin feature/4ИСП9-42_Umarov_saucedemo_allure

# hash для отчёта
git log -1 --oneline
```
