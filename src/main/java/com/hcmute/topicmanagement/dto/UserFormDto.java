package com.hcmute.topicmanagement.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFormDto {

    private Long id;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    @Builder.Default
    private boolean enabled = true;
    private Long roleId;

    // Student specific fields
    private String studentCode;
    private String className;
    private String academicYear;

    // Teacher specific fields
    private String teacherCode;
    private String academicRank;
    private String degree;

    // Common relation for Student / Teacher
    private Long departmentId;
}
