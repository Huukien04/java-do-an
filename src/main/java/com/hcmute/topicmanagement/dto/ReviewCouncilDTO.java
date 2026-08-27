package com.hcmute.topicmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewCouncilDTO {

    private Long id;

    @NotBlank(message = "Mã hội đồng không được để trống")
    private String councilCode;

    @NotBlank(message = "Tên hội đồng không được để trống")
    private String name;

    @NotNull(message = "Vui lòng chọn đợt đăng ký")
    private Long periodId;
    private String periodName;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime defenseDate;

    private String location;

    @NotNull(message = "Phải phân công Chủ tịch hội đồng")
    private Long chairmanTeacherId;
    private String chairmanTeacherName;

    @NotNull(message = "Phải phân công Thư ký hội đồng")
    private Long secretaryTeacherId;
    private String secretaryTeacherName;

    private List<Long> memberTeacherIds;
    private List<CouncilMemberDTO> members;

    private List<Long> assignedTopicIds;
    private List<TopicDTO> assignedTopics;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CouncilMemberDTO {
        private Long teacherId;
        private String teacherCode;
        private String fullName;
        private String role; // CHAIRMAN, SECRETARY, MEMBER
    }
}
