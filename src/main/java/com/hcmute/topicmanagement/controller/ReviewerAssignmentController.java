package com.hcmute.topicmanagement.controller;

import com.hcmute.topicmanagement.dto.TopicDTO;
import com.hcmute.topicmanagement.enums.PeriodType;
import com.hcmute.topicmanagement.exception.BusinessException;
import com.hcmute.topicmanagement.repository.TeacherRepository;
import com.hcmute.topicmanagement.repository.TopicReviewerRepository;
import com.hcmute.topicmanagement.service.ScoreService;
import com.hcmute.topicmanagement.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/reviewers")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ReviewerAssignmentController {

    private final ScoreService scoreService;
    private final TopicService topicService;
    private final TeacherRepository teacherRepository;
    private final TopicReviewerRepository topicReviewerRepository;

    @GetMapping("/{topicId}")
    public String assignmentForm(@PathVariable Long topicId, Model model) {
        TopicDTO topic = topicService.getTopicById(topicId);
        if (topic.getPeriodType() == PeriodType.KLTN) {
            throw new BusinessException("KLTN phải được phân công vào hội đồng phản biện.");
        }
        model.addAttribute("topic", topic);
        model.addAttribute("teachers", teacherRepository.findAll());
        model.addAttribute("selectedTeacherIds", topicReviewerRepository.findByTopicId(topicId).stream()
                .map(reviewer -> reviewer.getTeacher().getId()).toList());
        model.addAttribute("readyToScore", scoreService.isReadyToScore(topicId));
        return "scoring/reviewer_assignment";
    }

    @PostMapping("/{topicId}")
    public String assignReviewers(@PathVariable Long topicId,
                                  @RequestParam(value = "teacherIds", required = false) List<Long> teacherIds,
                                  RedirectAttributes redirectAttributes) {
        try {
            scoreService.assignReviewers(topicId, teacherIds);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật GVPB cho đề tài.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/reviewers/" + topicId;
        }
        return "redirect:/topics";
    }
}
