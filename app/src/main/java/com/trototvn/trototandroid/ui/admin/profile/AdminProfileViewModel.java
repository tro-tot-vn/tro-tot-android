package com.trototvn.trototandroid.ui.admin.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.admin.Moderator;
import com.trototvn.trototandroid.data.repository.AdminRepository;

import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import timber.log.Timber;

@HiltViewModel
public class AdminProfileViewModel extends ViewModel {

    private final AdminRepository adminRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<Resource<Moderator>> profileLiveData = new MutableLiveData<>();
    private final MutableLiveData<Resource<String>> updateLiveData = new MutableLiveData<>();
    private final MutableLiveData<Resource<String>> passwordLiveData = new MutableLiveData<>();

    @Inject
    public AdminProfileViewModel(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public LiveData<Resource<Moderator>> getProfileLiveData() {
        return profileLiveData;
    }

    public LiveData<Resource<String>> getUpdateLiveData() {
        return updateLiveData;
    }

    public LiveData<Resource<String>> getPasswordLiveData() {
        return passwordLiveData;
    }

    public void loadProfile() {
        profileLiveData.setValue(Resource.loading(null));
        disposables.add(adminRepository.getMyProfile()
                .subscribe(profileLiveData::setValue, err(profileLiveData)));
    }

    public void updateProfile(Map<String, Object> body) {
        updateLiveData.setValue(Resource.loading(null));
        disposables.add(adminRepository.updateMyProfile(body)
                .subscribe(updateLiveData::setValue, err(updateLiveData)));
    }

    public void changePassword(String oldPassword, String newPassword) {
        passwordLiveData.setValue(Resource.loading(null));
        disposables.add(adminRepository.changePassword(oldPassword, newPassword)
                .subscribe(passwordLiveData::setValue, err(passwordLiveData)));
    }

    private <T> io.reactivex.rxjava3.functions.Consumer<Throwable> err(MutableLiveData<Resource<T>> liveData) {
        return throwable -> {
            Timber.e(throwable, "Admin profile error");
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
