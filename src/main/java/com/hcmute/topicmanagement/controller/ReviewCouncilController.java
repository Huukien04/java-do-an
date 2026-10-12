package com.hcmute.topicmanagement.controller;

import com.hcmute.topicmanagement.dto.ReviewCouncilDTO;
import com.hcmute.topicmanagement.enums.TopicStatus;
import com.hcmute.topicmanagement.repository.TeacherRepository;
import com.hcmute.topicmanagement.repository.TopicRepository;
import com.hcmute.topicmanagement.service.RegistrationPeriodService;
import com.hcmute.topicmanagement.service.ReviewCouncilService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/councils")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ReviewCouncilController {

    private final ReviewCouncilService councilService;
    private final RegistrationPeriodService periodService;
    private final TeacherRepository teacherRepository;
    private final TopicRepository topicRepository;

    @GetMapping
    public String listCouncils(@RequestParam(required = false) Long periodId, Model model) {
        model.addAttribute("periods", periodService.getAllPeriods());
        if (periodId != null) {
            model.addAttribute("councils", councilService.getCouncilsByPeriod(periodId));
        } else {
            model.addAttribute("councils", councilService.getAllCouncils());
        }
        return "council/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("councilDTO", new ReviewCouncilDTO());
        model.addAttribute("periods", periodService.getAllPeriods());
        model.addAttribute("teachers", teacherRepository.findAll());
        model.addAttribute("assignedTopics", topicRepository.findUnassignedByStatus(TopicStatus.ASSIGNED));
        return "council/create";
    }

    @PostMapping("/create")
    public String createCouncil(@Valid @ModelAttribute("councilDTO") ReviewCouncilDTO councilDTO,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("periods", periodService.getAllPeriods());
            model.addAttribute("teachers", teacherRepository.findAll());
            model.addAttribute("assignedTopics", topicRepository.findUnassignedByStatus(TopicStatus.ASSIGNED));
            return "council/create";
        }
        try {
            councilService.createCouncil(councilDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Thành lập Hội đồng phản biện thành công!");
            return "redirect:/admin/councils";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("periods", periodService.getAllPeriods());
            model.addAttribute("teachers", teacherRepository.findAll());
            model.addAttribute("assignedTopics", topicRepository.findUnassignedByStatus(TopicStatus.ASSIGNED));
            return "council/create";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("council", councilService.getCouncilById(id));
        return "council/detail";
    }
}
