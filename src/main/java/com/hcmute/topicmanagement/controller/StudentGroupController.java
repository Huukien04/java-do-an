package com.hcmute.topicmanagement.controller;

import com.hcmute.topicmanagement.dto.StudentGroupDTO;
import com.hcmute.topicmanagement.repository.TopicRegistrationRepository;
import com.hcmute.topicmanagement.security.CustomUserDetails;
import com.hcmute.topicmanagement.service.RegistrationPeriodService;
import com.hcmute.topicmanagement.service.StudentGroupService;
import com.hcmute.topicmanagement.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/student/group")
@RequiredArgsConstructor
public class StudentGroupController {

    private final StudentGroupService groupService;
    private final RegistrationPeriodService periodService;
    private final TopicService topicService;
    private final TopicRegistrationRepository registrationRepository;

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public String myGroup(@AuthenticationPrincipal CustomUserDetails userDetails,
                          @RequestParam(required = false) Long periodId,
                          Model model) {
        Long studentId = userDetails.getStudent().getId();
        var activePeriods = periodService.getActivePeriodsForStudent();
        model.addAttribute("activePeriods", activePeriods);

        Long selectedPeriodId = (periodId != null) ? periodId : (!activePeriods.isEmpty() ? activePeriods.get(0).getId() : null);

        if (selectedPeriodId != null) {
            StudentGroupDTO myGroup = groupService.getMyGroupByPeriod(studentId, selectedPeriodId);
            model.addAttribute("myGroup", myGroup);
            model.addAttribute("availableTopics", topicService.getApprovedTopicsByPeriod(selectedPeriodId));
        }

        return "group/my_group";
    }

    @GetMapping("/register")
    @PreAuthorize("hasRole('STUDENT')")
    public String registerTopicView(@AuthenticationPrincipal CustomUserDetails userDetails,
                                    @RequestParam(required = false) Long periodId,
                                    Model model) {
        Long studentId = userDetails.getStudent().getId();
        var activePeriods = periodService.getActivePeriodsForStudent();
        model.addAttribute("activePeriods", activePeriods);

        Long selectedPeriodId = (periodId != null) ? periodId : (!activePeriods.isEmpty() ? activePeriods.get(0).getId() : null);

        if (selectedPeriodId != null) {
            StudentGroupDTO myGroup = groupService.getMyGroupByPeriod(studentId, selectedPeriodId);
            model.addAttribute("myGroup", myGroup);
            model.addAttribute("availableTopics", topicService.getApprovedTopicsByPeriod(selectedPeriodId));
        }

        return "group/register_topic";
    }


    @PostMapping("/create")
    @PreAuthorize("hasRole('STUDENT')")
    public String createGroup(@RequestParam Long periodId,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        try {
            groupService.createGroup(userDetails.getStudent().getId(), periodId);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo nhóm mới thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/student/group?periodId=" + periodId;
    }

    @PostMapping("/add-member")
    @PreAuthorize("hasRole('STUDENT')")
    public String addMember(@RequestParam Long groupId,
                            @RequestParam String studentCode,
                            @AuthenticationPrincipal CustomUserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        try {
            groupService.addMember(groupId, studentCode, userDetails.getStudent().getId());
            redirectAttributes.addFlashAttribute("successMessage", "Thêm thành viên thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/student/group";
    }

    @PostMapping("/register-topic")
    @PreAuthorize("hasRole('STUDENT')")
    public String registerTopic(@RequestParam Long groupId,
                                @RequestParam Long topicId,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            groupService.registerTopic(groupId, topicId, userDetails.getStudent().getId());
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký đề tài cho nhóm thành công! Chờ Khoa duyệt.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/student/group";
    }

    @GetMapping("/admin/registrations")
    @PreAuthorize("hasRole('ADMIN')")
    public String viewRegistrations(@RequestParam(required = false) Long periodId, Model model) {
        model.addAttribute("periods", periodService.getAllPeriods());
        if (periodId != null) {
            model.addAttribute("registrations", registrationRepository.findByGroupPeriodId(periodId));
        } else {
            model.addAttribute("registrations", registrationRepository.findAll());
        }
        return "group/admin_registrations";
    }

    @PostMapping("/admin/registrations/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public String approveRegistration(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        groupService.approveTopicRegistration(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã duyệt đề tài cho nhóm thành công.");
        return "redirect:/student/group/admin/registrations";
    }

    @PostMapping("/admin/registrations/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public String rejectRegistration(@PathVariable Long id, @RequestParam(required = false) String reason, RedirectAttributes redirectAttributes) {
        groupService.rejectTopicRegistration(id, reason);
        redirectAttributes.addFlashAttribute("successMessage", "Đã từ chối đăng ký đề tài của nhóm.");
        return "redirect:/student/group/admin/registrations";
    }
}
