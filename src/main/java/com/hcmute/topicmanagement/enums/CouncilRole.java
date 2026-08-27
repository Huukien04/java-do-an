package com.hcmute.topicmanagement.enums;

public enum CouncilRole {
    CHAIRMAN("Chủ tịch hội đồng"),
    SECRETARY("Thư ký hội đồng"),
    MEMBER("Ủy viên hội đồng");

    private final String displayName;

    CouncilRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
