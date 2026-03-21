# 🧑‍🏫 Teachers Preferences App
## 👨‍🎓 Участник проекта

- **ФИО:** Гамин Вадим Игоревич  
- **Группа:** 5130202/20202
## 📦 Стек технологий

| Компонент      | Технология             |
|----------------|------------------------|
| Backend        | Java + Spring Boot     |
| Database       | PostgreSQL             |
| Frontend       | React + Vite           |
| Сборка и запуск| Docker Compose         |
| Безопасность   | JWT Token Auth         |
| Тесты          | JUnit, Spring Test     |

Контейнеризация через Docker. Все тесты автоматически запускаются в процессе сборки.

---

## 🚀 Быстрый запуск

### ⚙️ Требования

- Docker
- Bash (на Windows можно использовать Git Bash или WSL)

---

### 🧬 Клонирование репозитория

```bash
git clone https://github.com/gaminv/teachers_preferences.git
cd teachers_preferences
```

---

### ▶️ Сборка, тесты и запуск

```bash
docker compose up --build
```

Эта команда:
- 🔨 Соберёт backend и frontend
- ✅ Запустит unit- и интеграционные тесты
- 🚀 Запустит PostgreSQL, Spring Boot backend и React frontend
- 🌐 Приложение будет доступно на http://localhost
- 👤 Вход в администратора: `admin / admin1`

---

## 🧪 Тесты

Все **unit** и **integration** тесты автоматически запускаются на этапе `mvn verify` при сборке backend-контейнера.

### Запуск тестов локально (полный контур по ТЗ)

Backend:

```bash
cd backend
mvn clean verify
mvn test "-Dtest=*UnitTest,*ServiceTest,*ControllerTest,*AuthServiceTest,*AdminServiceTest,*TeacherServiceTest"
mvn test "-Dtest=*IntegrationTest"
mvn test "-Dtest=*SystemTest,*SystemE2ETest"
```

Frontend:

```bash
cd frontend
npm ci
npm run test:unit
npm run test:integration
npm run test:system
npm run test:coverage
```

Документы по тестированию:
- `docs/TEST_PLAN_INTEGRATION.md`
- `docs/TEST_PLAN_SYSTEM_E2E.md`
- `docs/TESTING_REPORT.md`

Покрытие:
- Backend (JaCoCo): `backend/target/site/jacoco/index.html`
- Frontend (Vitest): `frontend/coverage/index.html`

![image](https://github.com/user-attachments/assets/1a93974e-87be-4b36-97e7-34555e2ce11f)

---

## 🖼️ Интерфейс веб-сайта

### 🔐 Вход в систему (экран авторизации)
Пользователь видит форму входа, в которой необходимо ввести логин и пароль. После успешной авторизации пользователь перенаправляется на панель в зависимости от своей роли: админ или преподаватель.  
🔒 Форма защищена проверкой токена JWT. Логин: admin. Пароль admin1
![image](https://github.com/user-attachments/assets/1a42219f-7b89-40b3-8cc2-0a7b135fd92c)

---

### 🛠️ Панель администратора

Администратор после входа видит список пожеланий преподавателей. Доступны следующие действия:

- 📋 Регистрация нового преподавателя
- ⚙️ Изменение текста кнопок и дедлайнов
- 👁️ Просмотр предпочтений преподавателей
- 📤 Экспорт всех предпочтений в Excel
![image](https://github.com/user-attachments/assets/0a5acf39-2765-45b4-8778-c54b03839880)

---

### 👤 Регистрация нового пользователя

На отдельной форме администратор может создать нового пользователя:
- 🧑‍🏫 Указывается логин, полное имя и пароль.
  ![image](https://github.com/user-attachments/assets/90caedef-d3e2-4b3b-9687-d0deff2a70ee)

---

### 🧾 Интерфейс преподавателя

После входа преподаватель попадает на страницу с анкетой, где можно выбрать:

- 📅 Предпочтения к **семестру**
- 📆 Предпочтения к **сессии**

![image](https://github.com/user-attachments/assets/0c6e185d-d6c3-409d-a1f8-eefc50ef22d4)

---

### 📝 Добавление и редактирование пожеланий

Пожелания преподавателя заполняются через интуитивную форму:

- ✅ Валидируются и сохраняются на сервере
- 🔁 Можно скопировать предпочтения из предыдущего заполнения

Поля анкеты включают:

- 📆 Желаемые дни и часы
- 🧑‍💻 Формат (офлайн/онлайн)
- 🏢 Аудитория, доска, техника и т.п.
- 💬 Комментарии
![image](https://github.com/user-attachments/assets/9912b85f-5386-4e62-bb34-32591b46fd43)

---

### 📤 Экспорт предпочтений преподавателей

Администратор может экспортировать все заполненные формы преподавателей:

- 📄 Формат: Excel
- 📊 Данные структурированы и подходят для обработки в Excel/Google Sheets
![image](https://github.com/user-attachments/assets/860206a7-9643-43fe-ad78-49e9bc478140)

---
