package com.trototvn.trototandroid.data.repository;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.ResponseData;
import com.trototvn.trototandroid.data.model.admin.AddModeratorRequest;
import com.trototvn.trototandroid.data.model.admin.AdminPost;
import com.trototvn.trototandroid.data.model.admin.DashboardStats;
import com.trototvn.trototandroid.data.model.admin.ModeratePostRequest;
import com.trototvn.trototandroid.data.model.admin.Moderator;
import com.trototvn.trototandroid.data.model.admin.ModeratorActionHistoryItem;
import com.trototvn.trototandroid.data.model.admin.PostModerationHistoryItem;
import com.trototvn.trototandroid.data.model.admin.UpdateModeratorStatusRequest;
import com.trototvn.trototandroid.data.remote.AdminApiService;
import com.trototvn.trototandroid.data.remote.ApiErrorParser;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Implementation of AdminRepository. Maps the backend ResponseData envelope into Resource<T>,
 * mirroring the conventions in PostRepositoryImpl (IO scheduling + onErrorReturn).
 */
@Singleton
public class AdminRepositoryImpl implements AdminRepository {

    private final AdminApiService api;

    @Inject
    public AdminRepositoryImpl(AdminApiService api) {
        this.api = api;
    }

    @Override
    public Single<Resource<DashboardStats>> getDashboardStats() {
        return mapData(api.getDashboardStats(), "Không thể tải thống kê");
    }

    @Override
    public Single<Resource<List<AdminPost>>> getPendingPosts() {
        return mapData(api.getPendingPosts(), "Không thể tải tin chờ duyệt");
    }

    @Override
    public Single<Resource<String>> moderatePost(int postId, ModeratePostRequest request) {
        return mapAction(api.moderatePost(postId, request), "Kiểm duyệt thất bại");
    }

    @Override
    public Single<Resource<List<PostModerationHistoryItem>>> getPostHistory(int postId) {
        return mapData(api.getPostHistory(postId), "Không thể tải lịch sử duyệt");
    }

    @Override
    public Single<Resource<List<Moderator>>> getModerators(String key) {
        return mapData(api.getModerators(key), "Không thể tải danh sách kiểm duyệt viên");
    }

    @Override
    public Single<Resource<String>> addModerator(AddModeratorRequest request) {
        return mapAction(api.addModerator(request), "Thêm kiểm duyệt viên thất bại");
    }

    @Override
    public Single<Resource<String>> updateModeratorStatus(int moderatorId, String status) {
        return mapAction(api.updateModeratorStatus(moderatorId, new UpdateModeratorStatusRequest(status)),
                "Cập nhật trạng thái thất bại");
    }

    @Override
    public Single<Resource<String>> resetModeratorPassword(int moderatorId) {
        return mapAction(api.resetModeratorPassword(moderatorId), "Đặt lại mật khẩu thất bại");
    }

    @Override
    public Single<Resource<Moderator>> getModeratorProfile(int moderatorId) {
        return mapData(api.getModeratorProfile(moderatorId), "Không thể tải hồ sơ");
    }

    @Override
    public Single<Resource<List<ModeratorActionHistoryItem>>> getModeratorHistory(int moderatorId) {
        return mapData(api.getModeratorHistory(moderatorId), "Không thể tải lịch sử");
    }

    @Override
    public Single<Resource<Moderator>> getMyProfile() {
        return mapData(api.getMyProfile(), "Không thể tải hồ sơ");
    }

    @Override
    public Single<Resource<String>> updateMyProfile(Map<String, Object> body) {
        return mapAction(api.updateMyProfile(body), "Cập nhật hồ sơ thất bại");
    }

    @Override
    public Single<Resource<String>> changePassword(String oldPassword, String newPassword) {
        return mapAction(
                api.changePassword(new com.trototvn.trototandroid.data.model.auth.ChangePasswordRequest(
                        oldPassword, newPassword)),
                "Đổi mật khẩu thất bại");
    }

    /**
     * Map a data-bearing endpoint: success (status 200) -> Resource.success(data); else Resource.error.
     */
    private <T> Single<Resource<T>> mapData(Single<ResponseData<T>> call, String defaultError) {
        return call
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> {
                    if (response.getStatus() == 200) {
                        return Resource.success(response.getData());
                    }
                    return Resource.<T>error(ApiErrorParser.translateCode(response.getMessage(), defaultError), null);
                })
                .onErrorReturn(throwable -> {
                    Timber.e(throwable, "Admin request failed");
                    return Resource.error(ApiErrorParser.message(throwable), null);
                });
    }

    /**
     * Map an action endpoint (returns a message/any): success -> Resource.success(message).
     */
    private Single<Resource<String>> mapAction(Single<ResponseData<Object>> call, String defaultError) {
        return call
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> {
                    if (response.getStatus() == 200) {
                        String msg = response.getMessage();
                        if ((msg == null || msg.isEmpty()) && response.getData() != null) {
                            msg = String.valueOf(response.getData());
                        }
                        return Resource.success(msg != null ? msg : "");
                    }
                    return Resource.<String>error(ApiErrorParser.translateCode(response.getMessage(), defaultError), null);
                })
                .onErrorReturn(throwable -> {
                    Timber.e(throwable, "Admin action failed");
                    return Resource.error(ApiErrorParser.message(throwable), null);
                });
    }
}
