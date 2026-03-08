package com.trototvn.trototandroid.data.repository;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.post.Post;
import com.trototvn.trototandroid.data.model.post.RecommendationResponse;
import com.trototvn.trototandroid.data.model.search.SearchParams;
import com.trototvn.trototandroid.data.model.search.SearchInteractionRequest;
import com.trototvn.trototandroid.data.model.search.SearchResponse;
import com.trototvn.trototandroid.data.remote.ApiService;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Implementation of PostRepository
 * Handles data operations for posts with clean error handling
 */
@Singleton
public class PostRepositoryImpl implements PostRepository {

    private final ApiService apiService;

    @Inject
    public PostRepositoryImpl(ApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public Single<Resource<List<Post>>> getLatestPosts(int limit) {
        return apiService.getLatestPosts(limit)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> {
                    if (response.getData() != null) {
                        return Resource.success(response.getData());
                    } else {
                        return Resource.<List<Post>>error(
                                response.getMessage() != null ? response.getMessage() : "Không thể tải bài đăng",
                                null);
                    }
                })
                .onErrorReturn(throwable -> {
                    Timber.e(throwable, "Error fetching latest posts");
                    return Resource.error(
                            throwable.getMessage() != null ? throwable.getMessage() : "Lỗi kết nối",
                            null);
                });
    }

    @Override
    public Single<Resource<RecommendationResponse>> getRecommendations(int page, int pageSize, Integer logId) {
        return apiService.getRecommendations(page, pageSize, logId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> {
                    if (response.isSuccess() && response.getData() != null) {
                        return Resource.success(response);
                    } else {
                        return Resource.<RecommendationResponse>error(
                                "Không thể tải gợi ý",
                                null);
                    }
                })
                .onErrorReturn(throwable -> {
                    Timber.e(throwable, "Error fetching recommendations");
                    return Resource.error(
                            throwable.getMessage() != null ? throwable.getMessage() : "Lỗi kết nối",
                            null);
                });
    }

    @Override
    public Single<Resource<SearchResponse>> search(SearchParams params) {
        return apiService.search(
                params.getQuery(),
                params.getCity(),
                params.getDistrict(),
                params.getWard(),
                params.getPriceMin(),
                params.getPriceMax(),
                params.getAcreageMin(),
                params.getAcreageMax(),
                params.getInteriorCondition(),
                params.getPage(),
                params.getPageSize())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> {
                    if (response.isSuccess() && response.getData() != null) {
                        return Resource.success(response);
                    } else {
                        return Resource.<SearchResponse>error(
                                "Không tìm thấy kết quả",
                                null);
                    }
                })
                .onErrorReturn(throwable -> {
                    Timber.e(throwable, "Error searching");
                    return Resource.error(
                            throwable.getMessage() != null ? throwable.getMessage() : "Lỗi kết nối",
                            null);
                });
    }

    @Override
    public Single<Resource<Void>> logSearchClick(int searchLogId, int postId) {
        return apiService.logSearchClick(new SearchInteractionRequest.Click(searchLogId, postId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> Resource.<Void>success(null))
                .onErrorReturn(throwable -> {
                    Timber.e(throwable, "Error logging search click");
                    return Resource.<Void>error("Không thể ghi nhận tương tác", null);
                });
    }

    @Override
    public Single<Resource<Void>> submitSearchFeedback(int searchLogId, boolean isHelpful, String comment) {
        return apiService
                .submitSearchFeedback(new SearchInteractionRequest.Feedback(searchLogId, isHelpful, null, comment))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> Resource.<Void>success(null))
                .onErrorReturn(throwable -> {
                    Timber.e(throwable, "Error submitting search feedback");
                    return Resource.error("Không thể gửi phản hồi", null);
                });
    }
}
