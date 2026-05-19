package com.trototvn.trototandroid.data.model.location;

/**
 * Ward/Commune model
 * Matches dynamic ward data loaded from `/api/location/wards/{districtId}`
 */
public class Ward {
    private String id;
    private String name;

    public Ward(String id, String name) {
        this.id = id;
        this.name = name;
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
}
