package com.hcmute.topicmanagement.controller;

import com.hcmute.topicmanagement.dto.ScoreDTO;
import com.hcmute.topicmanagement.dto.TopicDTO;
import com.hcmute.topicmanagement.entity.CouncilTopic;
import com.hcmute.topicmanagement.entity.FinalResult;
import com.hcmute.topicmanagement.enums.CouncilRole;
import com.hcmute.topicmanagement.enums.PeriodType;
import com.hcmute.topicmanagement.enums.RoleName;
import com.hcmute.topicmanagement.exception.BusinessException;
import com.hcmute.topicmanagement.exception.ResourceNotFoundException;
import com.hcmute.topicmanagement.repository.CouncilMemberRepository;
import com.hcmute.topicmanagement.repository.CouncilTopicRepository;
import com.hcmute.topicmanagement.repository.FinalResultRepository;
import com.hcmute.topicmanagement.repository.TopicRegistrationRepository;
import com.hcmute.topicmanagement.security.CustomUserDetails;
import com.hcmute.topicmanagement.service.RegistrationPeriodService;
import com.hcmute.topicmanagement.service.ReviewCouncilService;
import com.hcmute.topicmanagement.service.ScoreService;
import com.hcmute.topicmanagement.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/scoring")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;
    private final TopicService topicService;
    private final ReviewCouncilService councilService;
    private final RegistrationPeriodService periodService;
    private final FinalResultRepository finalResultRepository;
    private final TopicRegistrationRepository topicRegistrationRepository;
    private final CouncilTopicRepository councilTopicRepository;
    private final CouncilMemberRepository councilMemberRepository;

    @GetMapping("/results")
    public String viewPublishedResults(@AuthenticationPrincipal CustomUserDetails userDetails,
                                       @RequestParam(required = false) Long periodId,
                                       Model model) {
        boolean isAdminOrTeacher = userDetails != null &&
                (userDetails.getUser().getRole().getName() == RoleName.ROLE_ADMIN ||
                 userDetails.getUser().getRole().getName() == RoleName.ROLE_TEACHER);

        List<FinalResult> results;
        if (isAdminOrTeacher) {
            results = periodId == null
                    ? finalResultRepository.findAllByOrderByTopicTitleAsc()
                    : finalResultRepository.findByTopicPeriodIdOrderByTopicTitleAsc(periodId);
        } else {
            results = periodId == null
                    ? scoreService.getPublishedFinalResults()
                    : scoreService.getPublishedFinalResultsByPeriod(periodId);
            if (userDetails != null && userDetails.getStudent() != null) {
                Set<Long> ownTopicIds = topicRegistrationRepository.findAllByStudentId(
                                userDetails.getStudent().getId()).stream()
                        .map(registration -> registration.getTopic().getId())
                        .collect(Collectors.toSet());
                results = results.stream()
                        .filter(result -> ownTopicIds.contains(result.getTopic().getId()))
                        .toList();
            }
        }

        model.addAttribute("results", results);
        model.addAttribute("periods", periodService.getAllPeriods());
        model.addAttribute("isAdminOrTeacher", isAdminOrTeacher);
        model.addAttribute("selectedPeriodId", periodId);
        return "scoring/results_lookup";
    }

    @GetMapping("/teacher")
    @PreAuthorize("hasRole('TEACHER')")
    public String teacherScoringList(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long teacherId = userDetails.getTeacher().getId();
        var councils = councilService.getCouncilsByTeacher(teacherId);
        councils.forEach(council -> council.setAssignedTopics(council.getAssignedTopics().stream()
                .filter(topic -> scoreService.canViewScoring(topic.getId(), teacherId))
                .toList()));
        model.addAttribute("councils", councils);
        model.addAttribute("reviewerTopics", scoreService.getReviewerTopicIds(teacherId).stream()
                .map(topicService::getTopicById).toList());
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
        if (isTeacher && !scoreService.canViewScoring(topicId, teacherId)) {
            throw new AccessDeniedException("Bạn không được phân công chấm đề tài này.");
        }
        List<ScoreDTO> existingScores = scoreService.getScoresByTopic(topicId);
        var scoringProgress = scoreService.getScoringProgress(topicId);

        model.addAttribute("topic", topic);
        model.addAttribute("existingScores", existingScores);
        model.addAttribute("scoringProgress", scoringProgress);
        model.addAttribute("finalResult", scoreService.getFinalResultByTopic(topicId));
        model.addAttribute("scoreCount", existingScores.size());
        model.addAttribute("criteriaCount", topic.getPeriodType() == PeriodType.KLTN ? 4 : 3);

        ScoreDTO myScore = teacherId == null
                ? ScoreDTO.builder().topicId(topicId).build()
                : existingScores.stream()
                        .filter(s -> teacherId.equals(s.getTeacherId()))
                        .findFirst()
                        .orElse(ScoreDTO.builder().topicId(topicId).teacherId(teacherId).build());

        model.addAttribute("scoreDTO", myScore);
        boolean isChairman = teacherId != null && scoreService.isChairman(topicId, teacherId);
        model.addAttribute("isChairman", isChairman);
        model.addAttribute("canConfirmResult", isChairman
                && (scoringProgress.isComplete() || scoringProgress.isDeadlinePassed()));
        model.addAttribute("councilId", councilTopicRepository.findByTopicId(topicId)
                .map(assignment -> assignment.getCouncil().getId()).orElse(null));

        boolean isAdvisor = teacherId != null
                && (teacherId.equals(topic.getPrimaryAdvisorId()) || teacherId.equals(topic.getSecondaryAdvisorId()));
        model.addAttribute("isAdvisor", isAdvisor);
        var currentResult = scoreService.getFinalResultByTopic(topicId);
        model.addAttribute("canSubmitScore", isTeacher && !isAdvisor
                && scoreService.canViewScoring(topicId, teacherId)
                && (currentResult == null || !currentResult.isPublished()));
        return "scoring/topic_form";
    }

    @PostMapping("/submit")
    @PreAuthorize("hasRole('TEACHER')")
    public String submitScore(@RequestParam Long topicId,
                              @RequestParam BigDecimal contentScore,
                              @RequestParam BigDecimal productScore,
                              @RequestParam BigDecimal reportScore,
                              @RequestParam(required = false) BigDecimal presentationScore,
                              @RequestParam(required = false) String comments,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        try {
            ScoreDTO dto = ScoreDTO.builder()
                    .topicId(topicId)
                    .contentScore(contentScore)
                    .productScore(productScore)
                    .reportScore(reportScore)
                    .presentationScore(presentationScore)
                    .comments(comments)
                    .build();
            scoreService.submitScore(dto, userDetails.getTeacher().getId());
            redirectAttributes.addFlashAttribute("successMessage", "Gửi điểm đánh giá thành công!");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/scoring/topic/" + topicId;
    }

    @PostMapping("/topic/{topicId}")
    @PreAuthorize("hasRole('TEACHER')")
    public String submitScoreAtTopic(@PathVariable Long topicId,
                                     @RequestParam BigDecimal contentScore,
                                     @RequestParam BigDecimal productScore,
                                     @RequestParam BigDecimal reportScore,
                                     @RequestParam(required = false) BigDecimal presentationScore,
                                     @RequestParam(required = false) String comments,
                                     @AuthenticationPrincipal CustomUserDetails userDetails,
                                     RedirectAttributes redirectAttributes) {
        return submitScore(topicId, contentScore, productScore, reportScore, presentationScore,
                comments, userDetails, redirectAttributes);
    }

    @PostMapping("/chairman/confirm")
    @PreAuthorize("hasRole('TEACHER')")
    public String chairmanConfirmResult(@RequestParam Long topicId,
                                        @RequestParam(required = false) String chairmanComments,
                                        @AuthenticationPrincipal CustomUserDetails userDetails,
                                        RedirectAttributes redirectAttributes) {
        try {
            scoreService.confirmFinalResult(topicId, chairmanComments, userDetails.getTeacher().getId());
            redirectAttributes.addFlashAttribute("successMessage", "Tổng hợp & xác nhận điểm hội đồng thành công.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/scoring/topic/" + topicId;
    }

    @GetMapping("/council/{councilId}/summary")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public String councilSummary(@PathVariable Long councilId,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 Model model) {
        boolean isAdmin = userDetails.getUser().getRole().getName() == RoleName.ROLE_ADMIN;
        if (!isAdmin && (userDetails.getTeacher() == null
                || !councilMemberRepository.existsByCouncilIdAndTeacherIdAndRole(
                        councilId, userDetails.getTeacher().getId(), CouncilRole.CHAIRMAN))) {
            throw new AccessDeniedException("Chỉ Chủ tịch hội đồng và quản trị viên được xem bảng tổng hợp.");
        }
        List<CouncilTopic> assignments = councilTopicRepository.findByCouncilId(councilId);
        if (assignments.isEmpty()) {
            throw new ResourceNotFoundException("Hội đồng hoặc đề tài", "councilId", councilId);
        }
        model.addAttribute("councilId", councilId);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("summaryTopics", assignments.stream().map(assignment -> {
            Long topicId = assignment.getTopic().getId();
            var progress = scoreService.getScoringProgress(topicId);
            return new CouncilSummaryTopic(topicId, assignment.getTopic().getTitle(),
                    scoreService.getScoresByTopic(topicId), progress,
                    scoreService.getFinalResultByTopic(topicId),
                    !isAdmin && userDetails.getTeacher() != null
                            && scoreService.isChairman(topicId, userDetails.getTeacher().getId())
                            && (progress.isComplete() || progress.isDeadlinePassed()));
        }).toList());
        return "scoring/council_summary";
    }

    @PostMapping("/admin/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public String publishResults(@RequestParam Long periodId,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        try {
            periodService.performAction(periodId, com.hcmute.topicmanagement.enums.PeriodAction.PUBLISH_RESULTS,
                    null, userDetails.getUserId());
            redirectAttributes.addFlashAttribute("successMessage", "Công bố kết quả thành công cho toàn bộ Đợt đăng ký!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/periods";
    }

    @PostMapping("/admin/unpublish")
    @PreAuthorize("hasRole('ADMIN')")
    public String unpublishResults(@RequestParam Long periodId,
                                   @AuthenticationPrincipal CustomUserDetails userDetails,
                                   RedirectAttributes redirectAttributes) {
        try {
            periodService.performAction(periodId, com.hcmute.topicmanagement.enums.PeriodAction.UNPUBLISH_RESULTS,
                    null, userDetails.getUserId());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã hủy công bố. Điểm được mở khóa để chỉnh sửa và công bố lại.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/periods";
    }

    public record CouncilSummaryTopic(Long topicId, String title, List<ScoreDTO> scores,
                                      com.hcmute.topicmanagement.dto.ScoreProgressDTO progress,
                                      FinalResult result, boolean canConfirm) {
    }
}
