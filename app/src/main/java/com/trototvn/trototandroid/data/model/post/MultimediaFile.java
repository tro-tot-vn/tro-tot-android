package com.trototvn.trototandroid.data.model.post;

public class MultimediaFile {
    private int fileId;
    private Integer postId;
    private MultimediaFileDetail file;

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public MultimediaFileDetail getFile() {
        return file;
    }

    public void setFile(MultimediaFileDetail file) {
        this.file = file;
    }
}
