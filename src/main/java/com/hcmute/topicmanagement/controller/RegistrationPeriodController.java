package com.hcmute.topicmanagement.controller;

import com.hcmute.topicmanagement.dto.RegistrationPeriodDTO;
import com.hcmute.topicmanagement.enums.PeriodStatus;
import com.hcmute.topicmanagement.enums.PeriodType;
import com.hcmute.topicmanagement.service.RegistrationPeriodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/periods")
@PreAuthorize("hasAnyRole('ADMIN', 'TRUONG_KHOA')")
@RequiredArgsConstructor
public class RegistrationPeriodController {

    private final RegistrationPeriodService periodService;

    @GetMapping
    public String listPeriods(Model model) {
        model.addAttribute("periods", periodService.getAllPeriods());
        return "period/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("periodDTO", new RegistrationPeriodDTO());
        model.addAttribute("periodTypes", PeriodType.values());
        return "period/create";
    }

    @PostMapping("/create")
    public String createPeriod(@Valid @ModelAttribute("periodDTO") RegistrationPeriodDTO periodDTO,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("periodTypes", PeriodType.values());
            return "period/create";
        }
        try {
            periodService.createPeriod(periodDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo Đợt đăng ký mới thành công!");
            return "redirect:/admin/periods";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("periodTypes", PeriodType.values());
            return "period/create";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("periodDTO", periodService.getPeriodById(id));
        model.addAttribute("periodTypes", PeriodType.values());
        return "period/edit";
    }

    @PostMapping("/{id}/edit")
    public String updatePeriod(@PathVariable Long id,
                               @Valid @ModelAttribute("periodDTO") RegistrationPeriodDTO periodDTO,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("periodTypes", PeriodType.values());
            return "period/edit";
        }
        try {
            periodService.updatePeriod(id, periodDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật Đợt đăng ký thành công!");
            return "redirect:/admin/periods";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("periodTypes", PeriodType.values());
            return "period/edit";
        }
    }

    @PostMapping("/{id}/status")
    public String changeStatus(@PathVariable Long id,
                               @RequestParam PeriodStatus status,
                               RedirectAttributes redirectAttributes) {
        periodService.changeStatus(id, status);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái đợt thành thành công: " + status.getDisplayName());
        return "redirect:/admin/periods";
    }
}
