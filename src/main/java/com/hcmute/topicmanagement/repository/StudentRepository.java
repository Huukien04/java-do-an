package com.hcmute.topicmanagement.repository;

import com.hcmute.topicmanagement.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentCode(String studentCode);
    Optional<Student> findByUserUsername(String username);
    List<Student> findByDepartmentId(Long departmentId);
    boolean existsByStudentCode(String studentCode);
    boolean existsByStudentCodeAndIdNot(String studentCode, Long id);
    boolean existsByDepartmentId(Long departmentId);
}
