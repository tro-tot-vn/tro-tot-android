package com.trototvn.trototandroid.data.repository;

import com.trototvn.trototandroid.data.model.ResponseData;
import com.trototvn.trototandroid.data.model.video.IceConfigDto;
import com.trototvn.trototandroid.data.remote.ApiService;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Single;

/**
 * VideoCallRepository - Quản lý các cuộc gọi API và dữ liệu liên quan đến Video Call.
 */
@Singleton
public class VideoCallRepository {

    private final ApiService apiService;

    @Inject
    public VideoCallRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    /**
     * Lấy cấu hình máy chủ ICE từ API
     */
    public Single<ResponseData<IceConfigDto>> getIceConfig() {
        return apiService.getIceConfig();
    }
}
