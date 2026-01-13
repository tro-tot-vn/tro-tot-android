# 📱 Base Components Implementation - USAGE GUIDE

## 🎯 Overview
This project now includes a complete base architecture for CRUD operations with RxJava, Hilt DI, and modern Android practices.

---

## 📦 Dependencies Added
All dependencies have been added to `build.gradle`:
- ✅ RxJava (Core, Android, Retrofit Adapter)
- ✅ Hilt (Dependency Injection)
- ✅ EncryptedSharedPreferences (Security)
- ✅ Timber (Logging)
- ✅ Navigation Component
- ✅ Paging Library
- ✅ SwipeRefreshLayout

---

## 🏗️ Architecture Components

### 1. **Base Classes** (`ui/base/`)
- `BaseActivity<VB>` - Generic activity with ViewBinding
- `BaseFragment<VB>` - Generic fragment with ViewBinding
- `BaseViewModel` - ViewModel with RxJava support
- `BaseAdapter<T, VB>` - RecyclerView adapter with DiffUtil
- `BaseDiffCallback<T>` - DiffUtil helper

### 2. **Data Layer** (`data/`)
- `Resource<T>` - State wrapper (Loading/Success/Error)
- `ApiClient` - Retrofit singleton with RxJava
- `ApiService` - Example API interface
- `AuthInterceptor` - Auto token injection
- `NetworkInterceptor` - Connectivity check

### 3. **Utilities** (`utils/`)
- `SessionManager` - Encrypted session storage
- `ErrorHandler` - Centralized error messages
- `StringUtils` - Validation & formatting
- `ViewUtils` - UI helpers
- `DateUtils` - Date operations
- `NetworkUtils` - Connectivity checks
- `SnackbarHelper` - Snackbar variants
- `Constants` - App constants

### 4. **UI Components** (`ui/dialog/`)
- `LoadingDialog` - Custom loading dialog
- `ConfirmDialog` - Confirmation dialog with builder

### 5. **DI Setup** (`di/`)
- `NetworkModule` - Provides Retrofit, OkHttp, etc.
- `App` - Hilt application class

---

## 🚀 Usage Examples

### **Creating an Activity with ViewBinding**
```java
@AndroidEntryPoint
public class UserListActivity extends BaseActivity<ActivityUserListBinding> {
    
    @Inject
    UserViewModel viewModel;
    
    @Override
    protected void setupViews() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadUsers());
    }
    
    @Override
    protected void observeData() {
        viewModel.getUsersLiveData().observe(this, resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    showLoading(true);
                    break;
                case SUCCESS:
                    showLoading(false);
                    updateUI(resource.getData());
                    break;
                case ERROR:
                    showLoading(false);
                    showToast(resource.getMessage());
                    break;
            }
        });
    }
}
```

### **Creating a ViewModel**
```java
@HiltViewModel
public class UserViewModel extends BaseViewModel {
    
    private final UserRepository repository;
    private final MutableLiveData<Resource<List<User>>> usersLiveData = new MutableLiveData<>();
    
    @Inject
    public UserViewModel(UserRepository repository) {
        this.repository = repository;
    }
    
    public void loadUsers() {
        handleLoading(usersLiveData);
        
        Disposable disposable = repository.getUsers()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        users -> handleSuccess(usersLiveData, users),
                        error -> handleError(usersLiveData, ErrorHandler.getErrorMessage(error))
                );
        
        addDisposable(disposable);
    }
    
    public LiveData<Resource<List<User>>> getUsersLiveData() {
        return usersLiveData;
    }
}
```

### **Creating a Repository**
```java
public class UserRepository {
    
    private final ApiService apiService;
    
    @Inject
    public UserRepository(ApiService apiService) {
        this.apiService = apiService;
    }
    
    public Single<List<User>> getUsers() {
        return apiService.getUsers();
    }
    
    public Single<User> createUser(User user) {
        return apiService.createUser(user);
    }
}
```

### **Using Dialogs**
```java
// Loading Dialog
LoadingDialog loadingDialog = new LoadingDialog(this);
loadingDialog.show("Fetching data...");
// loadingDialog.dismiss();

// Confirm Dialog
new ConfirmDialog.Builder(this)
    .setTitle("Delete User")
    .setMessage("Are you sure you want to delete this user?")
    .setPositiveButton("Delete")
    .setNegativeButton("Cancel")
    .setListener(new ConfirmDialog.OnConfirmListener() {
        @Override
        public void onConfirm() {
            deleteUser();
        }
        
        @Override
        public void onCancel() {
            // Do nothing
        }
    })
    .show();
```

### **Using Utilities**
```java
// String validation
if (StringUtils.isValidEmail(email)) {
    // Valid email
}

// Show snackbar
SnackbarHelper.showSuccess(binding.getRoot(), "User created successfully!");

// Check network
if (NetworkUtils.isConnected(this)) {
    // Make API call
}

// Session management
SessionManager sessionManager = new SessionManager(this);
sessionManager.saveSession(token, refreshToken, userId, userName, email);
String token = sessionManager.getToken();
```

---

## 🔧 Configuration Required

### 1. **Update API Base URL**
Edit these files:
- `data/remote/ApiClient.java` line 21
- `di/NetworkModule.java` line 28
- `utils/Constants.java` line 11

### 2. **Sync Gradle**
Run: `Gradle Sync` to download all dependencies

### 3. **Create Your Models**
Modify `data/model/User.java` or create new models based on your API

### 4. **Create Your API Endpoints**
Modify `data/remote/ApiService.java` with your actual endpoints

### 5. **Provide ApiService in NetworkModule**
Uncomment and modify lines 92-96 in `di/NetworkModule.java`:
```java
@Provides
@Singleton
public ApiService provideApiService(Retrofit retrofit) {
    return retrofit.create(ApiService.class);
}
```

---

## ✅ Next Steps
1. ⚙️ Sync Gradle project
2. 🔗 Update BASE_URL in configuration
3. 📝 Define your data models
4. 🌐 Create API service interfaces
5. 🎨 Build your UI screens
6. 🧪 Test the implementation

---

**Happy Coding! 🚀**
