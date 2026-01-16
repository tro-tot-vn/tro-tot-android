package com.trototvn.trototandroid.data.model.post;

import java.util.ArrayList;
import java.util.List;

/**
 * Post Detail model - matches backend GetDetailPostResponse
 */
public class PostDetail {
    private int postId;
    private String status;
    private String createdAt;
    private String title;
    private String description;
    private double price;
    private String streetNumber;
    private String street;
    private String ward;
    private String district;
    private String city;
    private String interiorCondition;
    private double acreage;
    private String extendedAt;
    private List<MultimediaFileDetail> multimediaFiles;
    private PostOwner owner;

    // Getters and Setters
    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getStreetNumber() { return streetNumber; }
    public void setStreetNumber(String streetNumber) { this.streetNumber = streetNumber; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getWard() { return ward; }
    public void setWard(String ward) { this.ward = ward; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getInteriorCondition() { return interiorCondition; }
    public void setInteriorCondition(String interiorCondition) {
        this.interiorCondition = interiorCondition;
    }

    public double getAcreage() { return acreage; }
    public void setAcreage(double acreage) { this.acreage = acreage; }

    public String getExtendedAt() { return extendedAt; }
    public void setExtendedAt(String extendedAt) { this.extendedAt = extendedAt; }

    public List<MultimediaFileDetail> getMultimediaFiles() { return multimediaFiles; }
    public void setMultimediaFiles(List<MultimediaFileDetail> multimediaFiles) {
        this.multimediaFiles = multimediaFiles;
    }

    public PostOwner getOwner() { return owner; }
    public void setOwner(PostOwner owner) { this.owner = owner; }

    /**
     * Helper: Get full address string
     */
    public String getFullAddress() {
        StringBuilder address = new StringBuilder();
        if (streetNumber != null && !streetNumber.isEmpty()) {
            address.append(streetNumber).append(" ");
        }
        if (street != null && !street.isEmpty()) {
            address.append(street).append(", ");
        }
        if (ward != null && !ward.isEmpty()) {
            address.append(ward).append(", ");
        }
        if (district != null && !district.isEmpty()) {
            address.append(district).append(", ");
        }
        if (city != null && !city.isEmpty()) {
            address.append(city);
        }
        return address.toString();
    }

    /**
     * Helper: Get list of image URLs from multimedia files
     */
    public List<String> getImageUrls() {
        List<String> urls = new ArrayList<>();
        if (multimediaFiles != null) {
            for (MultimediaFileDetail mf : multimediaFiles) {
                if (mf != null && mf.getFileType() == FileType.IMAGE) {
                    urls.add("https://storage.googleapis.com/tro-tot-vn-storage/" + mf.getFileId());
                }
            }
        }
        return urls;
    }
}
