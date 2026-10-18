package com.hcmute.topicmanagement.service.impl;

import com.hcmute.topicmanagement.dto.NotificationDTO;
import com.hcmute.topicmanagement.dto.RegistrationPeriodDTO;
import com.hcmute.topicmanagement.entity.RegistrationPeriod;
import com.hcmute.topicmanagement.entity.StudentGroup;
import com.hcmute.topicmanagement.entity.Topic;
import com.hcmute.topicmanagement.enums.NotificationType;
import com.hcmute.topicmanagement.enums.PeriodAction;
import com.hcmute.topicmanagement.enums.PeriodStatus;
import com.hcmute.topicmanagement.enums.PeriodType;
import com.hcmute.topicmanagement.enums.StudentGroupStatus;
import com.hcmute.topicmanagement.enums.TopicStatus;
import com.hcmute.topicmanagement.exception.BusinessException;
import com.hcmute.topicmanagement.exception.ResourceNotFoundException;
import com.hcmute.topicmanagement.repository.RegistrationPeriodRepository;
import com.hcmute.topicmanagement.repository.CouncilTopicRepository;
import com.hcmute.topicmanagement.repository.StudentGroupRepository;
import com.hcmute.topicmanagement.repository.SubmissionRepository;
import com.hcmute.topicmanagement.repository.TopicRegistrationRepository;
import com.hcmute.topicmanagement.repository.TopicRepository;
import com.hcmute.topicmanagement.repository.TopicReviewerRepository;
import com.hcmute.topicmanagement.service.NotificationService;
import com.hcmute.topicmanagement.service.RegistrationPeriodService;
import com.hcmute.topicmanagement.service.ScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RegistrationPeriodServiceImpl implements RegistrationPeriodService {

    private final RegistrationPeriodRepository periodRepository;
    private final TopicRepository topicRepository;
    private final TopicRegistrationRepository topicRegistrationRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final SubmissionRepository submissionRepository;
    private final TopicReviewerRepository topicReviewerRepository;
    private final CouncilTopicRepository councilTopicRepository;
    private final NotificationService notificationService;
    private final ScoreService scoreService;

    @Value("${app.period.max-extensions:2}")
    private int maxExtensions = 2;

    private static final DateTimeFormatter NOTICE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    @Override
    public RegistrationPeriodDTO createPeriod(RegistrationPeriodDTO dto) {
        validatePeriodRules(dto, true);

        RegistrationPeriod period = RegistrationPeriod.builder()
                .name(dto.getName())
                .periodType(dto.getPeriodType())
                .teacherStartDate(dto.getTeacherStartDate())
                .teacherEndDate(dto.getTeacherEndDate())
                .approvalStartDate(dto.getApprovalStartDate())
                .approvalEndDate(dto.getApprovalEndDate())
                .studentStartDate(dto.getStudentStartDate())
                .studentEndDate(dto.getStudentEndDate())
                .submissionDeadline(dto.getSubmissionDeadline())
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

        boolean teacherWindowChanged = !dto.getTeacherStartDate().equals(period.getTeacherStartDate())
                || !dto.getTeacherEndDate().equals(period.getTeacherEndDate());
        if (teacherWindowChanged && topicRegistrationRepository.existsByGroupPeriodId(id)) {
            throw new BusinessException("Không thể sửa giai đoạn GV sau khi đã có nhóm đăng ký đề tài.");
        }
        if (period.getStatus() != PeriodStatus.DRAFT && scheduleChanged(period, dto)) {
            throw new BusinessException("Sau khi mở đợt, chỉ được thay đổi thời gian bằng thao tác gia hạn trong menu trạng thái.");
        }
        validatePeriodRules(dto, !dto.getTeacherStartDate().equals(period.getTeacherStartDate()));

        period.setName(dto.getName());
        period.setPeriodType(dto.getPeriodType());
        period.setTeacherStartDate(dto.getTeacherStartDate());
        period.setTeacherEndDate(dto.getTeacherEndDate());
        period.setApprovalStartDate(dto.getApprovalStartDate());
        period.setApprovalEndDate(dto.getApprovalEndDate());
        period.setStudentStartDate(dto.getStudentStartDate());
        period.setStudentEndDate(dto.getStudentEndDate());
        period.setSubmissionDeadline(dto.getSubmissionDeadline());
        period.setReviewSubmissionDeadline(dto.getReviewSubmissionDeadline());
        period.setCouncilReportDate(dto.getCouncilReportDate());

        period = periodRepository.save(period);
        return mapToDTO(period);
    }

    @Override
    public void changeStatus(Long id, PeriodStatus newStatus) {
        RegistrationPeriod period = periodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đợt đăng ký", "id", id));

        if (newStatus == null) {
            throw new BusinessException("Vui lòng chọn trạng thái của đợt đăng ký.");
        }

        period.setStatus(newStatus);
        period.setTeacherRegistrationManuallyOpened(newStatus == PeriodStatus.PHASE1_TEACHER_REG);
        period.setStudentRegistrationManuallyOpened(newStatus == PeriodStatus.PHASE2_STUDENT_REG);
        period.setStatusManuallyControlled(true);
        periodRepository.save(period);
    }

    @Override
    public void resumeAutomaticStatus(Long id) {
        RegistrationPeriod period = periodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đợt đăng ký", "id", id));
        period.setStatusManuallyControlled(false);
        period.setTeacherRegistrationManuallyOpened(false);
        period.setStudentRegistrationManuallyOpened(false);
        periodRepository.save(period);
    }

    @Override
    public void performAction(Long id, PeriodAction action, LocalDateTime newEndDate, Long actorUserId) {
        RegistrationPeriod period = periodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đợt đăng ký", "id", id));
        String notificationRole;
        String notificationContent;
        List<Long> individualRecipients = List.of();
        switch (action) {
            case OPEN_TEACHER_REGISTRATION -> {
                requireStatus(period, PeriodStatus.DRAFT);
                period.setTeacherRegistrationManuallyOpened(true);
                period.setStatus(PeriodStatus.PHASE1_TEACHER_REG);
                notificationRole = "ROLE_TEACHER";
                notificationContent = "Giai đoạn đăng ký đề tài của đợt \"" + period.getName() + "\" đã mở.";
            }
            case CLOSE_TEACHER_REGISTRATION -> {
                requireStatus(period, PeriodStatus.PHASE1_TEACHER_REG);
                if (topicRepository.countByPeriodId(id) == 0) {
                    throw new BusinessException("Chưa có đề tài; hãy gia hạn GV đăng ký hoặc hủy đợt.");
                }
                period.setTeacherRegistrationManuallyOpened(false);
                period.setStatus(PeriodStatus.PHASE1_DEAN_REVIEW);
                notificationRole = "ROLE_TEACHER";
                notificationContent = "Giai đoạn GV đăng ký đề tài đợt \"" + period.getName() + "\" đã đóng.";
            }
            case EXTEND_TEACHER_REGISTRATION -> {
                requireStatus(period, PeriodStatus.PHASE1_TEACHER_REG,
                        PeriodStatus.PHASE1_DEAN_REVIEW, PeriodStatus.INSUFFICIENT_TOPICS);
                ensureTeacherWindowEditable(period, id);
                extendPeriod(period, PeriodExtension.TEACHER, newEndDate);
                period.setTeacherRegistrationManuallyOpened(false);
                period.setStatus(PeriodStatus.PHASE1_TEACHER_REG);
                notificationRole = "ROLE_TEACHER";
                notificationContent = "Giai đoạn GV đăng ký đề tài đợt \"" + period.getName()
                        + "\" đã được gia hạn đến " + newEndDate.format(NOTICE_DATE_FORMAT) + ".";
            }
            case PUBLISH_TOPICS -> {
                requireStatus(period, PeriodStatus.PHASE1_DEAN_REVIEW, PeriodStatus.INSUFFICIENT_TOPICS);
                publishApprovedTopics(period);
                notificationRole = "ROLE_STUDENT";
                notificationContent = "Danh sách đề tài đợt \"" + period.getName() + "\" đã được công bố.";
            }
            case REOPEN_TEACHER_REGISTRATION -> {
                requireStatus(period, PeriodStatus.PHASE1_DEAN_REVIEW, PeriodStatus.INSUFFICIENT_TOPICS);
                ensureTeacherWindowEditable(period, id);
                period.setTeacherRegistrationManuallyOpened(true);
                period.setStatus(PeriodStatus.PHASE1_TEACHER_REG);
                notificationRole = "ROLE_TEACHER";
                notificationContent = "Giai đoạn GV đăng ký đề tài đợt \"" + period.getName()
                        + "\" đã mở lại.";
            }
            case OPEN_STUDENT_REGISTRATION, OPEN_STUDENT_REGISTRATION_EARLY -> {
                if (action == PeriodAction.OPEN_STUDENT_REGISTRATION_EARLY) {
                    requireStatus(period, PeriodStatus.PHASE1_TEACHER_REG);
                } else {
                    requireStatus(period, PeriodStatus.PUBLISHED_TOPICS);
                }
                long approvedCount = topicRepository.countByPeriodIdAndStatus(id, TopicStatus.APPROVED);
                if (approvedCount == 0 && topicRepository.countByPeriodIdAndStatus(id, TopicStatus.PUBLISHED) == 0) {
                    throw new BusinessException("Chưa có đề tài APPROVED để mở đăng ký sinh viên.");
                }
                publishApprovedTopics(period);
                period.setStudentRegistrationManuallyOpened(true);
                period.setStatus(PeriodStatus.PHASE2_STUDENT_REG);
                notificationRole = "ROLE_STUDENT";
                notificationContent = "Đợt \"" + period.getName()
                        + "\" đã mở đăng ký đề tài cho sinh viên.";
            }
            case CLOSE_STUDENT_REGISTRATION -> {
                requireStatus(period, PeriodStatus.PHASE2_STUDENT_REG);
                period.setStudentRegistrationManuallyOpened(false);
                period.setStatus(PeriodStatus.EXECUTION);
                notificationRole = "ROLE_STUDENT";
                notificationContent = "Giai đoạn đăng ký đề tài đợt \"" + period.getName()
                        + "\" đã đóng; các nhóm bắt đầu thực hiện đề tài.";
            }
            case EXTEND_STUDENT_REGISTRATION -> {
                requireStatus(period, PeriodStatus.PHASE2_STUDENT_REG);
                extendPeriod(period, PeriodExtension.STUDENT, newEndDate);
                period.setStudentRegistrationManuallyOpened(false);
                notificationRole = "ROLE_STUDENT";
                notificationContent = "Giai đoạn sinh viên đăng ký đợt \"" + period.getName()
                        + "\" đã được gia hạn đến " + newEndDate.format(NOTICE_DATE_FORMAT) + ".";
            }
            case REOPEN_STUDENT_REGISTRATION -> {
                requireStatus(period, PeriodStatus.EXECUTION);
                if (topicRepository.countByPeriodIdAndStatus(id, TopicStatus.PUBLISHED) == 0
                        && topicRepository.countByPeriodIdAndStatus(id, TopicStatus.APPROVED) == 0) {
                    throw new BusinessException("Chưa có đề tài được công bố để mở lại đăng ký sinh viên.");
                }
                boolean hasSubmission = studentGroupRepository.findByPeriodId(id).stream()
                        .anyMatch(group -> submissionRepository.existsByGroupId(group.getId()));
                if (hasSubmission) {
                    throw new BusinessException("Không thể mở lại đăng ký sinh viên sau khi nhóm đã nộp báo cáo.");
                }
                period.setStudentRegistrationManuallyOpened(true);
                period.setStatus(PeriodStatus.PHASE2_STUDENT_REG);
                notificationRole = "ROLE_STUDENT";
                notificationContent = "Đợt \"" + period.getName()
                        + "\" đã mở lại đăng ký đề tài cho sinh viên.";
            }
            case EXTEND_SUBMISSION_DEADLINE -> {
                requireStatus(period, PeriodStatus.EXECUTION, PeriodStatus.DEFENSE_EVALUATION);
                extendPeriod(period, PeriodExtension.SUBMISSION, newEndDate);
                if (period.getStatus() == PeriodStatus.DEFENSE_EVALUATION) {
                    period.setStatus(PeriodStatus.EXECUTION);
                }
                individualRecipients = studentGroupRepository.findByPeriodId(id).stream()
                        .map(group -> group.getLeader().getUser().getId()).distinct().toList();
                notificationRole = individualRecipients.isEmpty() ? "ROLE_STUDENT" : "INDIVIDUAL";
                notificationContent = "Hạn nộp báo cáo đợt \"" + period.getName()
                        + "\" đã được gia hạn đến " + newEndDate.format(NOTICE_DATE_FORMAT) + ".";
            }
            case EXTEND_SCORING_DEADLINE -> {
                requireStatus(period, PeriodStatus.DEFENSE_EVALUATION);
                extendPeriod(period, PeriodExtension.SCORING, newEndDate);
                period.setScoringReminderSentAt(null);
                individualRecipients = scoringTeacherUserIds(period);
                notificationRole = individualRecipients.isEmpty() ? "ROLE_TEACHER" : "INDIVIDUAL";
                notificationContent = "Hạn nộp điểm đợt \"" + period.getName()
                        + "\" đã được gia hạn đến " + newEndDate.format(NOTICE_DATE_FORMAT) + ".";
            }
            case PUBLISH_RESULTS -> {
                requireStatus(period, PeriodStatus.DEFENSE_EVALUATION);
                scoreService.publishResults(id);
                period.setStatus(PeriodStatus.COMPLETED);
                notificationRole = "ROLE_STUDENT";
                notificationContent = "Kết quả đề tài đợt \"" + period.getName() + "\" đã được công bố.";
            }
            case CANCEL_PERIOD -> {
                period.setStatus(PeriodStatus.CANCELLED);
                period.setStatusManuallyControlled(true);
                period.setTeacherRegistrationManuallyOpened(false);
                period.setStudentRegistrationManuallyOpened(false);
                notificationRole = "ALL";
                notificationContent = "Đợt đăng ký \"" + period.getName() + "\" đã bị hủy.";
            }
            case UNPUBLISH_RESULTS -> {
                requireStatus(period, PeriodStatus.COMPLETED);
                scoreService.unpublishResults(id);
                period.setStatus(PeriodStatus.DEFENSE_EVALUATION);
                notificationRole = "ROLE_STUDENT";
                notificationContent = "Kết quả đợt \"" + period.getName()
                        + "\" đã được tạm ẩn để điều chỉnh.";
            }
            default -> throw new BusinessException("Thao tác đợt đăng ký không được hỗ trợ.");
        }
        periodRepository.save(period);
        sendPeriodNotification(period, actorUserId, notificationRole, notificationContent, individualRecipients);
    }

    @Override
    @Scheduled(fixedDelayString = "${app.period.automation-interval-ms:60000}")
    public void advancePeriodsAutomatically() {
        LocalDateTime now = LocalDateTime.now();
        for (RegistrationPeriod period : periodRepository.findAll()) {
            if (period.isStatusManuallyControlled()
                    && period.getStatus() != PeriodStatus.SUBMISSION_CLOSED) {
                continue;
            }
            switch (period.getStatus()) {
                case DRAFT -> {
                    if (!now.isBefore(period.getTeacherStartDate())) {
                        period.setStatus(PeriodStatus.PHASE1_TEACHER_REG);
                        periodRepository.save(period);
                    }
                }
                case PHASE1_TEACHER_REG -> {
                    if (!period.isTeacherRegistrationManuallyOpened()
                            && !now.isBefore(period.getTeacherEndDate())) {
                        period.setStatus(topicRepository.countByPeriodId(period.getId()) == 0
                                ? PeriodStatus.INSUFFICIENT_TOPICS : PeriodStatus.PHASE1_DEAN_REVIEW);
                        periodRepository.save(period);
                    }
                }
                case PHASE1_DEAN_REVIEW -> {
                    if (period.getApprovalEndDate() != null && !now.isBefore(period.getApprovalEndDate())) {
                        if (topicRepository.countByPeriodIdAndStatus(period.getId(), TopicStatus.APPROVED) == 0) {
                            period.setStatus(PeriodStatus.INSUFFICIENT_TOPICS);
                        } else {
                            publishApprovedTopics(period);
                        }
                        periodRepository.save(period);
                    }
                }
                case PUBLISHED_TOPICS -> {
                    if (!now.isBefore(period.getStudentStartDate())) {
                        period.setStatus(PeriodStatus.PHASE2_STUDENT_REG);
                        periodRepository.save(period);
                    }
                }
                case PHASE2_STUDENT_REG -> {
                    if (!period.isStudentRegistrationManuallyOpened()
                            && !now.isBefore(period.getStudentEndDate())) {
                        period.setStatus(PeriodStatus.EXECUTION);
                        periodRepository.save(period);
                    }
                }
                case EXECUTION -> {
                    LocalDateTime deadline = period.getSubmissionDeadline() != null
                            ? period.getSubmissionDeadline() : period.getReviewSubmissionDeadline();
                    if (deadline != null && !now.isBefore(deadline)) {
                        movePeriodToDefenseEvaluation(period);
                    }
                }
                case SUBMISSION_CLOSED -> {
                    LocalDateTime deadline = period.getSubmissionDeadline() != null
                            ? period.getSubmissionDeadline() : period.getReviewSubmissionDeadline();
                    if (deadline != null && !now.isBefore(deadline)) {
                        movePeriodToDefenseEvaluation(period);
                    }
                }
                default -> {
                }
            }
        }
    }

    private void movePeriodToDefenseEvaluation(RegistrationPeriod period) {
        for (StudentGroup group : studentGroupRepository.findByPeriodIdAndStatus(
                period.getId(), StudentGroupStatus.APPROVED)) {
            if (!submissionRepository.existsByGroupId(group.getId())) {
                group.setStatus(StudentGroupStatus.NOT_SUBMITTED);
                studentGroupRepository.save(group);
            }
        }
        period.setStatus(PeriodStatus.DEFENSE_EVALUATION);
        period.setStatusManuallyControlled(false);
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
        LocalDateTime now = LocalDateTime.now();
        return periodRepository.findAll().stream()
                .filter(p -> p.getStatus() == PeriodStatus.PHASE2_STUDENT_REG)
                .filter(p -> p.isStudentRegistrationManuallyOpened()
                        || (!now.isBefore(p.getStudentStartDate())
                        && !now.isAfter(p.getStudentEndDate()))
                )
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationPeriodDTO> getPeriodsForStudentGroupManagement() {
        LocalDateTime now = LocalDateTime.now();
        return periodRepository.findAll().stream()
                .filter(period -> period.getStatus() == PeriodStatus.EXECUTION
                        || period.getStatus() == PeriodStatus.SUBMISSION_CLOSED
                        || (period.getStatus() == PeriodStatus.PHASE2_STUDENT_REG
                        && (period.isStudentRegistrationManuallyOpened()
                        || (!now.isBefore(period.getStudentStartDate())
                        && !now.isAfter(period.getStudentEndDate())))))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationPeriodDTO> getActivePeriodsForTeacher() {
        LocalDateTime now = LocalDateTime.now();
        return periodRepository.findAll().stream()
                .filter(p -> p.getStatus() == PeriodStatus.PHASE1_TEACHER_REG
                        || p.getStatus() == PeriodStatus.PHASE2_STUDENT_REG)
                .filter(p -> p.isTeacherRegistrationManuallyOpened()
                        || (!now.isBefore(p.getTeacherStartDate())
                        && !now.isAfter(p.getTeacherEndDate())))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private void validatePeriodRules(RegistrationPeriodDTO dto, boolean validateFutureStart) {
        if (validateFutureStart) {
            LocalDateTime now = LocalDateTime.now();
            if (dto.getTeacherStartDate().isBefore(now)
                    || dto.getTeacherEndDate().isBefore(now)
                    || dto.getApprovalStartDate().isBefore(now)
                    || dto.getApprovalEndDate().isBefore(now)
                    || dto.getStudentStartDate().isBefore(now)
                    || dto.getStudentEndDate().isBefore(now)
                    || (dto.getSubmissionDeadline() != null && dto.getSubmissionDeadline().isBefore(now))
                    || (dto.getReviewSubmissionDeadline() != null && dto.getReviewSubmissionDeadline().isBefore(now))
                    || (dto.getCouncilReportDate() != null && dto.getCouncilReportDate().isBefore(now))) {
                throw new BusinessException("Các mốc thời gian của đợt mới không được ở quá khứ.");
            }
        }
        if (!dto.getTeacherStartDate().isBefore(dto.getTeacherEndDate())
                || !dto.getApprovalStartDate().isBefore(dto.getApprovalEndDate())
                || !dto.getStudentStartDate().isBefore(dto.getStudentEndDate())) {
            throw new BusinessException("Thời điểm bắt đầu phải trước thời điểm kết thúc trong từng giai đoạn.");
        }
        validateScheduleOrder(dto.getTeacherEndDate(), dto.getApprovalStartDate(), dto.getApprovalEndDate(),
                dto.getStudentStartDate(), dto.getStudentEndDate(), dto.getSubmissionDeadline(),
                dto.getReviewSubmissionDeadline(), dto.getCouncilReportDate());
        if (dto.getSubmissionDeadline() == null) {
            throw new BusinessException("Hạn nộp báo cáo của nhóm là bắt buộc.");
        }
        if (dto.getReviewSubmissionDeadline() == null) {
            throw new BusinessException("Hạn chót nộp điểm là bắt buộc đối với mọi loại đợt.");
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
                .approvalStartDate(period.getApprovalStartDate())
                .approvalEndDate(period.getApprovalEndDate())
                .studentStartDate(period.getStudentStartDate())
                .studentEndDate(period.getStudentEndDate())
                .submissionDeadline(period.getSubmissionDeadline())
                .reviewSubmissionDeadline(period.getReviewSubmissionDeadline())
                .councilReportDate(period.getCouncilReportDate())
                .status(period.getStatus())
                .statusManuallyControlled(period.isStatusManuallyControlled())
                .build();
    }

    private void requireStatus(RegistrationPeriod period, PeriodStatus... allowedStatuses) {
        for (PeriodStatus allowedStatus : allowedStatuses) {
            if (period.getStatus() == allowedStatus) {
                return;
            }
        }
        throw new BusinessException("Thao tác này không khả dụng khi đợt đang ở trạng thái "
                + period.getStatus().getDisplayName() + ".");
    }

    private boolean scheduleChanged(RegistrationPeriod period, RegistrationPeriodDTO dto) {
        return !period.getTeacherStartDate().equals(dto.getTeacherStartDate())
                || !period.getTeacherEndDate().equals(dto.getTeacherEndDate())
                || !period.getApprovalStartDate().equals(dto.getApprovalStartDate())
                || !period.getApprovalEndDate().equals(dto.getApprovalEndDate())
                || !period.getStudentStartDate().equals(dto.getStudentStartDate())
                || !period.getStudentEndDate().equals(dto.getStudentEndDate())
                || !java.util.Objects.equals(period.getSubmissionDeadline(), dto.getSubmissionDeadline())
                || !java.util.Objects.equals(period.getReviewSubmissionDeadline(), dto.getReviewSubmissionDeadline())
                || !java.util.Objects.equals(period.getCouncilReportDate(), dto.getCouncilReportDate());
    }

    private void ensureTeacherWindowEditable(RegistrationPeriod period, Long periodId) {
        if (topicRegistrationRepository.existsByGroupPeriodId(periodId)
                || period.getStatus() == PeriodStatus.PUBLISHED_TOPICS
                || period.getStatus() == PeriodStatus.PHASE2_STUDENT_REG
                || period.getStatus() == PeriodStatus.EXECUTION
                || period.getStatus() == PeriodStatus.DEFENSE_EVALUATION
                || period.getStatus() == PeriodStatus.COMPLETED) {
            throw new BusinessException("Không thể thay đổi giai đoạn GV sau khi đã công bố đề tài hoặc có SV đăng ký.");
        }
    }

    private void extendPeriod(RegistrationPeriod period, PeriodExtension extension, LocalDateTime newEndDate) {
        if (newEndDate == null || !newEndDate.isAfter(LocalDateTime.now())) {
            throw new BusinessException("Mốc thời gian mới phải ở tương lai theo giờ máy chủ.");
        }
        int extensionCount = switch (extension) {
            case TEACHER -> period.getTeacherExtensionCount();
            case STUDENT -> period.getStudentExtensionCount();
            case SUBMISSION -> period.getSubmissionExtensionCount();
            case SCORING -> period.getScoringExtensionCount();
        };
        if (extensionCount >= maxExtensions) {
            throw new BusinessException("Giai đoạn này đã được gia hạn tối đa " + maxExtensions + " lần.");
        }
        LocalDateTime oldEndDate = switch (extension) {
            case TEACHER -> period.getTeacherEndDate();
            case STUDENT -> period.getStudentEndDate();
            case SUBMISSION -> period.getSubmissionDeadline() == null
                    ? period.getStudentEndDate() : period.getSubmissionDeadline();
            case SCORING -> period.getReviewSubmissionDeadline();
        };
        if (oldEndDate == null || !newEndDate.isAfter(oldEndDate)) {
            throw new BusinessException("Ngày kết thúc gia hạn phải sau ngày kết thúc hiện tại.");
        }
        Duration delta = Duration.between(oldEndDate, newEndDate);
        switch (extension) {
            case TEACHER -> {
                period.setTeacherEndDate(newEndDate);
                period.setApprovalStartDate(shift(period.getApprovalStartDate(), delta));
                period.setApprovalEndDate(shift(period.getApprovalEndDate(), delta));
                period.setStudentStartDate(shift(period.getStudentStartDate(), delta));
                period.setStudentEndDate(shift(period.getStudentEndDate(), delta));
                period.setSubmissionDeadline(shift(period.getSubmissionDeadline(), delta));
                period.setReviewSubmissionDeadline(shift(period.getReviewSubmissionDeadline(), delta));
                period.setCouncilReportDate(shift(period.getCouncilReportDate(), delta));
                period.setTeacherExtensionCount(extensionCount + 1);
            }
            case STUDENT -> {
                period.setStudentEndDate(newEndDate);
                period.setSubmissionDeadline(shift(period.getSubmissionDeadline(), delta));
                period.setReviewSubmissionDeadline(shift(period.getReviewSubmissionDeadline(), delta));
                period.setCouncilReportDate(shift(period.getCouncilReportDate(), delta));
                period.setStudentExtensionCount(extensionCount + 1);
            }
            case SUBMISSION -> {
                period.setSubmissionDeadline(newEndDate);
                period.setReviewSubmissionDeadline(shift(period.getReviewSubmissionDeadline(), delta));
                period.setCouncilReportDate(shift(period.getCouncilReportDate(), delta));
                period.setSubmissionExtensionCount(extensionCount + 1);
            }
            case SCORING -> {
                period.setReviewSubmissionDeadline(newEndDate);
                period.setCouncilReportDate(shift(period.getCouncilReportDate(), delta));
                period.setScoringExtensionCount(extensionCount + 1);
            }
        }
        validateScheduleOrder(period.getTeacherEndDate(), period.getApprovalStartDate(), period.getApprovalEndDate(),
                period.getStudentStartDate(), period.getStudentEndDate(), period.getSubmissionDeadline(),
                period.getReviewSubmissionDeadline(), period.getCouncilReportDate());
    }

    private LocalDateTime shift(LocalDateTime value, Duration delta) {
        return value == null ? null : value.plus(delta);
    }

    private void validateScheduleOrder(LocalDateTime teacherEnd, LocalDateTime approvalStart,
                                       LocalDateTime approvalEnd, LocalDateTime studentStart,
                                       LocalDateTime studentEnd, LocalDateTime submissionDeadline,
                                       LocalDateTime scoringDeadline, LocalDateTime councilDate) {
        if (!teacherEnd.isBefore(approvalStart) || !approvalEnd.isBefore(studentStart)
                || !studentEnd.isBefore(submissionDeadline == null ? scoringDeadline == null
                    ? councilDate == null ? LocalDateTime.MAX : councilDate
                    : scoringDeadline : submissionDeadline)
                || (submissionDeadline != null && scoringDeadline != null
                    && !submissionDeadline.isBefore(scoringDeadline))
                || (scoringDeadline != null && councilDate != null && !scoringDeadline.isBefore(councilDate))) {
            throw new BusinessException("Các mốc phải theo thứ tự: kết thúc GV < bắt đầu duyệt < kết thúc duyệt "
                    + "< bắt đầu SV < kết thúc SV < hạn nộp bài < hạn nộp điểm < ngày hội đồng.");
        }
    }

    private void publishApprovedTopics(RegistrationPeriod period) {
        List<Topic> approvedTopics = topicRepository.findByPeriodIdAndStatus(period.getId(), TopicStatus.APPROVED);
        if (approvedTopics.isEmpty()
                && topicRepository.countByPeriodIdAndStatus(period.getId(), TopicStatus.PUBLISHED) == 0) {
            throw new BusinessException("Chưa có đề tài APPROVED để công bố.");
        }
        approvedTopics.forEach(topic -> {
            topic.setStatus(TopicStatus.PUBLISHED);
            topicRepository.save(topic);
        });
        period.setStatus(PeriodStatus.PUBLISHED_TOPICS);
    }

    private void sendPeriodNotification(RegistrationPeriod period, Long actorUserId,
                                        String targetRole, String content, List<Long> recipientIds) {
        if ("INDIVIDUAL".equals(targetRole) && recipientIds.isEmpty()) {
            return;
        }
        notificationService.createNotification(NotificationDTO.builder()
                .title("Cập nhật đợt đăng ký")
                .content(content)
                .type(NotificationType.PERIOD)
                .targetRole(targetRole)
                .targetUserIds(recipientIds)
                .periodId(period.getId())
                .build(), actorUserId);
    }

    private List<Long> scoringTeacherUserIds(RegistrationPeriod period) {
        return topicRegistrationRepository.findByGroupPeriodId(period.getId()).stream()
                .flatMap(registration -> {
                    Long topicId = registration.getTopic().getId();
                    if (period.getPeriodType() == PeriodType.KLTN) {
                        return councilTopicRepository.findByTopicId(topicId).stream()
                                .flatMap(assignment -> assignment.getCouncil().getMembers().stream())
                                .map(member -> member.getTeacher().getUser().getId());
                    }
                    return topicReviewerRepository.findByTopicId(topicId).stream()
                            .map(reviewer -> reviewer.getTeacher().getUser().getId());
                })
                .distinct()
                .toList();
    }

    private enum PeriodExtension {
        TEACHER,
        STUDENT,
        SUBMISSION,
        SCORING
    }
}
