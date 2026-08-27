package com.hcmute.topicmanagement.repository;

import com.hcmute.topicmanagement.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByGroupId(Long groupId);
    boolean existsByGroupIdAndStudentId(Long groupId, Long studentId);

    @Query("SELECT COUNT(m) > 0 FROM GroupMember m WHERE m.student.id = :studentId AND m.group.period.id = :periodId")
    boolean isStudentInAnyGroupInPeriod(@Param("studentId") Long studentId, @Param("periodId") Long periodId);
}
