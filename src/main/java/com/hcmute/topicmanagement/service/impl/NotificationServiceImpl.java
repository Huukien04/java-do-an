package com.hcmute.topicmanagement.service.impl;

import com.hcmute.topicmanagement.dto.NotificationDTO;
import com.hcmute.topicmanagement.entity.Notification;
import com.hcmute.topicmanagement.entity.RegistrationPeriod;
import com.hcmute.topicmanagement.entity.User;
import com.hcmute.topicmanagement.exception.ResourceNotFoundException;
import com.hcmute.topicmanagement.repository.NotificationRepository;
import com.hcmute.topicmanagement.repository.RegistrationPeriodRepository;
import com.hcmute.topicmanagement.repository.UserRepository;
import com.hcmute.topicmanagement.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final RegistrationPeriodRepository periodRepository;

    @Override
    public NotificationDTO createNotification(NotificationDTO dto, Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", currentUserId));

        RegistrationPeriod period = null;
        if (dto.getPeriodId() != null) {
            period = periodRepository.findById(dto.getPeriodId()).orElse(null);
        }

        Notification notification = Notification.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .type(dto.getType())
                .targetRole(dto.getTargetRole() != null ? dto.getTargetRole() : "ALL")
                .period(period)
                .createdBy(user)
                .build();

        notification = notificationRepository.save(notification);
        return mapToDTO(notification);
    }

    @Override
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotificationsForRole(String roleName) {
        return notificationRepository.findNotificationsByRole(roleName).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getAllNotifications() {
        return notificationRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private NotificationDTO mapToDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .type(notification.getType())
                .targetRole(notification.getTargetRole())
                .periodId(notification.getPeriod() != null ? notification.getPeriod().getId() : null)
                .periodName(notification.getPeriod() != null ? notification.getPeriod().getName() : null)
                .createdByUserId(notification.getCreatedBy().getId())
                .createdByFullName(notification.getCreatedBy().getFullName())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
