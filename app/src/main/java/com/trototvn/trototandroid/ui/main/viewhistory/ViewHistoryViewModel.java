package com.trototvn.trototandroid.ui.main.viewhistory;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.post.Post;
import com.trototvn.trototandroid.data.repository.ProfileRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import timber.log.Timber;

/**
 * ViewModel for View History screen
 * Manages view history posts fetching and state
 */
@HiltViewModel
public class ViewHistoryViewModel extends ViewModel {

    private final ProfileRepository profileRepository;
    private final CompositeDisposable disposable = new CompositeDisposable();

    private final MutableLiveData<Resource<List<Post>>> viewHistory = new MutableLiveData<>();

    @Inject
    public ViewHistoryViewModel(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public LiveData<Resource<List<Post>>> getViewHistory() {
        return viewHistory;
    }

    public void loadViewHistory() {
        viewHistory.setValue(Resource.loading(null));

        disposable.add(
                profileRepository.getViewHistory()
                        .subscribe(
                                resource -> {
                                    viewHistory.setValue(resource);
                                    if (resource.getStatus() == Resource.Status.SUCCESS) {
                                        Timber.d("View history loaded successfully: %d posts",
                                                resource.getData() != null ? resource.getData().size() : 0);
                                    }
                                },
                                error -> {
                                    Timber.e(error, "Error loading view history");
                                    viewHistory.setValue(Resource.error("Lỗi tải lịch sử xem", null));
                                }
                        )
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}

