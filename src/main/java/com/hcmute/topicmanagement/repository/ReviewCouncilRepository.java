package com.hcmute.topicmanagement.repository;

import com.hcmute.topicmanagement.entity.ReviewCouncil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewCouncilRepository extends JpaRepository<ReviewCouncil, Long> {
    List<ReviewCouncil> findByPeriodId(Long periodId);
    Optional<ReviewCouncil> findByCouncilCode(String councilCode);
    boolean existsByCouncilCode(String councilCode);

    @Query("SELECT DISTINCT c FROM ReviewCouncil c JOIN c.members m WHERE m.teacher.id = :teacherId")
    List<ReviewCouncil> findCouncilsByTeacherId(@Param("teacherId") Long teacherId);
}
