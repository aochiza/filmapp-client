# FilmApp Android Client

FilmApp — Android-клиент мобильной системы-справочника по фильмам, предназначенный для просмотра информации о фильмах, поиска, фильтрации, управления избранным и списком «Посмотреть позже».

Приложение является частью клиент–серверной системы и взаимодействует с backend-сервером через REST API (`/api/v1/...`).

Backend-сервер отвечает за обработку бизнес-логики, авторизацию пользователей и работу с базой данных PostgreSQL, а Android-клиент реализует пользовательский интерфейс, управление состоянием приложения и взаимодействие с API.

### Основные возможности

- регистрация и авторизация пользователей;
- просмотр каталога фильмов;
- поиск и фильтрация фильмов;
- просмотр детальной информации о фильме;
- управление списком избранного;
- управление списком «Посмотреть позже»;
- получение случайного фильма;
- управление каталогом фильмов (для пользователей с ролью `ADMIN`).

## 1) Возможности приложения

- **Авторизация / регистрация**
  - Экран логина и регистрации.
  - Первичная аутентификация выполняется через Firebase Authentication, после чего приложение взаимодействует с backend-сервером для получения JWT-токена.
  - После успешного ответа backend токен сохраняется локально (DataStore).
- **Список фильмов (главный экран)**
  - Постраничная загрузка с подгрузкой следующей страницы при скролле.
  - Поиск по строке (debounce).
  - Фильтрация по жанру (через `genreId` в API).
  - Дополнительные расширенные фильтры в UI (bottom sheet): год (range), мульти-жанры, страна, минимальный рейтинг.
- **Детали фильма**
  - Загрузка фильма по `id`.
  - Добавление/удаление из избранного.
  - Добавление/удаление в «Посмотреть позже».
  - Snackbar уведомления по действиям.
- **Поиск**
  - Поиск по названию/режиссёру через API `films?search=...` (debounce).
  - Доп. фильтры в UI: жанр (через `genreId`), минимальный рейтинг (локально), год (локально).
- **Избранное**
  - Просмотр списка избранных фильмов (API `films/favorites`).
- **Посмотреть позже (Watch later)**
  - Просмотр списка «посмотреть позже» (API `watch-later`), отображается на экране профиля.
  - Удаление фильма из списка «посмотреть позже».
- **Случайный фильм**
  - Получение случайного фильма (API `films/random`) с опциональным `genreId`.
  - Свайп для получения следующего фильма (анимации/параллакс в UI).
  - Фильтрация по жанрам (чипы) — влияет на `genreId` запроса.
- **Работа с изображениями**
  - Загрузка постеров через Coil (`AsyncImage`) по `posterUrl`.
- **Админ-функции**
  - Для роли `ADMIN` отображается FAB «Добавить фильм» и доступно удаление фильма свайпом.
  - Создание фильма — экран добавления (`AddEditFilmScreen`).

---

## 2) Стек технологий

### Язык
- **Kotlin**
  - Основной язык проекта (`.kt` файлы, Gradle Kotlin DSL).

### UI
- **Jetpack Compose**
  - Все экраны реализованы на Compose (`presentation/.../*.kt`), `setContent { ... }` в `MainActivity`.
- **Material 3**
  - Используются компоненты Material 3 (`Scaffold`, `TopAppBar`, `NavigationBar`, `FilterChip`, `ModalBottomSheet`, `RangeSlider`, `SwipeToDismissBox` и т.д.).
- **Navigation Compose**
  - Навигация через `NavHost`, `composable`, параметры маршрутов (`film_detail/{filmId}`) — `presentation/navigation/NavGraph.kt`.

### Архитектура
- **MVVM**
  - UI (Compose) подписывается на `StateFlow` из `ViewModel` (`collectAsState`, `collectAsStateWithLifecycle`).
  - Примеры: `FilmsViewModel`, `AuthViewModel`, `RandomViewModel`, `SearchViewModel`.
