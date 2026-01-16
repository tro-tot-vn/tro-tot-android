package com.trototvn.trototandroid.data.repository;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.post.PostDetail;

import io.reactivex.rxjava3.core.Single;

/**
 * Post Detail Repository interface
 * Clean architecture - defines contract for post detail operations
 */
public interface PostDetailRepository {
    /**
     * Get post detail by ID
     * @param postId Post ID to fetch
     * @return Single with Resource wrapper containing PostDetail or error
     */
    Single<Resource<PostDetail>> getPostDetail(int postId);
}
