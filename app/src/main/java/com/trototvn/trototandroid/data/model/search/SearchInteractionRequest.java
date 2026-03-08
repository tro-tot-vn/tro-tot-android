package com.trototvn.trototandroid.data.model.search;

import com.google.gson.annotations.SerializedName;

/**
 * Models for search interaction tracking (Clicks and Feedback)
 */
public class SearchInteractionRequest {

    /**
     * Request for logging a search result click
     */
    public static class Click {
        @SerializedName("searchLogId")
        private final int searchLogId;

        @SerializedName("searchLogItemId")
        private final int searchLogItemId;

        public Click(int searchLogId, int searchLogItemId) {
            this.searchLogId = searchLogId;
            this.searchLogItemId = searchLogItemId;
        }
    }

    /**
     * Request for submitting search feedback
     */
    public static class Feedback {
        @SerializedName("searchLogId")
        private final int searchLogId;

        @SerializedName("isHelpful")
        private final boolean isHelpful;

        @SerializedName("issues")
        private final String issues;

        @SerializedName("comment")
        private final String comment;

        public Feedback(int searchLogId, boolean isHelpful, String issues, String comment) {
            this.searchLogId = searchLogId;
            this.isHelpful = isHelpful;
            this.issues = issues;
            this.comment = comment;
        }
    }
}
