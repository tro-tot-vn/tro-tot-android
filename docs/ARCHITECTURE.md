# 🏗️ Architecture Guide

## Overview

This project follows **Clean Architecture** principles combined with **MVVM pattern** for a maintainable, testable, and scalable Android application.

---

## 📐 Architecture Layers

```
┌─────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │   Activity   │  │   Fragment   │  │    Dialog    │  │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  │
│         │                  │                  │          │
│         └──────────────────┴──────────────────┘          │
│                            │                             │
│                     ┌──────▼────────┐                    │
│                     │   ViewModel   │                    │
│                     └──────┬────────┘                    │
└────────────────────────────┼──────────────────────────────┘
                             │
┌────────────────────────────┼──────────────────────────────┐
│                    DOMAIN LAYER                           │
│                     ┌──────▼────────┐                     │
│                     │  Repository   │                     │
│                     └──────┬────────┘                     │
└────────────────────────────┼──────────────────────────────┘
                             │
┌────────────────────────────┼──────────────────────────────┐
│                     DATA LAYER                            │
│           ┌─────────────────┴─────────────────┐           │
│    ┌──────▼──────┐                   ┌────────▼───────┐  │
│    │ Remote Data │                   │  Local Data    │  │
│    │ (Retrofit)  │                   │ (Room/Prefs)   │  │
│    └─────────────┘                   └────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

---

## 🎯 MVVM Pattern

### **Model**
- Data classes (`User`, `Product`, etc.)
- Represents business data
- No Android dependencies

### **View** 
- Activities, Fragments
- Observes ViewModel via LiveData
- Updates UI based on state changes
- No business logic

### **ViewModel**
- Holds UI state
- Exposes data via LiveData
- Communicates with Repository
- Survives configuration changes

---

## 📦 Layer Responsibilities

### **1. Presentation Layer** (`ui/`)

**Responsibilities:**
- Display data to user
- Handle user interactions
- Observe ViewModel state changes

**Components:**
```java
// Activities extend BaseActivity
public class UserListActivity extends BaseActivity<ActivityUserListBinding> {
    @Inject UserViewModel viewModel;
    
    @Override
    protected void observeData() {
        viewModel.getUsersLiveData().observe(this, resource -> {
            // Update UI based on resource state
        });
    }
}
```

**Rules:**
- ✅ Only UI logic
- ✅ Observe ViewModel
- ❌ No direct API calls
- ❌ No business logic

---

### **2. Domain Layer** (`data/repository/`)

**Responsibilities:**
- Abstract data sources
- Coordinate between remote and local data
- Business logic decisions

**Components:**
```java
public class UserRepository {
    private final ApiService apiService;
    private final UserDao userDao; // If using Room
    
    @Inject
    public UserRepository(ApiService apiService) {
        this.apiService = apiService;
    }
    
    public Single<List<User>> getUsers() {
        // Can combine remote + local data here
        return apiService.getUsers();
    }
}
```

**Rules:**
- ✅ Coordinate data sources
- ✅ Return RxJava types
- ❌ No Android dependencies
- ❌ No UI logic

---

### **3. Data Layer** (`data/`)

#### **Remote Data** (`data/remote/`)

**API Service:**
```java
public interface ApiService {
    @GET("users")
    Single<List<User>> getUsers();
}
```

**Interceptors:**
- `AuthInterceptor` - Adds Bearer token
- `NetworkInterceptor` - Checks connectivity
- `LoggingInterceptor` - Logs requests/responses

#### **Local Data** (Future: Room Database)
```java
@Dao
public interface UserDao {
    @Query("SELECT * FROM users")
    Single<List<User>> getUsers();
    
    @Insert
    Completable insertUsers(List<User> users);
}
```

---

## 🔄 Data Flow

### **Example: Fetching Users**

```
┌──────────┐  1. User clicks   ┌──────────┐
│ Activity │ ─────────────────> │ViewModel │
└──────────┘                    └────┬─────┘
     ▲                               │
     │                               │ 2. Calls repository
     │                               ▼
     │                          ┌────────────┐
     │                          │ Repository │
     │                          └─────┬──────┘
     │                                │
     │                                │ 3. Makes API call
     │                                ▼
     │                          ┌────────────┐
     │                          │ ApiService │
     │                          └─────┬──────┘
     │ 7. Updates UI                  │
     │    based on state              │ 4. HTTP request
     │                                ▼
┌────┴─────┐  6. Emits        ┌──────────────┐
│ Activity │ <─ Resource ───  │ Backend API  │
└──────────┘    state         └──────────────┘
                │
                │ 5. Returns data
                ▼
           ┌─────────┐
           │ViewModel│ (processes & wraps in Resource)
           └─────────┘
```

### **Code Flow:**

```java
// 1. Activity triggers action
binding.btnLoad.setOnClickListener(v -> viewModel.loadUsers());

