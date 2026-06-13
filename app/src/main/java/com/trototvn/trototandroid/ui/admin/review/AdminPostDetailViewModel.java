package com.trototvn.trototandroid.ui.admin.review;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.admin.ModeratePostRequest;
import com.trototvn.trototandroid.data.model.admin.PostModerationHistoryItem;
import com.trototvn.trototandroid.data.repository.AdminRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import timber.log.Timber;

@HiltViewModel
public class AdminPostDetailViewModel extends ViewModel {

    private final AdminRepository adminRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<Resource<List<PostModerationHistoryItem>>> historyLiveData = new MutableLiveData<>();
    private final MutableLiveData<Resource<String>> moderateLiveData = new MutableLiveData<>();

    @Inject
    public AdminPostDetailViewModel(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public LiveData<Resource<List<PostModerationHistoryItem>>> getHistoryLiveData() {
        return historyLiveData;
    }

    public LiveData<Resource<String>> getModerateLiveData() {
        return moderateLiveData;
    }

    public void loadHistory(int postId) {
        disposables.add(adminRepository.getPostHistory(postId)
                .subscribe(
                        historyLiveData::setValue,
                        throwable -> {
                            Timber.e(throwable, "Error loading post history");
                            historyLiveData.setValue(Resource.error(
                                    throwable.getMessage() != null ? throwable.getMessage() : "Lỗi kết nối", null));
                        }));
    }

    public void moderate(int postId, ModeratePostRequest request) {
        moderateLiveData.setValue(Resource.loading(null));
        disposables.add(adminRepository.moderatePost(postId, request)
                .subscribe(
                        moderateLiveData::setValue,
                        throwable -> {
                            Timber.e(throwable, "Error moderating post");
                            moderateLiveData.setValue(Resource.error(
                                    throwable.getMessage() != null ? throwable.getMessage() : "Lỗi kết nối", null));
                        }));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
