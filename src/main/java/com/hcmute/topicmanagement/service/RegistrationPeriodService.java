package com.hcmute.topicmanagement.service;

import com.hcmute.topicmanagement.dto.RegistrationPeriodDTO;
import com.hcmute.topicmanagement.enums.PeriodStatus;

import java.util.List;

public interface RegistrationPeriodService {
    RegistrationPeriodDTO createPeriod(RegistrationPeriodDTO dto);
    RegistrationPeriodDTO updatePeriod(Long id, RegistrationPeriodDTO dto);
    void changeStatus(Long id, PeriodStatus newStatus);
    RegistrationPeriodDTO getPeriodById(Long id);
    List<RegistrationPeriodDTO> getAllPeriods();
    List<RegistrationPeriodDTO> getActivePeriodsForStudent();
    List<RegistrationPeriodDTO> getActivePeriodsForTeacher();
}
