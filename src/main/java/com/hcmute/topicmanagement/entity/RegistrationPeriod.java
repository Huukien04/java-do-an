package com.hcmute.topicmanagement.entity;

import com.hcmute.topicmanagement.enums.PeriodStatus;
import com.hcmute.topicmanagement.enums.PeriodType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "registration_periods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 20)
    private PeriodType periodType;

    @Column(name = "teacher_start_date", nullable = false)
    private LocalDateTime teacherStartDate;

    @Column(name = "teacher_end_date", nullable = false)
    private LocalDateTime teacherEndDate;

    @Column(name = "approval_start_date")
    private LocalDateTime approvalStartDate;

    @Column(name = "approval_end_date")
    private LocalDateTime approvalEndDate;

    @Column(name = "student_start_date", nullable = false)
    private LocalDateTime studentStartDate;

    @Column(name = "student_end_date", nullable = false)
    private LocalDateTime studentEndDate;

    @Column(name = "review_submission_deadline")
    private LocalDateTime reviewSubmissionDeadline; // Required for TLCN & KLTN

    @Column(name = "council_report_date")
    private LocalDateTime councilReportDate; // Required for KLTN

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private PeriodStatus status = PeriodStatus.DRAFT;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
