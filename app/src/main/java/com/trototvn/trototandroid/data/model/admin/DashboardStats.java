package com.trototvn.trototandroid.data.model.admin;

/**
 * Response for GET api/admin/dashboard-stats
 */
public class DashboardStats {
    private int totalPendingPost;
    private int totalRejectedPostInWeek;
    private int totalApprovedPostInWeek;

    public int getTotalPendingPost() {
        return totalPendingPost;
    }

    public int getTotalRejectedPostInWeek() {
        return totalRejectedPostInWeek;
    }

    public int getTotalApprovedPostInWeek() {
        return totalApprovedPostInWeek;
    }
}
