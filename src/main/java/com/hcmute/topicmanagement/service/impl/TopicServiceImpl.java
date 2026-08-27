package com.hcmute.topicmanagement.service.impl;

import com.hcmute.topicmanagement.dto.TopicDTO;
import com.hcmute.topicmanagement.entity.*;
import com.hcmute.topicmanagement.enums.AdvisorRole;
import com.hcmute.topicmanagement.enums.PeriodStatus;
import com.hcmute.topicmanagement.enums.TopicStatus;
import com.hcmute.topicmanagement.exception.BusinessException;
import com.hcmute.topicmanagement.exception.ResourceNotFoundException;
import com.hcmute.topicmanagement.repository.*;
import com.hcmute.topicmanagement.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TopicServiceImpl implements TopicService {

    private final TopicRepository topicRepository;
    private final TopicAdvisorRepository topicAdvisorRepository;
    private final TeacherRepository teacherRepository;
    private final DepartmentRepository departmentRepository;
    private final RegistrationPeriodRepository periodRepository;

    @Override
    public TopicDTO createTopic(TopicDTO dto, Long currentTeacherId) {
        Teacher teacher = teacherRepository.findById(currentTeacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Giảng viên", "id", currentTeacherId));

        RegistrationPeriod period = periodRepository.findById(dto.getPeriodId())
                .orElseThrow(() -> new ResourceNotFoundException("Đợt đăng ký", "id", dto.getPeriodId()));

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Bộ môn", "id", dto.getDepartmentId()));

        // Check timeline rule: GV chỉ được đăng ký đề tài trong thời gian đăng ký GV
        LocalDateTime now = LocalDateTime.now();
        if (period.getStatus() != PeriodStatus.PHASE1_TEACHER_REG && (now.isBefore(period.getTeacherStartDate()) || now.isAfter(period.getTeacherEndDate()))) {
            throw new BusinessException("Thời gian đăng ký đề tài của Giảng viên ngoài khung giờ quy định.");
        }

        Topic topic = Topic.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .requirements(dto.getRequirements())
                .department(department)
                .period(period)
                .status(TopicStatus.PENDING_APPROVAL)
                .createdByTeacher(teacher)
                .advisors(new ArrayList<>())
                .build();

        topic = topicRepository.save(topic);

        // Validate Advisor Uniqueness first
        if (dto.getSecondaryAdvisorId() != null && dto.getSecondaryAdvisorId().equals(dto.getPrimaryAdvisorId())) {
            throw new BusinessException("GVHD 1 và GVHD 2 không được là cùng một người.");
        }

        Teacher primaryAdvisor = teacherRepository.findById(dto.getPrimaryAdvisorId())
                .orElseThrow(() -> new ResourceNotFoundException("Giảng viên hướng dẫn 1", "id", dto.getPrimaryAdvisorId()));

        TopicAdvisor ta1 = TopicAdvisor.builder()
                .topic(topic)
                .teacher(primaryAdvisor)
                .advisorRole(AdvisorRole.PRIMARY)
                .build();
        topic.getAdvisors().add(ta1);

        if (dto.getSecondaryAdvisorId() != null) {
            Teacher secondaryAdvisor = teacherRepository.findById(dto.getSecondaryAdvisorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Giảng viên hướng dẫn 2", "id", dto.getSecondaryAdvisorId()));
            TopicAdvisor ta2 = TopicAdvisor.builder()
                    .topic(topic)
                    .teacher(secondaryAdvisor)
                    .advisorRole(AdvisorRole.SECONDARY)
                    .build();
            topic.getAdvisors().add(ta2);
        }

        topic = topicRepository.save(topic);
        return mapToDTO(topic);
    }

    @Override
    public TopicDTO updateTopic(Long id, TopicDTO dto, Long currentTeacherId) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đề tài", "id", id));

        if (!topic.getCreatedByTeacher().getId().equals(currentTeacherId)) {
            throw new BusinessException("Bạn không có quyền chỉnh sửa đề tài của người khác.");
        }

        if (topic.getStatus() == TopicStatus.APPROVED || topic.getStatus() == TopicStatus.ASSIGNED) {
            throw new BusinessException("Đề tài đã được duyệt hoặc đã phân công không thể chỉnh sửa.");
        }

        if (dto.getSecondaryAdvisorId() != null && dto.getSecondaryAdvisorId().equals(dto.getPrimaryAdvisorId())) {
            throw new BusinessException("GVHD 1 và GVHD 2 không được là cùng một người.");
        }

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Bộ môn", "id", dto.getDepartmentId()));

        topic.setTitle(dto.getTitle());
        topic.setDescription(dto.getDescription());
        topic.setRequirements(dto.getRequirements());
        topic.setDepartment(department);
        topic.setStatus(TopicStatus.PENDING_APPROVAL);

        // Clear existing advisors and flush to trigger orphanRemoval immediately in database
        topic.getAdvisors().clear();
        topicRepository.saveAndFlush(topic);

        Teacher primaryAdvisor = teacherRepository.findById(dto.getPrimaryAdvisorId())
                .orElseThrow(() -> new ResourceNotFoundException("GVHD 1", "id", dto.getPrimaryAdvisorId()));
        TopicAdvisor ta1 = TopicAdvisor.builder()
                .topic(topic)
                .teacher(primaryAdvisor)
                .advisorRole(AdvisorRole.PRIMARY)
                .build();
        topic.getAdvisors().add(ta1);

        if (dto.getSecondaryAdvisorId() != null) {
            Teacher secondaryAdvisor = teacherRepository.findById(dto.getSecondaryAdvisorId())
                    .orElseThrow(() -> new ResourceNotFoundException("GVHD 2", "id", dto.getSecondaryAdvisorId()));
            TopicAdvisor ta2 = TopicAdvisor.builder()
                    .topic(topic)
                    .teacher(secondaryAdvisor)
                    .advisorRole(AdvisorRole.SECONDARY)
                    .build();
            topic.getAdvisors().add(ta2);
        }

        topic = topicRepository.save(topic);
        return mapToDTO(topic);
    }

    @Override
    public void deleteTopic(Long id, Long currentTeacherId) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đề tài", "id", id));
        if (!topic.getCreatedByTeacher().getId().equals(currentTeacherId)) {
            throw new BusinessException("Bạn không có quyền xóa đề tài của người khác.");
        }
        if (topic.getStatus() == TopicStatus.APPROVED || topic.getStatus() == TopicStatus.ASSIGNED) {
            throw new BusinessException("Không thể xóa đề tài đã phê duyệt.");
        }
        topicRepository.delete(topic);
    }

    @Override
    public void approveTopic(Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Đề tài", "id", topicId));
        topic.setStatus(TopicStatus.APPROVED);
        topicRepository.save(topic);
    }

    @Override
    public void rejectTopic(Long topicId, String reason) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Đề tài", "id", topicId));
        topic.setStatus(TopicStatus.REJECTED);
        topicRepository.save(topic);
    }

    @Override
    @Transactional(readOnly = true)
    public TopicDTO getTopicById(Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Đề tài", "id", topicId));
        return mapToDTO(topic);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopicDTO> getTopicsByPeriod(Long periodId) {
        return topicRepository.findByPeriodId(periodId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopicDTO> getApprovedTopicsByPeriod(Long periodId) {
        return topicRepository.findByPeriodIdAndStatus(periodId, TopicStatus.APPROVED).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopicDTO> getTopicsByTeacher(Long teacherId) {
        return topicRepository.findTopicsByAdvisorTeacherId(teacherId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopicDTO> getAllTopics() {
        return topicRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private TopicDTO mapToDTO(Topic topic) {
        TopicDTO.TopicDTOBuilder builder = TopicDTO.builder()
                .id(topic.getId())
                .title(topic.getTitle())
                .description(topic.getDescription())
                .requirements(topic.getRequirements())
                .departmentId(topic.getDepartment().getId())
                .departmentName(topic.getDepartment().getName())
                .periodId(topic.getPeriod().getId())
                .periodName(topic.getPeriod().getName())
                .status(topic.getStatus())
                .createdByTeacherId(topic.getCreatedByTeacher().getId())
                .createdByTeacherName(topic.getCreatedByTeacher().getUser().getFullName())
                .createdAt(topic.getCreatedAt());

        for (TopicAdvisor ta : topic.getAdvisors()) {
            if (ta.getAdvisorRole() == AdvisorRole.PRIMARY) {
                builder.primaryAdvisorId(ta.getTeacher().getId());
                builder.primaryAdvisorName(ta.getTeacher().getUser().getFullName());
            } else if (ta.getAdvisorRole() == AdvisorRole.SECONDARY) {
                builder.secondaryAdvisorId(ta.getTeacher().getId());
                builder.secondaryAdvisorName(ta.getTeacher().getUser().getFullName());
            }
        }
        return builder.build();
    }
}
