# Collection Preferences

Полноценный Full-Stack проект на Spring Boot + React для управления преподавательскими предпочтениями расписания.

## Требования

- Git
- Java 17+
- Maven 3.6+
- Node.js 16+ (npm 8+)
- PostgreSQL 12+ (создать БД с именем `webapp`)

## Клонирование репозитория

```bash
git clone https://github.com/gaminv/teachers_preferences.git
cd teachers_preferences
```

## Запуск бэкенда

1. Создать базу данных `webapp`.
2. Запустить:
```bash
cd backend
mvn spring-boot:run
```
Адрес: http://localhost:8080

## Настройка и запуск фронтенда

```bash
cd frontend
npm install
npm run dev
```
Адрес: http://localhost:5173


