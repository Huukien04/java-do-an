package com.hcmute.topicmanagement.service.impl;

import com.hcmute.topicmanagement.dto.NotificationDTO;
import com.hcmute.topicmanagement.dto.StudentGroupDTO;
import com.hcmute.topicmanagement.entity.*;
import com.hcmute.topicmanagement.enums.*;
import com.hcmute.topicmanagement.exception.BusinessException;
import com.hcmute.topicmanagement.exception.ResourceNotFoundException;
import com.hcmute.topicmanagement.repository.*;
import com.hcmute.topicmanagement.service.NotificationService;
import com.hcmute.topicmanagement.service.StudentGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentGroupServiceImpl implements StudentGroupService {

    private final StudentGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final TopicRegistrationRepository registrationRepository;
    private final StudentRepository studentRepository;
    private final RegistrationPeriodRepository periodRepository;
    private final TopicRepository topicRepository;
    private final NotificationService notificationService;

    @Override
    public StudentGroupDTO createGroup(Long leaderStudentId, Long periodId) {
        Student leader = studentRepository.findById(leaderStudentId)
                .orElseThrow(() -> new ResourceNotFoundException("Sinh viên", "id", leaderStudentId));

        RegistrationPeriod period = periodRepository.findById(periodId)
                .orElseThrow(() -> new ResourceNotFoundException("Đợt đăng ký", "id", periodId));

        // Check timeline rule for student registration
        LocalDateTime now = LocalDateTime.now();
        if (period.getStatus() != PeriodStatus.PHASE2_STUDENT_REG
            || now.isBefore(period.getStudentStartDate())
            || now.isAfter(period.getStudentEndDate())) {
            throw new BusinessException("Hiện tại ngoài thời gian cho phép Sinh viên tạo nhóm / đăng ký đợt.");
        }

        // Rule: Each student can join ONLY 1 group per period
        if (memberRepository.isStudentInAnyGroupInPeriod(leaderStudentId, periodId)) {
            throw new BusinessException("Sinh viên đã thuộc một nhóm khác trong đợt đăng ký này.");
        }

        String groupCode = "G" + System.currentTimeMillis() % 100000;

        StudentGroup group = StudentGroup.builder()
                .groupCode(groupCode)
                .period(period)
                .leader(leader)
                .status(StudentGroupStatus.FORMING)
                .members(new ArrayList<>())
                .build();

        group = groupRepository.save(group);

        // Add leader as first member
        GroupMember leaderMember = GroupMember.builder()
                .group(group)
                .student(leader)
                .build();
        memberRepository.save(leaderMember);
        group.getMembers().add(leaderMember);

        return mapToDTO(group);
    }

    @Override
    public StudentGroupDTO addMember(Long groupId, String studentCode, Long currentStudentId) {
        StudentGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Nhóm sinh viên", "id", groupId));

        if (!group.getLeader().getId().equals(currentStudentId)) {
            throw new BusinessException("Chỉ Nhóm trưởng mới có quyền thêm thành viên vào nhóm.");
        }

        if (group.getMembers().size() >= 3) {
            throw new BusinessException("Nhóm đã đạt số lượng tối đa 03 thành viên.");
        }

        Student student = studentRepository.findByStudentCode(studentCode)
                .orElseThrow(() -> new ResourceNotFoundException("Sinh viên", "mã SV", studentCode));

        if (memberRepository.isStudentInAnyGroupInPeriod(student.getId(), group.getPeriod().getId())) {
            throw new BusinessException("Sinh viên " + student.getUser().getFullName() + " (Mã SV: " + studentCode + ") đã thuộc một nhóm khác trong đợt này.");
        }

        GroupMember member = GroupMember.builder()
                .group(group)
                .student(student)
                .build();
        memberRepository.save(member);
        group.getMembers().add(member);

        if (group.getMembers().size() >= 1) {
            group.setStatus(StudentGroupStatus.READY);
            groupRepository.save(group);
        }

        return mapToDTO(group);
    }

    @Override
    public void removeMember(Long groupId, Long memberStudentId, Long currentStudentId) {
        StudentGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Nhóm sinh viên", "id", groupId));

        if (!group.getLeader().getId().equals(currentStudentId)) {
            throw new BusinessException("Chỉ Nhóm trưởng mới có quyền xóa thành viên khỏi nhóm.");
        }

        if (memberStudentId.equals(group.getLeader().getId())) {
            throw new BusinessException("Không thể xóa Nhóm trưởng khỏi nhóm.");
        }

        GroupMember member = group.getMembers().stream()
                .filter(m -> m.getStudent().getId().equals(memberStudentId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Sinh viên không thuộc nhóm này."));

        group.getMembers().remove(member);
        memberRepository.delete(member);
    }

    @Override
    public StudentGroupDTO registerTopic(Long groupId, Long topicId, Long currentStudentId) {
        StudentGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Nhóm sinh viên", "id", groupId));

        // Rule: Only Leader can register topic
        if (!group.getLeader().getId().equals(currentStudentId)) {
            throw new BusinessException("Chỉ Nhóm trưởng mới được phép đăng ký đề tài cho nhóm.");
        }

        RegistrationPeriod period = group.getPeriod();
        LocalDateTime now = LocalDateTime.now();
        if (period.getStatus() != PeriodStatus.PHASE2_STUDENT_REG
            || now.isBefore(period.getStudentStartDate())
            || now.isAfter(period.getStudentEndDate())) {
            throw new BusinessException("Thời gian đăng ký đề tài của Sinh viên đã kết thúc hoặc chưa bắt đầu.");
        }

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Đề tài", "id", topicId));

        if (topic.getStatus() != TopicStatus.APPROVED) {
            throw new BusinessException("Đề tài này chưa được Khoa phê duyệt hoặc đã được phân công.");
        }

        // Rule: One group registers only 1 topic
        if (registrationRepository.existsByGroupId(groupId)) {
            throw new BusinessException("Nhóm của bạn đã đăng ký một đề tài trong đợt này rồi.");
        }

        if (registrationRepository.existsByTopicId(topicId)) {
            throw new BusinessException("Đề tài này đã được một nhóm khác đăng ký.");
        }

        TopicRegistration reg = TopicRegistration.builder()
                .group(group)
                .topic(topic)
                .status(TopicRegistrationStatus.PENDING)
                .build();
        registrationRepository.save(reg);

        group.setStatus(StudentGroupStatus.REGISTERED);
        groupRepository.save(group);

        return mapToDTO(group);
    }

    @Override
    public void approveTopicRegistration(Long registrationId) {
        TopicRegistration reg = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Đăng ký đề tài", "id", registrationId));

        reg.setStatus(TopicRegistrationStatus.APPROVED);
        registrationRepository.save(reg);

        Topic topic = reg.getTopic();
        topic.setStatus(TopicStatus.ASSIGNED);
        topicRepository.save(topic);

        StudentGroup group = reg.getGroup();
        group.setStatus(StudentGroupStatus.APPROVED);
        groupRepository.save(group);

        notifyGroupMembers(reg, "Đăng ký đề tài \"" + topic.getTitle()
                + "\" của nhóm " + group.getGroupCode() + " đã được giảng viên duyệt.");
    }

    @Override
    public void rejectTopicRegistration(Long registrationId, String reason) {
        TopicRegistration reg = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Đăng ký đề tài", "id", registrationId));

        reg.setStatus(TopicRegistrationStatus.REJECTED);
        reg.setRejectionReason(reason);
        registrationRepository.save(reg);

        StudentGroup group = reg.getGroup();
        group.setStatus(StudentGroupStatus.READY);
        groupRepository.save(group);

        String content = "Đăng ký đề tài \"" + reg.getTopic().getTitle()
                + "\" của nhóm " + group.getGroupCode() + " đã bị giảng viên từ chối."
                + (reason == null || reason.isBlank() ? "" : " Lý do: " + reason.trim());
        notifyGroupMembers(reg, content);
    }

    private void notifyGroupMembers(TopicRegistration registration, String content) {
        StudentGroup group = registration.getGroup();
        Set<Long> recipientIds = new LinkedHashSet<>();
        recipientIds.add(group.getLeader().getUser().getId());
        group.getMembers().forEach(member -> recipientIds.add(member.getStudent().getUser().getId()));

        notificationService.createNotification(NotificationDTO.builder()
                .title("Cập nhật đăng ký đề tài")
                .content(content)
                .type(NotificationType.RESULT)
                .targetRole("INDIVIDUAL")
                .targetUserIds(List.copyOf(recipientIds))
                .periodId(group.getPeriod().getId())
                .build(), registration.getTopic().getCreatedByTeacher().getUser().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public StudentGroupDTO getGroupById(Long groupId) {
        StudentGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Nhóm sinh viên", "id", groupId));
        return mapToDTO(group);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentGroupDTO getMyGroupByPeriod(Long studentId, Long periodId) {
        Optional<StudentGroup> groupOpt = groupRepository.findGroupByStudentIdAndPeriodId(studentId, periodId);
        return groupOpt.map(this::mapToDTO).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentGroupDTO> getGroupsByPeriod(Long periodId) {
        return groupRepository.findByPeriodId(periodId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private StudentGroupDTO mapToDTO(StudentGroup group) {
        List<StudentGroupDTO.StudentMemberDTO> memberDTOs = group.getMembers().stream()
                .map(m -> StudentGroupDTO.StudentMemberDTO.builder()
                        .studentId(m.getStudent().getId())
                        .studentCode(m.getStudent().getStudentCode())
                        .fullName(m.getStudent().getUser().getFullName())
                        .className(m.getStudent().getClassName())
                        .isLeader(m.getStudent().getId().equals(group.getLeader().getId()))
                        .build())
                .collect(Collectors.toList());

        Optional<TopicRegistration> regOpt = registrationRepository.findByGroupId(group.getId());

        StudentGroupDTO.StudentGroupDTOBuilder builder = StudentGroupDTO.builder()
                .id(group.getId())
                .groupCode(group.getGroupCode())
                .periodId(group.getPeriod().getId())
                .periodName(group.getPeriod().getName())
                .leaderStudentId(group.getLeader().getId())
                .leaderStudentName(group.getLeader().getUser().getFullName())
                .leaderStudentCode(group.getLeader().getStudentCode())
                .status(group.getStatus())
                .members(memberDTOs)
                .createdAt(group.getCreatedAt());

        if (regOpt.isPresent()) {
            TopicRegistration reg = regOpt.get();
            builder.registeredTopicId(reg.getTopic().getId());
            builder.registeredTopicTitle(reg.getTopic().getTitle());
            builder.registrationStatus(reg.getStatus().name());
            builder.rejectionReason(reg.getRejectionReason());
        }

        return builder.build();
    }
}
