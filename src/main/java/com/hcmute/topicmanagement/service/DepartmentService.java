package com.hcmute.topicmanagement.service;

import com.hcmute.topicmanagement.entity.Department;

import java.util.List;

public interface DepartmentService {
    List<Department> findAll();
    Department findById(Long id);
    Department create(Department department);
    Department update(Long id, Department departmentDetails);
    void delete(Long id);
    long countDepartments();
}
