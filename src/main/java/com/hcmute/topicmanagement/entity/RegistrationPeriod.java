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

    @Column(name = "submission_deadline")
    private LocalDateTime submissionDeadline;

    @Column(name = "review_submission_deadline")
    private LocalDateTime reviewSubmissionDeadline; // Required for TLCN & KLTN

    @Column(name = "council_report_date")
    private LocalDateTime councilReportDate; // Required for KLTN

    @Column(name = "teacher_extension_count", nullable = false)
    @Builder.Default
    private int teacherExtensionCount = 0;

    @Column(name = "student_extension_count", nullable = false)
    @Builder.Default
    private int studentExtensionCount = 0;

    @Column(name = "submission_extension_count", nullable = false)
    @Builder.Default
    private int submissionExtensionCount = 0;

    @Column(name = "scoring_extension_count", nullable = false)
    @Builder.Default
    private int scoringExtensionCount = 0;

    @Column(name = "scoring_reminder_sent_at")
    private LocalDateTime scoringReminderSentAt;

    @Column(name = "teacher_registration_manually_opened", nullable = false)
    @Builder.Default
    private boolean teacherRegistrationManuallyOpened = false;

    @Column(name = "student_registration_manually_opened", nullable = false)
    @Builder.Default
    private boolean studentRegistrationManuallyOpened = false;

    @Column(name = "status_manually_controlled", nullable = false)
    @Builder.Default
    private boolean statusManuallyControlled = false;

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
