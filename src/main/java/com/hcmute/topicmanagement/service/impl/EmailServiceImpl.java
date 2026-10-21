package com.hcmute.topicmanagement.service.impl;

import com.hcmute.topicmanagement.entity.FinalResult;
import com.hcmute.topicmanagement.entity.StudentGroup;
import com.hcmute.topicmanagement.entity.TopicRegistration;
import com.hcmute.topicmanagement.enums.TopicRegistrationStatus;
import com.hcmute.topicmanagement.repository.FinalResultRepository;
import com.hcmute.topicmanagement.repository.RegistrationPeriodRepository;
import com.hcmute.topicmanagement.repository.TopicRegistrationRepository;
import com.hcmute.topicmanagement.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final FinalResultRepository finalResultRepository;
    private final TopicRegistrationRepository topicRegistrationRepository;
    private final RegistrationPeriodRepository periodRepository;

    @Value("${app.mail.enabled:false}")
    private boolean enabled;

    @Value("${app.mail.host:smtp.gmail.com}")
    private String host;

    @Value("${app.mail.port:587}")
    private int port;

    @Value("${app.mail.username:}")
    private String username;

    @Value("${app.mail.password:}")
    private String password;

    @Async
    @Override
    public void notifyResultsPublished(Long periodId) {
        if (!enabled || username == null || username.isBlank() || password == null || password.isBlank()) {
            log.info("Bo qua gui email cong bo ket qua cho dot {}: chua cau hinh app.mail (MAIL_ENABLED/MAIL_USERNAME/MAIL_PASSWORD).", periodId);
            return;
        }
        try {
            String periodName = periodRepository.findById(periodId)
                    .map(period -> period.getName())
                    .orElse("");
            JavaMailSenderImpl sender = buildSender();
            List<FinalResult> results = finalResultRepository.findByTopicPeriodIdOrderByTopicTitleAsc(periodId);
            int sent = 0;
            for (FinalResult result : results) {
                TopicRegistration registration = topicRegistrationRepository.findByTopicId(result.getTopic().getId())
                        .filter(item -> item.getStatus() == TopicRegistrationStatus.APPROVED)
                        .orElse(null);
                if (registration == null) {
                    continue;
                }
                StudentGroup group = registration.getGroup();
                // Danh sach nguoi nhan: tung SV trong nhom (gom ca nhom truong), khong trung email
                Map<String, String> recipients = new LinkedHashMap<>();
                group.getMembers().forEach(member -> {
                    String email = member.getStudent().getUser().getEmail();
                    if (email != null && !email.isBlank()) {
                        recipients.putIfAbsent(email.trim(), member.getStudent().getUser().getFullName());
                    }
                });
                for (Map.Entry<String, String> recipient : recipients.entrySet()) {
                    try {
                        SimpleMailMessage message = new SimpleMailMessage();
                        message.setFrom(username);
                        message.setTo(recipient.getKey());
                        message.setSubject("[Khoa CNTT] Kết quả đề tài đã được công bố - " + periodName);
                        message.setText(buildBody(recipient.getValue(), periodName, group.getGroupCode(), result));
                        sender.send(message);
                        sent++;
                    } catch (Exception ex) {
                        log.warn("Gui email cong bo ket qua toi {} that bai: {}", recipient.getKey(), ex.getMessage());
                    }
                }
            }
            log.info("Da gui {} email thong bao cong bo ket qua cho dot {}.", sent, periodId);
        } catch (Exception ex) {
            log.warn("Loi khi gui email cong bo ket qua cho dot {}: {}", periodId, ex.getMessage());
        }
    }

    private String buildBody(String studentName, String periodName, String groupCode, FinalResult result) {
        String score = result.getFinalScore() == null ? "-"
                : result.getFinalScore().setScale(2, RoundingMode.HALF_UP).toPlainString();
        String status = result.getEvaluationStatus() == null ? "-"
                : result.getEvaluationStatus().getDisplayName();
        return "Kính gửi sinh viên " + studentName + ",\n\n"
                + "Kết quả đánh giá đề tài của nhóm bạn đã được công bố trên Hệ thống Quản lý Đề tài - Khoa CNTT.\n\n"
                + "- Đợt đăng ký: " + periodName + "\n"
                + "- Nhóm: " + groupCode + "\n"
                + "- Đề tài: " + result.getTopic().getTitle() + "\n"
                + "- Điểm tổng kết: " + score + "\n"
                + "- Kết quả: " + status + "\n\n"
                + "Sinh viên đăng nhập hệ thống, vào mục \"Điểm & kết quả\" để xem chi tiết.\n\n"
                + "Đây là email tự động, vui lòng không trả lời.";
    }

    private JavaMailSenderImpl buildSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(port);
        sender.setUsername(username);
        sender.setPassword(password);
        sender.setDefaultEncoding("UTF-8");
        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
        return sender;
    }
}
