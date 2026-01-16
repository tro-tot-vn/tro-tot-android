package com.trototvn.trototandroid.data.model.search;

/**
 * Pagination info for search results (offset-based)
 */
public class SearchPagination {
    private int page;
    private int pageSize;
    private int total;
    private int totalPages;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean hasNextPage() {
        return page < totalPages;
    }
}
