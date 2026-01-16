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
        SavePostRequest request = new SavePostRequest(postId);
        
        return apiService.savePost(request)
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
