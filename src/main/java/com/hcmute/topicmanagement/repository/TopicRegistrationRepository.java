package com.hcmute.topicmanagement.repository;

import com.hcmute.topicmanagement.entity.TopicRegistration;
import com.hcmute.topicmanagement.enums.TopicRegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicRegistrationRepository extends JpaRepository<TopicRegistration, Long> {
    Optional<TopicRegistration> findByGroupId(Long groupId);
    Optional<TopicRegistration> findByTopicId(Long topicId);
    List<TopicRegistration> findByStatus(TopicRegistrationStatus status);
    List<TopicRegistration> findByGroupPeriodId(Long periodId);
    boolean existsByGroupId(Long groupId);
    boolean existsByTopicId(Long topicId);
}
