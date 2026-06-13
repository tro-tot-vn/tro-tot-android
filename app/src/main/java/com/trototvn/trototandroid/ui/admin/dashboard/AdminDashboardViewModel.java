package com.trototvn.trototandroid.ui.admin.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.admin.DashboardStats;
import com.trototvn.trototandroid.data.repository.AdminRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import timber.log.Timber;

@HiltViewModel
public class AdminDashboardViewModel extends ViewModel {

    private final AdminRepository adminRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final MutableLiveData<Resource<DashboardStats>> statsLiveData = new MutableLiveData<>();

    @Inject
    public AdminDashboardViewModel(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public LiveData<Resource<DashboardStats>> getStatsLiveData() {
        return statsLiveData;
    }

    public void loadStats() {
        statsLiveData.setValue(Resource.loading(null));
        disposables.add(adminRepository.getDashboardStats()
                .subscribe(
                        statsLiveData::setValue,
                        throwable -> {
                            Timber.e(throwable, "Error loading dashboard stats");
                            statsLiveData.setValue(Resource.error(
                                    throwable.getMessage() != null ? throwable.getMessage() : "Lỗi kết nối", null));
                        }));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
