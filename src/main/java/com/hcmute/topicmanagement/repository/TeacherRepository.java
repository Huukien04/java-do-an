package com.hcmute.topicmanagement.repository;

import com.hcmute.topicmanagement.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByTeacherCode(String teacherCode);
    Optional<Teacher> findByUserUsername(String username);
    List<Teacher> findByDepartmentId(Long departmentId);
    boolean existsByTeacherCode(String teacherCode);
    boolean existsByTeacherCodeAndIdNot(String teacherCode, Long id);
    boolean existsByDepartmentId(Long departmentId);
}
