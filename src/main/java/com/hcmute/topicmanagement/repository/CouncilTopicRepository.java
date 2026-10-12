package com.hcmute.topicmanagement.repository;

import com.hcmute.topicmanagement.entity.CouncilTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouncilTopicRepository extends JpaRepository<CouncilTopic, Long> {
    List<CouncilTopic> findByCouncilId(Long councilId);
    @Query("""
            SELECT topic.id AS id, topic.title AS title
            FROM CouncilTopic councilTopic
            JOIN councilTopic.topic topic
            WHERE councilTopic.council.id = :councilId
            """)
    List<TopicSummary> findTopicSummariesByCouncilId(@Param("councilId") Long councilId);
    Optional<CouncilTopic> findByTopicId(Long topicId);
    boolean existsByTopicId(Long topicId);

    interface TopicSummary {
        Long getId();
        String getTitle();
    }
}
