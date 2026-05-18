package com.trototvn.trototandroid.ui.main.subscriptions;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.profile.Subscription;
import com.trototvn.trototandroid.data.repository.ProfileRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import timber.log.Timber;

@HiltViewModel
public class SubscriptionsViewModel extends ViewModel {

    private final ProfileRepository profileRepository;
    private final CompositeDisposable disposable = new CompositeDisposable();

    private final MutableLiveData<Resource<List<Subscription>>> subscriptionsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Resource<Subscription>> addSubscriptionResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>> removeSubscriptionResult = new MutableLiveData<>();

    @Inject
    public SubscriptionsViewModel(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
        loadSubscriptions();
    }

    public LiveData<Resource<List<Subscription>>> getSubscriptions() {
        return subscriptionsLiveData;
    }

    public LiveData<Resource<Subscription>> getAddSubscriptionResult() {
        return addSubscriptionResult;
    }

    public LiveData<Resource<Void>> getRemoveSubscriptionResult() {
        return removeSubscriptionResult;
    }

    public void loadSubscriptions() {
        subscriptionsLiveData.setValue(Resource.loading(null));
        disposable.add(
                profileRepository.getSubscriptions()
                        .subscribe(
                                resource -> subscriptionsLiveData.setValue(resource),
                                error -> {
                                    Timber.e(error, "Error loading subscriptions");
                                    subscriptionsLiveData.setValue(Resource.error("Lỗi tải danh sách khu vực", null));
                                }
                        )
        );
    }

    public void addSubscription(String city, String district) {
        addSubscriptionResult.setValue(Resource.loading(null));
        disposable.add(
                profileRepository.createSubscription(city, district)
                        .subscribe(
                                resource -> {
                                    addSubscriptionResult.setValue(resource);
                                    if (resource.getStatus() == Resource.Status.SUCCESS) {
                                        loadSubscriptions(); // Reload list after success
                                    }
                                },
                                error -> {
                                    Timber.e(error, "Error adding subscription");
                                    addSubscriptionResult.setValue(Resource.error("Lỗi thêm khu vực theo dõi", null));
                                }
                        )
        );
    }

    public void removeSubscription(Subscription subscription) {
        removeSubscriptionResult.setValue(Resource.loading(null));
        disposable.add(
                profileRepository.deleteSubscription(subscription.getSubscriptionId())
                        .subscribe(
                                resource -> {
                                    removeSubscriptionResult.setValue(resource);
                                    if (resource.getStatus() == Resource.Status.SUCCESS) {
                                        loadSubscriptions(); // Reload list after success
                                    }
                                },
                                error -> {
                                    Timber.e(error, "Error deleting subscription");
                                    removeSubscriptionResult.setValue(Resource.error("Lỗi xoá khu vực theo dõi", null));
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

