package com.trototvn.trototandroid.ui.postdetail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.post.PostDetail;
import com.trototvn.trototandroid.data.repository.PostDetailRepository;
import com.trototvn.trototandroid.data.local.SessionManager;
import com.trototvn.trototandroid.ui.base.BaseViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

/**
 * PostDetailViewModel - manages post detail screen state
 * Clean architecture MVVM pattern
 */
@HiltViewModel
public class PostDetailViewModel extends BaseViewModel {

    private final PostDetailRepository repository;
    private final SessionManager sessionManager;

    private final MutableLiveData<Resource<PostDetail>> postDetail = new MutableLiveData<>();

    @Inject
    public PostDetailViewModel(PostDetailRepository repository, SessionManager sessionManager) {
        this.repository = repository;
        this.sessionManager = sessionManager;
    }

    public LiveData<Resource<PostDetail>> getPostDetail() {
        return postDetail;
    }

    /**
     * Load post detail by ID
     */
    public void loadPostDetail(int postId) {
        postDetail.setValue(Resource.loading(null));

        compositeDisposable.add(
                repository.getPostDetail(postId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                postDetail::setValue,
                                throwable -> {
                                    Timber.e(throwable, "Error loading post detail in ViewModel");
                                    postDetail.setValue(Resource.error("Lỗi khi tải tin đăng", null));
                                }
                        )
        );
    }

    /**
     * Check if user is authenticated
     */
    public boolean isAuthenticated() {
        return sessionManager.getAccessToken() != null && !sessionManager.getAccessToken().isEmpty();
    }

    /**
     * Get masked phone number for non-authenticated users
     * Example: 0987654321 -> 0987***
     */
    public String getMaskedPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return phone;
        }
        return phone.substring(0, phone.length() - 3) + "***";
    }
}
