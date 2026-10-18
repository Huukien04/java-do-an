package com.hcmute.topicmanagement.service;

import com.hcmute.topicmanagement.dto.RegistrationPeriodDTO;
import com.hcmute.topicmanagement.enums.PeriodStatus;
import com.hcmute.topicmanagement.enums.PeriodAction;
import java.time.LocalDateTime;

import java.util.List;

public interface RegistrationPeriodService {
    RegistrationPeriodDTO createPeriod(RegistrationPeriodDTO dto);
    RegistrationPeriodDTO updatePeriod(Long id, RegistrationPeriodDTO dto);
    void changeStatus(Long id, PeriodStatus newStatus);
    void resumeAutomaticStatus(Long id);
    void performAction(Long id, PeriodAction action, LocalDateTime newEndDate, Long actorUserId);
    void advancePeriodsAutomatically();
    RegistrationPeriodDTO getPeriodById(Long id);
    List<RegistrationPeriodDTO> getAllPeriods();
    List<RegistrationPeriodDTO> getActivePeriodsForStudent();
    List<RegistrationPeriodDTO> getPeriodsForStudentGroupManagement();
    List<RegistrationPeriodDTO> getActivePeriodsForTeacher();
}
