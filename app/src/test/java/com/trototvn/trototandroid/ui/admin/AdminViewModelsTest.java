package com.trototvn.trototandroid.ui.admin;

import static org.junit.Assert.assertEquals;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.trototvn.trototandroid.data.model.Resource;
import com.trototvn.trototandroid.data.model.admin.AdminPost;
import com.trototvn.trototandroid.data.model.admin.ModeratePostRequest;
import com.trototvn.trototandroid.data.model.admin.Moderator;
import com.trototvn.trototandroid.data.repository.FakeAdminRepository;
import com.trototvn.trototandroid.ui.admin.dashboard.AdminDashboardViewModel;
import com.trototvn.trototandroid.ui.admin.moderators.AdminModeratorsViewModel;
import com.trototvn.trototandroid.ui.admin.review.AdminPostDetailViewModel;
import com.trototvn.trototandroid.ui.admin.review.AdminReviewViewModel;

import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel unit tests using a fake repository (synchronous Single emissions).
 */
public class AdminViewModelsTest {

    @Rule
    public InstantTaskExecutorRule instantRule = new InstantTaskExecutorRule();

    @Test
    public void dashboard_load_emitsSuccess() {
        FakeAdminRepository repo = new FakeAdminRepository();
        AdminDashboardViewModel vm = new AdminDashboardViewModel(repo);

        vm.loadStats();

        assertEquals(Resource.Status.SUCCESS, vm.getStatsLiveData().getValue().getStatus());
    }

    @Test
    public void review_load_emitsSuccessWithData() {
        FakeAdminRepository repo = new FakeAdminRepository();
        List<AdminPost> posts = new ArrayList<>();
        posts.add(new AdminPost());
        repo.pendingResult = Resource.success(posts);
        AdminReviewViewModel vm = new AdminReviewViewModel(repo);

        vm.loadPendingPosts();

        Resource<List<AdminPost>> value = vm.getPostsLiveData().getValue();
        assertEquals(Resource.Status.SUCCESS, value.getStatus());
        assertEquals(1, value.getData().size());
    }

    @Test
    public void review_load_errorPropagates() {
        FakeAdminRepository repo = new FakeAdminRepository();
        repo.pendingResult = Resource.error("POST_NOT_FOUND", null);
        AdminReviewViewModel vm = new AdminReviewViewModel(repo);

        vm.loadPendingPosts();

        Resource<List<AdminPost>> value = vm.getPostsLiveData().getValue();
        assertEquals(Resource.Status.ERROR, value.getStatus());
        assertEquals("POST_NOT_FOUND", value.getMessage());
    }

    @Test
    public void detail_approve_forwardsActionTypeAndEmitsSuccess() {
        FakeAdminRepository repo = new FakeAdminRepository();
        AdminPostDetailViewModel vm = new AdminPostDetailViewModel(repo);

        vm.moderate(42, ModeratePostRequest.approve("looks good"));

        assertEquals(42, repo.lastModeratorId);
        assertEquals(ModeratePostRequest.ACTION_APPROVED, repo.lastModerateRequest.getActionType());
        assertEquals(Resource.Status.SUCCESS, vm.getModerateLiveData().getValue().getStatus());
    }

    @Test
    public void moderators_toggleStatus_activeBecomesInactive() {
        FakeAdminRepository repo = new FakeAdminRepository();
        AdminModeratorsViewModel vm = new AdminModeratorsViewModel(repo);

        Moderator active = buildModerator(5, Moderator.STATUS_ACTIVE);
        vm.toggleStatus(active);

        assertEquals(5, repo.lastModeratorId);
        assertEquals(Moderator.STATUS_INACTIVE, repo.lastStatus);
    }

    @Test
    public void moderators_toggleStatus_inactiveBecomesActive() {
        FakeAdminRepository repo = new FakeAdminRepository();
        AdminModeratorsViewModel vm = new AdminModeratorsViewModel(repo);

        Moderator inactive = buildModerator(6, Moderator.STATUS_INACTIVE);
        vm.toggleStatus(inactive);

        assertEquals(Moderator.STATUS_ACTIVE, repo.lastStatus);
    }

    /** Build a Moderator with a given status via Gson (fields are private, no setters). */
    private static Moderator buildModerator(int adminId, String status) {
        String json = "{\"adminId\":" + adminId + ",\"account\":{\"status\":\"" + status + "\"}}";
        return new com.google.gson.Gson().fromJson(json, Moderator.class);
    }
}
