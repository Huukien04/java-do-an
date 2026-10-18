package com.hcmute.topicmanagement.enums;

public enum StudentGroupStatus {
    FORMING("Đang tạo nhóm"),
    READY("Đã đủ thành viên"),
    REGISTERED("Đã đăng ký đề tài"),
    APPROVED("Đã duyệt đề tài"),
    SUBMITTED("Đã nộp báo cáo"),
    NOT_SUBMITTED("Chưa nộp báo cáo"),
    DEFENDED("Đã bảo vệ hội đồng");

    private final String displayName;

    StudentGroupStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
