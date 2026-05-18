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
    public Single<Resource<Void>> logSearchClick(int searchLogId, int searchLogItemId) {
        return apiService.logSearchClick(new SearchInteractionRequest.Click(searchLogId, searchLogItemId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> Resource.<Void>success(null))
                .onErrorReturn(throwable -> {
                    Timber.e(throwable, "Error logging search click");
                    return Resource.<Void>error("Không thể ghi nhận tương tác", null);
                });
    }

    @Override
    public Single<Resource<Void>> submitSearchFeedback(int searchLogId, boolean isHelpful, java.util.List<String> issues, String comment) {
        return apiService
                .submitSearchFeedback(new SearchInteractionRequest.Feedback(searchLogId, isHelpful, issues, comment))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> Resource.<Void>success(null))
                .onErrorReturn(throwable -> {
                    Timber.e(throwable, "Error submitting search feedback");
                    return Resource.error("Không thể gửi phản hồi", null);
                });
    }

    @Override
    public Single<Resource<com.trototvn.trototandroid.data.model.post.MyPostsResponse>> getMyPosts(String status, Integer cursor, int limit) {
        return apiService.getMyPosts(status, cursor, limit)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> {
                    if (response.getStatus() == 200 && response.getData() != null) {
                        return Resource.success(response.getData());
                    } else {
                        return Resource.<com.trototvn.trototandroid.data.model.post.MyPostsResponse>error(
                                response.getMessage() != null ? response.getMessage() : "Không thể tải danh sách bài đăng",
                                null);
                    }
                })
                .onErrorReturn(throwable -> {
                    Timber.e(throwable, "Error fetching my posts");
                    return Resource.error(
                            throwable.getMessage() != null ? throwable.getMessage() : "Lỗi kết nối",
                            null);
                });
    }

    @Override
    public Single<Resource<Void>> hidePost(int postId) {
        return apiService.hidePost(new com.trototvn.trototandroid.data.model.post.HidePostRequest(postId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> {
                    if (response.getStatus() == 200) {
                        return Resource.<Void>success(null);
                    } else {
                        return Resource.<Void>error(
                                response.getMessage() != null ? response.getMessage() : "Không thể ẩn bài viết",
                                null);
                    }
                })
                .onErrorReturn(throwable -> {
                    Timber.e(throwable, "Error hiding post");
                    return Resource.<Void>error(
                            throwable.getMessage() != null ? throwable.getMessage() : "Lỗi kết nối",
                            null);
                });
    }

    @Override
    public Single<Resource<Void>> unhidePost(int postId) {
        return apiService.unhidePost(new com.trototvn.trototandroid.data.model.post.HidePostRequest(postId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> {
                    if (response.getStatus() == 200) {
                        return Resource.<Void>success(null);
                    } else {
                        return Resource.<Void>error(
                                response.getMessage() != null ? response.getMessage() : "Không thể hiện bài viết",
                                null);
                    }
                })
                .onErrorReturn(throwable -> {
                    Timber.e(throwable, "Error unhiding post");
                    return Resource.<Void>error(
                            throwable.getMessage() != null ? throwable.getMessage() : "Lỗi kết nối",
                            null);
                });
    }
}
