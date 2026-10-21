package com.hcmute.topicmanagement.controller;

import com.hcmute.topicmanagement.dto.ReviewCouncilDTO;
import com.hcmute.topicmanagement.entity.Topic;
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

import java.util.ArrayList;
import java.util.List;

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

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        ReviewCouncilDTO councilDTO = councilService.getCouncilById(id);
        model.addAttribute("councilDTO", councilDTO);
        populateEditForm(model, councilDTO);
        return "council/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateCouncil(@PathVariable Long id,
                                @Valid @ModelAttribute("councilDTO") ReviewCouncilDTO councilDTO,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        councilDTO.setId(id);
        if (result.hasErrors()) {
            populateEditForm(model, councilDTO);
            return "council/edit";
        }
        try {
            councilService.updateCouncil(id, councilDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật Hội đồng phản biện thành công!");
            return "redirect:/admin/councils/" + id;
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            populateEditForm(model, councilDTO);
            return "council/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteCouncil(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            councilService.deleteCouncil(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa Hội đồng phản biện.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/councils";
    }

    // Danh sách đề tài chọn được khi sửa = đề tài chưa gán hội đồng nào + đề tài đang gán cho chính hội đồng này.
    private void populateEditForm(Model model, ReviewCouncilDTO councilDTO) {
        model.addAttribute("periods", periodService.getAllPeriods());
        model.addAttribute("teachers", teacherRepository.findAll());
        List<Topic> selectableTopics = new ArrayList<>(topicRepository.findUnassignedByStatus(TopicStatus.ASSIGNED));
        if (councilDTO.getAssignedTopics() != null) {
            councilDTO.getAssignedTopics().forEach(t -> {
                if (selectableTopics.stream().noneMatch(x -> x.getId().equals(t.getId()))) {
                    topicRepository.findById(t.getId()).ifPresent(selectableTopics::add);
                }
            });
        }
        model.addAttribute("assignedTopics", selectableTopics);
    }
}
