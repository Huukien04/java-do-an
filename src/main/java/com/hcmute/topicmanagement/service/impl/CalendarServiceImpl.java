package com.hcmute.topicmanagement.service.impl;

import com.hcmute.topicmanagement.dto.CalendarEventDTO;
import com.hcmute.topicmanagement.entity.CouncilTopic;
import com.hcmute.topicmanagement.entity.RegistrationPeriod;
import com.hcmute.topicmanagement.entity.ReviewCouncil;
import com.hcmute.topicmanagement.entity.StudentGroup;
import com.hcmute.topicmanagement.entity.TopicRegistration;
import com.hcmute.topicmanagement.enums.RoleName;
import com.hcmute.topicmanagement.repository.CouncilMemberRepository;
import com.hcmute.topicmanagement.repository.CouncilTopicRepository;
import com.hcmute.topicmanagement.repository.RegistrationPeriodRepository;
import com.hcmute.topicmanagement.repository.ReviewCouncilRepository;
import com.hcmute.topicmanagement.repository.StudentGroupRepository;
import com.hcmute.topicmanagement.repository.TopicRegistrationRepository;
import com.hcmute.topicmanagement.security.CustomUserDetails;
import com.hcmute.topicmanagement.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarServiceImpl implements CalendarService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private final RegistrationPeriodRepository periodRepository;
    private final ReviewCouncilRepository councilRepository;
    private final CouncilMemberRepository councilMemberRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final TopicRegistrationRepository topicRegistrationRepository;
    private final CouncilTopicRepository councilTopicRepository;

    @Override
    public List<CalendarEventDTO> getEventsForUser(CustomUserDetails user) {
        if (user == null || user.getUser() == null) {
            return List.of();
        }
        RoleName role = user.getUser().getRole().getName();
        List<CalendarEventDTO> events = new ArrayList<>();

        if (role == RoleName.ROLE_ADMIN) {
            periodRepository.findAll().forEach(period -> addPeriodEvents(events, period, true, true));
            councilRepository.findAll().forEach(council -> addDefense(events, council, "Báo cáo hội đồng"));
        } else if (role == RoleName.ROLE_TEACHER) {
            periodRepository.findAll().forEach(period -> addPeriodEvents(events, period, true, false));
            Long teacherId = user.getTeacher() != null ? user.getTeacher().getId() : null;
            if (teacherId != null) {
                councilMemberRepository.findByTeacherId(teacherId).forEach(member ->
                        addDefense(events, member.getCouncil(), "Lịch hội đồng của bạn"));
            }
        } else {
            Long studentId = user.getStudent() != null ? user.getStudent().getId() : null;
            Set<Long> seenPeriods = new LinkedHashSet<>();
            if (studentId != null) {
                List<StudentGroup> groups = studentGroupRepository.findGroupsByStudentId(studentId);
                for (StudentGroup group : groups) {
                    RegistrationPeriod period = group.getPeriod();
                    if (period != null && seenPeriods.add(period.getId())) {
                        addPeriodEvents(events, period, false, true);
                    }
                    Optional<TopicRegistration> registration = topicRegistrationRepository.findByGroupId(group.getId());
                    if (registration.isPresent() && registration.get().getTopic() != null) {
                        councilTopicRepository.findByTopicId(registration.get().getTopic().getId())
                                .map(CouncilTopic::getCouncil)
                                .ifPresent(council -> addDefense(events, council, "Báo cáo đề tài nhóm " + group.getGroupCode()));
                    }
                }
            }
            if (events.isEmpty()) {
                periodRepository.findAll().forEach(period -> addPeriodEvents(events, period, false, true));
            }
        }

        events.sort(Comparator.comparing(CalendarEventDTO::getDate, Comparator.nullsLast(String::compareTo))
                .thenComparing(CalendarEventDTO::getTime, Comparator.nullsLast(String::compareTo)));
        return events;
    }

    private void addPeriodEvents(List<CalendarEventDTO> events, RegistrationPeriod period, boolean includeTeacher, boolean includeStudent) {
        String name = period.getName();
        if (includeTeacher) {
            addDate(events, period.getTeacherStartDate(), "GV bắt đầu đăng ký", name, "gv");
            addDate(events, period.getTeacherEndDate(), "Hạn GV đăng ký đề tài", name, "deadline");
        }
        if (includeStudent) {
            addDate(events, period.getStudentStartDate(), "SV bắt đầu đăng ký", name, "sv");
            addDate(events, period.getStudentEndDate(), "Hạn SV đăng ký đề tài", name, "deadline");
        }
        addDate(events, period.getReviewSubmissionDeadline(), "Hạn nộp điểm phản biện", name, "deadline");
        addDate(events, period.getCouncilReportDate(), "Ngày báo cáo hội đồng", name, "report");
    }

    private void addDefense(List<CalendarEventDTO> events, ReviewCouncil council, String title) {
        if (council == null) {
            return;
        }
        String detail = council.getName();
        if (council.getLocation() != null && !council.getLocation().isBlank()) {
            detail += " · " + council.getLocation();
        }
        addDate(events, council.getDefenseDate(), title, detail, "defense");
    }

    private void addDate(List<CalendarEventDTO> events, LocalDateTime dateTime, String title, String detail, String type) {
        if (dateTime == null) {
            return;
        }
        events.add(CalendarEventDTO.builder()
                .date(dateTime.toLocalDate().format(DATE))
                .time(dateTime.format(TIME))
                .title(title)
                .detail(detail)
                .type(type)
                .build());
    }
}
