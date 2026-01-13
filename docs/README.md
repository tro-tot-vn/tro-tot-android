# 🏠 Trọ Tốt Android - Base Architecture

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Java](https://img.shields.io/badge/Language-Java-orange.svg)](https://www.java.com/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

> **Modern Android CRUD application base with RxJava, Hilt DI, and clean architecture**

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Documentation](#documentation)
- [Contributing](#contributing)

---

## 🎯 Overview

This project provides a **production-ready base architecture** for Android CRUD applications with:

✅ **Clean Architecture** - Separation of concerns with clear layers  
✅ **Reactive Programming** - RxJava3 for async operations  
✅ **Dependency Injection** - Hilt for clean DI  
✅ **Type Safety** - ViewBinding for all UI  
✅ **Security** - EncryptedSharedPreferences for sensitive data  
✅ **Best Practices** - Modern Android development patterns  

---

## ✨ Features

### **Core Architecture**
- 🏗️ **MVVM Pattern** - ViewModel + LiveData
- 🔄 **Repository Pattern** - Abstract data sources
- 📦 **Generic Base Classes** - Reduce boilerplate
- 🎨 **ViewBinding** - Auto-inflated, type-safe views

### **Networking**
- 🌐 **Retrofit + OkHttp** - RESTful API client
- 🔌 **RxJava Adapter** - Reactive API calls
- 🔐 **Auto Token Injection** - AuthInterceptor
- 📡 **Network Monitoring** - Connectivity checks
- 🐛 **Request Logging** - Debug network calls

### **State Management**
- 📊 **Resource Wrapper** - Loading/Success/Error states
- 🎯 **Centralized Error Handling** - User-friendly messages
- 💾 **Session Management** - Encrypted storage

### **UI Components**
- 🔄 **LoadingDialog** - Custom progress dialogs
- ✅ **ConfirmDialog** - Builder pattern dialogs
- 🎨 **SnackbarHelper** - Colored feedback (success/error/warning/info)

### **Utilities**
- ✔️ **StringUtils** - Email, phone, password validation
- 🕒 **DateUtils** - Formatting, parsing, relative time
- 📱 **ViewUtils** - Toast, keyboard, visibility helpers
- 🌐 **NetworkUtils** - WiFi/mobile data detection

---

## 🛠️ Tech Stack

| Category | Technology |
|----------|-----------|
| **Language** | Java 11 |
| **Architecture** | MVVM + Clean Architecture |
| **Async** | RxJava3 |
| **DI** | Hilt (Dagger) |
| **Networking** | Retrofit 2.9, OkHttp 4.10 |
| **UI** | ViewBinding, Material Design 3 |
| **Storage** | EncryptedSharedPreferences |
| **Image Loading** | Glide 4.16 |
| **Logging** | Timber 5.0 |
| **Navigation** | Navigation Component 2.7 |
| **Pagination** | Paging 3 + RxJava |

---

## 📁 Project Structure

```
app/src/main/java/com/trototvn/trototandroid/
│
├── 🎯 App.java                        # Hilt Application
├── 📱 MainActivity.java               # Entry point
│
├── 📊 data/
│   ├── model/                         # Data models
│   │   ├── Resource.java             # State wrapper
│   │   └── User.java                 # Example model
│   ├── remote/                        # API layer
│   │   ├── ApiClient.java            # Retrofit setup
│   │   ├── ApiService.java           # API endpoints
│   │   ├── AuthInterceptor.java      # Token injection
│   │   └── NetworkInterceptor.java   # Network check
│   └── repository/                    # Data repositories
│       └── UserRepository.java       # Example repo
│
├── 💉 di/                             # Dependency Injection
│   └── NetworkModule.java            # Network DI
│
├── 🎨 ui/
│   ├── base/                          # Base components
│   │   ├── BaseActivity.java        # Generic activity
│   │   ├── BaseFragment.java        # Generic fragment
│   │   ├── BaseViewModel.java       # Generic ViewModel
│   │   ├── BaseAdapter.java         # Generic adapter
│   │   └── BaseDiffCallback.java    # DiffUtil helper
│   └── dialog/                        # Custom dialogs
│       ├── LoadingDialog.java
│       └── ConfirmDialog.java
│
└── 🔧 utils/                          # Utilities
    ├── Constants.java
    ├── DateUtils.java
    ├── ErrorHandler.java
    ├── NetworkUtils.java
    ├── SessionManager.java
    ├── SnackbarHelper.java
    ├── StringUtils.java
    └── ViewUtils.java
```

---

## 🚀 Getting Started

### **Prerequisites**

- Android Studio Hedgehog or later
- JDK 11+
- Gradle 8.0+
- Android SDK 27+ (minSdk 27, targetSdk 36)

### **Installation**

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/trtt-android.git
   cd trtt-android
   ```

2. **Update API Base URL**  
   Edit these files with your API endpoint:
   - `data/remote/ApiClient.java` (line 19)
   - `di/NetworkModule.java` (line 26)
   - `utils/Constants.java` (line 11)

3. **Sync Gradle**
   ```bash
   ./gradlew build
   ```

4. **Run the app**
   ```bash
   ./gradlew installDebug
   ```

---

## 📚 Documentation

Comprehensive documentation is available in the `docs/` folder:

| Document | Description |
|----------|-------------|
| 📖 [Setup Guide](docs/SETUP.md) | Step-by-step setup instructions |
| 🏗️ [Architecture](docs/ARCHITECTURE.md) | Architecture patterns & principles |
| 🌐 [API Guide](docs/API_GUIDE.md) | How to integrate APIs |
| 🧩 [Components](docs/COMPONENTS.md) | Base components usage |
| 💡 [Best Practices](docs/BEST_PRACTICES.md) | Code standards & patterns |

Quick references:
- 📝 [USAGE_GUIDE.md](../USAGE_GUIDE.md) - Code examples
- ✅ [Task Checklist](../.gemini/antigravity/brain/*/task.md)

---

## 🎓 Usage Example

### **Create a new CRUD screen in 3 steps:**

**1️⃣ Define your model:**
```java
public class Product {
    private int id;
    private String name;
    private double price;
    // getters/setters...
}
```

**2️⃣ Create API service:**
```java
public interface ApiService {
    @GET("products")
    Single<List<Product>> getProducts();
    
    @POST("products")
    Single<Product> createProduct(@Body Product product);
}
```

**3️⃣ Build your UI:**
```java
@AndroidEntryPoint
public class ProductActivity extends BaseActivity<ActivityProductBinding> {
    @Inject ProductViewModel viewModel;
    
    @Override
    protected void observeData() {
        viewModel.getProducts().observe(this, resource -> {
            // Handle Loading/Success/Error
        });
    }
}
```

**That's it!** 🎉

---

## 🤝 Contributing

Contributions are welcome! Please read our [Contributing Guide](CONTRIBUTING.md) first.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- [Android Jetpack](https://developer.android.com/jetpack)
- [RxJava](https://github.com/ReactiveX/RxJava)
- [Hilt](https://dagger.dev/hilt/)
- [Retrofit](https://square.github.io/retrofit/)
- [Material Design](https://material.io/develop/android)

---

## 📞 Contact

For questions or support, please open an issue or contact:
- 📧 Email: your.email@example.com
- 🐦 Twitter: [@yourhandle](https://twitter.com/yourhandle)

---

<div align="center">
  <strong>⭐ Star this repo if you find it helpful! ⭐</strong>
</div>
