package com.trototvn.trototandroid.utils;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.trototvn.trototandroid.data.model.location.City;
import com.trototvn.trototandroid.data.model.location.District;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * LocationService - Loads and provides city/district data from JSON assets
 * Matches frontend locationService.ts logic
 */
@Singleton
public class LocationService {
    
    private final Context context;
    private final Gson gson;
    private List<City> cities;

    @Inject
    public LocationService(@ApplicationContext Context context, Gson gson) {
        this.context = context;
        this.gson = gson;
        initializeCities();
    }

    /**
     * Initialize cities from provinces.json
     * Matches frontend initializeProvinces() logic
     */
    private void initializeCities() {
        try {
            String json = loadJSONFromAsset();
            JsonObject root = gson.fromJson(json, JsonObject.class);
            JsonArray regions = root.getAsJsonArray("regions");

            List<City> cityList = new ArrayList<>();

            for (JsonElement regionElement : regions) {
                JsonObject regionObj = regionElement.getAsJsonObject();
                
                // Each region is a single object with city ID as key
                for (Map.Entry<String, JsonElement> entry : regionObj.entrySet()) {
                    String cityId = entry.getKey();
                    JsonObject cityData = entry.getValue().getAsJsonObject();
                    String cityName = cityData.get("name").getAsString();
                    JsonArray areaArray = cityData.getAsJsonArray("area");

                    // Parse districts
                    List<District> districts = new ArrayList<>();
                    for (JsonElement areaElement : areaArray) {
                        JsonObject districtObj = areaElement.getAsJsonObject();
                        
                        // Each district is {id: name}
                        for (Map.Entry<String, JsonElement> districtEntry : districtObj.entrySet()) {
                            String districtId = districtEntry.getKey();
                            String districtName = districtEntry.getValue().getAsString();
                            districts.add(new District(districtId, districtName, cityId));
                        }
                    }

                    cityList.add(new City(cityId, cityName, districts));
                }
            }

            this.cities = cityList;
        } catch (Exception e) {
            timber.log.Timber.e(e, "Error loading provinces JSON");
            this.cities = new ArrayList<>(); // Empty list on error
        }
    }

    /**
     * Load JSON from assets folder
     */
    private String loadJSONFromAsset() {
        try {
            InputStream is = context.getAssets().open("provinces.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            timber.log.Timber.e(e, "Error reading JSON file");
            return "{}";
        }
    }

    /**
     * Get all cities
     */
    public List<City> getAllCities() {
        return cities;
    }

    /**
     * Get city by ID
     */
    public City getCityById(String cityId) {
        for (City city : cities) {
            if (city.getId().equals(cityId)) {
                return city;
            }
        }
        return null;
    }

    /**
     * Get city by name
     */
    public City getCityByName(String name) {
        for (City city : cities) {
            if (city.getName().equals(name)) {
                return city;
            }
        }
        return null;
    }

    /**
     * Get districts by city ID
     */
    public List<District> getDistrictsByCityId(String cityId) {
        City city = getCityById(cityId);
        return city != null ? city.getDistricts() : new ArrayList<>();
    }

    /**
     * Get district by ID
     */
    public District getDistrictById(String districtId) {
        for (City city : cities) {
            for (District district : city.getDistricts()) {
                if (district.getId().equals(districtId)) {
                    return district;
                }
            }
        }
        return null;
    }
}
