package com.hcmute.topicmanagement.repository;

import com.hcmute.topicmanagement.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByPeriodIdOrderByCreatedAtDesc(Long periodId);
    List<Notification> findByRecipients_IdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT n FROM Notification n WHERE n.targetRole = 'ALL' OR n.targetRole = :roleName ORDER BY n.createdAt DESC")
    List<Notification> findNotificationsByRole(@Param("roleName") String roleName);

    @Query("SELECT n FROM Notification n WHERE n.targetRole IS NULL OR n.targetRole <> 'INDIVIDUAL' ORDER BY n.createdAt DESC")
    List<Notification> findGeneralNotifications();
}
