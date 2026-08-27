package com.hcmute.topicmanagement.service.impl;

import com.hcmute.topicmanagement.entity.Student;
import com.hcmute.topicmanagement.entity.StudentGroup;
import com.hcmute.topicmanagement.entity.Submission;
import com.hcmute.topicmanagement.enums.StudentGroupStatus;
import com.hcmute.topicmanagement.exception.BusinessException;
import com.hcmute.topicmanagement.exception.ResourceNotFoundException;
import com.hcmute.topicmanagement.repository.StudentGroupRepository;
import com.hcmute.topicmanagement.repository.StudentRepository;
import com.hcmute.topicmanagement.repository.SubmissionRepository;
import com.hcmute.topicmanagement.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final StudentGroupRepository groupRepository;
    private final StudentRepository studentRepository;

    @Value("${app.upload.dir:${user.home}/uploads/fit-topics/}")
    private String uploadDir;

    @Override
    public Submission uploadSubmission(Long groupId, MultipartFile file, String notes, Long currentStudentId) {
        if (file.isEmpty()) {
            throw new BusinessException("Vui lòng chọn file báo cáo để tải lên.");
        }

        StudentGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Nhóm sinh viên", "id", groupId));

        // Rule: Only leader can upload
        if (!group.getLeader().getId().equals(currentStudentId)) {
            throw new BusinessException("Chỉ Nhóm trưởng mới được phép nộp báo cáo cho nhóm.");
        }

        Student leader = studentRepository.findById(currentStudentId)
                .orElseThrow(() -> new ResourceNotFoundException("Sinh viên", "id", currentStudentId));

        int nextVersion = submissionRepository.findMaxVersionByGroupId(groupId) + 1;

        try {
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String storedFileName = "Group_" + groupId + "_v" + nextVersion + "_" + UUID.randomUUID().toString().substring(0, 8) + "_" + originalFilename;
            Path filePath = Paths.get(uploadDir, storedFileName);
            Files.copy(file.getInputStream(), filePath);

            Submission submission = Submission.builder()
                    .group(group)
                    .fileName(originalFilename)
                    .filePath(filePath.toString())
                    .fileSize(file.getSize())
                    .versionNumber(nextVersion)
                    .uploadedBy(leader)
                    .notes(notes)
                    .build();

            submission = submissionRepository.save(submission);

            group.setStatus(StudentGroupStatus.SUBMITTED);
            groupRepository.save(group);

            return submission;

        } catch (IOException e) {
            throw new BusinessException("Lỗi khi lưu file báo cáo: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Submission> getSubmissionsByGroup(Long groupId) {
        return submissionRepository.findByGroupIdOrderByVersionNumberDesc(groupId);
    }

    @Override
    @Transactional(readOnly = true)
    public Submission getLatestSubmissionByGroup(Long groupId) {
        return submissionRepository.findTopByGroupIdOrderByVersionNumberDesc(groupId).orElse(null);
    }
}
