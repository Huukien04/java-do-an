package com.hcmute.topicmanagement.repository;

import com.hcmute.topicmanagement.entity.Topic;
import com.hcmute.topicmanagement.enums.TopicStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
    List<Topic> findByPeriodId(Long periodId);
    long countByPeriodId(Long periodId);
    List<Topic> findByPeriodIdAndStatus(Long periodId, TopicStatus status);
    List<Topic> findByPeriodIdAndStatusIn(Long periodId, List<TopicStatus> statuses);
    long countByPeriodIdAndStatus(Long periodId, TopicStatus status);
    long countByPeriodIdAndStatusIn(Long periodId, List<TopicStatus> statuses);
    List<Topic> findByCreatedByTeacherId(Long teacherId);
    List<Topic> findByDepartmentIdAndPeriodId(Long departmentId, Long periodId);
    boolean existsByDepartmentId(Long departmentId);
    boolean existsByCreatedByTeacherId(Long teacherId);

    @Query("""
            SELECT topic
            FROM Topic topic
            WHERE topic.status = :status
              AND NOT EXISTS (
                  SELECT councilTopic.id
                  FROM CouncilTopic councilTopic
                  WHERE councilTopic.topic.id = topic.id
              )
            """)
    List<Topic> findUnassignedByStatus(@Param("status") TopicStatus status);

    @Query("SELECT DISTINCT t FROM Topic t JOIN t.advisors ta WHERE ta.teacher.id = :teacherId")
    List<Topic> findTopicsByAdvisorTeacherId(@Param("teacherId") Long teacherId);
}
