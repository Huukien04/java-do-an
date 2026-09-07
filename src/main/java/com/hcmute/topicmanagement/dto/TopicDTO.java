package com.hcmute.topicmanagement.dto;

import com.hcmute.topicmanagement.enums.TopicStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicDTO {

    private Long id;

    @NotBlank(message = "Tên đề tài không được để trống")
    private String title;

    @NotBlank(message = "Mô tả đề tài không được để trống")
    private String description;

    private String requirements;

    @NotNull(message = "Vui lòng chọn Bộ môn")
    private Long departmentId;
    private String departmentName;

    @NotNull(message = "Vui lòng chọn Đợt đăng ký")
    private Long periodId;
    private String periodName;

    private TopicStatus status;

    private Long createdByTeacherId;
    private String createdByTeacherName;

    @NotNull(message = "Phải chọn GVHD chính (GVHD1)")
    private Long primaryAdvisorId;
    private String primaryAdvisorName;

    private Long secondaryAdvisorId; // Optional
    private String secondaryAdvisorName;

    private List<Long> criticTeacherIds;
    private List<String> criticTeacherNames;

    private LocalDateTime createdAt;
}
