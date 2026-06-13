package com.trototvn.trototandroid.ui.admin.review;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.admin.AdminPost;
import com.trototvn.trototandroid.data.repository.AdminRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import timber.log.Timber;

@HiltViewModel
public class AdminReviewViewModel extends ViewModel {

    private final AdminRepository adminRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final MutableLiveData<Resource<List<AdminPost>>> postsLiveData = new MutableLiveData<>();

    @Inject
    public AdminReviewViewModel(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public LiveData<Resource<List<AdminPost>>> getPostsLiveData() {
        return postsLiveData;
    }

    public void loadPendingPosts() {
        postsLiveData.setValue(Resource.loading(null));
        disposables.add(adminRepository.getPendingPosts()
                .subscribe(
                        postsLiveData::setValue,
                        throwable -> {
                            Timber.e(throwable, "Error loading pending posts");
                            postsLiveData.setValue(Resource.error(
                                    throwable.getMessage() != null ? throwable.getMessage() : "Lỗi kết nối", null));
                        }));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
