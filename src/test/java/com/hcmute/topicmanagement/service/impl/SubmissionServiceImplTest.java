package com.hcmute.topicmanagement.service.impl;

import com.hcmute.topicmanagement.entity.RegistrationPeriod;
import com.hcmute.topicmanagement.entity.Student;
import com.hcmute.topicmanagement.entity.StudentGroup;
import com.hcmute.topicmanagement.entity.Submission;
import com.hcmute.topicmanagement.entity.Topic;
import com.hcmute.topicmanagement.entity.TopicRegistration;
import com.hcmute.topicmanagement.enums.PeriodStatus;
import com.hcmute.topicmanagement.enums.PeriodType;
import com.hcmute.topicmanagement.enums.StudentGroupStatus;
import com.hcmute.topicmanagement.enums.TopicRegistrationStatus;
import com.hcmute.topicmanagement.exception.BusinessException;
import com.hcmute.topicmanagement.repository.CouncilMemberRepository;
import com.hcmute.topicmanagement.repository.CouncilTopicRepository;
import com.hcmute.topicmanagement.repository.StudentGroupRepository;
import com.hcmute.topicmanagement.repository.StudentRepository;
import com.hcmute.topicmanagement.repository.SubmissionRepository;
import com.hcmute.topicmanagement.repository.TopicAdvisorRepository;
import com.hcmute.topicmanagement.repository.TopicRegistrationRepository;
import com.hcmute.topicmanagement.repository.TopicReviewerRepository;
import com.hcmute.topicmanagement.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceImplTest {

    @Mock
    private SubmissionRepository submissionRepository;
    @Mock
    private StudentGroupRepository groupRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private TopicRegistrationRepository topicRegistrationRepository;
    @Mock
    private TopicAdvisorRepository topicAdvisorRepository;
    @Mock
    private TopicReviewerRepository topicReviewerRepository;
    @Mock
    private CouncilTopicRepository councilTopicRepository;
    @Mock
    private CouncilMemberRepository councilMemberRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private SubmissionServiceImpl submissionService;

    @Test
    void approvedGroupCanSubmitDuringStudentRegistrationPhase(@TempDir Path uploadDirectory) throws Exception {
        Long groupId = 42L;
        Long studentId = 7L;
        Student leader = Student.builder().id(studentId).build();
        RegistrationPeriod period = RegistrationPeriod.builder()
                .id(12L)
                .periodType(PeriodType.TLCN)
                .status(PeriodStatus.PHASE2_STUDENT_REG)
                .submissionDeadline(LocalDateTime.now().plusDays(1))
                .build();
        StudentGroup group = StudentGroup.builder()
                .id(groupId)
                .groupCode("G42")
                .period(period)
                .leader(leader)
                .status(StudentGroupStatus.APPROVED)
                .build();
        Topic topic = Topic.builder().id(25L).title("Test topic").build();
        TopicRegistration registration = TopicRegistration.builder()
                .group(group)
                .topic(topic)
                .status(TopicRegistrationStatus.APPROVED)
                .build();
        MultipartFile report = org.mockito.Mockito.mock(MultipartFile.class);

        ReflectionTestUtils.setField(submissionService, "uploadDir", uploadDirectory.toString());
        ReflectionTestUtils.setField(submissionService, "maxFileSize", 50L * 1024 * 1024);
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(topicRegistrationRepository.findByGroupId(groupId)).thenReturn(Optional.of(registration));
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(leader));
        when(submissionRepository.findMaxVersionByGroupId(groupId)).thenReturn(0);
        when(submissionRepository.save(any(Submission.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(topicAdvisorRepository.findByTopicId(topic.getId())).thenReturn(List.of());
        when(report.isEmpty()).thenReturn(false);
        when(report.getSize()).thenReturn(4L);
        when(report.getOriginalFilename()).thenReturn("report.pdf");
        when(report.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[] {1, 2, 3, 4}));

        Submission submission = submissionService.uploadSubmission(
                groupId, report, null, null, null, studentId);

        assertNotNull(submission.getFilePath());
        assertEquals(1, submission.getVersionNumber());
        assertEquals(StudentGroupStatus.SUBMITTED, group.getStatus());
        verify(submissionRepository).save(any(Submission.class));
        verify(groupRepository).save(group);
    }

    @Test
    void closedSubmissionPeriodRejectsNewSubmissionsAndResubmissions() {
        Long groupId = 42L;
        Long studentId = 7L;
        Student leader = Student.builder().id(studentId).build();
        RegistrationPeriod period = RegistrationPeriod.builder()
                .id(12L)
                .periodType(PeriodType.TLCN)
                .status(PeriodStatus.SUBMISSION_CLOSED)
                .submissionDeadline(LocalDateTime.now().plusDays(1))
                .build();
        StudentGroup group = StudentGroup.builder()
                .id(groupId)
                .period(period)
                .leader(leader)
                .status(StudentGroupStatus.SUBMITTED)
                .build();
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

        assertThrows(BusinessException.class, () -> submissionService.uploadSubmission(
                groupId, null, null, null, null, studentId));

        verify(submissionRepository, never()).save(any(Submission.class));
    }

    @Test
    void submittedGroupCanUploadAnotherVersionBeforeTheDeadline(@TempDir Path uploadDirectory) throws Exception {
        Long groupId = 42L;
        Long studentId = 7L;
        Student leader = Student.builder().id(studentId).build();
        RegistrationPeriod period = RegistrationPeriod.builder()
                .id(12L)
                .periodType(PeriodType.TLCN)
                .status(PeriodStatus.EXECUTION)
                .submissionDeadline(LocalDateTime.now().plusDays(1))
                .build();
        StudentGroup group = StudentGroup.builder()
                .id(groupId)
                .groupCode("G42")
                .period(period)
                .leader(leader)
                .status(StudentGroupStatus.SUBMITTED)
                .build();
        Topic topic = Topic.builder().id(25L).title("Test topic").build();
        TopicRegistration registration = TopicRegistration.builder()
                .group(group)
                .topic(topic)
                .status(TopicRegistrationStatus.APPROVED)
                .build();
        MultipartFile report = org.mockito.Mockito.mock(MultipartFile.class);

        ReflectionTestUtils.setField(submissionService, "uploadDir", uploadDirectory.toString());
        ReflectionTestUtils.setField(submissionService, "maxFileSize", 50L * 1024 * 1024);
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(topicRegistrationRepository.findByGroupId(groupId)).thenReturn(Optional.of(registration));
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(leader));
        when(submissionRepository.findMaxVersionByGroupId(groupId)).thenReturn(1);
        when(submissionRepository.save(any(Submission.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(topicAdvisorRepository.findByTopicId(topic.getId())).thenReturn(List.of());
        when(report.isEmpty()).thenReturn(false);
        when(report.getSize()).thenReturn(4L);
        when(report.getOriginalFilename()).thenReturn("report-v2.pdf");
        when(report.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[] {1, 2, 3, 4}));

        Submission submission = submissionService.uploadSubmission(
                groupId, report, null, null, null, studentId);

        assertEquals(2, submission.getVersionNumber());
        verify(submissionRepository).save(any(Submission.class));
    }
}
