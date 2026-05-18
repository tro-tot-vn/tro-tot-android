package com.trototvn.trototandroid.data.model.location;

import java.util.List;

/**
 * WardListResponse - Response mapping for dynamic location wards lookup API
 */
public class WardListResponse {
    private String code;
    private List<Ward> wards;

    public WardListResponse(String code, List<Ward> wards) {
        this.code = code;
        this.wards = wards;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<Ward> getWards() {
        return wards;
    }

    public void setWards(List<Ward> wards) {
        this.wards = wards;
    }
}
