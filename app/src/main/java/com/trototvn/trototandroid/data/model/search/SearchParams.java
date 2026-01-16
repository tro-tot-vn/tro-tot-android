package com.trototvn.trototandroid.data.model.search;

/**
 * Search parameters for building query
 */
public class SearchParams {
    private String query;
    private String city;
    private String district;
    private String ward;
    private Integer priceMin;
    private Integer priceMax;
    private Integer acreageMin;
    private Integer acreageMax;
    private String interiorCondition;
    private int page = 1;
    private int pageSize = 20;

    public SearchParams(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public Integer getPriceMin() {
        return priceMin;
    }

    public void setPriceMin(Integer priceMin) {
        this.priceMin = priceMin;
    }

    public Integer getPriceMax() {
        return priceMax;
    }

    public void setPriceMax(Integer priceMax) {
        this.priceMax = priceMax;
    }

    public Integer getAcreageMin() {
        return acreageMin;
    }

    public void setAcreageMin(Integer acreageMin) {
        this.acreageMin = acreageMin;
    }

    public Integer getAcreageMax() {
        return acreageMax;
    }

    public void setAcreageMax(Integer acreageMax) {
        this.acreageMax = acreageMax;
    }

    public String getInteriorCondition() {
        return interiorCondition;
    }

    public void setInteriorCondition(String interiorCondition) {
        this.interiorCondition = interiorCondition;
    }

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
}
