package com.hcmute.topicmanagement.repository;

import com.hcmute.topicmanagement.entity.CouncilMember;
import com.hcmute.topicmanagement.enums.CouncilRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouncilMemberRepository extends JpaRepository<CouncilMember, Long> {
    List<CouncilMember> findByCouncilId(Long councilId);
    List<CouncilMember> findByTeacherId(Long teacherId);
    boolean existsByCouncilIdAndTeacherId(Long councilId, Long teacherId);
    java.util.Optional<CouncilMember> findByCouncilIdAndTeacherId(Long councilId, Long teacherId);
    boolean existsByCouncilIdAndTeacherIdAndRole(Long councilId, Long teacherId, CouncilRole role);
    boolean existsByTeacherId(Long teacherId);
    void deleteByCouncilId(Long councilId);
}
