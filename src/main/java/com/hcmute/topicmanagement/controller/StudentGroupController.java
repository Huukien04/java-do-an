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
        model.addAttribute("selectedPeriodId", selectedPeriodId);

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
        model.addAttribute("selectedPeriodId", selectedPeriodId);

        if (selectedPeriodId != null) {
            StudentGroupDTO myGroup = groupService.getMyGroupByPeriod(studentId, selectedPeriodId);
            model.addAttribute("myGroup", myGroup);
            model.addAttribute("availableTopics", topicService.getApprovedTopicsByPeriod(selectedPeriodId));
        }

        return "group/register_topic";
    }


    @PostMapping("/create")
    @PreAuthorize("hasRole('STUDENT')")
    public String createGroup(@RequestParam(required = false) Long periodId,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        if (periodId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đợt đăng ký phù hợp để tạo nhóm.");
            return "redirect:/student/group";
        }
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
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký đề tài cho nhóm thành công! Chờ duyệt.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/student/group";
    }
}
