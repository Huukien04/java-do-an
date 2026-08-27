package com.hcmute.topicmanagement.enums;

public enum NotificationType {
    GENERAL("Thông báo chung"),
    PERIOD("Thông báo đợt đăng ký"),
    DEFENSE("Thông báo lịch phản biện"),
    RESULT("Thông báo kết quả");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
