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
    private String posterName;
    private String posterAvatar;
    private String location;
    private boolean isSaved;

    public PostItem(int id, String thumbnail, String title, double price, int area,
                   String posterName, String posterAvatar, String location, boolean isSaved) {
        this.id = id;
        this.thumbnail = thumbnail;
        this.title = title;
        this.price = price;
        this.area = area;
        this.posterName = posterName;
        this.posterAvatar = posterAvatar;
        this.location = location;
        this.isSaved = isSaved;
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

    public String getPosterName() {
        return posterName;
    }

    public String getPosterAvatar() {
        return posterAvatar;
    }

    public String getLocation() {
        return location;
    }

    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }
}

