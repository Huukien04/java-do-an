package com.hcmute.topicmanagement.enums;

public enum TopicStatus {
    DRAFT("Bản nháp"),
    PENDING_APPROVAL("Chờ duyệt"),
    APPROVED("Khoa đã duyệt"),
    PUBLISHED("Đã công bố"),
    REJECTED("Khoa từ chối"),
    ASSIGNED("Đã được nhóm SV đăng ký"),
    SCORING("Đang chấm điểm"),
    SCORED("Đã có điểm"),
    RESULT_PUBLISHED("Đã công bố kết quả"),
    COMPLETED("Đã hoàn thành");

    private final String displayName;

    TopicStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
