package com.hcmute.topicmanagement.enums;

public enum TopicRegistrationStatus {
    PENDING("Chờ Khoa duyệt"),
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
