package com.hcmute.topicmanagement.repository;

import com.hcmute.topicmanagement.entity.StudentGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentGroupRepository extends JpaRepository<StudentGroup, Long> {
    List<StudentGroup> findByPeriodId(Long periodId);
    List<StudentGroup> findByPeriodIdAndStatus(Long periodId, com.hcmute.topicmanagement.enums.StudentGroupStatus status);
    boolean existsByLeaderId(Long studentId);
    Optional<StudentGroup> findByLeaderIdAndPeriodId(Long studentId, Long periodId);

    @Query("SELECT g FROM StudentGroup g JOIN g.members m WHERE m.student.id = :studentId AND g.period.id = :periodId")
    Optional<StudentGroup> findGroupByStudentIdAndPeriodId(@Param("studentId") Long studentId, @Param("periodId") Long periodId);

    @Query("SELECT DISTINCT g FROM StudentGroup g LEFT JOIN g.members m WHERE g.leader.id = :studentId OR m.student.id = :studentId")
    List<StudentGroup> findGroupsByStudentId(@Param("studentId") Long studentId);
}
