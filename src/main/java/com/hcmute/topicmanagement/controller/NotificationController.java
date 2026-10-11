package com.hcmute.topicmanagement.controller;

import com.hcmute.topicmanagement.dto.NotificationDTO;
import com.hcmute.topicmanagement.enums.NotificationType;
import com.hcmute.topicmanagement.security.CustomUserDetails;
import com.hcmute.topicmanagement.repository.UserRepository;
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
    private final UserRepository userRepository;

    @GetMapping
    public String listNotifications(@RequestParam(defaultValue = "general") String view,
                                    @AuthenticationPrincipal CustomUserDetails userDetails,
                                    Model model) {
        String selectedView = "personal".equals(view) ? "personal" : "general";
        String roleName = userDetails.getUser().getRole().getName().name();
        model.addAttribute("view", selectedView);
        model.addAttribute("notifications", "personal".equals(selectedView)
                ? notificationService.getPersonalNotifications(userDetails.getUserId())
                : notificationService.getGeneralNotifications(roleName));
        return "notification/list";
    }

    @GetMapping("/{id}")
    public String notificationDetail(@PathVariable Long id,
                                     @RequestParam(defaultValue = "general") String view,
                                     @AuthenticationPrincipal CustomUserDetails userDetails,
                                     Model model) {
        String selectedView = "personal".equals(view) ? "personal" : "general";
        String roleName = userDetails.getUser().getRole().getName().name();
        model.addAttribute("view", selectedView);
        model.addAttribute("notification",
                notificationService.getNotificationForUser(id, userDetails.getUserId(), roleName));
        return "notification/detail";
    }

    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createForm(Model model) {
        populateCreateForm(model, new NotificationDTO());
        return "notification/create";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createNotification(@Valid @ModelAttribute("notificationDTO") NotificationDTO dto,
                                     BindingResult result,
                                     @AuthenticationPrincipal CustomUserDetails userDetails,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        if ("INDIVIDUAL".equals(dto.getTargetRole())
                && (dto.getTargetUserIds() == null || dto.getTargetUserIds().isEmpty())) {
            result.rejectValue("targetUserIds", "notification.recipients.required",
                    "Chọn ít nhất một người nhận.");
        }
        if (result.hasErrors()) {
            populateCreateForm(model, dto);
            return "notification/create";
        }
        try {
            notificationService.createNotification(dto, userDetails.getUserId());
            redirectAttributes.addFlashAttribute("successMessage", "Đăng tải thông báo mới thành công!");
            return "redirect:/notifications";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            populateCreateForm(model, dto);
            return "notification/create";
        }
    }

    private void populateCreateForm(Model model, NotificationDTO dto) {
        model.addAttribute("notificationDTO", dto);
        model.addAttribute("notificationTypes", NotificationType.values());
        model.addAttribute("periods", periodService.getAllPeriods());
        model.addAttribute("users", userRepository.findByEnabledTrueOrderByFullNameAsc());
    }
}
