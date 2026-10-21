package com.hcmute.topicmanagement.controller;

import com.hcmute.topicmanagement.dto.RegistrationPeriodDTO;
import com.hcmute.topicmanagement.dto.StudentGroupDTO;
import com.hcmute.topicmanagement.dto.TopicDTO;
import com.hcmute.topicmanagement.entity.FinalResult;
import com.hcmute.topicmanagement.entity.RegistrationPeriod;
import com.hcmute.topicmanagement.entity.Submission;
import com.hcmute.topicmanagement.enums.PeriodStatus;
import com.hcmute.topicmanagement.enums.RoleName;
import com.hcmute.topicmanagement.enums.TopicRegistrationStatus;
import com.hcmute.topicmanagement.enums.TopicStatus;
import com.hcmute.topicmanagement.repository.*;
import com.hcmute.topicmanagement.security.CustomUserDetails;
import com.hcmute.topicmanagement.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final RegistrationPeriodService periodService;
    private final TopicService topicService;
    private final StudentGroupService groupService;
    private final ReviewCouncilService councilService;
    private final ScoreService scoreService;
    private final SubmissionService submissionService;
    private final NotificationService notificationService;
    private final RegistrationPeriodRepository periodRepository;
    private final TopicRegistrationRepository registrationRepository;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        RoleName role = userDetails.getUser().getRole().getName();
        model.addAttribute("currentUser", userDetails.getUser());
        model.addAttribute("currentRole", role.name());

        if (role == RoleName.ROLE_ADMIN) {
            // Dashboard Statistics for Dean / Admin
            model.addAttribute("totalPeriods", periodService.getAllPeriods().size());
            model.addAttribute("pendingTopicsCount", topicService.getAllTopics().stream()
                    .filter(t -> t.getStatus() == TopicStatus.PENDING_APPROVAL).count());
            model.addAttribute("pendingRegistrationsCount", registrationRepository.findByStatus(TopicRegistrationStatus.PENDING).size());
            model.addAttribute("totalCouncilsCount", councilService.getAllCouncils().size());
            model.addAttribute("publishedResultsCount", scoreService.getPublishedFinalResults().size());
            model.addAttribute("notifications", notificationService.getAllNotifications());

            return "dashboard/admin_dashboard";

        } else if (role == RoleName.ROLE_TEACHER) {
            // Dashboard for Lecturer / Teacher
            Long teacherId = userDetails.getTeacher() != null ? userDetails.getTeacher().getId() : null;
            if (teacherId != null) {
                List<TopicDTO> myTopics = topicService.getTopicsByTeacher(teacherId);
                model.addAttribute("myTopicsCount", myTopics.size());
                model.addAttribute("myTopics", myTopics);
                model.addAttribute("myCouncils", councilService.getCouncilsByTeacher(teacherId));
                model.addAttribute("pendingRegistrationsCount", registrationRepository.countByTopicCreatedByTeacherIdAndStatus(teacherId, TopicRegistrationStatus.PENDING));
            }
            model.addAttribute("activePeriods", periodService.getActivePeriodsForTeacher());
            model.addAttribute("notifications", notificationService.getNotificationsForRole("ROLE_TEACHER"));

            return "dashboard/teacher_dashboard";

        } else {
            // Dashboard for Student
            Long studentId = userDetails.getStudent() != null ? userDetails.getStudent().getId() : null;
            List<RegistrationPeriodDTO> activePeriods = periodService.getActivePeriodsForStudent();
            model.addAttribute("activePeriods", activePeriods);

            if (studentId != null) {
                // Tìm nhóm đã đăng ký đề tài của SV trên tất cả các đợt (ưu tiên đợt đang
                // active trước, rồi tới các đợt đã hoàn tất) để khối "Kết quả Đánh giá"
                // hiển thị đúng cả khi đợt chứa nhóm không còn active. Trước đây chỉ xét
                // đợt active đầu tiên nên SV không bao giờ thấy kết quả đã công bố.
                StudentGroupDTO myGroup = null;
                List<RegistrationPeriodDTO> periodsToCheck = new java.util.ArrayList<>(activePeriods);
                for (RegistrationPeriodDTO p : periodService.getAllPeriods()) {
                    if (periodsToCheck.stream().noneMatch(ap -> ap.getId().equals(p.getId()))) {
                        periodsToCheck.add(p);
                    }
                }
                for (RegistrationPeriodDTO p : periodsToCheck) {
                    StudentGroupDTO candidate = groupService.getMyGroupByPeriod(studentId, p.getId());
                    if (candidate != null && candidate.getRegisteredTopicId() != null) {
                        if (myGroup == null) {
                            myGroup = candidate;
                        }
                        if (scoreService.getFinalResultByTopic(candidate.getRegisteredTopicId()) != null) {
                            myGroup = candidate;
                            break;
                        }
                    }
                }
                if (myGroup == null && !activePeriods.isEmpty()) {
                    myGroup = groupService.getMyGroupByPeriod(studentId, activePeriods.get(0).getId());
                }
                model.addAttribute("myGroup", myGroup);

                if (myGroup != null && myGroup.getRegisteredTopicId() != null) {
                    TopicDTO topic = topicService.getTopicById(myGroup.getRegisteredTopicId());
                    model.addAttribute("registeredTopic", topic);

                    Submission latestSubmission = submissionService.getLatestSubmissionByGroup(myGroup.getId());
                    model.addAttribute("latestSubmission", latestSubmission);

                    FinalResult finalResult = scoreService.getFinalResultByTopic(myGroup.getRegisteredTopicId());
                    model.addAttribute("finalResult", finalResult);
                }
            }
            model.addAttribute("notifications", notificationService.getNotificationsForRole("ROLE_STUDENT"));

            return "dashboard/student_dashboard";
        }
    }
}
