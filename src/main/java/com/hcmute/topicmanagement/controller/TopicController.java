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

import com.hcmute.topicmanagement.enums.TopicStatus;
import com.hcmute.topicmanagement.exception.BusinessException;

@Controller
@RequestMapping("/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;
    private final RegistrationPeriodService periodService;
    private final DepartmentRepository departmentRepository;
    private final TeacherRepository teacherRepository;

    @GetMapping
    public String listTopics(@RequestParam(required = false) Long periodId,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             Model model) {
        model.addAttribute("periods", periodService.getAllPeriods());
        if (periodId != null) {
            model.addAttribute("topics", topicService.getTopicsByPeriod(periodId));
            model.addAttribute("selectedPeriodId", periodId);
        } else {
            model.addAttribute("topics", topicService.getAllTopics());
        }
        if (userDetails != null && userDetails.getTeacher() != null) {
            model.addAttribute("currentTeacherId", userDetails.getTeacher().getId());
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
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký đề tài mới thành công! Chờ duyệt.");
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
    public String viewTopic(@PathVariable Long id,
                            @AuthenticationPrincipal CustomUserDetails userDetails,
                            Model model) {
        TopicDTO topic = topicService.getTopicById(id);
        model.addAttribute("topic", topic);
        if (userDetails != null && userDetails.getTeacher() != null) {
            model.addAttribute("currentTeacherId", userDetails.getTeacher().getId());
        }
        return "topic/detail";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('TEACHER')")
    public String editForm(@PathVariable Long id,
                           @AuthenticationPrincipal CustomUserDetails userDetails,
                           Model model) {
        TopicDTO topic = topicService.getTopicById(id);
        Long currentTeacherId = userDetails.getTeacher().getId();

        if (!topic.getCreatedByTeacherId().equals(currentTeacherId)) {
            throw new BusinessException("Bạn không có quyền chỉnh sửa đề tài của người khác.");
        }
        if (topic.getStatus() == TopicStatus.APPROVED || topic.getStatus() == TopicStatus.ASSIGNED) {
            throw new BusinessException("Đề tài đã được duyệt hoặc đã phân công không thể chỉnh sửa.");
        }

        model.addAttribute("topicDTO", topic);
        model.addAttribute("periods", periodService.getActivePeriodsForTeacher());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("teachers", teacherRepository.findAll());
        return "topic/edit";
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasRole('TEACHER')")
    public String updateTopic(@PathVariable Long id,
                              @Valid @ModelAttribute("topicDTO") TopicDTO topicDTO,
                              BindingResult result,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("periods", periodService.getActivePeriodsForTeacher());
            model.addAttribute("departments", departmentRepository.findAll());
            model.addAttribute("teachers", teacherRepository.findAll());
            return "topic/edit";
        }
        try {
            topicService.updateTopic(id, topicDTO, userDetails.getTeacher().getId());
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật đề tài thành công!");
            return "redirect:/topics";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("periods", periodService.getActivePeriodsForTeacher());
            model.addAttribute("departments", departmentRepository.findAll());
            model.addAttribute("teachers", teacherRepository.findAll());
            return "topic/edit";
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('TEACHER')")
    public String deleteTopic(@PathVariable Long id,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        try {
            topicService.deleteTopic(id, userDetails.getTeacher().getId());
            redirectAttributes.addFlashAttribute("successMessage", "Xóa đề tài thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/topics";
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRUONG_KHOA')")
    public String approveTopic(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        topicService.approveTopic(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã phê duyệt đề tài thành công.");
        return "redirect:/topics";
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRUONG_KHOA')")
    public String rejectTopic(@PathVariable Long id, @RequestParam(required = false) String reason, RedirectAttributes redirectAttributes) {
        topicService.rejectTopic(id, reason);
        redirectAttributes.addFlashAttribute("successMessage", "Đã từ chối đề tài.");
        return "redirect:/topics";
    }
}
