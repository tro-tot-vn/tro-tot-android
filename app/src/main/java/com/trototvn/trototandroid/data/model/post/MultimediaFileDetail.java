package com.trototvn.trototandroid.data.model.post;

import java.util.Date;

public class MultimediaFileDetail {
    private int fileId;
    private String fileCloudId;
    private FileType fileType;
    private Date createdAt;

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public String getFileCloudId() {
        return fileCloudId;
    }

    public void setFileCloudId(String fileCloudId) {
        this.fileCloudId = fileCloudId;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
