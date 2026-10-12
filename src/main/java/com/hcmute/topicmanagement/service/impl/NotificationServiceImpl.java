package com.hcmute.topicmanagement.service.impl;

import com.hcmute.topicmanagement.dto.NotificationDTO;
import com.hcmute.topicmanagement.entity.Notification;
import com.hcmute.topicmanagement.entity.NotificationRead;
import com.hcmute.topicmanagement.entity.RegistrationPeriod;
import com.hcmute.topicmanagement.entity.User;
import com.hcmute.topicmanagement.config.NotificationWebSocketHandler;
import com.hcmute.topicmanagement.exception.ResourceNotFoundException;
import com.hcmute.topicmanagement.repository.NotificationRepository;
import com.hcmute.topicmanagement.repository.NotificationReadRepository;
import com.hcmute.topicmanagement.repository.RegistrationPeriodRepository;
import com.hcmute.topicmanagement.repository.UserRepository;
import com.hcmute.topicmanagement.service.NotificationService;
import com.hcmute.topicmanagement.enums.RoleName;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationReadRepository notificationReadRepository;
    private final UserRepository userRepository;
    private final RegistrationPeriodRepository periodRepository;
    private final NotificationWebSocketHandler webSocketHandler;

    @Override
    public NotificationDTO createNotification(NotificationDTO dto, Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", currentUserId));

        Set<User> recipients = new LinkedHashSet<>();
        String targetRole = dto.getTargetRole() != null ? dto.getTargetRole() : "ALL";
        if ("INDIVIDUAL".equals(targetRole)) {
            List<Long> recipientIds = dto.getTargetUserIds() == null
                    ? List.of()
                    : dto.getTargetUserIds().stream().distinct().toList();
            if (recipientIds.isEmpty()) {
                throw new IllegalArgumentException("Chọn ít nhất một người nhận thông báo.");
            }
            recipients.addAll(userRepository.findAllById(recipientIds));
            if (recipients.size() != recipientIds.size()
                    || recipients.stream().anyMatch(recipient -> !recipient.isEnabled())) {
                throw new IllegalArgumentException("Một hoặc nhiều người nhận không tồn tại hoặc đã bị vô hiệu hóa.");
            }
        }

        RegistrationPeriod period = null;
        if (dto.getPeriodId() != null) {
            period = periodRepository.findById(dto.getPeriodId()).orElse(null);
        }

        Notification notification = Notification.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .type(dto.getType())
                .targetRole(targetRole)
                .period(period)
                .createdBy(user)
                .recipients(recipients)
                .build();

        notification = notificationRepository.save(notification);
        NotificationDTO savedNotification = mapToDTO(notification);
        List<User> pushRecipients = switch (targetRole) {
            case "INDIVIDUAL" -> List.copyOf(recipients);
            case "ALL" -> userRepository.findByEnabledTrueOrderByFullNameAsc();
            default -> userRepository.findByEnabledTrueAndRole_Name(RoleName.valueOf(targetRole));
        };
        List<String> recipientUsernames = pushRecipients.stream().map(User::getUsername).toList();
        java.util.Map<String, Object> pushMessage = java.util.Map.of(
                "id", savedNotification.getId(),
                "title", savedNotification.getTitle(),
                "content", savedNotification.getContent(),
                "url", "/notifications/" + savedNotification.getId()
        );
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    webSocketHandler.sendToUsers(recipientUsernames, pushMessage);
                }
            });
        } else {
            webSocketHandler.sendToUsers(recipientUsernames, pushMessage);
        }
        return savedNotification;
    }

    @Override
    public void deleteNotification(Long id) {
        notificationReadRepository.deleteByNotificationId(id);
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
    public List<NotificationDTO> getGeneralNotifications(String roleName) {
        List<Notification> notifications = "ROLE_ADMIN".equals(roleName)
                ? notificationRepository.findGeneralNotifications()
                : notificationRepository.findNotificationsByRole(roleName);
        return notifications.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getPersonalNotifications(Long userId) {
        return notificationRepository.findByRecipients_IdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationDTO getNotificationForUser(Long id, Long userId, String roleName) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thông báo", "id", id));

        boolean isIndividual = "INDIVIDUAL".equals(notification.getTargetRole());
        boolean isRecipient = notification.getRecipients().stream()
                .anyMatch(recipient -> recipient.getId().equals(userId));
        boolean isAdmin = "ROLE_ADMIN".equals(roleName);
        boolean isGeneral = notification.getTargetRole() == null
                || "ALL".equals(notification.getTargetRole())
                || notification.getTargetRole().equals(roleName)
                || (isAdmin && !isIndividual);

        if ((isIndividual && !isRecipient) || (!isIndividual && !isGeneral)) {
            throw new ResourceNotFoundException("Thông báo", "id", id);
        }
        return mapToDTO(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getAllNotifications() {
        return notificationRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId, String roleName) {
        return notificationReadRepository.countUnreadForUser(userId, roleName);
    }

    @Override
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Thông báo", "id", notificationId));
        if (!notificationReadRepository.existsByNotificationIdAndUserId(notificationId, userId)) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", userId));
            notificationReadRepository.save(NotificationRead.builder()
                    .notification(notification)
                    .user(user)
                    .build());
        }
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
