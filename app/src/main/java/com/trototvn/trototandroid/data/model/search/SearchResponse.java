package com.trototvn.trototandroid.data.model.search;

import com.trototvn.trototandroid.data.model.post.Post;

import java.util.List;

/**
 * Search response from vector hybrid search API
 */
public class SearchResponse {
    private boolean success;
    private Integer searchLogId;
    private List<Post> data;
    private SearchPagination pagination;
    private long searchTimeMs;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Integer getSearchLogId() {
        return searchLogId;
    }

    public void setSearchLogId(Integer searchLogId) {
        this.searchLogId = searchLogId;
    }

    public List<Post> getData() {
        return data;
    }

    public void setData(List<Post> data) {
        this.data = data;
    }

    public SearchPagination getPagination() {
        return pagination;
    }

    public void setPagination(SearchPagination pagination) {
        this.pagination = pagination;
    }

    public long getSearchTimeMs() {
        return searchTimeMs;
    }

    public void setSearchTimeMs(long searchTimeMs) {
        this.searchTimeMs = searchTimeMs;
    }
}
