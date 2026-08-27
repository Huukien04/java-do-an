package com.hcmute.topicmanagement.dto;

import com.hcmute.topicmanagement.enums.StudentGroupStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentGroupDTO {

    private Long id;
    private String groupCode;
    private Long periodId;
    private String periodName;
    private Long leaderStudentId;
    private String leaderStudentName;
    private String leaderStudentCode;
    private StudentGroupStatus status;
    private List<StudentMemberDTO> members;
    private Long registeredTopicId;
    private String registeredTopicTitle;
    private String registrationStatus;
    private String rejectionReason;
    private LocalDateTime createdAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StudentMemberDTO {
        private Long studentId;
        private String studentCode;
        private String fullName;
        private String className;
        private boolean isLeader;
    }
}
