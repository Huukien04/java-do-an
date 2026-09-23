package com.hcmute.topicmanagement.repository;

import com.hcmute.topicmanagement.entity.CouncilMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouncilMemberRepository extends JpaRepository<CouncilMember, Long> {
    List<CouncilMember> findByCouncilId(Long councilId);
    List<CouncilMember> findByTeacherId(Long teacherId);
    boolean existsByCouncilIdAndTeacherId(Long councilId, Long teacherId);
    void deleteByCouncilId(Long councilId);
}
