package com.trototvn.trototandroid.data.repository;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.post.Post;
import com.trototvn.trototandroid.data.model.post.RecommendationResponse;
import com.trototvn.trototandroid.data.model.search.SearchParams;
import com.trototvn.trototandroid.data.model.search.SearchResponse;

import java.util.List;

import io.reactivex.rxjava3.core.Single;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Repository interface for Post operations
 * Follows clean architecture principles - domain layer contract
 */
public interface PostRepository {

    /**
     * Get latest posts for home screen
     * 
     * @param limit Number of posts to fetch
     * @return Single emitting Resource with list of posts
     */
    Single<Resource<List<Post>>> getLatestPosts(int page, int limit);

    /**
     * Get personalized recommendations
     * 
     * @param page     Page number (1-indexed)
     * @param pageSize Number of items per page
     * @param logId    Optional recommendation log ID for caching
     * @return Single emitting Resource with recommendation response
     */
    Single<Resource<RecommendationResponse>> getRecommendations(int page, int pageSize, Integer logId);

    /**
     * Search posts with filters
     * 
     * @param params Search parameters
     * @return Single emitting Resource with search response
     */
    Single<Resource<SearchResponse>> search(SearchParams params);

    /**
     * Log a click on a search result
     */
    Single<Resource<Void>> logSearchClick(int searchLogId, int searchLogItemId);

    /**
     * Submit feedback for a search
     */
    Single<Resource<Void>> submitSearchFeedback(int searchLogId, boolean isHelpful, java.util.List<String> issues, String comment);

    /**
     * Get user's own posts with cursor pagination and status filter
     */
    Single<Resource<com.trototvn.trototandroid.data.model.post.MyPostsResponse>> getMyPosts(String status, Integer cursor, int limit);

    Single<Resource<Void>> hidePost(int postId);

    /**
     * Unhide my post to make it public
     */
    Single<Resource<Void>> unhidePost(int postId);

    /**
     * Get dynamic list of Wards by District ID
     */
    Single<Resource<com.trototvn.trototandroid.data.model.location.WardListResponse>> getWards(String districtId);

    /**
     * Create new post (multipart/form-data)
     */
    Single<Resource<Void>> createPost(
        RequestBody title,
        RequestBody description,
        RequestBody price,
        RequestBody acreage,
        RequestBody streetNumber,
        RequestBody street,
        RequestBody ward,
        RequestBody district,
        RequestBody city,
        RequestBody interiorStatus,
        List<MultipartBody.Part> images,
        MultipartBody.Part video
    );

    /**
     * Edit existing post (multipart/form-data)
     */
    Single<Resource<Void>> editPost(
        int postId,
        RequestBody title,
        RequestBody description,
        RequestBody price,
        RequestBody acreage,
        RequestBody streetNumber,
        RequestBody street,
        RequestBody ward,
        RequestBody district,
        RequestBody city,
        RequestBody interiorStatus,
        RequestBody oldFiles,
        List<MultipartBody.Part> images,
        MultipartBody.Part video
    );

    /**
     * Get details of my post for editing
     */
    Single<Resource<com.trototvn.trototandroid.data.model.post.PostDetail>> getDetailMyPost(int postId);
}
