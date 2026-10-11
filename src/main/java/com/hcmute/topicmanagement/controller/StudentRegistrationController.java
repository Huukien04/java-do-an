package com.hcmute.topicmanagement.controller;

import com.hcmute.topicmanagement.entity.RegistrationPeriod;
import com.hcmute.topicmanagement.entity.TopicRegistration;
import com.hcmute.topicmanagement.repository.TopicRegistrationRepository;
import com.hcmute.topicmanagement.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/student/registrations")
@RequiredArgsConstructor
public class StudentRegistrationController {

    private final TopicRegistrationRepository registrationRepository;

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public String listRegistrations(@AuthenticationPrincipal CustomUserDetails userDetails,
                                    @RequestParam(required = false) Long periodId,
                                    Model model) {
        List<TopicRegistration> studentRegistrations =
                registrationRepository.findAllByStudentId(userDetails.getStudent().getId());
        List<RegistrationPeriod> periods = studentRegistrations.stream()
                .map(registration -> registration.getGroup().getPeriod())
                .distinct()
                .sorted(Comparator.comparing(RegistrationPeriod::getStudentStartDate).reversed())
                .toList();

        List<TopicRegistration> visibleRegistrations = studentRegistrations.stream()
                .filter(registration -> periodId == null
                        || registration.getGroup().getPeriod().getId().equals(periodId))
                .toList();

        model.addAttribute("registrations", visibleRegistrations);
        model.addAttribute("periods", periods);
        model.addAttribute("selectedPeriodId", periodId);
        model.addAttribute("totalRegistrations", visibleRegistrations.size());

        return "group/registration_results";
    }
}
