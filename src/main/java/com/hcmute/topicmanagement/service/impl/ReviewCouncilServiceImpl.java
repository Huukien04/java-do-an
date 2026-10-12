package com.hcmute.topicmanagement.service.impl;

import com.hcmute.topicmanagement.dto.ReviewCouncilDTO;
import com.hcmute.topicmanagement.dto.TopicDTO;
import com.hcmute.topicmanagement.entity.*;
import com.hcmute.topicmanagement.enums.CouncilRole;
import com.hcmute.topicmanagement.enums.TopicStatus;
import com.hcmute.topicmanagement.exception.BusinessException;
import com.hcmute.topicmanagement.exception.ResourceNotFoundException;
import com.hcmute.topicmanagement.repository.*;
import com.hcmute.topicmanagement.service.ReviewCouncilService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewCouncilServiceImpl implements ReviewCouncilService {

    private final ReviewCouncilRepository councilRepository;
    private final CouncilMemberRepository councilMemberRepository;
    private final CouncilTopicRepository councilTopicRepository;
    private final RegistrationPeriodRepository periodRepository;
    private final TeacherRepository teacherRepository;
    private final TopicRepository topicRepository;

    @Override
    public ReviewCouncilDTO createCouncil(ReviewCouncilDTO dto) {
        RegistrationPeriod period = periodRepository.findById(dto.getPeriodId())
                .orElseThrow(() -> new ResourceNotFoundException("Đợt đăng ký", "id", dto.getPeriodId()));

        validateCouncilMembers(dto);

        ReviewCouncil council = ReviewCouncil.builder()
                .councilCode(generateCouncilCode(dto.getName()))
                .name(dto.getName())
                .period(period)
                .defenseDate(dto.getDefenseDate())
                .location(dto.getLocation())
                .members(new ArrayList<>())
                .councilTopics(new ArrayList<>())
                .build();

        council = councilRepository.save(council);

        // Add Chairman
        saveMember(council, dto.getChairmanTeacherId(), CouncilRole.CHAIRMAN);

        // Add Secretary
        saveMember(council, dto.getSecretaryTeacherId(), CouncilRole.SECRETARY);

        // Add Members
        if (dto.getMemberTeacherIds() != null) {
            for (Long teacherId : dto.getMemberTeacherIds()) {
                saveMember(council, teacherId, CouncilRole.MEMBER);
            }
        }

        // Assign topics if provided
        if (dto.getAssignedTopicIds() != null && !dto.getAssignedTopicIds().isEmpty()) {
            assignTopicsToCouncil(council.getId(), dto.getAssignedTopicIds());
        }

        return mapToDTO(councilRepository.findById(council.getId()).get());
    }

    @Override
    public ReviewCouncilDTO updateCouncil(Long id, ReviewCouncilDTO dto) {
        ReviewCouncil council = councilRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hội đồng", "id", id));

        validateCouncilMembers(dto);

        council.setName(dto.getName());
        council.setDefenseDate(dto.getDefenseDate());
        council.setLocation(dto.getLocation());

        // Rebuild members
        councilMemberRepository.deleteByCouncilId(council.getId());
        council.getMembers().clear();

        saveMember(council, dto.getChairmanTeacherId(), CouncilRole.CHAIRMAN);
        saveMember(council, dto.getSecretaryTeacherId(), CouncilRole.SECRETARY);

        if (dto.getMemberTeacherIds() != null) {
            for (Long teacherId : dto.getMemberTeacherIds()) {
                saveMember(council, teacherId, CouncilRole.MEMBER);
            }
        }

        if (dto.getAssignedTopicIds() != null) {
            assignTopicsToCouncil(council.getId(), dto.getAssignedTopicIds());
        }

        return mapToDTO(councilRepository.findById(council.getId()).get());
    }

    @Override
    public void deleteCouncil(Long id) {
        ReviewCouncil council = councilRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hội đồng", "id", id));
        councilRepository.delete(council);
    }

    @Override
    public void assignTopicsToCouncil(Long councilId, List<Long> topicIds) {
        ReviewCouncil council = councilRepository.findById(councilId)
                .orElseThrow(() -> new ResourceNotFoundException("Hội đồng", "id", councilId));

        Set<Long> uniqueTopicIds = new HashSet<>(topicIds);
        if (uniqueTopicIds.size() != topicIds.size()) {
            throw new BusinessException("Danh sách đề tài được gán có mục trùng lặp.");
        }

        List<Topic> topics = uniqueTopicIds.stream()
                .map(topicId -> topicRepository.findById(topicId)
                        .orElseThrow(() -> new ResourceNotFoundException("Đề tài", "id", topicId)))
                .toList();
        for (Topic topic : topics) {
            if (topic.getStatus() != TopicStatus.ASSIGNED) {
                throw new BusinessException("Chỉ được gán các đề tài đã được phân công cho nhóm.");
            }
            if (!topic.getPeriod().getId().equals(council.getPeriod().getId())) {
                throw new BusinessException("Đề tài được gán phải thuộc cùng đợt đăng ký với Hội đồng.");
            }
            if (councilTopicRepository.existsByTopicId(topic.getId())) {
                throw new BusinessException("Đề tài \"" + topic.getTitle() + "\" đã được gán cho Hội đồng khác.");
            }
        }

        for (Topic topic : topics) {
            CouncilTopic councilTopic = CouncilTopic.builder()
                    .council(council)
                    .topic(topic)
                    .build();
            councilTopicRepository.save(councilTopic);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewCouncilDTO getCouncilById(Long id) {
        ReviewCouncil council = councilRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hội đồng", "id", id));
        return mapToDTO(council);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewCouncilDTO> getCouncilsByPeriod(Long periodId) {
        return councilRepository.findByPeriodId(periodId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewCouncilDTO> getCouncilsByTeacher(Long teacherId) {
        return councilRepository.findCouncilsByTeacherId(teacherId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewCouncilDTO> getAllCouncils() {
        return councilRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private void validateCouncilMembers(ReviewCouncilDTO dto) {
        if (dto.getChairmanTeacherId().equals(dto.getSecretaryTeacherId())) {
            throw new BusinessException("Chủ tịch và Thư ký hội đồng không được là cùng một Giảng viên.");
        }

        Set<Long> uniqueTeacherIds = new HashSet<>();
        uniqueTeacherIds.add(dto.getChairmanTeacherId());
        uniqueTeacherIds.add(dto.getSecretaryTeacherId());

        if (dto.getMemberTeacherIds() != null) {
            for (Long mId : dto.getMemberTeacherIds()) {
                if (!uniqueTeacherIds.add(mId)) {
                    throw new BusinessException("Danh sách Giảng viên trong Hội đồng bị trùng lặp.");
                }
            }
        }

        int totalMembers = uniqueTeacherIds.size();
        if (totalMembers < 3 || totalMembers > 5) {
            throw new BusinessException("Mỗi Hội đồng phản biện phải có từ 03 đến 05 Giảng viên (hiện tại: " + totalMembers + ").");
        }
    }

    private void saveMember(ReviewCouncil council, Long teacherId, CouncilRole role) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Giảng viên", "id", teacherId));

        CouncilMember member = CouncilMember.builder()
                .council(council)
                .teacher(teacher)
                .role(role)
                .build();
        councilMemberRepository.save(member);
        council.getMembers().add(member);
    }

    private ReviewCouncilDTO mapToDTO(ReviewCouncil council) {
        Long chairmanId = null;
        String chairmanName = "";
        Long secretaryId = null;
        String secretaryName = "";

        List<Long> memberIds = new ArrayList<>();
        List<ReviewCouncilDTO.CouncilMemberDTO> memberDTOs = new ArrayList<>();

        for (CouncilMember cm : council.getMembers()) {
            memberDTOs.add(ReviewCouncilDTO.CouncilMemberDTO.builder()
                    .teacherId(cm.getTeacher().getId())
                    .teacherCode(cm.getTeacher().getTeacherCode())
                    .fullName(cm.getTeacher().getUser().getFullName())
                    .role(cm.getRole().name())
                    .build());

            if (cm.getRole() == CouncilRole.CHAIRMAN) {
                chairmanId = cm.getTeacher().getId();
                chairmanName = cm.getTeacher().getUser().getFullName();
            } else if (cm.getRole() == CouncilRole.SECRETARY) {
                secretaryId = cm.getTeacher().getId();
                secretaryName = cm.getTeacher().getUser().getFullName();
            } else {
                memberIds.add(cm.getTeacher().getId());
            }
        }

        List<TopicDTO> assignedTopics = councilTopicRepository.findTopicSummariesByCouncilId(council.getId()).stream()
                .map(topic -> TopicDTO.builder()
                        .id(topic.getId())
                        .title(topic.getTitle())
                        .build())
                .collect(Collectors.toList());

        List<Long> assignedTopicIds = assignedTopics.stream().map(TopicDTO::getId).collect(Collectors.toList());

        return ReviewCouncilDTO.builder()
                .id(council.getId())
                .councilCode(council.getCouncilCode())
                .name(council.getName())
                .periodId(council.getPeriod().getId())
                .periodName(council.getPeriod().getName())
                .defenseDate(council.getDefenseDate())
                .location(council.getLocation())
                .chairmanTeacherId(chairmanId)
                .chairmanTeacherName(chairmanName)
                .secretaryTeacherId(secretaryId)
                .secretaryTeacherName(secretaryName)
                .memberTeacherIds(memberIds)
                .members(memberDTOs)
                .assignedTopicIds(assignedTopicIds)
                .assignedTopics(assignedTopics)
                .build();
    }

    private String generateCouncilCode(String councilName) {
        String normalized = Normalizer.normalize(councilName.replace('đ', 'd').replace('Đ', 'D'),
                        Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase()
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        String base = normalized.isBlank() ? "HOI_DONG" : normalized;
        String candidate = base.substring(0, Math.min(base.length(), 30));
        int suffix = 2;
        while (councilRepository.existsByCouncilCode(candidate)) {
            String suffixText = "_" + suffix++;
            candidate = base.substring(0, Math.min(base.length(), 30 - suffixText.length())) + suffixText;
        }
        return candidate;
    }
}
