package com.trototvn.trototandroid.ui.main.accountsettings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.repository.ProfileRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@HiltViewModel
public class AccountSettingsViewModel extends ViewModel {

    private final ProfileRepository profileRepository;
    private final CompositeDisposable disposable = new CompositeDisposable();
    private final MutableLiveData<Resource<Void>> changePasswordResult = new MutableLiveData<>();

    @Inject
    public AccountSettingsViewModel(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public LiveData<Resource<Void>> getChangePasswordResult() {
        return changePasswordResult;
    }

    public void changePassword(String currentPassword, String newPassword) {
        changePasswordResult.setValue(Resource.loading(null));
        disposable.add(
                profileRepository.changePassword(currentPassword, newPassword)
                        .subscribe(
                                changePasswordResult::setValue,
                                throwable -> changePasswordResult.setValue(Resource.error("Lỗi kết nối", null))
                        )
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}
