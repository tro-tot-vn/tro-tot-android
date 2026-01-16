package com.trototvn.trototandroid.data.model.location;

/**
 * District model
 * Matches frontend District type from location.types.ts
 */
public class District {
    private String id;
    private String name;
    private String cityId;

    public District(String id, String name, String cityId) {
        this.id = id;
        this.name = name;
        this.cityId = cityId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }
}
