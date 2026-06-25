> ⚠️ **Deprecated** — this READ.ME is archived.

# Celesma API

REST API для системы управления проектами с React фронтендом.

## Stack

- **Backend:** Spring Boot 3.5.4, Java 21, PostgreSQL
- **Security:** JWT (stateless)
- **Frontend:** React (отдельный проект)

## Запуск

### 1. База данных

```bash
docker run -d \
  --name celesma-db \
  -e POSTGRES_DB=celesma_db \
  -e POSTGRES_USER=celesma_user \
  -e POSTGRES_PASSWORD=celesma_password \
  -p 5432:5432 \
  postgres:15-alpine
```

### 2. Backend

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

API доступен на `http://localhost:8080`

### 3. Frontend (React)

```bash
cd ../celesma-frontend
npm install
npm run dev
```

Frontend доступен на `http://localhost:3000`

## API Endpoints

### Auth
- `POST /api/v1/auth/register` — регистрация
- `POST /api/v1/auth/login` — вход

### Projects
- `GET /api/v1/projects` — список проектов
- `POST /api/v1/projects` — создать проект
- `GET /api/v1/projects/{id}` — детали проекта

### Tasks
- `GET /api/v1/projects/{id}/tasks` — задачи проекта
- `POST /api/v1/projects/{id}/tasks` — создать задачу
- `PATCH /api/v1/tasks/{id}/status` — изменить статус

## Архитектура

```
controller/     — REST endpoints
service/        — бизнес-логика
  interfaces/   — интерфейсы сервисов
repository/     — JPA репозитории
model/          — JPA entities
  enums/        — перечисления
dto/            — Data Transfer Objects
  auth/         — DTO для аутентификации
security/       — JWT, фильтры, UserPrincipal
exception/      — глобальная обработка ошибок
config/         — конфигурация Spring
```

## Принципы

- Dependency Injection через интерфейсы
- `@RequiredArgsConstructor` вместо ручных конструкторов
- `@Override` на всех методах интерфейса
- Spring Events для межсервисной коммуникации
- Stateless JWT аутентификация
- CORS настроен для React на порту 3000
