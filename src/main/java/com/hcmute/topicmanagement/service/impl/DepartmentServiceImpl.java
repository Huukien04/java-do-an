package com.hcmute.topicmanagement.service.impl;

import com.hcmute.topicmanagement.entity.Department;
import com.hcmute.topicmanagement.exception.ResourceNotFoundException;
import com.hcmute.topicmanagement.repository.DepartmentRepository;
import com.hcmute.topicmanagement.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    public List<Department> findAll() {
        return departmentRepository.findAll();
    }

    @Override
    public Department findById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }

    @Override
    @Transactional
    public Department create(Department department) {
        if (departmentRepository.existsByCode(department.getCode())) {
            throw new IllegalArgumentException("Mã bộ môn '" + department.getCode() + "' đã tồn tại!");
        }
        return departmentRepository.save(department);
    }

    @Override
    @Transactional
    public Department update(Long id, Department details) {
        Department existing = findById(id);
        if (!existing.getCode().equalsIgnoreCase(details.getCode()) && departmentRepository.existsByCode(details.getCode())) {
            throw new IllegalArgumentException("Mã bộ môn '" + details.getCode() + "' đã được sử dụng bởi bộ môn khác!");
        }
        existing.setCode(details.getCode().trim());
        existing.setName(details.getName().trim());
        existing.setDescription(details.getDescription());
        return departmentRepository.save(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Department department = findById(id);
        departmentRepository.delete(department);
    }

    @Override
    public long countDepartments() {
        return departmentRepository.count();
    }
}
