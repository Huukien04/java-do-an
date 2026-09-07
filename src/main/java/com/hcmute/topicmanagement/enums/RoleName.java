package com.hcmute.topicmanagement.enums;

public enum RoleName {
    ROLE_ADMIN("Quản trị hệ thống"),
    ROLE_DEAN("Trưởng khoa"),
    ROLE_TEACHER("Giảng viên"),
    ROLE_STUDENT("Sinh viên");

    private final String displayName;

    RoleName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
