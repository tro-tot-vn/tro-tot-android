package com.trototvn.trototandroid.data.repository;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.post.Post;
import com.trototvn.trototandroid.data.model.profile.CustomerProfile;
import com.trototvn.trototandroid.data.model.profile.Subscription;

import java.io.File;
import java.util.List;

import io.reactivex.rxjava3.core.Single;

/**
 * Repository interface for Profile operations
 * Follows clean architecture - domain layer contract
 */
public interface ProfileRepository {
    
    /**
     * Get current user's profile
     * @return Single emitting Resource with customer profile
     */
    Single<Resource<CustomerProfile>> getMyProfile();
    
    /**
     * Update profile without avatar
     * @param profile Updated profile data
     * @return Single emitting Resource with void
     */
    Single<Resource<Void>> updateProfile(CustomerProfile profile);
    
    /**
     * Update profile with avatar
     * @param profile Updated profile data
     * @param avatarFile Avatar image file (optional)
     * @return Single emitting Resource with void
     */
    Single<Resource<Void>> updateProfileWithAvatar(CustomerProfile profile, File avatarFile);
    
    /**
     * Get saved posts
     * @return Single emitting Resource with list of posts
     */
    Single<Resource<List<Post>>> getSavedPosts();
    
    /**
     * Save a post
     * @param postId Post ID to save
     * @return Single emitting Resource with void
     */
    Single<Resource<Void>> savePost(int postId);
    
    /**
     * Unsave a post
     * @param postId Post ID to unsave
     * @return Single emitting Resource with void
     */
    Single<Resource<Void>> unsavePost(int postId);
    
    /**
     * Get view history
     * @return Single emitting Resource with list of posts
     */
    Single<Resource<List<Post>>> getViewHistory();
    
    /**
     * Get subscriptions
     * @return Single emitting Resource with list of subscriptions
     */
    Single<Resource<List<Subscription>>> getSubscriptions();
    
    /**
     * Create subscription
     * @param city City name
     * @param district District name
     * @return Single emitting Resource with created subscription
     */
    Single<Resource<Subscription>> createSubscription(String city, String district);
    
    /**
     * Delete subscription
     * @param subscriptionId Subscription ID to delete
     * @return Single emitting Resource with void
     */
    Single<Resource<Void>> deleteSubscription(int subscriptionId);
    
    /**
     * Change password
     * @param oldPassword Current password
     * @param newPassword New password
     * @return Single emitting Resource with void
     */
    Single<Resource<Void>> changePassword(String oldPassword, String newPassword);
}
