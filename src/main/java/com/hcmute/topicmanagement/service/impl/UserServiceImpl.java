package com.hcmute.topicmanagement.service.impl;

import com.hcmute.topicmanagement.dto.UserFormDto;
import com.hcmute.topicmanagement.entity.Department;
import com.hcmute.topicmanagement.entity.Role;
import com.hcmute.topicmanagement.entity.Student;
import com.hcmute.topicmanagement.entity.Teacher;
import com.hcmute.topicmanagement.entity.User;
import com.hcmute.topicmanagement.enums.RoleName;
import com.hcmute.topicmanagement.exception.ResourceNotFoundException;
import com.hcmute.topicmanagement.repository.DepartmentRepository;
import com.hcmute.topicmanagement.repository.RoleRepository;
import com.hcmute.topicmanagement.repository.StudentRepository;
import com.hcmute.topicmanagement.repository.TeacherRepository;
import com.hcmute.topicmanagement.repository.UserRepository;
import com.hcmute.topicmanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<User> findAllUsers(String keyword, Long roleId, Long departmentId) {
        List<User> users = userRepository.findAll();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String lowerKw = keyword.trim().toLowerCase();
            users = users.stream()
                    .filter(u -> u.getUsername().toLowerCase().contains(lowerKw) ||
                            u.getFullName().toLowerCase().contains(lowerKw) ||
                            u.getEmail().toLowerCase().contains(lowerKw) ||
                            (u.getPhone() != null && u.getPhone().contains(lowerKw)))
                    .collect(Collectors.toList());
        }

        if (roleId != null) {
            users = users.stream()
                    .filter(u -> u.getRole() != null && u.getRole().getId().equals(roleId))
                    .collect(Collectors.toList());
        }

        if (departmentId != null) {
            users = users.stream().filter(u -> {
                if (u.getRole().getName() == RoleName.ROLE_STUDENT) {
                    Optional<Student> studentOpt = studentRepository.findById(u.getId());
                    return studentOpt.isPresent() && studentOpt.get().getDepartment() != null &&
                            studentOpt.get().getDepartment().getId().equals(departmentId);
                } else if (u.getRole().getName() == RoleName.ROLE_TEACHER) {
                    Optional<Teacher> teacherOpt = teacherRepository.findById(u.getId());
                    return teacherOpt.isPresent() && teacherOpt.get().getDepartment() != null &&
                            teacherOpt.get().getDepartment().getId().equals(departmentId);
                }
                return false;
            }).collect(Collectors.toList());
        }

        return users;
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    public UserFormDto getUserFormDtoById(Long id) {
        User user = getUserById(id);
        UserFormDto dto = UserFormDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .enabled(user.isEnabled())
                .roleId(user.getRole() != null ? user.getRole().getId() : null)
                .build();

        if (user.getRole().getName() == RoleName.ROLE_STUDENT) {
            studentRepository.findById(user.getId()).ifPresent(student -> {
                dto.setStudentCode(student.getStudentCode());
                dto.setClassName(student.getClassName());
                dto.setAcademicYear(student.getAcademicYear());
                if (student.getDepartment() != null) {
                    dto.setDepartmentId(student.getDepartment().getId());
                }
            });
        } else if (user.getRole().getName() == RoleName.ROLE_TEACHER) {
            teacherRepository.findById(user.getId()).ifPresent(teacher -> {
                dto.setTeacherCode(teacher.getTeacherCode());
                dto.setAcademicRank(teacher.getAcademicRank());
                dto.setDegree(teacher.getDegree());
                if (teacher.getDepartment() != null) {
                    dto.setDepartmentId(teacher.getDepartment().getId());
                }
            });
        }

        return dto;
    }

    @Override
    @Transactional
    public User createUser(UserFormDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Tên đăng nhập '" + dto.getUsername() + "' đã tồn tại!");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email '" + dto.getEmail() + "' đã tồn tại!");
        }

        Role role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + dto.getRoleId()));

        String rawPassword = (dto.getPassword() != null && !dto.getPassword().trim().isEmpty())
                ? dto.getPassword()
                : "123456";

        User user = User.builder()
                .username(dto.getUsername().trim())
                .password(passwordEncoder.encode(rawPassword))
                .fullName(dto.getFullName().trim())
                .email(dto.getEmail().trim())
                .phone(dto.getPhone() != null ? dto.getPhone().trim() : null)
                .enabled(dto.isEnabled())
                .role(role)
                .build();

        user = userRepository.save(user);

        saveRoleProfile(user, role, dto);

        return user;
    }

    @Override
    @Transactional
    public User updateUser(Long id, UserFormDto dto) {
        User user = getUserById(id);

        if (!user.getEmail().equalsIgnoreCase(dto.getEmail().trim()) && userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email '" + dto.getEmail() + "' đã được sử dụng bởi tài khoản khác!");
        }

        Role newRole = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + dto.getRoleId()));

        user.setFullName(dto.getFullName().trim());
        user.setEmail(dto.getEmail().trim());
        user.setPhone(dto.getPhone() != null ? dto.getPhone().trim() : null);
        user.setEnabled(dto.isEnabled());
        user.setRole(newRole);

        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword().trim()));
        }

        user = userRepository.save(user);

        saveRoleProfile(user, newRole, dto);

        return user;
    }

    private void saveRoleProfile(User user, Role role, UserFormDto dto) {
        if (role.getName() == RoleName.ROLE_STUDENT) {
            Department dept = null;
            if (dto.getDepartmentId() != null) {
                dept = departmentRepository.findById(dto.getDepartmentId()).orElse(null);
            }
            Student student = studentRepository.findById(user.getId())
                    .orElse(Student.builder().user(user).build());

            student.setStudentCode(dto.getStudentCode() != null && !dto.getStudentCode().trim().isEmpty()
                    ? dto.getStudentCode().trim() : "SV" + user.getId());
            student.setClassName(dto.getClassName() != null ? dto.getClassName().trim() : "N/A");
            student.setAcademicYear(dto.getAcademicYear() != null ? dto.getAcademicYear().trim() : "2023-2027");
            student.setDepartment(dept);
            studentRepository.save(student);

            // Clean up teacher if role changed
            if (teacherRepository.existsById(user.getId())) {
                teacherRepository.deleteById(user.getId());
            }

        } else if (role.getName() == RoleName.ROLE_TEACHER) {
            Department dept = null;
            if (dto.getDepartmentId() != null) {
                dept = departmentRepository.findById(dto.getDepartmentId()).orElse(null);
            }
            Teacher teacher = teacherRepository.findById(user.getId())
                    .orElse(Teacher.builder().user(user).build());

            teacher.setTeacherCode(dto.getTeacherCode() != null && !dto.getTeacherCode().trim().isEmpty()
                    ? dto.getTeacherCode().trim() : "GV" + user.getId());
            teacher.setAcademicRank(dto.getAcademicRank() != null ? dto.getAcademicRank().trim() : "Giảng viên");
            teacher.setDegree(dto.getDegree() != null ? dto.getDegree().trim() : "Thạc sĩ");
            teacher.setDepartment(dept);
            teacherRepository.save(teacher);

            // Clean up student if role changed
            if (studentRepository.existsById(user.getId())) {
                studentRepository.deleteById(user.getId());
            }
        } else {
            // ROLE_ADMIN
            if (studentRepository.existsById(user.getId())) {
                studentRepository.deleteById(user.getId());
            }
            if (teacherRepository.existsById(user.getId())) {
                teacherRepository.deleteById(user.getId());
            }
        }
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long id) {
        User user = getUserById(id);
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        User user = getUserById(id);
        String passwordToSet = (newPassword != null && !newPassword.trim().isEmpty()) ? newPassword.trim() : "123456";
        user.setPassword(passwordEncoder.encode(passwordToSet));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        if (studentRepository.existsById(id)) {
            studentRepository.deleteById(id);
        }
        if (teacherRepository.existsById(id)) {
            teacherRepository.deleteById(id);
        }
        userRepository.delete(user);
    }

    @Override
    public long countTotalUsers() {
        return userRepository.count();
    }

    @Override
    public long countTeachers() {
        return teacherRepository.count();
    }

    @Override
    public long countStudents() {
        return studentRepository.count();
    }
}
