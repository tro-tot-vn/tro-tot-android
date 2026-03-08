# TroTot Android Application 🏠🚀

Trọ Tốt Android is a modern, high-performance mobile application for searching and management of rental properties in Vietnam. Built with official Android best practices, focusing on scalability, maintainability, and a premium user experience.

---

## 🛠️ Tech Stack

The application leverages a robust set of modern Android libraries and tools to ensure a seamless development and user experience:

- **Language**: **Java** (SDK 33+ compatibility).
- **Architecture**: **MVVM (Model-View-ViewModel)** with **Repository Pattern**.
- **Dependency Injection**: **Hilt** (Dagger) for clean and testable code.
- **Networking**: **Retrofit 2** & **OkHttp 4** with custom Interceptors (Auth, Logging, Network status).
- **Reactive Programming**: **RxJava 3** & **RxAndroid** for efficient asynchronous handling.
- **UI & Layout**: **XML** with **Material Components (M3)**, **View Binding**, and **Advanced Animators**.
- **Navigation**: **Jetpack Navigation Component** for robust fragment-to-fragment routing.
- **Image Loading**: **Glide** with hardware acceleration and caching.
- **Real-time Communication**: **Socket.IO** for instant messaging and updates.
- **Push Notifications**: **Firebase Cloud Messaging (FCM)**.
- **Logging & Debugging**: **Timber** for centralized logging.
- **Data Persistence**: **Encrypted SharedPreferences** via `SessionManager`.

---

## 🏗️ Architecture (MVVM)

The project follows a modular, clean architecture approach to separate concerns and ensure high performance:

### 1. Presentation Layer (`ui`)
- **Fragments**: Responsible for UI rendering and user interactions.
- **ViewModels**: Handle UI state logic, survive configuration changes, and communicate with the Domain/Data layers via **LiveData**.
- **ViewBinding**: Type-safe view access, replacing `findViewById`.

### 2. Domain Layer (`data.model`)
- **POJOs/DTOs**: Pure data classes representing API responses and internal models.
- **Resource Wrapper**: A generic wrapper class (`Resource<T>`) to handle `LOADING`, `SUCCESS`, and `ERROR` states across the app.

### 3. Data Layer (`data.repository` & `data.remote`)
- **Repositories**: Single source of truth. They abstract the data source (API vs. Local Cache) from the ViewModels.
- **API Services**: Retrofit interfaces defining RESTful endpoints.
- **Interceptors**: 
    - `AuthInterceptor`: Automatically attaches JWT tokens to outgoing requests.
    - `NetworkInterceptor`: Handles offline scenarios and informs the user.
    - `TokenAuthenticator`: Manages automatic token refresh (Logout if expired).

### 4. Utility Layer (`utils`)
- **SessionManager**: Securely handles user sessions, tokens, and preferences.
- **SocketIOManager**: Manages real-time socket connections with RxJava lifecycle support.
- **ImageUtils**: Centralized Glide helpers for image transformations and loading states.

---

## 🚀 Getting Started

1. Clone the repository.
2. Ensure you have the latest **Android Studio (Giraffe or newer)**.
3. Add your `google-services.json` to the `app/` directory (required for FCM).
4. Sync project with Gradle files.
5. Update `Constants.BASE_URL` with your backend endpoint.

---

## ✅ Current Features

- **User Profiling**: Secure Login/Register, Session persistence, and custom Profile management.
- **Real-time Chat**: Instant communication between renters and owners.
- **Notifications**: System-wide push notifications for interactions.
- **Responsive UI**: Optimized for both high-end and budget Android devices.

---

**Developed with ❤️ by the TroTot Engineering Team.**
