package com.hcmute.topicmanagement.repository;

import com.hcmute.topicmanagement.entity.RegistrationPeriod;
import com.hcmute.topicmanagement.enums.PeriodStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface RegistrationPeriodRepository extends JpaRepository<RegistrationPeriod, Long> {
    List<RegistrationPeriod> findByStatus(PeriodStatus status);
    List<RegistrationPeriod> findByStatusNot(PeriodStatus status);
    List<RegistrationPeriod> findByStatusIn(List<PeriodStatus> statuses);
    List<RegistrationPeriod> findByStatusAndReviewSubmissionDeadlineIsNotNullAndReviewSubmissionDeadlineLessThanEqualAndScoringReminderSentAtIsNull(
            PeriodStatus status, LocalDateTime now);
}
