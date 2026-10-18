package com.hcmute.topicmanagement.service.impl;

import com.hcmute.topicmanagement.dto.ScoreDTO;
import com.hcmute.topicmanagement.dto.NotificationDTO;
import com.hcmute.topicmanagement.entity.*;
import com.hcmute.topicmanagement.enums.PeriodStatus;
import com.hcmute.topicmanagement.enums.PeriodType;
import com.hcmute.topicmanagement.enums.RoleName;
import com.hcmute.topicmanagement.enums.StudentGroupStatus;
import com.hcmute.topicmanagement.enums.TopicRegistrationStatus;
import com.hcmute.topicmanagement.enums.TopicStatus;
import com.hcmute.topicmanagement.exception.BusinessException;
import com.hcmute.topicmanagement.repository.*;
import com.hcmute.topicmanagement.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScoreServiceImplTest {

    @Mock
    private ScoreRepository scoreRepository;

    @Mock
    private FinalResultRepository finalResultRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private TopicAdvisorRepository topicAdvisorRepository;

    @Mock
    private TopicReviewerRepository topicReviewerRepository;

    @Mock
    private CouncilTopicRepository councilTopicRepository;

    @Mock
    private CouncilMemberRepository councilMemberRepository;

    @Mock
    private TopicRegistrationRepository topicRegistrationRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RegistrationPeriodRepository registrationPeriodRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ScoreServiceImpl scoreService;

    @Test
    void submittingTeacherScoreAutomaticallyCalculatesAndSavesFinalResult() {
        RegistrationPeriod period = period(PeriodType.MON_HOC);
        Topic topic = topic(period);
        StudentGroup group = submittedGroup(period);
        TopicRegistration registration = registration(topic, group);
        User user = User.builder().id(20L).fullName("Giảng viên").build();
        Teacher teacher = Teacher.builder().id(20L).user(user).build();
        TopicReviewer reviewer = TopicReviewer.builder().topic(topic).teacher(teacher).build();
        Score savedScore = Score.builder()
                .id(30L)
                .topic(topic)
                .teacher(teacher)
                .scoreValue(new BigDecimal("8.50"))
                .build();

        when(topicRepository.findById(10L)).thenReturn(Optional.of(topic));
        when(topicRegistrationRepository.findByTopicId(10L)).thenReturn(Optional.of(registration));
        when(submissionRepository.existsByGroupId(50L)).thenReturn(true);
        when(teacherRepository.findById(20L)).thenReturn(Optional.of(teacher));
        when(topicReviewerRepository.existsByTopicIdAndTeacherId(10L, 20L)).thenReturn(true);
        when(topicReviewerRepository.findByTopicId(10L)).thenReturn(List.of(reviewer));
        when(topicAdvisorRepository.existsByTopicIdAndTeacherId(10L, 20L)).thenReturn(false);
        when(scoreRepository.findByTopicIdAndTeacherId(10L, 20L)).thenReturn(Optional.empty());
        when(scoreRepository.save(any(Score.class))).thenReturn(savedScore);
        when(scoreRepository.findByTopicId(10L)).thenReturn(List.of(savedScore));
        when(finalResultRepository.findByTopicId(10L)).thenReturn(Optional.empty());
        when(finalResultRepository.save(any(FinalResult.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        scoreService.submitScore(ScoreDTO.builder()
                .topicId(10L)
                .contentScore(new BigDecimal("8.00"))
                .productScore(new BigDecimal("9.00"))
                .reportScore(new BigDecimal("8.50"))
                .comments("Tốt")
                .build(), 20L);

        org.mockito.ArgumentCaptor<FinalResult> resultCaptor =
                org.mockito.ArgumentCaptor.forClass(FinalResult.class);
        verify(finalResultRepository, times(1)).save(resultCaptor.capture());
        assertEquals(new BigDecimal("8.50"), resultCaptor.getValue().getFinalScore());
    }

    @Test
    void cannotCalculateFinalResultBeforeAnyTeacherHasScored() {
        Topic topic = topic(period(PeriodType.MON_HOC));
        when(topicRepository.findById(10L)).thenReturn(Optional.of(topic));
        when(topicReviewerRepository.findByTopicId(10L)).thenReturn(List.of());
        when(scoreRepository.findByTopicId(10L)).thenReturn(List.of());

        assertThrows(BusinessException.class,
                () -> scoreService.calculateAndSaveFinalResult(10L, null));
    }

    @Test
    void cannotCalculateUntilEveryAssignedReviewerHasScored() {
        RegistrationPeriod period = period(PeriodType.MON_HOC);
        Topic topic = topic(period);
        Teacher first = Teacher.builder().id(20L).user(User.builder().fullName("Giảng viên A").build()).build();
        Teacher second = Teacher.builder().id(21L).user(User.builder().fullName("Giảng viên B").build()).build();
        TopicReviewer firstReviewer = TopicReviewer.builder().topic(topic).teacher(first).build();
        TopicReviewer secondReviewer = TopicReviewer.builder().topic(topic).teacher(second).build();
        Score firstScore = Score.builder().topic(topic).teacher(first).scoreValue(new BigDecimal("8.00")).build();
        when(topicRepository.findById(10L)).thenReturn(Optional.of(topic));
        when(scoreRepository.findByTopicId(10L)).thenReturn(List.of(firstScore));
        when(topicReviewerRepository.findByTopicId(10L)).thenReturn(List.of(firstReviewer, secondReviewer));
        when(topicAdvisorRepository.existsByTopicIdAndTeacherId(10L, 20L)).thenReturn(false);
        when(topicAdvisorRepository.existsByTopicIdAndTeacherId(10L, 21L)).thenReturn(false);

        BusinessException error = assertThrows(BusinessException.class,
                () -> scoreService.calculateAndSaveFinalResult(10L, null));

        org.junit.jupiter.api.Assertions.assertTrue(error.getMessage().contains("Giảng viên B"));
    }

    @Test
    void deadlineRemindsPendingReviewersAndCalculatesAvailableScores() {
        RegistrationPeriod period = period(PeriodType.MON_HOC);
        period.setStatus(PeriodStatus.DEFENSE_EVALUATION);
        period.setReviewSubmissionDeadline(LocalDateTime.now().minusMinutes(1));
        Topic topic = topic(period);
        StudentGroup group = submittedGroup(period);
        TopicRegistration registration = registration(topic, group);
        Teacher first = Teacher.builder().id(20L)
                .user(User.builder().id(120L).fullName("Giảng viên A").build()).build();
        Teacher second = Teacher.builder().id(21L)
                .user(User.builder().id(121L).fullName("Giảng viên B").build()).build();
        Score firstScore = Score.builder().topic(topic).teacher(first)
                .scoreValue(new BigDecimal("8.00")).build();

        when(registrationPeriodRepository
                .findByStatusAndReviewSubmissionDeadlineIsNotNullAndReviewSubmissionDeadlineLessThanEqualAndScoringReminderSentAtIsNull(
                        org.mockito.ArgumentMatchers.eq(PeriodStatus.DEFENSE_EVALUATION),
                        org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(List.of(period));
        when(topicRepository.findByPeriodIdAndStatusIn(period.getId(),
                List.of(TopicStatus.ASSIGNED, TopicStatus.SCORING))).thenReturn(List.of(topic));
        when(topicRepository.findById(10L)).thenReturn(Optional.of(topic));
        when(topicRegistrationRepository.findByTopicId(10L)).thenReturn(Optional.of(registration));
        when(submissionRepository.existsByGroupId(50L)).thenReturn(true);
        when(topicReviewerRepository.findByTopicId(10L)).thenReturn(List.of(
                TopicReviewer.builder().topic(topic).teacher(first).build(),
                TopicReviewer.builder().topic(topic).teacher(second).build()));
        when(topicAdvisorRepository.existsByTopicIdAndTeacherId(10L, 20L)).thenReturn(false);
        when(topicAdvisorRepository.existsByTopicIdAndTeacherId(10L, 21L)).thenReturn(false);
        when(scoreRepository.findByTopicId(10L)).thenReturn(List.of(firstScore));
        when(finalResultRepository.findByTopicId(10L)).thenReturn(Optional.empty());
        when(finalResultRepository.save(any(FinalResult.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        User admin = User.builder().id(99L).build();
        when(userRepository.findFirstByRole_NameOrderByIdAsc(com.hcmute.topicmanagement.enums.RoleName.ROLE_ADMIN))
                .thenReturn(Optional.of(admin));

        scoreService.processScoringDeadlines();

        org.mockito.ArgumentCaptor<NotificationDTO> notificationCaptor =
                org.mockito.ArgumentCaptor.forClass(NotificationDTO.class);
        verify(notificationService).createNotification(notificationCaptor.capture(), org.mockito.ArgumentMatchers.eq(99L));
        assertEquals(List.of(121L), notificationCaptor.getValue().getTargetUserIds());
        verify(finalResultRepository).save(any(FinalResult.class));
        verify(registrationPeriodRepository).save(period);
        org.junit.jupiter.api.Assertions.assertNotNull(period.getScoringReminderSentAt());
    }

    @Test
    void closedPeriodAutomaticallyPublishesWhenAllReviewersHaveScored() {
        RegistrationPeriod period = period(PeriodType.MON_HOC);
        period.setStatus(PeriodStatus.SUBMISSION_CLOSED);
        Topic topic = topic(period);
        topic.setStatus(TopicStatus.SCORED);
        Teacher reviewer = Teacher.builder().id(20L)
                .user(User.builder().id(120L).fullName("Giảng viên A").build()).build();
        TopicReviewer assignment = TopicReviewer.builder().topic(topic).teacher(reviewer).build();
        Score score = Score.builder().topic(topic).teacher(reviewer)
                .scoreValue(new BigDecimal("8.00")).build();
        FinalResult result = FinalResult.builder().topic(topic).isPublished(false).build();

        when(registrationPeriodRepository.findByStatusIn(
                List.of(PeriodStatus.SUBMISSION_CLOSED, PeriodStatus.DEFENSE_EVALUATION)))
                .thenReturn(List.of(period));
        when(topicRepository.findByPeriodId(period.getId())).thenReturn(List.of(topic));
        when(topicRepository.findById(topic.getId())).thenReturn(Optional.of(topic));
        when(topicReviewerRepository.findByTopicId(topic.getId())).thenReturn(List.of(assignment));
        when(topicAdvisorRepository.existsByTopicIdAndTeacherId(topic.getId(), reviewer.getId()))
                .thenReturn(false);
        when(scoreRepository.findByTopicId(topic.getId())).thenReturn(List.of(score));
        when(finalResultRepository.findByTopicId(topic.getId())).thenReturn(Optional.of(result));
        when(finalResultRepository.save(any(FinalResult.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findFirstByRole_NameOrderByIdAsc(RoleName.ROLE_ADMIN))
                .thenReturn(Optional.of(User.builder().id(99L).build()));

        scoreService.processScoringDeadlines();

        assertEquals(PeriodStatus.COMPLETED, period.getStatus());
        org.junit.jupiter.api.Assertions.assertTrue(result.isPublished());
        verify(registrationPeriodRepository).save(period);
        verify(notificationService).createNotification(
                any(NotificationDTO.class), org.mockito.ArgumentMatchers.eq(99L));
    }

    @Test
    void closedPeriodWaitsForEveryAssignedReviewerBeforeAutomaticPublication() {
        RegistrationPeriod period = period(PeriodType.MON_HOC);
        period.setStatus(PeriodStatus.SUBMISSION_CLOSED);
        Topic topic = topic(period);
        FinalResult result = FinalResult.builder().topic(topic).isPublished(false).build();

        when(registrationPeriodRepository.findByStatusIn(
                List.of(PeriodStatus.SUBMISSION_CLOSED, PeriodStatus.DEFENSE_EVALUATION)))
                .thenReturn(List.of(period));
        when(topicRepository.findByPeriodId(period.getId())).thenReturn(List.of(topic));
        when(topicRepository.findById(topic.getId())).thenReturn(Optional.of(topic));
        when(finalResultRepository.findByTopicId(topic.getId())).thenReturn(Optional.of(result));
        when(topicReviewerRepository.findByTopicId(topic.getId())).thenReturn(List.of());
        when(scoreRepository.findByTopicId(topic.getId())).thenReturn(List.of());

        scoreService.processScoringDeadlines();

        assertEquals(PeriodStatus.SUBMISSION_CLOSED, period.getStatus());
        org.junit.jupiter.api.Assertions.assertFalse(result.isPublished());
        verify(registrationPeriodRepository, org.mockito.Mockito.never()).save(period);
        verify(notificationService, org.mockito.Mockito.never())
                .createNotification(any(NotificationDTO.class), any());
    }

    private RegistrationPeriod period(PeriodType periodType) {
        return RegistrationPeriod.builder()
                .id(60L)
                .periodType(periodType)
                .reviewSubmissionDeadline(LocalDateTime.now().plusDays(2))
                .build();
    }

    private Topic topic(RegistrationPeriod period) {
        return Topic.builder()
                .id(10L)
                .period(period)
                .status(TopicStatus.ASSIGNED)
                .build();
    }

    private StudentGroup submittedGroup(RegistrationPeriod period) {
        return StudentGroup.builder()
                .id(50L)
                .period(period)
                .status(StudentGroupStatus.SUBMITTED)
                .build();
    }

    private TopicRegistration registration(Topic topic, StudentGroup group) {
        return TopicRegistration.builder()
                .topic(topic)
                .group(group)
                .status(TopicRegistrationStatus.APPROVED)
                .build();
    }
}
