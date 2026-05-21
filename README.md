# AREcommerceApi

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-6DB33F?style=flat-square&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.25-7F52FF?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=java)](https://www.oracle.com/java/)
[![MongoDB](https://img.shields.io/badge/MongoDB-6.0-13AA52?style=flat-square&logo=mongodb)](https://www.mongodb.com)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker)](https://www.docker.com)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)

REST API бэкенда для мобильного AR e-commerce приложения. Разработан в рамках выпускной квалификационной работы (ВКР) с использованием современного стека технологий Spring Boot, Kotlin и MongoDB.

---

## Содержание

- [Обзор](#обзор)
- [Возможности](#возможности)
- [Требования](#требования)
- [Установка и запуск](#установка-и-запуск)
- [API справочник](#api-справочник)
- [Аутентификация и безопасность](#аутентификация-и-безопасность)
- [Конфигурация](#конфигурация)
- [Результаты нагрузочного тестирования](#результаты-нагрузочного-тестирования)
- [Структура проекта](#структура-проекта)
- [Технологический стек](#технологический-стек)
- [Инициализация данных](#инициализация-данных)

---

## Обзор

### Диаграмма развёртывания

![Диаграмма развёртывания](docs/diagrams/deployment.svg)

### C4 Container Diagram

![C4 Container](docs/diagrams/c4_container.svg)

**AREcommerceApi** — это high-performance REST API, специально разработанный для поддержки мобильного приложения с функциями дополненной реальности (AR). Бэкенд обеспечивает:

- Каталог товаров мебели с AR-моделями в платформенных форматах (GLB для Android, USDZ для iOS)
- Управление корзиной пользователя
- Загрузку 3D-моделей и изображений товаров в облачное хранилище (Yandex Object Storage)
- Аутентификацию администраторов через API-ключи
- Rate limiting для защиты от злоупотребления
- Валидацию подписей файлов при загрузке

Приложение полностью контейнеризировано и готово к развёртыванию в production-среде.

---

## Возможности

✅ **Публичный каталог товаров**
- Список товаров с базовой информацией (SKU, название, цена, изображение)
- Детальная карточка товара с AR-метаданными и спецификациями
- Платформенные URL для 3D-моделей (GLB для Android, USDZ для iOS)
- Физические размеры товаров в миллиметрах

✅ **Управление корзиной**
- Добавление товаров в корзину по пользователю
- Получение содержимого корзины
- Идемпотентные операции

✅ **Загрузка медиа-файлов (Admin)**
- Загрузка 3D-моделей (GLB, USDZ) с валидацией подписи файла
- Загрузка изображений товаров (PNG, JPEG, WebP)
- Интеграция с Yandex Object Storage (S3-compatible)
- Возврат URL-адресов файлов после загрузки

✅ **Безопасность**
- API-ключ аутентификация для админ-endpoints
- Rate limiting (10 req/min для админ, 60 req/min для публичного API)
- Валидация magic-bytes для загружаемых файлов

✅ **Производительность**
- Среднее время ответа: 3.23ms
- p95 latency: 9.11ms
- Пропускная способность: 185 req/s при 100 VU
- Нулевой процент ошибок при стандартной нагрузке

---

## Требования

### Минимальные требования

- **Java**: 17 или выше
- **Kotlin**: 1.9.25 (включена в Spring Boot)
- **MongoDB**: 6.0+
- **Docker** и **Docker Compose** (для запуска в контейнерах)
- **Gradle**: 8.0+ (если запуск без Docker)

### Учётные данные для облачного хранилища

Для полной работоспособности загрузки медиа-файлов требуются:
- Yandex Cloud Object Storage credentials (доступные через Yandex Cloud Console)
- Или AWS S3-compatible хранилище

---

## Установка и запуск

### Быстрый старт с Docker Compose

1. **Клонируйте репозиторий и перейдите в каталог:**
   ```bash
   cd AREcommerceApi
   ```

2. **Создайте файл .env из шаблона:**
   ```bash
   cp .env.example .env
   ```

3. **Отредактируйте .env и заполните значения:**
   ```env
   MONGO_USER=arecommerce_user
   MONGO_PASSWORD=your_secure_password_here
   ADMIN_API_KEY=your_secure_admin_key_here
   AWS_ACCESS_KEY_ID=your_yandex_access_key
   AWS_SECRET_ACCESS_KEY=your_yandex_secret_key
   ```

4. **Запустите приложение:**
   ```bash
   docker-compose up -d
   ```

5. **Проверьте статус:**
   ```bash
   docker-compose ps
   ```

Приложение будет доступно по адресу: **http://localhost:8080**

MongoDB будет доступна локально на **127.0.0.1:27017** (для отладки)

### Локальный запуск (без Docker)

Требуется локально установленная и запущенная MongoDB на порту 27017.

1. **Установите переменные окружения:**
   ```bash
   export MONGO_USER=arecommerce_user
   export MONGO_PASSWORD=your_password
   export ADMIN_API_KEY=your_api_key
   export AWS_ACCESS_KEY_ID=your_key
   export AWS_SECRET_ACCESS_KEY=your_secret
   ```

2. **Запустите приложение:**
   ```bash
   ./gradlew bootRun
   ```

3. **Приложение будет доступно по адресу:** http://localhost:8080

---

## API справочник

### Базовый URL
```
http://localhost:8080/api        — Публичный API
http://localhost:8080/admin      — Админ API
```

### Публичный API

#### 1. Создание нового пользователя
```http
GET /api/createUserId
```
**Ответ (200 OK):**
```
"550e8400-e29b-41d4-a716-446655440000"
```
Возвращает UUID пользователя для использования в корзине и отслеживания.

---

#### 2. Список товаров (PLP — Product List Page)
```http
GET /api/plp
```
**Ответ (200 OK):**
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
  },
  {
    "sku": 1002,
    "name": "Стул офисный",
    "description": "Эргономичный стул с регулируемой высотой",
    "price": "5999.99 ₽",
    "imageUrl": "https://storage.yandex.net/arecommerce/1002-image.jpg",
    "rate": 4.5
  }
]
```

**Параметры:** нет

---

#### 3. Детальная карточка товара (PDP — Product Detail Page)
```http
GET /api/pdp/{sku}?osType=ANDROID|IOS
```

**Параметры пути:**
| Параметр | Тип | Описание |
|----------|-----|---------|
| `sku` | Long | SKU товара |

**Параметры запроса:**
| Параметр | Тип | Описание |
|----------|-----|---------|
| `osType` | Enum | `ANDROID` или `IOS` — определяет формат 3D-модели (GLB для Android, USDZ для iOS) |

**Ответ (200 OK):**
```json
{
  "sku": 1001,
  "name": "Шезлонг складной",
  "description": "Удобный пляжный шезлонг с текстильным покрытием",
  "price": "2499.99 ₽",
  "images": [
    "https://storage.yandex.net/arecommerce/1001-image-1.jpg",
    "https://storage.yandex.net/arecommerce/1001-image-2.jpg"
  ],
  "oldPrice": "3299.99 ₽",
  "discount": 24,
  "rating": 4.8,
  "characteristics": {
    "material": "ткань",
    "color": "голубой",
    "length": "190 см"
  },
  "stock": 15,
  "deliveryInfo": "Доставка в течение 2-3 дней",
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

**AR Metadata:**
- **arRecourceUrl**: платформенный URL (GLB для Android, USDZ для iOS)
- **width, height, depth**: физические размеры в миллиметрах
- **arType**: тип расстановки (OBJECT, FLOOR, WALL)
- **placement**: допустимые поверхности размещения

---

#### 4. Добавление товара в корзину
```http
POST /api/basket
Content-Type: application/json

{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "sku": 1001,
  "quantity": 1
}
```

**Ответ (200 OK):**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "items": [
    {
      "sku": 1001,
      "name": "Шезлонг складной",
      "quantity": 1,
      "price": "2499.99 ₽"
    }
  ],
  "totalPrice": "2499.99 ₽",
  "totalQuantity": 1
}
```

---

#### 5. Получение содержимого корзины
```http
GET /api/basket?userId=550e8400-e29b-41d4-a716-446655440000
```

**Параметры запроса:**
| Параметр | Тип | Описание |
|----------|-----|---------|
| `userId` | String | UUID пользователя |

**Ответ (200 OK):**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "items": [
    {
      "sku": 1001,
      "name": "Шезлонг складной",
      "quantity": 1,
      "price": "2499.99 ₽"
    },
    {
      "sku": 1005,
      "name": "Кресло",
      "quantity": 2,
      "price": "4299.99 ₽"
    }
  ],
  "totalPrice": "10999.97 ₽",
  "totalQuantity": 3
}
```

---

### Admin API

#### Требование к аутентификации

Все admin-endpoints требуют заголовка:
```http
X-Admin-Api-Key: your_secure_admin_key_here
```

---

#### 1. Загрузка 3D-модели товара
```http
POST /admin/upload/model?sku=1001&format=GLB
Content-Type: multipart/form-data

file: @path/to/model.glb
```

**Параметры запроса:**
| Параметр | Тип | Описание |
|----------|-----|---------|
| `sku` | Long | SKU товара |
| `format` | Enum | `GLB` или `USDZ` |
| `file` | File | 3D-модель (GLB или USDZ) |

**Валидация:**
- GLB-файлы должны начинаться с magic bytes: `67 6C 54 46` (ASCII: `glTF`)
- USDZ-файлы должны быть zip-архивами с расширением `.usdz`

**Ответ (200 OK):**
```json
{
  "sku": 1001,
  "format": "GLB",
  "url": "https://storage.yandex.net/arecommerce/1001.glb"
}
```

**Ошибки:**
- `400 Bad Request` — отсутствует Content-Type или некорректный формат
- `409 Conflict` — модель для этого SKU и формата уже существует
- `422 Unprocessable Entity` — некорректная подпись файла

---

#### 2. Загрузка изображения товара
```http
POST /admin/upload/image?sku=1001
Content-Type: multipart/form-data

file: @path/to/image.jpg
```

**Параметры запроса:**
| Параметр | Тип | Описание |
|----------|-----|---------|
| `sku` | Long | SKU товара |
| `file` | File | Изображение (PNG, JPEG или WebP) |

**Поддерживаемые форматы:**
- PNG (magic bytes: `89 50 4E 47`)
- JPEG (magic bytes: `FF D8 FF`)
- WebP (magic bytes: `52 49 46 46 ... 57 45 42 50`)

**Ответ (200 OK):**
```json
{
  "sku": 1001,
  "url": "https://storage.yandex.net/arecommerce/1001-image.jpg"
}
```

**Ошибки:**
- `400 Bad Request` — отсутствует Content-Type
- `409 Conflict` — изображение для этого SKU уже существует
- `422 Unprocessable Entity` — некорректная подпись файла

---

## Аутентификация и безопасность

### API-ключ аутентификация

Все admin-endpoints защищены через API-ключ, передаваемый в заголовке `X-Admin-Api-Key`.

```bash
curl -X POST http://localhost:8080/admin/upload/model?sku=1001&format=GLB \
  -H "X-Admin-Api-Key: your_admin_key" \
  -F "file=@model.glb"
```

### Rate Limiting

Применяется token bucket алгоритм через Bucket4j:

| Endpoint | Лимит | Окно |
|----------|-------|------|
| Admin API | 10 req/min (по IP) | 1 минута |
| Public API | 60 req/min (по Device ID или IP) | 1 минута |

При превышении лимита возвращается: `429 Too Many Requests`

### Валидация файлов

Все загружаемые файлы проходят валидацию подписи (magic bytes):

- **GLB**: `67 6C 54 46` (ASCII: `glTF`)
- **USDZ**: ZIP-архив с расширением `.usdz`
- **PNG**: `89 50 4E 47`
- **JPEG**: `FF D8 FF`
- **WebP**: `52 49 46 46 ... 57 45 42 50`

Некорректные файлы отклоняются с ошибкой `422 Unprocessable Entity`.

---

## Конфигурация

### Переменные окружения

Все параметры конфигурации передаются через переменные окружения:

```env
# MongoDB
MONGO_USER=arecommerce_user
MONGO_PASSWORD=change_me_strong_password

# Admin Authentication
ADMIN_API_KEY=change_me_strong_admin_key

# Yandex Object Storage (S3-compatible)
AWS_ACCESS_KEY_ID=your_yandex_access_key
AWS_SECRET_ACCESS_KEY=your_yandex_secret_key

# Rate Limiting (опционально, значения по умолчанию)
RATE_LIMIT_ADMIN_RPM=10
RATE_LIMIT_PUBLIC_RPM=60
```

### Переменные Spring Boot

Стандартные переменные Spring Boot для конфигурации MongoDB:
```env
SPRING_DATA_MONGODB_URI=mongodb://user:pass@localhost:27017/arecommerce?authSource=admin
```

(Автоматически генерируется из `MONGO_USER` и `MONGO_PASSWORD` в docker-compose)

### Файл .env.example

Репозиторий содержит `.env.example` с полным списком переменных:
```bash
cat .env.example
```

---

## Результаты нагрузочного тестирования

### Инструмент тестирования
**k6** — современный инструмент для load testing с поддержкой JavaScript

### Тесты проводились по следующим сценариям

#### 1. Load Test (Линейный рост нагрузки)
**Параметры:**
- Рост от 0 до 100 виртуальных пользователей (VU) за 4 минуты
- SLA: p95 ≤ 500ms, error rate ≤ 1%

**Результаты:**
- **Общее количество запросов:** 44,600
- **Ошибки:** 0 (0%)
- **Среднее время отклика:** 3.23ms
- **p(95) latency:** 9.11ms
- **p(99) latency:** 14.3ms
- **Пропускная способность:** 185 req/sec

**Результаты по endpoints:**

| Endpoint | p95 (ms) | p99 (ms) | Avg (ms) |
|----------|----------|----------|----------|
| `GET /api/createUserId` | 7.7 | 11.2 | 2.1 |
| `GET /api/plp` | 5.4 | 8.9 | 2.8 |
| `GET /api/pdp/{sku}` | 6.7 | 10.5 | 3.1 |
| `POST /api/basket` | 12.1 | 18.3 | 4.2 |
| `GET /api/basket` | 10.0 | 14.8 | 3.8 |

---

#### 2. Stress Test (Экстремальная нагрузка)
**Параметры:**
- Линейный рост до 600 VU

**Результаты:**
- **Общее количество запросов:** 266,020
- **p(95) latency:** 724ms при 600 VU
- **Система стабильна до ~250 VU**

---

#### 3. Capacity Test (Устойчивая нагрузка)
**Параметры:**
- Продолжительный тест при постоянной нагрузке

**Результаты:**
- **Оптимальная устойчивая нагрузка:** 200–250 VU
- **p(95) latency при 250 VU:** 741ms
- **Рекомендуемая максимальная нагрузка:** 200 VU (p95 ≈ 100–200ms)

---

### Графики результатов

#### Latency по Endpoint

![Latency by Endpoint](load-tests/charts/endpoint-p95.svg)

#### Latency vs Concurrent Users

![Latency vs VUs](load-tests/charts/latency-vus.svg)

---

## Структура проекта

```
AREcommerceApi/
├── src/
│   ├── main/kotlin/com/poroshin/rut/ar/api/
│   │   ├── ArEcommerceApiApplication.kt         — Spring Boot entry point
│   │   ├── config/
│   │   │   ├── ApiKeyFilter.kt                  — Фильтр API-ключа
│   │   │   ├── RateLimitFilter.kt               — Rate limiting фильтр (Bucket4j)
│   │   │   ├── DataInitializer.kt               — Инициализация BD с товарами
│   │   │   ├── S3Config.kt                      — Конфигурация AWS SDK
│   │   │   ├── YandexS3Properties.kt            — Параметры Yandex S3
│   │   │   └── AppConfig.kt                     — Основная конфигурация
│   │   ├── controller/
│   │   │   ├── EcommerceController.kt           — Публичный API
│   │   │   └── AdminController.kt               — Admin API
│   │   ├── service/
│   │   │   ├── EcommerceService.kt              — Бизнес-логика каталога и корзины
│   │   │   ├── AdminUploadService.kt            — Бизнес-логика загрузки файлов
│   │   │   └── FileSignatureValidator.kt        — Валидация подписей файлов
│   │   ├── repository/
│   │   │   ├── ProductRepository.kt             — Spring Data MongoDB для товаров
│   │   │   ├── BasketRepository.kt              — Spring Data MongoDB для корзины
│   │   │   ├── UserRepository.kt                — Spring Data MongoDB для пользователей
│   │   │   └── ObjectStorageRepository.kt       — Интеграция с Yandex S3
│   │   ├── entity/
│   │   │   ├── ProductDocument.kt               — MongoDB документ товара
│   │   │   ├── BasketDocument.kt                — MongoDB документ корзины
│   │   │   └── UserDocument.kt                  — MongoDB документ пользователя
│   │   ├── model/
│   │   │   ├── ProductModels.kt                 — Domain модели (Product, ProductPageInfo, ArInfo)
│   │   │   └── ModelFormat.kt                   — Enum для форматов моделей
│   │   └── dto/
│   │       ├── BasketDtos.kt                    — DTO для корзины
│   │       └── AdminDtos.kt                     — DTO для админ API
│   └── test/
│       ├── AdminControllerTest.kt               — Unit-тесты контроллера
│       ├── AdminUploadServiceTest.kt            — Unit-тесты сервиса загрузки
│       ├── ObjectStorageRepositoryTest.kt       — Unit-тесты хранилища
│       └── ArEcommerceApiApplicationTests.kt    — Интеграционные тесты
├── load-tests/
│   ├── smoke-test.js                           — Smoke test сценарий
│   ├── load-test.js                            — Load test (100 VU)
│   ├── stress-test.js                          — Stress test (600 VU)
│   ├── capacity-test.js                        — Capacity test
│   └── charts/
│       ├── endpoint-p95.svg                     — График p95 по endpoints
│       └── latency-vus.svg                      — График latency vs VU
├── build.gradle.kts                            — Gradle build конфигурация
├── Dockerfile                                  — Docker-образ приложения
├── docker-compose.yml                          — Docker Compose для app + MongoDB
├── .env.example                                — Шаблон переменных окружения
└── README.md                                   — Этот файл
```

---

## Технологический стек

### Backend Framework
- **Spring Boot** 3.5.7 — современный фреймворк для быстрого развития REST API
- **Kotlin** 1.9.25 — статически типизированный язык на JVM с лучшей безопасностью типов

### Base
- **Java** 17 — LTS версия с модульной системой и улучшенной производительностью

### Database
- **MongoDB** 6.0 — NoSQL база данных для гибкой схемы документов
- **Spring Data MongoDB** — удобный ORM для работы с MongoDB

### File Storage
- **Yandex Object Storage** (S3-compatible) — облачное хранилище 3D-моделей и изображений
- **AWS SDK for Java v2** — клиент для работы с S3 API

### Security & Rate Limiting
- **Spring Security** (встроена в Spring Boot) — базовая защита endpoints
- **Bucket4j** 8.10.1 — библиотека для rate limiting через token bucket

### Serialization
- **Jackson** (включена в Spring Boot Web) — сериализация JSON

### Testing
- **JUnit 5** — фреймворк для unit-тестов
- **MockK** 1.13.13 — моки для Kotlin
- **Spring MockK** 4.0.2 — интеграция MockK с Spring
- **Embedded MongoDB** (Flapdoodle) 4.11.0 — встроенная MongoDB для тестов

### Build & Deployment
- **Gradle** 8.0+ — система сборки
- **Docker** & **Docker Compose** — контейнеризация и оркестрация

### Load Testing
- **k6** — инструмент для нагрузочного тестирования

---

## Инициализация данных

### Автоматическая инициализация

При первом запуске приложения класс `DataInitializer` автоматически заполняет базу данных 8 реалистичными товарами мебели:

1. **Шезлонг складной** (SKU: 1001)
2. **Стул офисный** (SKU: 1002)
3. **Стеллаж открытый** (SKU: 1003)
4. **Журнальный столик** (SKU: 1004)
5. **Кресло** (SKU: 1005)
6. **Кровать двуспальная** (SKU: 1006)
7. **Балконный набор** (SKU: 1007)
8. **Тумба ТВ** (SKU: 1008)

Каждый товар содержит:
- Базовую информацию (название, описание, цену)
- Скидку и рейтинг
- Характеристики (материал, размеры)
- AR-метаданные (платформенные URL, физические размеры)
- Информацию о доставке и наличии

### Идемпотентность инициализации

Инициализация выполняется **только один раз** при пустой базе данных:
- Если товары уже существуют, инициализация пропускается
- Это позволяет безопасно перезапускать приложение без дублирования данных

### Просмотр инициализованных товаров

После запуска приложения получить полный список товаров можно через API:

```bash
curl http://localhost:8080/api/plp
```

---

## Тестирование

### Unit-тесты

Запуск всех unit-тестов:
```bash
./gradlew test
```

Тесты покрывают:
- Валидацию подписей файлов (`FileSignatureValidator`)
- Загрузку файлов в хранилище (`AdminUploadService`)
- Контроллеры (`AdminController`)
- Интеграцию с S3

### Load Testing с k6

Установка k6:
```bash
# macOS (Homebrew)
brew install k6

# или скачать с https://k6.io/docs/getting-started/installation/
```

Запуск тестов:

**Smoke Test (проверка базовой функциональности):**
```bash
k6 run load-tests/smoke-test.js
```

**Load Test (100 VU за 4 минуты):**
```bash
k6 run load-tests/load-test.js
```

**Stress Test (до 600 VU):**
```bash
k6 run load-tests/stress-test.js
```

**Capacity Test (устойчивая нагрузка):**
```bash
k6 run load-tests/capacity-test.js
```

---

## Примеры использования

### Пример 1: Получить список товаров
```bash
curl -X GET http://localhost:8080/api/plp
```

### Пример 2: Создать нового пользователя
```bash
USER_ID=$(curl -s -X GET http://localhost:8080/api/createUserId)
echo $USER_ID
# Вывод: "550e8400-e29b-41d4-a716-446655440000"
```

### Пример 3: Получить карточку товара с AR для Android
```bash
curl -X GET "http://localhost:8080/api/pdp/1001?osType=ANDROID"
```

### Пример 4: Добавить товар в корзину
```bash
curl -X POST http://localhost:8080/api/basket \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "sku": 1001,
    "quantity": 2
  }'
```

### Пример 5: Получить корзину пользователя
```bash
curl -X GET "http://localhost:8080/api/basket?userId=550e8400-e29b-41d4-a716-446655440000"
```

### Пример 6: Загрузить 3D-модель (Admin)
```bash
curl -X POST "http://localhost:8080/admin/upload/model?sku=1001&format=GLB" \
  -H "X-Admin-Api-Key: your_admin_key" \
  -F "file=@model.glb"
```

### Пример 7: Загрузить изображение товара (Admin)
```bash
curl -X POST "http://localhost:8080/admin/upload/image?sku=1001" \
  -H "X-Admin-Api-Key: your_admin_key" \
  -F "file=@image.jpg"
```

---

## Интеграция с мобильным приложением

Этот бэкенд разработан для работы с мобильным приложением **E-Commerce-AR-app** (Kotlin Multiplatform).

### Особенности интеграции

- **Платформенные URL для 3D-моделей**: API возвращает разные URL в зависимости от `osType` (ANDROID/IOS)
- **GLB для Android**: оптимизирован для быстрой загрузки и рендеринга на мобильных устройствах
- **USDZ для iOS**: native формат для ARKit
- **AR Metadata**: физические размеры товара в миллиметрах для корректного отображения в AR

Мобильное приложение использует эти данные для:
1. Отображения каталога товаров
2. Предпросмотра товаров в AR
3. Управления корзиной
4. Оформления заказа

---

## Лицензия

MIT License — см. [LICENSE](LICENSE)

---

## Справка для ВКР

**Программный проект:** AREcommerceApi  
**Тип:** REST API для AR e-commerce приложения  
**Язык:** Kotlin + Java 17  
**Фреймворк:** Spring Boot 3.5.7  
**База данных:** MongoDB 6.0  
**Развёртывание:** Docker Compose  
**Нагрузочное тестирование:** k6  
**Платформы:** Android, iOS (через KMP приложение)

Бэкенд полностью готов к сдаче в production и соответствует best practices современной разработки REST API.

---

**Автор:** Поршин Глеб  
**Университет:** РТУ МИРЭА  
**Год:** 2025
