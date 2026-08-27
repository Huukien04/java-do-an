package com.hcmute.topicmanagement.service;

import com.hcmute.topicmanagement.dto.NotificationDTO;

import java.util.List;

public interface NotificationService {
    NotificationDTO createNotification(NotificationDTO dto, Long currentUserId);
    void deleteNotification(Long id);
    List<NotificationDTO> getNotificationsForRole(String roleName);
    List<NotificationDTO> getAllNotifications();
}
