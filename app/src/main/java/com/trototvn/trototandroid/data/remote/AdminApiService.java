package com.trototvn.trototandroid.data.remote;

import com.trototvn.trototandroid.data.model.ResponseData;
import com.trototvn.trototandroid.data.model.auth.ChangePasswordRequest;
import com.trototvn.trototandroid.data.model.admin.AddModeratorRequest;
import com.trototvn.trototandroid.data.model.admin.AdminPost;
import com.trototvn.trototandroid.data.model.admin.DashboardStats;
import com.trototvn.trototandroid.data.model.admin.ModeratePostRequest;
import com.trototvn.trototandroid.data.model.admin.Moderator;
import com.trototvn.trototandroid.data.model.admin.ModeratorActionHistoryItem;
import com.trototvn.trototandroid.data.model.admin.PostModerationHistoryItem;
import com.trototvn.trototandroid.data.model.admin.UpdateModeratorStatusRequest;

import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Admin / moderation API endpoints.
 * Contract source of truth: tro-tot-vn-be (branch dev) src/web/controllers/admin.controller.ts.
 * All paths are mounted under /api on the backend; BASE_URL ends with '/'.
 */
public interface AdminApiService {

    // ===== Dashboard ===== (roles: Admin, Manager)
    @GET("api/admin/dashboard-stats")
    Single<ResponseData<DashboardStats>> getDashboardStats();

    // ===== Post review / moderation ===== (roles: Admin, Manager)
    @GET("api/admin/posts/pending")
    Single<ResponseData<List<AdminPost>>> getPendingPosts();

    @POST("api/admin/posts/{postId}/moderate")
    Single<ResponseData<Object>> moderatePost(
            @Path("postId") int postId,
            @Body ModeratePostRequest request);

    @GET("api/admin/posts/{postId}/history")
    Single<ResponseData<List<PostModerationHistoryItem>>> getPostHistory(@Path("postId") int postId);

    // ===== Moderator management ===== (role: Manager)
    @GET("api/admin/moderators")
    Single<ResponseData<List<Moderator>>> getModerators(@Query("key") String key);

    @POST("api/admin/moderators")
    Single<ResponseData<Object>> addModerator(@Body AddModeratorRequest request);

    @PUT("api/admin/moderators/{moderatorId}/status")
    Single<ResponseData<Object>> updateModeratorStatus(
            @Path("moderatorId") int moderatorId,
            @Body UpdateModeratorStatusRequest request);

    @PUT("api/admin/moderators/{moderatorId}/reset-password")
    Single<ResponseData<Object>> resetModeratorPassword(@Path("moderatorId") int moderatorId);

    @GET("api/admin/moderators/{moderatorId}/profile")
    Single<ResponseData<Moderator>> getModeratorProfile(@Path("moderatorId") int moderatorId);

    @GET("api/admin/moderators/{moderatorId}/history")
    Single<ResponseData<List<ModeratorActionHistoryItem>>> getModeratorHistory(@Path("moderatorId") int moderatorId);

    // ===== Self profile ===== (roles: Admin, Manager)
    @GET("api/admin/me/profile")
    Single<ResponseData<Moderator>> getMyProfile();

    @PUT("api/admin/me/profile")
    Single<ResponseData<Object>> updateMyProfile(@Body Map<String, Object> body);

    // ===== Account =====
    @POST("api/auth/change-password")
    Single<ResponseData<Object>> changePassword(@Body ChangePasswordRequest request);
}
