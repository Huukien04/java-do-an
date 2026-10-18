package com.hcmute.topicmanagement.controller;

import com.hcmute.topicmanagement.entity.Department;
import com.hcmute.topicmanagement.entity.Teacher;
import com.hcmute.topicmanagement.entity.User;
import com.hcmute.topicmanagement.repository.*;
import com.hcmute.topicmanagement.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminManagementControllerTest {

    @Mock private DepartmentRepository departmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private TeacherRepository teacherRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private TopicRepository topicRepository;
    @Mock private TopicAdvisorRepository topicAdvisorRepository;
    @Mock private TopicReviewerRepository topicReviewerRepository;
    @Mock private CouncilMemberRepository councilMemberRepository;
    @Mock private ScoreRepository scoreRepository;
    @Mock private StudentGroupRepository studentGroupRepository;
    @Mock private GroupMemberRepository groupMemberRepository;
    @Mock private SubmissionRepository submissionRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private NotificationReadRepository notificationReadRepository;
    @Mock private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminManagementController controller;

    @Test
    void departmentInUseCannotBeDeleted() {
        Department department = Department.builder().id(3L).build();
        when(departmentRepository.findById(3L)).thenReturn(Optional.of(department));
        when(teacherRepository.existsByDepartmentId(3L)).thenReturn(true);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.deleteDepartment(3L, redirect);

        assertEquals("redirect:/admin/departments", view);
        assertEquals(true, redirect.getFlashAttributes().containsKey("errorMessage"));
        verify(departmentRepository, never()).delete(department);
    }

    @Test
    void accountUsedByATopicCannotBeDeleted() {
        User user = User.builder().id(8L).build();
        Teacher teacher = Teacher.builder().id(8L).build();
        when(userRepository.findById(8L)).thenReturn(Optional.of(user));
        when(teacherRepository.findById(8L)).thenReturn(Optional.of(teacher));
        when(topicRepository.existsByCreatedByTeacherId(8L)).thenReturn(true);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.deleteAccount(8L, null, redirect);

        assertEquals("redirect:/admin/accounts", view);
        assertEquals(true, redirect.getFlashAttributes().containsKey("errorMessage"));
        verify(userRepository, never()).delete(user);
    }

    @Test
    void adminCannotDeleteTheirOwnAccount() {
        User user = User.builder().id(8L).build();
        when(userRepository.findById(8L)).thenReturn(Optional.of(user));
        CustomUserDetails currentUser = org.mockito.Mockito.mock(CustomUserDetails.class);
        when(currentUser.getUserId()).thenReturn(8L);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.deleteAccount(8L, currentUser, redirect);

        assertEquals("redirect:/admin/accounts", view);
        verify(teacherRepository, never()).findById(8L);
        verify(userRepository, never()).delete(user);
    }
}
