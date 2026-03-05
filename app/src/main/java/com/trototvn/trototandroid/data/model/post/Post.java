package com.trototvn.trototandroid.data.model.post;

import com.trototvn.trototandroid.utils.Constants;

import java.util.Date;
import java.util.List;

/**
 * Post model matching backend ListPostRes structure
 */
public class Post {
    private int postId;
    private String status;
    private Date createdAt;
    private Date extendedAt;
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
    
    // Optional fields for tracking
    private Integer searchLogItemId;
    private Integer recommendationLogItemId;

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getExtendedAt() {
        return extendedAt;
    }

    public void setExtendedAt(Date extendedAt) {
        this.extendedAt = extendedAt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getInteriorCondition() {
        return interiorCondition;
    }

    public void setInteriorCondition(String interiorCondition) {
        this.interiorCondition = interiorCondition;
    }

    public double getAcreage() {
        return acreage;
    }

    public void setAcreage(double acreage) {
        this.acreage = acreage;
    }

    public List<MultimediaFile> getMultimediaFiles() {
        return multimediaFiles;
    }

    public void setMultimediaFiles(List<MultimediaFile> multimediaFiles) {
        this.multimediaFiles = multimediaFiles;
    }

    public Integer getSearchLogItemId() {
        return searchLogItemId;
    }

    public void setSearchLogItemId(Integer searchLogItemId) {
        this.searchLogItemId = searchLogItemId;
    }

    public Integer getRecommendationLogItemId() {
        return recommendationLogItemId;
    }

    public void setRecommendationLogItemId(Integer recommendationLogItemId) {
        this.recommendationLogItemId = recommendationLogItemId;
    }

    /**
     * Get the first image URL for display
     * Matches frontend: getFileUrl(post.multimediaFiles[0].fileId)
     */
    public String getFirstImageUrl() {
        if (multimediaFiles != null && !multimediaFiles.isEmpty()) {
            MultimediaFile firstFile = multimediaFiles.get(0);
            if (firstFile != null) {
                // Use fileId directly from MultimediaFile, like frontend
                return Constants.BASE_URL +  "api/files/" + firstFile.getFileId();
            }
        }
        return null;
    }
}
