package com.trototvn.trototandroid.data.model.admin;

/**
 * Body for PUT api/admin/moderators/{moderatorId}/status
 * status: Active | Inactive | Blocked
 */
public class UpdateModeratorStatusRequest {
    private final String status;

    public UpdateModeratorStatusRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
