package com.hcmute.topicmanagement.entity;

import com.hcmute.topicmanagement.enums.RoleName;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 30)
    private RoleName name;

    public String getDisplayName() {
        if (name == null) return "";
        switch (name) {
            case ROLE_ADMIN: return "Quản trị Kỹ thuật (ADMIN)";
            case ROLE_TRUONG_KHOA: return "Trưởng Khoa";
            case ROLE_TEACHER: return "Giảng viên";
            case ROLE_STUDENT: return "Sinh viên";
            default: return name.name();
        }
    }
}
