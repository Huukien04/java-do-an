package com.hcmute.topicmanagement.repository;

import com.hcmute.topicmanagement.entity.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {
    List<Score> findByTopicId(Long topicId);
    Optional<Score> findByTopicIdAndTeacherId(Long topicId, Long teacherId);
    boolean existsByTopicIdAndTeacherId(Long topicId, Long teacherId);
    boolean existsByTeacherId(Long teacherId);
}
