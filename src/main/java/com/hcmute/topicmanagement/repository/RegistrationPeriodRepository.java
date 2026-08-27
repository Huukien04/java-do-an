package com.hcmute.topicmanagement.repository;

import com.hcmute.topicmanagement.entity.RegistrationPeriod;
import com.hcmute.topicmanagement.enums.PeriodStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistrationPeriodRepository extends JpaRepository<RegistrationPeriod, Long> {
    List<RegistrationPeriod> findByStatus(PeriodStatus status);
    List<RegistrationPeriod> findByStatusNot(PeriodStatus status);
}
