package com.hcmute.topicmanagement.service.impl;

import com.hcmute.topicmanagement.entity.*;
import com.hcmute.topicmanagement.enums.PeriodStatus;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentGroupServiceImplTest {

    @Mock
    private StudentGroupRepository groupRepository;

    @Mock
    private GroupMemberRepository memberRepository;

    @Mock
    private TopicRegistrationRepository registrationRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private RegistrationPeriodRepository periodRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private StudentGroupServiceImpl groupService;

    @Test
    void rejectedGroupCanRegisterAgainAndReusesItsRegistration() {
        LocalDateTime now = LocalDateTime.now();
        RegistrationPeriod period = RegistrationPeriod.builder()
                .id(1L)
                .name("Đợt 1")
                .status(PeriodStatus.PHASE2_STUDENT_REG)
                .studentStartDate(now.minusDays(1))
                .studentEndDate(now.plusDays(1))
                .build();
        User leaderUser = User.builder().id(2L).fullName("Nhóm trưởng").build();
        Student leader = Student.builder().id(2L).user(leaderUser).build();
        StudentGroup group = StudentGroup.builder()
                .id(3L)
                .period(period)
                .leader(leader)
                .members(new ArrayList<>())
                .build();
        Topic topic = Topic.builder()
                .id(4L)
                .title("Đề tài")
                .period(period)
                .status(TopicStatus.APPROVED)
                .build();
        TopicRegistration registration = TopicRegistration.builder()
                .id(5L)
                .group(group)
                .topic(topic)
                .status(TopicRegistrationStatus.REJECTED)
                .rejectionReason("Cần bổ sung")
                .build();

        when(groupRepository.findById(3L)).thenReturn(Optional.of(group));
        when(topicRepository.findById(4L)).thenReturn(Optional.of(topic));
        when(registrationRepository.findByGroupId(3L)).thenReturn(Optional.of(registration));
        when(registrationRepository.findByTopicId(4L)).thenReturn(Optional.of(registration));
        when(registrationRepository.save(any(TopicRegistration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        groupService.registerTopic(3L, 4L, 2L);

        assertEquals(TopicRegistrationStatus.PENDING, registration.getStatus());
        assertNull(registration.getRejectionReason());
        assertEquals(com.hcmute.topicmanagement.enums.StudentGroupStatus.REGISTERED, group.getStatus());
        assertSame(topic, registration.getTopic());
        verify(registrationRepository).save(registration);
    }

    @Test
    void leaderCanDisbandARejectedGroupAndReleaseItsRegistration() {
        Student leader = Student.builder().id(2L).build();
        StudentGroup group = StudentGroup.builder().id(3L).leader(leader).build();
        TopicRegistration registration = TopicRegistration.builder()
                .id(5L)
                .group(group)
                .status(TopicRegistrationStatus.REJECTED)
                .build();
        when(groupRepository.findById(3L)).thenReturn(Optional.of(group));
        when(registrationRepository.findByGroupId(3L)).thenReturn(Optional.of(registration));

        groupService.disbandRejectedGroup(3L, 2L);

        verify(registrationRepository).delete(registration);
        verify(groupRepository).delete(group);
    }

    @Test
    void onlyGroupLeaderCanDisbandRejectedGroup() {
        Student leader = Student.builder().id(2L).build();
        StudentGroup group = StudentGroup.builder().id(3L).leader(leader).build();
        when(groupRepository.findById(3L)).thenReturn(Optional.of(group));

        assertThrows(BusinessException.class, () -> groupService.disbandRejectedGroup(3L, 9L));
    }
}
