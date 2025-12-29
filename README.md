# service-monitor

Учебное Java-приложение (Spring Boot) для мониторинга доступности сервисов.

Сервис **раз в минуту** опрашивает заданные *health endpoints* и сохраняет результаты проверок в PostgreSQL.  
Далее предоставляет REST API (JSON) для получения:

- **периодов недоступности** за выбранный интервал времени
- **процента доступности (availability %)** за выбранный интервал времени
- **сводки** (availability + outages)
- **причин недоступности** (timeout/DNS/SSL/HTTP 4xx/5xx/invalid JSON и т.д.)

---

## Требования, которые закрывает проект

- ✅ Авторизация (пользователи в БД, Spring Security, Basic Auth)
- ✅ Admin API: добавление сервисов на мониторинг
- ✅ Cron-сбор метрик доступности (по расписанию, по умолчанию каждую минуту)
- ✅ API (JSON): периоды недоступности и процент доступности за интервал
- ✅ Обработка разных причин недоступности и отображение в статистике
- ✅ PostgreSQL + Flyway (миграции)
- ✅ Swagger UI (OpenAPI)

---

## Формат health endpoint

Ожидается JSON в стиле Spring Boot Actuator (берётся верхнеуровневое поле `status`):

```json
{"status":"UP"}
```

или

```json
{"status":"DOWN"}
```

Если запрос не удался (таймаут/ошибка DNS/SSL/HTTP 5xx/невалидный JSON/нет поля `status`) — проверка считается **DOWN** и причина фиксируется в БД.

---

## Стек

- Java 17 (проект компилируется как `release 17`)
- Spring Boot 3.3.x
- Maven
- PostgreSQL
- Flyway migrations
- Swagger UI (OpenAPI)

---

## Что должно быть установлено

1) **JDK 17+**
```bash
java -version
```

2) **Maven 3.9+**
```bash
mvn -v
```

3) **PostgreSQL 12+**
```bash
psql --version
```

---

## Запуск (без Docker)

### 1) Создать базу данных

Создай БД (пример через psql):

```powershell
psql -U postgres -c "CREATE DATABASE service_monitor;"
```

> Если `psql` не находится, запускай его из `C:\Program Files\PostgreSQL\<version>\bin\psql.exe`.

### 2) Настроить подключение к БД

Открой файл:

`src/main/resources/application.yml`

Проверь/измени:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/service_monitor
    username: postgres
    password: postgres
```

⚠️ Пароль **должен совпадать** с реальным паролем пользователя PostgreSQL.

### 3) Запустить приложение

Запускать нужно из **корня проекта** (где лежит `pom.xml`):

```powershell
mvn clean spring-boot:run
```

После успешного запуска увидишь строку вида:
`Started ServiceMonitorApplication ...`

Приложение будет работать, пока открыт этот терминал.

---

## Важное про PostgreSQL 16+ (Flyway)

Если у тебя PostgreSQL **16+** и при запуске появляется ошибка:

`Unsupported Database: PostgreSQL ...`

Добавь в `pom.xml` зависимость рядом с `flyway-core`:

```xml
<dependency>
  <groupId>org.flywaydb</groupId>
  <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

После этого перезапусти:

```powershell
mvn -U clean spring-boot:run
```

---

## Swagger UI

После запуска:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

---

## Авторизация (Basic Auth)

Используется **HTTP Basic Auth**.

Тестовые пользователи создаются миграцией Flyway (`V1__init.sql`):

- **ADMIN**  
  login: `admin`  
  password: `admin123`

- **USER**  
  login: `user`  
  password: `user123`

---

## API

### Admin API (только ADMIN)

Базовый путь: `/api/admin/apps`

- `GET  /api/admin/apps` — список приложений
- `GET  /api/admin/apps/{id}` — получить приложение по id
- `POST /api/admin/apps` — добавить приложение
- `PUT  /api/admin/apps/{id}` — обновить приложение
- `PATCH /api/admin/apps/{id}/active` — включить/выключить мониторинг

**Пример добавления приложения (PowerShell)**  
⚠️ В PowerShell лучше использовать `curl.exe` (а не алиас `curl`):

```powershell
curl.exe -u admin:admin123 -X POST "http://localhost:8080/api/admin/apps" ^
  -H "Content-Type: application/json" ^
  -d "{"name":"demo","healthUrl":"http://localhost:8081/actuator/health","active":true}"
```

### Metrics API (USER или ADMIN)

- `GET /api/metrics/apps/{id}/availability?from=...&to=...`
- `GET /api/metrics/apps/{id}/outages?from=...&to=...`
- `GET /api/metrics/apps/{id}/summary?from=...&to=...`

Параметры:
- `from` и `to` — время в ISO-8601 формате, например: `2025-01-01T00:00:00Z`

**Пример (availability):**
```powershell
curl.exe -u user:user123 "http://localhost:8080/api/metrics/apps/1/availability?from=2025-01-01T00:00:00Z&to=2025-12-31T23:59:59Z"
```

---

## Как работает сбор метрик (cron)

Сбор выполняется по cron-расписанию из `application.yml`:

```yaml
app:
  monitoring:
    cron: "0 * * * * *"
```

Формат cron — **Spring cron с секундами** (6 частей).  
По умолчанию: **каждую минуту в 0 секунд**.

Можно временно ускорить для демонстрации, например раз в 10 секунд:

```yaml
app:
  monitoring:
    cron: "*/10 * * * * *"
```

---

## База данных (кратко)

Таблицы создаются Flyway-миграцией:

- `users` — пользователи (логин, хэш пароля, роль)
- `monitored_app` — мониторимые приложения (name, health_url, active)
- `health_check` — результаты проверок (время, status, error_type, http_status, latency и т.д.)

Миграции:
`src/main/resources/db/migration/`

---

## Частые проблемы

### `mvn` не распознан
Установи Maven и добавь `...\bin` в `PATH`, затем открой новый терминал.

### Ошибка подключения к PostgreSQL (28P01 / password authentication failed)
Проверь `username/password` в `application.yml` и что PostgreSQL запущен.

### Порт 8080 занят
Поменяй порт в `application.yml`:

```yaml
server:
  port: 8081
```
