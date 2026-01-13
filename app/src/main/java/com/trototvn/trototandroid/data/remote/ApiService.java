package com.trototvn.trototandroid.data.remote;

import com.trototvn.trototandroid.data.model.User;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Example API Service with RxJava support
 * This is a template - modify according to your actual API endpoints
 */
public interface ApiService {

    // ========== Example CRUD operations for User ==========

    /**
     * GET - Fetch list of users
     */
    @GET("users")
    Single<List<User>> getUsers();

    /**
     * GET - Fetch user by ID
     */
    @GET("users/{id}")
    Single<User> getUserById(@Path("id") int userId);

    /**
     * GET - Search users with pagination
     */
    @GET("users/search")
    Single<List<User>> searchUsers(
            @Query("query") String query,
            @Query("page") int page,
            @Query("limit") int limit);

    /**
     * POST - Create new user
     */
    @POST("users")
    Single<User> createUser(@Body User user);

    /**
     * PUT - Update existing user
     */
    @PUT("users/{id}")
    Single<User> updateUser(@Path("id") int userId, @Body User user);

    /**
     * DELETE - Delete user
     */
    @DELETE("users/{id}")
    Completable deleteUser(@Path("id") int userId);

    // ========== Authentication (example) ==========

    /**
     * POST - Login
     */
    // @POST("auth/login")
    // Single<LoginResponse> login(@Body LoginRequest request);

    /**
     * POST - Register
     */
    // @POST("auth/register")
    // Single<User> register(@Body RegisterRequest request);
}
