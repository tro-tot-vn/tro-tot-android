package com.trototvn.trototandroid.data.repository;

import com.trototvn.trototandroid.data.model.User;
import com.trototvn.trototandroid.data.remote.ApiService;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

/**
 * Example Repository for User data
 * Repository pattern abstracts data sources from ViewModel
 */
public class UserRepository {

    private final ApiService apiService;

    @Inject
    public UserRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    /**
     * Get all users
     */
    public Single<List<User>> getUsers() {
        return apiService.getUsers();
    }

    /**
     * Get user by ID
     */
    public Single<User> getUserById(int userId) {
        return apiService.getUserById(userId);
    }

    /**
     * Search users
     */
    public Single<List<User>> searchUsers(String query, int page, int limit) {
        return apiService.searchUsers(query, page, limit);
    }

    /**
     * Create user
     */
    public Single<User> createUser(User user) {
        return apiService.createUser(user);
    }

    /**
     * Update user
     */
    public Single<User> updateUser(int userId, User user) {
        return apiService.updateUser(userId, user);
    }

    /**
     * Delete user
     */
    public Completable deleteUser(int userId) {
        return apiService.deleteUser(userId);
    }
}
