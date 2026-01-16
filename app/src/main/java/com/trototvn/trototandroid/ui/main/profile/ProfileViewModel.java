package com.trototvn.trototandroid.ui.main.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.profile.CustomerProfile;
import com.trototvn.trototandroid.data.repository.ProfileRepository;
import com.trototvn.trototandroid.utils.SessionManager;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import timber.log.Timber;

/**
 * ViewModel for Profile screen
 * Manages profile data and menu state
 */
@HiltViewModel
public class ProfileViewModel extends ViewModel {

    private final ProfileRepository profileRepository;
    private final SessionManager sessionManager;
    private final CompositeDisposable disposable = new CompositeDisposable();

    private final MutableLiveData<Resource<CustomerProfile>> profile = new MutableLiveData<>();
    private final MutableLiveData<Integer> savedPostsCount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> historyCount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> subscriptionsCount = new MutableLiveData<>(0);

    @Inject
    public ProfileViewModel(ProfileRepository profileRepository, SessionManager sessionManager) {
        this.profileRepository = profileRepository;
        this.sessionManager = sessionManager;
    }

    // ========== Getters ==========

    public LiveData<Resource<CustomerProfile>> getProfile() {
        return profile;
    }

    public LiveData<Integer> getSavedPostsCount() {
        return savedPostsCount;
    }

    public LiveData<Integer> getHistoryCount() {
        return historyCount;
    }

    public LiveData<Integer> getSubscriptionsCount() {
        return subscriptionsCount;
    }

    // ========== Actions ==========

    /**
     * Load user profile
     */
    public void loadProfile() {
        profile.setValue(Resource.loading(null));

        disposable.add(
                profileRepository.getMyProfile()
                        .subscribe(
                                resource -> {
                                    profile.setValue(resource);
                                    if (resource.getStatus() == Resource.Status.SUCCESS) {
                                        Timber.d("Profile loaded successfully");
                                    }
                                },
                                error -> {
                                    Timber.e(error, "Error loading profile");
                                    profile.setValue(Resource.error("Lỗi tải hồ sơ", null));
                                }
                        )
        );
    }

    /**
     * Load menu item counts
     */
    public void loadCounts() {
        // Saved posts count
        disposable.add(
                profileRepository.getSavedPosts()
                        .subscribe(
                                resource -> {
                                    if (resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                                        savedPostsCount.setValue(resource.getData().size());
                                    }
                                },
                                error -> Timber.e(error, "Error loading saved posts count")
                        )
        );

        // View history count
        disposable.add(
                profileRepository.getViewHistory()
                        .subscribe(
                                resource -> {
                                    if (resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                                        historyCount.setValue(resource.getData().size());
                                    }
                                },
                                error -> Timber.e(error, "Error loading history count")
                        )
        );

        // Subscriptions count
        disposable.add(
                profileRepository.getSubscriptions()
                        .subscribe(
                                resource -> {
                                    if (resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                                        subscriptionsCount.setValue(resource.getData().size());
                                    }
                                },
                                error -> Timber.e(error, "Error loading subscriptions count")
                        )
        );
    }

    /**
     * Logout user
     */
    public void logout() {
        sessionManager.clearSession();
    }

    /**
     * Calculate "member since" duration
     * @param joinedAt Joined date
     * @return Formatted duration string
     */
    public String getMemberSinceDuration(java.util.Date joinedAt) {
        if (joinedAt == null) return "";

        long now = System.currentTimeMillis();
        long diff = now - joinedAt.getTime();

        long days = TimeUnit.MILLISECONDS.toDays(diff);
        if (days < 30) {
            return days + " ngày";
        }

        long months = days / 30;
        if (months < 12) {
            return months + " tháng";
        }

        long years = months / 12;
        return years + " năm";
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}
