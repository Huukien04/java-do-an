package com.hcmute.topicmanagement.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreDTO {

    private Long id;

    @NotNull(message = "Thiếu ID Đề tài")
    private Long topicId;

    @NotNull(message = "Thiếu ID Giảng viên chấm điểm")
    private Long teacherId;
    private String teacherName;

    @NotNull(message = "Điểm số không được để trống")
    @DecimalMin(value = "0.00", message = "Điểm tối thiểu là 0.0")
    @DecimalMax(value = "10.00", message = "Điểm tối đa là 10.0")
    private BigDecimal scoreValue;

    private String comments;
    private LocalDateTime scoredAt;
}
