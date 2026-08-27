package com.hcmute.topicmanagement.enums;

public enum TopicRegistrationStatus {
    PENDING("Chờ duyệt"),
    APPROVED("Đã phê duyệt"),
    REJECTED("Từ chối"),
    CANCELLED("Hủy đăng ký");

    private final String displayName;

    TopicRegistrationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