- **Repository pattern**
  - Доменные интерфейсы в `domain/repository/*`, реализации в `data/repository/*`.
  - Примеры: `FilmRepositoryImpl`, `AuthRepositoryImpl`, `GenreRepositoryImpl`.
- **UseCase слой**
  - `domain/usecase/...` содержит use case классы, которые вызываются из ViewModel (например, `GetFilmsUseCase`, `GetRandomFilmUseCase`, `CreateFilmUseCase`).

### Асинхронность
- **Kotlin Coroutines**
  - `viewModelScope.launch { ... }`, suspend-функции репозиториев и API.
- **Flow / StateFlow**
  - `MutableStateFlow`/`StateFlow` для состояния UI и данных (`filmsState`, `authState`, `randomState`).
  - Debounce и combine в поиске (`SearchViewModel`).

### Сетевой слой
- **Retrofit**
  - API интерфейсы `AuthApi`, `FilmApi`, `GenreApi`.
- **OkHttp**
  - `OkHttpClient` + interceptors (логирование + авторизация).
- **Gson Converter**
  - `GsonConverterFactory` для сериализации/десериализации DTO.

### DI
- **Hilt**
  - `@HiltAndroidApp` (`App`), `@AndroidEntryPoint` (`MainActivity`), `@HiltViewModel`.
  - Модули: `NetworkModule`, `DatabaseModule`, `RepositoryModule`, `AppModule`.

### Изображения
- **Coil (coil-compose)**
  - `AsyncImage` для загрузки `posterUrl`.

### Локальное хранение данных (клиент)

- **Room**
  - Используется для локального кеширования данных на устройстве.
  - Содержит `AppDatabase`, сущности `FilmEntity`, `GenreEntity` и DAO (`FilmDao`, `GenreDao`).
  - Реализована миграция `1 → 2` с добавлением поля `isWatchLater`.

- **DataStore Preferences**
  - Используется для хранения пользовательских данных:
    - JWT-токена;
    - идентификатора пользователя;
    - имени;
    - роли пользователя.

### Интеграция с backend

Android-клиент взаимодействует с backend-сервером, реализованным на Kotlin + Ktor.

Сервер предоставляет REST API для:

- авторизации пользователей;
- получения каталога фильмов;
- работы с избранным;
- управления списком «Посмотреть позже»;
- получения жанров;
- получения случайного фильма.

Backend использует JWT-авторизацию и PostgreSQL в качестве основной базы данных.

---

## 3) Архитектура приложения

### Пакеты и назначение

- `app/src/main/java/com/filmapp/`
  - `App.kt` — точка входа Hilt (`@HiltAndroidApp`).
- `presentation/`
  - `MainActivity` — определяет стартовый экран (login/films) по DataStore токену и Firebase.
  - `navigation/` — маршруты (`Screen`), нижняя навигация (`BottomNavItem`) и `NavGraph`.
  - `auth/` — экраны и `AuthViewModel`.
  - `films/` — список фильмов, карточка, экран добавления фильма, фильтры, `FilmsViewModel`.
  - `detail/` — экран деталей + `FilmDetailViewModel`.
  - `search/` — экран поиска + `SearchViewModel`.
  - `favorites/` — избранное + `FavoritesViewModel`.
  - `profile/` — профиль + watch later + `ProfileViewModel`.
  - `random/` — случайный фильм + `RandomViewModel`.
  - `components/` — переиспользуемые UI-компоненты (кнопки, текстфилды, состояния, top bar).
  - `theme/` — Material 3 тема, типографика, формы, цвета.
- `domain/`
  - `model/` — доменные модели (`Film`, `Genre`, `User`).
  - `repository/` — интерфейсы репозиториев.
  - `usecase/` — use cases (auth/film/genre).
- `data/`
  - `remote/api/` — Retrofit интерфейсы.
  - `remote/dto/` — DTO запросов/ответов (`AuthResponse`, `FilmResponse`, `FilmsListResponse`, ...).
  - `remote/interceptor/` — `AuthInterceptor`, `TokenProvider`.
  - `remote/firebase/` — `FirebaseAuthSource` (Firebase Auth).
  - `repository/` — реализации репозиториев.
  - `local/` — Room database, dao, entities.
