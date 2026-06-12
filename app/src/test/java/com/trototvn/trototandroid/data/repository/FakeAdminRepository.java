package com.trototvn.trototandroid.data.repository;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.admin.AddModeratorRequest;
import com.trototvn.trototandroid.data.model.admin.AdminPost;
import com.trototvn.trototandroid.data.model.admin.DashboardStats;
import com.trototvn.trototandroid.data.model.admin.ModeratePostRequest;
import com.trototvn.trototandroid.data.model.admin.Moderator;
import com.trototvn.trototandroid.data.model.admin.ModeratorActionHistoryItem;
import com.trototvn.trototandroid.data.model.admin.PostModerationHistoryItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Single;

/**
 * Configurable fake AdminRepository for ViewModel unit tests.
 * Emits synchronously via Single.just so LiveData updates on the test thread
 * (use with InstantTaskExecutorRule).
 */
public class FakeAdminRepository implements AdminRepository {

    public Resource<DashboardStats> dashboardResult = Resource.success(new DashboardStats());
    public Resource<List<AdminPost>> pendingResult = Resource.success(new ArrayList<>());
    public Resource<String> moderateResult = Resource.success("ok");
    public Resource<List<PostModerationHistoryItem>> postHistoryResult = Resource.success(new ArrayList<>());
    public Resource<List<Moderator>> moderatorsResult = Resource.success(new ArrayList<>());
    public Resource<String> actionResult = Resource.success("ok");
    public Resource<Moderator> profileResult = Resource.success(new Moderator());
    public Resource<List<ModeratorActionHistoryItem>> moderatorHistoryResult = Resource.success(new ArrayList<>());
    public Resource<Moderator> myProfileResult = Resource.success(new Moderator());

    // Recorded inputs
    public int lastModeratorId = -1;
    public String lastStatus = null;
    public ModeratePostRequest lastModerateRequest = null;
    public Map<String, Object> lastProfileUpdate = null;

    @Override
    public Single<Resource<DashboardStats>> getDashboardStats() {
        return Single.just(dashboardResult);
    }

    @Override
    public Single<Resource<List<AdminPost>>> getPendingPosts() {
        return Single.just(pendingResult);
    }

    @Override
    public Single<Resource<String>> moderatePost(int postId, ModeratePostRequest request) {
        lastModeratorId = postId;
        lastModerateRequest = request;
        return Single.just(moderateResult);
    }

    @Override
    public Single<Resource<List<PostModerationHistoryItem>>> getPostHistory(int postId) {
        return Single.just(postHistoryResult);
    }

    @Override
    public Single<Resource<List<Moderator>>> getModerators(String key) {
        return Single.just(moderatorsResult);
    }

    @Override
    public Single<Resource<String>> addModerator(AddModeratorRequest request) {
        return Single.just(actionResult);
    }

    @Override
    public Single<Resource<String>> updateModeratorStatus(int moderatorId, String status) {
        lastModeratorId = moderatorId;
        lastStatus = status;
        return Single.just(actionResult);
    }

    @Override
    public Single<Resource<String>> resetModeratorPassword(int moderatorId) {
        lastModeratorId = moderatorId;
        return Single.just(actionResult);
    }

    @Override
    public Single<Resource<Moderator>> getModeratorProfile(int moderatorId) {
        lastModeratorId = moderatorId;
        return Single.just(profileResult);
    }

    @Override
    public Single<Resource<List<ModeratorActionHistoryItem>>> getModeratorHistory(int moderatorId) {
        lastModeratorId = moderatorId;
        return Single.just(moderatorHistoryResult);
    }

    @Override
    public Single<Resource<Moderator>> getMyProfile() {
        return Single.just(myProfileResult);
    }

    @Override
    public Single<Resource<String>> updateMyProfile(Map<String, Object> body) {
        lastProfileUpdate = body;
        return Single.just(actionResult);
    }

    @Override
    public Single<Resource<String>> changePassword(String oldPassword, String newPassword) {
        return Single.just(actionResult);
    }
}
