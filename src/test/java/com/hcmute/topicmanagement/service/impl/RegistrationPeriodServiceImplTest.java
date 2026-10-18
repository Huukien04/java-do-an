package com.hcmute.topicmanagement.service.impl;

import com.hcmute.topicmanagement.dto.RegistrationPeriodDTO;
import com.hcmute.topicmanagement.entity.RegistrationPeriod;
import com.hcmute.topicmanagement.enums.PeriodAction;
import com.hcmute.topicmanagement.enums.PeriodStatus;
import com.hcmute.topicmanagement.enums.PeriodType;
import com.hcmute.topicmanagement.enums.TopicStatus;
import com.hcmute.topicmanagement.exception.BusinessException;
import com.hcmute.topicmanagement.repository.CouncilTopicRepository;
import com.hcmute.topicmanagement.repository.RegistrationPeriodRepository;
import com.hcmute.topicmanagement.repository.StudentGroupRepository;
import com.hcmute.topicmanagement.repository.SubmissionRepository;
import com.hcmute.topicmanagement.repository.TopicRegistrationRepository;
import com.hcmute.topicmanagement.repository.TopicRepository;
import com.hcmute.topicmanagement.repository.TopicReviewerRepository;
import com.hcmute.topicmanagement.service.NotificationService;
import com.hcmute.topicmanagement.service.ScoreService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationPeriodServiceImplTest {

    @Mock
    private RegistrationPeriodRepository periodRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private TopicRegistrationRepository topicRegistrationRepository;

    @Mock
    private StudentGroupRepository studentGroupRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private TopicReviewerRepository topicReviewerRepository;

    @Mock
    private CouncilTopicRepository councilTopicRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ScoreService scoreService;

    @InjectMocks
    private RegistrationPeriodServiceImpl periodService;

    @Test
    void createPeriodAcceptsChronologicalSchedule() {
        LocalDateTime base = LocalDateTime.now().plusDays(3);
        RegistrationPeriodDTO dto = RegistrationPeriodDTO.builder()
                .name("Đợt kiểm thử")
                .periodType(PeriodType.MON_HOC)
                .teacherStartDate(base)
                .teacherEndDate(base.plusDays(10))
                .approvalStartDate(base.plusDays(11))
                .approvalEndDate(base.plusDays(12))
                .studentStartDate(base.plusDays(13))
                .studentEndDate(base.plusDays(20))
                .submissionDeadline(base.plusDays(22))
                .reviewSubmissionDeadline(base.plusDays(25))
                .build();
        when(periodRepository.save(any(RegistrationPeriod.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RegistrationPeriodDTO created = periodService.createPeriod(dto);

        assertEquals(PeriodStatus.DRAFT, created.getStatus());
    }

    @Test
    void createPeriodRejectsAnInvalidWindow() {
        LocalDateTime base = LocalDateTime.now().plusDays(3);
        RegistrationPeriodDTO dto = RegistrationPeriodDTO.builder()
                .name("Đợt kiểm thử")
                .periodType(PeriodType.MON_HOC)
                .teacherStartDate(base)
                .teacherEndDate(base.minusMinutes(1))
                .approvalStartDate(base)
                .approvalEndDate(base.plusDays(1))
                .studentStartDate(base)
                .studentEndDate(base.plusDays(2))
                .build();

        assertThrows(BusinessException.class, () -> periodService.createPeriod(dto));
    }

    @Test
    void canSetStatusWithoutCheckingWhetherTopicsExist() {
        RegistrationPeriod period = period(PeriodStatus.PHASE1_TEACHER_REG);
        when(periodRepository.findById(period.getId())).thenReturn(Optional.of(period));

        periodService.changeStatus(period.getId(), PeriodStatus.PHASE1_DEAN_REVIEW);

        assertEquals(PeriodStatus.PHASE1_DEAN_REVIEW, period.getStatus());
        assertTrue(period.isStatusManuallyControlled());
        verify(periodRepository).save(period);
    }

    @Test
    void canSelectStudentRegistrationWithoutTopicsOrSubmissions() {
        RegistrationPeriod period = period(PeriodStatus.CANCELLED);
        when(periodRepository.findById(period.getId())).thenReturn(Optional.of(period));

        periodService.changeStatus(period.getId(), PeriodStatus.PHASE2_STUDENT_REG);

        assertEquals(PeriodStatus.PHASE2_STUDENT_REG, period.getStatus());
        assertTrue(period.isStudentRegistrationManuallyOpened());
        assertTrue(period.isStatusManuallyControlled());
        verify(periodRepository).save(period);
    }

    @Test
    void endingSubmissionPeriodClosesItAndStillAdvancesToScoringAtDeadline() {
        RegistrationPeriod period = period(PeriodStatus.PHASE2_STUDENT_REG);
        period.setSubmissionDeadline(LocalDateTime.now().minusMinutes(1));
        when(periodRepository.findById(period.getId())).thenReturn(Optional.of(period));
        when(periodRepository.findAll()).thenReturn(List.of(period));
        when(studentGroupRepository.findByPeriodIdAndStatus(
                period.getId(), com.hcmute.topicmanagement.enums.StudentGroupStatus.APPROVED))
                .thenReturn(List.of());

        periodService.changeStatus(period.getId(), PeriodStatus.SUBMISSION_CLOSED);
        assertTrue(period.isStatusManuallyControlled());

        periodService.advancePeriodsAutomatically();

        assertEquals(PeriodStatus.DEFENSE_EVALUATION, period.getStatus());
        assertFalse(period.isStatusManuallyControlled());
        verify(periodRepository, times(2)).save(period);
    }

    @Test
    void closedSubmissionPeriodRemainsAvailableForStudentGroupManagement() {
        RegistrationPeriod period = period(PeriodStatus.SUBMISSION_CLOSED);
        when(periodRepository.findAll()).thenReturn(List.of(period));

        List<RegistrationPeriodDTO> periods = periodService.getPeriodsForStudentGroupManagement();

        assertEquals(1, periods.size());
        assertEquals(PeriodStatus.SUBMISSION_CLOSED, periods.get(0).getStatus());
    }

    @Test
    void canCancelCompletedPeriodAndChangeItsStatusAgain() {
        RegistrationPeriod period = period(PeriodStatus.COMPLETED);
        when(periodRepository.findById(period.getId())).thenReturn(Optional.of(period));

        periodService.changeStatus(period.getId(), PeriodStatus.CANCELLED);
        periodService.changeStatus(period.getId(), PeriodStatus.PHASE1_TEACHER_REG);

        assertEquals(PeriodStatus.PHASE1_TEACHER_REG, period.getStatus());
        assertTrue(period.isTeacherRegistrationManuallyOpened());
        assertTrue(period.isStatusManuallyControlled());
        verify(periodRepository, times(2)).save(period);
    }

    @Test
    void canManuallySelectStudentRegistrationWithPastDeadlineAndNoTopics() {
        RegistrationPeriod period = period(PeriodStatus.PUBLISHED_TOPICS);
        period.setApprovalEndDate(LocalDateTime.now().plusDays(2));
        period.setStudentStartDate(LocalDateTime.now().minusMinutes(1));
        period.setStudentEndDate(LocalDateTime.now().minusDays(1));
        when(periodRepository.findById(period.getId())).thenReturn(Optional.of(period));

        periodService.changeStatus(period.getId(), PeriodStatus.PHASE2_STUDENT_REG);

        assertEquals(PeriodStatus.PHASE2_STUDENT_REG, period.getStatus());
        assertTrue(period.isStudentRegistrationManuallyOpened());
        assertTrue(period.isStatusManuallyControlled());
        verify(periodRepository).save(period);
    }

    @Test
    void manuallySelectedTeacherRegistrationIgnoresConfiguredDeadlineAndStaysSelected() {
        RegistrationPeriod period = period(PeriodStatus.DRAFT);
        period.setTeacherStartDate(LocalDateTime.now().minusDays(3));
        period.setTeacherEndDate(LocalDateTime.now().minusDays(1));
        when(periodRepository.findById(period.getId())).thenReturn(Optional.of(period));
        when(periodRepository.findAll()).thenReturn(List.of(period));

        periodService.changeStatus(period.getId(), PeriodStatus.PHASE1_TEACHER_REG);
        periodService.advancePeriodsAutomatically();

        assertEquals(PeriodStatus.PHASE1_TEACHER_REG, period.getStatus());
        assertTrue(period.isTeacherRegistrationManuallyOpened());
        assertTrue(period.isStatusManuallyControlled());
        verify(periodRepository, times(1)).save(period);
    }

    @Test
    void resumingAutomaticStatusClearsManualOverrides() {
        RegistrationPeriod period = period(PeriodStatus.PHASE1_TEACHER_REG);
        period.setTeacherRegistrationManuallyOpened(true);
        period.setStudentRegistrationManuallyOpened(true);
        period.setStatusManuallyControlled(true);
        when(periodRepository.findById(period.getId())).thenReturn(Optional.of(period));

        periodService.resumeAutomaticStatus(period.getId());

        assertFalse(period.isTeacherRegistrationManuallyOpened());
        assertFalse(period.isStudentRegistrationManuallyOpened());
        assertFalse(period.isStatusManuallyControlled());
        verify(periodRepository).save(period);
    }

    @Test
    void teacherRegistrationRemainsAvailableDuringStudentPhaseWhenItsWindowIsOpen() {
        RegistrationPeriod period = period(PeriodStatus.PHASE2_STUDENT_REG);
        period.setTeacherStartDate(LocalDateTime.now().minusDays(1));
        period.setTeacherEndDate(LocalDateTime.now().plusDays(1));
        when(periodRepository.findAll()).thenReturn(List.of(period));

        assertEquals(1, periodService.getActivePeriodsForTeacher().size());
    }

    @Test
    void openStudentPeriodRemainsVisibleWhenAllApprovedTopicsAreTaken() {
        RegistrationPeriod period = period(PeriodStatus.PHASE2_STUDENT_REG);
        when(periodRepository.findAll()).thenReturn(List.of(period));

        assertEquals(1, periodService.getActivePeriodsForStudent().size());
    }

    @Test
    void reopensTeacherRegistrationWithoutChangingConfiguredTimes() {
        LocalDateTime now = LocalDateTime.now();
        RegistrationPeriod period = period(PeriodStatus.PHASE1_DEAN_REVIEW);
        LocalDateTime teacherStart = now.minusDays(5);
        LocalDateTime teacherEnd = now.minusDays(2);
        period.setTeacherStartDate(teacherStart);
        period.setTeacherEndDate(teacherEnd);
        when(periodRepository.findById(period.getId())).thenReturn(Optional.of(period));
        when(topicRegistrationRepository.existsByGroupPeriodId(period.getId())).thenReturn(false);

        periodService.performAction(period.getId(), PeriodAction.REOPEN_TEACHER_REGISTRATION,
                null, 99L);

        assertEquals(PeriodStatus.PHASE1_TEACHER_REG, period.getStatus());
        assertEquals(teacherStart, period.getTeacherStartDate());
        assertEquals(teacherEnd, period.getTeacherEndDate());
        assertTrue(period.isTeacherRegistrationManuallyOpened());
        verify(periodRepository).save(period);
    }

    @Test
    void reopenedTeacherRegistrationAppearsActiveAfterConfiguredDeadline() {
        RegistrationPeriod period = period(PeriodStatus.PHASE1_DEAN_REVIEW);
        period.setTeacherEndDate(LocalDateTime.now().minusMinutes(1));
        when(periodRepository.findById(period.getId())).thenReturn(Optional.of(period));
        when(topicRegistrationRepository.existsByGroupPeriodId(period.getId())).thenReturn(false);

        periodService.performAction(period.getId(), PeriodAction.REOPEN_TEACHER_REGISTRATION, null, 99L);
        when(periodRepository.findAll()).thenReturn(List.of(period));

        assertEquals(1, periodService.getActivePeriodsForTeacher().size());
    }

    @Test
    void reopensStudentRegistrationWithoutChangingConfiguredTimes() {
        LocalDateTime now = LocalDateTime.now();
        RegistrationPeriod period = period(PeriodStatus.EXECUTION);
        period.setTeacherEndDate(now.minusDays(10));
        period.setApprovalStartDate(now.minusDays(9));
        period.setApprovalEndDate(now.minusDays(8));
        period.setStudentStartDate(now.minusDays(7));
        period.setStudentEndDate(now.minusDays(1));
        period.setSubmissionDeadline(now.plusDays(1));
        period.setReviewSubmissionDeadline(now.plusDays(3));
        when(periodRepository.findById(period.getId())).thenReturn(Optional.of(period));
        when(topicRepository.countByPeriodIdAndStatus(period.getId(), TopicStatus.PUBLISHED)).thenReturn(1L);
        when(studentGroupRepository.findByPeriodId(period.getId())).thenReturn(List.of());

        LocalDateTime originalStudentStart = period.getStudentStartDate();
        LocalDateTime originalStudentEnd = period.getStudentEndDate();
        LocalDateTime originalSubmissionDeadline = period.getSubmissionDeadline();
        LocalDateTime originalScoringDeadline = period.getReviewSubmissionDeadline();
        periodService.performAction(period.getId(), PeriodAction.REOPEN_STUDENT_REGISTRATION,
                null, 99L);

        assertEquals(PeriodStatus.PHASE2_STUDENT_REG, period.getStatus());
        assertEquals(originalStudentStart, period.getStudentStartDate());
        assertEquals(originalStudentEnd, period.getStudentEndDate());
        assertEquals(originalSubmissionDeadline, period.getSubmissionDeadline());
        assertEquals(originalScoringDeadline, period.getReviewSubmissionDeadline());
        assertTrue(period.isStudentRegistrationManuallyOpened());
        verify(periodRepository).save(period);
    }

    private RegistrationPeriod period(PeriodStatus status) {
        LocalDateTime now = LocalDateTime.now();
        return RegistrationPeriod.builder()
                .id(42L)
                .name("Đợt kiểm thử")
                .periodType(PeriodType.MON_HOC)
                .status(status)
                .teacherStartDate(now.minusDays(4))
                .teacherEndDate(now.plusDays(1))
                .approvalStartDate(now.minusDays(2))
                .approvalEndDate(now.plusDays(2))
                .studentStartDate(now.minusMinutes(1))
                .studentEndDate(now.plusDays(4))
                .build();
    }
}
