<p align="center">
  <img src="docs/celesma_architecture.png" alt="Celesma Architecture" width="100%"/>
</p>

# Celesma

Платформа для управления проектами и задачами с **realtime-обновлениями**, **AI-ассистентом**, **ролевой моделью доступа**, **историей изменений задач** и **S3-хранилищем вложений**. Аналог упрощённого Jira/Trello с фокусом на совместную работу команд небольших и средних проектов.

> README фронтенда: [celesma-frontend](https://github.com/dispixxx/celesma-frontend)

---

## Содержание

- [Технологии](#технологии)
- [Архитектура](#архитектура)
- [Структура проекта](#структура-проекта)
- [Быстрый старт](#быстрый-старт)
- [Конфигурация](#конфигурация)
- [REST API](#rest-api)
- [WebSocket-события](#websocket-события)
- [Kafka-события](#kafka-события)
- [Модель данных](#модель-данных)
- [Безопасность](#безопасность)
- [Roadmap](#roadmap)

---

### Backend
| Категория | Технология |
|-----------|------------|
| Язык | Java 21 (records, switch expressions, pattern matching) |
| Фреймворк | Spring Boot 3.5.4 |
| Web | Spring MVC, Spring WebSocket (STOMP + SockJS) |
| Безопасность | Spring Security, JWT (jjwt 0.12), OAuth2 Client |
| Данные | Spring Data JPA, Hibernate, PostgreSQL 15 |
| Маппинг | MapStruct 1.6 |
| Бойлерплейт | Lombok |
| Интеграции | Spring Kafka, AWS SDK v2 (S3), Spring OAuth2 |
| API-документация | springdoc-openapi (Swagger UI) |
| Валидация | Jakarta Bean Validation |

### Frontend
| Категория | Технология |
|-----------|------------|
| UI | React 19, TypeScript |
| Сборка | Vite 8 |
| Состояние | Zustand |
| Роутинг | React Router 7 |
| Realtime | @stomp/stompjs, sockjs-client |
| HTTP | Axios |
| Анимации | Framer Motion |

### Инфраструктура
- Docker (multi-stage build)
- Docker Compose для локального старта (app + PostgreSQL)
- Профили Spring: `dev`, `prod`, `kafka`, `s3`

---

## Структура проекта

```
src/main/java/com/disp/celesma/
├── CelesmaApplication.java          # Точка входа
│
├── controller/                      # REST-контроллеры
│   ├── ws/                          # WebSocket (STOMP) контроллеры
│   ├── AuthController.java
│   ├── ProjectController.java
│   ├── TaskController.java
│   ├── CommentController.java
│   ├── ProjectMemberController.java
│   ├── TaskAttachmentController.java
│   ├── ProjectAttachmentController.java
│   ├── TaskHistoryController.java
│   ├── ProjectApplicantController.java
│   ├── RoadmapApiController.java
│   └── UserController.java
│
├── service/                         # Бизнес-логика
│   ├── interfaces/                  # Контракты сервисов (для DI)
│   ├── TaskService.java
│   ├── ProjectService.java
│   ├── ProjectMemberService.java
│   ├── TaskHistoryService.java
│   ├── CommentService.java
│   ├── AuthService.java
│   ├── UserService.java
│   ├── RoadmapService.java
│   └── AiService.java               # Интеграция с DeepSeek
│
├── repository/                      # Spring Data JPA репозитории
├── model/                           # JPA-сущности
│   ├── enums/                       # TaskStatus, TaskPriority, ProjectRole
│   ├── Project.java
│   ├── Task.java
│   ├── User.java
│   ├── ProjectMember.java
│   ├── Comment.java
│   ├── TaskHistory.java
│   ├── TaskAttachment.java
│   ├── ProjectAttachment.java
│   ├── RoadmapBranch.java
│   └── TaskRoadmapEntry.java
│
├── dto/                             # Data Transfer Objects (records)
│   ├── auth/
│   ├── project/
│   ├── task/
│   ├── member/
│   ├── user/
│   ├── applicant/
│   └── common/                      # AiDescriptionRequest, AiTitleRequest
│
├── mapper/                          # MapStruct-мапперы entity ↔ DTO
│
├── event/                           # Spring Application Events
│   ├── task/                        # TaskCreatedEvent, TaskStatusChangedEvent
│   └── member/                      # MemberExitedProjectEvent
│
├── kafka/                           # Интеграция с Apache Kafka (профиль `kafka`)
│   ├── config/                      # KafkaTopicConfig
│   ├── producer/                    # TaskEventProducer
│   ├── listener/                    # TaskEventKafkaListener
│   └── dto/                         # TaskEventDto
│
├── s3/                              # S3 (Yandex Object Storage)
│   ├── config/                      # S3Config
│   ├── service/                     # S3StorageService
│   └── service/interfaces/
│
├── security/                        # Авторизация и аутентификация
│   ├── JwtAuthFilter.java           # Фильтр JWT в HTTP-цепочке
│   ├── JwtChannelInterceptor.java   # JWT-проверка в STOMP CONNECT
│   ├── JwtService.java              # Генерация/валидация токенов
│   ├── ProjectSecurityService.java  # Бин для @PreAuthorize SpEL
│   ├── UserPrincipal.java           # Principal обёртка над User
│   └── UserDetailsServiceImpl.java
│
├── config/                          # Spring-конфигурации
│   ├── SecurityConfig.java
│   ├── WebSocketConfig.java
│   ├── OpenApiConfig.java
│   └── RestTemplateConfig.java
│
└── exception/                       # Глобальная обработка ошибок
    └── GlobalExceptionHandler.java
```

---

## Быстрый старт

### Предварительные требования

- **JDK 21+**
- **Maven 3.9+**
- **Docker** и **Docker Compose** (опционально, но рекомендуется)
- **PostgreSQL 15+** (если запускаешь без Docker)
- **Yandex Cloud аккаунт** с Object Storage (для S3-фич) — можно запускать и без S3, фичи будут недоступны
- **DeepSeek API key** — для AI-функций (можно заглушить)
- **Google OAuth2 credentials** — для входа через Google (можно заглушить)

### Вариант 1: Docker Compose (рекомендуется)

```bash
git clone https://github.com/dispixxx/celesma-api.git
cd celesma-api

# Создай .env файл (см. раздел "Конфигурация")
cp .env.example .env
# отредактируй .env: впиши JWT_SECRET, DEEPSEEK_API_KEY, GOOGLE_CLIENT_ID/SECRET

docker-compose up --build
```

После старта:
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

### Вариант 2: Локальный запуск (разработка)

1. Подними PostgreSQL:
   ```bash
   docker run -d --name celesma-db \
     -e POSTGRES_DB=celesma_db \
     -e POSTGRES_USER=postgres \
     -e POSTGRES_PASSWORD=postgres \
     -p 5432:5432 \
     postgres:15-alpine
   ```

2. Задай переменные окружения:
   ```bash
   export JWT_SECRET="dev-secret-key-at-least-32-characters-long-enough"
   export DEEPSEEK_API_KEY="sk-..."
   export GOOGLE_CLIENT_ID="..."
   export GOOGLE_CLIENT_SECRET="..."
   export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/celesma_db"
   export SPRING_DATASOURCE_USERNAME="postgres"
   export SPRING_DATASOURCE_PASSWORD="postgres"
   ```

3. Запусти приложение:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

### Вариант 3: С Kafka (профиль `kafka`)

```bash
# Подними Kafka (например, через Bitnami-образ)
docker run -d --name kafka -p 9092:9092 \
  -e KAFKA_ENABLE_KRAFT=yes \
  -e KAFKA_CFG_NODE_ID=1 \
  -e KAFKA_CFG_PROCESS_ROLES=controller,broker \
  -e KAFKA_CFG_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093 \
  -e KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER \
  -e KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=1@localhost:9093 \
  -e KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT \
  bitnami/kafka:3.7

# Запусти приложение с профилем kafka
mvn spring-boot:run -Dspring-boot.run.profiles=dev,kafka
```

### Frontend

```bash
git clone https://github.com/dispixxx/celesma-frontend.git
cd celesma-frontend
npm install
npm run dev
```

Frontend доступен на http://localhost:3000, проксирует запросы на backend:8080.

---

## Конфигурация

Все секреты передаются через переменные окружения. Базовые настройки в `application.properties`, профиль-специфичные в `application-{profile}.properties`.

| Переменная | Описание | Обязательная | Пример |
|------------|----------|--------------|--------|
| `SPRING_DATASOURCE_URL` | URL подключения к PostgreSQL | Да | `jdbc:postgresql://localhost:5432/celesma_db` |
| `SPRING_DATASOURCE_USERNAME` | Пользователь БД | Да | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Пароль БД | Да | `postgres` |
| `JWT_SECRET` | Секрет для подписи JWT (минимум 32 символа) | Да | `dev-secret-key-at-least-32-characters-long-enough` |
| `DEEPSEEK_API_KEY` | API-ключ DeepSeek для AI-функций | Да (для AI) | `sk-...` |
| `GOOGLE_CLIENT_ID` | OAuth2 client ID Google | Да (для Google login) | `xxxxx.apps.googleusercontent.com` |
| `GOOGLE_CLIENT_SECRET` | OAuth2 client secret Google | Да (для Google login) | `GOCSPX-...` |
| `STORAGE_BUCKET_NAME` | Имя бакета Yandex S3 | Да (для S3) | `celesma-bucket` |
| `STORAGE_ENDPOINT` | Endpoint S3 | Да (для S3) | `https://storage.yandexcloud.net` |
| `AWS_ACCESS_KEY_ID` | AWS-совместимый ключ доступа к S3 | Да (для S3) | `YCAJE...` |
| `AWS_SECRET_ACCESS_KEY` | Секретный ключ S3 | Да (для S3) | `YCN...` |
| `SPRING_PROFILES_ACTIVE` | Активные профили | Нет | `dev,kafka,s3` |

### `.env.example`

```env
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/celesma_db
SPRING_DATASOURCE_USERNAME=celesma_user
SPRING_DATASOURCE_PASSWORD=celesma_password

# JWT
JWT_SECRET=change-me-to-a-long-random-string-at-least-32-chars

# OAuth2 Google
GOOGLE_CLIENT_ID=your-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=GOCSPX-your-secret

# DeepSeek AI
DEEPSEEK_API_KEY=sk-your-deepseek-key

# Yandex Object Storage
STORAGE_BUCKET_NAME=celesma
STORAGE_ENDPOINT=https://storage.yandexcloud.net
AWS_ACCESS_KEY_ID=YCAJE...
AWS_SECRET_ACCESS_KEY=YCN...

# Active Spring profiles
SPRING_PROFILES_ACTIVE=prod,s3
```

---

## REST API

Базовый префикс: `/api/v1`

Полная интерактивная документация — в Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html).

### Аутентификация

| Метод | Путь | Описание | Auth |
|-------|------|----------|------|
| `POST` | `/auth/register` | Регистрация по email + пароль | — |
| `POST` | `/auth/login` | Вход, возвращает JWT | — |
| `GET` | `/auth/oauth2/google` | Редирект на Google OAuth2 | — |

### Проекты

| Метод | Путь | Описание | Auth |
|-------|------|----------|------|
| `GET` | `/projects` | Список проектов текущего пользователя | JWT |
| `POST` | `/projects` | Создать проект (становишься OWNER) | JWT |
| `GET` | `/projects/{id}` | Детали проекта | JWT + member |
| `PATCH` | `/projects/{id}` | Обновить проект | JWT + privileged |
| `DELETE` | `/projects/{id}` | Удалить проект | JWT + owner |
| `GET` | `/projects/{id}/members` | Список участников | JWT + member |
| `POST` | `/projects/{id}/applicants` | Подать заявку на вступление | JWT |
| `POST` | `/projects/{id}/applicants/{userId}/accept` | Принять заявку | JWT + privileged |

### Задачи

| Метод | Путь | Описание | Auth |
|-------|------|----------|------|
| `GET` | `/projects/{projectId}/tasks` | Список задач проекта | JWT + member |
| `POST` | `/projects/{projectId}/tasks` | Создать задачу | JWT + member |
| `GET` | `/tasks/{taskId}` | Получить задачу | JWT + member |
| `PUT` | `/tasks/{taskId}` | Обновить задачу | JWT + creator/assignee/privileged |
| `PATCH` | `/tasks/{taskId}/status` | Сменить статус | JWT + member |
| `PATCH` | `/tasks/{taskId}/title` | Обновить только заголовок | JWT + member |
| `DELETE` | `/tasks/{taskId}` | Удалить задачу | JWT + privileged |
| `GET` | `/tasks/{taskId}/history` | История изменений | JWT + member |
| `POST` | `/tasks/{taskId}/attachments` | Загрузить вложение | JWT + member |
| `DELETE` | `/tasks/{taskId}/attachments/{attachmentId}` | Удалить вложение | JWT + uploader/privileged |

### AI

| Метод | Путь | Описание |
|-------|------|----------|
| `POST` | `/tasks/generate-title` | Сгенерировать заголовок по описанию |
| `POST` | `/tasks/ai-process` | Действия: `TITLE`, `IMPROVE`, `FORMALIZE`, `SUBTASKS` |

### Пример запроса на создание задачи

```http
POST /api/v1/projects/42/tasks HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOi...
Content-Type: application/json

{
  "title": "Реализовать экспорт задач в CSV",
  "description": "Нужна кнопка экспорта текущих задач проекта в CSV-файл...",
  "assigneeId": 7,
  "priority": "HIGH",
  "endDate": "2025-08-15"
}
```

---

## WebSocket-события

Подключение: `ws://localhost:8080/ws` (SockJS). В STOMP-фрейме `CONNECT` нужно передать заголовок `Authorization: Bearer <jwt>`. `JwtChannelInterceptor` проверяет токен.

| Канал (subscribe) | Тип события |
|-------------------|-------------|
| `/topic/project/{projectId}/tasks` | Создание/обновление/смена статуса задачи в проекте |
| `/topic/task/{taskId}/comments` | Новый или обновлённый комментарий в задаче |

| Endpoint (send) | Назначение |
|-----------------|------------|
| `/app/task/{taskId}/comment` | Отправить комментарий в задачу |

Пример STOMP-фрейма на подписку:

```
SUBSCRIBE
id:sub-1
destination:/topic/project/42/tasks

```

---

## Kafka-события

Активируется профилем `kafka`. Topic: **`task-events`**, 3 партиции, ключ — `taskId` (строка).

### Событие `TASK_CREATED`

```json
{
  "eventType": "TASK_CREATED",
  "taskId": 123,
  "taskTitle": "Реализовать экспорт задач в CSV",
  "projectId": 42,
  "projectName": "Celesma",
  "assigneeId": 7,
  "assigneeUsername": "anna",
  "creatorId": 3,
  "creatorUsername": "max",
  "newStatus": "NEW",
  "occurredAt": "2025-06-28T12:34:56"
}
```

### Событие `TASK_STATUS_CHANGED`

```json
{
  "eventType": "TASK_STATUS_CHANGED",
  "taskId": 123,
  "taskTitle": "...",
  "projectId": 42,
  "projectName": "...",
  "assigneeId": 7,
  "assigneeUsername": "anna",
  "creatorId": 3,
  "creatorUsername": "max",
  "oldStatus": "IN_PROGRESS",
  "newStatus": "COMPLETED",
  "occurredAt": "2025-06-28T13:00:00"
}
```
