package com.trototvn.trototandroid.data.repository;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.rating.Rating;
import com.trototvn.trototandroid.data.model.rating.RatingListResponse;
import com.trototvn.trototandroid.data.model.rating.RatingStats;

import io.reactivex.rxjava3.core.Single;

/**
 * Rating Repository interface
 * Clean architecture - defines contract for rating operations
 */
public interface RatingRepository {
    /**
     * Add or update rating for a post
     */
    Single<Resource<Void>> addRating(int postId, int numStar, String comment);

    /**
     * Get ratings list with cursor pagination
     */
    Single<Resource<RatingListResponse>> getRatings(int postId, int limit, String cursor);

    /**
     * Get current user's rating on a post
     */
    Single<Resource<Rating>> getMyRating(int postId);

    /**
     * Delete current user's rating
     */
    Single<Resource<Void>> deleteMyRating(int postId);

    /**
     * Get rating statistics (average, count)
     */
    Single<Resource<RatingStats>> getRatingStats(int postId);
}
