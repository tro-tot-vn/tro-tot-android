package com.trototvn.trototandroid.data.model.post;

import java.util.List;

/**
 * My Post model - extends Post with moderation history
 * Matches backend ListPostRes
 */
public class MyPost {
    private int postId;
    private String status;  // APPROVED, PENDING, REJECTED, EXPOSED, HIDDEN
    private String createdAt;
    private String extendedAt;
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
    private List<MultimediaFile> multimediaFiles;
    private List<ModerationHistory> moderationHistories;

    // Getters and Setters
    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getExtendedAt() { return extendedAt; }
    public void setExtendedAt(String extendedAt) { this.extendedAt = extendedAt; }

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

    public List<MultimediaFile> getMultimediaFiles() { return multimediaFiles; }
    public void setMultimediaFiles(List<MultimediaFile> multimediaFiles) {
        this.multimediaFiles = multimediaFiles;
    }

    public List<ModerationHistory> getModerationHistories() { return moderationHistories; }
    public void setModerationHistories(List<ModerationHistory> moderationHistories) {
        this.moderationHistories = moderationHistories;
    }

    /**
     * Helper: Get first image URL
     */
    public String getFirstImageUrl() {
        if (multimediaFiles != null && !multimediaFiles.isEmpty()) {
            for (MultimediaFile mf : multimediaFiles) {
                if (mf != null && mf.getFile() != null) {
                    MultimediaFileDetail fileDetail = mf.getFile();
                    if (fileDetail.getFileType() == FileType.IMAGE) {
                        return "https://storage.googleapis.com/tro-tot-vn-storage/" + fileDetail.getFileId();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Helper: Get full address
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
}
