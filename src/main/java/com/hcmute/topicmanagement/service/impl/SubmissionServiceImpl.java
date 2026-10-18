package com.hcmute.topicmanagement.service.impl;

import com.hcmute.topicmanagement.dto.NotificationDTO;
import com.hcmute.topicmanagement.entity.*;
import com.hcmute.topicmanagement.enums.NotificationType;
import com.hcmute.topicmanagement.enums.PeriodStatus;
import com.hcmute.topicmanagement.enums.PeriodType;
import com.hcmute.topicmanagement.enums.RoleName;
import com.hcmute.topicmanagement.enums.StudentGroupStatus;
import com.hcmute.topicmanagement.enums.TopicRegistrationStatus;
import com.hcmute.topicmanagement.exception.BusinessException;
import com.hcmute.topicmanagement.exception.ResourceNotFoundException;
import com.hcmute.topicmanagement.repository.*;
import com.hcmute.topicmanagement.service.NotificationService;
import com.hcmute.topicmanagement.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final StudentGroupRepository groupRepository;
    private final StudentRepository studentRepository;
    private final TopicRegistrationRepository topicRegistrationRepository;
    private final TopicAdvisorRepository topicAdvisorRepository;
    private final TopicReviewerRepository topicReviewerRepository;
    private final CouncilTopicRepository councilTopicRepository;
    private final CouncilMemberRepository councilMemberRepository;
    private final NotificationService notificationService;

    @Value("${app.upload.dir:${user.home}/uploads/fit-topics/}")
    private String uploadDir;

    @Value("${app.upload.max-file-size:52428800}")
    private long maxFileSize;

    @Override
    public Submission uploadSubmission(Long groupId, MultipartFile reportFile, MultipartFile sourceFile,
                                       String demoUrl, String notes, Long currentStudentId) {
        StudentGroup group = findGroup(groupId);
        if (!group.getLeader().getId().equals(currentStudentId)) {
            throw new AccessDeniedException("Chỉ nhóm trưởng mới được phép nộp báo cáo cho nhóm.");
        }
        PeriodStatus periodStatus = group.getPeriod().getStatus();
        if (periodStatus == PeriodStatus.SUBMISSION_CLOSED) {
            throw new BusinessException("Đợt đã kết thúc nhận bài. Không thể nộp hoặc nộp lại báo cáo.");
        }
        if (periodStatus != PeriodStatus.PHASE2_STUDENT_REG
                && periodStatus != PeriodStatus.EXECUTION) {
            throw new BusinessException("Chỉ được nộp bài trong giai đoạn đăng ký sinh viên hoặc thực hiện đề tài.");
        }
        if (group.getPeriod().getSubmissionDeadline() != null
                && !LocalDateTime.now().isBefore(group.getPeriod().getSubmissionDeadline())) {
            throw new BusinessException("Đã quá hạn nộp bài. Vui lòng liên hệ Trưởng khoa để gia hạn.");
        }
        if (group.getStatus() != StudentGroupStatus.APPROVED
                && group.getStatus() != StudentGroupStatus.SUBMITTED
                && group.getStatus() != StudentGroupStatus.NOT_SUBMITTED) {
            throw new BusinessException("Nhóm phải được duyệt đề tài trước khi nộp kết quả.");
        }
        TopicRegistration registration = topicRegistrationRepository.findByGroupId(groupId)
                .filter(item -> item.getStatus() == TopicRegistrationStatus.APPROVED)
                .orElseThrow(() -> new BusinessException("Đề tài của nhóm chưa được Khoa phê duyệt."));

        validateFile(reportFile, SetOfExtensions.REPORT, true, "Báo cáo");
        boolean requiresSource = group.getPeriod().getPeriodType() == PeriodType.MON_HOC;
        validateFile(sourceFile, SetOfExtensions.ZIP, requiresSource, "Source code");
        validateDemoUrl(demoUrl);

        Student leader = studentRepository.findById(currentStudentId)
                .orElseThrow(() -> new ResourceNotFoundException("Sinh viên", "id", currentStudentId));
        int nextVersion = submissionRepository.findMaxVersionByGroupId(groupId) + 1;
        List<Path> writtenFiles = new ArrayList<>();
        try {
            Path reportPath = saveFile(groupId, nextVersion, reportFile, "report");
            writtenFiles.add(reportPath);
            Path sourcePath = null;
            if (sourceFile != null && !sourceFile.isEmpty()) {
                sourcePath = saveFile(groupId, nextVersion, sourceFile, "source");
                writtenFiles.add(sourcePath);
            }

            Submission submission = Submission.builder()
                    .group(group)
                    .fileName(cleanOriginalName(reportFile.getOriginalFilename()))
                    .filePath(reportPath.toString())
                    .fileSize(reportFile.getSize())
                    .sourceFileName(sourcePath == null ? null : cleanOriginalName(sourceFile.getOriginalFilename()))
                    .sourceFilePath(sourcePath == null ? null : sourcePath.toString())
                    .sourceFileSize(sourcePath == null ? null : sourceFile.getSize())
                    .demoUrl(trimToNull(demoUrl))
                    .versionNumber(nextVersion)
                    .uploadedBy(leader)
                    .notes(trimToNull(notes))
                    .build();
            submission = submissionRepository.save(submission);
            group.setStatus(StudentGroupStatus.SUBMITTED);
            groupRepository.save(group);
            notifyAdvisors(registration, leader, nextVersion);
            return submission;
        } catch (IOException e) {
            deleteFiles(writtenFiles);
            throw new BusinessException("Không thể lưu tệp nộp bài: " + e.getMessage());
        } catch (RuntimeException e) {
            deleteFiles(writtenFiles);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Submission> getSubmissionsByGroup(Long groupId, Long userId, RoleName role,
                                                  Long studentId, Long teacherId) {
        StudentGroup group = findGroup(groupId);
        assertCanView(group, userId, role, studentId, teacherId);
        return submissionRepository.findByGroupIdOrderByVersionNumberDesc(groupId);
    }

    @Override
    @Transactional(readOnly = true)
    public Submission getSubmission(Long submissionId, Long userId, RoleName role,
                                    Long studentId, Long teacherId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Bài nộp", "id", submissionId));
        assertCanView(submission.getGroup(), userId, role, studentId, teacherId);
        return submission;
    }

    @Override
    @Transactional(readOnly = true)
    public Submission getLatestSubmissionByGroup(Long groupId) {
        return submissionRepository.findTopByGroupIdOrderByVersionNumberDesc(groupId).orElse(null);
    }

    private StudentGroup findGroup(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Nhóm sinh viên", "id", groupId));
    }

    private void assertCanView(StudentGroup group, Long userId, RoleName role,
                               Long studentId, Long teacherId) {
        if (role == RoleName.ROLE_ADMIN) {
            return;
        }
        if (role == RoleName.ROLE_STUDENT && studentId != null
                && (group.getLeader().getId().equals(studentId)
                || group.getMembers().stream().anyMatch(member -> member.getStudent().getId().equals(studentId)))) {
            return;
        }
        TopicRegistration registration = topicRegistrationRepository.findByGroupId(group.getId()).orElse(null);
        if (role == RoleName.ROLE_TEACHER && teacherId != null && registration != null) {
            Long topicId = registration.getTopic().getId();
            boolean assigned = topicAdvisorRepository.existsByTopicIdAndTeacherId(topicId, teacherId)
                    || topicReviewerRepository.existsByTopicIdAndTeacherId(topicId, teacherId)
                    || councilTopicRepository.findByTopicId(topicId)
                    .map(assignment -> councilMemberRepository.existsByCouncilIdAndTeacherId(
                            assignment.getCouncil().getId(), teacherId))
                    .orElse(false);
            if (assigned) {
                return;
            }
        }
        throw new AccessDeniedException("Bạn không có quyền xem bài nộp của nhóm này.");
    }

    private void validateFile(MultipartFile file, SetOfExtensions extensions, boolean required, String label) {
        if (file == null || file.isEmpty()) {
            if (required) {
                throw new BusinessException(label + " là tệp bắt buộc.");
            }
            return;
        }
        if (file.getSize() > maxFileSize) {
            throw new BusinessException(label + " vượt quá dung lượng tối đa "
                    + (maxFileSize / (1024 * 1024)) + " MB.");
        }
        String extension = extension(file.getOriginalFilename());
        if (!extensions.values.contains(extension)) {
            throw new BusinessException(label + " sai định dạng. Định dạng cho phép: "
                    + String.join(", ", extensions.values) + ".");
        }
    }

    private void validateDemoUrl(String demoUrl) {
        String value = trimToNull(demoUrl);
        if (value == null) {
            return;
        }
        try {
            URI uri = URI.create(value);
            if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    || uri.getHost() == null) {
                throw new BusinessException("Link demo phải là địa chỉ HTTP hoặc HTTPS hợp lệ.");
            }
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Link demo không hợp lệ.");
        }
    }

    private Path saveFile(Long groupId, int version, MultipartFile file, String kind) throws IOException {
        Path directory = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(directory);
        String extension = extension(file.getOriginalFilename());
        Path destination = directory.resolve("group-" + groupId + "-v" + version + "-"
                + kind + "-" + UUID.randomUUID() + "." + extension).normalize();
        if (!destination.getParent().equals(directory)) {
            throw new BusinessException("Đường dẫn tệp nộp không hợp lệ.");
        }
        try (var inputStream = file.getInputStream()) {
            Files.copy(inputStream, destination);
        }
        return destination;
    }

    private void notifyAdvisors(TopicRegistration registration, Student leader, int version) {
        List<Long> advisorUserIds = topicAdvisorRepository.findByTopicId(registration.getTopic().getId()).stream()
                .map(advisor -> advisor.getTeacher().getUser().getId())
                .distinct()
                .toList();
        if (advisorUserIds.isEmpty()) {
            return;
        }
        notificationService.createNotification(NotificationDTO.builder()
                .title("Nhóm sinh viên đã nộp kết quả đề tài")
                .content("Nhóm " + registration.getGroup().getGroupCode()
                        + " đã nộp kết quả đề tài \"" + registration.getTopic().getTitle()
                        + "\" (phiên bản " + version + ").")
                .type(NotificationType.RESULT)
                .targetRole("INDIVIDUAL")
                .targetUserIds(advisorUserIds)
                .periodId(registration.getGroup().getPeriod().getId())
                .build(), leader.getUser().getId());
    }

    private String extension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String cleanOriginalName(String fileName) {
        if (fileName == null) {
            return "submission";
        }
        return fileName.replace('\\', '/').substring(fileName.replace('\\', '/').lastIndexOf('/') + 1);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private void deleteFiles(List<Path> paths) {
        for (Path path : paths) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                throw new BusinessException("Không thể dọn tệp nộp chưa lưu: " + e.getMessage());
            }
        }
    }

    private enum SetOfExtensions {
        REPORT(List.of("pdf", "docx")),
        ZIP(List.of("zip"));

        private final List<String> values;

        SetOfExtensions(List<String> values) {
            this.values = values;
        }
    }
}
