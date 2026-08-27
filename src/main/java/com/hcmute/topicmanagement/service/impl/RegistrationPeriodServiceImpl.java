package com.hcmute.topicmanagement.service.impl;

import com.hcmute.topicmanagement.dto.RegistrationPeriodDTO;
import com.hcmute.topicmanagement.entity.RegistrationPeriod;
import com.hcmute.topicmanagement.enums.PeriodStatus;
import com.hcmute.topicmanagement.enums.PeriodType;
import com.hcmute.topicmanagement.exception.BusinessException;
import com.hcmute.topicmanagement.exception.ResourceNotFoundException;
import com.hcmute.topicmanagement.repository.RegistrationPeriodRepository;
import com.hcmute.topicmanagement.service.RegistrationPeriodService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RegistrationPeriodServiceImpl implements RegistrationPeriodService {

    private final RegistrationPeriodRepository periodRepository;

    @Override
    public RegistrationPeriodDTO createPeriod(RegistrationPeriodDTO dto) {
        validatePeriodRules(dto);

        RegistrationPeriod period = RegistrationPeriod.builder()
                .name(dto.getName())
                .periodType(dto.getPeriodType())
                .teacherStartDate(dto.getTeacherStartDate())
                .teacherEndDate(dto.getTeacherEndDate())
                .studentStartDate(dto.getStudentStartDate())
                .studentEndDate(dto.getStudentEndDate())
                .reviewSubmissionDeadline(dto.getReviewSubmissionDeadline())
                .councilReportDate(dto.getCouncilReportDate())
                .status(PeriodStatus.DRAFT)
                .build();

        period = periodRepository.save(period);
        return mapToDTO(period);
    }

    @Override
    public RegistrationPeriodDTO updatePeriod(Long id, RegistrationPeriodDTO dto) {
        RegistrationPeriod period = periodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đợt đăng ký", "id", id));

        validatePeriodRules(dto);

        period.setName(dto.getName());
        period.setPeriodType(dto.getPeriodType());
        period.setTeacherStartDate(dto.getTeacherStartDate());
        period.setTeacherEndDate(dto.getTeacherEndDate());
        period.setStudentStartDate(dto.getStudentStartDate());
        period.setStudentEndDate(dto.getStudentEndDate());
        period.setReviewSubmissionDeadline(dto.getReviewSubmissionDeadline());
        period.setCouncilReportDate(dto.getCouncilReportDate());

        period = periodRepository.save(period);
        return mapToDTO(period);
    }

    @Override
    public void changeStatus(Long id, PeriodStatus newStatus) {
        RegistrationPeriod period = periodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đợt đăng ký", "id", id));
        period.setStatus(newStatus);
        periodRepository.save(period);
    }

    @Override
    @Transactional(readOnly = true)
    public RegistrationPeriodDTO getPeriodById(Long id) {
        RegistrationPeriod period = periodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đợt đăng ký", "id", id));
        return mapToDTO(period);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationPeriodDTO> getAllPeriods() {
        return periodRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationPeriodDTO> getActivePeriodsForStudent() {
        return periodRepository.findAll().stream()
                .filter(p -> p.getStatus() == PeriodStatus.PHASE2_STUDENT_REG || p.getStatus() == PeriodStatus.PUBLISHED_TOPICS)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationPeriodDTO> getActivePeriodsForTeacher() {
        return periodRepository.findAll().stream()
                .filter(p -> p.getStatus() == PeriodStatus.PHASE1_TEACHER_REG)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private void validatePeriodRules(RegistrationPeriodDTO dto) {
        if (dto.getTeacherStartDate().isAfter(dto.getTeacherEndDate())) {
            throw new BusinessException("Thời gian bắt đầu đăng ký của GV phải trước thời gian kết thúc.");
        }
        if (dto.getStudentStartDate().isAfter(dto.getStudentEndDate())) {
            throw new BusinessException("Thời gian bắt đầu đăng ký của SV phải trước thời gian kết thúc.");
        }
        if (dto.getPeriodType() == PeriodType.TLCN || dto.getPeriodType() == PeriodType.KLTN) {
            if (dto.getReviewSubmissionDeadline() == null) {
                throw new BusinessException("Hạn chót GVPB nộp điểm là BẮT BUỘC đối với " + dto.getPeriodType().getDisplayName());
            }
        }
        if (dto.getPeriodType() == PeriodType.KLTN) {
            if (dto.getCouncilReportDate() == null) {
                throw new BusinessException("Ngày báo cáo hội đồng là BẮT BUỘC đối với " + dto.getPeriodType().getDisplayName());
            }
        }
    }

    private RegistrationPeriodDTO mapToDTO(RegistrationPeriod period) {
        return RegistrationPeriodDTO.builder()
                .id(period.getId())
                .name(period.getName())
                .periodType(period.getPeriodType())
                .teacherStartDate(period.getTeacherStartDate())
                .teacherEndDate(period.getTeacherEndDate())
                .studentStartDate(period.getStudentStartDate())
                .studentEndDate(period.getStudentEndDate())
                .reviewSubmissionDeadline(period.getReviewSubmissionDeadline())
                .councilReportDate(period.getCouncilReportDate())
                .status(period.getStatus())
                .build();
    }
}
