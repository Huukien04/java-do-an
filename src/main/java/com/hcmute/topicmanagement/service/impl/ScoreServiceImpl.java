package com.hcmute.topicmanagement.service.impl;

import com.hcmute.topicmanagement.dto.ScoreDTO;
import com.hcmute.topicmanagement.dto.ScoreProgressDTO;
import com.hcmute.topicmanagement.dto.NotificationDTO;
import com.hcmute.topicmanagement.entity.*;
import com.hcmute.topicmanagement.enums.CouncilRole;
import com.hcmute.topicmanagement.enums.EvaluationStatus;
import com.hcmute.topicmanagement.enums.PeriodType;
import com.hcmute.topicmanagement.enums.NotificationType;
import com.hcmute.topicmanagement.enums.PeriodStatus;
import com.hcmute.topicmanagement.enums.RoleName;
import com.hcmute.topicmanagement.enums.StudentGroupStatus;
import com.hcmute.topicmanagement.enums.TopicStatus;
import com.hcmute.topicmanagement.exception.BusinessException;
import com.hcmute.topicmanagement.exception.ResourceNotFoundException;
import com.hcmute.topicmanagement.repository.*;
import com.hcmute.topicmanagement.service.ScoreService;
import com.hcmute.topicmanagement.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ScoreServiceImpl implements ScoreService {

    private final ScoreRepository scoreRepository;
    private final FinalResultRepository finalResultRepository;
    private final TopicRepository topicRepository;
    private final TeacherRepository teacherRepository;
    private final TopicAdvisorRepository topicAdvisorRepository;
    private final TopicReviewerRepository topicReviewerRepository;
    private final CouncilTopicRepository councilTopicRepository;
    private final CouncilMemberRepository councilMemberRepository;
    private final TopicRegistrationRepository topicRegistrationRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final RegistrationPeriodRepository registrationPeriodRepository;
    private final NotificationService notificationService;

    @Override
    public ScoreDTO submitScore(ScoreDTO dto, Long currentTeacherId) {
        Topic topic = findTopic(dto.getTopicId());
        requireSubmittedTopic(topic);
        if (finalResultRepository.findByTopicId(topic.getId()).filter(FinalResult::isPublished).isPresent()) {
            throw new BusinessException("Kết quả đã công bố; điểm đã bị khóa. Trưởng khoa phải hủy công bố trước khi sửa.");
        }
        if (topicAdvisorRepository.existsByTopicIdAndTeacherId(topic.getId(), currentTeacherId)) {
            throw new BusinessException("Giảng viên không được chấm đề tài mà mình đang hướng dẫn.");
        }
        if (!isAssignedScorer(topic, currentTeacherId)) {
            throw new AccessDeniedException("Bạn không được phân công chấm đề tài này.");
        }
        if (topic.getPeriod().getReviewSubmissionDeadline() != null
                && !LocalDateTime.now().isBefore(topic.getPeriod().getReviewSubmissionDeadline())) {
            throw new BusinessException("Đã quá hạn nộp điểm. Trưởng khoa cần gia hạn thời gian chấm điểm.");
        }

        Teacher teacher = teacherRepository.findById(currentTeacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Giảng viên", "id", currentTeacherId));
        int criteriaCount = isThesis(topic) ? 4 : 3;
        validateCriteria(dto, criteriaCount);
        BigDecimal scoreValue = averageCriteria(dto, criteriaCount);

        Score score = scoreRepository.findByTopicIdAndTeacherId(topic.getId(), currentTeacherId)
                .orElseGet(() -> Score.builder().topic(topic).teacher(teacher).build());
        score.setContentScore(dto.getContentScore());
        score.setProductScore(dto.getProductScore());
        score.setReportScore(dto.getReportScore());
        score.setPresentationScore(isThesis(topic) ? dto.getPresentationScore() : null);
        score.setScoreValue(scoreValue);
        score.setComments(dto.getComments());
        score = scoreRepository.save(score);

        topic.setStatus(TopicStatus.SCORING);
        topicRepository.save(topic);

        ScoreProgressDTO progress = getScoringProgress(topic.getId());
        if (isThesis(topic)) {
            finalResultRepository.findByTopicId(topic.getId())
                    .filter(result -> !result.isPublished())
                    .ifPresent(finalResultRepository::delete);
        } else if (progress.isComplete() || progress.isDeadlinePassed()) {
            calculateAndSaveFinalResult(topic.getId(), null);
        } else if (!progress.isComplete()) {
            finalResultRepository.findByTopicId(topic.getId())
                    .filter(result -> !result.isPublished())
                    .ifPresent(finalResultRepository::delete);
        }
        return mapToDTO(score);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScoreDTO> getScoresByTopic(Long topicId) {
        return scoreRepository.findByTopicId(topicId).stream().map(this::mapToDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ScoreProgressDTO getScoringProgress(Long topicId) {
        Topic topic = findTopic(topicId);
        List<Teacher> requiredTeachers = getRequiredTeachers(topic);
        List<Score> assignedScores = scoreRepository.findByTopicId(topicId).stream()
                .filter(score -> requiredTeachers.stream()
                        .anyMatch(teacher -> teacher.getId().equals(score.getTeacher().getId())))
                .toList();
        Set<Long> scoredTeacherIds = assignedScores.stream()
                .map(score -> score.getTeacher().getId())
                .collect(Collectors.toSet());
        List<String> pendingTeacherNames = requiredTeachers.stream()
                .filter(teacher -> !scoredTeacherIds.contains(teacher.getId()))
                .map(teacher -> teacher.getUser().getFullName())
                .toList();
        boolean deadlinePassed = topic.getPeriod().getReviewSubmissionDeadline() != null
                && !LocalDateTime.now().isBefore(topic.getPeriod().getReviewSubmissionDeadline());
        boolean spreadWarning = assignedScores.size() > 1 && scoreSpreadExceedsThreshold(assignedScores);

        return ScoreProgressDTO.builder()
                .requiredCount(requiredTeachers.size())
                .submittedCount(assignedScores.size())
                .complete(!requiredTeachers.isEmpty() && pendingTeacherNames.isEmpty())
                .pendingTeacherNames(pendingTeacherNames)
                .scoreSpreadWarning(spreadWarning)
                .deadlinePassed(deadlinePassed)
                .build();
    }

    @Override
    public FinalResult confirmFinalResult(Long topicId, String chairmanComments, Long currentTeacherId) {
        Topic topic = findTopic(topicId);
        if (!isThesis(topic)) {
            throw new BusinessException("Chỉ đề tài KLTN cần Chủ tịch hội đồng xác nhận kết quả.");
        }
        CouncilTopic assignment = councilTopicRepository.findByTopicId(topicId)
                .orElseThrow(() -> new BusinessException("Đề tài chưa được gán vào hội đồng KLTN."));
        if (!councilMemberRepository.existsByCouncilIdAndTeacherIdAndRole(
                assignment.getCouncil().getId(), currentTeacherId, CouncilRole.CHAIRMAN)) {
            throw new AccessDeniedException("Chỉ Chủ tịch hội đồng được xác nhận kết quả đề tài.");
        }
        ScoreProgressDTO progress = getScoringProgress(topicId);
        if (!progress.isComplete() && !progress.isDeadlinePassed()) {
            throw new BusinessException("Chưa thể xác nhận điểm trước hạn: còn "
                    + progress.getPendingTeacherNames().size() + " thành viên chưa chấm.");
        }
        if (progress.getSubmittedCount() == 0) {
            throw new BusinessException("Chưa có giảng viên nào nhập điểm cho đề tài này.");
        }
        requireSubmittedTopic(topic);
        FinalResult result = saveFinalResult(topic, chairmanComments);
        result.setChairmanConfirmed(true);
        return finalResultRepository.save(result);
    }

    @Override
    public FinalResult calculateAndSaveFinalResult(Long topicId, String chairmanComments) {
        Topic topic = findTopic(topicId);
        if (isThesis(topic)) {
            throw new BusinessException("KLTN chỉ được tổng hợp và xác nhận bởi Chủ tịch hội đồng.");
        }
        ScoreProgressDTO progress = getScoringProgress(topicId);
        if (!progress.isComplete() && !progress.isDeadlinePassed()) {
            throw new BusinessException("Chưa thể tổng hợp điểm. Các giảng viên sau chưa chấm: "
                    + String.join(", ", progress.getPendingTeacherNames()) + ".");
        }
        if (progress.getRequiredCount() == 0 || progress.getSubmittedCount() == 0) {
            throw new BusinessException("Đề tài chưa có người chấm được phân công hoặc chưa có điểm.");
        }
        requireSubmittedTopic(topic);
        return saveFinalResult(topic, chairmanComments);
    }

    @Override
    public void publishResults(Long periodId) {
        List<Topic> topicsToPublish = getTopicsToPublish(periodId);
        if (topicsToPublish.isEmpty()) {
            throw new BusinessException("Đợt này chưa có đề tài đã được phân công để công bố điểm.");
        }
        for (Topic topic : topicsToPublish) {
            FinalResult result = finalResultRepository.findByTopicId(topic.getId())
                    .orElseThrow(() -> new BusinessException("Đề tài \"" + topic.getTitle()
                            + "\" chưa được tổng hợp hoặc Chủ tịch chưa xác nhận kết quả."));
            ScoreProgressDTO progress = getScoringProgress(topic.getId());
            if (!progress.isComplete() && !progress.isDeadlinePassed()) {
                throw new BusinessException("Chưa thể công bố kết quả. Đề tài \"" + topic.getTitle()
                        + "\" còn giảng viên chưa chấm: "
                        + String.join(", ", progress.getPendingTeacherNames()) + ".");
            }
            if (isThesis(topic) && !result.isChairmanConfirmed()) {
                throw new BusinessException("Đề tài KLTN \"" + topic.getTitle()
                        + "\" chưa được Chủ tịch hội đồng xác nhận.");
            }
        }
        LocalDateTime publishedAt = LocalDateTime.now();
        for (Topic topic : topicsToPublish) {
            FinalResult result = finalResultRepository.findByTopicId(topic.getId()).orElseThrow();
            result.setPublished(true);
            result.setPublishedAt(publishedAt);
            finalResultRepository.save(result);
            topic.setStatus(TopicStatus.RESULT_PUBLISHED);
            topicRepository.save(topic);
        }
    }

    @Override
    public boolean publishResultsIfReady(Long periodId) {
        List<Topic> topicsToPublish = getTopicsToPublish(periodId);
        if (topicsToPublish.isEmpty()) {
            return false;
        }
        for (Topic topic : topicsToPublish) {
            FinalResult result = finalResultRepository.findByTopicId(topic.getId()).orElse(null);
            if (result == null || !getScoringProgress(topic.getId()).isComplete()
                    || (isThesis(topic) && !result.isChairmanConfirmed())) {
                return false;
            }
        }
        publishResults(periodId);
        return true;
    }

    @Override
    public void unpublishResults(Long periodId) {
        List<Topic> topics = topicRepository.findByPeriodId(periodId);
        boolean foundPublished = false;
        for (Topic topic : topics) {
            Optional<FinalResult> resultOpt = finalResultRepository.findByTopicId(topic.getId());
            if (resultOpt.isPresent() && resultOpt.get().isPublished()) {
                FinalResult result = resultOpt.get();
                result.setPublished(false);
                result.setPublishedAt(null);
                finalResultRepository.save(result);
                topic.setStatus(TopicStatus.SCORED);
                topicRepository.save(topic);
                foundPublished = true;
            }
        }
        if (!foundPublished) {
            throw new BusinessException("Đợt này chưa có kết quả nào được công bố.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canViewScoring(Long topicId, Long teacherId) {
        Topic topic = findTopic(topicId);
        return teacherId != null && isReadyToScore(topicId)
                && !topicAdvisorRepository.existsByTopicIdAndTeacherId(topicId, teacherId)
                && isAssignedScorer(topic, teacherId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isChairman(Long topicId, Long teacherId) {
        return councilTopicRepository.findByTopicId(topicId)
                .map(assignment -> councilMemberRepository.existsByCouncilIdAndTeacherIdAndRole(
                        assignment.getCouncil().getId(), teacherId, CouncilRole.CHAIRMAN))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isReadyToScore(Long topicId) {
        Topic topic = findTopic(topicId);
        return (topic.getStatus() == TopicStatus.ASSIGNED || topic.getStatus() == TopicStatus.SCORING
                || topic.getStatus() == TopicStatus.SCORED || topic.getStatus() == TopicStatus.RESULT_PUBLISHED)
                && topicRegistrationRepository.findByTopicId(topicId)
                .filter(registration -> registration.getGroup().getStatus() == StudentGroupStatus.SUBMITTED)
                .filter(registration -> submissionRepository.existsByGroupId(registration.getGroup().getId()))
                .isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getReviewerTopicIds(Long teacherId) {
        return topicReviewerRepository.findByTeacherId(teacherId).stream()
                .map(reviewer -> reviewer.getTopic().getId())
                .filter(this::isReadyToScore)
                .toList();
    }

    @Override
    public void assignReviewers(Long topicId, List<Long> teacherIds) {
        Topic topic = findTopic(topicId);
        if (isThesis(topic)) {
            throw new BusinessException("KLTN phải được chấm bởi hội đồng, không phân công GVPB độc lập.");
        }
        if (topic.getStatus() != TopicStatus.ASSIGNED || !isReadyToScore(topicId)) {
            throw new BusinessException("Chỉ được phân công GVPB cho đề tài đã được nhóm duyệt và đã nộp bài.");
        }
        if (teacherIds == null) {
            throw new BusinessException("Chọn từ 1 đến 2 giảng viên phản biện.");
        }
        List<Long> uniqueIds = teacherIds.stream().distinct().toList();
        if (uniqueIds.size() != teacherIds.size() || uniqueIds.isEmpty() || uniqueIds.size() > 2) {
            throw new BusinessException("Mỗi đề tài cần có từ 1 đến 2 GVPB khác nhau.");
        }
        List<Teacher> reviewers = uniqueIds.stream()
                .map(id -> teacherRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Giảng viên", "id", id)))
                .toList();
        if (reviewers.stream().anyMatch(teacher ->
                topicAdvisorRepository.existsByTopicIdAndTeacherId(topicId, teacher.getId()))) {
            throw new BusinessException("GVHD không được phân công phản biện đề tài mình hướng dẫn.");
        }
        topicReviewerRepository.deleteByTopicId(topicId);
        reviewers.forEach(teacher -> topicReviewerRepository.save(TopicReviewer.builder()
                .topic(topic)
                .teacher(teacher)
                .build()));
    }

    @Override
    @Scheduled(fixedDelayString = "${app.scoring.automation-interval-ms:60000}")
    public void processScoringDeadlines() {
        LocalDateTime now = LocalDateTime.now();
        for (RegistrationPeriod period : registrationPeriodRepository
                .findByStatusAndReviewSubmissionDeadlineIsNotNullAndReviewSubmissionDeadlineLessThanEqualAndScoringReminderSentAtIsNull(
                        PeriodStatus.DEFENSE_EVALUATION, now)) {
            if (period.getStatus() != PeriodStatus.DEFENSE_EVALUATION) {
                continue;
            }
            List<Topic> topics = topicRepository.findByPeriodIdAndStatusIn(period.getId(),
                    List.of(TopicStatus.ASSIGNED, TopicStatus.SCORING));
            List<Long> pendingTeacherIds = topics.stream()
                    .filter(topic -> isReadyToScore(topic.getId()))
                    .filter(topic -> getScoringProgress(topic.getId()).getRequiredCount() > 0)
                    .flatMap(topic -> getPendingScorerIds(topic).stream())
                    .distinct()
                    .toList();
            if (!pendingTeacherIds.isEmpty()) {
                User admin = userRepository.findFirstByRole_NameOrderByIdAsc(RoleName.ROLE_ADMIN)
                        .orElseThrow(() -> new BusinessException("Không tìm thấy tài khoản quản trị để gửi nhắc hạn chấm điểm."));
                notificationService.createNotification(NotificationDTO.builder()
                        .title("Nhắc hạn nộp điểm")
                        .content("Đợt \"" + period.getName()
                                + "\" đã đến hạn nộp điểm. Vui lòng hoàn tất chấm điểm hoặc liên hệ Chủ tịch/Trưởng khoa.")
                        .type(NotificationType.RESULT)
                        .targetRole("INDIVIDUAL")
                        .targetUserIds(pendingTeacherIds)
                        .periodId(period.getId())
                        .build(), admin.getId());
            }
            if (period.getPeriodType() != PeriodType.KLTN) {
                for (Topic topic : topics) {
                    if (!isReadyToScore(topic.getId())) {
                        continue;
                    }
                    ScoreProgressDTO progress = getScoringProgress(topic.getId());
                    if (progress.getSubmittedCount() > 0 && progress.getRequiredCount() > 0
                            && finalResultRepository.findByTopicId(topic.getId())
                            .filter(result -> !result.isPublished()).isEmpty()) {
                        calculateAndSaveFinalResult(topic.getId(), null);
                    }
                }
            }
            period.setScoringReminderSentAt(now);
            registrationPeriodRepository.save(period);
        }
        publishReadyPeriodsAutomatically();
    }

    private void publishReadyPeriodsAutomatically() {
        for (RegistrationPeriod period : registrationPeriodRepository.findByStatusIn(
                List.of(PeriodStatus.SUBMISSION_CLOSED, PeriodStatus.DEFENSE_EVALUATION))) {
            if (!publishResultsIfReady(period.getId())) {
                continue;
            }
            period.setStatus(PeriodStatus.COMPLETED);
            period.setStatusManuallyControlled(false);
            registrationPeriodRepository.save(period);
            User admin = userRepository.findFirstByRole_NameOrderByIdAsc(RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new BusinessException(
                            "Không tìm thấy tài khoản quản trị để thông báo công bố kết quả."));
            notificationService.createNotification(NotificationDTO.builder()
                    .title("Kết quả đề tài đã được công bố")
                    .content("Kết quả đợt \"" + period.getName()
                            + "\" đã được tự động công bố khi hoàn tất chấm điểm.")
                    .type(NotificationType.RESULT)
                    .targetRole("ROLE_STUDENT")
                    .periodId(period.getId())
                    .build(), admin.getId());
        }
    }

    private List<Topic> getTopicsToPublish(Long periodId) {
        return topicRepository.findByPeriodId(periodId).stream()
                .filter(topic -> topic.getStatus() == TopicStatus.ASSIGNED
                        || topic.getStatus() == TopicStatus.SCORING
                        || topic.getStatus() == TopicStatus.SCORED
                        || topic.getStatus() == TopicStatus.RESULT_PUBLISHED)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FinalResult getFinalResultByTopic(Long topicId) {
        return finalResultRepository.findByTopicId(topicId).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinalResult> getPublishedFinalResults() {
        return finalResultRepository.findByIsPublishedTrueOrderByTopicTitleAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinalResult> getPublishedFinalResultsByPeriod(Long periodId) {
        return finalResultRepository.findByIsPublishedTrueAndTopicPeriodIdOrderByTopicTitleAsc(periodId);
    }

    private FinalResult saveFinalResult(Topic topic, String chairmanComments) {
        List<Teacher> requiredTeachers = getRequiredTeachers(topic);
        Set<Long> requiredTeacherIds = requiredTeachers.stream().map(Teacher::getId).collect(Collectors.toSet());
        List<Score> scores = scoreRepository.findByTopicId(topic.getId()).stream()
                .filter(score -> requiredTeacherIds.contains(score.getTeacher().getId()))
                .toList();
        if (scores.isEmpty()) {
            throw new BusinessException("Không có điểm hợp lệ để tổng hợp.");
        }
        BigDecimal total = scores.stream().map(Score::getScoreValue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal average = total.divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
        EvaluationStatus status = average.compareTo(BigDecimal.valueOf(5)) >= 0
                ? EvaluationStatus.PASSED : EvaluationStatus.FAILED;
        FinalResult result = finalResultRepository.findByTopicId(topic.getId())
                .orElseGet(() -> FinalResult.builder().topic(topic).isPublished(false).build());
        if (result.isPublished()) {
            throw new BusinessException("Kết quả đã công bố; hãy hủy công bố trước khi chỉnh sửa.");
        }
        result.setFinalScore(average);
        result.setEvaluationStatus(status);
        if (chairmanComments != null) {
            result.setChairmanComments(chairmanComments);
        }
        topic.setStatus(TopicStatus.SCORED);
        topicRepository.save(topic);
        return finalResultRepository.save(result);
    }

    private Topic findTopic(Long topicId) {
        return topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Đề tài", "id", topicId));
    }

    private void requireSubmittedTopic(Topic topic) {
        TopicRegistration registration = topicRegistrationRepository.findByTopicId(topic.getId())
                .orElseThrow(() -> new BusinessException("Đề tài chưa được nhóm sinh viên đăng ký."));
        if (registration.getGroup().getStatus() != StudentGroupStatus.SUBMITTED
                || !submissionRepository.existsByGroupId(registration.getGroup().getId())) {
            throw new BusinessException("Chỉ đề tài có báo cáo đã nộp mới được đưa vào chấm điểm.");
        }
    }

    private List<Teacher> getRequiredTeachers(Topic topic) {
        if (isThesis(topic)) {
            return councilTopicRepository.findByTopicId(topic.getId())
                    .map(assignment -> assignment.getCouncil().getMembers().stream()
                            .map(CouncilMember::getTeacher)
                            .filter(teacher -> !topicAdvisorRepository.existsByTopicIdAndTeacherId(
                                    topic.getId(), teacher.getId()))
                            .toList())
                    .orElse(List.of());
        }
        return topicReviewerRepository.findByTopicId(topic.getId()).stream()
                .map(TopicReviewer::getTeacher)
                .filter(teacher -> !topicAdvisorRepository.existsByTopicIdAndTeacherId(
                        topic.getId(), teacher.getId()))
                .toList();
    }

    private boolean isAssignedScorer(Topic topic, Long teacherId) {
        if (isThesis(topic)) {
            return councilTopicRepository.findByTopicId(topic.getId())
                    .map(assignment -> councilMemberRepository.existsByCouncilIdAndTeacherId(
                            assignment.getCouncil().getId(), teacherId))
                    .orElse(false);
        }
        return topicReviewerRepository.existsByTopicIdAndTeacherId(topic.getId(), teacherId);
    }

    private boolean isThesis(Topic topic) {
        return topic.getPeriod().getPeriodType() == PeriodType.KLTN;
    }

    private void validateCriteria(ScoreDTO dto, int count) {
        if (dto.getContentScore() == null || dto.getProductScore() == null || dto.getReportScore() == null
                || (count == 4 && dto.getPresentationScore() == null)) {
            throw new BusinessException("Vui lòng nhập đầy đủ các tiêu chí chấm điểm.");
        }
        validateRange(dto.getContentScore());
        validateRange(dto.getProductScore());
        validateRange(dto.getReportScore());
        if (count == 4) {
            validateRange(dto.getPresentationScore());
        }
    }

    private void validateRange(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(BigDecimal.TEN) > 0) {
            throw new BusinessException("Mỗi tiêu chí phải nằm trong khoảng từ 0 đến 10.");
        }
    }

    private BigDecimal averageCriteria(ScoreDTO dto, int count) {
        BigDecimal sum = dto.getContentScore().add(dto.getProductScore()).add(dto.getReportScore());
        if (count == 4) {
            sum = sum.add(dto.getPresentationScore());
        }
        return sum.divide(BigDecimal.valueOf(count), 1, RoundingMode.HALF_UP);
    }

    private boolean scoreSpreadExceedsThreshold(List<Score> scores) {
        BigDecimal min = scores.stream().map(Score::getScoreValue).min(BigDecimal::compareTo).orElseThrow();
        BigDecimal max = scores.stream().map(Score::getScoreValue).max(BigDecimal::compareTo).orElseThrow();
        return max.subtract(min).compareTo(BigDecimal.valueOf(3)) > 0;
    }

    private List<Long> getPendingScorerIds(Topic topic) {
        Set<Long> scoredIds = scoreRepository.findByTopicId(topic.getId()).stream()
                .map(score -> score.getTeacher().getId())
                .collect(Collectors.toSet());
        return getRequiredTeachers(topic).stream()
                .filter(teacher -> !scoredIds.contains(teacher.getId()))
                .map(teacher -> teacher.getUser().getId())
                .toList();
    }

    private ScoreDTO mapToDTO(Score score) {
        return ScoreDTO.builder()
                .id(score.getId())
                .topicId(score.getTopic().getId())
                .teacherId(score.getTeacher().getId())
                .teacherName(score.getTeacher().getUser().getFullName())
                .contentScore(score.getContentScore())
                .productScore(score.getProductScore())
                .reportScore(score.getReportScore())
                .presentationScore(score.getPresentationScore())
                .scoreValue(score.getScoreValue())
                .comments(score.getComments())
                .scoredAt(score.getScoredAt())
                .build();
    }
}
