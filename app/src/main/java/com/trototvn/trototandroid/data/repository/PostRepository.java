package com.trototvn.trototandroid.data.repository;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.post.Post;
import com.trototvn.trototandroid.data.model.post.RecommendationResponse;
import com.trototvn.trototandroid.data.model.search.SearchParams;
import com.trototvn.trototandroid.data.model.search.SearchResponse;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

/**
 * Repository interface for Post operations
 * Follows clean architecture principles - domain layer contract
 */
public interface PostRepository {

    /**
     * Get latest posts for home screen
     * @param limit Number of posts to fetch
     * @return Single emitting Resource with list of posts
     */
    Single<Resource<List<Post>>> getLatestPosts(int limit);

    /**
     * Get personalized recommendations
     * @param page Page number (1-indexed)
     * @param pageSize Number of items per page
     * @param logId Optional recommendation log ID for caching
     * @return Single emitting Resource with recommendation response
     */
    Single<Resource<RecommendationResponse>> getRecommendations(int page, int pageSize, Integer logId);

    /**
     * Search posts with filters
     * @param params Search parameters
     * @return Single emitting Resource with search response
     */
    Single<Resource<SearchResponse>> search(SearchParams params);
}
