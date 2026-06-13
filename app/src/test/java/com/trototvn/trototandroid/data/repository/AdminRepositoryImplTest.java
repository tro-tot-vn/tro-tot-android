package com.trototvn.trototandroid.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
import com.trototvn.trototandroid.data.model.auth.ChangePasswordRequest;
import com.trototvn.trototandroid.data.remote.AdminApiService;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Unit tests for AdminRepositoryImpl's ResponseData -> Resource mapping.
 * RxAndroid's mainThread() scheduler is replaced with a trampoline so it runs on the JVM.
 */
public class AdminRepositoryImplTest {

    @BeforeClass
    public static void setUpClass() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(scheduler -> Schedulers.trampoline());
    }

    @AfterClass
    public static void tearDownClass() {
        RxAndroidPlugins.reset();
    }

    private static <T> ResponseData<T> response(int status, String message, T data) {
        ResponseData<T> r = new ResponseData<>();
        r.setStatus(status);
        r.setMessage(message);
        r.setData(data);
        return r;
    }

    @Test
    public void getPendingPosts_success_mapsToResourceSuccess() {
        List<AdminPost> posts = new ArrayList<>();
        FakeAdminApiService api = new FakeAdminApiService();
        api.pending = response(200, "", posts);

        Resource<List<AdminPost>> result = new AdminRepositoryImpl(api).getPendingPosts().blockingGet();

        assertEquals(Resource.Status.SUCCESS, result.getStatus());
        assertEquals(posts, result.getData());
    }

    @Test
    public void getDashboardStats_nonSuccessStatus_translatesCodeToVietnamese() {
        // Defensive branch: 2xx HTTP but body.status != 200 -> translateCode maps the known code.
        FakeAdminApiService api = new FakeAdminApiService();
        api.dashboard = response(500, "INTERNAL_SERVER_ERROR", null);

        Resource<DashboardStats> result = new AdminRepositoryImpl(api).getDashboardStats().blockingGet();

        assertEquals(Resource.Status.ERROR, result.getStatus());
        assertEquals("Lỗi máy chủ, vui lòng thử lại sau", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    public void getDashboardStats_unknownCode_fallsBackToDefaultMessage() {
        FakeAdminApiService api = new FakeAdminApiService();
        api.dashboard = response(500, "SOME_UNMAPPED_CODE", null);

        Resource<DashboardStats> result = new AdminRepositoryImpl(api).getDashboardStats().blockingGet();

        assertEquals(Resource.Status.ERROR, result.getStatus());
        assertEquals("Không thể tải thống kê", result.getMessage());
    }

    @Test
    public void moderatePost_actionSuccess_returnsMessage() {
        FakeAdminApiService api = new FakeAdminApiService();
        api.moderate = response(200, "Moderated successfully", null);

        Resource<String> result = new AdminRepositoryImpl(api)
                .moderatePost(7, ModeratePostRequest.approve("")).blockingGet();

        assertEquals(Resource.Status.SUCCESS, result.getStatus());
        assertEquals("Moderated successfully", result.getData());
    }

    @Test
    public void addModerator_conflict_surfacesBackendMessage() {
        FakeAdminApiService api = new FakeAdminApiService();
        api.addModerator = response(400, "EMAIL_ALREADY_EXISTS", null);

        Resource<String> result = new AdminRepositoryImpl(api)
                .addModerator(new AddModeratorRequest("A", "B", "a@b.com", "0900000000", "Male", "1990-01-01"))
                .blockingGet();

        assertEquals(Resource.Status.ERROR, result.getStatus());
        assertEquals("Email đã được sử dụng", result.getMessage());
    }

    @Test
    public void apiThrows_mapsToResourceErrorNotCrash() {
        FakeAdminApiService api = new FakeAdminApiService();
        api.dashboardError = new RuntimeException("boom");

        Resource<DashboardStats> result = new AdminRepositoryImpl(api).getDashboardStats().blockingGet();

        assertEquals(Resource.Status.ERROR, result.getStatus());
        assertTrue(result.getMessage() != null);
    }

    /** Minimal configurable fake of AdminApiService. */
    private static class FakeAdminApiService implements AdminApiService {
        ResponseData<DashboardStats> dashboard = response(200, "", new DashboardStats());
        RuntimeException dashboardError;
        ResponseData<List<AdminPost>> pending = response(200, "", new ArrayList<>());
        ResponseData<Object> moderate = response(200, "", null);
        ResponseData<Object> addModerator = response(200, "", null);

        @Override
        public Single<ResponseData<DashboardStats>> getDashboardStats() {
            if (dashboardError != null) return Single.error(dashboardError);
            return Single.just(dashboard);
        }

        @Override
        public Single<ResponseData<List<AdminPost>>> getPendingPosts() {
            return Single.just(pending);
        }

        @Override
        public Single<ResponseData<Object>> moderatePost(int postId, ModeratePostRequest request) {
            return Single.just(moderate);
        }

        @Override
        public Single<ResponseData<List<PostModerationHistoryItem>>> getPostHistory(int postId) {
            return Single.just(response(200, "", new ArrayList<>()));
        }

        @Override
        public Single<ResponseData<List<Moderator>>> getModerators(String key) {
            return Single.just(response(200, "", new ArrayList<>()));
        }

        @Override
        public Single<ResponseData<Object>> addModerator(AddModeratorRequest request) {
            return Single.just(addModerator);
        }

        @Override
        public Single<ResponseData<Object>> updateModeratorStatus(int moderatorId, UpdateModeratorStatusRequest request) {
            return Single.just(response(200, "", null));
        }

        @Override
        public Single<ResponseData<Object>> resetModeratorPassword(int moderatorId) {
            return Single.just(response(200, "", null));
        }

        @Override
        public Single<ResponseData<Moderator>> getModeratorProfile(int moderatorId) {
            return Single.just(response(200, "", new Moderator()));
        }

        @Override
        public Single<ResponseData<List<ModeratorActionHistoryItem>>> getModeratorHistory(int moderatorId) {
            return Single.just(response(200, "", new ArrayList<>()));
        }

        @Override
        public Single<ResponseData<Moderator>> getMyProfile() {
            return Single.just(response(200, "", new Moderator()));
        }

        @Override
        public Single<ResponseData<Object>> updateMyProfile(Map<String, Object> body) {
            return Single.just(response(200, "", null));
        }

        @Override
        public Single<ResponseData<Object>> changePassword(ChangePasswordRequest request) {
            return Single.just(response(200, "", null));
        }
    }
}
