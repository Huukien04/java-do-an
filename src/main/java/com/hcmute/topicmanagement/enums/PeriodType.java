package com.hcmute.topicmanagement.enums;

public enum PeriodType {
    MON_HOC("Đề tài môn học"),
    NCKH("Nghiên cứu khoa học"),
    TLCN("Tiểu luận chuyên ngành"),
    KLTN("Khóa luận tốt nghiệp"),
    KHAN_LUAN("Khóa luận tốt nghiệp");

    private final String displayName;

    PeriodType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