- `di/`
  - Hilt модули: сеть, БД, биндинги репозиториев, Firebase источник.

### Поток данных

UI (Compose) → ViewModel → UseCase → Repository → REST API / локальное хранилище (Room) → Domain Model → UI

Пример (список фильмов):
`FilmsScreen` → `FilmsViewModel.loadFilms()` → `GetFilmsUseCase` → `FilmRepositoryImpl.getFilms()` → `FilmApi.getFilms()` → маппинг DTO → `Film`.

---

## 4) API взаимодействие

Базовый URL задаётся в `app/build.gradle.kts`:

- `BuildConfig.BASE_URL = "http://10.0.2.2:8081/api/v1/"`

### Эндпоинты

- POST `/auth/register` — регистрация пользователя
- POST `/auth/login` — авторизация
- GET `/films` — список фильмов
- GET `/films/{id}` — детали фильма
- POST `/films` — создание фильма (ADMIN)
- DELETE `/films/{id}` — удаление фильма (ADMIN)
- GET `/films/favorites` — избранное
- POST `/films/{id}/favorite` — добавить в избранное
- DELETE `/films/{id}/favorite` — удалить из избранного
- GET `/watch-later` — список «Посмотреть позже»
- POST `/watch-later/{filmId}` — добавить в watch later
- DELETE `/watch-later/{filmId}` — удалить из watch later
- GET `/genres` — список жанров
- GET `/films/random` — случайный фильм

---

## 5) Структура экранов

Список экранов определяется в `presentation/navigation/Screen.kt` и `NavGraph.kt`.

### `Login` (`route = "login"`)
- **Назначение**: вход пользователя.
- **Данные/логика**: `AuthViewModel.login()` → `LoginUseCase` → `AuthRepositoryImpl.login()`.
- **API**: `POST auth/login`.
- **Дополнительно**: перед backend логином выполняется `FirebaseAuthSource.login()`.

### `Register` (`route = "register"`)
- **Назначение**: регистрация пользователя.
- **Данные/логика**: `AuthViewModel.register()` → `RegisterUseCase` → `AuthRepositoryImpl.register()`.
- **API**: `POST auth/register`.
- **Дополнительно**: перед backend регистрацией выполняется `FirebaseAuthSource.register()`.

### `Films` (`route = "films"`)
- **Назначение**: главный каталог.
- **Данные/логика**: `FilmsViewModel` (pagination, search debounce, жанры, админ роль).
- **API**:
  - `GET films` (список)
  - `GET genres` (жанры)
  - `POST/DELETE films/{id}/favorite` (избранное)
  - `POST/DELETE watch-later/{filmId}` (watch later)
  - `DELETE films/{id}` (удаление, только UI-путь для ADMIN)
- **UI-компоненты**: поиск (OutlinedTextField), чипы жанров, карточки, bottom sheet фильтров, FAB (ADMIN), swipe-to-delete (ADMIN).

### `FilmDetail` (`route = "film_detail/{filmId}"`)
- **Назначение**: детали фильма.
- **API**: `GET films/{id}`, favorite/watch-later actions.
- **UI**: постер, текстовые блоки, top bar действия, snackbar.

### `Search` (`route = "search"`)
- **Назначение**: поиск по каталогу.
- **API**: `GET films?search=...&genreId=...` + локальные фильтры по году/рейтингу.
- **UI**: строка поиска, панель фильтров (жанры/slider), список карточек.

### `Favorites` (`route = "favorites"`)
- **Назначение**: список избранных.
- **API**: `GET films/favorites`.
- **UI**: список карточек, empty/loading/error состояния.

### `Profile` (`route = "profile"`)
- **Назначение**: профиль и список «посмотреть позже», выход.
- **API**: `GET watch-later`, `DELETE watch-later/{filmId}`.
- **Данные**: имя берётся из DataStore (`TokenProvider.getName()`).
- **UI**: карточка профиля, список watch-later, диалог logout.

