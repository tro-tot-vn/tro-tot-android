package com.trototvn.trototandroid.data.model.chat;

import java.util.List;

public class CreateConversationRequest {
    private String conversationType;
    private List<Integer> participantIds;

    public CreateConversationRequest(String conversationType, List<Integer> participantIds) {
        this.conversationType = conversationType;
        this.participantIds = participantIds;
    }

    public String getConversationType() {
        return conversationType;
    }

    public void setConversationType(String conversationType) {
        this.conversationType = conversationType;
    }

    public List<Integer> getParticipantIds() {
        return participantIds;
    }

    public void setParticipantIds(List<Integer> participantIds) {
        this.participantIds = participantIds;
    }
}
