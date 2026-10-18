package com.hcmute.topicmanagement.repository;

import com.hcmute.topicmanagement.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByGroupIdOrderByVersionNumberDesc(Long groupId);
    Optional<Submission> findTopByGroupIdOrderByVersionNumberDesc(Long groupId);
    boolean existsByUploadedBy_Id(Long studentId);
    boolean existsByGroupId(Long groupId);

    @Query("SELECT COALESCE(MAX(s.versionNumber), 0) FROM Submission s WHERE s.group.id = :groupId")
    Integer findMaxVersionByGroupId(@Param("groupId") Long groupId);
}
