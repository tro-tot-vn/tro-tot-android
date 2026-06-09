package com.trototvn.trototandroid.ui.videocall;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.video.IceConfigDto;
import com.trototvn.trototandroid.data.repository.VideoCallRepository;
import com.trototvn.trototandroid.ui.base.BaseViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

/**
 * VideoCallViewModel - Lớp quản lý trạng thái dữ liệu và gọi API lấy cấu hình ICE Servers
 */
@HiltViewModel
public class VideoCallViewModel extends BaseViewModel {

    private final VideoCallRepository videoCallRepository;
    private final MutableLiveData<Resource<IceConfigDto>> iceConfig = new MutableLiveData<>();

    @Inject
    public VideoCallViewModel(VideoCallRepository videoCallRepository) {
        this.videoCallRepository = videoCallRepository;
    }

    public LiveData<Resource<IceConfigDto>> getIceConfigLiveData() {
        return iceConfig;
    }

    /**
     * Lấy cấu hình ICE Servers từ Backend
     */
    public void fetchIceConfig() {
        handleLoading(iceConfig);
        addDisposable(
                videoCallRepository.getIceConfig()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    if (response != null && response.getData() != null) {
                                        handleSuccess(iceConfig, response.getData());
                                    } else {
                                        handleError(iceConfig, "Không nhận được cấu hình ICE từ máy chủ");
                                    }
                                },
                                error -> handleError(iceConfig, error.getMessage() != null ? error.getMessage() : "Lỗi tải cấu hình ICE")
                        )
        );
    }
}
