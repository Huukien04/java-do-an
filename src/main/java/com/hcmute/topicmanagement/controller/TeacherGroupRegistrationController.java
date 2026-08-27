package com.hcmute.topicmanagement.controller;

import com.hcmute.topicmanagement.entity.TopicRegistration;
import com.hcmute.topicmanagement.exception.BusinessException;
import com.hcmute.topicmanagement.repository.TopicRegistrationRepository;
import com.hcmute.topicmanagement.security.CustomUserDetails;
import com.hcmute.topicmanagement.service.RegistrationPeriodService;
import com.hcmute.topicmanagement.service.StudentGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/teacher/registrations")
@PreAuthorize("hasRole('TEACHER')")
@RequiredArgsConstructor
public class TeacherGroupRegistrationController {

    private final StudentGroupService groupService;
    private final RegistrationPeriodService periodService;
    private final TopicRegistrationRepository registrationRepository;

    @GetMapping
    public String viewRegistrations(@AuthenticationPrincipal CustomUserDetails userDetails,
                                    @RequestParam(required = false) Long periodId,
                                    Model model) {
        if (userDetails.getTeacher() == null) {
            throw new BusinessException("Tài khoản chưa được liên kết với hồ sơ Giảng viên.");
        }
        Long teacherId = userDetails.getTeacher().getId();

        model.addAttribute("periods", periodService.getAllPeriods());
        List<TopicRegistration> registrations;
        if (periodId != null) {
            registrations = registrationRepository.findByTopicCreatedByTeacherIdAndGroupPeriodId(teacherId, periodId);
        } else {
            registrations = registrationRepository.findByTopicCreatedByTeacherId(teacherId);
        }
        model.addAttribute("registrations", registrations);
        model.addAttribute("selectedPeriodId", periodId);

        return "group/teacher_registrations";
    }

    @PostMapping("/{id}/approve")
    public String approveRegistration(@PathVariable Long id,
                                      @AuthenticationPrincipal CustomUserDetails userDetails,
                                      RedirectAttributes redirectAttributes) {
        Long teacherId = userDetails.getTeacher().getId();
        TopicRegistration reg = registrationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy thông tin đăng ký."));

        if (!reg.getTopic().getCreatedByTeacher().getId().equals(teacherId)) {
            throw new BusinessException("Bạn chỉ được quyền duyệt đăng ký cho đề tài do chính mình hướng dẫn.");
        }

        groupService.approveTopicRegistration(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã duyệt đề tài cho nhóm thành công.");
        return "redirect:/teacher/registrations";
    }

    @PostMapping("/{id}/reject")
    public String rejectRegistration(@PathVariable Long id,
                                     @RequestParam(required = false) String reason,
                                     @AuthenticationPrincipal CustomUserDetails userDetails,
                                     RedirectAttributes redirectAttributes) {
        Long teacherId = userDetails.getTeacher().getId();
        TopicRegistration reg = registrationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy thông tin đăng ký."));

        if (!reg.getTopic().getCreatedByTeacher().getId().equals(teacherId)) {
            throw new BusinessException("Bạn chỉ được quyền từ chối đăng ký cho đề tài do chính mình hướng dẫn.");
        }

        groupService.rejectTopicRegistration(id, reason);
        redirectAttributes.addFlashAttribute("successMessage", "Đã từ chối đăng ký đề tài của nhóm.");
        return "redirect:/teacher/registrations";
    }
}