### `Random` (`route = "random"`)
- **Назначение**: случайный фильм (свайп), фильтрация по жанрам.
- **API**: `GET films/random?genreId=...`, `GET genres`.
- **UI**: полноэкранная карточка, градиенты/параллакс/анимации, чипы жанров.

### `AddFilm` (`route = "add_film"`)
- **Назначение**: создание фильма.
- **API**: `POST films` + `GET genres`.
- **Важно**: сам доступ контролируется UI по роли `ADMIN` (см. `FilmsViewModel.isAdmin`).

---

## 6) Работа с изображениями

- **Библиотека**: Coil (`coil-compose`).
- **Где используется**: карточки фильмов и детальная страница, случайный фильм.
- **Источник изображения**: поле `posterUrl` в `FilmResponse`/`Film` (может быть `null`).

---

## 7) Авторизация

### Как происходит login/register

1) UI вызывает `AuthViewModel.login/register`.
2) `AuthRepositoryImpl` сначала обращается к **FirebaseAuthSource**:
   - `register(email, password)` или `login(email, password)`
3) Затем выполняется запрос в backend:
   - `AuthApi.register(LoginRequest/RegisterRequest)`
4) Ответ backend (`AuthResponse`) сохраняется в `TokenProvider.saveAuthData()`:
   - `jwt_token`, `user_id`, `name`, `role` (в DataStore Preferences `auth_prefs`)

### Хранение и передача токена

- **Хранение**: `TokenProvider` (DataStore Preferences).
- **Передача**: `AuthInterceptor` добавляет заголовок:
  - `Authorization: Bearer <jwt_token>`
  - Токен добавляется только если он не пустой (`getTokenSync()`).

### Стартовая проверка логина

В `MainActivity` стартовый экран выбирается по двум условиям:
- `tokenProvider.isLoggedIn().first()` (токен есть в DataStore)
- `firebaseAuthSource.isLoggedIn()` (Firebase currentUser != null)

Если оба true → стартуем с `films`, иначе `login`.

---

## 8) Установка и запуск

### Требования

- Android Studio;
- JDK 17;
- backend-сервер FilmApp;
- Firebase configuration (`google-services.json`).

### Подготовка backend

Перед запуском Android-клиента необходимо убедиться, что backend-сервер запущен и доступен.

По умолчанию клиент ожидает API по адресу:

```text
http://10.0.2.2:8081/api/v1/
```

### Запуск Android-клиента

1. Открыть проект в Android Studio.
2. Выполнить Gradle Sync.
3. Убедиться, что backend и PostgreSQL запущены.
4. Запустить приложение на эмуляторе или устройстве.

### Настройка URL backend

В Android-клиенте используется:

```kotlin
BuildConfig.BASE_URL = "http://10.0.2.2:8081/api/v1/"
```

Для физического устройства необходимо заменить адрес на IP компьютера либо публичный домен backend-сервера.

---

## 9) Структура проекта

Упрощённая структура (ключевые директории):

```
filmapp/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/filmapp/
│       │   ├── App.kt
│       │   ├── di/
│       │   │   ├── AppModule.kt
│       │   │   ├── DatabaseModule.kt
│       │   │   ├── NetworkModule.kt
│       │   │   └── RepositoryModule.kt
│       │   ├── data/
│       │   │   ├── local/ (Room)
│       │   │   ├── remote/ (Retrofit + Firebase + Interceptors)
│       │   │   └── repository/ (impl)
│       │   ├── domain/
│       │   │   ├── model/
│       │   │   ├── repository/
│       │   │   └── usecase/
│       │   └── presentation/
│       │       ├── MainActivity.kt
│       │       ├── navigation/
│       │       ├── theme/
│       │       ├── components/
│       │       ├── auth/
│       │       ├── films/
│       │       ├── detail/
│       │       ├── search/
│       │       ├── favorites/
│       │       ├── profile/
│       │       └── random/
│       └── res/
│           └── xml/network_security_config.xml
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/libs.versions.toml
```

