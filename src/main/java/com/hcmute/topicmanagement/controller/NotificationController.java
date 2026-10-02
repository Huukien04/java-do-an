package com.hcmute.topicmanagement.controller;

import com.hcmute.topicmanagement.dto.NotificationDTO;
import com.hcmute.topicmanagement.enums.NotificationType;
import com.hcmute.topicmanagement.security.CustomUserDetails;
import com.hcmute.topicmanagement.service.NotificationService;
import com.hcmute.topicmanagement.service.RegistrationPeriodService;
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
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final RegistrationPeriodService periodService;

    @GetMapping
    public String listNotifications(Model model) {
        model.addAttribute("notifications", notificationService.getAllNotifications());
        return "notification/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRUONG_KHOA')")
    public String createForm(Model model) {
        model.addAttribute("notificationDTO", new NotificationDTO());
        model.addAttribute("notificationTypes", NotificationType.values());
        model.addAttribute("periods", periodService.getAllPeriods());
        return "notification/create";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRUONG_KHOA')")
    public String createNotification(@Valid @ModelAttribute("notificationDTO") NotificationDTO dto,
                                     BindingResult result,
                                     @AuthenticationPrincipal CustomUserDetails userDetails,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("notificationTypes", NotificationType.values());
            model.addAttribute("periods", periodService.getAllPeriods());
            return "notification/create";
        }
        try {
            notificationService.createNotification(dto, userDetails.getUserId());
            redirectAttributes.addFlashAttribute("successMessage", "Đăng tải thông báo mới thành công!");
            return "redirect:/notifications";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("notificationTypes", NotificationType.values());
            model.addAttribute("periods", periodService.getAllPeriods());
            return "notification/create";
        }
    }
}
