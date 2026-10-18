package com.hcmute.topicmanagement.repository;

import com.hcmute.topicmanagement.entity.NotificationRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationReadRepository extends JpaRepository<NotificationRead, Long> {

    boolean existsByNotificationIdAndUserId(Long notificationId, Long userId);
    boolean existsByUserId(Long userId);
    void deleteByNotificationId(Long notificationId);

    @Query("""
            SELECT COUNT(DISTINCT notification.id)
            FROM Notification notification
            LEFT JOIN notification.recipients recipient
            WHERE (
                notification.targetRole IS NULL
                OR notification.targetRole = 'ALL'
                OR notification.targetRole = :roleName
                OR (:roleName = 'ROLE_ADMIN' AND notification.targetRole <> 'INDIVIDUAL')
                OR (notification.targetRole = 'INDIVIDUAL' AND recipient.id = :userId)
            )
            AND NOT EXISTS (
                SELECT notificationRead.id
                FROM NotificationRead notificationRead
                WHERE notificationRead.notification.id = notification.id
                  AND notificationRead.user.id = :userId
            )
            """)
    long countUnreadForUser(@Param("userId") Long userId, @Param("roleName") String roleName);
}
