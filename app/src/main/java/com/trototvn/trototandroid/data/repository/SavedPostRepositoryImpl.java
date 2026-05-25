package com.trototvn.trototandroid.data.repository;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.post.ContactLogRequest;
import com.trototvn.trototandroid.data.model.post.SavePostRequest;
import com.trototvn.trototandroid.data.remote.ApiService;

import javax.inject.Inject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Saved Post Repository implementation
 * Handles save post and interaction logging with error handling
 */
public class SavedPostRepositoryImpl implements SavedPostRepository {

    private final ApiService apiService;

    @Inject
    public SavedPostRepositoryImpl(ApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public Single<Resource<Void>> savePost(int postId) {
        return apiService.savePost(postId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> Resource.<Void>success(null))
                .onErrorReturn(throwable -> {
                    Timber.e(throwable, "Error saving post %d", postId);
                    String errorMessage;

                    if (throwable instanceof retrofit2.HttpException) {
                        retrofit2.HttpException httpException = (retrofit2.HttpException) throwable;
                        int code = httpException.code();

                        if (code == 400) {
                            errorMessage = "Tin đã có trong danh sách đã lưu";
                        } else if (code == 401) {
                            errorMessage = "Vui lòng đăng nhập để lưu tin";
                        } else if (code == 404) {
                            errorMessage = "Tin đăng không tồn tại";
                        } else {
                            errorMessage = "Lỗi khi lưu tin";
                        }
                    } else if (throwable instanceof java.net.UnknownHostException ||
                               throwable instanceof java.net.SocketTimeoutException) {
                        errorMessage = "Không có kết nối mạng";
                    } else {
                        errorMessage = "Lỗi khi lưu tin";
                    }

                    return Resource.error(errorMessage, null);
                });
    }

    @Override
    public Single<Resource<Void>> deleteSavedPost(int postId) {
        return apiService.deleteSavedPost(postId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> Resource.<Void>success(null))
                .onErrorReturn(throwable -> {
                    Timber.e(throwable, "Error deleting saved post %d", postId);
                    String errorMessage;

                    if (throwable instanceof retrofit2.HttpException) {
                        retrofit2.HttpException httpException = (retrofit2.HttpException) throwable;
                        int code = httpException.code();

                        if (code == 401) {
                            errorMessage = "Vui lòng đăng nhập";
                        } else if (code == 404) {
                            errorMessage = "Tin đăng không tồn tại";
                        } else {
                            errorMessage = "Lỗi khi bỏ lưu tin";
                        }
                    } else if (throwable instanceof java.net.UnknownHostException ||
                               throwable instanceof java.net.SocketTimeoutException) {
                        errorMessage = "Không có kết nối mạng";
                    } else {
                        errorMessage = "Lỗi khi bỏ lưu tin";
                    }

                    return Resource.error(errorMessage, null);
                });
    }

    @Override
    public Single<Resource<java.util.List<com.trototvn.trototandroid.data.model.post.Post>>> getSavedPosts() {
        return apiService.getSavedPosts()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> {
                    if (response != null && response.getData() != null) {
                        return Resource.success(response.getData());
                    } else {
                        return Resource.success((java.util.List<com.trototvn.trototandroid.data.model.post.Post>) new java.util.ArrayList<com.trototvn.trototandroid.data.model.post.Post>());
                    }
                })
                .onErrorReturn(throwable -> {
                    Timber.e(throwable, "Error fetching saved posts");
                    return Resource.error("Lỗi khi lấy danh sách tin đã lưu", null);
                });
    }

    @Override
    public Single<Resource<Boolean>> checkSavedPost(int postId) {
        return apiService.checkSavedPost(postId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> {
                    if (response != null && response.getData() != null) {
                        return Resource.success(response.getData());
                    } else {
                        return Resource.success(false);
                    }
                })
                .onErrorReturn(throwable -> {
                    Timber.e(throwable, "Error checking if post %d is saved", postId);
                    String errorMessage = "Lỗi khi kiểm tra trạng thái lưu tin";
                    if (throwable instanceof retrofit2.HttpException) {
                        errorMessage = "HTTP " + ((retrofit2.HttpException) throwable).code();
                    }
                    return Resource.error(errorMessage, false);
                });
    }

    @Override
    public Single<Resource<Void>> logContact(int postId) {
        ContactLogRequest request = new ContactLogRequest(postId);
        
        return apiService.logContact(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> Resource.<Void>success(null))
                .onErrorReturn(throwable -> {
                    // Silent fail - don't break UX
                    Timber.w(throwable, "Contact logging failed for post %d (silent)", postId);
                    return Resource.success(null);  // Return success even on error
                });
    }
}
