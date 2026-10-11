package com.hcmute.topicmanagement.dto;

import com.hcmute.topicmanagement.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {

    private Long id;

    @NotBlank(message = "Tiêu đề thông báo không được để trống")
    private String title;

    @NotBlank(message = "Nội dung thông báo không được để trống")
    private String content;

    @NotNull(message = "Loại thông báo không được để trống")
    private NotificationType type;

    private String targetRole; // ALL, ROLE_TEACHER, ROLE_STUDENT

    @Builder.Default
    private List<Long> targetUserIds = new ArrayList<>();

    private Long periodId;
    private String periodName;

    private Long createdByUserId;
    private String createdByFullName;

    private LocalDateTime createdAt;
}
