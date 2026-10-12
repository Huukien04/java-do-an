package com.hcmute.topicmanagement.config;

import com.hcmute.topicmanagement.entity.*;
import com.hcmute.topicmanagement.enums.RoleName;
import com.hcmute.topicmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DataInitializer implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final RegistrationPeriodRepository periodRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking initial seed data...");

        // Tự động phân loại và reset mật khẩu riêng cho từng nhóm tài khoản
        userRepository.findAll().forEach(u -> {
            if ("admin".equals(u.getUsername())) {
                u.setPassword(passwordEncoder.encode("admin123"));
            } else if ("truongkhoa".equals(u.getUsername())) {
                u.setPassword(passwordEncoder.encode("truongkhoa123"));
            } else {
                u.setPassword(passwordEncoder.encode("123456"));
            }
            u.setEnabled(true);
            userRepository.save(u);
        });

        // 1. Seed Roles
        Role adminRole = seedRoleIfAbsent(RoleName.ROLE_ADMIN);
        Role truongKhoaRole = seedRoleIfAbsent(RoleName.ROLE_TRUONG_KHOA);
        Role teacherRole = seedRoleIfAbsent(RoleName.ROLE_TEACHER);
        Role studentRole = seedRoleIfAbsent(RoleName.ROLE_STUDENT);

        // 2. Seed Departments
        Department ktpm = seedDepartmentIfAbsent("KTPM", "Kỹ thuật Phần mềm", "Bộ môn Kỹ thuật Phần mềm");
        Department httt = seedDepartmentIfAbsent("HTTT", "Hệ thống Thông tin", "Bộ môn Hệ thống Thông tin");
        Department khmt = seedDepartmentIfAbsent("KHMT", "Khoa học Máy tính", "Bộ môn Khoa học Máy tính");
        Department mmt = seedDepartmentIfAbsent("MMT", "Mạng máy tính & Truyền thông", "Bộ môn Mạng máy tính");
        Department cntt = seedDepartmentIfAbsent("CNTT", "Công nghệ Thông tin", "Bộ môn Công nghệ Thông tin");

        // 3. Seed System Admin & Trưởng Khoa
        User adminUser = userRepository.findByUsername("admin").orElse(null);
        if (adminUser == null) {
            adminUser = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("Quản trị viên Hệ thống")
                    .email("admin@hcmute.edu.vn")
                    .phone("0900000000")
                    .enabled(true)
                    .role(adminRole)
                    .build();
            userRepository.save(adminUser);
            log.info("Created default Admin: username=admin / pass=admin123");
        } else {
            // Update admin email and role if it was seeded previously as truongkhoa email
            if ("truongkhoa@hcmute.edu.vn".equals(adminUser.getEmail())) {
                adminUser.setEmail("admin@hcmute.edu.vn");
                adminUser.setRole(adminRole);
                userRepository.save(adminUser);
            }
        }

        if (!userRepository.existsByUsername("truongkhoa")) {
            String tkEmail = "truongkhoa@hcmute.edu.vn";
            if (!userRepository.existsByEmail(tkEmail)) {
                User truongKhoaUser = User.builder()
                        .username("truongkhoa")
                        .password(passwordEncoder.encode("truongkhoa123"))
                        .fullName("GS.TS. Trưởng Khoa CNTT")
                        .email(tkEmail)
                        .phone("0901234567")
                        .enabled(true)
                        .role(truongKhoaRole)
                        .build();
                userRepository.save(truongKhoaUser);
                log.info("Created default Trưởng khoa: username=truongkhoa / pass=truongkhoa123");
            }
        }

        // 4. Seed Sample Teachers
        seedTeacherIfAbsent("gv_nguyenvana", "TS. Nguyễn Văn A", "gv.nguyenvana@hcmute.edu.vn", "GV001", "TS",
                "Tiến sĩ", ktpm, teacherRole);
        seedTeacherIfAbsent("gv_tranvanb", "ThS. Trần Văn B", "gv.tranvanb@hcmute.edu.vn", "GV002", "ThS", "Thạc sĩ",
                httt, teacherRole);
        seedTeacherIfAbsent("gv_lethic", "PGS.TS. Lê Thị C", "gv.lethic@hcmute.edu.vn", "GV003", "PGS.TS",
                "Phó Giáo sư Tiến sĩ", khmt, teacherRole);
        seedTeacherIfAbsent("gv_phamvand", "TS. Phạm Văn D", "gv.phamvand@hcmute.edu.vn", "GV004", "TS", "Tiến sĩ", mmt,
                teacherRole);
        seedTeacherIfAbsent("gv_hoangvane", "ThS. Hoàng Văn E", "gv.hoangvane@hcmute.edu.vn", "GV005", "ThS", "Thạc sĩ",
                cntt, teacherRole);

        // 5. Seed Sample Students
        seedStudentIfAbsent("sv_nguyenvanf", "Nguyễn Văn F", "sv.nguyenvanf@student.hcmute.edu.vn", "SV2001",
                "ĐH KTPM16A", "2022-2026", ktpm, studentRole);
        seedStudentIfAbsent("sv_tranvang", "Trần Văn G", "sv.tranvang@student.hcmute.edu.vn", "SV2002", "ĐH KTPM16A",
                "2022-2026", ktpm, studentRole);
        seedStudentIfAbsent("sv_lethih", "Lê Thị H", "sv.lethih@student.hcmute.edu.vn", "SV2003", "ĐH KTPM16A",
                "2022-2026", ktpm, studentRole);
        seedStudentIfAbsent("sv_phamvani", "Phạm Văn I", "sv.phamvani@student.hcmute.edu.vn", "SV2004", "ĐH HTTT16A",
                "2022-2026", httt, studentRole);
        seedStudentIfAbsent("sv_hoangvank", "Hoàng Văn K", "sv.hoangvank@student.hcmute.edu.vn", "SV2005", "ĐH HTTT16A",
                "2022-2026", httt, studentRole);

        // 6. Seed Default Registration Period if none exists
        if (periodRepository.count() == 0) {
            RegistrationPeriod defaultPeriod = RegistrationPeriod.builder()
                    .name("Đợt đăng ký khóa luận tốt nghiệp lần 1 (2026-2027)")
                    .periodType(com.hcmute.topicmanagement.enums.PeriodType.KLTN)
                    .teacherStartDate(java.time.LocalDateTime.now().minusDays(10))
                    .teacherEndDate(java.time.LocalDateTime.now().plusDays(10))
                    .studentStartDate(java.time.LocalDateTime.now().minusDays(5))
                    .studentEndDate(java.time.LocalDateTime.now().plusDays(15))
                    .reviewSubmissionDeadline(java.time.LocalDateTime.now().plusDays(25))
                    .councilReportDate(java.time.LocalDateTime.now().plusDays(30))
                    .status(com.hcmute.topicmanagement.enums.PeriodStatus.PHASE2_STUDENT_REG)
                    .build();
            periodRepository.save(defaultPeriod);
            log.info("Created default active Registration Period for testing.");
        }

        log.info("Data initial seeding completed successfully!");
    }

    private Role seedRoleIfAbsent(RoleName roleName) {
        return roleRepository.findByName(roleName).orElseGet(() -> {
            Role role = Role.builder().name(roleName).build();
            return roleRepository.save(role);
        });
    }

    private Department seedDepartmentIfAbsent(String code, String name, String desc) {
        return departmentRepository.findByCode(code).orElseGet(() -> {
            Department d = Department.builder().code(code).name(name).description(desc).build();
            return departmentRepository.save(d);
        });
    }

    private void seedTeacherIfAbsent(String username, String fullName, String email, String code, String rank,
            String degree, Department dept, Role role) {
        if (!userRepository.existsByUsername(username)) {
            User u = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode("123456"))
                    .fullName(fullName)
                    .email(email)
                    .phone("0912345678")
                    .enabled(true)
                    .role(role)
                    .build();
            u = userRepository.save(u);

            Teacher t = Teacher.builder()
                    .user(u)
                    .teacherCode(code)
                    .academicRank(rank)
                    .degree(degree)
                    .department(dept)
                    .build();
            teacherRepository.save(t);
            log.info("Created Teacher user: username={} / pass=123456", username);
        }
    }

    private void seedStudentIfAbsent(String username, String fullName, String email, String code, String className,
            String year, Department dept, Role role) {
        if (!userRepository.existsByUsername(username)) {
            User u = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode("123456"))
                    .fullName(fullName)
                    .email(email)
                    .phone("0987654321")
                    .enabled(true)
                    .role(role)
                    .build();
            u = userRepository.save(u);

            Student s = Student.builder()
                    .user(u)
                    .studentCode(code)
                    .className(className)
                    .academicYear(year)
                    .department(dept)
                    .build();
            studentRepository.save(s);
            log.info("Created Student user: username={} / pass=123456", username);
        }
    }
}
