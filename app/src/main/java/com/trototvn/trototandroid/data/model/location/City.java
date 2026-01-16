package com.trototvn.trototandroid.data.model.location;

import java.util.List;

/**
 * City/Province model
 * Matches frontend Province type from location.types.ts
 */
public class City {
    private String id;
    private String name;
    private List<District> districts;

    public City(String id, String name, List<District> districts) {
        this.id = id;
        this.name = name;
        this.districts = districts;
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

    public List<District> getDistricts() {
        return districts;
    }

    public void setDistricts(List<District> districts) {
        this.districts = districts;
    }
}
