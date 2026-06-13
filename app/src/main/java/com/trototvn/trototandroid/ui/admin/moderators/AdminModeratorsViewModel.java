package com.trototvn.trototandroid.ui.admin.moderators;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.admin.AddModeratorRequest;
import com.trototvn.trototandroid.data.model.admin.Moderator;
import com.trototvn.trototandroid.data.model.admin.ModeratorActionHistoryItem;
import com.trototvn.trototandroid.data.repository.AdminRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import timber.log.Timber;

@HiltViewModel
public class AdminModeratorsViewModel extends ViewModel {

    private final AdminRepository adminRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<Resource<List<Moderator>>> moderatorsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Resource<String>> actionLiveData = new MutableLiveData<>();
    private final MutableLiveData<Resource<Moderator>> profileLiveData = new MutableLiveData<>();
    private final MutableLiveData<Resource<List<ModeratorActionHistoryItem>>> historyLiveData = new MutableLiveData<>();

    @Inject
    public AdminModeratorsViewModel(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public LiveData<Resource<List<Moderator>>> getModeratorsLiveData() {
        return moderatorsLiveData;
    }

    public LiveData<Resource<String>> getActionLiveData() {
        return actionLiveData;
    }

    public LiveData<Resource<Moderator>> getProfileLiveData() {
        return profileLiveData;
    }

    public LiveData<Resource<List<ModeratorActionHistoryItem>>> getHistoryLiveData() {
        return historyLiveData;
    }

    public void loadModerators(String key) {
        moderatorsLiveData.setValue(Resource.loading(null));
        disposables.add(adminRepository.getModerators(key)
                .subscribe(moderatorsLiveData::setValue, err(moderatorsLiveData)));
    }

    public void createModerator(AddModeratorRequest request) {
        actionLiveData.setValue(Resource.loading(null));
        disposables.add(adminRepository.addModerator(request)
                .subscribe(actionLiveData::setValue, err(actionLiveData)));
    }

    public void toggleStatus(Moderator moderator) {
        String newStatus = moderator.isActive() ? Moderator.STATUS_INACTIVE : Moderator.STATUS_ACTIVE;
        actionLiveData.setValue(Resource.loading(null));
        disposables.add(adminRepository.updateModeratorStatus(moderator.getAdminId(), newStatus)
                .subscribe(actionLiveData::setValue, err(actionLiveData)));
    }

    public void resetPassword(int moderatorId) {
        actionLiveData.setValue(Resource.loading(null));
        disposables.add(adminRepository.resetModeratorPassword(moderatorId)
                .subscribe(actionLiveData::setValue, err(actionLiveData)));
    }

    public void loadProfile(int moderatorId) {
        profileLiveData.setValue(Resource.loading(null));
        disposables.add(adminRepository.getModeratorProfile(moderatorId)
                .subscribe(profileLiveData::setValue, err(profileLiveData)));
    }

    public void loadHistory(int moderatorId) {
        historyLiveData.setValue(Resource.loading(null));
        disposables.add(adminRepository.getModeratorHistory(moderatorId)
                .subscribe(historyLiveData::setValue, err(historyLiveData)));
    }

    private <T> io.reactivex.rxjava3.functions.Consumer<Throwable> err(MutableLiveData<Resource<T>> liveData) {
        return throwable -> {
            Timber.e(throwable, "Moderator management error");
            liveData.setValue(Resource.error(
                    throwable.getMessage() != null ? throwable.getMessage() : "Lỗi kết nối", null));
        };
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
