package com.hcmute.topicmanagement.repository;

import com.hcmute.topicmanagement.entity.TopicReviewer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicReviewerRepository extends JpaRepository<TopicReviewer, Long> {
    List<TopicReviewer> findByTopicId(Long topicId);
    List<TopicReviewer> findByTeacherId(Long teacherId);
    boolean existsByTopicIdAndTeacherId(Long topicId, Long teacherId);
    boolean existsByTeacherId(Long teacherId);
    void deleteByTopicId(Long topicId);
}
