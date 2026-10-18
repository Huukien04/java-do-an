package com.hcmute.topicmanagement.dto;

import com.hcmute.topicmanagement.enums.RoleName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAccountForm {
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private RoleName roleName;
    private boolean enabled = true;
    private String teacherCode;
    private String academicRank;
    private String degree;
    private String studentCode;
    private String className;
    private String academicYear;
    private Long departmentId;
}
