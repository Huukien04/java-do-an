package com.hcmute.topicmanagement.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ScoreProgressDTO {
    private final int requiredCount;
    private final int submittedCount;
    private final boolean complete;
    private final List<String> pendingTeacherNames;
    private final boolean scoreSpreadWarning;
    private final boolean deadlinePassed;
}
