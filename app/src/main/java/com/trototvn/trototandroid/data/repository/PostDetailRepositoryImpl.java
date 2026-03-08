package com.trototvn.trototandroid.data.repository;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.ResponseData;
import com.trototvn.trototandroid.data.model.post.PostDetail;
import com.trototvn.trototandroid.data.remote.ApiService;

import javax.inject.Inject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Post Detail Repository implementation
 * Handles post detail fetching with error handling
 */
public class PostDetailRepositoryImpl implements PostDetailRepository {

    private final ApiService apiService;

    @Inject
    public PostDetailRepositoryImpl(ApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public Single<Resource<PostDetail>> getPostDetail(int postId) {
        Timber.d("Fetching post detail for ID: %d", postId);
        return apiService.getPostDetail(postId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> {
                    if (response != null && response.getData() != null) {
                        Timber.d("Successfully fetched post detail for ID: %d, title: %s", postId,
                                response.getData().getTitle());
                        return Resource.success(response.getData());
                    } else {
                        Timber.w("Post detail response data is null for ID: %d. Response: %s", postId,
                                response != null ? response.getMessage() : "null");
                        return Resource.<PostDetail>error("Không tìm thấy tin đăng", null);
                    }
                })
                .onErrorReturn(throwable -> {
                    Timber.e(throwable, "Error fetching post detail for ID: %d", postId);
                    String errorMessage;

                    if (throwable instanceof retrofit2.HttpException) {
                        retrofit2.HttpException httpException = (retrofit2.HttpException) throwable;
                        int code = httpException.code();
                        Timber.e("HTTP Error Code: %d for ID: %d", code, postId);

                        if (code == 404) {
                            errorMessage = "Tin đăng không tồn tại (404)";
                        } else if (code == 401) {
                            errorMessage = "Vui lòng đăng nhập để xem tin này (401)";
                        } else if (code == 403) {
                            errorMessage = "Bạn không có quyền xem tin này (403)";
                        } else {
                            errorMessage = "Lỗi hệ thống (" + code + "). Vui lòng thử lại";
                        }
                    } else if (throwable instanceof java.net.UnknownHostException ||
                            throwable instanceof java.net.SocketTimeoutException) {
                        errorMessage = "Không có kết nối mạng (Network Error)";
                    } else {
                        errorMessage = "Lỗi không xác định: " + throwable.getMessage();
                    }

                    return Resource.error(errorMessage, null);
                });
    }
}
