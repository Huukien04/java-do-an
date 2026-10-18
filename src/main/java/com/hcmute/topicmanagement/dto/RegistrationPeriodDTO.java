package com.hcmute.topicmanagement.dto;

import com.hcmute.topicmanagement.enums.PeriodStatus;
import com.hcmute.topicmanagement.enums.PeriodType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationPeriodDTO {

    private Long id;

    @NotBlank(message = "Tên đợt đăng ký không được để trống")
    private String name;

    @NotNull(message = "Vui lòng chọn loại đợt đăng ký")
    private PeriodType periodType;

    @NotNull(message = "Thời gian bắt đầu GV đăng ký không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime teacherStartDate;

    @NotNull(message = "Thời gian kết thúc GV đăng ký không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime teacherEndDate;

    @NotNull(message = "Thời gian bắt đầu Khoa duyệt không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime approvalStartDate;

    @NotNull(message = "Thời gian kết thúc Khoa duyệt không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime approvalEndDate;

    @NotNull(message = "Thời gian bắt đầu SV đăng ký không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime studentStartDate;

    @NotNull(message = "Thời gian kết thúc SV đăng ký không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime studentEndDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @NotNull(message = "Hạn nộp báo cáo của nhóm không được để trống")
    private LocalDateTime submissionDeadline;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @NotNull(message = "Hạn chót nộp điểm không được để trống")
    private LocalDateTime reviewSubmissionDeadline;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime councilReportDate;

    private PeriodStatus status;

    private boolean statusManuallyControlled;
}
