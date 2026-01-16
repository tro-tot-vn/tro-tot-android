package com.trototvn.trototandroid.data.repository;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.rating.AddRatingRequest;
import com.trototvn.trototandroid.data.model.rating.Rating;
import com.trototvn.trototandroid.data.model.rating.RatingListResponse;
import com.trototvn.trototandroid.data.model.rating.RatingStats;
import com.trototvn.trototandroid.data.remote.ApiService;

import javax.inject.Inject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Rating Repository implementation
 * Handles rating operations with error handling
 */
public class RatingRepositoryImpl implements RatingRepository {

    private final ApiService apiService;

    @Inject
    public RatingRepositoryImpl(ApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public Single<Resource<Void>> addRating(int postId, int numStar, String comment) {
        AddRatingRequest request = new AddRatingRequest(numStar, comment);
        
        return apiService.addRating(postId, request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> Resource.<Void>success(null))
                .onErrorReturn(throwable -> {
                    Timber.e(throwable, "Error adding rating");
                    return handleError(throwable, "Lỗi khi đánh giá");
                });
    }

    @Override
    public Single<Resource<RatingListResponse>> getRatings(int postId, int limit, String cursor) {
        return apiService.getRatings(postId, limit, cursor)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> {
                    if (response != null && response.getData() != null) {
                        return Resource.success(response.getData());
                    } else {
                        return Resource.<RatingListResponse>error("Không có đánh giá", null);
                    }
                })
                .onErrorReturn(throwable -> {
                    Timber.e(throwable, "Error fetching ratings");
                    return handleError(throwable, "Lỗi khi tải đánh giá");
                });
    }

    @Override
    public Single<Resource<Rating>> getMyRating(int postId) {
        return apiService.getMyRating(postId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> {
                    if (response != null && response.getData() != null) {
                        return Resource.success(response.getData());
                    } else {
                        return Resource.<Rating>error("Bạn chưa đánh giá tin này", null);
                    }
                })
                .onErrorReturn(throwable -> {
                    Timber.e(throwable, "Error fetching my rating");
                    
                    if (throwable instanceof retrofit2.HttpException) {
                        retrofit2.HttpException httpException = (retrofit2.HttpException) throwable;
                        if (httpException.code() == 404) {
                            return Resource.error("Chưa có đánh giá", null);
                        }
                    }
                    return handleError(throwable, "Lỗi khi tải đánh giá của bạn");
                });
    }

    @Override
    public Single<Resource<Void>> deleteMyRating(int postId) {
        return apiService.deleteMyRating(postId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> Resource.<Void>success(null))
                .onErrorReturn(throwable -> {
                    Timber.e(throwable, "Error deleting rating");
                    return handleError(throwable, "Lỗi khi xóa đánh giá");
                });
    }

    @Override
    public Single<Resource<RatingStats>> getRatingStats(int postId) {
        return apiService.getRatingStats(postId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> {
                    if (response != null && response.getData() != null) {
                        return Resource.success(response.getData());
                    } else {
                        return Resource.success(new RatingStats(0, 0));  // Default stats
                    }
                })
                .onErrorReturn(throwable -> {
                    Timber.e(throwable, "Error fetching rating stats");
                    return Resource.success(new RatingStats(0, 0));  // Return default on error
                });
    }

    /**
     * Common error handler with Vietnamese messages
     */
    private <T> Resource<T> handleError(Throwable throwable, String defaultMessage) {
        String errorMessage;

        if (throwable instanceof retrofit2.HttpException) {
            retrofit2.HttpException httpException = (retrofit2.HttpException) throwable;
            int code = httpException.code();

            if (code == 400) {
                errorMessage = "Nội dung đánh giá không hợp lệ";
            } else if (code == 401) {
                errorMessage = "Vui lòng đăng nhập để đánh giá";
            } else if (code == 404) {
                errorMessage = "Tin đăng không tồn tại";
            } else {
                errorMessage = defaultMessage;
            }
        } else if (throwable instanceof java.net.UnknownHostException ||
                   throwable instanceof java.net.SocketTimeoutException) {
            errorMessage = "Không có kết nối mạng";
        } else {
            errorMessage = defaultMessage;
        }

        return Resource.error(errorMessage, null);
    }
}
