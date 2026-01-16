package com.trototvn.trototandroid.data.model.post;

import java.util.List;

/**
 * Recommendation response from backend
 */
public class RecommendationResponse {
    private boolean success;
    private Integer recommendationLogId;
    private List<Post> data;
    private PaginationResponse pagination;
    private long processingTimeMs;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Integer getRecommendationLogId() {
        return recommendationLogId;
    }

    public void setRecommendationLogId(Integer recommendationLogId) {
        this.recommendationLogId = recommendationLogId;
    }

    public List<Post> getData() {
        return data;
    }

    public void setData(List<Post> data) {
        this.data = data;
    }

    public PaginationResponse getPagination() {
        return pagination;
    }

    public void setPagination(PaginationResponse pagination) {
        this.pagination = pagination;
    }

    public long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }
}
