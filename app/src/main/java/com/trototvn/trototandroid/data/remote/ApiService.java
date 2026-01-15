package com.trototvn.trototandroid.data.remote;

import com.trototvn.trototandroid.data.model.ResponseData;
import com.trototvn.trototandroid.data.model.User;
import com.trototvn.trototandroid.data.model.auth.LoginRequest;
import com.trototvn.trototandroid.data.model.auth.LoginResponse;
import com.trototvn.trototandroid.data.model.auth.RegisterRequest;
import com.trototvn.trototandroid.data.model.auth.RegisterResponse;

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
 * API Service interface
 */
public interface ApiService {

    // ========== Authentication ==========

    /**
     * POST - Login
     */
    @POST("api/auth/login")
    Single<ResponseData<LoginResponse>> login(@Body LoginRequest request);

    /**
     * POST - Register
     */
    @POST("api/auth/register")
    Single<ResponseData<RegisterResponse>> register(@Body RegisterRequest request);

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
}
