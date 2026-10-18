package com.hcmute.topicmanagement.repository;

import com.hcmute.topicmanagement.entity.TopicAdvisor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicAdvisorRepository extends JpaRepository<TopicAdvisor, Long> {
    List<TopicAdvisor> findByTopicId(Long topicId);
    List<TopicAdvisor> findByTeacherId(Long teacherId);
    boolean existsByTopicIdAndTeacherId(Long topicId, Long teacherId);
    boolean existsByTeacherId(Long teacherId);
    void deleteByTopicId(Long topicId);
}
