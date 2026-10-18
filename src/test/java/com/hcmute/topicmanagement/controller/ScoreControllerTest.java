package com.hcmute.topicmanagement.controller;

import com.hcmute.topicmanagement.entity.Role;
import com.hcmute.topicmanagement.entity.User;
import com.hcmute.topicmanagement.enums.RoleName;
import com.hcmute.topicmanagement.repository.FinalResultRepository;
import com.hcmute.topicmanagement.security.CustomUserDetails;
import com.hcmute.topicmanagement.service.RegistrationPeriodService;
import com.hcmute.topicmanagement.service.ReviewCouncilService;
import com.hcmute.topicmanagement.service.ScoreService;
import com.hcmute.topicmanagement.service.TopicService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScoreControllerTest {

    @Mock
    private ScoreService scoreService;

    @Mock
    private TopicService topicService;

    @Mock
    private ReviewCouncilService councilService;

    @Mock
    private RegistrationPeriodService periodService;

    @Mock
    private FinalResultRepository finalResultRepository;

    @InjectMocks
    private ScoreController scoreController;

    @Test
    void adminResultsAreFilteredByRegistrationPeriod() {
        ExtendedModelMap model = new ExtendedModelMap();
        CustomUserDetails admin = userWithRole(RoleName.ROLE_ADMIN);
        when(finalResultRepository.findByTopicPeriodIdOrderByTopicTitleAsc(42L)).thenReturn(List.of());
        when(periodService.getAllPeriods()).thenReturn(List.of());

        String view = scoreController.viewPublishedResults(admin, 42L, model);

        assertEquals("scoring/results_lookup", view);
        assertEquals(42L, model.getAttribute("selectedPeriodId"));
        assertEquals(true, model.getAttribute("isAdminOrTeacher"));
        verify(finalResultRepository).findByTopicPeriodIdOrderByTopicTitleAsc(42L);
    }

    @Test
    void studentResultsUsePublishedResultsForSelectedPeriod() {
        ExtendedModelMap model = new ExtendedModelMap();
        CustomUserDetails student = userWithRole(RoleName.ROLE_STUDENT);
        when(scoreService.getPublishedFinalResultsByPeriod(42L)).thenReturn(List.of());
        when(periodService.getAllPeriods()).thenReturn(List.of());

        String view = scoreController.viewPublishedResults(student, 42L, model);

        assertEquals("scoring/results_lookup", view);
        assertEquals(42L, model.getAttribute("selectedPeriodId"));
        assertEquals(false, model.getAttribute("isAdminOrTeacher"));
        verify(scoreService).getPublishedFinalResultsByPeriod(42L);
    }

    private CustomUserDetails userWithRole(RoleName roleName) {
        Role role = Role.builder().name(roleName).build();
        User user = User.builder().role(role).build();
        return new CustomUserDetails(user, null, null);
    }
}
