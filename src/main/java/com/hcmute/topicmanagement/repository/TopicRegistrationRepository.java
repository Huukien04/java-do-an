package com.hcmute.topicmanagement.repository;

import com.hcmute.topicmanagement.entity.TopicRegistration;
import com.hcmute.topicmanagement.enums.TopicRegistrationStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicRegistrationRepository extends JpaRepository<TopicRegistration, Long> {
    @Query("""
            SELECT DISTINCT registration
            FROM TopicRegistration registration
            JOIN registration.group studentGroup
            LEFT JOIN studentGroup.members member
            WHERE studentGroup.leader.id = :studentId
               OR member.student.id = :studentId
            ORDER BY registration.registeredAt DESC
            """)
    List<TopicRegistration> findAllByStudentId(@Param("studentId") Long studentId);

    Optional<TopicRegistration> findByGroupId(Long groupId);
    Optional<TopicRegistration> findByTopicId(Long topicId);
    List<TopicRegistration> findByStatus(TopicRegistrationStatus status);
    List<TopicRegistration> findByGroupPeriodId(Long periodId);
    List<TopicRegistration> findByTopicCreatedByTeacherId(Long teacherId);
    List<TopicRegistration> findByTopicCreatedByTeacherIdAndGroupPeriodId(Long teacherId, Long periodId);
    long countByTopicCreatedByTeacherIdAndStatus(Long teacherId, TopicRegistrationStatus status);
    boolean existsByGroupId(Long groupId);
    boolean existsByGroupPeriodId(Long periodId);
    boolean existsByTopicId(Long topicId);
}
