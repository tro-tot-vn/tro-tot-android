package com.trototvn.trototandroid.data.model.post;

public enum FileType {
    IMAGE("Image"),
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
