package com.hcmute.topicmanagement.enums;

public enum EvaluationStatus {
    PASSED("Đạt"),
    FAILED("Không đạt");

    private final String displayName;

    EvaluationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
