package com.trototvn.trototandroid.data.model.post;

import com.google.gson.annotations.SerializedName;

public enum FileType {
    @SerializedName(value = "IMAGE", alternate = {"Image", "image"})
    IMAGE("Image"),

    @SerializedName(value = "VIDEO", alternate = {"Video", "video"})
    VIDEO("Video");

    private final String value;

    FileType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static FileType fromString(String value) {
        for (FileType type : FileType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return IMAGE; // Default fallback
    }
}
