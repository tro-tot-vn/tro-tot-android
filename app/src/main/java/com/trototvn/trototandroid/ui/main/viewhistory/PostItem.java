package com.trototvn.trototandroid.ui.main.viewhistory;

/**
 * Model for a post item (used in View History, Saved Posts, etc.)
 */
public class PostItem {
    private int id;
    private String thumbnail;
    private String title;
    private double price;
    private int area; // in m2
    private String location;

    public PostItem(int id, String thumbnail, String title, double price, int area,
                   String location) {
        this.id = id;
        this.thumbnail = thumbnail;
        this.title = title;
        this.price = price;
        this.area = area;
        this.location = location;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getTitle() {
        return title;
    }

    public double getPrice() {
        return price;
    }

    public int getArea() {
        return area;
    }

    public String getLocation() {
        return location;
    }
}

