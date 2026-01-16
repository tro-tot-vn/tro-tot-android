package com.trototvn.trototandroid.data.model.post;

/**
 * Post Status enum matching backend
 */
public enum PostStatus {
    APPROVED,
    PENDING,
    REJECTED,
    EXPIRED,
    HIDDEN;

    @Override
    public String toString() {
        return name();
    }

    /**
     * Convert to backend string format
     */
    public String toBackendString() {
        return name();
    }
}
