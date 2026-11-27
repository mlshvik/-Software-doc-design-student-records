# План розробки

Проєкт розробляється поетапно, кожен етап – окремий набір комітів.

## Етап 1 – Аналіз і каркас ✅
- [x] Аналіз предметної області, актори, функціональні та нефункціональні вимоги (`docs/01-domain-analysis.md`)
- [x] Use Case діаграма (`docs/diagrams/use-case.*`)
- [x] Каркас бекенду Spring Boot 3 (Maven, Java 17), `docker-compose.yml` з PostgreSQL

## Етап 2 – Модель даних ✅
- [x] JPA-сутності: Faculty, Specialty, StudyGroup, Student, Teacher, Subject, TeachingAssignment, Grade, AcademicDebt, ScheduleEntry, Notification, AppUser, SystemSetting
- [x] Перелічення: StudentStatus, ControlType, GradeAttempt, LessonType, WeekType, Role, DebtStatus
- [x] Міграції Flyway (`V1__init_schema.sql`, `V2__seed_data.sql`)
- [x] Репозиторії Spring Data JPA
- [x] Діаграма класів системи з позначенням GoF-шаблонів і ER-діаграма (`docs/02-class-diagram.md`)
- [x] Тести доменної моделі та перевірка міграцій на PostgreSQL у CI

## Етап 3 – Бізнес-логіка і шаблони проектування ✅
- [x] Singleton – `GradingScale` (100-бальна → національна → ЄКТС)
- [x] Strategy – `GpaCalculationStrategy` (`SimpleAverageStrategy`, `CreditWeightedStrategy`)
- [x] State – стани студента (`ActiveState`, `AcademicLeaveState`, `ExpelledState`, `GraduatedState`)
- [x] Chain of Responsibility – валідація оцінки (`ScoreRangeHandler` → `StudentActiveHandler` → `TeacherOwnsAssignmentHandler` → `AttemptLimitHandler`)
- [x] Observer – `GradeEventPublisher` + `AcademicDebtObserver`, `NotificationObserver`, `GpaRecalculationObserver`
- [x] Facade – `StudentRecordFacade` (оцінки + бал + борги + розклад)
- [x] Сервіси: `StudentService`, `GradeService`, `GpaService`, `DebtService`, `ScheduleService` з `ScheduleConflictChecker`
- [x] Unit-тести шаблонів (JUnit 5 + Mockito), тест контексту Spring на H2
- [x] Опис реалізації шаблонів, діаграма послідовності і діаграма станів (`docs/design-patterns.md`)

## Етап 4 – REST API, безпека, звіти ✅
- [x] DTO, мапери, глобальна обробка помилок (ProblemDetail, RFC 7807)
- [x] Spring Security + JWT, ролі ADMIN / TEACHER / STUDENT, доступ лише до власних даних
- [x] REST-контролери для всіх сутностей
- [x] Template Method – `AbstractReportGenerator`; Factory Method – `ReportGeneratorFactory`; Builder – `Report.Builder`
- [x] Звіти: успішність за групами та курсами, статистика по викладачах, академічні борги
- [x] Swagger UI, опис API (`docs/04-rest-api.md`), діаграма послідовності формування звіту
- [x] Тести: `@WebMvcTest`, `@SpringBootTest` (H2), JWT, шаблони звітів

## Етап 5 – Клієнтська частина (React) ✅
- [x] Vite + React + TypeScript, маршрутизація, авторизація (JWT, відновлення сесії)
- [x] Кабінет студента: оцінки, середній бал, борги, розклад, сповіщення
- [x] Кабінет викладача: мої дисципліни, відомість групи, виставлення оцінок і перескладань, статистика
- [x] CI: перевірка типів і збірка фронтенду (`docs/frontend.md`)

## Етап 6 – Адмін-частина
- [ ] CRUD довідників, студентів, викладачів, предметів, призначень
- [ ] Конструктор розкладу з перевіркою конфліктів
- [ ] Сторінки звітів із графіками, налаштування системи

## Етап 7 – Тести, розгортання, документація
- [ ] Інтеграційні тести API
- [ ] Dockerfile бекенду і фронтенду, запуск усього проєкту через `docker compose up`
- [ ] Опис архітектури (`docs/03-architecture.md`), фінальний README
