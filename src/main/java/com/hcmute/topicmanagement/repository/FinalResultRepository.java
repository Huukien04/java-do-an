package com.hcmute.topicmanagement.repository;

import com.hcmute.topicmanagement.entity.FinalResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FinalResultRepository extends JpaRepository<FinalResult, Long> {
    Optional<FinalResult> findByTopicId(Long topicId);
    List<FinalResult> findByIsPublishedTrue();
    List<FinalResult> findByIsPublishedTrueOrderByTopicTitleAsc();
    List<FinalResult> findByIsPublishedTrueAndTopicPeriodIdOrderByTopicTitleAsc(Long periodId);
    List<FinalResult> findAllByOrderByTopicTitleAsc();
    List<FinalResult> findByTopicPeriodIdOrderByTopicTitleAsc(Long periodId);
    boolean existsByTopicId(Long topicId);
}
