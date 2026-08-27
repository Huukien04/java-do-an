package com.hcmute.topicmanagement.enums;

public enum AdvisorRole {
    PRIMARY("GV Hướng dẫn chính (GVHD1)"),
    SECONDARY("GV Hướng dẫn phụ (GVHD2)");

    private final String displayName;

    AdvisorRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
