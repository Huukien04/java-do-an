package com.hcmute.topicmanagement.enums;

public enum PeriodStatus {
    DRAFT("Mới tạo (Nháp)"),
    PHASE1_TEACHER_REG("Phase 1: GV Đăng ký đề tài"),
    PHASE1_DEAN_REVIEW("Phase 1: Khoa duyệt đề tài"),
    PUBLISHED_TOPICS("Đã công bố danh sách đề tài"),
    PHASE2_STUDENT_REG("Phase 2: SV Lập nhóm & Đăng ký đề tài"),
    STUDENT_REGISTRATION("Phase 2: SV Lập nhóm & Đăng ký đề tài"),
    EXECUTION("SV Đang thực hiện đề tài & Nộp báo cáo"),
    DEFENSE_EVALUATION("Phản biện & Chấm điểm hội đồng"),
    COMPLETED("Đã hoàn tất & Công bố kết quả"),
    CANCELLED("Đã hủy đợt");

    private final String displayName;

    PeriodStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}