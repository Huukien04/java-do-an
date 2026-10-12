package com.hcmute.topicmanagement.controller;

import com.hcmute.topicmanagement.dto.ScoreDTO;
import com.hcmute.topicmanagement.dto.TopicDTO;
import com.hcmute.topicmanagement.entity.FinalResult;
import com.hcmute.topicmanagement.enums.RoleName;
import com.hcmute.topicmanagement.repository.FinalResultRepository;
import com.hcmute.topicmanagement.security.CustomUserDetails;
import com.hcmute.topicmanagement.service.RegistrationPeriodService;
import com.hcmute.topicmanagement.service.ReviewCouncilService;
import com.hcmute.topicmanagement.service.ScoreService;
import com.hcmute.topicmanagement.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/scoring")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;
    private final TopicService topicService;
    private final ReviewCouncilService councilService;
    private final RegistrationPeriodService periodService;
    private final FinalResultRepository finalResultRepository;

    @GetMapping("/results")
    public String viewPublishedResults(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        boolean isAdminOrTeacher = userDetails != null &&
                (userDetails.getUser().getRole().getName() == RoleName.ROLE_ADMIN ||
                 userDetails.getUser().getRole().getName() == RoleName.ROLE_TEACHER);

        List<FinalResult> results;
        if (isAdminOrTeacher) {
            results = finalResultRepository.findAll();
        } else {
            results = scoreService.getPublishedFinalResults();
        }

        model.addAttribute("results", results);
        model.addAttribute("periods", periodService.getAllPeriods());
        model.addAttribute("isAdminOrTeacher", isAdminOrTeacher);
        return "scoring/results_lookup";
    }

    @GetMapping("/teacher")
    @PreAuthorize("hasRole('TEACHER')")
    public String teacherScoringList(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long teacherId = userDetails.getTeacher().getId();
        model.addAttribute("councils", councilService.getCouncilsByTeacher(teacherId));
        return "scoring/teacher_list";
    }

    @GetMapping("/topic/{topicId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public String topicScoringForm(@PathVariable Long topicId,
                                   @AuthenticationPrincipal CustomUserDetails userDetails,
                                   Model model) {
        TopicDTO topic = topicService.getTopicById(topicId);
        boolean isTeacher = userDetails.getUser().getRole().getName() == RoleName.ROLE_TEACHER;
        Long teacherId = isTeacher && userDetails.getTeacher() != null
                ? userDetails.getTeacher().getId()
                : null;
        List<ScoreDTO> existingScores = scoreService.getScoresByTopic(topicId);

        model.addAttribute("topic", topic);
        model.addAttribute("existingScores", existingScores);

        ScoreDTO myScore = teacherId == null
                ? ScoreDTO.builder().topicId(topicId).build()
                : existingScores.stream()
                        .filter(s -> teacherId.equals(s.getTeacherId()))
                        .findFirst()
                        .orElse(ScoreDTO.builder().topicId(topicId).teacherId(teacherId).build());

        model.addAttribute("scoreDTO", myScore);

        boolean isAdvisor = teacherId != null
                && (teacherId.equals(topic.getPrimaryAdvisorId()) || teacherId.equals(topic.getSecondaryAdvisorId()));
        model.addAttribute("isAdvisor", isAdvisor);
        model.addAttribute("canSubmitScore", isTeacher && !isAdvisor);

        return "scoring/topic_form";
    }

    @PostMapping("/submit")
    @PreAuthorize("hasRole('TEACHER')")
    public String submitScore(@RequestParam Long topicId,
                              @RequestParam BigDecimal scoreValue,
                              @RequestParam(required = false) String comments,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        try {
            ScoreDTO dto = ScoreDTO.builder()
                    .topicId(topicId)
                    .scoreValue(scoreValue)
                    .comments(comments)
                    .build();
            scoreService.submitScore(dto, userDetails.getTeacher().getId());
            redirectAttributes.addFlashAttribute("successMessage", "Gửi điểm đánh giá thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/scoring/topic/" + topicId;
    }

    @PostMapping("/chairman/confirm")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public String chairmanConfirmResult(@RequestParam Long topicId,
                                        @RequestParam(required = false) String chairmanComments,
                                        RedirectAttributes redirectAttributes) {
        try {
            scoreService.calculateAndSaveFinalResult(topicId, chairmanComments);
            redirectAttributes.addFlashAttribute("successMessage", "Tổng hợp & xác nhận điểm hội đồng thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/scoring/topic/" + topicId;
    }

    @PostMapping("/admin/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public String publishResults(@RequestParam Long periodId, RedirectAttributes redirectAttributes) {
        try {
            scoreService.publishResults(periodId);
            redirectAttributes.addFlashAttribute("successMessage", "Công bố kết quả thành công cho toàn bộ Đợt đăng ký!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/periods";
    }
}
