package com.hcmute.topicmanagement.service;

import com.hcmute.topicmanagement.dto.UserFormDto;
import com.hcmute.topicmanagement.entity.User;

import java.util.List;

public interface UserService {
    List<User> findAllUsers(String keyword, Long roleId, Long departmentId);
    User getUserById(Long id);
    UserFormDto getUserFormDtoById(Long id);
    User createUser(UserFormDto dto);
    User updateUser(Long id, UserFormDto dto);
    void toggleUserStatus(Long id);
    void resetPassword(Long id, String newPassword);
    void deleteUser(Long id);

    long countTotalUsers();
    long countTeachers();
    long countStudents();
}
