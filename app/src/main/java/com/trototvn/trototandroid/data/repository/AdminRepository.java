package com.trototvn.trototandroid.data.repository;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.admin.AddModeratorRequest;
import com.trototvn.trototandroid.data.model.admin.AdminPost;
import com.trototvn.trototandroid.data.model.admin.DashboardStats;
import com.trototvn.trototandroid.data.model.admin.ModeratePostRequest;
import com.trototvn.trototandroid.data.model.admin.Moderator;
import com.trototvn.trototandroid.data.model.admin.ModeratorActionHistoryItem;
import com.trototvn.trototandroid.data.model.admin.PostModerationHistoryItem;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

/**
 * Repository contract for the admin / moderation feature.
 * Targets the backend (dev branch) admin controller endpoints.
 */
public interface AdminRepository {

    Single<Resource<DashboardStats>> getDashboardStats();

    Single<Resource<List<AdminPost>>> getPendingPosts();

    Single<Resource<String>> moderatePost(int postId, ModeratePostRequest request);

    Single<Resource<List<PostModerationHistoryItem>>> getPostHistory(int postId);

    Single<Resource<List<Moderator>>> getModerators(String key);

    Single<Resource<String>> addModerator(AddModeratorRequest request);

    Single<Resource<String>> updateModeratorStatus(int moderatorId, String status);

    Single<Resource<String>> resetModeratorPassword(int moderatorId);

    Single<Resource<Moderator>> getModeratorProfile(int moderatorId);

    Single<Resource<List<ModeratorActionHistoryItem>>> getModeratorHistory(int moderatorId);

    Single<Resource<Moderator>> getMyProfile();

    Single<Resource<String>> updateMyProfile(java.util.Map<String, Object> body);

    Single<Resource<String>> changePassword(String oldPassword, String newPassword);
}