// 2. ViewModel calls repository
public void loadUsers() {
    handleLoading(usersLiveData);
    
    Disposable d = repository.getUsers()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            users -> handleSuccess(usersLiveData, users),
            error -> handleError(usersLiveData, ErrorHandler.getErrorMessage(error))
        );
    addDisposable(d);
}

// 3-5. Repository -> ApiService -> Backend
public Single<List<User>> getUsers() {
    return apiService.getUsers();
}

// 6-7. Activity observes LiveData
viewModel.getUsersLiveData().observe(this, resource -> {
    switch (resource.getStatus()) {
        case LOADING: showLoading(true); break;
        case SUCCESS: displayUsers(resource.getData()); break;
        case ERROR: showError(resource.getMessage()); break;
    }
});
```

---

## 🎨 State Management

### **Resource Wrapper Pattern**

All API responses are wrapped in `Resource<T>`:

```java
public class Resource<T> {
    public enum Status { SUCCESS, ERROR, LOADING }
    
    private final Status status;
    private final T data;
    private final String message;
}
```

**Benefits:**
- ✅ Single source of truth for state
- ✅ Consistent UI updates
- ✅ Easy to handle loading/error/success

**Usage in ViewModel:**
```java
private MutableLiveData<Resource<List<User>>> usersLiveData = new MutableLiveData<>();

// Set loading
handleLoading(usersLiveData);

// Set success
handleSuccess(usersLiveData, users);

// Set error
handleError(usersLiveData, "Error message");
```

---

## 💉 Dependency Injection

### **Hilt Architecture**

```
┌──────────────────┐
│  @HiltAndroidApp │
│       App        │
└────────┬─────────┘
         │
         ├─> @InstallIn(SingletonComponent)
         │   ├─ NetworkModule
         │   ├─ DatabaseModule
         │   └─ RepositoryModule
         │
         └─> @AndroidEntryPoint
             ├─ Activities
             ├─ Fragments
             └─ ViewModels (@HiltViewModel)
```

**Modules:**

```java
@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {
    
    @Provides
    @Singleton
    public Retrofit provideRetrofit() {
        return new Retrofit.Builder()...build();
    }
    
    @Provides
    @Singleton
    public ApiService provideApiService(Retrofit retrofit) {
        return retrofit.create(ApiService.class);
    }
}
```

**Injection:**

```java
// In Activity
@AndroidEntryPoint
public class MainActivity extends BaseActivity<...> {
    @Inject UserViewModel viewModel;
}

// In ViewModel
@HiltViewModel
public class UserViewModel extends BaseViewModel {
    @Inject
    public UserViewModel(UserRepository repository) {
        this.repository = repository;
    }
}

// In Repository
public class UserRepository {
    @Inject
    public UserRepository(ApiService apiService) {
        this.apiService = apiService;
    }
}
```

---

## 🔄 Reactive Programming

### **RxJava Operators**

```java
repository.getUsers()
    .subscribeOn(Schedulers.io())        // Run on IO thread
    .observeOn(AndroidSchedulers.mainThread()) // Observe on Main
    .map(users -> filterActive(users))    // Transform data
    .retry(3)                             // Retry on error
    .timeout(30, TimeUnit.SECONDS)        // Timeout
    .subscribe(
        users -> handleSuccess(users),
        error -> handleError(error)
    );
```

### **RxJava Types**

| Type | Use Case | Example |
|------|----------|---------|
| `Single<T>` | Single value | `Single<User> getUser()` |
| `Observable<T>` | Stream of values | `Observable<Location> trackLocation()` |
| `Completable` | No return value | `Completable deleteUser()` |
| `Maybe<T>` | 0 or 1 value | `Maybe<User> findUser()` |

---

## 🧪 Testing Strategy

```
┌─────────────────────────────────────┐
│         Unit Tests (70%)            │
│  - ViewModels                       │
│  - Repositories                     │
│  - Utils                            │
└─────────────────────────────────────┘
┌─────────────────────────────────────┐
│      Integration Tests (20%)        │
│  - API Service                      │
│  - Database                         │
└─────────────────────────────────────┘
┌─────────────────────────────────────┐
│         UI Tests (10%)              │
│  - Critical user flows              │
└─────────────────────────────────────┘
```

---

## 📝 Best Practices

1. **Single Responsibility** - Each class has one job
2. **Dependency Inversion** - Depend on abstractions
3. **Immutability** - Prefer immutable data classes
4. **Testability** - Design for easy testing
5. **Error Handling** - Centralized error messages
6. **Resource Management** - Auto-dispose RxJava subscriptions

---

## 🚀 Scalability

This architecture scales well because:

✅ **New features** - Just add new ViewModel + Repository  
✅ **Team growth** - Clear layer boundaries  
✅ **Testing** - Each layer independently testable  
✅ **Maintenance** - Changes isolated to specific layers  

---

**Next:** [API Integration Guide](API_GUIDE.md) →
