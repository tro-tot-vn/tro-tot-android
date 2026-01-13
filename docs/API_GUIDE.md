# 🌐 API Integration Guide

Complete guide for integrating REST APIs using Retrofit + RxJava.

---

## 📋 Table of Contents

1. [Quick Start](#quick-start)
2. [API Service Definition](#api-service-definition)
3. [Request Types](#request-types)
4. [Authentication](#authentication)
5. [Error Handling](#error-handling)
6. [Interceptors](#interceptors)
7. [Testing APIs](#testing-apis)

---

## 🚀 Quick Start

### **Step 1: Define Your Model**

```java
package com.trototvn.trototandroid.data.model;

public class Product {
    private int id;
    private String name;
    private String description;
    private double price;
    private String imageUrl;
    
    // Constructors, getters, setters...
}
```

### **Step 2: Create API Service Interface**

```java
package com.trototvn.trototandroid.data.remote;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.*;

public interface ApiService {
    
    @GET("products")
    Single<List<Product>> getProducts();
    
    @GET("products/{id}")
    Single<Product> getProductById(@Path("id") int productId);
    
    @POST("products")
    Single<Product> createProduct(@Body Product product);
    
    @PUT("products/{id}")
    Single<Product> updateProduct(@Path("id") int id, @Body Product product);
    
    @DELETE("products/{id}")
    Completable deleteProduct(@Path("id") int productId);
}
```

### **Step 3: Provide Service in Hilt Module**

```java
// In di/NetworkModule.java
@Provides
@Singleton
public ApiService provideApiService(Retrofit retrofit) {
    return retrofit.create(ApiService.class);
}
```

### **Step 4: Create Repository**

```java
public class ProductRepository {
    private final ApiService apiService;
    
    @Inject
    public ProductRepository(ApiService apiService) {
        this.apiService = apiService;
    }
    
    public Single<List<Product>> getProducts() {
        return apiService.getProducts();
    }
}
```

### **Step 5: Use in ViewModel**

```java
@HiltViewModel
public class ProductViewModel extends BaseViewModel {
    private final ProductRepository repository;
    private final MutableLiveData<Resource<List<Product>>> productsLiveData = new MutableLiveData<>();
    
    @Inject
    public ProductViewModel(ProductRepository repository) {
        this.repository = repository;
    }
    
    public void loadProducts() {
        handleLoading(productsLiveData);
        
        Disposable d = repository.getProducts()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                products -> handleSuccess(productsLiveData, products),
                error -> handleError(productsLiveData, ErrorHandler.getErrorMessage(error))
            );
        
        addDisposable(d);
    }
    
    public LiveData<Resource<List<Product>>> getProductsLiveData() {
        return productsLiveData;
    }
}
```

---

## 📡 API Service Definition

### **HTTP Methods**

#### **GET - Retrieve Data**
```java
// Simple GET
@GET("users")
Single<List<User>> getUsers();

// GET with path parameter
@GET("users/{id}")
Single<User> getUserById(@Path("id") int userId);

// GET with query parameters
@GET("users/search")
Single<List<User>> searchUsers(
    @Query("query") String searchQuery,
    @Query("page") int page,
    @Query("limit") int limit
);

// GET with multiple query params as Map
@GET("products/filter")
Single<List<Product>> filterProducts(@QueryMap Map<String, String> filters);

// GET with header
@GET("users/me")
@Headers("X-Custom-Header: value")
Single<User> getCurrentUser();
```

#### **POST - Create Data**
```java
// POST with JSON body
@POST("users")
Single<User> createUser(@Body User user);

// POST with form data
@FormUrlEncoded
@POST("login")
Single<LoginResponse> login(
    @Field("email") String email,
    @Field("password") String password
);

// POST with multipart (file upload)
@Multipart
@POST("users/avatar")
Single<User> uploadAvatar(
    @Part("user_id") RequestBody userId,
    @Part MultipartBody.Part file
);
```

#### **PUT - Update Data**
```java
// PUT entire object
@PUT("users/{id}")
Single<User> updateUser(
    @Path("id") int userId,
    @Body User user
);

// PATCH partial update
@PATCH("users/{id}")
Single<User> partialUpdateUser(
    @Path("id") int userId,
    @Body Map<String, Object> updates
);
```

#### **DELETE - Remove Data**
```java
// DELETE with path parameter
@DELETE("users/{id}")
Completable deleteUser(@Path("id") int userId);

// DELETE with query parameter
@DELETE("posts")
Completable deletePost(@Query("post_id") int postId);
```

---

## 🔐 Authentication

### **Bearer Token Authentication**

The `AuthInterceptor` automatically adds tokens to requests:

```java
// AuthInterceptor.java (already implemented)
@Override
public Response intercept(Chain chain) throws IOException {
    Request original = chain.request();
    Request.Builder requestBuilder = original.newBuilder();
    
    String token = sessionManager.getToken();
    if (token != null && !token.isEmpty()) {
        requestBuilder.addHeader("Authorization", "Bearer " + token);
    }
    
    return chain.proceed(requestBuilder.build());
}
```

### **Login Flow**

```java
// 1. Define login request/response models
public class LoginRequest {
    private String email;
    private String password;
}

public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private User user;
}

// 2. Add login endpoint
@POST("auth/login")
Single<LoginResponse> login(@Body LoginRequest request);

// 3. Handle in repository
public Single<LoginResponse> login(String email, String password) {
    LoginRequest request = new LoginRequest(email, password);
    return apiService.login(request);
}

// 4. Save token in ViewModel
public void login(String email, String password) {
    Disposable d = repository.login(email, password)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            response -> {
                // Save token
                sessionManager.saveSession(
                    response.getAccessToken(),
                    response.getRefreshToken(),
                    response.getUser().getId(),
                    response.getUser().getName(),
                    response.getUser().getEmail()
                );
                
                handleSuccess(loginLiveData, response.getUser());
            },
            error -> handleError(loginLiveData, ErrorHandler.getErrorMessage(error))
        );
    addDisposable(d);
}
```

---

## ⚠️ Error Handling

### **Centralized Error Messages**

The `ErrorHandler` converts exceptions to user-friendly messages:

```java
// Automatically handles these error types:
- HttpException (400, 401, 403, 404, 500, 503)
- SocketTimeoutException
- UnknownHostException
- JsonParseException
- IOException
```

### **Custom Error Response**

```java
// 1. Define error response model
public class ApiError {
    private String message;
    private int code;
    private List<String> errors;
}

// 2. Parse error in repository
public Single<User> createUser(User user) {
    return apiService.createUser(user)
        .onErrorResumeNext(error -> {
            if (error instanceof HttpException) {
                HttpException httpError = (HttpException) error;
                try {
                    String errorBody = httpError.response().errorBody().string();
                    ApiError apiError = new Gson().fromJson(errorBody, ApiError.class);
                    return Single.error(new Exception(apiError.getMessage()));
                } catch (IOException e) {
                    return Single.error(error);
                }
            }
            return Single.error(error);
        });
}
```

### **Retry Logic**

```java
// Retry failed requests
repository.getUsers()
    .retry(3) // Retry up to 3 times
    .retryWhen(errors -> errors.flatMap(error -> {
        if (error instanceof IOException) {
            return Observable.timer(2, TimeUnit.SECONDS); // Wait 2s before retry
        }
        return Observable.error(error); // Don't retry other errors
    }))
    .subscribe(...);
```

---

## 🔌 Interceptors

### **Current Interceptors**

1. **AuthInterceptor** - Adds Bearer token
2. **NetworkInterceptor** - Checks internet connection
3. **LoggingInterceptor** - Logs requests/responses

### **Add Custom Interceptor**

```java
// 1. Create interceptor
public class CustomHeaderInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request().newBuilder()
            .addHeader("X-App-Version", BuildConfig.VERSION_NAME)
            .addHeader("X-Platform", "Android")
            .build();
        return chain.proceed(request);
    }
}

// 2. Add to NetworkModule
@Provides
@Singleton
public OkHttpClient provideOkHttpClient(...) {
    return new OkHttpClient.Builder()
        .addInterceptor(new CustomHeaderInterceptor())
        .addInterceptor(authInterceptor)
        .addInterceptor(networkInterceptor)
        .addInterceptor(loggingInterceptor)
        .build();
}
```

---

## 🎯 Advanced Patterns

### **Pagination**

```java
// API service
@GET("products")
Single<ProductResponse> getProducts(
    @Query("page") int page,
    @Query("limit") int limit
);

// Response model
public class ProductResponse {
    private List<Product> data;
    private int currentPage;
    private int totalPages;
    private int totalItems;
}

// Repository
public Single<ProductResponse> getProducts(int page) {
    return apiService.getProducts(page, Constants.PAGE_SIZE);
}

// ViewModel with Paging 3
// (Use PagingSource - see Paging 3 docs)
```

### **Caching Responses**

```java
// 1. Add cache interceptor
public class CacheInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        
        CacheControl cacheControl = new CacheControl.Builder()
            .maxAge(10, TimeUnit.MINUTES)
            .build();
        
        return response.newBuilder()
            .header("Cache-Control", cacheControl.toString())
            .build();
    }
}

// 2. Add cache to OkHttpClient
File cacheDir = new File(context.getCacheDir(), "http_cache");
Cache cache = new Cache(cacheDir, 10 * 1024 * 1024); // 10 MB

OkHttpClient client = new OkHttpClient.Builder()
    .cache(cache)
    .addInterceptor(new CacheInterceptor())
    .build();
```

### **Combining Multiple APIs**

```java
// Parallel requests
Single.zip(
    apiService.getUserProfile(),
    apiService.getUserPosts(),
    apiService.getUserFollowers(),
    (profile, posts, followers) -> {
        return new UserDetails(profile, posts, followers);
    }
)
.subscribeOn(Schedulers.io())
.observeOn(AndroidSchedulers.mainThread())
.subscribe(...);

// Sequential requests
apiService.getUser()
    .flatMap(user -> apiService.getUserPosts(user.getId()))
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(...);
```

---

## 🧪 Testing APIs

### **Mock Server for Testing**

```java
@Test
public void testGetUsers() {
    // Setup mock server
    MockWebServer server = new MockWebServer();
    String mockResponse = "[{\"id\":1,\"name\":\"John\"}]";
    server.enqueue(new MockResponse().setBody(mockResponse));
    
    // Create API service pointing to mock server
    ApiService apiService = new Retrofit.Builder()
        .baseUrl(server.url("/"))
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .build()
        .create(ApiService.class);
    
    // Test
    List<User> users = apiService.getUsers().blockingGet();
    assertEquals(1, users.size());
    assertEquals("John", users.get(0).getName());
}
```

---

## 📝 Best Practices

✅ **Use RxJava types** - Single, Completable, Observable  
✅ **Handle errors** - Always subscribe with error handler  
✅ **Dispose subscriptions** - Use BaseViewModel.addDisposable()  
✅ **Thread switching** - subscribeOn(IO), observeOn(Main)  
✅ **Centralize errors** - Use ErrorHandler  
✅ **Log requests** - Enable LoggingInterceptor in debug  
✅ **Timeouts** - Set reasonable timeout values  
✅ **Validate input** - Use StringUtils before API calls  

❌ **Don't block main thread** - Always use subscribeOn(Schedulers.io())  
❌ **Don't ignore errors** - Handle all error cases  
❌ **Don't hardcode URLs** - Use Constants  
❌ **Don't leak subscriptions** - Always dispose  

---

**Next:** [Components Guide](COMPONENTS.md) →
