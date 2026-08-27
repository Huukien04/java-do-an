package com.hcmute.topicmanagement.controller;

import com.hcmute.topicmanagement.dto.TopicDTO;
import com.hcmute.topicmanagement.repository.DepartmentRepository;
import com.hcmute.topicmanagement.repository.TeacherRepository;
import com.hcmute.topicmanagement.security.CustomUserDetails;
import com.hcmute.topicmanagement.service.RegistrationPeriodService;
import com.hcmute.topicmanagement.service.TopicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;
    private final RegistrationPeriodService periodService;
    private final DepartmentRepository departmentRepository;
    private final TeacherRepository teacherRepository;

    @GetMapping
    public String listTopics(@RequestParam(required = false) Long periodId, Model model) {
        model.addAttribute("periods", periodService.getAllPeriods());
        if (periodId != null) {
            model.addAttribute("topics", topicService.getTopicsByPeriod(periodId));
            model.addAttribute("selectedPeriodId", periodId);
        } else {
            model.addAttribute("topics", topicService.getAllTopics());
        }
        return "topic/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasRole('TEACHER')")
    public String createForm(Model model) {
        model.addAttribute("topicDTO", new TopicDTO());
        model.addAttribute("periods", periodService.getActivePeriodsForTeacher());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("teachers", teacherRepository.findAll());
        return "topic/create";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('TEACHER')")
    public String createTopic(@Valid @ModelAttribute("topicDTO") TopicDTO topicDTO,
                              BindingResult result,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("periods", periodService.getActivePeriodsForTeacher());
            model.addAttribute("departments", departmentRepository.findAll());
            model.addAttribute("teachers", teacherRepository.findAll());
            return "topic/create";
        }
        try {
            topicService.createTopic(topicDTO, userDetails.getTeacher().getId());
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký đề tài mới thành công! Chờ Khoa duyệt.");
            return "redirect:/dashboard";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("periods", periodService.getActivePeriodsForTeacher());
            model.addAttribute("departments", departmentRepository.findAll());
            model.addAttribute("teachers", teacherRepository.findAll());
            return "topic/create";
        }
    }

    @GetMapping("/{id}")
    public String viewTopic(@PathVariable Long id, Model model) {
        model.addAttribute("topic", topicService.getTopicById(id));
        return "topic/detail";
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public String approveTopic(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        topicService.approveTopic(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã phê duyệt đề tài thành công.");
        return "redirect:/topics";
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public String rejectTopic(@PathVariable Long id, @RequestParam(required = false) String reason, RedirectAttributes redirectAttributes) {
        topicService.rejectTopic(id, reason);
        redirectAttributes.addFlashAttribute("successMessage", "Đã từ chối đề tài.");
        return "redirect:/topics";
    }
}
