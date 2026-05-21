# AREcommerceApi

Бэкенд для мобильного AR-приложения электронной коммерции. Каталог мебели с 3D-моделями, корзина пользователя, защищённая админ-загрузка моделей и изображений в Yandex Object Storage. Сделано в рамках дипломной работы.

Клиентское приложение — в отдельном репозитории [`E-Commerce-AR-app`](https://github.com/GlebPoroshin/E-Commerce-AR-app).

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-6DB33F?style=flat-square&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.25-7F52FF?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=java)](https://www.oracle.com/java/)
[![MongoDB](https://img.shields.io/badge/MongoDB-6.0-13AA52?style=flat-square&logo=mongodb)](https://www.mongodb.com)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker)](https://www.docker.com)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)

## Содержание

- [Архитектура](#архитектура)
- [Запуск](#запуск)
- [Публичное API](#публичное-api)
- [Админ API](#админ-api)
- [Безопасность](#безопасность)
- [Конфигурация](#конфигурация)
- [Нагрузочное тестирование](#нагрузочное-тестирование)
- [Структура проекта](#структура-проекта)
- [Стек](#стек)
- [Тесты](#тесты)
- [Инициализация данных](#инициализация-данных)
- [Лицензия](#лицензия)

## Архитектура

![Диаграмма развёртывания](docs/diagrams/deployment.svg)

![C4 Container](docs/diagrams/c4_container.svg)

Spring Boot 3 на Kotlin, MongoDB, Yandex Object Storage. Доставка через Docker Compose. Для локального подключения мобильного клиента наружу прокинут Tailscale Funnel.

## Запуск

### Docker Compose

```bash
cp .env.example .env       # заполнить значения
docker-compose up -d
```

API поднимается на `http://localhost:8080`. MongoDB слушает `127.0.0.1:27017` для отладки.

Минимальный набор переменных в `.env`:

```env
MONGO_USER=arecommerce_user
MONGO_PASSWORD=...
ADMIN_API_KEY=...
AWS_ACCESS_KEY_ID=...
AWS_SECRET_ACCESS_KEY=...
```

### Локально без Docker

Нужна локальная MongoDB на 27017. Дальше:

```bash
export MONGO_USER=... MONGO_PASSWORD=... ADMIN_API_KEY=... \
       AWS_ACCESS_KEY_ID=... AWS_SECRET_ACCESS_KEY=...
./gradlew bootRun
```

## Публичное API

База: `http://localhost:8080/api`

### `GET /api/createUserId`

Создаёт анонимную сессию: возвращает UUID, который клиент сохраняет локально и передаёт в последующих запросах корзины. Полноценной авторизации в проекте нет, идентификация — по device ID.

```
"550e8400-e29b-41d4-a716-446655440000"
```

### `GET /api/plp`

Список товаров для PLP:

```json
[
  {
    "sku": 1001,
    "name": "Шезлонг складной",
    "description": "Удобный пляжный шезлонг с текстильным покрытием",
    "price": "2499.99 ₽",
    "imageUrl": "https://storage.yandex.net/arecommerce/1001-image.jpg",
    "oldPrice": "3299.99 ₽",
    "discount": 24,
    "rate": 4.8
  }
]
```

### `GET /api/pdp/{sku}?osType=ANDROID|IOS`

Карточка товара. `osType` определяет формат 3D-модели в ответе: GLB для Android, USDZ для iOS.

```json
{
  "sku": 1001,
  "name": "Шезлонг складной",
  "price": "2499.99 ₽",
  "images": ["https://.../1001-image-1.jpg"],
  "characteristics": { "material": "ткань", "color": "голубой" },
  "stock": 15,
  "ar": {
    "version": 1,
    "arType": "OBJECT",
    "placement": "ANY_HORIZONTAL",
    "arRecourceUrl": "https://storage.yandex.net/arecommerce/1001.glb",
    "width": 600,
    "height": 350,
    "depth": 1900
  }
}
```

Блок `ar`: `arRecourceUrl` — платформенный URL, `width / height / depth` — физические размеры в миллиметрах, `arType` ∈ {`OBJECT`, `FLOOR`, `WALL`}, `placement` — допустимая поверхность.

### `POST /api/basket`

```json
{ "userId": "550e...", "sku": 1001, "quantity": 1 }
```

Идемпотентно: повторный вызов с той же парой `userId+sku` обновляет количество, а не добавляет вторую строку.

### `GET /api/basket?userId={uuid}`

Содержимое корзины пользователя с агрегатами `totalPrice` и `totalQuantity`.

## Админ API

Все эндпоинты под `/admin` требуют заголовок `X-Admin-Api-Key`.

### `POST /admin/upload/model?sku=...&format=GLB|USDZ`

Multipart-загрузка 3D-модели. Файл проверяется по magic bytes:

- GLB — `67 6C 54 46` (ASCII `glTF`)
- USDZ — ZIP с расширением `.usdz`

Ответ:

```json
{ "sku": 1001, "format": "GLB", "url": "https://.../1001.glb" }
```

Ошибки: `409 Conflict`, если модель для пары `sku+format` уже есть; `422 Unprocessable Entity`, если подпись файла не сходится с заявленным форматом.

### `POST /admin/upload/image?sku=...`

Multipart-загрузка картинки. Поддержка PNG (`89 50 4E 47`), JPEG (`FF D8 FF`) и WebP (`52 49 46 46 … 57 45 42 50`).

```json
{ "sku": 1001, "url": "https://.../1001-image.jpg" }
```

Те же правила на `409` и `422`.

Пример вызова:

```bash
curl -X POST "http://localhost:8080/admin/upload/model?sku=1001&format=GLB" \
  -H "X-Admin-Api-Key: $ADMIN_API_KEY" \
  -F "file=@model.glb"
```

## Безопасность

`ApiKeyFilter` пропускает запросы на `/admin/*` только при совпадении заголовка `X-Admin-Api-Key` с переменной `ADMIN_API_KEY`. Несовпадение — `401`.

`RateLimitFilter` на Bucket4j применяет token-bucket:

| Контур | Лимит | Ключ |
|---|---|---|
| Админ | 10 запросов/мин | IP |
| Публичный | 60 запросов/мин | Device ID, иначе IP |

Превышение — `429 Too Many Requests`.

`FileSignatureValidator` читает первые байты загружаемого файла и сверяет с известными magic bytes. Расширение имени файла не доверяется. Несовпадение — `422`.

## Конфигурация

Все параметры приходят из переменных окружения, `application.yml` подхватывает через `${VAR:default}`:

```env
# MongoDB
MONGO_USER=...
MONGO_PASSWORD=...
SPRING_DATA_MONGODB_URI=mongodb://user:pass@localhost:27017/arecommerce?authSource=admin

# Админ
ADMIN_API_KEY=...

# Yandex Object Storage, S3-совместимое
AWS_ACCESS_KEY_ID=...
AWS_SECRET_ACCESS_KEY=...

# Rate limit, по умолчанию 10 и 60
RATE_LIMIT_ADMIN_RPM=10
RATE_LIMIT_PUBLIC_RPM=60
```

Полный шаблон лежит в `.env.example`.

## Нагрузочное тестирование

Инструмент — k6. Сценарии в `load-tests/`: `smoke-test.js`, `load-test.js`, `stress-test.js`, `capacity-test.js`.

### Load test, ramp 0 → 100 VU за 4 минуты

VU (virtual users) — параллельные виртуальные пользователи k6. SLA: `p95 ≤ 500ms`, `error_rate ≤ 1%`.

- Запросов: 44 600
- Ошибок: 0
- Средняя задержка: 3.23 мс
- p95: 9.11 мс
- p99: 14.3 мс
- Пропускная: 185 RPS

Распределение по эндпоинтам (миллисекунды):

| Эндпоинт | avg | p95 | p99 |
|---|---|---|---|
| `GET /api/createUserId` | 2.1 | 7.7 | 11.2 |
| `GET /api/plp` | 2.8 | 5.4 | 8.9 |
| `GET /api/pdp/{sku}` | 3.1 | 6.7 | 10.5 |
| `POST /api/basket` | 4.2 | 12.1 | 18.3 |
| `GET /api/basket` | 3.8 | 10.0 | 14.8 |

### Stress test, ramp до 600 VU

266 020 запросов. Система держится до ~250 VU. На 600 VU p95 ≈ 724 мс — точка отказа по SLA.

### Capacity

Устойчивый рабочий диапазон — 200–250 VU. На 250 VU p95 ≈ 741 мс. Рекомендованный предел — 200 VU, p95 ≈ 100–200 мс.

### Графики

p95 по эндпоинтам:

![Latency by Endpoint](load-tests/charts/endpoint-p95.svg)

Задержка от количества VU:

![Latency vs VUs](load-tests/charts/latency-vus.svg)

## Структура проекта

```
src/main/kotlin/com/poroshin/rut/ar/api/
├── ArEcommerceApiApplication.kt
├── config/
│   ├── ApiKeyFilter.kt              проверка X-Admin-Api-Key
│   ├── RateLimitFilter.kt           Bucket4j
│   ├── DataInitializer.kt           заполнение БД при первом запуске
│   ├── S3Config.kt
│   ├── YandexS3Properties.kt
│   └── AppConfig.kt
├── controller/
│   ├── EcommerceController.kt       публичное API
│   └── AdminController.kt
├── service/
│   ├── EcommerceService.kt
│   ├── AdminUploadService.kt
│   └── FileSignatureValidator.kt    magic-bytes валидация
├── repository/                       Spring Data MongoDB и S3-клиент
├── entity/                           MongoDB-документы
├── model/                            доменные модели и enum'ы
└── dto/

load-tests/
├── smoke-test.js | load-test.js | stress-test.js | capacity-test.js
├── common.js
└── charts/
    ├── endpoint-p95.svg
    └── latency-vus.svg
```

## Стек

- Spring Boot 3.5.7 на Kotlin 1.9.25, Java 17
- MongoDB 6.0 через Spring Data, embedded Flapdoodle 4.11 в тестах
- AWS SDK for Java v2 для Yandex Object Storage
- Bucket4j 8.10.1 для rate limit
- JUnit 5, MockK 1.13, Spring MockK 4.0 для тестов
- k6 для нагрузки
- Gradle 8, Docker Compose

## Тесты

Unit:

```bash
./gradlew test
```

Покрытие: `FileSignatureValidator`, `AdminUploadService`, `AdminController`, интеграция с S3-клиентом.

Нагрузка через k6:

```bash
brew install k6
k6 run load-tests/smoke-test.js     # дымовой
k6 run load-tests/load-test.js      # основной 100 VU
k6 run load-tests/stress-test.js    # 600 VU
k6 run load-tests/capacity-test.js  # устойчивая нагрузка
```

## Инициализация данных

`DataInitializer` при пустой коллекции создаёт 8 товаров мебели с реальными AR-размерами в миллиметрах: SKU 1001–1008 (шезлонг, офисный стул, открытый стеллаж, журнальный столик, кресло, двуспальная кровать, балконный набор, ТВ-тумба). Если документы уже есть, инициализация пропускается.

## Лицензия

MIT, см. [LICENSE](LICENSE).
