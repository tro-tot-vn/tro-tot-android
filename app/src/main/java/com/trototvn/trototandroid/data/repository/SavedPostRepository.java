package com.trototvn.trototandroid.data.repository;

import com.trototvn.trototandroid.data.model.Resource;

import io.reactivex.rxjava3.core.Single;

/**
 * Saved Post Repository interface
 * Clean architecture - defines contract for saved post operations
 */
public interface SavedPostRepository {
    /**
     * Save post to favorites
     */
    Single<Resource<Void>> savePost(int postId);

    /**
     * Log contact interaction (view phone)
     */
    Single<Resource<Void>> logContact(int postId);
}
