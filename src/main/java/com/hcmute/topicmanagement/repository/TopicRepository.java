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
    List<Topic> findByPeriodIdAndStatus(Long periodId, TopicStatus status);
    List<Topic> findByCreatedByTeacherId(Long teacherId);
    List<Topic> findByDepartmentIdAndPeriodId(Long departmentId, Long periodId);

    @Query("SELECT DISTINCT t FROM Topic t JOIN t.advisors ta WHERE ta.teacher.id = :teacherId")
    List<Topic> findTopicsByAdvisorTeacherId(@Param("teacherId") Long teacherId);
}
